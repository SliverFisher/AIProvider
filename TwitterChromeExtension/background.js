const X_HOME = "https://x.com/home";
const CONFIG_MAX_AGE = 6 * 60 * 60 * 1000;
const POLL_ALARM = "aiprovider-twitter-poll";
const LOCAL_AGENT = "http://127.0.0.1:32145";
let polling = false;

chrome.runtime.onInstalled.addListener(() => ensurePollAlarm());
chrome.runtime.onStartup.addListener(() => ensurePollAlarm());
chrome.alarms.onAlarm.addListener((alarm) => {
  if (alarm.name === POLL_ALARM) pollDueTasks().catch(() => {});
});
ensurePollAlarm();

chrome.runtime.onMessage.addListener((message, _sender, sendResponse) => {
  handle(message).then(sendResponse).catch((error) => sendResponse({
    success: false,
    message: error?.message || "扩展直接请求 X 失败"
  }));
  return true;
});

async function handle(message) {
  if (message?.action === "STATUS") return status(null);
  if (message?.action === "CONNECT") return status(message.payload?.username);
  if (message?.action === "CONFIGURE") return configure(message.payload || {});
  if (message?.action === "POLL_NOW") return pollDueTasks();
  if (message?.action === "PUBLISH") return publish(message.payload || {});
  return { success: false, message: "不支持的扩展操作" };
}

async function ensurePollAlarm() {
  const current = await chrome.alarms.get(POLL_ALARM);
  if (!current || current.periodInMinutes !== 1) {
    await chrome.alarms.create(POLL_ALARM, { delayInMinutes: 1, periodInMinutes: 1 });
  }
}

async function configure(payload) {
  const serverBase = normalizeServerBase(payload.serverBase);
  const accountId = Number(payload.accountId);
  if (!serverBase) throw new Error("服务器地址必须是 HTTP 或 HTTPS 地址");
  if (!Number.isSafeInteger(accountId) || accountId <= 0) throw new Error("Twitter 账号 ID 不正确");
  await chrome.storage.local.set({ publisherConfig: { serverBase, accountId } });
  await ensurePollAlarm();
  return { success: true, data: { configured: true, intervalMinutes: 1, serverBase, accountId } };
}

async function pollDueTasks() {
  if (polling) return { success: true, data: { skipped: true, processed: 0 } };
  polling = true;
  try {
    const stored = await chrome.storage.local.get("publisherConfig");
    const config = stored.publisherConfig;
    if (!config?.serverBase || !config?.accountId) {
      return { success: true, data: { configured: false, processed: 0 } };
    }
    const session = await cookieSession(null);
    if (!session.connected) throw new Error("当前 Chrome 没有可用的 X 登录 Session");
    const pending = await api(config, `/api/twitter/posts/pending?accountId=${encodeURIComponent(config.accountId)}&limit=10`);
    let processed = 0;
    const failures = [];
    for (const item of Array.isArray(pending) ? pending : []) {
      try {
        const post = await api(config, `/api/twitter/posts/${item.id}/claim`, { method: "POST" });
        const images = [];
        for (const media of post.images || []) images.push(await taskImage(config, post.id, media));
        const sent = await publish({ content: post.content || "", images });
        await reportClientResult(config, post.id, { success: true, tweetUrl: sent.data?.tweetUrl || null });
        processed++;
      } catch (error) {
        failures.push({ id: item.id, message: error?.message || "发布失败" });
        try {
          await reportClientResult(config, item.id, { success: false, errorMessage: error?.message || "本机扩展发布失败" });
        } catch { /* The task may not have been claimed. */ }
      }
    }
    return { success: true, data: { configured: true, processed, failures } };
  } finally {
    polling = false;
  }
}

async function taskImage(config, postId, media) {
  const response = await fetch(`${config.serverBase}/api/twitter/posts/${postId}/images/${media.id}`, { cache: "no-store" });
  if (!response.ok) throw new Error(`读取图片 ${media.originalFileName || media.id} 失败（HTTP ${response.status}）`);
  const blob = await response.blob();
  return { name: media.originalFileName || `image-${media.id}`, dataUrl: await blobToDataUrl(blob) };
}

async function reportClientResult(config, id, value) {
  return api(config, `/api/twitter/posts/${id}/client-result`, {
    method: "POST", headers: { "content-type": "application/json" }, body: JSON.stringify(value)
  });
}

async function api(config, path, options = {}) {
  const response = await fetch(`${config.serverBase}${path}`, { cache: "no-store", ...options });
  const text = await response.text();
  let value;
  try { value = text ? JSON.parse(text) : null; } catch { value = null; }
  if (!response.ok) throw new Error(value?.message || `服务器请求失败（HTTP ${response.status}）`);
  if (value && value.code != null && value.code !== 200) throw new Error(value.message || "服务器返回失败");
  return value && Object.prototype.hasOwnProperty.call(value, "data") ? value.data : value;
}

async function blobToDataUrl(blob) {
  const bytes = new Uint8Array(await blob.arrayBuffer());
  let binary = "";
  for (let offset = 0; offset < bytes.length; offset += 0x8000) {
    binary += String.fromCharCode(...bytes.subarray(offset, offset + 0x8000));
  }
  return `data:${blob.type || "application/octet-stream"};base64,${btoa(binary)}`;
}

function normalizeServerBase(value) {
  try {
    const url = new URL(String(value || ""));
    return /^(https?:)$/.test(url.protocol) ? url.origin : null;
  } catch { return null; }
}

async function status(usernameHint) {
  const session = await cookieSession(usernameHint);
  return { success: true, data: {
    ready: session.ready,
    connected: session.connected,
    username: session.username,
    status: session.status
  } };
}

async function publish(payload) {
  const content = String(payload.content || "");
  const images = Array.isArray(payload.images) ? payload.images.slice(0, 4) : [];
  if (!content.trim() && !images.length) throw new Error("文字和图片不能同时为空");
  const session = await cookieSession(null);
  if (!session.connected) throw new Error("当前 Chrome 没有 X 登录 Session（缺少 auth_token 或 ct0）");
  const web = await discoverWebClient();
  const headers = authHeaders(web.bearerToken, session.csrfToken);
  const mediaIds = [];
  for (const image of images) mediaIds.push(await uploadImage(image, headers));
  const result = await createTweet(content, mediaIds, web, headers);
  const tweetId = findTweetId(result);
  const tweetUrl = tweetId && session.username ? `https://x.com/${session.username}/status/${tweetId}`
    : tweetId ? `https://x.com/i/status/${tweetId}` : null;
  return { success: true, data: { success: true, tweetUrl, tweetId, username: session.username } };
}

async function cookieSession(usernameHint) {
  const [xAuth, xCsrf, twitterAuth, twitterCsrf, stored] = await Promise.all([
    chrome.cookies.get({ url: "https://x.com/", name: "auth_token" }),
    chrome.cookies.get({ url: "https://x.com/", name: "ct0" }),
    chrome.cookies.get({ url: "https://twitter.com/", name: "auth_token" }),
    chrome.cookies.get({ url: "https://twitter.com/", name: "ct0" }),
    chrome.storage.local.get("twitterUsername")
  ]);
  const authToken = xAuth?.value || twitterAuth?.value || null;
  const csrfToken = xCsrf?.value || twitterCsrf?.value || null;
  const username = normalizeUsername(usernameHint || stored.twitterUsername);
  if (usernameHint && username) await chrome.storage.local.set({ twitterUsername: username });
  return {
    ready: true,
    connected: Boolean(authToken && csrfToken),
    username,
    status: authToken && csrfToken ? "CONNECTED" : "DISCONNECTED",
    csrfToken
  };
}

async function discoverWebClient(force = false) {
  const stored = await chrome.storage.local.get("xWebClientConfig");
  if (!force && stored.xWebClientConfig?.bearerToken && stored.xWebClientConfig?.createTweetQueryId
      && Date.now() - stored.xWebClientConfig.savedAt < CONFIG_MAX_AGE) return stored.xWebClientConfig;
  const homeResponse = await fetch(X_HOME, { credentials: "include", cache: "no-store" });
  if (!homeResponse.ok) throw new Error(`读取 X Web 客户端失败（HTTP ${homeResponse.status}）`);
  const html = await homeResponse.text();
  const scriptUrls = [...html.matchAll(/<script[^>]+src=["']([^"']+\.js[^"']*)["']/gi)]
    .map((match) => absoluteXUrl(match[1])).filter(Boolean);
  let bearerToken = extractBearer(html);
  let createTweetQueryId = extractQueryId(html);
  for (const url of scriptUrls.slice(-35).reverse()) {
    if (bearerToken && createTweetQueryId) break;
    try {
      const response = await fetch(url, { credentials: "include", cache: "force-cache" });
      if (!response.ok) continue;
      const source = await response.text();
      bearerToken ||= extractBearer(source);
      createTweetQueryId ||= extractQueryId(source);
    } catch { /* A nonessential X asset may fail; continue scanning. */ }
  }
  if (!bearerToken) throw new Error("未能从 X Web 资源提取 Bearer Token");
  if (!createTweetQueryId) throw new Error("未能从 X Web 资源提取 CreateTweet Query ID");
  const config = { bearerToken, createTweetQueryId, savedAt: Date.now() };
  await chrome.storage.local.set({ xWebClientConfig: config });
  return config;
}

function extractBearer(source) {
  const encoded = source.match(/Bearer\s+([A-Za-z0-9_%=-]{60,})/)?.[1]
    || source.match(/["'](AAAAA[A-Za-z0-9_%=-]{60,})["']/)?.[1];
  if (!encoded) return null;
  try { return decodeURIComponent(encoded); } catch { return encoded; }
}

function extractQueryId(source) {
  return source.match(/queryId["']?\s*:\s*["']([^"']+)["']\s*,\s*operationName["']?\s*:\s*["']CreateTweet["']/)?.[1]
    || source.match(/operationName["']?\s*:\s*["']CreateTweet["']\s*,\s*queryId["']?\s*:\s*["']([^"']+)["']/)?.[1]
    || null;
}

async function uploadImage(image, headers) {
  if (!image?.dataUrl) throw new Error("待上传图片数据为空");
  const blob = await (await fetch(image.dataUrl)).blob();
  const endpoints = [
    "https://upload.x.com/i/media/upload.json",
    "https://upload.twitter.com/i/media/upload.json"
  ];
  let lastError = "";
  for (const endpoint of endpoints) {
    const form = new FormData();
    form.append("media", blob, image.name || "image.png");
    form.append("media_category", "tweet_image");
    try {
      const response = await fetch(endpoint, { method: "POST", headers, credentials: "include", body: form });
      const text = await response.text();
      if (!response.ok) { lastError = `${endpoint} HTTP ${response.status}: ${short(text)}`; continue; }
      const result = JSON.parse(text);
      const id = result.media_id_string || String(result.media_id || "");
      if (id) return id;
      lastError = `${endpoint} 未返回 media_id`;
    } catch (error) { lastError = `${endpoint}: ${error.message}`; }
  }
  throw new Error(`X 图片上传失败：${lastError}`);
}

async function createTweet(content, mediaIds, web, headers, retried = false) {
  const variables = {
    tweet_text: content,
    dark_request: false,
    media: {
      media_entities: mediaIds.map((media_id) => ({ media_id, tagged_users: [] })),
      possibly_sensitive: false
    },
    semantic_annotation_ids: [],
    disallowed_reply_options: null
  };
  const body = JSON.stringify({ variables, features: createTweetFeatures(), queryId: web.createTweetQueryId });
  const response = await fetch(`https://x.com/i/api/graphql/${web.createTweetQueryId}/CreateTweet`, {
    method: "POST",
    headers: { ...headers, "content-type": "application/json" },
    credentials: "include",
    body
  });
  const text = await response.text();
  if ((!response.ok || /PersistedQueryNotFound|query id/i.test(text)) && !retried) {
    const refreshed = await discoverWebClient(true);
    return createTweet(content, mediaIds, refreshed, authHeaders(refreshed.bearerToken, headers["x-csrf-token"]), true);
  }
  if (!response.ok) throw new Error(`CreateTweet HTTP ${response.status}: ${short(text)}`);
  const result = JSON.parse(text);
  if (result.errors?.length) throw new Error(`CreateTweet: ${result.errors.map((item) => item.message).join("；")}`);
  return result;
}

function authHeaders(bearerToken, csrfToken) {
  return {
    authorization: `Bearer ${bearerToken}`,
    "x-csrf-token": csrfToken,
    "x-twitter-auth-type": "OAuth2Session",
    "x-twitter-client-language": "zh-cn"
  };
}

function createTweetFeatures() {
  return {
    communities_web_enable_tweet_community_results_fetch: true,
    c9s_tweet_anatomy_moderator_badge_enabled: true,
    responsive_web_edit_tweet_api_enabled: true,
    graphql_is_translatable_rweb_tweet_is_translatable_enabled: true,
    view_counts_everywhere_api_enabled: true,
    longform_notetweets_consumption_enabled: true,
    responsive_web_twitter_article_tweet_consumption_enabled: true,
    tweet_awards_web_tipping_enabled: false,
    creator_subscriptions_quote_tweet_preview_enabled: false,
    freedom_of_speech_not_reach_fetch_enabled: true,
    standardized_nudges_misinfo: true,
    tweet_with_visibility_results_prefer_gql_limited_actions_policy_enabled: true,
    rweb_video_timestamps_enabled: true,
    longform_notetweets_rich_text_read_enabled: true,
    longform_notetweets_inline_media_enabled: true,
    profile_label_improvements_pcf_label_in_post_enabled: true,
    responsive_web_grok_share_attachment_enabled: true,
    responsive_web_grok_show_grok_translated_post: false,
    responsive_web_jetfuel_frame: false,
    responsive_web_grok_analyze_post_followups_enabled: true
  };
}

function findTweetId(result) {
  return result?.data?.create_tweet?.tweet_results?.result?.rest_id
    || result?.data?.create_tweet?.tweet_results?.result?.tweet?.rest_id
    || null;
}

function normalizeUsername(value) {
  const result = String(value || "").trim().replace(/^@/, "");
  return /^[A-Za-z0-9_]{1,50}$/.test(result) ? result : null;
}

function absoluteXUrl(value) {
  try { return new URL(value, "https://x.com").href; } catch { return null; }
}

function short(value) { return String(value || "").replace(/\s+/g, " ").slice(0, 300); }
