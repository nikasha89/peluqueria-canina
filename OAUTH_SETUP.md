# 🔐 Configuración OAuth para Peluquería Canina

Esta guía te ayudará a configurar autenticación segura con Google OAuth usando un servidor backend.

## ¿Por qué usar OAuth Backend?

✅ **Más seguro**: Las credenciales de Google nunca se exponen al cliente  
✅ **Mejor control**: Gestión de sesiones en el servidor  
✅ **Tokens seguros**: Manejo de refresh tokens en el backend  
✅ **Cumplimiento**: Sigue las recomendaciones de seguridad de Google  

## Paso 1: Crear credenciales en Google Cloud Console

1. **Ir a:** https://console.cloud.google.com/
2. **Crear proyecto** (si no lo tienes):
   - Click en "Seleccionar un proyecto" > "Nuevo proyecto"
   - Nombre: "Peluquería Canina"
   - Crear

3. **Habilitar APIs:**
   - Ve a "APIs y servicios" > "Biblioteca"
   - Busca y habilita:
     - Google Calendar API
     - Google Drive API

4. **Crear credenciales OAuth:**
   - Ve a "APIs y servicios" > "Credenciales"
   - Click en "Crear credenciales" > "ID de cliente OAuth"
   - Selecciona "Aplicación web"
   - En "URIs de redirección autorizados" agrega:
     ```
     http://localhost:3001/auth/google/callback
     ```
   - Si vas a producción, agregar también:
     ```
     https://tudominio.com/auth/google/callback
     ```
   - Crear
   - Copiar: **Client ID** y **Client Secret**

## Paso 2: Configurar servidor backend

### Instalación:

```bash
# En la carpeta del proyecto
npm install express cors dotenv axios

# O si tienes package.json:
npm install
```

### Crear archivo `.env`:

```env
GOOGLE_CLIENT_ID=tu_client_id_aqui.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=tu_client_secret_aqui
GOOGLE_REDIRECT_URI=http://localhost:3001/auth/google/callback
BACKEND_PORT=3001
```

### Iniciar servidor:

```bash
node oauth-server.js
```

Deberías ver:
```
🔐 OAuth Backend Server running on http://localhost:3001
```

## Paso 3: Integrar OAuth en tu app

### En `index.html`, añade antes de `</head>`:

```html
<!-- OAuth Manager -->
<script src="oauth-manager.js"></script>
```

### En `app.js`, en la clase `PeluqueriaCanina`:

```javascript
constructor() {
    // ... código existente ...
    
    // Inicializar OAuth
    this.oauth = new OAuthManager('http://localhost:3001');
}

async inicializar() {
    // Procesar callback OAuth si existe
    const autenticado = await this.oauth.procesarCallbackOAuth();
    
    if (autenticado) {
        console.log('Usuario autenticado:', this.oauth.obtenerUsuario());
        this.mostrarPanelAutenticado();
    } else if (this.oauth.estaAutenticado()) {
        // Restaurar sesión guardada
        await this.oauth.obtenerPerfil();
        this.mostrarPanelAutenticado();
    } else {
        this.mostrarPanelLogin();
    }
    
    // ... resto del código ...
}

async mostrarPanelLogin() {
    // Mostrar botón de login
    const loginBtn = document.getElementById('loginBtn');
    loginBtn.addEventListener('click', () => this.oauth.iniciarLoginGoogle());
}

async mostrarPanelAutenticado() {
    const usuario = this.oauth.obtenerUsuario();
    console.log(`Bienvenido, ${usuario.name}!`);
    
    // Obtener token para usar APIs
    const token = await this.oauth.obtenerAccessToken();
    
    // Usar token para Google Calendar API, etc.
    // ...
}

async logout() {
    await this.oauth.logout();
    this.mostrarPanelLogin();
}
```

### En `index.html`, añade botón de login:

```html
<div id="loginPanel" class="login-panel">
    <button id="loginBtn" class="btn btn-primary">Iniciar sesión con Google</button>
</div>

<div id="authPanel" class="auth-panel" style="display:none;">
    <div id="userInfo"></div>
    <button id="logoutBtn" class="btn btn-danger">Cerrar sesión</button>
</div>
```

## Paso 4: Usar tokens para APIs de Google

### Ejemplo: Sincronizar con Google Calendar

```javascript
async sincronizarCalendario() {
    try {
        const token = await this.oauth.obtenerAccessToken();
        
        const response = await fetch(
            'https://www.googleapis.com/calendar/v3/calendars/primary/events',
            {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            }
        );
        
        const eventos = await response.json();
        console.log('Eventos del calendario:', eventos);
        
    } catch (error) {
        // Si error 401, refrescar token
        if (error.status === 401) {
            await this.oauth.refrescarToken();
            // Reintentar
        }
    }
}
```

## Despliegue a Producción

### Opción 1: Firebase Hosting + Cloud Functions

```bash
npm install -g firebase-tools
firebase init hosting
firebase init functions
# Copiar oauth-server.js a functions/
firebase deploy
```

### Opción 2: Heroku

```bash
npm install -g heroku
heroku create peluqueria-canina-oauth
git push heroku main
heroku config:set GOOGLE_CLIENT_ID=...
heroku config:set GOOGLE_CLIENT_SECRET=...
```

### Opción 3: Tu propio servidor

```bash
# En producción, usar PM2 o similar
npm install -g pm2
pm2 start oauth-server.js --name "oauth-server"
pm2 save
```

## Configurar Capacitor para producción

En `capacitor.config.json`:

```json
{
  "appId": "com.peluqueriacanina.app",
  "appName": "Peluquería Canina",
  "webDir": "www",
  "server": {
    "url": "https://tudominio.com"
  }
}
```

## Seguridad en Producción

- ✅ Usar HTTPS en todas las URLs
- ✅ Usar variables de entorno seguras
- ✅ Implementar rate limiting
- ✅ Validar CSRF tokens
- ✅ Usar secure cookies (HttpOnly, Secure, SameSite)
- ✅ Limpiar sesiones expiradas
- ✅ Logging y monitoreo

## Troubleshooting

### Error: "Invalid redirect URI"
- Verifica que la URL en `.env` coincida exactamente con la en Google Cloud Console

### Error: "Client ID not found"
- Asegúrate de que `.env` existe y tiene las variables correctas
- Reinicia el servidor: `node oauth-server.js`

### Error: "Session expired"
- El token expiró, el usuario debe volver a autenticarse
- Implementar refresh automático antes de que expire

### Error: "CORS error"
- Asegúrate que el backend permite CORS para tu dominio
- En producción, cambiar `origin` en oauth-server.js a tu dominio real

## Recursos

- [Google OAuth Documentation](https://developers.google.com/identity/protocols/oauth2)
- [Google Calendar API](https://developers.google.com/calendar/api)
- [Google Drive API](https://developers.google.com/drive/api)
- [Express.js Documentation](https://expressjs.com/)

---

¡Tu app está lista con autenticación segura! 🔐📱
