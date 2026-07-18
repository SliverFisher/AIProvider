package com.aiprovider.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TwitterPublicWebClient {
    private static final Pattern STATUS = Pattern.compile("/status/([0-9]+)");
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    private final ObjectMapper json;
    private final boolean headless;
    private final double timeoutMs;
    private final String executable;

    public TwitterPublicWebClient(
            ObjectMapper json,
            @Value("${twitter-source.headless:true}") boolean headless,
            @Value("${twitter-source.navigation-timeout-ms:60000}") long timeoutMs,
            @Value("${twitter-source.browser-executable-path:}") String executable) {
        this.json = json;
        this.headless = headless;
        this.timeoutMs = timeoutMs;
        this.executable = executable == null ? "" : executable.trim();
    }

    public TwitterFetchedPost fetchLatest(String handle) {
        try (Playwright playwright = Playwright.create();
             Browser browser = launch(playwright);
             BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                     .setViewportSize(1280, 720)
                     .setLocale("zh-CN")
                     .setUserAgent(USER_AGENT))) {
            context.setDefaultTimeout(timeoutMs);
            Page page = context.newPage();
            page.navigate("https://x.com/" + handle, new Page.NavigateOptions().setTimeout(timeoutMs));
            page.waitForTimeout(1500);
            if (page.url().contains("/i/flow/login")) {
                throw new ContentSourceException("TWITTER_LOGIN_REQUIRED", "X 对当前服务器要求登录，公开网页采集不可用；请改用 Twitter API Bearer 适配器");
            }

            Locator articles = page.locator("article[data-testid='tweet']");
            articles.first().waitFor(new Locator.WaitForOptions().setTimeout(timeoutMs));
            Locator article = latestUnpinned(articles);
            Locator statusLink = article.locator("a[href*='/status/']").first();
            String href = statusLink.getAttribute("href");
            Matcher matcher = STATUS.matcher(href == null ? "" : href);
            if (!matcher.find()) {
                throw new ContentSourceException("TWITTER_POST_ID_MISSING", "公开页面没有返回可识别的最新推文 ID");
            }

            String id = matcher.group(1);
            Locator textNode = article.locator("[data-testid='tweetText']").first();
            String text = textNode.count() > 0 ? textNode.innerText().trim() : "";
            if (text.isEmpty()) {
                throw new ContentSourceException("TWITTER_TEXT_MISSING", "最新推文没有可采集的文字内容");
            }

            LocalDateTime publishedAt = publishedAt(article);
            ObjectNode raw = json.createObjectNode();
            raw.put("adapter", "TWITTER_WEB");
            raw.put("handle", handle);
            raw.put("id", id);
            raw.put("text", text);
            raw.put("pageUrl", page.url());
            return new TwitterFetchedPost(id, text, "https://x.com/" + handle + "/status/" + id, publishedAt, raw);
        } catch (ContentSourceException e) {
            throw e;
        } catch (PlaywrightException e) {
            throw new ContentSourceException("TWITTER_WEB_UNAVAILABLE", "Twitter 公开网页采集失败：" + safe(e), e);
        }
    }

    private Locator latestUnpinned(Locator articles) {
        int count = Math.min(articles.count(), 10);
        for (int index = 0; index < count; index++) {
            Locator article = articles.nth(index);
            Locator socialContext = article.locator("[data-testid='socialContext']");
            String context = socialContext.count() == 0 ? "" : socialContext.first().innerText();
            if (!context.contains("置顶") && !context.toLowerCase().contains("pinned")) return article;
        }
        throw new ContentSourceException("TWITTER_LATEST_POST_MISSING", "公开页面没有返回可识别的非置顶推文");
    }

    private LocalDateTime publishedAt(Locator article) {
        Locator time = article.locator("time").first();
        if (time.count() == 0) return null;
        try {
            return OffsetDateTime.parse(time.getAttribute("datetime")).toLocalDateTime();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private Browser launch(Playwright playwright) {
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setArgs(Arrays.asList("--disable-dev-shm-usage", "--no-sandbox"));
        if (!executable.isEmpty()) options.setExecutablePath(java.nio.file.Paths.get(executable));
        return playwright.chromium().launch(options);
    }

    private String safe(Exception e) {
        String value = e.getMessage();
        if (value == null || value.trim().isEmpty()) return e.getClass().getSimpleName();
        int line = value.indexOf('\n');
        value = line < 0 ? value : value.substring(0, line);
        return value.length() > 300 ? value.substring(0, 300) : value;
    }
}
