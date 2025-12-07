# Configuración de Google OAuth

## Paso 1: Crear archivo de configuración

1. Copia el archivo de ejemplo:
   ```bash
   cp config.sample.js config.js
   ```

2. Edita `config.js` y añade tus credenciales de Google Cloud Console

## Paso 2: Obtener credenciales de Google

1. Ve a [Google Cloud Console](https://console.cloud.google.com)

2. **Crear proyecto** (si no tienes uno):
   - Click en el selector de proyecto (arriba)
   - "Nuevo proyecto"
   - Nombre: "Peluqueria Canina"

3. **Habilitar APIs**:
   - Ve a "APIs & Services" > "Library"
   - Busca y habilita:
     - Google Calendar API
     - Google Drive API
     - People API

4. **Crear credenciales OAuth 2.0**:
   - Ve a "APIs & Services" > "Credentials"
   - Click "Create Credentials" > "OAuth client ID"
   - Tipo: "Web application"
   - Nombre: "Peluqueria Canina Web"
   - JavaScript origins:
     - `http://localhost:8100`
     - `http://127.0.0.1:8100`
   - Redirect URIs:
     - `http://localhost:8100`
     - `http://127.0.0.1:8100`
   - Guarda el **Client ID**

5. **Crear API Key**:
   - Click "Create Credentials" > "API key"
   - Guarda la **API Key**

6. **Configurar OAuth consent screen**:
   - Ve a "OAuth consent screen"
   - User Type: "External"
   - App name: "Peluqueria Canina"
   - User support email: tu email
   - Developer contact: tu email
   - En "Scopes", añade:
     - `.../auth/calendar`
     - `.../auth/calendar.events`
     - `.../auth/drive.file`
     - `.../auth/userinfo.email`
     - `.../auth/userinfo.profile`
   - En "Test users", añade tu email

## Paso 3: Configurar credenciales

Edita `config.js` y reemplaza:

```javascript
clientId: 'TU_CLIENT_ID_AQUI',  // ← Pega tu Client ID
apiKey: 'TU_API_KEY_AQUI',      // ← Pega tu API Key
```

## ⚠️ Seguridad

- **NUNCA** subas `config.js` a Git (ya está en `.gitignore`)
- Solo sube `config.sample.js` como plantilla
- Las credenciales son sensibles y personales

## 🚀 Ejecutar la app

```bash
# Instalar servidor HTTP si no lo tienes
npm install -g http-server

# Ejecutar
http-server -p 8100
```

Abre: http://localhost:8100
