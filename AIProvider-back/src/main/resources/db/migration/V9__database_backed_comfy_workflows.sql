CREATE TABLE IF NOT EXISTS ComfyWorkflows (
  Id VARCHAR(64) NOT NULL PRIMARY KEY,
  Name VARCHAR(120) NOT NULL,
  Description VARCHAR(500) NULL,
  DefinitionJson JSON NOT NULL,
  BindingJson JSON NOT NULL,
  DefaultParametersJson JSON NOT NULL,
  Active BOOLEAN NOT NULL DEFAULT TRUE,
  CreatedAt DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  UpdatedAt DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ComfyParameterSchemes (
  Id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  Title VARCHAR(100) NOT NULL,
  WorkflowId VARCHAR(64) NOT NULL,
  ParametersJson JSON NOT NULL,
  OutputFolder VARCHAR(240) NOT NULL DEFAULT 'aimaid',
  CreatedAt DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  UpdatedAt DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT FK_ComfyParameterSchemes_Workflow FOREIGN KEY (WorkflowId) REFERENCES ComfyWorkflows(Id),
  KEY IX_ComfyParameterSchemes_UpdatedAt (UpdatedAt),
  KEY IX_ComfyParameterSchemes_WorkflowId (WorkflowId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DELETE FROM ComfyParameterSchemes;
DELETE FROM ComfyWorkflows;

INSERT INTO ComfyWorkflows(Id, Name, Description, DefinitionJson, BindingJson, DefaultParametersJson, Active)
VALUES (
  'futa01',
  'Futa 01 · 竖版文生图',
  '用户提供的 futa01 API 工作流',
  '{"1":{"inputs":{"ckpt_name":"noobai\\\\waiNSFWIllustrious_v150.safetensors"},"class_type":"CheckpointLoaderSimple","_meta":{"title":"主模型"}},"3":{"inputs":{"text":"score_4, score_3, low quality, blurry, bad anatomy, close-up, medium shot, cowboy shot, lower-body focus, leg focus, foot focus, crotch focus, zoomed in, cropped, out of frame, cut off, partial body, missing head, missing torso, missing arms, missing hands, missing legs, missing feet, extra limbs, deformed limbs, bad composition, text, watermark, logo, censored shoes","clip":["30",1]},"class_type":"CLIPTextEncode","_meta":{"title":"负向提示词"}},"4":{"inputs":{"seed":217713203340610,"steps":30,"cfg":5,"sampler_name":"uni_pc","scheduler":"normal","denoise":1,"model":["30",0],"positive":["28",0],"negative":["3",0],"latent_image":["5",0]},"class_type":"KSampler","_meta":{"title":"采样参数"}},"5":{"inputs":{"width":1080,"height":1920,"batch_size":1},"class_type":"EmptyLatentImage","_meta":{"title":"生成尺寸"}},"6":{"inputs":{"samples":["4",0],"vae":["15",0]},"class_type":"VAEDecode","_meta":{"title":"VAE 解码"}},"7":{"inputs":{"filename_prefix":"AIProvider","images":["6",0]},"class_type":"SaveImage","_meta":{"title":"最终输出"}},"15":{"inputs":{"vae_name":"sdxl_vae.safetensors"},"class_type":"VAELoader","_meta":{"title":"外置 VAE"}},"28":{"inputs":{"text":"futanari girl, white silk thighhighs, tiny penis, detailed penis, full_body","clip":["30",1],"trigger_words1":["29",0]},"class_type":"Prompt (LoraManager)","_meta":{"title":"正向提示词"}},"29":{"inputs":{"group_mode":true,"default_active":true,"allow_strength_adjustment":false,"toggle_trigger_words":{"__value__":[]},"orinalMessage":"","trigger_words":["30",3]},"class_type":"TriggerWord Toggle (LoraManager)","_meta":{"title":"LoRA 触发词开关"}},"30":{"inputs":{"text":"","loras":{"__value__":[]},"model":["1",0],"clip":["1",1]},"class_type":"Lora Loader (LoraManager)","_meta":{"title":"LoRA 加载器"}}}',
  '{"fields":{"positivePrompt":{"nodeTitle":"正向提示词","input":"text"},"negativePrompt":{"nodeTitle":"负向提示词","input":"text"},"width":{"nodeTitle":"生成尺寸","input":"width"},"height":{"nodeTitle":"生成尺寸","input":"height"},"batchSize":{"nodeTitle":"生成尺寸","input":"batch_size"},"seed":{"nodeTitle":"采样参数","input":"seed"},"steps":{"nodeTitle":"采样参数","input":"steps"},"cfg":{"nodeTitle":"采样参数","input":"cfg"},"sampler":{"nodeTitle":"采样参数","input":"sampler_name"},"scheduler":{"nodeTitle":"采样参数","input":"scheduler"},"denoise":{"nodeTitle":"采样参数","input":"denoise"},"filenamePrefix":{"nodeTitle":"最终输出","input":"filename_prefix"}},"outputNode":{"title":"最终输出"},"capabilities":{"styleReference":false,"poseReference":false,"controlNet":false}}',
  '{"positivePrompt":"futanari girl, white silk thighhighs, tiny penis, detailed penis, full_body","negativePrompt":"score_4, score_3, low quality, blurry, bad anatomy, close-up, medium shot, cowboy shot, lower-body focus, leg focus, foot focus, crotch focus, zoomed in, cropped, out of frame, cut off, partial body, missing head, missing torso, missing arms, missing hands, missing legs, missing feet, extra limbs, deformed limbs, bad composition, text, watermark, logo, censored shoes","seed":217713203340610,"randomSeed":false,"width":1080,"height":1920,"batchSize":1,"steps":30,"cfg":5,"denoise":1,"sampler":"uni_pc","scheduler":"normal"}',
  TRUE
);
