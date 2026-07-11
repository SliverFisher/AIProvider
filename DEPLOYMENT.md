# Aimaid 运行与维护

## 架构

WPF 使用只读 SQLite 扫描器检测指定业务表的新增和修改，把未发送记录持久化到 `data-sync-state.json`，断网后指数退避重试。Spring Boot 通过 `(DeviceId, EntityType, EntityId)` 唯一键幂等写入 MySQL。React 由 Nginx 静态托管；WebRTC 视频点对点传输，Java WebSocket 只转发 SDP/ICE 信令。

按用户要求，当前版本**完全没有鉴权**。不要直接暴露到不可信公网；建议至少用防火墙限制来源 IP 或通过 VPN 使用。

## 本地启动

1. 复制 `.env.example` 并设置数据库连接环境变量。
2. 后端：Java 17，运行 `mvn spring-boot:run`。
3. 前端：运行 `npm install && npm run dev`。
4. WPF 设置 `AIMAID_SERVER_URL=http://服务器地址` 和可选的 `AIMAID_DEVICE_ID`，然后启动应用。未设置 URL 时同步服务保持关闭。

## 服务器

- JAR：`/opt/aimaid/ai-provider.jar`
- 前端：`/var/www/aimaid`
- 环境：`/etc/aimaid/aimaid.env`（权限 `600`）
- systemd：`aimaid.service`
- Nginx：`/etc/nginx/conf.d/aimaid.conf`

常用命令：`systemctl status aimaid`、`journalctl -u aimaid -n 200 --no-pager`、`nginx -t`。

摄像头要求可信 HTTPS。绑定域名后应使用受信任证书，并把 HTTP 重定向到 HTTPS。当前仅有 IP 时，普通 HTTP 页面不能在手机浏览器调用摄像头。跨运营商网络直连失败时还需部署 coturn，并把 TURN 地址和临时凭据加入前端 ICE 配置。

当前使用 Cloudflare Quick Tunnel 提供临时 HTTPS。启动：`systemctl start cloudflared-quick`；停止：`systemctl stop cloudflared-quick`；查看当前地址：`journalctl -u cloudflared-quick -b --no-pager | grep -oE 'https://[A-Za-z0-9-]+\.trycloudflare\.com' | tail -1`。Quick Tunnel 重启后会生成新地址，需要同步更新 AI_maid 的 `DataSync.ServerUrl`。

## 备份与恢复

备份：`mysqldump --single-transaction ai_provider | gzip > ai_provider-$(date +%F).sql.gz`。恢复前先停服务，再执行 `gunzip -c backup.sql.gz | mysql ai_provider`。WPF 本地同时备份 `timer.db` 和 `data-sync-state.json`；复制 SQLite 前先退出 Aimaid。

Flyway 迁移位于 `AIProvider-back/src/main/resources/db/migration`。部署前备份数据库；Flyway 会记录已执行版本，不应手工删除 `flyway_schema_history`。
