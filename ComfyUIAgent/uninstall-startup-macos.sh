#!/bin/zsh
set -euo pipefail
PLIST="$HOME/Library/LaunchAgents/com.aiprovider.comfyuiagent.plist"
launchctl bootout "gui/$(id -u)" "$PLIST" 2>/dev/null || true
rm -f "$PLIST"
echo "macOS ComfyUI Agent 已移除"
