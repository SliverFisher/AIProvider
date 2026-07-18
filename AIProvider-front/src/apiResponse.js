export async function readJsonResponse(response, message = "服务响应格式异常") {
  try {
    return await response.json();
  } catch {
    const status = Number(response?.status);
    throw new Error(`${message}${status ? ` · HTTP ${status}` : ""}`);
  }
}
