# 🎯 Sistema OAuth Automático - Peluquería Canina

## ¿Qué hemos hecho?

Tu usuario ahora solo necesita **meter sus credenciales de Google UNA VEZ** y todo funciona automáticamente:

```
Usuario final:
1️⃣ Ingresa Client ID y Secret de Google
2️⃣ Hace clic en "Iniciar Sesión"
3️⃣ ✅ LISTO - Todo funciona automáticamente
```

## 🔄 Proceso Automático Detrás

### 1. **Autenticación**
- Usuario se autentica con Google
- Backend obtiene tokens seguros
- Frontend recibe un `sessionId` (no los tokens directamente)

### 2. **Sincronización Automática** (se ejecuta después del login)
- 📅 Descarga eventos del calendario (próximos 30 días)
- 📂 Descarga archivos de Google Drive
- 💾 Se guarda en localStorage para acceso offline
- ⏰ Se sincroniza automáticamente cada 5 minutos

### 3. **Integración con la App**
- Los eventos del calendario se importan a la agenda
- Los backups de Drive se pueden descargar
- Las nuevas citas se exportan automáticamente a Calendar

### 4. **Backups Automáticos**
- Cada vez que se crea/modifica una cita, se sube a Drive
- Se crea un archivo JSON con la fecha del día
- Se guarda automáticamente

## 📦 Archivos Creados/Modificados

### Nuevos Archivos
1. **`oauth-server.js`** - Backend Node.js/Express
   - 11 endpoints para OAuth, Calendar y Drive
   - Manejo seguro de credenciales
   - Sincronización automática

2. **`oauth-manager-v2.js`** - Cliente OAuth avanzado
   - Gestión de sesiones
   - Sincronización automática
   - Caché de datos
   - Eventos personalizados

3. **`oauth-integration.js`** - Integración con la app
   - Conecta OAuth con PeluqueriaCanina
   - Procesa eventos de Calendar
   - Maneja backups
   - Actualiza interfaz

4. **`credentials-setup.html`** - Página de configuración
   - UI simplificada para ingresar credenciales
   - Verifica conexión del backend
   - Muestra estado de sincronización

5. **`SETUP_RAPIDO.md`** - Guía rápida (3 pasos)
   - Instrucciones paso a paso
   - Ejemplos de código
   - Troubleshooting

### Archivos Modificados
1. **`index.html`** - Agregados scripts OAuth
2. **`oauth-server.js`** - Mejorado con nuevos endpoints

## 🎨 UI/UX Mejorada

### Indicador de Estado en Header
```
✓ Juan García (12 eventos) [EN LA APP PRINCIPAL]
```

### Página de Configuración (`credentials-setup.html`)
```
┌─────────────────────────────┐
│ Peluquería Canina OAuth     │
├─────────────────────────────┤
│ 0️⃣ Estado de Conexión      │
│                             │
│ 1️⃣ URL del Backend          │
│    [http://localhost:3001]  │
│                             │
│ 2️⃣ Credenciales de Google  │
│    [Client ID]              │
│    [Client Secret]          │
│                             │
│  [🧪 Probar] [🚀 Login]    │
│                             │
│ 📊 Estado de Sincronización│
│    📅 12 eventos            │
│    📂 5 archivos            │
│    ⏰ Sincronizado hace 2m  │
└─────────────────────────────┘
```

## ⚙️ Endpoints del Backend

| Endpoint | Método | Función |
|----------|--------|---------|
| `/auth/google/login` | GET | Obtener URL de login |
| `/auth/google/callback` | GET | Procesar respuesta de Google |
| `/auth/google/token` | POST | Obtener token de acceso |
| `/auth/google/refresh` | POST | Refrescar token expirado |
| `/auth/google/logout` | POST | Cerrar sesión |
| `/auth/profile` | GET | Obtener perfil del usuario |
| `/calendar/events` | GET | Obtener eventos del calendario |
| `/calendar/events` | POST | Crear evento en calendario |
| `/drive/files` | GET | Listar archivos de Drive |
| `/drive/upload` | POST | Subir archivo a Drive |
| `/sync/auto` | POST | Sincronizar todo automáticamente |

## 🔐 Flujo Seguro

```
Navegador                    Backend OAuth           Google
  │                               │                    │
  ├──── Pedir login ────────────>│                     │
  │                               ├─── Redirigir a Google login ──>│
  │                               │                                 │
  │ <─────────────────────────────────────── redirect con code ────│
  │                               │                                 │
  │ ───── callback con code ────>│                                 │
  │                               ├─── cambiar code por tokens ──>│
  │                               │<─── tokens de acceso ──────────│
  │                               │                                 │
  │ <────── sessionId ────────────│                                 │
  │ (sin tokens nunca al navegador)                                │
  │                               │
```

## 💡 Ejemplos de Uso

### Desde el HTML
```html
<!-- Botón de Login -->
<button onclick="oauthIntegration.oauth.iniciarLoginGoogle()">
  Conectar con Google
</button>

<!-- Mostrar usuario autenticado -->
<script>
  setInterval(() => {
    const estado = oauthIntegration.obtenerEstado();
    document.querySelector('#usuario').textContent = 
      estado.usuario?.name || 'No autenticado';
  }, 1000);
</script>
```

### Desde JavaScript
```javascript
// Crear cita en Google Calendar
await oauthIntegration.agregarCitaAlCalendar({
  cliente: "María López",
  perro: "Bella",
  fecha: "2025-01-15",
  hora: "10:00",
  servicio: "Baño y Corte"
});

// Hacer backup manual
await oauthIntegration.hacerBackup();

// Obtener archivos de backup
const backups = await oauthIntegration.obtenerBackupsDisponibles();
```

## 🚀 Ventajas

✅ **Usuario**: No ve complejidad de OAuth, todo es automático
✅ **Seguridad**: Credenciales en backend, nunca en navegador
✅ **Eficiencia**: Sincronización automática cada 5 minutos
✅ **Backup**: Se guarda todo en Google Drive automáticamente
✅ **Integración**: Los datos fluyen automáticamente entre sistemas
✅ **Offline**: Datos en caché disponibles aunque falle internet
✅ **Eventos**: Notificaciones de qué ocurre (sync, backup, etc)

## 📋 Checklist de Implementación

```
Para que funcione correctamente:

✅ Archivo .env configurado
✅ npm install ejecutado
✅ Backend OAuth corriendo (npm run oauth:dev)
✅ Google Calendar API habilitada
✅ Google Drive API habilitada
✅ Redirect URI autorizado en Google
✅ index.html con scripts OAuth
✅ credentials-setup.html accesible

Verificar:
- http://localhost:3001/health responde
- credentials-setup.html muestra "Conectado"
- Login redirige a Google correctamente
- Eventos aparecen en la agenda
- Archivos se ven en Drive
```

## 🎓 Cómo Funciona por Dentro

1. **Primer Login**
   - `credentials-setup.html` → usuario ingresa credenciales
   - Backend autentica con Google
   - Se crea sesión y `sessionId` se guarda
   - Se ejecuta sincronización automática
   - Eventos y archivos se guardan en localStorage

2. **Uso Diario**
   - Usuario abre `index.html`
   - `oauth-integration.js` detecta `sessionId` guardado
   - Se restauran datos del localStorage
   - Cada 5 minutos se sincroniza automáticamente
   - Cuando se crea una cita → se agrega a Google Calendar

3. **Cierre de Sesión**
   - Se borra `sessionId`
   - Se borra caché de eventos y archivos
   - Frontend lo notifica al backend
   - Backend invalida sesión

## 📞 Soporte Rápido

| Problema | Solución |
|----------|----------|
| "Cannot connect to backend" | Ejecutar: `npm run oauth:dev` |
| "Invalid Client ID" | Verificar archivo `.env` |
| "Redirect URI mismatch" | Actualizar en Google Cloud Console |
| Eventos no sincronizan | Forzar: `oauthIntegration.oauth.sincronizarAutomatico()` |
| ¿Dónde están mis datos? | localStorage → DevTools → Application |

## 🎉 ¡Listo para usar!

Tu app ahora tiene:
- ✅ Autenticación segura con Google
- ✅ Sincronización de Calendar y Drive
- ✅ Backups automáticos
- ✅ Interfaz amigable
- ✅ Manejo de errores
- ✅ Eventos personalizados
- ✅ Datos en caché

**Instrucciones finales**:
1. Crea archivo `.env` con tus credenciales
2. Ejecuta `npm run oauth:dev`
3. Abre `www/credentials-setup.html`
4. ¡Disfruta! 🚀
