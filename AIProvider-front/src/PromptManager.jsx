import { useEffect, useMemo, useState } from "react";
import { Copy, FloppyDisk, Plus, Star, Trash, Warning } from "@phosphor-icons/react";
import { FALLBACK_FORM } from "./comfy/workbench";
import "./PromptManager.css";

const emptyDraft = () => ({ id: null, title: "", workflowId: "futa01", outputFolder: "aimaid", notes: "", defaultPreset: false, parameters: { ...FALLBACK_FORM } });

async function request(path, options) {
  const response = await fetch(path, options);
  const payload = await response.json();
  if (!response.ok || payload.code !== 200) throw new Error(payload.message || `请求失败 · ${response.status}`);
  return payload.data;
}

export default function PromptManager() {
  const [items, setItems] = useState([]);
  const [query, setQuery] = useState("");
  const [draft, setDraft] = useState(emptyDraft);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const filtered = useMemo(() => items.filter((item) => item.title.toLowerCase().includes(query.trim().toLowerCase())), [items, query]);

  const select = (item) => setDraft({ ...item, notes: item.notes || "", parameters: { ...FALLBACK_FORM, ...(item.parameters || {}) } });
  const load = async (preferredId) => {
    const next = await request("/api/comfy-presets");
    setItems(next || []);
    const selected = next?.find((item) => String(item.id) === String(preferredId)) || next?.find((item) => item.defaultPreset) || next?.[0];
    if (selected) select(selected);
  };
  useEffect(() => { load().catch((e) => setError(e.message)); }, []);

  const setRoot = (key, value) => setDraft((current) => ({ ...current, [key]: value }));
  const setParameter = (key, value) => setDraft((current) => ({ ...current, parameters: { ...current.parameters, [key]: value } }));
  const payload = () => ({ title: draft.title.trim(), workflowId: draft.workflowId, outputFolder: draft.outputFolder || "aimaid", notes: draft.notes, parameters: draft.parameters });
  const save = async () => {
    if (!draft.title.trim()) return setError("请填写方案名称");
    setBusy(true); setError("");
    try {
      let preferredId = draft.id;
      if (draft.id) await request(`/api/comfy-presets/${draft.id}`, { method: "PUT", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload()) });
      else {
        const created = await request("/api/comfy-presets", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload()) });
        preferredId = created.id;
      }
      await load(preferredId);
    } catch (e) { setError(e.message); } finally { setBusy(false); }
  };
  const remove = async () => {
    if (!draft.id || !window.confirm(`删除 Prompt 方案“${draft.title}”？`)) return;
    setBusy(true);
    try { await request(`/api/comfy-presets/${draft.id}`, { method: "DELETE" }); setDraft(emptyDraft()); await load(); }
    catch (e) { setError(e.message); } finally { setBusy(false); }
  };
  const markDefault = async () => {
    if (!draft.id) return setError("请先保存方案");
    setBusy(true);
    try { await request(`/api/comfy-presets/${draft.id}/default`, { method: "POST" }); await load(draft.id); }
    catch (e) { setError(e.message); } finally { setBusy(false); }
  };
  const createNew = () => setDraft({ ...emptyDraft(), workflowId: draft.workflowId || "futa01" });
  const duplicate = () => setDraft({ ...draft, id: null, title: `${draft.title || "未命名方案"} - 副本`, defaultPreset: false, parameters: { ...draft.parameters } });
  const p = draft.parameters;

  return <div className="prompt-manager">
    {error && <div className="prompt-manager-error"><Warning />{error}<button onClick={() => setError("")}>×</button></div>}
    <aside className="prompt-scheme-list">
      <header><div><span>PROMPT LIBRARY</span><h2>Prompt 方案</h2></div><button onClick={createNew} title="新建方案"><Plus /></button></header>
      <input className="prompt-search" value={query} onChange={(e) => setQuery(e.target.value)} placeholder="搜索方案名称…" />
      <div className="prompt-list-scroll">
        {filtered.map((item) => <button key={item.id} className={String(item.id) === String(draft.id) ? "active" : ""} onClick={() => select(item)}>
          <span>{item.title}</span>{item.defaultPreset && <Star weight="fill" />}<small>{item.parameters?.width || "-"} × {item.parameters?.height || "-"}</small>
        </button>)}
        {!filtered.length && <p>没有匹配的 Prompt 方案</p>}
      </div>
    </aside>
    <section className="prompt-editor">
      <header><div><span>{draft.id ? `方案 #${draft.id}` : "新方案"}</span><h2>{draft.title || "未命名 Prompt 方案"}</h2></div><div className="prompt-editor-actions">
        <button onClick={createNew}><Plus />新建</button><button onClick={duplicate}><Copy />复制</button><button onClick={save} disabled={busy}><FloppyDisk />保存</button><button onClick={markDefault} disabled={busy || !draft.id} className={draft.defaultPreset ? "is-default" : ""}><Star weight={draft.defaultPreset ? "fill" : "regular"} />设为默认</button><button onClick={remove} disabled={busy || !draft.id} className="danger"><Trash />删除</button>
      </div></header>
      <div className="prompt-editor-form">
        <label className="wide">方案名称<input value={draft.title} maxLength="100" onChange={(e) => setRoot("title", e.target.value)} /></label>
        <label className="wide">正向 Prompt<textarea rows="7" value={p.positivePrompt} onChange={(e) => setParameter("positivePrompt", e.target.value)} /></label>
        <label className="wide">反向 Prompt<textarea rows="5" value={p.negativePrompt} onChange={(e) => setParameter("negativePrompt", e.target.value)} /></label>
        <label>默认宽度<input type="number" min="64" value={p.width} onChange={(e) => setParameter("width", +e.target.value)} /></label>
        <label>默认高度<input type="number" min="64" value={p.height} onChange={(e) => setParameter("height", +e.target.value)} /></label>
        <label>默认 Seed<input type="number" min="0" value={p.seed} onChange={(e) => setParameter("seed", +e.target.value)} /></label>
        <label>默认生成数量<input type="number" min="1" max="16" value={p.batchSize} onChange={(e) => setParameter("batchSize", +e.target.value)} /></label>
        <label className="check"><input type="checkbox" checked={Boolean(p.generateTransparent)} onChange={(e) => setParameter("generateTransparent", e.target.checked)} />默认透明背景</label>
        <label>Steps<input type="number" min="1" value={p.steps} onChange={(e) => setParameter("steps", +e.target.value)} /></label>
        <label>CFG<input type="number" min="0" step="0.5" value={p.cfg} onChange={(e) => setParameter("cfg", +e.target.value)} /></label>
        <label>Sampler<input value={p.sampler || ""} onChange={(e) => setParameter("sampler", e.target.value)} /></label>
        <label>Scheduler<input value={p.scheduler || ""} onChange={(e) => setParameter("scheduler", e.target.value)} /></label>
        <label className="wide">默认 LoRA<textarea rows="3" value={p.loras || ""} onChange={(e) => setParameter("loras", e.target.value)} placeholder="未使用" /></label>
        <label className="wide">默认主模型<input value={p.checkpoint || ""} onChange={(e) => setParameter("checkpoint", e.target.value)} placeholder="由工作流决定" /></label>
        <label className="wide">备注<textarea rows="3" maxLength="1000" value={draft.notes} onChange={(e) => setRoot("notes", e.target.value)} /></label>
      </div>
    </section>
  </div>;
}
