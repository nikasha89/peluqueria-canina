$ErrorActionPreference = "Stop"

# Configuración de rutas
$NODE_PATH = "C:\nodejs\node-v20.11.0-win-x64"
$JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.17.10-hotspot"
$ANDROID_HOME = "C:\Users\RodriguezAn\AppData\Local\Android\Sdk"
$PROJECT_PATH = "C:\repos\MisJuguetes\peluqueria-canina"

# Configurar variables de entorno
$env:PATH = "$NODE_PATH;$JAVA_HOME\bin;$env:PATH"
$env:JAVA_HOME = $JAVA_HOME
$env:ANDROID_HOME = $ANDROID_HOME

Write-Host "Preparando archivos para APK..." -ForegroundColor Cyan
Push-Location $PROJECT_PATH

# Copiar archivos a www/
Write-Host "Copiando archivos a www/..." -ForegroundColor Yellow
Copy-Item "index.html" -Destination "www/index.html" -Force
Copy-Item "oauth-manager-v2.js" -Destination "www/oauth-manager-v2.js" -Force
Copy-Item "oauth-integration.js" -Destination "www/oauth-integration.js" -Force
Copy-Item "app.js" -Destination "www/app.js" -Force
Copy-Item "styles.css" -Destination "www/styles.css" -Force
Copy-Item "config.js" -Destination "www/config.js" -Force
Write-Host "Archivos copiados correctamente" -ForegroundColor Green

# Sincronizar con Capacitor
Write-Host ""
Write-Host "Sincronizando con Capacitor..." -ForegroundColor Cyan
npx capacitor sync android

# Compilar APK
Write-Host ""
Write-Host "Compilando APK con Gradle..." -ForegroundColor Cyan
Push-Location "android"
.\gradlew assembleDebug
Pop-Location

# Copiar APK a la raiz
Write-Host ""
Write-Host "Copiando APK a la raiz del proyecto..." -ForegroundColor Cyan
$apkSource = Join-Path $PROJECT_PATH "android/app/build/outputs/apk/debug/app-debug.apk"
$apkDest = Join-Path $PROJECT_PATH "peluqueria-canina.apk"
Copy-Item $apkSource -Destination $apkDest -Force

Write-Host ""
Write-Host "APK generado exitosamente!" -ForegroundColor Green
Write-Host "Ubicacion: peluqueria-canina.apk" -ForegroundColor Cyan

Pop-Location
