import { useEffect, useMemo, useRef, useState } from "react";
import { ArrowCounterClockwise, Eraser, ShieldCheck, Trash } from "@phosphor-icons/react";

function parseEditorData(value) {
  try {
    const parsed = JSON.parse(value || "{}");
    return { points: Array.isArray(parsed.points) ? parsed.points : [], bboxes: Array.isArray(parsed.bboxes) ? parsed.bboxes : [] };
  } catch {
    return { points: [], bboxes: [] };
  }
}

export default function MaskPointEditor({ file, value, radius = 12, onChange, onRadiusChange }) {
  const canvasRef = useRef(null);
  const imageRef = useRef(null);
  const drawingRef = useRef(false);
  const lastPointRef = useRef(null);
  const [mode, setMode] = useState(1);
  const [imageReady, setImageReady] = useState(false);
  const data = useMemo(() => parseEditorData(value), [value]);
  const imageUrl = useMemo(() => file ? URL.createObjectURL(file) : "", [file]);

  useEffect(() => () => { if (imageUrl) URL.revokeObjectURL(imageUrl); }, [imageUrl]);

  useEffect(() => {
    const canvas = canvasRef.current;
    const image = imageRef.current;
    if (!canvas || !image || !imageReady) return;
    canvas.width = image.naturalWidth;
    canvas.height = image.naturalHeight;
    const context = canvas.getContext("2d");
    if (!context) return;
    context.clearRect(0, 0, canvas.width, canvas.height);
    context.drawImage(image, 0, 0, canvas.width, canvas.height);
    for (const point of data.points) {
      const pointRadius = Number(point.radius || radius);
      const positive = Number(point.label ?? 1) === 1;
      context.beginPath();
      context.arc(Number(point.x), Number(point.y), pointRadius, 0, Math.PI * 2);
      context.fillStyle = positive ? "rgba(255, 54, 104, .42)" : "rgba(52, 220, 190, .42)";
      context.fill();
      context.lineWidth = Math.max(1, pointRadius * 0.14);
      context.strokeStyle = positive ? "#ff3668" : "#34dcbe";
      context.stroke();
    }
  }, [data.points, imageReady, radius]);

  const commit = (points) => onChange(JSON.stringify({ points, bboxes: data.bboxes }));
  const addPoint = (event) => {
    const canvas = canvasRef.current;
    if (!canvas || !imageReady) return;
    const bounds = canvas.getBoundingClientRect();
    const x = ((event.clientX - bounds.left) / bounds.width) * canvas.width;
    const y = ((event.clientY - bounds.top) / bounds.height) * canvas.height;
    const last = lastPointRef.current;
    const spacing = Math.max(2, Number(radius) * 0.45);
    if (last && Math.hypot(x - last.x, y - last.y) < spacing) return;
    const point = { x: Math.round(x * 100) / 100, y: Math.round(y * 100) / 100, label: mode, radius: Number(radius) };
    lastPointRef.current = point;
    commit([...data.points, point]);
  };
  const start = (event) => { drawingRef.current = true; lastPointRef.current = null; event.currentTarget.setPointerCapture?.(event.pointerId); addPoint(event); };
  const move = (event) => { if (drawingRef.current) addPoint(event); };
  const stop = () => { drawingRef.current = false; lastPointRef.current = null; };

  if (!file) return <div className="mask-editor mask-editor--empty"><strong>区域编辑</strong><span>先选择待处理原图，再在图片上涂抹需要删除的区域。</span></div>;
  return <section className="mask-editor" aria-label="区域编辑器">
    <header><div><strong>区域编辑</strong><small>红色删除，绿色保留</small></div><span>{data.points.length} 个笔触点</span></header>
    <div className="mask-editor__toolbar">
      <button type="button" className={mode === 1 ? "active danger" : ""} onClick={() => setMode(1)}><Eraser />涂抹删除</button>
      <button type="button" className={mode === 0 ? "active protect" : ""} onClick={() => setMode(0)}><ShieldCheck />涂抹保留</button>
      <label>笔刷<input aria-label="区域编辑笔刷大小" type="range" min="2" max="80" value={radius} onChange={(event) => onRadiusChange(Number(event.target.value))} /><span>{radius}px</span></label>
      <button type="button" disabled={!data.points.length} onClick={() => commit(data.points.slice(0, -1))}><ArrowCounterClockwise />撤销</button>
      <button type="button" disabled={!data.points.length} onClick={() => commit([])}><Trash />清空</button>
    </div>
    <div className="mask-editor__stage">
      <img ref={imageRef} src={imageUrl} alt="区域编辑原图" onLoad={() => setImageReady(true)} />
      <canvas ref={canvasRef} aria-label="涂抹删除区域" onPointerDown={start} onPointerMove={move} onPointerUp={stop} onPointerCancel={stop} onPointerLeave={stop} />
    </div>
    <p>按住鼠标或触控笔直接涂抹。至少标记一处删除区域后再开始生成。</p>
  </section>;
}
