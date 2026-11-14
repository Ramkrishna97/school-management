@echo off
setlocal
set "SM_ROOT=%~dp0"
set "SM_RUN_DIR=%SM_ROOT%.run"
pushd "%SM_ROOT%" || exit /b 1

if not defined JAVA_HOME set "JAVA_HOME=%ProgramFiles%\Java\jdk-17"
if not defined SCHOOL_BOOTSTRAP_PASSWORD set "SCHOOL_BOOTSTRAP_PASSWORD=ChangeMe123!"
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo Java 17 was not found. Set JAVA_HOME to a JDK 17 installation.
    popd
    exit /b 1
)
powershell -NoProfile -Command "$version = & (Join-Path $env:JAVA_HOME 'bin\java.exe') -version 2>&1; if (($version | Out-String) -notmatch '17[.]') { exit 1 }"
if errorlevel 1 (
    echo JAVA_HOME must point to Java 17.
    popd
    exit /b 1
)

if not exist "%SM_RUN_DIR%" mkdir "%SM_RUN_DIR%"
powershell -NoProfile -Command "$root = $env:SM_ROOT.ToLowerInvariant(); foreach ($name in @('discovery-server','auth-user-service','teacher-hr-service','timetable-substitution-service','inventory-asset-service','api-gateway','frontend')) { $file = Join-Path $env:SM_RUN_DIR ($name + '.pid'); if (Test-Path $file) { $processId = [int](Get-Content $file); $process = Get-CimInstance Win32_Process -Filter ('ProcessId = ' + $processId); if ($process -and $process.CommandLine -and $process.CommandLine.ToLowerInvariant().Contains($root)) { Write-Host ('Already running: ' + $name); exit 1 } } }"
if errorlevel 1 (
    echo Use stop.cmd before starting another copy.
    popd
    exit /b 1
)

echo Building backend services...
call "%SM_ROOT%gradlew.bat" bootJar
if errorlevel 1 goto failed

if not exist "%SM_ROOT%frontend\node_modules\vite\bin\vite.js" call :installFrontend
if errorlevel 1 goto failed

echo Starting Eureka, auth, teacher HR, timetable, inventory, gateway, and frontend...
call :launch discovery-server
if errorlevel 1 goto failed
call :launch auth-user-service
if errorlevel 1 goto failed
call :launch teacher-hr-service
if errorlevel 1 goto failed
call :launch timetable-substitution-service
if errorlevel 1 goto failed
call :launch inventory-asset-service
if errorlevel 1 goto failed
call :launch api-gateway
if errorlevel 1 goto failed

powershell -NoProfile -Command "$vite = Join-Path $env:SM_ROOT 'frontend\node_modules\vite\bin\vite.js'; $node = (Get-Command node.exe -ErrorAction Stop).Source; $process = Start-Process -FilePath $node -ArgumentList ([char]34 + $vite + [char]34 + ' --host 127.0.0.1 --port 5173 --strictPort') -WorkingDirectory (Join-Path $env:SM_ROOT 'frontend') -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $env:SM_RUN_DIR 'frontend.out.log') -RedirectStandardError (Join-Path $env:SM_RUN_DIR 'frontend.err.log'); Set-Content -LiteralPath (Join-Path $env:SM_RUN_DIR 'frontend.pid') -Value $process.Id; Write-Host ('frontend PID ' + $process.Id)"
if errorlevel 1 goto failed

echo Waiting for gateway and inter-service routing...
powershell -NoProfile -Command "$login = $null; for ($attempt = 0; $attempt -lt 60; $attempt++) { try { if ($null -eq $login) { $body = @{ username = 'principal'; password = $env:SCHOOL_BOOTSTRAP_PASSWORD } | ConvertTo-Json; $login = Invoke-RestMethod -Method Post 'http://localhost:8080/api/auth/login' -ContentType 'application/json' -Body $body -TimeoutSec 3 }; $headers = @{ Authorization = 'Bearer ' + $login.accessToken }; $null = Invoke-RestMethod 'http://localhost:8080/api/teachers' -Headers $headers -TimeoutSec 3; $null = Invoke-RestMethod 'http://localhost:8080/api/timetables?dayOfWeek=MONDAY' -Headers $headers -TimeoutSec 3; $null = Invoke-RestMethod 'http://localhost:8080/api/inventory/items' -Headers $headers -TimeoutSec 3; $date = Get-Date -Format 'yyyy-MM-dd'; $null = Invoke-RestMethod -Method Post ('http://localhost:8080/api/substitutions/allocate-absences/' + $date) -Headers $headers -TimeoutSec 3; $page = Invoke-WebRequest 'http://localhost:5173/' -UseBasicParsing -TimeoutSec 3; if ($page.StatusCode -eq 200) { exit 0 } } catch {} Start-Sleep -Seconds 5 }; exit 1"
if errorlevel 1 goto failed

echo All services are ready. Opening http://localhost:5173/
start "" "http://localhost:5173/"
popd
exit /b 0

:launch
set "SM_SERVICE=%~1"
powershell -NoProfile -Command "$name = $env:SM_SERVICE; $jar = Join-Path $env:SM_ROOT ($name + '\build\libs\' + $name + '-0.1.0-SNAPSHOT.jar'); $java = Join-Path $env:JAVA_HOME 'bin\java.exe'; $process = Start-Process -FilePath $java -ArgumentList ('-jar ' + [char]34 + $jar + [char]34) -WorkingDirectory $env:SM_ROOT -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $env:SM_RUN_DIR ($name + '.out.log')) -RedirectStandardError (Join-Path $env:SM_RUN_DIR ($name + '.err.log')); Set-Content -LiteralPath (Join-Path $env:SM_RUN_DIR ($name + '.pid')) -Value $process.Id; Write-Host ($name + ' PID ' + $process.Id)"
exit /b %errorlevel%

:installFrontend
echo Installing frontend dependencies...
pushd "%SM_ROOT%frontend" || exit /b 1
call npm ci
set "SM_NPM_RESULT=%errorlevel%"
popd
exit /b %SM_NPM_RESULT%

:failed
echo Startup failed. Check logs in "%SM_RUN_DIR%".
call "%SM_ROOT%stop.cmd"
popd
exit /b 1

