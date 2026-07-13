$shortcut = Join-Path ([Environment]::GetFolderPath('Startup')) 'Local ComfyUI Bridge.lnk'
Remove-Item -LiteralPath $shortcut -Force -ErrorAction SilentlyContinue
Write-Host 'Local ComfyUI Bridge startup shortcut removed.'
