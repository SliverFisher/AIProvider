# Codex 任务说明：远程前端控制本机 ComfyUI

## 一、先确认真实架构

当前项目结构是：

- 前端部署在远程服务器
- Java 后端部署在远程服务器
- ComfyUI 安装并运行在当前 Windows 电脑
- 使用者在这台 Windows 电脑的浏览器中打开远程前端页面
- ComfyUI 地址为 `http://127.0.0.1:8188`

本次需求不是远程控制另一台机器，也不是让远程 Java 后端操作 ComfyUI。

远程前端页面虽然由服务器提供，但页面 JavaScript 实际运行在当前 Windows 浏览器中，因此应由浏览器直接访问本机服务。

## 二、最终目标

在现有远程前端中增加一个 ComfyUI 工作台，实现：

- 查看本机 ComfyUI 是否运行
- 一键启动本机 ComfyUI
- 一键停止本机 ComfyUI
- 在前端填写 Prompt 和生成参数
- 调用本机 ComfyUI API 提交工作流
- 显示生成进度
- 生成完成后直接查看本机图片
- 下载本机生成结果

整个 ComfyUI 操作链路只发生在当前 Windows 电脑。

## 三、正确架构

```text
远程服务器上的前端静态资源
        ↓ 浏览器加载
Windows 本机浏览器中的前端 JavaScript
        ↓
Windows 本机启动器 / 桥接器
        ↓
Windows 本机 ComfyUI（127.0.0.1:8188）
        ↓
Windows 本机 ComfyUI/output
```

远程 Java 后端不参与以下流程：

- 不启动 ComfyUI
- 不停止 ComfyUI
- 不转发 ComfyUI 请求
- 不接收 Prompt
- 不接收工作流
- 不接收输入图片
- 不接收生成图片
- 不保存 ComfyUI 任务状态

## 四、为什么需要一个本机启动器

浏览器不能直接执行 Windows 的 `.bat`、`.exe` 或系统命令。

因此只需要实现一个很小的本机启动器，也可以称为本机桥接器。

它不是远程 Agent，不连接服务器，不上传数据，不做远程任务调度。

它只负责：

- 检查 ComfyUI 是否运行
- 启动固定的 ComfyUI 启动脚本
- 停止由它启动的 ComfyUI 进程
- 向本机浏览器提供状态接口
- 必要时代理本机 ComfyUI API，解决 CORS、WebSocket 和浏览器访问限制

## 五、本机启动器要求

优先使用 C# 实现一个轻量 Windows 程序，可使用：

- .NET Minimal API
- 控制台程序
- 托盘程序

建议最终打包成单个可执行文件。

启动器只监听：

```text
127.0.0.1
```

不得监听：

```text
0.0.0.0
```

建议端口：

```text
127.0.0.1:32145
```

至少提供：

```text
GET  /api/comfy/status
POST /api/comfy/start
POST /api/comfy/stop
```

返回统一 JSON，例如：

```json
{
  "success": true,
  "running": true,
  "message": "ComfyUI is running"
}
```

## 六、一键启动 ComfyUI

启动器读取本机配置中的 ComfyUI 启动脚本路径，例如：

```json
{
  "comfyUiBaseUrl": "http://127.0.0.1:8188",
  "startScript": "D:\\ComfyUI\\run_nvidia_gpu.bat",
  "workingDirectory": "D:\\ComfyUI"
}
```

要求：

- 路径来自本地配置
- 不允许前端传入任意命令或脚本路径
- 启动前先检测 `127.0.0.1:8188`
- 已运行时不得重复启动
- 启动后轮询 ComfyUI 状态
- ComfyUI 真正可访问后再返回成功
- 启动超时应返回明确错误
- 保存启动后的进程信息，供停止操作使用

## 七、一键停止 ComfyUI

停止功能只能停止：

- 由当前启动器启动的 ComfyUI 进程
- 或配置中明确允许停止的 ComfyUI 进程

不得实现任意进程终止接口。

停止后重新检查 `127.0.0.1:8188`，确认服务已关闭。

## 八、前端访问方式

前端页面虽然部署在远程服务器，但调用本机启动器时必须使用：

```text
http://127.0.0.1:32145
```

不要把请求发给远程 Java 后端。

前端需要：

- 页面加载时查询本机启动器状态
- 启动器未安装或未运行时显示明确提示
- 点击“启动 ComfyUI”后调用本机启动器
- 启动成功后继续调用本机 ComfyUI
- 点击“停止 ComfyUI”后刷新状态
- 所有本机请求超时后给出可理解的错误信息

## 九、CORS 和浏览器限制

本机启动器需要正确处理 CORS 和预检请求。

只允许现有前端的准确来源，例如：

```text
https://实际前端域名
```

开发环境可额外允许：

```text
http://localhost:5173
```

不要使用：

```text
Access-Control-Allow-Origin: *
```

至少处理：

- `OPTIONS`
- `GET`
- `POST`
- 必要请求头
- Private Network Access 相关预检
- WebSocket 连接限制

如浏览器直接访问 ComfyUI 存在 CORS、混合内容或 WebSocket 问题，则由本机启动器反向代理 ComfyUI API。

## 十、ComfyUI API 代理

为了让远程前端稳定操作本机 ComfyUI，建议启动器提供本机代理接口，例如：

```text
127.0.0.1:32145/comfy/*
```

代理到：

```text
127.0.0.1:8188/*
```

需要支持现有工作台实际使用的接口：

- 提交 API Format 工作流
- 上传输入图片
- 查询任务历史
- 获取队列状态
- 获取生成结果
- 获取图片
- WebSocket 进度事件

如果前端能够稳定直接访问 `127.0.0.1:8188`，可不做代理；但必须经过实际浏览器测试后再决定。

## 十一、图片和数据流向

所有数据必须保持本地：

```text
浏览器
→ 本机启动器或本机 ComfyUI
→ 本机 ComfyUI/output
```

明确禁止：

- 上传 Prompt 到远程 Java 后端
- 上传工作流到远程 Java 后端
- 上传参考图片到远程服务器
- 上传生成结果到远程服务器
- 由远程服务器中转图片
- 在远程数据库保存生成图片

生成完成后，前端直接通过本机 ComfyUI `/view` 或本机代理查看图片。

## 十二、前端工作台功能

在现有前端中增加一个 ComfyUI 页面，至少包含：

- 本机启动器状态
- ComfyUI 运行状态
- 启动按钮
- 停止按钮
- 工作流选择
- 正向 Prompt
- 反向 Prompt
- Seed
- Width
- Height
- Steps
- CFG
- Sampler
- Scheduler
- 输入图片上传
- 开始生成
- 当前进度
- 完成状态
- 查看结果
- 下载结果
- 本机最近任务记录

不要复制 ComfyUI 节点编辑器。

目标是把常用生成操作做成更简单、更漂亮的表单界面。

## 十三、工作流

使用 ComfyUI 导出的 API Format JSON。

前端或本机启动器在提交前，仅修改需要暴露的节点参数，例如：

- 正向 Prompt
- 反向 Prompt
- Seed
- Width
- Height
- Steps
- CFG
- Sampler
- Scheduler
- 输入图片名称
- 输出文件名前缀

不要修改原始工作流文件。

每次生成使用一份任务副本。

## 十四、进度和完成状态

提交工作流后保存 `prompt_id`。

优先监听 ComfyUI WebSocket：

- `progress`
- `executing`
- `executed`
- `execution_success`
- 错误事件

如果 WebSocket 漏掉完成事件，则轮询：

```text
/history/{prompt_id}
```

任务完成时：

- 前端进度设为 100%
- 显示“生成完成”
- 显示“查看结果”
- 不上传图片
- 不等待远程服务器处理

## 十五、本机启动器自动运行

为了实现真正的一键启动 ComfyUI，本机启动器本身应支持随 Windows 自动启动。

可采用：

- Windows 启动项
- 任务计划程序
- 托盘程序开机启动

启动器资源占用应尽量低。

正常情况下：

1. Windows 开机后启动器自动运行
2. 使用者打开远程前端
3. 点击“启动 ComfyUI”
4. 本机 ComfyUI 自动启动
5. 直接开始生成

不应要求每次手动先运行启动器。

## 十六、安全边界

必须做到：

- 启动器只监听 `127.0.0.1`
- 不接受公网连接
- 不主动连接远程服务器
- 不上传任何本地数据
- 不允许执行任意命令
- 不允许前端传入脚本路径
- 只允许指定前端 Origin
- 对启动、停止接口增加本机随机 Token
- Token 保存在本机配置中
- 前端只能从本机读取 Token 或通过安装流程配置
- 日志不输出敏感信息

## 十七、明确不要做的事情

不要实现：

- 远程 Agent
- Agent 与 Java 后端的 WebSocket 长连接
- 图片上传服务器
- Prompt 上传服务器
- 服务器任务队列
- 服务器保存 ComfyUI 历史
- 远程 Java 后端调用本机 ComfyUI
- 公网访问本机 ComfyUI
- TURN、Tunnel 或内网穿透
- 多机器调度
- 多用户系统

## 十八、实施顺序

1. 检查现有前端 ComfyUI 页面和相关代码
2. 删除或停用错误的远程上传、远程 Agent、远程任务逻辑
3. 定义本机启动器接口
4. 实现本机状态、启动、停止
5. 配置严格 CORS
6. 前端改为访问 `127.0.0.1`
7. 接入本机 ComfyUI API
8. 修复进度和完成状态
9. 实现本机结果查看与下载
10. 配置启动器开机自动运行
11. 完成端到端测试

## 十九、验收标准

完成后必须满足：

- 远程前端可以在当前 Windows 浏览器中打开
- 页面可以检测本机启动器
- 页面可以一键启动本机 ComfyUI
- 不需要手动运行 ComfyUI
- 页面可以停止本机 ComfyUI
- 页面可以直接提交本机工作流
- Prompt 和图片不经过远程 Java 后端
- 生成图片不上传服务器
- 图片保存在本机 `ComfyUI/output`
- 生成完成后立即显示 100%
- 点击查看结果直接读取本机图片
- 断开互联网后，已加载的本地工作台核心生成链路仍不依赖远程 Java 后端
- 本机启动器不开放公网端口

请严格按照此架构改造，不要再次引入远程 Agent、远程图片上传或远程 Java 后端中转。
