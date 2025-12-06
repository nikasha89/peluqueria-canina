$ErrorActionPreference = "Stop"
$NODE_PATH = "C:\nodejs\node-v20.11.0-win-x64"
$JAVA_HOME = "C:\jdk\jdk-17.0.10+7"
$PROJECT_PATH = "c:\Users\nikas\Downloads\Perruquería canina"

$env:PATH = "$NODE_PATH;$JAVA_HOME\bin;$env:PATH"
$env:JAVA_HOME = $JAVA_HOME

Write-Host "Compilando APK..." -ForegroundColor Green
Push-Location $PROJECT_PATH

node --version
npm --version

npx capacitor sync android
npx capacitor build android

Write-Host "APK generado exitosamente!" -ForegroundColor Green
Pop-Location
