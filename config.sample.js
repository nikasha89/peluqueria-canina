/**
 * Configuración de ejemplo para Google OAuth
 * 
 * INSTRUCCIONES:
 * 1. Copia este archivo y renómbralo a: config.js
 * 2. Rellena tus credenciales reales de Google Cloud Console
 * 3. NO subas config.js a Git (ya está en .gitignore)
 */

const CONFIG = {
    google: {
        // Obtén estas credenciales en: https://console.cloud.google.com
        clientId: 'TU_CLIENT_ID_AQUI.apps.googleusercontent.com',
        apiKey: 'TU_API_KEY_AQUI',
        
        // Scopes (permisos) que necesita la aplicación
        scopes: [
            'https://www.googleapis.com/auth/calendar',
            'https://www.googleapis.com/auth/calendar.events',
            'https://www.googleapis.com/auth/drive.file',
            'https://www.googleapis.com/auth/userinfo.email',
            'https://www.googleapis.com/auth/userinfo.profile'
        ].join(' '),
        
        // APIs de descubrimiento
        discoveryDocs: [
            'https://www.googleapis.com/discovery/v1/apis/calendar/v3/rest',
            'https://www.googleapis.com/discovery/v1/apis/drive/v3/rest'
        ]
    }
};

// Hacer disponible globalmente
window.APP_CONFIG = CONFIG;
