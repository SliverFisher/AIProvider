import { useEffect, useMemo, useRef, useState } from 'react'
import {
  Waveform, Broadcast, Camera, ChartLineUp, ChatCircle, CheckCircle, Clock,
  Database, Desktop, Heart, House, Link, MagnifyingGlass, Robot, Sparkle,
  Warning, Wrench, X, ArrowsClockwise, VideoCamera, Stop,
  Brain, CaretRight, Bell, Pulse, CameraRotate, User
  , CirclesFour, ChatsTeardrop, Monitor, Binoculars, Notebook, MicrophoneStage, FilmSlate, Stack, Table, Rows, CaretLeft
} from '@phosphor-icons/react'
import {
  Area, AreaChart, Bar, BarChart, CartesianGrid, Line, LineChart,
  ResponsiveContainer, Tooltip, XAxis, YAxis
} from 'recharts'
import portrait from './assets/maid-neural-portrait.png'
import './App.css'

const API = '/api'
const NAV = [
  { key: 'maid', label: '我的女仆', icon: Heart },
  { key: 'search', label: '全局搜索', icon: Binoculars },
  { key: 'camera', label: '手机监控', icon: VideoCamera },
]
const MOBILE_NAV = NAV

const fmt = value => Number(value || 0).toLocaleString('zh-CN')
const compact = value => new Intl.NumberFormat('zh-CN', { notation: 'compact', maximumFractionDigits: 2 }).format(Number(value || 0))
const formatTime = seconds => {
  const s = Number(seconds || 0), h = Math.floor(s / 3600), m = Math.floor((s % 3600) / 60)
  return h ? `${h}h ${m}m` : m ? `${m}m` : `${Math.floor(s)}s`
}
const timeAgo = date => {
  if (!date) return '刚刚'
  const delta = Math.max(0, Date.now() - new Date(date).getTime()) / 60000
  if (delta < 1) return '刚刚'
  if (delta < 60) return `${Math.floor(delta)} 分钟前`
  if (delta < 1440) return `${Math.floor(delta / 60)} 小时前`
  return new Date(date).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}
const chartTip = { background: '#0b1425', border: '1px solid #23365b', borderRadius: 12, color: '#eef4ff', fontSize: 12 }

async function get(path) {
  const response = await fetch(`${API}${path}`)
  if (!response.ok) throw new Error(`请求失败 · ${response.status}`)
  const result = await response.json()
  if (result.code !== 200) throw new Error(result.message || '请求失败')
  return result.data
}

function useDashboardData() {
  const [data, setData] = useState({ overview: {}, llm: [], models: [], chats: [], calls: [], time: [], tools: [], apps: [], broadcasts: [], chatStats: {}, sync: { recentRuns: [] }, insights: { counts: {}, reminders: [], notes: [], voice: [], videos: [], remoteVideos: [], runtime: {} } })
  const [state, setState] = useState('loading')
  const [error, setError] = useState('')
  const load = async () => {
    setState('loading'); setError('')
    try {
      const [overview, llm, models, chats, calls, time, tools, apps, broadcasts, chatStats, sync, insights] = await Promise.all([
        get('/dashboard/overview'), get('/dashboard/llm-usage-daily?days=30'), get('/dashboard/llm-model-stats'),
        get('/dashboard/recent-chats?limit=40'), get('/dashboard/recent-llm-calls?limit=30'),
        get('/dashboard/time-tracking-daily?days=30'), get('/dashboard/agent-tool-usage'),
        get('/dashboard/desktop-app-usage'), get('/dashboard/broadcast-stats'), get('/dashboard/chat-stats'),
        get('/sync/status').catch(() => ({ recentRuns: [] })), get('/insights/command').catch(() => ({ counts: {}, reminders: [], notes: [], voice: [], videos: [], remoteVideos: [], runtime: {} })),
      ])
      setData({ overview, llm, models, chats, calls, time, tools, apps, broadcasts, chatStats, sync, insights }); setState('ready')
    } catch (e) { setError(e.message); setState('error') }
  }
  useEffect(() => { load() }, [])
  return { ...data, state, error, reload: load }
}

function App() {
  const [view, setView] = useState('home')
  const dashboard = useDashboardData()
  const current = NAV.find(item => item.key === view)
  return <div className="neural-shell">
    <aside className="rail">
      <div className="rail-brand" onClick={() => setView('home')} style={{ cursor: 'pointer' }}><Sparkle weight="fill" /><span>AI</span></div>
      <nav>
        <button className={view === 'home' ? 'nav-button active' : 'nav-button'} onClick={() => setView('home')} title="首页"><House size={22} weight={view === 'home' ? 'duotone' : 'regular'} /><span>首页</span></button>
        {NAV.map(item => <NavButton key={item.key} item={item} active={view === item.key} onClick={() => setView(item.key)} />)}
      </nav>
      <div className="rail-signal"><span /><span /><span /></div>
    </aside>
    <header className="mobile-head"><div className="mobile-logo"><Sparkle weight="fill" /> AI Maid</div><span className="live-copy"><i /> LIVE</span></header>
    <main className="workspace">
      {view !== 'home' && current && <div className="section-head"><div><span className="eyebrow">AI Maid · Neural Command</span><h1>{current.label}</h1></div><SystemClock /></div>}
      {view === 'home' && <HomeView />}
      {view === 'maid' && (
        dashboard.state === 'loading' ? <LoadingState /> :
        dashboard.state === 'error' ? <ErrorState message={dashboard.error} retry={dashboard.reload} /> :
        <MaidView data={dashboard} />
      )}
      {view === 'search' && <SearchView />}
      {view === 'camera' && <CameraMonitor />}
    </main>
    <nav className="bottom-nav">{MOBILE_NAV.map(item => <NavButton key={item.key} item={item} active={view === item.key} onClick={() => setView(item.key)} />)}</nav>
  </div>
}

function NavButton({ item, active, onClick }) {
  const Icon = item.icon
  return <button className={active ? 'nav-button active' : 'nav-button'} onClick={onClick} title={item.label}><Icon size={22} weight={active ? 'duotone' : 'regular'} /><span>{item.label}</span></button>
}

function SystemClock() {
  const [now, setNow] = useState(new Date())
  useEffect(() => { const id = setInterval(() => setNow(new Date()), 1000); return () => clearInterval(id) }, [])
  return <div className="system-clock"><span>{now.toLocaleDateString('zh-CN', { month: 'long', day: 'numeric', weekday: 'short' })}</span><strong>{now.toLocaleTimeString('zh-CN', { hour12: false })}</strong></div>
}

/* ========== 首页：纯大图 ========== */
function HomeView() {
  return <div className="home-hero">
    <div className="home-portrait-wrap">
      <div className="home-orbit-a" />
      <div className="home-orbit-b" />
      <div className="home-orbit-c" />
      <img src={portrait} alt="AI Maid" />
      <div className="home-glow" />
    </div>
    <div className="home-title">
      <span className="live-copy"><i /> LIVE · 在线</span>
      <h1>思娴</h1>
      <p>你的 AI 女仆助手 · 随时待命</p>
    </div>
  </div>
}

/* ========== 我的女仆：聚合视图 ========== */
function MaidView({ data }) {
  const ov = data.overview, maid = ov.maidState || {}
  const syncRun = data.sync.recentRuns?.[0]
  const favor = Math.max(0, Math.min(100, Number(maid.Favorability || 89)))
  const companion = Number(maid.CompanionshipSeconds || 0)

  return <div className="maid-consolidated">
    {/* 女仆状态卡片 */}
    <section className="maid-hero-card">
      <div className="maid-hero-left">
        <div className="portrait-core">
          <div className="orbit orbit-a" /><div className="orbit orbit-b" /><div className="orbit orbit-c" />
          <img src={portrait} alt={maid.Name || '思娴'} />
          <div className="pulse-line" />
        </div>
      </div>
      <div className="maid-hero-right">
        <span className="live-copy"><i /> LIVE · 在线</span>
        <h1>{maid.Name || '思娴'}</h1>
        <div className="mood"><Heart weight="fill" /><div><strong>心情 · {maid.Mood || '愉悦'}</strong><span>情绪稳定，状态良好</span></div></div>
        <Progress label="好感度" value={favor} suffix={`${favor}%`} />
        <Progress label="陪伴时长" value={Math.min(100, companion / 360)} suffix={formatTime(companion)} violet />
      </div>
    </section>

    {/* 核心指标卡片 */}
    <section className="maid-stat-grid">
      <StatCard icon={Brain} label="LLM 调用" value={fmt(ov.totalLlmCalls)} sub="次" tone="violet" />
      <StatCard icon={Database} label="Token 消耗" value={compact(ov.totalTokens)} sub="Tokens" tone="blue" />
      <StatCard icon={ChatCircle} label="对话消息" value={fmt(ov.totalChatMessages)} sub="条" tone="cyan" />
      <StatCard icon={Clock} label="追踪时长" value={formatTime(ov.totalTrackedSeconds)} sub="" tone="coral" />
      <StatCard icon={Desktop} label="桌面快照" value={fmt(ov.totalDesktopSnapshots)} sub="张" tone="green" />
      <StatCard icon={Wrench} label="Agent 工具" value={fmt(ov.agentSuccessCount)} sub="次成功" tone="amber" />
    </section>

    {/* AI 活动趋势 */}
    <section className="maid-panel">
      <PanelHeader title="AI 活动趋势" subtitle="调用次数 · 近 14 天" />
      {data.llm.slice(-14).length ? <ResponsiveContainer width="100%" height={260}><AreaChart data={data.llm.slice(-14)} margin={{ left: -18, right: 8, top: 18, bottom: 0 }}><defs><linearGradient id="neuralArea" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stopColor="#8b5cf6" stopOpacity=".55"/><stop offset="1" stopColor="#8b5cf6" stopOpacity="0"/></linearGradient></defs><CartesianGrid stroke="#1b2941" strokeDasharray="2 8" vertical={false}/><XAxis dataKey="day" tickFormatter={v => String(v).slice(5)} tick={{ fill: '#687995', fontSize: 11 }} axisLine={false} tickLine={false}/><YAxis tick={{ fill: '#687995', fontSize: 11 }} axisLine={false} tickLine={false}/><Tooltip contentStyle={chartTip}/><Area type="monotone" dataKey="callCount" stroke="#9d7bff" strokeWidth={2.5} fill="url(#neuralArea)" name="调用次数" /></AreaChart></ResponsiveContainer> : <EmptyMini />}
    </section>

    {/* Token 消耗 + 时间追踪 */}
    <section className="maid-dual">
      <div className="maid-panel">
        <PanelHeader title="Token 消耗趋势" subtitle="近 30 天" />
        <ResponsiveContainer width="100%" height={280}><LineChart data={data.llm}><CartesianGrid stroke="#1b2941" strokeDasharray="3 8"/><XAxis dataKey="day" tick={{fill:'#71809b',fontSize:11}} axisLine={false}/><YAxis tick={{fill:'#71809b',fontSize:11}} axisLine={false}/><Tooltip contentStyle={chartTip}/><Line dataKey="totalTokens" stroke="#8b5cf6" dot={false} strokeWidth={2.5}/><Line dataKey="completionTokens" stroke="#ff6b77" dot={false}/></LineChart></ResponsiveContainer>
      </div>
      <div className="maid-panel">
        <PanelHeader title="时间追踪" subtitle="近 30 天" />
        <ResponsiveContainer width="100%" height={280}><AreaChart data={data.time}><defs><linearGradient id="timefill" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stopColor="#31d8ff" stopOpacity=".4"/><stop offset="1" stopColor="#31d8ff" stopOpacity="0"/></linearGradient></defs><XAxis dataKey="day" tick={{fill:'#71809b',fontSize:11}} axisLine={false}/><YAxis tick={{fill:'#71809b',fontSize:11}} axisLine={false}/><Tooltip contentStyle={chartTip}/><Area dataKey="totalSeconds" stroke="#31d8ff" fill="url(#timefill)"/></AreaChart></ResponsiveContainer>
      </div>
    </section>

    {/* 模型排行 + Agent 工具 */}
    <section className="maid-dual">
      <div className="maid-panel">
        <PanelHeader title="模型调用排行" subtitle="Top 8" />
        <ResponsiveContainer width="100%" height={280}><BarChart data={data.models.slice(0,8)} layout="vertical"><XAxis type="number" hide/><YAxis dataKey="Model" type="category" width={130} tick={{fill:'#9aa7bc',fontSize:11}} axisLine={false} tickLine={false}/><Tooltip contentStyle={chartTip}/><Bar dataKey="callCount" fill="#7357d9" radius={[0,8,8,0]}/></BarChart></ResponsiveContainer>
      </div>
      <div className="maid-panel">
        <PanelHeader title="Agent 工具健康" subtitle="调用统计" />
        <div className="table-scroll"><table><thead><tr><th>能力</th><th>调用</th><th>成功</th><th>错误</th></tr></thead><tbody>{data.tools.map(x => <tr key={x.tool_name}><td>{x.tool_name}</td><td>{fmt(x.count)}</td><td>{fmt(x.successCount)}</td><td>{fmt(x.errorCount)}</td></tr>)}</tbody></table></div>
      </div>
    </section>

    {/* 对话记忆 */}
    <section className="maid-panel">
      <PanelHeader title="近期对话记忆" subtitle={`${fmt(data.chatStats.userCount)} 条用户消息 · ${fmt(data.chatStats.assistantCount)} 条 AI 回应`} />
      <div className="conversation-list">{data.chats.slice(0, 20).map((chat,i) => <article key={`${chat.Id}-${i}`} className={chat.Role === 'user' ? 'user' : 'maid'}><div className="conversation-avatar">{chat.Role === 'user' ? <Waveform/> : <Sparkle weight="fill"/>}</div><div><header><strong>{chat.Role === 'user' ? '你' : '思娴'}</strong><time>{timeAgo(chat.CreatedAt)}</time></header><p>{chat.Content}</p><footer>{chat.ModelName || 'AI Maid'} · 会话 {String(chat.ConversationId||'').slice(0,8)}</footer></div></article>)}</div>
    </section>

    {/* 业务感知 */}
    <BusinessMatrix insights={data.insights} />

    {/* 桌面应用 + 广播 */}
    <section className="maid-dual">
      <div className="maid-panel">
        <PanelHeader title="桌面应用热度" subtitle="Top 10" />
        <ResponsiveContainer width="100%" height={280}><BarChart data={data.apps.slice(0,10)} layout="vertical"><XAxis type="number" hide/><YAxis dataKey="app" type="category" width={135} tick={{fill:'#9aa7bc',fontSize:11}} axisLine={false}/><Tooltip contentStyle={chartTip}/><Bar dataKey="count" fill="#3c71ef" radius={[0,8,8,0]}/></BarChart></ResponsiveContainer>
      </div>
      <div className="maid-panel">
        <PanelHeader title="最近 LLM 调用" subtitle="最近 30 次" />
        <div className="table-scroll"><table><thead><tr><th>时间</th><th>模型</th><th>Token</th><th>耗时</th></tr></thead><tbody>{data.calls.map((x,i) => <tr key={i}><td>{timeAgo(x.CreatedAt)}</td><td>{x.Model}</td><td>{fmt(x.TotalTokens)}</td><td>{x.DurationMs||0}ms</td></tr>)}</tbody></table></div>
      </div>
    </section>

    {/* 最近活动动态 */}
    <section className="maid-panel">
      <PanelHeader title="近期动态" subtitle="实时更新" />
      <div className="activity-feed">{data.chats.slice(0, 8).map((chat, i) => <div key={`${chat.Id}-${i}`}><i /><time>{new Date(chat.CreatedAt).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}</time><span>{chat.Role === 'user' ? '与你互动' : '思娴回应'} · {String(chat.Content || '').slice(0, 26)}</span></div>)}</div>
    </section>

    <div style={{ height: 40 }} />
  </div>
}

function PanelHeader({ title, subtitle }) {
  return <div className="panel-heading"><div><h2>{title}</h2><span>{subtitle}</span></div><span className="panel-live"><i /> 实时</span></div>
}

function StatCard({ icon: Icon, label, value, sub, tone }) {
  return <div className={`stat-card ${tone}`}>
    <Icon size={28} weight="duotone" />
    <div>
      <span>{label}</span>
      <strong>{value}</strong>
      {sub && <small>{sub}</small>}
    </div>
  </div>
}

function Progress({ label, value, suffix, violet }) {
  return <div className="progress-stat">
    <div><span>{label}</span><strong>{suffix}</strong></div>
    <div className={violet ? 'progress violet' : 'progress'}><i style={{ width: `${Math.min(100, value)}%` }} /></div>
  </div>
}

function BusinessMatrix({ insights = {} }) {
  const counts = insights.counts || {}, runtime = insights.runtime || {}
  const modules = [
    { icon: Notebook, label: '知识笔记', value: fmt(counts.NotebookNotes), meta: insights.notes?.[0]?.Title || '等待新的记录', tone: 'violet' },
    { icon: Bell, label: '提醒计划', value: fmt(counts.Reminders), meta: insights.reminders?.[0]?.Title || '暂无待办提醒', tone: 'coral' },
    { icon: MicrophoneStage, label: '语音互动', value: fmt(counts.VoiceTriggerLogs), meta: runtime.TtsStatus || 'TTS 状态待同步', tone: 'cyan' },
    { icon: FilmSlate, label: '视频收藏', value: fmt(Number(counts.VideoItems || 0) + Number(counts.RemoteVideoItems || 0)), meta: insights.videos?.[0]?.Title || insights.remoteVideos?.[0]?.Title || '暂无最近播放', tone: 'blue' },
  ]
  return <section className="business-matrix">
    <div className="business-title">
      <div><span>AI MAID BUSINESS MATRIX</span><h2>业务感知</h2></div>
      <div className="runtime-chips"><span>LLM {runtime.OllamaStatus || '—'}</span><span>TTS {runtime.TtsStatus || '—'}</span><span>{runtime.LastLlmLatencyMs ? `${runtime.LastLlmLatencyMs}ms` : '延迟待同步'}</span></div>
    </div>
    <div className="business-modules">{modules.map(item => { const Icon = item.icon; return <article key={item.label} className={item.tone}><Icon size={25} weight="duotone"/><div><span>{item.label}</span><strong>{item.value}</strong><small>{item.meta}</small></div><CaretRight/></article> })}</div>
  </section>
}

/* ========== 搜索 ========== */
function SearchView() {
  const [q,setQ]=useState(''), [results,setResults]=useState(null), [loading,setLoading]=useState(false), [error,setError]=useState('')
  const run=async()=>{if(q.trim().length<2)return;setLoading(true);setError('');try{setResults(await get(`/search?q=${encodeURIComponent(q)}&limit=50`))}catch(e){setError(e.message)}finally{setLoading(false)}}
  return <div className="content-stack search-view"><div className="search-hero"><MagnifyingGlass size={32}/><div><h2>搜索整个 AI Maid 记忆库</h2><p>对话、LLM 调用、笔记、提醒与桌面活动都在这里。</p></div></div><div className="search-control"><MagnifyingGlass/><input value={q} onChange={e=>setQ(e.target.value)} onKeyDown={e=>e.key==='Enter'&&run()} placeholder="输入至少两个字符…"/><button onClick={run} disabled={loading}>{loading?'搜索中':'开始搜索'}</button></div>{error&&<div className="inline-error">{error}</div>}{results&&<DataPanel title={`搜索结果 · ${results.length}`}><div className="result-list">{results.map((x,i)=><article key={i}><span>{x._table}</span><div><strong>{x._sub||x._matchField}</strong><p>{x._text}</p></div><CaretRight/></article>)}</div></DataPanel>}</div>
}

/* ========== 手机监控 ========== */
function CameraMonitor() {
  const [role,setRole]=useState('viewer'), [room,setRoom]=useState('aimaid-private'), [status,setStatus]=useState('等待连接'), [facing,setFacing]=useState('environment')
  const local=useRef(null), remote=useRef(null), socket=useRef(null), peer=useRef(null), stream=useRef(null)
  const send=data=>socket.current?.readyState===WebSocket.OPEN&&socket.current.send(JSON.stringify(data))
  const createPeer=()=>{peer.current?.close();const pc=new RTCPeerConnection({iceServers:[{urls:'stun:stun.cloudflare.com:3478'}]});pc.onicecandidate=e=>e.candidate&&send({type:'ice',candidate:e.candidate});pc.ontrack=e=>{remote.current.srcObject=e.streams[0];setStatus('直播中')};pc.onconnectionstatechange=()=>setStatus({connected:'直播中',failed:'连接失败',disconnected:'连接中断',closed:'已停止'}[pc.connectionState]||'正在建立连接');stream.current?.getTracks().forEach(t=>pc.addTrack(t,stream.current));peer.current=pc;return pc}
  const connect=()=>{socket.current?.close();const ws=new WebSocket(`${location.protocol==='https:'?'wss':'ws'}://${location.host}/ws/signal`);socket.current=ws;setStatus('连接信令中');ws.onopen=()=>send({type:'join',room});ws.onclose=()=>setStatus('信令已断开');ws.onmessage=async e=>{const m=JSON.parse(e.data);if(m.type==='joined')setStatus(m.peers?'对端已在线':'等待对端');if(m.type==='peer-joined'&&role==='publisher'&&stream.current){const pc=createPeer(),offer=await pc.createOffer();await pc.setLocalDescription(offer);send({type:'offer',sdp:offer})}if(m.type==='offer'&&role==='viewer'){const pc=createPeer();await pc.setRemoteDescription(m.sdp);const answer=await pc.createAnswer();await pc.setLocalDescription(answer);send({type:'answer',sdp:answer})}if(m.type==='answer')await peer.current?.setRemoteDescription(m.sdp);if(m.type==='ice'&&peer.current)try{await peer.current.addIceCandidate(m.candidate)}catch{}if(['peer-left','stop'].includes(m.type)){peer.current?.close();setStatus('对端已离线')}if(m.type==='room-full')setStatus('房间已有两台设备')}}
  const start=async(next=facing)=>{stream.current?.getTracks().forEach(t=>t.stop());stream.current=await navigator.mediaDevices.getUserMedia({video:{facingMode:{ideal:next},width:{ideal:1280},height:{ideal:720}},audio:false});local.current.srcObject=stream.current;setStatus('摄像头已开启');connect()}
  const switchCam=async()=>{const next=facing==='environment'?'user':'environment';setFacing(next);await start(next)}
  const stop=()=>{send({type:'stop'});stream.current?.getTracks().forEach(t=>t.stop());peer.current?.close();socket.current?.close();setStatus('已停止')}
  useEffect(()=>()=>stop(),[])
  return <div className="camera-command"><div className="camera-header"><div><span className="eyebrow">WEBRTC · PRIVATE ROOM</span><h2>实时手机视角</h2><p>画面点对点传输，服务器只负责信令协调。</p></div><div className={`camera-status ${status==='直播中'?'live':''}`}><i/>{status}</div></div><div className="camera-layout"><div className="video-console">{role==='publisher'?<video ref={local} autoPlay muted playsInline/>:<video ref={remote} autoPlay playsInline controls/>}<div className="video-placeholder"><Camera size={48} weight="thin"/><span>{role==='publisher'?'等待开启手机摄像头':'等待手机端加入房间'}</span></div></div><aside className="camera-side"><div className="role-toggle"><button className={role==='viewer'?'active':''} onClick={()=>setRole('viewer')}><Desktop/>电脑观看</button><button className={role==='publisher'?'active':''} onClick={()=>setRole('publisher')}><Camera/>手机直播</button></div><label>私有房间<input value={room} onChange={e=>setRoom(e.target.value.replace(/[^A-Za-z0-9_-]/g,''))}/></label>{role==='viewer'?<button className="primary-camera" onClick={connect}><Link/>连接观看</button>:<button className="primary-camera" onClick={()=>start().catch(()=>setStatus('请检查 HTTPS 与摄像头权限'))}><VideoCamera/>开启直播</button>}<button onClick={switchCam} disabled={role!=='publisher'}><CameraRotate/>切换摄像头</button><button onClick={stop}><Stop/>停止连接</button><div className="camera-help"><CheckCircle/>请在手机和电脑输入相同房间名。手机浏览器必须通过 HTTPS 打开。</div></aside></div></div>
}

/* ========== 通用组件 ========== */
function DataPanel({title,children}){return <section className="data-panel"><header><h2>{title}</h2><span className="panel-live"><i/> LIVE</span></header>{children}</section>}
function EmptyMini(){return <div className="empty-mini"><ChartLineUp size={32}/><span>等待活动数据</span></div>}
function LoadingState(){return <div className="loading-state"><div className="neural-loader"><i/><i/><i/></div><strong>正在连接 Neural Command</strong><span>读取 AI Maid 实时状态…</span></div>}
function ErrorState({message,retry}){return <div className="error-state"><Warning size={42}/><strong>数据链路暂时不可用</strong><span>{message}</span><button onClick={retry}><ArrowsClockwise/>重新连接</button></div>}

export default App