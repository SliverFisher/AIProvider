# SaveVideo filenamePrefix Binding Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bind `filenamePrefix` for local ComfyUI workflows whose final output is `SaveVideo`.

**Architecture:** Extract output-node recognition into a focused helper used by the existing local workflow builder. Cover `SaveImage` and `SaveVideo` selection with a targeted unit test, then keep the existing binding/default generation path unchanged.

**Tech Stack:** .NET 8, System.Text.Json, xUnit

## Global Constraints

- Do not change unrelated local workflow fields or image-output behavior.
- Run only the ComfyUI Agent tests covering this change.

---

### Task 1: Output node recognition

**Files:**
- Create: `ComfyUIAgent/WorkflowOutputBinding.cs`
- Modify: `ComfyUIAgent/Program.cs`
- Create: `ComfyUIAgent.Tests/ComfyUIAgent.Tests.csproj`
- Create: `ComfyUIAgent.Tests/WorkflowOutputBindingTests.cs`

**Interfaces:**
- Consumes: converted API-format workflow entries.
- Produces: `WorkflowOutputBinding.FindPrimaryOutput(...)` returning the preferred `SaveImage` or `SaveVideo` node.

- [ ] **Step 1: Write the failing test**

Test a `SaveVideo` node titled `SAVE_FINAL_VIDEO` with `filename_prefix` and assert it is selected.

- [ ] **Step 2: Run test to verify it fails**

Run: `dotnet test ComfyUIAgent.Tests/ComfyUIAgent.Tests.csproj --filter SaveVideo`
Expected: FAIL because `WorkflowOutputBinding` does not exist.

- [ ] **Step 3: Write minimal implementation**

Recognize `SaveImage` and `SaveVideo`, prefer titles containing `最终` or `final`, and update `BuildLocalWorkflow` to use the helper.

- [ ] **Step 4: Run test to verify it passes**

Run: `dotnet test ComfyUIAgent.Tests/ComfyUIAgent.Tests.csproj`
Expected: PASS.

- [ ] **Step 5: Verify build**

Run: `dotnet build ComfyUIAgent/ComfyUIAgent.csproj --no-restore`
Expected: Build succeeded.
