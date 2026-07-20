# CloudWatch Egress Progress Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Show an accurate AWS 100GB egress progress bar and keep the bottom monitor cards compact.

**Architecture:** Pass the existing AWS `traffic` snapshot into the CloudWatch card and derive the outbound percentage with the existing `percent` helper. Keep ingress outside the quota calculation and change only semantic labels and layout CSS.

**Tech Stack:** React 19, CSS, Vitest, Testing Library, Vite

## Global Constraints

- Do not combine ingress and egress for the 100GB allowance.
- Do not fabricate a percentage when traffic data is unavailable.
- Reuse `uiTheme` semantic CSS variables and existing status tones.
- Preserve unrelated dirty work.

---

### Task 1: Add egress quota presentation

**Files:**
- Modify: `AIProvider-front/src/MonitorCenter.test.jsx`
- Modify: `AIProvider-front/src/MonitorCenter.jsx`

**Interfaces:**
- Consumes: `server.traffic.usedBytes`, `totalBytes`, and `remainingBytes`.
- Produces: accessible `progressbar` named `免费出站额度`.

- [ ] Add a failing test for user-perspective labels, 20% egress usage, 80GB remaining, and progressbar semantics.
- [ ] Run `npm test -- --run src/MonitorCenter.test.jsx` and verify the expected failure.
- [ ] Pass `traffic` into `Network` and render the quota bar using only egress values.
- [ ] Re-run the focused test and verify it passes.

### Task 2: Compact the capacity cards

**Files:**
- Modify: `AIProvider-front/src/MonitorCenterEnhancements.css`
- Modify: `AIProvider-front/src/uiGate.test.js`

**Interfaces:**
- Produces: content-height capacity grid with top-aligned cards on desktop and mobile.

- [ ] Add a UI-gate assertion for `height:auto`/top alignment and verify RED.
- [ ] Remove the remaining-row stretch and align the capacity grid/cards to the start.
- [ ] Run `MonitorCenter.test.jsx`, `uiGate.test.js`, and `npm run build`.
- [ ] Validate `/admin/monitor` at desktop and mobile widths with rendered screenshots and console checks.
