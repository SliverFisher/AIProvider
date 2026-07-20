# SaveVideo filenamePrefix 绑定修复设计

## 问题

本地工作流导入器只把 `SaveImage` 识别为最终输出节点。`Wan22_I2V_Anime_Q4KM_Complete_Workflow.json` 的最终输出节点是 `SaveVideo`，其 `filename_prefix` 因而没有生成语义字段 `filenamePrefix`，最终触发“工作流绑定缺少字段 filenamePrefix”。

## 设计

- 最终输出识别同时接受 `SaveImage` 与 `SaveVideo`。
- 仍优先选择标题包含“最终”或 `final` 的输出节点；没有匹配时再选择首个支持的输出节点。
- 仅当该节点存在 `filename_prefix` 输入时，生成 `filenamePrefix` 绑定及默认值。
- 保持图片工作流、其他自动暴露字段和输出节点结构不变。

## 验证

- 新增回归测试，使用带 `SaveVideo.inputs.filename_prefix` 的最小工作流，先证明当前实现漏绑字段。
- 修复后验证 `binding.fields.filenamePrefix` 指向视频输出节点的 `filename_prefix`，默认值保持工作流中的前缀。
- 运行 ComfyUI Bridge 相关定向测试，不运行无关全量测试。
