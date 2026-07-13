# AIProvider X Publisher

1. 在 Chrome 打开 `chrome://extensions`。
2. 开启右上角“开发者模式”。
3. 点击“加载已解压的扩展程序”。
4. 选择本目录 `TwitterChromeExtension`。
5. 在当前 Chrome 登录 `https://x.com`，然后回到 AIProvider 的 Twitter 页面填写用户名并连接 Session。

前端负责编辑、创建定时任务和查看记录。扩展后台固定每 1 分钟向服务器查询到期任务；即使 AIProvider 页面已关闭或切到其他网页，只要 Chrome 正在运行、电脑没有休眠、本机 ComfyUI Agent 可用，就会从本机路径读取图片并发布。扩展通过 Chrome Cookies API 检查当前 `auth_token`/`ct0`，动态读取 X Web 客户端的 Bearer Token 与 `CreateTweet` Query ID，然后直接调用媒体上传和 GraphQL 接口。

扩展不会打开 X 发布页面，不会填写网页表单，不会模拟鼠标点击，也不会使用图像识别。Cookie 值不会发送给 AIProvider 前端或服务器。
