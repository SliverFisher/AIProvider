# Twitter 当前 Chrome 发布链路

生产模式为 `TWITTER_PUBLISH_MODE=client`。腾讯云保存账号名称、待发布文字、图片、状态、时间与结果，不访问 X/Twitter。

## 数据流

1. 前端把文字和最多 4 张图片上传到云端，后端写入 `TwitterPosts` 与 `TwitterPostMedia`。
2. 页面按 1、5、15 或 30 分钟轮询 `/api/twitter/posts/pending`。
3. 页面认领任务并下载原图，然后通过页面内容脚本交给 `TwitterChromeExtension`。
4. 扩展读取当前 Chrome 的 `auth_token`/`ct0`，直接调用 X 媒体上传和 `CreateTweet` GraphQL 接口。
5. 前端通过 `/client-result` 把成功链接或错误写回云端。

页面必须保持打开，浏览器定时器才会继续轮询。

## Chrome 扩展

扩展源码位于仓库根目录 `TwitterChromeExtension`。在 `chrome://extensions` 开启开发者模式，选择“加载已解压的扩展程序”，加载该目录即可。

扩展只在以下页面运行：

- AIProvider 生产前端、服务器 IP 前端和本地开发前端；
- `https://x.com/*` 与 `https://twitter.com/*`。

扩展不需要手工复制 Cookie。它通过 Chrome Cookies API 读取当前 Session，Cookie 值不发送给前端或服务器。扩展不会打开 X 发布页、操作 DOM、模拟鼠标或使用图像识别。

## 云端接口

- `POST /api/twitter/accounts/client-status`：登记扩展检测到的账号名称和状态。
- `POST /api/twitter/posts`：上传文字和图片并建立待发布任务。
- `GET /api/twitter/posts/pending?accountId={id}&limit=10`：读取待发布任务。
- `POST /api/twitter/posts/{id}/claim`：原子认领任务。
- `POST /api/twitter/posts/{id}/client-result`：写回 `SENT` 或 `FAILED`。
- `GET /api/twitter/posts/{postId}/images/{imageId}`：下载任务原图。

认领后超过 10 分钟仍为 `PROCESSING` 的任务会自动恢复为 `PENDING`。
