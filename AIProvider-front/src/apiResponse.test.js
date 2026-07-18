import { describe, expect, it, vi } from "vitest";
import { readJsonResponse } from "./apiResponse";

describe("readJsonResponse", () => {
  it("returns a valid JSON payload", async () => {
    const payload = { code: 200, data: { ready: true } };
    await expect(readJsonResponse({ json: vi.fn().mockResolvedValue(payload), status: 200 })).resolves.toBe(payload);
  });

  it("turns invalid server JSON into a localized actionable error", async () => {
    const response = { json: vi.fn().mockRejectedValue(new SyntaxError("Unexpected end of JSON input")), status: 502 };
    await expect(readJsonResponse(response, "内容运营服务响应异常")).rejects.toThrow("内容运营服务响应异常 · HTTP 502");
  });
});
