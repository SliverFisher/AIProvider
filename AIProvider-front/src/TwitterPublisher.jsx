import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import {
  ArrowClockwise,
  CheckCircle,
  Clock,
  ImageSquare,
  LinkSimple,
  PaperPlaneTilt,
  Plus,
  ShieldCheck,
  SpinnerGap,
  Trash,
  WarningCircle,
  X,
  XLogo,
} from "@phosphor-icons/react";
import "./TwitterPublisher.css";

const API = "/api/twitter";
const MAX_IMAGES = 4;
const EXTENSION_REQUEST = "AIPROVIDER_X_EXTENSION_REQUEST";
const EXTENSION_RESPONSE = "AIPROVIDER_X_EXTENSION_RESPONSE";

async function request(path, options = {}) {
  const response = await fetch(`${API}${path}`, options);
  let result;
  try { result = await response.json(); }
  catch { throw new Error(`请求失败 · HTTP ${response.status}`); }
  if (!response.ok || result.code !== 200) {
    if (response.status === 404) throw new Error("服务器尚未部署 Twitter 发布接口，请先更新后端服务");
    throw new Error(result.message || `请求失败 · HTTP ${response.status}`);
  }
  return result.data;
}

function extensionRequest(action, payload = {}, timeoutMs = 10000) {
  return new Promise((resolve, reject) => {
    const requestId = globalThis.crypto?.randomUUID?.()
      || `xext-${Date.now()}-${Math.random().toString(36).slice(2)}`;
    const timer = window.setTimeout(() => {
      window.removeEventListener("message", onMessage);
      reject(new Error("未检测到 Chrome 扩展，请先加载 AIProvider X Publisher 扩展"));
    }, timeoutMs);
    function onMessage(event) {
      if (event.source !== window || event.data?.type !== EXTENSION_RESPONSE || event.data.requestId !== requestId) return;
      window.clearTimeout(timer);
      window.removeEventListener("message", onMessage);
      const result = event.data.result;
      if (!result?.success) reject(new Error(result?.message || "Chrome 扩展执行失败"));
      else resolve(result.data ?? result);
    }
    window.addEventListener("message", onMessage);
    window.postMessage({ type: EXTENSION_REQUEST, requestId, action, payload }, "*");
  });
}

const statusMeta = {
  PENDING: { label: "等待发送", tone: "pending", icon: Clock },
  PROCESSING: { label: "正在发送", tone: "processing", icon: SpinnerGap },
  SENT: { label: "发送成功", tone: "sent", icon: CheckCircle },
  FAILED: { label: "发送失败", tone: "failed", icon: WarningCircle },
  CANCELLED: { label: "已取消", tone: "cancelled", icon: X },
};

function formatDate(value) {
  if (!value) return "—";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString("zh-CN", { hour12: false });
}

function formatFileSize(value) {
  const bytes = Number(value || 0);
  if (bytes < 1024 * 1024) return `${Math.max(1, Math.round(bytes / 1024))} KB`;
  return `${(bytes / 1024 / 1024).toFixed(2)} MB`;
}

function connectionLabel(status) {
  if (status === "CONNECTED") return "会话可用";
  if (status === "EXPIRED") return "会话已过期";
  return "未连接";
}

export default function TwitterPublisher() {
  const [accounts, setAccounts] = useState([]);
  const [posts, setPosts] = useState([]);
  const [accountId, setAccountId] = useState("");
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [notice, setNotice] = useState(null);
  const [localAgent, setLocalAgent] = useState({ ready: false, connected: false, username: "", status: "DISCONNECTED" });
  const [polling, setPolling] = useState(false);
  const [lastPolledAt, setLastPolledAt] = useState(null);
  const pollLock = useRef(false);

  const load = useCallback(async (quiet = false) => {
    if (!quiet) setLoading(true);
    setLoadError("");
    try {
      const [nextAccounts, nextPosts] = await Promise.all([
        request("/accounts"),
        request("/posts?limit=50"),
      ]);
      setAccounts(nextAccounts || []);
      setPosts(nextPosts || []);
      setAccountId((current) => {
        if (current && nextAccounts?.some((account) => String(account.id) === String(current))) return current;
        const connected = nextAccounts?.find((account) => account.sessionStatus === "CONNECTED");
        return connected ? String(connected.id) : nextAccounts?.[0] ? String(nextAccounts[0].id) : "";
      });
    } catch (error) {
      setLoadError(error.message);
    } finally {
      if (!quiet) setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);
  const loadLocalAgent = useCallback(async () => {
    try {
      const status = await extensionRequest("STATUS", {}, 12000);
      setLocalAgent({ ready: true, connected: Boolean(status.connected), username: status.username || "", status: status.status || "DISCONNECTED" });
      return status;
    } catch {
      setLocalAgent({ ready: false, connected: false, username: "", status: "DISCONNECTED" });
      return null;
    }
  }, []);
  useEffect(() => { loadLocalAgent(); }, [loadLocalAgent]);
  useEffect(() => {
    if (!localAgent.connected || !accountId) return;
    extensionRequest("CONFIGURE", { serverBase: window.location.origin, accountId: Number(accountId) }, 12000)
      .catch((error) => setNotice({ type: "error", message: `扩展后台配置失败：${error.message}` }));
  }, [accountId, localAgent.connected]);
  const hasActiveTasks = posts.some((post) => post.status === "PENDING" || post.status === "PROCESSING");
  useEffect(() => {
    if (!hasActiveTasks) return undefined;
    const timer = window.setInterval(() => load(true), 3000);
    return () => window.clearInterval(timer);
  }, [hasActiveTasks, load]);

  const selectedAccount = accounts.find((account) => String(account.id) === String(accountId));
  const pollNow = useCallback(async (manual = false) => {
    if (pollLock.current) return;
    if (!accountId || !localAgent.ready || !localAgent.connected) {
      if (manual) setNotice({ type: "error", message: "请先加载 Chrome 扩展并连接当前 X 账号。" });
      return;
    }
    pollLock.current = true; setPolling(true);
    try {
      await extensionRequest("CONFIGURE", { serverBase: window.location.origin, accountId: Number(accountId) }, 12000);
      const result = await extensionRequest("POLL_NOW", {}, 240000);
      setLastPolledAt(new Date());
      await load(true);
      if (manual) setNotice({ type: result.processed ? "success" : "success", message: result.processed ? `轮询完成，成功发布 ${result.processed} 条任务。` : "轮询完成，当前没有到期任务。" });
    } catch (error) { if (manual) setNotice({ type: "error", message: error.message }); }
    finally { pollLock.current = false; setPolling(false); }
  }, [accountId, localAgent, load]);

  return (
    <div className="twitter-publisher">
      <section className="twitter-intro">
        <div className="twitter-intro-mark"><XLogo weight="fill" /></div>
        <div>
          <span>WEB AUTOMATION CHANNEL</span>
          <h2>发布到 X / Twitter</h2>
          <p>服务器保存任务，Chrome 扩展使用当前 Session 直接调用 X Web 内部接口发布。</p>
        </div>
        <div className="twitter-security"><ShieldCheck weight="duotone" /><span><strong>当前 Chrome</strong><small>直接使用已登录会话</small></span></div>
      </section>

      {notice && (
        <div className={`twitter-notice ${notice.type}`}>
          {notice.type === "success" ? <CheckCircle weight="fill" /> : <WarningCircle weight="fill" />}
          <span>{notice.message}</span><button onClick={() => setNotice(null)} aria-label="关闭"><X /></button>
        </div>
      )}

      {loadError && !loading && (
        <div className="twitter-load-error"><WarningCircle /><span>{loadError}</span><button onClick={() => load()}>重新加载</button></div>
      )}

      <div className="twitter-layout">
        <div className="twitter-main-column">
          <Composer
            accounts={accounts}
            accountId={accountId}
            setAccountId={setAccountId}
            selectedAccount={selectedAccount}
            onPublished={(id) => {
              setNotice({ type: "success", message: `发布任务 #${id} 已保存，正在通知 Chrome 扩展。` });
              load(true);
              pollNow(false);
            }}
            onError={(message) => setNotice({ type: "error", message })}
          />
          <PostHistory posts={posts} loading={loading} onRetry={async (id) => {
            try {
              await request(`/posts/${id}/retry`, { method: "POST" });
              setNotice({ type: "success", message: `任务 #${id} 已重新排队。` });
              load(true);
            } catch (error) { setNotice({ type: "error", message: error.message }); }
          }} onCancel={async (id) => {
            if (!window.confirm(`取消任务 #${id}？`)) return;
            try {
              await request(`/posts/${id}/cancel`, { method: "POST" });
              setNotice({ type: "success", message: `任务 #${id} 已取消。` });
              load(true);
            } catch (error) { setNotice({ type: "error", message: error.message }); }
          }} />
        </div>
        <aside className="twitter-side-column">
          <AccountPanel accounts={accounts} selected={accountId} onSelect={(id) => setAccountId(String(id))} />
          <PollingPanel localAgent={localAgent} polling={polling} lastPolledAt={lastPolledAt} onPoll={() => pollNow(true)} onRefreshAgent={loadLocalAgent} />
          <div className="twitter-flow-card">
            <span>发布流程</span>
            <ol><li><i>1</i><div><strong>上传素材</strong><small>图片保存至服务器</small></div></li><li><i>2</i><div><strong>任务入队</strong><small>数据库记录全过程</small></div></li><li><i>3</i><div><strong>接口直发</strong><small>成功后返回推文链接</small></div></li></ol>
          </div>
        </aside>
      </div>

    </div>
  );
}

function Composer({ accounts, accountId, setAccountId, selectedAccount, onPublished, onError }) {
  const [content, setContent] = useState("");
  const [files, setFiles] = useState([]);
  const [previewing, setPreviewing] = useState(null);
  const [sending, setSending] = useState(false);
  const inputRef = useRef(null);
  const previews = useMemo(() => files.map((file) => ({ file, url: URL.createObjectURL(file) })), [files]);
  useEffect(() => () => previews.forEach((preview) => URL.revokeObjectURL(preview.url)), [previews]);

  const addFiles = (incoming) => {
    const images = Array.from(incoming || []).filter((file) => file.type.startsWith("image/"));
    if (!images.length) return;
    setFiles((current) => [...current, ...images].slice(0, MAX_IMAGES));
  };
  const submit = async () => {
    if (!accountId) return onError("请先连接并选择一个 Twitter 账号。");
    if (selectedAccount?.sessionStatus !== "CONNECTED") return onError("当前账号会话不可用，请重新连接账号。");
    if (!content.trim() && !files.length) return onError("文字和图片不能同时为空。");
    setSending(true);
    try {
      const body = new FormData();
      body.append("accountId", accountId);
      body.append("content", content.trim());
      files.forEach((file) => body.append("images", file));
      const result = await request("/posts", { method: "POST", body });
      setContent(""); setFiles([]); onPublished(result.id);
    } catch (error) { onError(error.message); }
    finally { setSending(false); }
  };

  return (
    <section className="twitter-composer-card">
      <header><div><span>NEW POST</span><h3>创建一条新推文</h3></div><a className="twitter-connect-shortcut" href="/accounts">前往账号中心</a></header>
      <label className="twitter-account-select"><span>发送账号</span><select value={accountId} onChange={(event) => setAccountId(event.target.value)}><option value="">选择账号</option>{accounts.map((account) => <option key={account.id} value={account.id}>@{account.username} · {connectionLabel(account.sessionStatus)}</option>)}</select></label>
      <div className="twitter-editor-wrap">
        <textarea value={content} maxLength={1000} onChange={(event) => setContent(event.target.value)} placeholder="此刻正在发生什么？" />
        <span className={content.length > 900 ? "near-limit" : ""}>{content.length} / 1000</span>
      </div>
      <div className={`twitter-dropzone ${files.length ? "has-files" : ""}`} onDragOver={(event) => event.preventDefault()} onDrop={(event) => { event.preventDefault(); addFiles(event.dataTransfer.files); }}>
        {previews.length ? <div className="twitter-upload-list">{previews.map((preview, index) => <div className="twitter-upload-item" key={`${preview.file.name}-${preview.file.lastModified}-${index}`}><button className="twitter-upload-thumb" onClick={() => setPreviewing(preview)} aria-label={`预览 ${preview.file.name}`}><img src={preview.url} alt="" /></button><span><strong>{preview.file.name}</strong><small>{formatFileSize(preview.file.size)} · {preview.file.type || "图片"}</small></span><i>{index + 1}</i><button className="twitter-upload-remove" onClick={() => { setPreviewing(null); setFiles((current) => current.filter((_, itemIndex) => itemIndex !== index)); }} aria-label={`移除 ${preview.file.name}`}><Trash weight="fill" /></button></div>)}</div> : <button className="twitter-pick-empty" onClick={() => inputRef.current?.click()}><ImageSquare weight="duotone" /><strong>选择图片</strong><small>支持拖放，最多 4 张 PNG、JPEG、WEBP 或 GIF</small></button>}
        {files.length > 0 && files.length < MAX_IMAGES && <button className="twitter-add-image" onClick={() => inputRef.current?.click()}><Plus />继续添加</button>}
        <input ref={inputRef} hidden type="file" accept="image/png,image/jpeg,image/webp,image/gif" multiple onChange={(event) => { addFiles(event.target.files); event.target.value = ""; }} />
      </div>
      <footer><div className="twitter-compose-hint"><ShieldCheck /><span>素材先保存到服务器，再进入发送队列</span></div><button className="twitter-send-button" disabled={sending || (!content.trim() && !files.length)} onClick={submit}>{sending ? <SpinnerGap className="spin" /> : <PaperPlaneTilt weight="fill" />}<span>{sending ? "正在提交" : "一键发送"}</span></button></footer>
      {previewing && <div className="twitter-image-preview" onMouseDown={(event) => event.target === event.currentTarget && setPreviewing(null)}><div><header><strong>{previewing.file.name}</strong><button onClick={() => setPreviewing(null)} aria-label="关闭图片预览"><X /></button></header><img src={previewing.url} alt={previewing.file.name} /></div></div>}
    </section>
  );
}

function AccountPanel({ accounts, selected, onSelect }) {
  return <section className="twitter-account-card"><header><div><span>ACCOUNTS</span><h3>发布账号</h3></div><a href="/accounts" aria-label="前往账号中心">账号中心</a></header><div className="twitter-account-list">{accounts.length ? accounts.map((account) => <button key={account.id} className={String(account.id) === String(selected) ? "active" : ""} onClick={() => onSelect(account.id)}><i className={account.sessionStatus === "CONNECTED" ? "online" : "offline"}><XLogo weight="fill" /></i><span><strong>@{account.username}</strong><small>{connectionLabel(account.sessionStatus)}</small></span>{String(account.id) === String(selected) && <CheckCircle weight="fill" />}</button>) : <div className="twitter-no-account"><XLogo /><span>请先在账号中心配置 X 账号</span></div>}</div></section>;
}

function PollingPanel({ localAgent, polling, lastPolledAt, onPoll, onRefreshAgent }) {
  return <section className="twitter-poll-card"><header><div><span>AUTO POLLING</span><h3>Chrome 自动发布</h3></div><i className={localAgent.ready ? "online" : "offline"} /></header><div className="twitter-agent-state"><strong>{localAgent.ready ? (localAgent.connected ? `@${localAgent.username}` : "扩展已连接，X 尚未登录") : "未检测到 Chrome 扩展"}</strong><small>{localAgent.connected ? "Chrome 开着即可，页面可关闭或切走" : "请加载 AIProvider X Publisher 扩展"}</small></div><label><span>后台轮询</span><strong>固定每 1 分钟</strong></label><div className="twitter-poll-actions"><button onClick={onRefreshAgent}><ArrowClockwise />刷新扩展</button><button className="primary" disabled={polling || !localAgent.connected} onClick={onPoll}>{polling ? <SpinnerGap className="spin" /> : <PaperPlaneTilt />}立即检查</button></div><small className="twitter-last-poll">本页上次手动检查：{lastPolledAt ? lastPolledAt.toLocaleTimeString("zh-CN", { hour12: false }) : "尚未执行"}</small></section>;
}

function PostHistory({ posts, loading, onRetry, onCancel }) {
  return <section className="twitter-history"><header><div><span>PUBLISHING TASKS</span><h3>发布任务</h3></div>{posts.some((post) => post.status === "PENDING" || post.status === "PROCESSING") && <small><SpinnerGap className="spin" />任务刷新中</small>}</header>{loading ? <div className="twitter-history-empty"><SpinnerGap className="spin" />正在读取任务</div> : posts.length ? <div className="twitter-post-list">{posts.map((post) => <PostItem key={post.id} post={post} onRetry={onRetry} onCancel={onCancel} />)}</div> : <div className="twitter-history-empty"><PaperPlaneTilt />还没有发布任务</div>}</section>;
}

function PostItem({ post, onRetry, onCancel }) {
  const meta = statusMeta[post.status] || statusMeta.PENDING;
  const Icon = meta.icon;
  const [previewing, setPreviewing] = useState(null);
  return <article className="twitter-post-item"><div className={`twitter-status-icon ${meta.tone}`}><Icon className={post.status === "PROCESSING" ? "spin" : ""} weight="fill" /></div><div className="twitter-post-body"><div className="twitter-post-top"><strong>@{post.username}</strong><span className={meta.tone}>{meta.label}</span><time>{post.status === "PENDING" ? `计划 ${formatDate(post.scheduledAt)}` : formatDate(post.sentAt || post.createdAt)}</time></div><p>{post.content || <em>仅图片推文</em>}</p>{post.images?.length > 0 && <div className="twitter-media-strip">{post.images.map((image) => <TaskImageThumb key={image.id} postId={post.id} image={image} onPreview={(src) => setPreviewing({ src, name: image.originalFileName })} />)}<span>{post.images.length} 张图片{post.images.some((image) => image.assetId) ? " · 含资产" : ""}</span></div>}{post.errorMessage && <div className="twitter-post-error"><WarningCircle />{post.errorMessage}</div>}<div className="twitter-post-actions"><small>任务 #{post.id} · 尝试 {post.attemptCount} 次</small>{post.tweetUrl && <a href={post.tweetUrl} target="_blank" rel="noreferrer"><LinkSimple />查看推文</a>}{post.status === "FAILED" && <button onClick={() => onRetry(post.id)}><ArrowClockwise />重新发送</button>}{["PENDING", "FAILED"].includes(post.status) && <button className="cancel" onClick={() => onCancel(post.id)}><X />取消任务</button>}</div></div>{previewing && <ImagePreview value={previewing} onClose={() => setPreviewing(null)} />}</article>;
}

function TaskImageThumb({ postId, image, onPreview }) {
  const [src] = useState(`${API}/posts/${postId}/images/${image.id}`);
  const [failed, setFailed] = useState(false);
  return <button className="twitter-task-image-thumb" onClick={() => onPreview(src)} title={`点击预览 ${image.originalFileName}`}>{failed ? <WarningCircle /> : <img src={src} alt={image.originalFileName} onError={() => setFailed(true)} />}</button>;
}

function ImagePreview({ value, onClose }) {
  return <div className="twitter-image-preview" onMouseDown={(event) => event.target === event.currentTarget && onClose()}><div><header><strong>{value.name}</strong><button onClick={onClose} aria-label="关闭图片预览"><X /></button></header><img src={value.src} alt={value.name} /></div></div>;
}
