// @vitest-environment jsdom
import React from "react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import FileTransfer from "./FileTransfer";

const jsonResponse = (data) => Promise.resolve({ ok: true, status: 200, json: () => Promise.resolve({ code: 200, data }) });

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
  delete globalThis.XMLHttpRequest;
});

describe("FileTransfer", () => {
  it("loads files and refreshes after deletion", async () => {
    const fetchMock = vi.spyOn(globalThis, "fetch").mockImplementation((url, options = {}) => {
      if (url === "/api/file-transfer/files") return jsonResponse([{ fileName: "设备文件.txt", fileSize: 2048, uploadedAt: "2026-07-19T01:02:03Z" }]);
      if (url === "/api/file-transfer/%E8%AE%BE%E5%A4%87%E6%96%87%E4%BB%B6.txt" && options.method === "DELETE") return jsonResponse({ deleted: "设备文件.txt" });
      throw new Error(`unexpected request: ${url}`);
    });
    vi.spyOn(window, "confirm").mockReturnValue(true);

    render(<FileTransfer />);
    expect(await screen.findByText("设备文件.txt")).toBeTruthy();
    expect(screen.getByText("2.00 KB")).toBeTruthy();
    fireEvent.click(screen.getByRole("button", { name: /删除/ }));
    expect(await screen.findByText("已删除 设备文件.txt")).toBeTruthy();
    expect(fetchMock).toHaveBeenCalledTimes(3);
  });

  it("reports upload progress and refreshes the list", async () => {
    let listCalls = 0;
    let activeRequest;
    vi.spyOn(globalThis, "fetch").mockImplementation((url) => {
      if (url !== "/api/file-transfer/files") throw new Error(`unexpected request: ${url}`);
      listCalls += 1;
      return jsonResponse(listCalls === 1 ? [] : [{ fileName: "large.bin", fileSize: 4, uploadedAt: "2026-07-19T01:02:03Z" }]);
    });
    class FakeRequest {
      constructor() { this.upload = {}; this.status = 200; this.response = { code: 200, data: {} }; }
      open(method, url) { this.method = method; this.url = url; }
      send(body) {
        expect(this.method).toBe("POST");
        expect(this.url).toBe("/api/file-transfer/upload");
        expect(body.get("file").name).toBe("large.bin");
        activeRequest = this;
        this.upload.onprogress({ lengthComputable: true, loaded: 2, total: 4 });
      }
    }
    globalThis.XMLHttpRequest = FakeRequest;

    render(<FileTransfer />);
    await screen.findByText("服务器文件夹为空");
    fireEvent.change(document.querySelector('input[type="file"]'), { target: { files: [new File(["data"], "large.bin")] } });
    await waitFor(() => expect(screen.getByRole("progressbar", { name: "large.bin 上传进度" }).value).toBe(50));
    activeRequest.onload();
    expect(await screen.findByText("已上传 1 个文件")).toBeTruthy();
    expect(await screen.findByText("large.bin")).toBeTruthy();
    await waitFor(() => expect(listCalls).toBe(2));
  });
});
