#!/bin/zsh
set -euo pipefail

AGENT_DIR="/Users/Shared/AIProvider/ComfyUIAgent"
PLIST="$HOME/Library/LaunchAgents/com.aiprovider.comfyuiagent.plist"

if [[ ! -x "$AGENT_DIR/ComfyUIAgent" ]]; then
  echo "请先把 macOS 发布包复制到 $AGENT_DIR"
  exit 1
fi
if [[ ! -f "$AGENT_DIR/appsettings.json" ]]; then
  echo "缺少 $AGENT_DIR/appsettings.json"
  exit 1
fi

mkdir -p "$HOME/Library/LaunchAgents"
cat > "$PLIST" <<PLIST
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0"><dict>
  <key>Label</key><string>com.aiprovider.comfyuiagent</string>
  <key>ProgramArguments</key><array><string>$AGENT_DIR/ComfyUIAgent</string></array>
  <key>WorkingDirectory</key><string>$AGENT_DIR</string>
  <key>RunAtLoad</key><true/>
  <key>KeepAlive</key><true/>
  <key>StandardOutPath</key><string>$AGENT_DIR/agent.log</string>
  <key>StandardErrorPath</key><string>$AGENT_DIR/agent-error.log</string>
</dict></plist>
PLIST

launchctl bootout "gui/$(id -u)" "$PLIST" 2>/dev/null || true
launchctl bootstrap "gui/$(id -u)" "$PLIST"
echo "macOS ComfyUI Agent 已安装并启动"
