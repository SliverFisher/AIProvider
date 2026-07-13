// @vitest-environment jsdom
import React from "react";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import TwitterPublisher from "./TwitterPublisher";

const result = (data) => new Response(JSON.stringify({ code: 200, message: "success", data }), {
  status: 200, headers: { "Content-Type": "application/json" },
});

describe("Twitter failed task preview", () => {
  beforeEach(() => {
    vi.stubGlobal("confirm", vi.fn(() => true));
    vi.stubGlobal("fetch", vi.fn(async (input) => {
      const url = String(input);
      if (url.endsWith("/api/twitter/accounts")) return result([{ id: 2, username: "tester", sessionStatus: "CONNECTED" }]);
      if (url.includes("/api/twitter/posts?")) return result([{
        id: 9, accountId: 2, username: "tester", content: "", status: "FAILED",
        attemptCount: 1, errorMessage: "发布失败", createdAt: "2026-07-13T20:00:00",
        images: [{ id: 3, assetId: 12, originalFileName: "failed.png" }],
      }]);
      if (url.endsWith("/api/twitter/posts/9/cancel")) return result(null);
      throw new Error(`Unexpected request: ${url}`);
    }));
  });

  afterEach(() => { cleanup(); vi.unstubAllGlobals(); });

  it("previews a failed task directly by backend media id", async () => {
    render(<TwitterPublisher />);
    expect(await screen.findByText("发布失败")).toBeTruthy();
    expect(screen.getByRole("img", { name: "failed.png" }).getAttribute("src")).toBe("/api/twitter/posts/9/images/3");
    fireEvent.click(screen.getByTitle("点击预览 failed.png"));
    expect(await screen.findAllByRole("img", { name: "failed.png" })).toHaveLength(2);
    expect(fetch).not.toHaveBeenCalledWith(expect.stringContaining("127.0.0.1:32145"), expect.anything());
  });

  it("cancels a failed task through the backend api", async () => {
    render(<TwitterPublisher />);
    fireEvent.click(await screen.findByRole("button", { name: "取消任务" }));
    await waitFor(() => expect(fetch).toHaveBeenCalledWith("/api/twitter/posts/9/cancel", { method: "POST" }));
  });
});
