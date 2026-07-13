import { useCallback, useEffect, useState } from "react";
import { ArrowsClockwise, CheckCircle, Clock, Cpu, Gauge, HardDrives, Pulse, Warning } from "@phosphor-icons/react";
import "./MonitorCenter.css";
import "./MonitorCenterEnhancements.css";

const API = "/api/monitor";
async function readSummary() {
  const paths = ["summary", "ai-overview", "ai-timeseries?range=24h"];
  const values = await Promise.all(paths.map(async (path) => {
    const response = await fetch(`${API}/${path}`); if (!response.ok) throw new Error(`HTTP ${response.status}`);
    const json = await response.json(); if (json.code !== 200) throw new Error(json.message || "监控数据读取失败"); return json.data;
  }));
  return { summary: values[0], overview: values[1], timeseries: values[2] || [] };
}
const bytes = (value) => {
  if (value == null) return "—";
  const units = ["B", "KB", "MB", "GB", "TB"];
  let number = Number(value), index = 0;
  while (number >= 1024 && index < units.length - 1) { number /= 1024; index += 1; }
  return `${number.toFixed(index < 2 ? 0 : 2)} ${units[index]}`;
};
const percent = (used, total) => total ? Math.min(100, Math.max(0, used / total * 100)) : 0;
const dateTime = (value) => value ? new Date(value).toLocaleString("zh-CN", { hour12: false }) : "—";

export default function MonitorCenter() {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState("");
  const load = useCallback(async (silent = false) => {
    if (!silent) setRefreshing(true);
    try { setSummary(await readSummary()); setError(""); }
    catch (exception) { setError(exception.message); }
    finally { setLoading(false); setRefreshing(false); }
  }, []);
  useEffect(() => {
    load();
    const timer = setInterval(() => load(true), 60000);
    return () => clearInterval(timer);
  }, [load]);

  if (loading) return <div className="cloud-skeleton"><i /><i /><i /></div>;
  return <section className="cloud-monitor">
    <header className="cloud-toolbar">
      <div><span className="eyebrow">SERVICE · SERVER · TENCENT CLOUD</span><p>服务请求、服务器资源与腾讯云流量</p></div>
      <button onClick={() => load()} disabled={refreshing}><ArrowsClockwise className={refreshing ? "spin" : ""} />{refreshing ? "刷新中" : "手动刷新"}</button>
    </header>
    {error && <div className="cloud-error"><Warning />部分数据暂不可用：{error}</div>}
    <div className={`cloud-health ${summary?.summary?.health?.status === "UP" ? "healthy" : "unhealthy"}`}>
      <Pulse /><div><span>Provider Health</span><strong>{summary?.summary?.health?.status || "UNKNOWN"}</strong><small>检查于 {dateTime(summary?.summary?.health?.checkedAt)}</small></div>
    </div>
    <ServiceRequests overview={summary?.overview} timeseries={summary?.timeseries} />
    <div className="cloud-capacity-grid">
      <Capacity title="服务器内存" icon={Cpu} resource={summary?.summary?.memory} collectedAt={summary?.summary?.collectedAt} />
      <Capacity title="系统磁盘" icon={HardDrives} resource={summary?.summary?.disk} collectedAt={summary?.summary?.collectedAt} />
      <Traffic traffic={summary?.summary?.traffic} />
    </div>
  </section>;
}

function ServiceRequests({ overview = {}, timeseries = [] }) {
  return <section className="service-requests">
    <header><div><h2>服务请求</h2><span>今天汇总 · 最近 24 小时明细</span></div></header>
    <div className="service-kpis">
      <div><Pulse /><span>请求总数</span><strong>{Number(overview.totalRequests || 0).toLocaleString("zh-CN")}</strong></div>
      <div><CheckCircle /><span>成功率</span><strong>{Number(overview.successRate || 0).toFixed(1)}%</strong></div>
      <div><Warning /><span>失败请求</span><strong>{Number(overview.failureCount || 0).toLocaleString("zh-CN")}</strong></div>
      <div><Clock /><span>P95 响应</span><strong>{Number(overview.p95DurationMs || 0).toLocaleString("zh-CN")} ms</strong></div>
    </div>
    <div className="request-table"><table><thead><tr><th>时间</th><th>请求</th><th>失败率</th><th>平均响应</th><th>P95</th></tr></thead><tbody>
      {timeseries.slice(-12).reverse().map((item) => <tr key={item.bucket}><td>{dateTime(item.bucket)}</td><td>{item.totalRequests || 0}</td><td>{Number(item.errorRate || 0).toFixed(1)}%</td><td>{Math.round(Number(item.avgDurationMs || 0))} ms</td><td>{Number(item.p95DurationMs || 0)} ms</td></tr>)}
      {!timeseries.length && <tr><td colSpan="5">最近 24 小时暂无请求</td></tr>}
    </tbody></table></div>
  </section>;
}

function Capacity({ title, icon: Icon, resource, collectedAt }) {
  const usage = percent(resource?.usedBytes, resource?.totalBytes);
  const tone = usage >= 90 ? "danger" : usage >= 70 ? "warning" : "normal";
  return <article className="cloud-card">
    <header><Icon /><div><h2>{title}</h2><span>{resource?.available ? "正常采集" : "不可用"}</span></div><b className={tone}>{resource?.available ? `${usage.toFixed(1)}%` : "—"}</b></header>
    <div className="cloud-main"><strong>{bytes(resource?.usedBytes)}</strong><span>/ {bytes(resource?.totalBytes)}</span></div>
    <div className="cloud-track"><i className={tone} style={{ width: `${usage}%` }} /></div>
    <small>采集时间 {dateTime(collectedAt)}</small>
  </article>;
}

function Traffic({ traffic }) {
  const usage = percent(traffic?.usedBytes, traffic?.totalBytes);
  const tone = usage >= 90 ? "danger" : usage >= 70 ? "warning" : "normal";
  return <article className="cloud-card traffic-card">
    <header><Gauge /><div><h2>本期流量包</h2><span>{traffic?.stale ? "数据可能已过期" : traffic?.status || "不可用"}</span></div><b className={traffic?.stale ? "warning" : tone}>{traffic?.available ? `${usage.toFixed(1)}%` : "—"}</b></header>
    <div className="cloud-main"><strong>{bytes(traffic?.usedBytes)}</strong><span>/ {bytes(traffic?.totalBytes)}</span></div>
    <div className="cloud-track"><i className={tone} style={{ width: `${usage}%` }} /></div>
    <dl><div><dt>剩余</dt><dd>{bytes(traffic?.remainingBytes)}</dd></div><div><dt>超额</dt><dd>{bytes(traffic?.overflowBytes)}</dd></div><div><dt>流量周期</dt><dd>{dateTime(traffic?.periodStart)} — {dateTime(traffic?.periodEnd)}</dd></div></dl>
    <small>腾讯云采集时间 {dateTime(traffic?.collectedAt)}</small>
  </article>;
}
