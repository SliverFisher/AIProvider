// @vitest-environment jsdom
import React from "react";
import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import UiToast from "./UiToast";

afterEach(() => cleanup());

describe("UiToast", () => {
  it("announces transient success without taking over page layout", () => {
    const dismiss = vi.fn();
    render(<UiToast message="文本已发送" tone="success" onDismiss={dismiss} />);
    expect(screen.getByRole("status").classList.contains("is-success")).toBe(true);
    fireEvent.click(screen.getByRole("button", { name: "关闭消息" }));
    expect(dismiss).toHaveBeenCalledTimes(1);
  });

  it("announces errors assertively", () => {
    render(<UiToast message="上传失败" tone="error" />);
    expect(screen.getByRole("alert").textContent).toContain("上传失败");
  });
});
