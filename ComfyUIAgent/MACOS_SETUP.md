# macOS 本机部署位置

1. 将 ComfyUI 整个文件夹复制到 `/Users/Shared/ComfyUI`。
2. Python 虚拟环境路径应为 `/Users/Shared/ComfyUI/.venv/bin/python3`，主程序应为 `/Users/Shared/ComfyUI/main.py`。
3. 将 `dotnet publish -c Release -r osx-arm64 --self-contained false` 的发布结果复制到 `/Users/Shared/AIProvider/ComfyUIAgent`。
4. 把 `appsettings.example.json` 复制为 `appsettings.json`，填写随机 LocalToken，并确认前端 Origin。
5. 执行 `chmod +x ComfyUIAgent install-startup-macos.sh`，再运行 `./install-startup-macos.sh`。

Intel Mac 将发布 RID 改为 `osx-x64`。路径尚未准备好时 Agent 仍可启动，网页会显示 macOS 配置占位和预期目录，但“启动 ComfyUI”按钮不会执行。
