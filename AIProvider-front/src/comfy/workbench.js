export const FALLBACK_FORM = {
  workflowId: "futa01",
  positivePrompt: "",
  negativePrompt: "",
  loras: "",
  seed: 1,
  randomSeed: true,
  controlMode: "none",
  styleStrength: 0.45,
  styleWeightType: "style transfer (SDXL)",
  styleEndAt: 0.65,
  combineEmbeds: "average",
  openPoseStrength: 0.8,
  depthStrength: 0.4,
  width: 1080,
  height: 1920,
  batchSize: 1,
  steps: 30,
  cfg: 5,
  denoise: 1,
  secondPassSteps: 22,
  secondPassDenoise: 0.28,
  faceDetailerSteps: 20,
  faceDetailerDenoise: 0.3,
  sampler: "uni_pc",
  scheduler: "normal",
  checkpoint: "",
  generateTransparent: true,
};

export function getWorkflowFieldKeys(workflow) {
  if (!workflow) return [];
  const bindingFields = workflow.binding?.fields;
  const keys = bindingFields && typeof bindingFields === "object" && Object.keys(bindingFields).length
    ? Object.keys(bindingFields)
    : Array.isArray(workflow.fields)
      ? workflow.fields
      : Object.keys(workflow.defaults || {});
  return [...new Set(keys)].filter(
    (key) => !["workflowId", "randomSeed", "generateTransparent"].includes(key),
  );
}

export function createWorkflowForm(workflow) {
  if (!workflow) return { ...FALLBACK_FORM, workflowId: "" };
  const fieldKeys = getWorkflowFieldKeys(workflow);
  const defaults = workflow.defaults || {};
  const values = Object.fromEntries(fieldKeys.map((key) => [key, defaults[key] ?? FALLBACK_FORM[key] ?? ""]));
  return {
    ...values,
    workflowId: workflow.id,
    randomSeed: defaults.randomSeed ?? true,
    generateTransparent: defaults.generateTransparent ?? false,
  };
}

export function getWorkflowRevision(workflow) {
  return JSON.stringify({ modifiedAt: workflow?.modifiedAt, fields: workflow?.fields, defaults: workflow?.defaults, binding: workflow?.binding });
}

export function refreshWorkflowForm(form, workflow, previousRevision) {
  if (form.workflowId === workflow.id && previousRevision === getWorkflowRevision(workflow)) return form;
  return createWorkflowForm(workflow);
}

export function applySchemeToWorkflow(form, scheme, workflow) {
  if (!scheme || !workflow || scheme.workflowId !== workflow.id) return form;
  const allowed = new Set([...getWorkflowFieldKeys(workflow), "randomSeed", "generateTransparent"]);
  const parameters = Object.fromEntries(Object.entries(scheme.parameters || {}).filter(([key]) => allowed.has(key)));
  return { ...form, ...parameters, workflowId: workflow.id };
}

export function findFinalOutput(item, preferredNodeId) {
  if (!item?.outputs) return null;
  if (preferredNodeId && item.outputs[preferredNodeId]?.images?.length) return item.outputs[preferredNodeId];
  const prompt = Array.isArray(item.prompt) ? item.prompt[2] || {} : {};
  const titled = Object.entries(prompt).find(([, node]) =>
    node?.class_type === "SaveImage" && ["最终输出", "保存最终成图"].includes(node?._meta?.title),
  );
  if (titled && item.outputs[titled[0]]?.images?.length) return item.outputs[titled[0]];
  return Object.values(item.outputs).find((output) => output?.images?.length) || null;
}

export function normalizeFolder(folder) {
  const value = String(folder || "").trim();
  return value || "aimaid";
}

export function calculateComfyProgress(payload, promptId) {
  if (!payload || String(payload.promptId || "") !== String(promptId)) return null;
  const nodes = Object.values(payload.nodes || {});
  if (!nodes.length) return null;
  let completed = 0;
  let runningFraction = 0;
  for (const node of nodes) {
    if (node?.state === "finished") completed += 1;
    else if (node?.state === "running") {
      const value = Number(node.value);
      const max = Number(node.max);
      if (Number.isFinite(value) && Number.isFinite(max) && max > 0) {
        runningFraction += Math.max(0, Math.min(1, value / max));
      }
    }
  }
  return Math.max(0, Math.min(99, Math.round(((completed + runningFraction) / nodes.length) * 100)));
}
