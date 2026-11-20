param (
    [string]$RunId = ""
)

$repo = "ivanJossephChuctayaPaucar/OraculoLoto5X"
$destProj = "C:\Proyectos\OraculoLoto5X"
$destDownloads = "C:\Users\Ivan Josseph\Downloads"

# Extraer token de Git Credential Manager
$cred = @("protocol=https", "host=github.com", "") | git credential fill
$token = ""
foreach ($line in $cred) {
    if ($line -like "password=*") {
        $token = $line.Substring(9)
    }
}

$headers = @{
    "User-Agent" = "Antigravity-Agent"
}
if ($token) {
    $headers["Authorization"] = "Bearer $token"
}

# Si no se pasó RunId, obtener la ejecución más reciente en 'main'
if ([string]::IsNullOrWhiteSpace($RunId)) {
    Write-Host "Buscando la ejecución más reciente en GitHub Actions..."
    $runsResp = Invoke-RestMethod -Uri "https://api.github.com/repos/$repo/actions/runs?branch=main&per_page=1" -Headers $headers
    if ($runsResp.total_count -eq 0) {
        Write-Error "No se encontraron ejecuciones de workflows en el repositorio."
        exit 1
    }
    $targetRun = $runsResp.workflow_runs[0]
    $RunId = $targetRun.id
    Write-Host "Ejecución detectada: Run #$RunId ($($targetRun.name)) - Commit: $($targetRun.head_sha.Substring(0,7))"
}

Write-Host "Monitoreando estado de la compilación (Run ID: $RunId)..."
$finished = $false
while (-not $finished) {
    $run = Invoke-RestMethod -Uri "https://api.github.com/repos/$repo/actions/runs/$RunId" -Headers $headers
    Write-Host "[$(Get-Date -Format 'HH:mm:ss')] Estado: $($run.status) | Conclusión: $($run.conclusion)"
    if ($run.status -eq "completed") {
        $finished = $true
        if ($run.conclusion -ne "success") {
            Write-Error "La compilación en GitHub Actions no fue exitosa (Estado: $($run.conclusion)). Revisa los logs en: $($run.html_url)"
            exit 1
        }
    } else {
        Start-Sleep -Seconds 6
    }
}

Write-Host "¡Compilación exitosa! Obteniendo el artefacto APK..."
$tempZip = Join-Path $destProj "apk_temp.zip"
$tempExtract = Join-Path $destProj "apk_temp_extracted"
New-Item -ItemType Directory -Force -Path $tempExtract | Out-Null

$artResp = Invoke-RestMethod -Uri "https://api.github.com/repos/$repo/actions/runs/$RunId/artifacts" -Headers $headers
if ($artResp.total_count -eq 0) {
    Write-Error "No se encontraron artefactos asociados a la ejecución."
    exit 1
}

$art = $artResp.artifacts[0]
Write-Host "Descargando artefacto '$($art.name)' ($([Math]::Round($art.size_in_bytes / 1MB, 2)) MB)..."

Invoke-WebRequest -Uri $art.archive_download_url -Headers $headers -OutFile $tempZip
Expand-Archive -Path $tempZip -DestinationPath $tempExtract -Force
Remove-Item $tempZip -Force

$foundApk = Get-ChildItem -Path $tempExtract -Filter "*.apk" -Recurse | Select-Object -First 1
if ($null -ne $foundApk) {
    $targetFile1 = Join-Path $destProj "OraculoLoto5X-v3.0.1.apk"
    $targetFile2 = Join-Path $destDownloads "OraculoLoto5X-v3.0.1.apk"
    Copy-Item -Path $foundApk.FullName -Destination $targetFile1 -Force
    Copy-Item -Path $foundApk.FullName -Destination $targetFile2 -Force
    Remove-Item $tempExtract -Recurse -Force
    Write-Host "============================================="
    Write-Host "APK LISTO Y DESCARGADO EXITOSAMENTE:"
    Write-Host "1. En Proyecto: $targetFile1"
    Write-Host "2. En Descargas: $targetFile2"
    Write-Host "Tamaño: $([Math]::Round((Get-Item $targetFile1).Length / 1MB, 2)) MB"
    Write-Host "============================================="
} else {
    Write-Error "No se encontró ningún archivo .apk dentro del zip descargado."
    exit 1
}
