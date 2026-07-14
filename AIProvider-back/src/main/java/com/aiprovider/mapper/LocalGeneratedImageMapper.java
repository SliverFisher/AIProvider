package com.aiprovider.mapper;

import com.aiprovider.model.dto.LocalGeneratedImageItemDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LocalGeneratedImageMapper {
    @Insert("INSERT INTO c_LocalGeneratedImages(Platform,PathHash,PromptId,ImagePath,FileName,WorkflowId,WorkflowName,Prompt,NegativePrompt,LorasJson,Seed,Steps,Cfg,Sampler,Scheduler,Width,Height,TaskCreatedAt,GenerationCompletedAt,GenerationDurationMs) " +
            "VALUES(#{platform},#{pathHash},#{item.promptId},#{item.imagePath},#{item.fileName},#{item.workflowId},#{item.workflowName},#{item.prompt},#{item.negativePrompt},#{item.lorasJson},#{item.seed},#{item.steps},#{item.cfg},#{item.sampler},#{item.scheduler},#{item.width},#{item.height},#{item.taskCreatedAt},#{item.generationCompletedAt},#{item.generationDurationMs}) " +
            "ON DUPLICATE KEY UPDATE PromptId=VALUES(PromptId),FileName=VALUES(FileName),WorkflowId=VALUES(WorkflowId),WorkflowName=VALUES(WorkflowName),Prompt=VALUES(Prompt),NegativePrompt=VALUES(NegativePrompt),LorasJson=VALUES(LorasJson),Seed=VALUES(Seed),Steps=VALUES(Steps),Cfg=VALUES(Cfg),Sampler=VALUES(Sampler),Scheduler=VALUES(Scheduler),Width=VALUES(Width),Height=VALUES(Height),TaskCreatedAt=VALUES(TaskCreatedAt),GenerationCompletedAt=VALUES(GenerationCompletedAt),GenerationDurationMs=VALUES(GenerationDurationMs),UpdatedAt=CURRENT_TIMESTAMP(3)")
    int upsert(@Param("platform") String platform, @Param("pathHash") String pathHash, @Param("item") LocalGeneratedImageItemDTO item);
}
