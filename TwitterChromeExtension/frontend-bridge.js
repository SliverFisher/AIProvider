const REQUEST = "AIPROVIDER_X_EXTENSION_REQUEST";
const RESPONSE = "AIPROVIDER_X_EXTENSION_RESPONSE";

window.postMessage({ type: "AIPROVIDER_X_EXTENSION_READY" }, "*");

window.addEventListener("message", async (event) => {
  if (event.source !== window || event.data?.type !== REQUEST) return;
  const { requestId, action, payload } = event.data;
  if (!requestId || !["STATUS", "CONNECT", "CONFIGURE", "POLL_NOW", "PUBLISH"].includes(action)) return;
  try {
    const result = await chrome.runtime.sendMessage({ action, payload });
    window.postMessage({ type: RESPONSE, requestId, result }, "*");
  } catch (error) {
    window.postMessage({
      type: RESPONSE,
      requestId,
      result: { success: false, message: error?.message || "Chrome 扩展通信失败" }
    }, "*");
  }
});
