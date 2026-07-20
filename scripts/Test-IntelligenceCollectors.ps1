[CmdletBinding()]
param(
    [string]$JinaProbeUrl = "https://example.com"
)

$ErrorActionPreference = "Stop"

function Write-ProbeResult {
    param(
        [string]$Adapter,
        [bool]$Available,
        [string]$Version,
        [string]$ErrorCode
    )

    [ordered]@{
        adapter = $Adapter
        available = $Available
        version = $Version
        errorCode = $ErrorCode
    } | ConvertTo-Json -Compress
}

function Invoke-CommandProbe {
    param(
        [string]$Adapter,
        [string]$Command,
        [string[]]$Arguments
    )

    if (-not (Get-Command $Command -ErrorAction SilentlyContinue)) {
        Write-ProbeResult $Adapter $false $null "EXECUTOR_MISSING"
        return
    }

    try {
        $output = & $Command @Arguments 2>$null
        if ($LASTEXITCODE -ne 0) {
            Write-ProbeResult $Adapter $false $null "EXECUTOR_UNAVAILABLE"
            return
        }
        $version = (($output | Select-Object -First 1) -as [string]).Trim()
        Write-ProbeResult $Adapter $true $version $null
    } catch {
        Write-ProbeResult $Adapter $false $null "EXECUTOR_UNAVAILABLE"
    }
}

Invoke-CommandProbe "GITHUB" "gh" @("--version")
Invoke-CommandProbe "RSS" "python" @("-c", "import feedparser; print(feedparser.__version__)")

try {
    $response = Invoke-WebRequest -UseBasicParsing -Uri ("https://r.jina.ai/" + $JinaProbeUrl) -Method Get -TimeoutSec 15
    if ($response.StatusCode -eq 200) {
        Write-ProbeResult "WEB" $true "jina-reader-http" $null
    } else {
        Write-ProbeResult "WEB" $false $null "UPSTREAM_UNAVAILABLE"
    }
} catch {
    Write-ProbeResult "WEB" $false $null "UPSTREAM_UNAVAILABLE"
}

Invoke-CommandProbe "YOUTUBE" "yt-dlp" @("--version")
