@echo off
setlocal
rem Verify the recorded command line before stopping a PID that Windows may have reused.
set "SM_ROOT=%~dp0"
set "SM_RUN_DIR=%SM_ROOT%.run"
if not exist "%SM_RUN_DIR%" (
    echo No School Management process records found.
    exit /b 0
)

powershell -NoProfile -Command "$root = $env:SM_ROOT.ToLowerInvariant(); foreach ($name in @('frontend','api-gateway','inventory-asset-service','timetable-substitution-service','teacher-hr-service','auth-user-service','discovery-server')) { $file = Join-Path $env:SM_RUN_DIR ($name + '.pid'); if (-not (Test-Path $file)) { continue }; $processId = [int](Get-Content $file); $process = Get-CimInstance Win32_Process -Filter ('ProcessId = ' + $processId); $expected = if ($name -eq 'frontend') { 'vite.js' } else { $name + '-0.1.0-SNAPSHOT.jar' }; if ($process -and $process.CommandLine -and $process.CommandLine.ToLowerInvariant().Contains($root) -and $process.CommandLine.Contains($expected)) { Stop-Process -Id $processId -Force; Write-Host ('Stopped ' + $name + ' (' + $processId + ')') } else { Write-Host ('Skipped stale process record: ' + $name) }; Remove-Item -LiteralPath $file -Force }"
exit /b %errorlevel%
