using Microsoft.Playwright;
using System.Text.Json;
using System.Text.Json.Nodes;

sealed class TwitterLocalPublisher
{
    private readonly BridgeSettings settings;
    private readonly SemaphoreSlim operationLock = new(1, 1);
    private readonly string profileDirectory;
    private readonly string accountFile;

    public TwitterLocalPublisher(BridgeSettings settings)
    {
        this.settings = settings;
        profileDirectory = string.IsNullOrWhiteSpace(settings.TwitterProfileDirectory)
            ? Path.Combine(AppContext.BaseDirectory, "TwitterProfile")
            : Path.GetFullPath(settings.TwitterProfileDirectory);
        accountFile = Path.Combine(profileDirectory, "account.json");
    }

    public TwitterLocalStatus Status()
    {
        try
        {
            if (!File.Exists(accountFile)) return new(false, null, "DISCONNECTED", null);
            var account = JsonSerializer.Deserialize<TwitterAccountState>(File.ReadAllText(accountFile));
            return account == null
                ? new(false, null, "DISCONNECTED", null)
                : new(true, account.Username, "CONNECTED", account.ConnectedAt);
        }
        catch { return new(false, null, "DISCONNECTED", null); }
    }

    public async Task<TwitterLocalStatus> ConnectAsync(TwitterLocalLoginRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.Username) || string.IsNullOrWhiteSpace(request.Password))
            throw new InvalidOperationException("Twitter 用户名和密码不能为空");
        await operationLock.WaitAsync();
        try
        {
            Directory.CreateDirectory(profileDirectory);
            using var playwright = await Playwright.CreateAsync();
            // X frequently suppresses the login form in headless browsers. Keep
            // account connection visible so the user can also complete any
            // consent, CAPTCHA or risk-control prompt that X presents.
            await using var context = await LaunchAsync(playwright, false);
            var page = context.Pages.FirstOrDefault() ?? await context.NewPageAsync();
            var loginResponse = await page.GotoAsync("https://x.com/i/flow/login", new() {
                Timeout = 60000,
                WaitUntil = WaitUntilState.DOMContentLoaded
            });
            if (loginResponse != null && loginResponse.Status >= 400)
                throw new InvalidOperationException($"X 登录页返回 HTTP {loginResponse.Status}，请检查本机代理或在弹出的 Chrome 中刷新");
            var username = page.Locator("input[autocomplete='username'], input[name='text'], input[data-testid='ocfEnterTextTextInput']").First;
            try { await username.WaitForAsync(new() { Timeout = 60000 }); }
            catch {
                var title = await page.TitleAsync();
                throw new InvalidOperationException($"X 登录页未显示用户名输入框；当前页面：{page.Url}，标题：{title}");
            }
            await username.FillAsync(NormalizeUsername(request.Username));
            await username.PressAsync("Enter");
            await WaitForLoginStep(page);
            if (await Visible(page, "input[data-testid='ocfEnterTextTextInput']") && !await Visible(page, "input[name='password']"))
            {
                if (string.IsNullOrWhiteSpace(request.EmailOrPhone))
                    throw new InvalidOperationException("X 要求确认邮箱、手机号或用户名");
                var identity = page.Locator("input[data-testid='ocfEnterTextTextInput']").First;
                await identity.FillAsync(request.EmailOrPhone.Trim());
                await identity.PressAsync("Enter");
                await WaitFor(page, "input[name='password']", 60000);
            }
            var password = page.Locator("input[name='password']").First;
            if (!await password.IsVisibleAsync()) throw new InvalidOperationException("X 未进入密码登录步骤");
            await password.FillAsync(request.Password);
            await password.PressAsync("Enter");
            await WaitForAuthOrChallenge(context, page, 180000);
            if (!await Authenticated(context) && await Visible(page, "input[data-testid='ocfEnterTextTextInput']"))
            {
                if (string.IsNullOrWhiteSpace(request.VerificationCode))
                    throw new InvalidOperationException("X 要求登录验证码，请填写后重试");
                var code = page.Locator("input[data-testid='ocfEnterTextTextInput']").First;
                await code.FillAsync(request.VerificationCode.Trim());
                await code.PressAsync("Enter");
                await WaitForAuthentication(context, page, 180000);
            }
            if (!await Authenticated(context)) throw new InvalidOperationException("X 登录失败，可能需要 CAPTCHA 或人工验证");
            var state = new TwitterAccountState(NormalizeUsername(request.Username), DateTimeOffset.Now);
            await File.WriteAllTextAsync(accountFile, JsonSerializer.Serialize(state));
            return new(true, state.Username, "CONNECTED", state.ConnectedAt);
        }
        finally { operationLock.Release(); }
    }

    public async Task<TwitterLocalPublishResult> PublishAsync(string content, IReadOnlyList<string> imagePaths)
    {
        var status = Status();
        if (!status.Connected || string.IsNullOrWhiteSpace(status.Username))
            throw new InvalidOperationException("本机尚未连接 Twitter 账号");
        await operationLock.WaitAsync();
        try
        {
            using var playwright = await Playwright.CreateAsync();
            await using var context = await LaunchAsync(playwright, settings.TwitterHeadless);
            if (!await Authenticated(context))
            {
                TryDeleteAccountFile();
                throw new InvalidOperationException("本机 Twitter 登录会话已过期，请重新连接");
            }
            var page = context.Pages.FirstOrDefault() ?? await context.NewPageAsync();
            await page.GotoAsync("https://x.com/compose/post", new() { Timeout = 60000 });
            if (page.Url.Contains("/i/flow/login", StringComparison.OrdinalIgnoreCase))
            {
                TryDeleteAccountFile();
                throw new InvalidOperationException("本机 Twitter 登录会话已过期，请重新连接");
            }
            var editor = page.Locator("[data-testid='tweetTextarea_0']").First;
            await editor.WaitForAsync(new() { Timeout = 60000 });
            if (!string.IsNullOrWhiteSpace(content)) await editor.FillAsync(content);
            if (imagePaths.Count > 0)
            {
                var fileInput = page.Locator("input[data-testid='fileInput'], input[type='file']").First;
                await fileInput.SetInputFilesAsync(imagePaths.ToArray());
                await page.WaitForTimeoutAsync(3000);
            }
            var responseTask = page.WaitForResponseAsync(response => response.Url.Contains("CreateTweet", StringComparison.OrdinalIgnoreCase), new() { Timeout = 60000 });
            var send = page.Locator("button[data-testid='tweetButton'], button[data-testid='tweetButtonInline']").First;
            await send.ClickAsync();
            var response = await responseTask;
            var body = await response.TextAsync();
            if (!response.Ok) throw new InvalidOperationException($"X 拒绝发布（HTTP {response.Status}）");
            var root = JsonNode.Parse(body);
            var tweetId = root?["data"]?["create_tweet"]?["tweet_results"]?["result"]?["rest_id"]?.ToString()
                          ?? root?["data"]?["create_tweet"]?["tweet_results"]?["result"]?["tweet"]?["rest_id"]?.ToString();
            var url = string.IsNullOrWhiteSpace(tweetId) ? null : $"https://x.com/{status.Username}/status/{tweetId}";
            return new(true, url, null);
        }
        finally { operationLock.Release(); }
    }

    private async Task<IBrowserContext> LaunchAsync(IPlaywright playwright, bool headless)
    {
        Directory.CreateDirectory(profileDirectory);
        return await playwright.Chromium.LaunchPersistentContextAsync(profileDirectory, new()
        {
            Headless = headless,
            ExecutablePath = ResolveChromePath(),
            Args = new[] { "--disable-dev-shm-usage" }
        });
    }

    private string ResolveChromePath()
    {
        if (!string.IsNullOrWhiteSpace(settings.TwitterChromePath) && File.Exists(settings.TwitterChromePath)) return settings.TwitterChromePath;
        var candidates = OperatingSystem.IsMacOS()
            ? new[] { "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" }
            : new[] {
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles), "Google", "Chrome", "Application", "chrome.exe"),
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86), "Google", "Chrome", "Application", "chrome.exe"),
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "Google", "Chrome", "Application", "chrome.exe")
            };
        return candidates.FirstOrDefault(File.Exists) ?? throw new InvalidOperationException("未找到本机 Google Chrome，请在 Agent 配置 TwitterChromePath");
    }

    private static async Task<bool> Authenticated(IBrowserContext context) =>
        (await context.CookiesAsync()).Any(cookie => cookie.Name == "auth_token" && !string.IsNullOrWhiteSpace(cookie.Value));
    private static async Task<bool> Visible(IPage page, string selector) { try { return await page.Locator(selector).First.IsVisibleAsync(); } catch { return false; } }
    private static async Task WaitFor(IPage page, string selector, int timeout)
    {
        await page.Locator(selector).First.WaitForAsync(new() { Timeout = timeout });
    }
    private static async Task WaitForLoginStep(IPage page)
    {
        var until = DateTime.UtcNow.AddSeconds(60);
        while (DateTime.UtcNow < until)
        {
            if (await Visible(page, "input[name='password']") || await Visible(page, "input[data-testid='ocfEnterTextTextInput']")) return;
            await page.WaitForTimeoutAsync(250);
        }
        throw new InvalidOperationException("等待 X 登录页面超时");
    }
    private static async Task WaitForAuthOrChallenge(IBrowserContext context, IPage page, int timeout)
    {
        var until = DateTime.UtcNow.AddMilliseconds(timeout);
        while (DateTime.UtcNow < until)
        {
            if (await Authenticated(context) || await Visible(page, "input[data-testid='ocfEnterTextTextInput']")) return;
            await page.WaitForTimeoutAsync(250);
        }
    }
    private static async Task WaitForAuthentication(IBrowserContext context, IPage page, int timeout)
    {
        var until = DateTime.UtcNow.AddMilliseconds(timeout);
        while (DateTime.UtcNow < until) { if (await Authenticated(context)) return; await page.WaitForTimeoutAsync(250); }
    }
    private static string NormalizeUsername(string value) => value.Trim().TrimStart('@');
    private void TryDeleteAccountFile() { try { File.Delete(accountFile); } catch { } }
    private sealed record TwitterAccountState(string Username, DateTimeOffset ConnectedAt);
}

sealed record TwitterLocalStatus(bool Connected, string? Username, string Status, DateTimeOffset? ConnectedAt);
sealed record TwitterLocalPublishResult(bool Success, string? TweetUrl, string? ErrorMessage);
sealed class TwitterLocalLoginRequest
{
    public string Username { get; set; } = "";
    public string Password { get; set; } = "";
    public string? EmailOrPhone { get; set; }
    public string? VerificationCode { get; set; }
}
