#!/usr/bin/env powershell

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Green
Write-Host "Configuración de APK - Peluquería Canina" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# Configurar rutas
$NODE_PATH = "C:\nodejs\node-v20.11.0-win-x64"
$JAVA_HOME = "C:\jdk\jdk-17.0.10+7"
$PROJECT_PATH = "c:\Users\nikas\Downloads\Perruquería canina"

# Actualizar PATH
$env:PATH = "$NODE_PATH;$JAVA_HOME\bin;$env:PATH"
$env:JAVA_HOME = $JAVA_HOME

# Paso 1: Verificar Node.js
Write-Host "Paso 1: Verificando Node.js..."
$nodeVersion = & node --version 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Node.js $nodeVersion encontrado" -ForegroundColor Green
} else {
    Write-Host "✗ Node.js no encontrado" -ForegroundColor Red
    exit 1
}

# Paso 2: Verificar npm
Write-Host ""
Write-Host "Paso 2: Verificando npm..."
$npmVersion = & npm --version 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ npm $npmVersion encontrado" -ForegroundColor Green
} else {
    Write-Host "✗ npm no encontrado" -ForegroundColor Red
    exit 1
}

# Paso 3: Instalar Java si es necesario
Write-Host ""
Write-Host "Paso 3: Verificando Java..."
$javaCheck = & java -version 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "Descargando e instalando OpenJDK..." -ForegroundColor Yellow
    
    if (-not (Test-Path "C:\Temp")) {
        New-Item -ItemType Directory -Path "C:\Temp" -Force | Out-Null
    }
    
    $ProgressPreference = 'SilentlyContinue'
    Invoke-WebRequest -Uri 'https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.10%2B7/OpenJDK17U-jdk_x64_windows_hotspot_17.0.10_7.zip' -OutFile 'C:\Temp\jdk.zip'
    
    if (-not (Test-Path "C:\jdk")) {
        New-Item -ItemType Directory -Path "C:\jdk" -Force | Out-Null
    }
    
    Expand-Archive -Path 'C:\Temp\jdk.zip' -DestinationPath 'C:\jdk' -Force
    Write-Host "✓ Java instalado" -ForegroundColor Green
} else {
    Write-Host "✓ Java encontrado" -ForegroundColor Green
}

# Actualizar JAVA_HOME después de instalar
$env:JAVA_HOME = "C:\jdk\jdk-17.0.10+7"
$env:PATH = "C:\jdk\jdk-17.0.10+7\bin;" + $env:PATH

# Paso 4: Verificar Capacitor
Write-Host ""
Write-Host "Paso 4: Verificando Capacitor CLI..."
$capVersion = & npx capacitor --version 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Capacitor $capVersion encontrado" -ForegroundColor Green
} else {
    Write-Host "✗ Error al verificar Capacitor" -ForegroundColor Red
}

# Paso 5: Navegar al proyecto
Write-Host ""
Write-Host "Paso 5: Navegando al proyecto..."
Push-Location $PROJECT_PATH
Write-Host "✓ En: $(Get-Location)" -ForegroundColor Green

# Paso 6: Sincronizar Android
Write-Host ""
Write-Host "Paso 6: Sincronizando con Android..." -ForegroundColor Yellow
& npx capacitor sync android
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error al sincronizar" -ForegroundColor Red
    Pop-Location
    exit 1
}

# Paso 7: Compilar APK
Write-Host ""
Write-Host "Paso 7: Compilando APK..." -ForegroundColor Yellow
& npx capacitor build android
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error al compilar APK" -ForegroundColor Red
    Pop-Location
    exit 1
}

Pop-Location

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "¡APK compilado exitosamente!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "El APK se encuentra en:" -ForegroundColor Cyan
Write-Host "$PROJECT_PATH\android\app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Yellow
Write-Host ""
