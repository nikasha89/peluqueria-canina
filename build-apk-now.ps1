$ErrorActionPreference = "Stop"
$JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.17.10-hotspot"
$PROJECT_PATH = "C:\repos\MisJuguetes\peluqueria-canina"

$env:PATH = "$JAVA_HOME\bin;$env:PATH"
$env:JAVA_HOME = $JAVA_HOME
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:ANDROID_SDK_ROOT = "$env:LOCALAPPDATA\Android\Sdk"

Write-Host "=== Configuración ===" -ForegroundColor Cyan
Write-Host "JAVA_HOME: $JAVA_HOME"
Write-Host "PROJECT_PATH: $PROJECT_PATH"
Write-Host ""

Push-Location $PROJECT_PATH

Write-Host "=== Verificando Java ===" -ForegroundColor Yellow
& "$JAVA_HOME\bin\java.exe" -version

Write-Host ""
Write-Host "=== Sincronizando Capacitor ===" -ForegroundColor Yellow
npm run cap:sync

Write-Host ""
Write-Host "=== Compilando APK ===" -ForegroundColor Green
Set-Location android
$env:PATH = "$JAVA_HOME\bin;$env:PATH"
& bash ./gradlew assembleDebug

Write-Host ""
Write-Host "=== APK generado ===" -ForegroundColor Green
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    Write-Host "APK ubicado en: $PROJECT_PATH\android\$apkPath" -ForegroundColor Cyan
    $apkSize = (Get-Item $apkPath).Length / 1MB
    Write-Host "Tamaño: $([math]::Round($apkSize, 2)) MB" -ForegroundColor Cyan
} else {
    Write-Host "APK no encontrado en la ruta esperada" -ForegroundColor Red
}

Pop-Location
