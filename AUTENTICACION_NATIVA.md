# Autenticación Nativa de Google en Android

## 📱 Implementación Completada

La aplicación ahora soporta autenticación nativa de Google cuando se ejecuta como APK en Android, usando el plugin `@codetrix-studio/capacitor-google-auth`.

## 🔧 Configuración

### Plugin Instalado
```bash
npm install @codetrix-studio/capacitor-google-auth
```

### Configuración en `capacitor.config.json`
```json
{
  "plugins": {
    "GoogleAuth": {
      "scopes": [
        "profile",
        "email",
        "https://www.googleapis.com/auth/calendar",
        "https://www.googleapis.com/auth/calendar.events",
        "https://www.googleapis.com/auth/drive.file"
      ],
      "serverClientId": "336593129164-givp069psmaqa62a59q554vp9crllmhs.apps.googleusercontent.com",
      "forceCodeForRefreshToken": true
    }
  }
}
```

### Variables de Android (`android/variables.gradle`)
```gradle
ext {
    ...
    googleAuthApplicationId = 'com.peluqueriacanina.app'
    googleAuthServerClientId = '336593129164-givp069psmaqa62a59q554vp9crllmhs.apps.googleusercontent.com'
}
```

## 🚀 Funcionalidades

### Detección Automática de Plataforma
La aplicación detecta automáticamente si se ejecuta en:
- **Web/Navegador**: Usa OAuth tradicional con Google API
- **Android (APK)**: Usa autenticación nativa con Capacitor

```javascript
detectarPlataforma() {
    return window.Capacitor !== undefined && window.Capacitor.isNativePlatform();
}
```

### Login Nativo
Cuando la app está en Android:
1. Se inicializa automáticamente el plugin de Google Auth
2. El botón "Conectar con Google" usa `loginNativo()`
3. Se muestra la pantalla nativa de selección de cuenta de Google
4. Se obtiene el token de acceso sin necesidad de redireccionamientos web

### Características Soportadas

✅ **Login con Google**
- Autenticación nativa sin navegador
- Selección de cuenta de Google del dispositivo
- Obtención automática de tokens

✅ **Integración con Google Calendar**
- Crear eventos desde citas
- Sincronizar citas con calendario
- Acceso directo a la API de Calendar

✅ **Integración con Google Drive**
- Backup automático de datos
- Restauración de datos
- Sincronización en la nube

✅ **Persistencia de Sesión**
- La sesión se mantiene entre ejecuciones de la app
- Refresh automático de tokens
- Logout completo al cerrar sesión

## 🔐 Flujo de Autenticación

### En Android (APK):
```
Usuario presiona "Conectar con Google"
    ↓
Se detecta plataforma nativa
    ↓
oauthIntegration.loginNativo()
    ↓
GoogleAuth.signIn() - Pantalla nativa de Google
    ↓
Usuario selecciona cuenta
    ↓
Se obtiene accessToken
    ↓
Se emite evento 'oauthLoginCompleto'
    ↓
Se actualiza UI con datos del usuario
    ↓
Se carga backup automático desde Google Drive
```

### En Web (Navegador):
```
Usuario presiona "Conectar con Google"
    ↓
Se detecta plataforma web
    ↓
oauthManager.iniciarLoginGoogle()
    ↓
Flujo OAuth tradicional
    ↓
[Continúa con el flujo normal existente]
```

## 📝 Código Importante

### Métodos Principales en `oauth-integration.js`

```javascript
// Inicializar autenticación nativa
async inicializarAutenticacionNativa()

// Login nativo
async loginNativo()

// Logout nativo
async logoutNativo()

// Verificar autenticación (web o nativo)
estaAutenticadoGeneral()

// Obtener token de acceso (web o nativo)
async obtenerAccessToken()

// Métodos de API que funcionan en ambas plataformas
async crearEventoCalendarGeneral(evento)
async buscarArchivosDriveGeneral(nombreArchivo)
async descargarArchivoDriveGeneral(fileId)
async subirArchivoDriveGeneral(nombreArchivo, contenido)
async actualizarArchivoDriveGeneral(fileId, contenido)
```

## 🎯 Ventajas de la Autenticación Nativa

1. **Experiencia de Usuario Mejorada**
   - No se abre navegador externo
   - Usa la cuenta de Google ya configurada en el dispositivo
   - Más rápido y fluido

2. **Seguridad**
   - Tokens manejados de forma segura por el sistema
   - No hay redirecciones web vulnerables
   - Refresh automático de tokens

3. **Integración Perfecta**
   - Funciona sin internet después del login inicial
   - Los tokens se guardan de forma segura
   - Sincronización automática en segundo plano

## 🔄 Sincronización

La app realiza sincronización automática:
- Al hacer login, busca backup en Google Drive
- Si encuentra backup, lo restaura automáticamente
- Los cambios locales pueden sincronizarse manualmente

## 🛠️ Comandos de Desarrollo

```bash
# Sincronizar cambios con Android
npx cap sync android

# Construir APK
.\build-apk.ps1

# Abrir en Android Studio
npx cap open android
```

## ⚠️ Notas Importantes

1. **Client ID de Android**: Se usa el Client ID específico de Android configurado en Google Cloud Console
2. **Scopes**: Los mismos permisos que en la versión web (Calendar, Drive, Profile)
3. **Compatibilidad**: Funciona en Android 5.0+ (API 22+)
4. **Fallback**: Si falla la autenticación nativa, se puede volver a intentar

## 📚 Referencias

- Plugin: [@codetrix-studio/capacitor-google-auth](https://www.npmjs.com/package/@codetrix-studio/capacitor-google-auth)
- Documentación de Capacitor: [capacitorjs.com](https://capacitorjs.com)
- Google Calendar API: [developers.google.com/calendar](https://developers.google.com/calendar)
- Google Drive API: [developers.google.com/drive](https://developers.google.com/drive)
