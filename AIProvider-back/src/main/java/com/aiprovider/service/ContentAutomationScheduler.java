package com.aiprovider.service;

import com.aiprovider.mapper.ContentOperationsMapper;
import com.aiprovider.model.vo.ContentPipelineTestVO;
import com.aiprovider.repository.ContentOperationsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ContentAutomationScheduler {
    private static final Logger log=LogManager.getLogger(ContentAutomationScheduler.class);private final ContentOperationsRepository repository;private final ContentPipelineService pipeline;private final ObjectMapper json;private final AtomicBoolean running=new AtomicBoolean(false);
    public ContentAutomationScheduler(ContentOperationsRepository repository,ContentPipelineService pipeline,ObjectMapper json){this.repository=repository;this.pipeline=pipeline;this.json=json;}
    @Scheduled(fixedDelayString="${content-operations.scheduler-delay-ms:60000}",initialDelayString="${content-operations.scheduler-initial-delay-ms:30000}")
    public void runDue(){if(!running.compareAndSet(false,true))return;try{for(Map<String,Object> binding:repository.findDueBindings())runOne(number(binding.get("accountId")),number(binding.get("sourceId")));}finally{running.set(false);}}
    private void runOne(long accountId,long sourceId){ContentOperationsMapper.OperationRunRecord run=new ContentOperationsMapper.OperationRunRecord();run.setRunType("CONTENT_PIPELINE");run.setTriggerType("SCHEDULED");long id=repository.insertOperationRun(run);try{ContentPipelineTestVO result=pipeline.processBoundSource(accountId,sourceId);Map<String,Object> metrics=new LinkedHashMap<>();metrics.put("accountId",accountId);metrics.put("sourceId",sourceId);metrics.put("contentItemId",result.getContentItemId());metrics.put("result",result.getResult());metrics.put("publicationId",result.getPublicationId());repository.finishOperationRun(id,json.writeValueAsString(metrics));}catch(Exception e){repository.failOperationRun(id,limit(e.getMessage(),1000));log.warn("Content pipeline run failed accountId={} sourceId={} code={}",accountId,sourceId,e.getClass().getSimpleName());}}
    private long number(Object value){return ((Number)value).longValue();}private String limit(String value,int max){if(value==null)return "未知错误";return value.length()<=max?value:value.substring(0,max);}
}
