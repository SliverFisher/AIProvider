// @vitest-environment jsdom
import React from "react";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import PromptManager from "./PromptManager";

const catalog = {
  generalNegativePrompt: "low quality, bad hands",
  categories: [
    { category: "Character", label: "人物", sortOrder: 1, multiple: true },
    { category: "Clothing", label: "服装", sortOrder: 2, multiple: true },
    { category: "Quality", label: "画质词", sortOrder: 3, multiple: true },
  ],
  options: [
    { id: "solo", category: "Character", name: "单人", positivePrompt: "solo", negativePrompt: "crowd", allowMultiple: true },
    { id: "girl", category: "Character", name: "女孩", positivePrompt: "1girl", negativePrompt: "male", allowMultiple: true },
    { id: "black_pantyhose", category: "Clothing", name: "黑丝袜 / 黑色连裤袜", positivePrompt: "black pantyhose, sheer black tights", negativePrompt: "bare legs", allowMultiple: true },
    { id: "masterpiece", category: "Quality", name: "杰作", positivePrompt: "masterpiece", negativePrompt: "low quality", allowMultiple: true },
  ],
};
const response = (data) => new Response(JSON.stringify({ code: 200, data }), { status: 200, headers: { "Content-Type": "application/json" } });

describe("PromptManager", () => {
  let saved;
  beforeEach(() => {
    saved = null;
    window.history.replaceState({}, "", "/prompts");
    vi.stubGlobal("fetch", vi.fn(async (input, options = {}) => {
      const url = String(input);
      if (url === "/api/prompt-options/config") return response({ generalNegativePrompt: catalog.generalNegativePrompt, categories: catalog.categories });
      if (url === "/api/prompt-options/resolve") {
        const ids = JSON.parse(options.body); return response(catalog.options.filter((option) => ids.includes(option.id)));
      }
      if (url.startsWith("/api/prompt-options?")) {
        const params = new URL(url, "http://local").searchParams;
        const keyword = (params.get("query") || "").toLowerCase();
        const category = params.get("category");
        const items = catalog.options.filter((option) => (!category || option.category === category) && (!keyword || [option.id, option.name, option.positivePrompt].join(" ").toLowerCase().includes(keyword)));
        return response({ items, total: items.length, pages: items.length ? 1 : 0 });
      }
      if (url === "/api/prompt-translations/prose") return response({ positivePrompt: "一名女子走过雨夜城市。", negativePrompt: "不要出现文字。" });
      if (options.method === "POST") { saved = JSON.parse(options.body); return response({ id: 9 }); }
      return response([]);
    }));
  });
  afterEach(() => { cleanup(); vi.unstubAllGlobals(); });

  it("builds and saves a complete structured scheme", async () => {
    render(<PromptManager />);
    await screen.findByLabelText("搜索人物");
    fireEvent.change(screen.getByLabelText("方案名称"), { target: { value: "结构化方案" } });
    fireEvent.change(screen.getByLabelText("搜索人物"), { target: { value: "单人" } });
    fireEvent.click(await screen.findByRole("button", { name: /单人/ }));
    fireEvent.change(screen.getByLabelText("搜索人物"), { target: { value: "女孩" } });
    fireEvent.click(await screen.findByRole("button", { name: /女孩/ }));
    expect(screen.getByLabelText("最终正向 Prompt").value).toBe("solo, 1girl");
    expect(screen.getByLabelText("最终反向 Prompt").value).toBe("low quality, bad hands, crowd, male");
    fireEvent.change(screen.getByLabelText("最终正向 Prompt"), { target: { value: "temporary final edit" } });
    fireEvent.click(screen.getByRole("button", { name: "保存" }));
    await waitFor(() => expect(saved).not.toBeNull());
    expect(saved).toMatchObject({
      name: "结构化方案", selectedOptions: { Character: ["solo", "girl"] },
      promptMode: "tags",
      positiveExtra: "temporary final edit", negativeExtra: "", positivePrompt: "temporary final edit",
      negativePrompt: "low quality, bad hands, crowd, male", remark: "", isDefault: false,
    });
    expect(saved).not.toHaveProperty("parameters");
    expect(screen.queryByLabelText("备注")).toBeNull();
  });

  it("searches Chinese and English Prompt text in the redesigned picker", async () => {
    render(<PromptManager />);
    const search = await screen.findByLabelText("搜索服装");
    fireEvent.focus(search);
    expect(await screen.findByText("常用词条")).toBeTruthy();
    fireEvent.change(search, { target: { value: "黑丝袜" } });
    expect(await screen.findByRole("button", { name: /黑丝袜/ })).toBeTruthy();
    fireEvent.change(search, { target: { value: "pantyhose" } });
    expect(screen.getByRole("button", { name: /black pantyhose/ })).toBeTruthy();
    fireEvent.pointerDown(document.body);
    expect(screen.queryByText("找到 1 项")).toBeNull();
  });

  it("restores every saved selection and final Prompt from the edit query", async () => {
    const selectedOptions = { Quality: ["masterpiece"] };
    const preset = { id: 7, name: "已保存", promptMode: "tags", selectedOptions, positiveExtra: "extra", negativeExtra: "", positivePrompt: "saved final", negativePrompt: "saved negative", remark: "memo", isDefault: true };
    window.history.replaceState({}, "", "/prompts?edit=7");
    fetch.mockImplementation(async (input, options = {}) => {
      const url = String(input);
      if (url === "/api/prompt-options/config") return response({ generalNegativePrompt: catalog.generalNegativePrompt, categories: catalog.categories });
      if (url === "/api/prompt-options/resolve") {
        const ids = JSON.parse(options.body); return response(catalog.options.filter((option) => ids.includes(option.id)));
      }
      return response([preset]);
    });
    render(<PromptManager />);
    expect(await screen.findByDisplayValue("已保存")).toBeTruthy();
    expect(screen.getByText("杰作")).toBeTruthy();
    expect(screen.getByLabelText("最终正向 Prompt").value).toBe("saved final");
    expect(screen.getByLabelText("是否默认方案").checked).toBe(true);
  });

  it("does not download the full catalog when the page opens", async () => {
    render(<PromptManager />);
    await waitFor(() => expect(fetch).toHaveBeenCalledWith("/api/comfy-presets", expect.objectContaining({ signal: expect.any(AbortSignal) })));
    expect(fetch.mock.calls.some(([url]) => String(url) === "/api/prompt-catalog")).toBe(false);
    expect(fetch.mock.calls.some(([url]) => String(url).includes("category="))).toBe(false);
    fireEvent.focus(screen.getByLabelText("搜索人物"));
    await waitFor(() => expect(fetch.mock.calls.some(([url]) => String(url).includes("category=Character"))).toBe(true));
  });

  it("separates prose schemes, saves paragraphs, and previews a full Chinese translation", async () => {
    render(<PromptManager />);
    await screen.findByLabelText("方案名称");
    fireEvent.change(screen.getByLabelText("方案名称"), { target: { value: "Flux 长文方案" } });
    fireEvent.click(screen.getByRole("radio", { name: /长文式/ }));
    expect(screen.queryByLabelText("全局搜索 Prompt 词条")).toBeNull();
    fireEvent.change(screen.getByLabelText("长文正向描述"), { target: { value: "A woman walks through a rainy city at night.\nNeon light reflects on the street." } });
    fireEvent.change(screen.getByLabelText("长文反向约束"), { target: { value: "Do not include text." } });
    fireEvent.click(screen.getByRole("button", { name: "翻译为中文" }));
    expect(await screen.findByDisplayValue("一名女子走过雨夜城市。")).toBeTruthy();
    fireEvent.click(screen.getByRole("button", { name: "保存" }));
    await waitFor(() => expect(saved).not.toBeNull());
    expect(saved).toMatchObject({ name: "Flux 长文方案", promptMode: "prose", selectedOptions: {}, positiveExtra: "", negativeExtra: "", negativePrompt: "Do not include text." });
    expect(saved.positivePrompt).toContain("\nNeon light");
  });

  it("cancels in-flight Prompt requests when switching workspaces", async () => {
    const signals = [];
    fetch.mockImplementation((_input, options = {}) => { signals.push(options.signal); return new Promise(() => {}); });
    const view = render(<PromptManager />);
    await waitFor(() => expect(signals).toHaveLength(1));
    view.unmount();
    expect(signals.every((signal) => signal.aborted)).toBe(true);
  });
});
