import { useCallback, useEffect, useMemo, useState } from "react";
import { ArrowClockwise, CaretLeft, CaretRight, CheckCircle, MicrophoneStage, PencilSimple, WarningCircle, X } from "@phosphor-icons/react";
import UiSearchField from "./UiSearchField";
import UiToast from "./UiToast";
import { readJsonResponse } from "./apiResponse";
import "./AsrRecords.css";

const emptyFilters={keyword:"",characterId:"",status:"",provider:"",model:"",startTime:"",endTime:""};
const duration=(value)=>value==null?"—":value<1000?`${value} ms`:`${(value/1000).toFixed(1)} 秒`;
const size=(value)=>{const bytes=Number(value||0);if(bytes<1024)return `${bytes} B`;if(bytes<1024*1024)return `${(bytes/1024).toFixed(1)} KB`;return `${(bytes/1024/1024).toFixed(2)} MB`;};
const dateTime=(value)=>value?new Date(value).toLocaleString("zh-CN",{hour12:false}):"—";
async function api(path,options){const response=await fetch(path,options);const body=await readJsonResponse(response,"语音识别记录响应异常");if(!response.ok||body.code!==200)throw new Error(body.message||`请求失败 · ${response.status}`);return body.data;}

export default function AsrRecords(){
  const [filters,setFilters]=useState(emptyFilters);const [page,setPage]=useState(1);const [data,setData]=useState({items:[],total:0,pages:1});const [options,setOptions]=useState({characters:[],providerModels:[]});const [selected,setSelected]=useState(null);const [loading,setLoading]=useState(true);const [error,setError]=useState("");const [notice,setNotice]=useState("");
  const query=useMemo(()=>{const params=new URLSearchParams({page:String(page),pageSize:"20"});Object.entries(filters).forEach(([key,value])=>{if(value)params.set(key,key.endsWith("Time")?new Date(value).toISOString():value);});return params.toString();},[filters,page]);
  const load=useCallback(async()=>{setLoading(true);setError("");try{setData(await api(`/api/admin/asr/records?${query}`));}catch(e){setError(e.message);}finally{setLoading(false);}},[query]);
  useEffect(()=>{load();},[load]);useEffect(()=>{api("/api/admin/asr/records/filters").then(setOptions).catch((e)=>setError(e.message));},[]);
  const updateFilter=(key,value)=>{setPage(1);setFilters((current)=>({...current,[key]:value,...(key==="provider"?{model:""}:null)}));};
  const models=useMemo(()=>[...new Set(options.providerModels.filter((item)=>!filters.provider||item.provider===filters.provider).map((item)=>item.model))],[options.providerModels,filters.provider]);
  const open=async(recordId)=>{setError("");try{setSelected(await api(`/api/admin/asr/records/${recordId}`));}catch(e){setError(e.message);}};
  const saveCorrection=async()=>{try{await api(`/api/admin/asr/records/${selected.recordId}/correction`,{method:"PUT",headers:{"Content-Type":"application/json"},body:JSON.stringify({correctedText:selected.correctedText||""})});setNotice("人工修正文字已保存");setSelected(await api(`/api/admin/asr/records/${selected.recordId}`));load();}catch(e){setError(e.message);}};
  return <section className="asr-records-page">
    <header className="asr-head"><div><span><MicrophoneStage weight="fill"/>云服务</span><h1>语音识别记录</h1><p>逐条播放 AIMaid 录音，核对识别结果并保存人工修正。</p></div><button type="button" onClick={load} disabled={loading}><ArrowClockwise className={loading?"is-spinning":""}/>{loading?"加载中":"刷新"}</button></header>
    <div className="asr-filter-card" aria-label="语音识别记录筛选">
      <UiSearchField aria-label="搜索识别文字" placeholder="搜索识别文字" value={filters.keyword} onChange={(e)=>updateFilter("keyword",e.target.value)}/>
      <label><span>角色</span><select aria-label="按角色筛选" value={filters.characterId} onChange={(e)=>updateFilter("characterId",e.target.value)}><option value="">全部角色</option>{options.characters.map((item)=><option key={item.characterId} value={item.characterId}>{item.characterName} · {item.characterId}</option>)}</select></label>
      <label><span>状态</span><select aria-label="按状态筛选" value={filters.status} onChange={(e)=>updateFilter("status",e.target.value)}><option value="">全部状态</option><option value="SUCCESS">成功</option><option value="FAILED">失败</option></select></label>
      <label><span>Provider</span><select aria-label="按 Provider 筛选" value={filters.provider} onChange={(e)=>updateFilter("provider",e.target.value)}><option value="">全部 Provider</option>{[...new Set(options.providerModels.map((item)=>item.provider))].map((item)=><option key={item}>{item}</option>)}</select></label>
      <label><span>模型</span><select aria-label="按模型筛选" value={filters.model} onChange={(e)=>updateFilter("model",e.target.value)}><option value="">全部模型</option>{models.map((item)=><option key={item}>{item}</option>)}</select></label>
      <label><span>开始时间</span><input aria-label="开始时间" type="datetime-local" value={filters.startTime} onChange={(e)=>updateFilter("startTime",e.target.value)}/></label><label><span>结束时间</span><input aria-label="结束时间" type="datetime-local" value={filters.endTime} onChange={(e)=>updateFilter("endTime",e.target.value)}/></label>
    </div>
    <div className="asr-table-card"><div className="asr-table-meta"><strong>{data.total} 条记录</strong><span>第 {page} / {Math.max(1,data.pages)} 页</span></div><div className="asr-table-wrap"><table><thead><tr><th>创建时间</th><th>角色</th><th>音频</th><th>Provider / 模型</th><th>识别文字</th><th>处理耗时</th><th>状态</th><th>操作</th></tr></thead><tbody>
      {!loading&&data.items.map((item)=><tr key={item.recordId}><td>{dateTime(item.createdAt)}</td><td><strong>{item.characterNameSnapshot}</strong><small>{item.characterId}</small></td><td>{duration(item.audioDurationMs)}<small>{size(item.audioSize)}</small></td><td>{item.provider}<small>{item.model}</small></td><td className="asr-text-preview">{item.recognizedText||item.errorMessage||"—"}</td><td>{duration(item.processingTimeMs)}</td><td><span className={`asr-status is-${item.status.toLowerCase()}`}>{item.status==="SUCCESS"?<CheckCircle/>:<WarningCircle/>}{item.status==="SUCCESS"?"成功":"失败"}</span></td><td><button type="button" className="asr-detail-button" onClick={()=>open(item.recordId)}>查看详情</button></td></tr>)}
      {!loading&&!data.items.length&&<tr><td colSpan="8" className="asr-empty">没有符合条件的语音识别记录</td></tr>}{loading&&<tr><td colSpan="8" className="asr-empty">正在加载记录…</td></tr>}
    </tbody></table></div><footer className="asr-pagination"><button type="button" aria-label="上一页" disabled={page<=1||loading} onClick={()=>setPage((value)=>value-1)}><CaretLeft/></button><span>{page} / {Math.max(1,data.pages)}</span><button type="button" aria-label="下一页" disabled={page>=data.pages||loading} onClick={()=>setPage((value)=>value+1)}><CaretRight/></button></footer></div>
    {selected&&<div className="asr-dialog-backdrop"><section className="asr-dialog" role="dialog" aria-modal="true" aria-label={`语音识别详情 ${selected.recordId}`}><header><div><span>{selected.recordId}</span><h2>{selected.characterNameSnapshot} 的语音记录</h2></div><button type="button" aria-label="关闭语音识别详情" onClick={()=>setSelected(null)}><X/></button></header>
      <audio controls preload="metadata" src={selected.audioUrl}>当前浏览器不支持音频播放。</audio><dl className="asr-detail-meta"><div><dt>角色 / 会话</dt><dd>{selected.characterId}<small>{selected.sessionId||"无会话 ID"}</small></dd></div><div><dt>Provider / 模型</dt><dd>{selected.provider}<small>{selected.model}</small></dd></div><div><dt>音频</dt><dd>{duration(selected.audioDurationMs)}<small>{size(selected.audioSize)} · {selected.audioFormat}</small></dd></div><div><dt>处理</dt><dd>{duration(selected.processingTimeMs)}<small>{dateTime(selected.createdAt)}</small></dd></div></dl>
      {selected.status==="FAILED"&&<div className="asr-detail-error"><WarningCircle/><div><strong>{selected.errorCode||"ASR_TRANSCRIPTION_FAILED"}</strong><span>{selected.errorMessage||"语音识别失败"}</span></div></div>}
      <div className="asr-copy-compare"><article><h3>识别文字</h3><p>{selected.recognizedText||"暂无识别文字"}</p></article><article><label htmlFor="asr-correction"><PencilSimple/>人工修正文字</label><textarea id="asr-correction" value={selected.correctedText||""} onChange={(e)=>setSelected((current)=>({...current,correctedText:e.target.value}))} placeholder="听取原始音频后填写确认文字"/></article></div>
      <footer><button type="button" onClick={()=>setSelected(null)}>取消</button><button type="button" className="asr-primary" onClick={saveCorrection} disabled={selected.status!=="SUCCESS"}>保存人工修正</button></footer></section></div>}
    <UiToast message={error||notice} tone={error?"error":"success"} onDismiss={()=>{setError("");setNotice("");}}/>
  </section>;
}
