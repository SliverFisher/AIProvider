# 情报雷达固定采集来源

更新时间：2026-07-21

## Agent-Reach

- 上游仓库：`https://github.com/Panniantong/Agent-Reach`
- 固定提交：`1494c2ab239e7355a77e7cceaf3271453a1f34b5`
- 项目目录：`third_party/agent-reach`
- 项目用途：作为采集命令、健康探测、输出约束和平台接入方式的固定参考实现。

情报雷达不会启用上游的自动路由。首批平台固定为：

| 平台 | 唯一执行器 | 上游依据 |
| --- | --- | --- |
| GitHub | `gh` | `agent_reach/channels/github.py` |
| RSS / Atom | Python `feedparser` | `agent_reach/channels/rss.py` |
| 普通网页 | `https://r.jina.ai/<URL>` | `agent_reach/channels/web.py` |
| YouTube | `yt-dlp` | `agent_reach/channels/youtube.py` |

本机和生产服务器的能力检查统一运行 `scripts/Test-IntelligenceCollectors.ps1`。输出仅包含执行器、可用状态、版本和稳定错误代码。
