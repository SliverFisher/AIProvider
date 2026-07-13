$ErrorActionPreference = 'Stop'
$exe = Join-Path $PSScriptRoot 'ComfyUIAgent.exe'
if (-not (Test-Path -LiteralPath $exe)) { throw "ComfyUIAgent.exe not found beside this script." }
$shortcut = Join-Path ([Environment]::GetFolderPath('Startup')) 'Local ComfyUI Bridge.lnk'
$shell = New-Object -ComObject WScript.Shell
$link = $shell.CreateShortcut($shortcut)
$link.TargetPath = $exe
$link.WorkingDirectory = $PSScriptRoot
$link.WindowStyle = 7
$link.Description = 'Local-only ComfyUI browser bridge'
$link.Save()
Write-Host "Installed startup shortcut: $shortcut"
