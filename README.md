# 🐕 Peluquería Canina - Sistema de Gestión

Aplicación web y móvil completa para gestionar tu peluquería canina desde cualquier dispositivo.

## ✨ Características

- **📅 Gestión de Citas**: Programa y organiza todas tus citas con información detallada
- **👥 Base de Clientes**: Mantén un registro de todos tus clientes y sus mascotas
- **📊 Estadísticas**: Visualiza el total de citas e ingresos
- **🔍 Búsqueda**: Encuentra rápidamente clientes por nombre, teléfono o nombre del perro
- **📱 Diseño Responsivo**: Funciona perfectamente en tablets, móviles y ordenadores
- **☁️ Sincronización con Google**: Google Calendar y Google Drive (opcional)
- **📲 App Nativa para Android**: Versión móvil compilada con Capacitor
- **💾 Almacenamiento Local**: Todos los datos se guardan en tu dispositivo

## 🚀 Configuración inicial

### 1. Configurar credenciales de Google (IMPORTANTE)

Los archivos de configuración con credenciales sensibles NO están en el repositorio por seguridad.

**Archivo: `capacitor.config.json`**
```bash
cp capacitor.config.sample.json capacitor.config.json
```

Edita `capacitor.config.json` y reemplaza:
- `YOUR_ANDROID_CLIENT_ID` con tu Client ID de Android
- `YOUR_WEB_CLIENT_ID` con tu Client ID Web

**Archivo: `config.js`**
```bash
cp config.sample.js config.js
```

Edita `config.js` con tus credenciales de Google Cloud Console.

### 2. Obtener credenciales de Google Cloud Console

1. Ve a [Google Cloud Console](https://console.cloud.google.com/apis/credentials)
2. Crea un proyecto nuevo o selecciona uno existente
3. Activa las APIs:
   - Google Calendar API
   - Google Drive API
   - Google People API
4. Crea credenciales OAuth 2.0:
   
   **Para Web:**
   - Tipo: "Web application"
   - Authorized JavaScript origins: `http://localhost:8100`
   - Authorized redirect URIs: `http://localhost:8100`
   
   **Para Android:**
   - Tipo: "Android"
   - Package name: `com.peluqueriacanina.app`
   - SHA-1 certificate fingerprint: 
     ```bash
     keytool -list -v -keystore ~/.android/debug.keystore \
             -alias androiddebugkey -storepass android \
             -keypass android | grep SHA1
     ```

5. Configura OAuth consent screen en modo "Testing" y agrega tu email como test user

## 📱 Desarrollo

### Instalar dependencias

```bash
npm install
```

### Ejecutar en navegador

```bash
npm start
# Abre http://localhost:8100
```

### Compilar APK para Android

```bash
# Sincronizar cambios
npx cap sync android

# Compilar
cd android
./gradlew assembleDebug
# En Windows: .\gradlew assembleDebug

# El APK estará en: android/app/build/outputs/apk/debug/app-debug.apk
```

## 🔒 Seguridad

**IMPORTANTE**: Los siguientes archivos contienen información sensible y NO deben subirse al repositorio:

- ❌ `capacitor.config.json` - Contiene Client IDs
- ❌ `config.js` - Contiene API Keys
- ❌ `android/local.properties` - Rutas locales del SDK
- ❌ `android/app/google-services.json` - Configuración de Firebase (si se usa)

✅ Estos archivos ya están protegidos en `.gitignore`

✅ Los archivos `.sample` SÍ están en el repo como plantillas

## 📋 Funcionalidades principales

### Pestaña Citas
- Registra nuevas citas con todos los detalles:
  - Datos del cliente (nombre y teléfono)
  - Información del perro (nombre y raza)
  - Fecha y hora de la cita
  - Servicio a realizar (Baño, Corte, etc.)
  - Precio del servicio
  - Notas adicionales

### Pestaña Clientes
- Lista completa de clientes
- Buscar clientes por nombre, teléfono o nombre del perro
- Ver historial de visitas de cada cliente
- Ver todos los perros de cada cliente

### Pestaña Agenda
- Ver todas las citas programadas
- Filtrar por: Todas, Hoy, Esta semana
- Marcar citas como completadas
- Eliminar citas
- Ver estadísticas de ingresos totales

## 🎨 Servicios disponibles

- Baño
- Corte
- Baño + Corte
- Deslanado
- Corte de Uñas
- Limpieza Dental

## 💻 Tecnologías utilizadas

- **HTML5**: Estructura de la aplicación
- **CSS3**: Diseño moderno y responsivo
- **JavaScript**: Lógica de la aplicación
- **LocalStorage**: Almacenamiento de datos en el navegador

## 📱 Compatibilidad

La aplicación es compatible con:
- Chrome, Firefox, Safari, Edge (versiones modernas)
- Tablets (iPad, Android)
- Móviles (iOS, Android)
- Ordenadores (Windows, Mac, Linux)

## 🔒 Privacidad

Todos los datos se almacenan localmente en tu dispositivo. No se envía ninguna información a servidores externos.

## 💡 Consejos de uso

1. **Backup de datos**: Los datos están en el navegador. Si borras el caché, se perderán. Considera exportar los datos periódicamente.
2. **Mismo navegador**: Usa siempre el mismo navegador en el mismo dispositivo para acceder a tus datos.
3. **Actualizar precios**: Puedes personalizar los precios según tu negocio.

## 🎯 Ventajas

- ✅ Gratuita y sin publicidad
- ✅ No requiere registro ni cuenta
- ✅ No necesita conexión a internet
- ✅ Interfaz intuitiva y fácil de usar
- ✅ Diseño atractivo y profesional
- ✅ Adaptada para uso táctil (tablets y móviles)

## 📞 Soporte

Esta es una aplicación de código abierto. Puedes modificarla según tus necesidades.

---

**¡Disfruta gestionando tu peluquería canina! 🐾**
