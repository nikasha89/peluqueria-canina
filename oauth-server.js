/**
 * Servidor Backend OAuth Seguro para Peluquería Canina
 * Maneja autenticación con Google Calendar y Drive de forma automática
 * 
 * Instalación:
 * npm install express cors dotenv axios
 * 
 * Variables de entorno (.env):
 * GOOGLE_CLIENT_ID=tu_client_id.apps.googleusercontent.com
 * GOOGLE_CLIENT_SECRET=tu_client_secret
 * GOOGLE_REDIRECT_URI=http://localhost:3001/auth/google/callback
 * BACKEND_PORT=3001
 */

const express = require('express');
const cors = require('cors');
const axios = require('axios');
require('dotenv').config();

const app = express();

// Middleware
app.use(cors({
    origin: ['http://localhost:3000', 'http://localhost:8100', 'capacitor://localhost'],
    credentials: true
}));
app.use(express.json());

// Configuración OAuth
const GOOGLE_CLIENT_ID = process.env.GOOGLE_CLIENT_ID;
const GOOGLE_CLIENT_SECRET = process.env.GOOGLE_CLIENT_SECRET;
const GOOGLE_REDIRECT_URI = process.env.GOOGLE_REDIRECT_URI || 'http://localhost:3001/auth/google/callback';
const GOOGLE_AUTH_URL = 'https://accounts.google.com/o/oauth2/v2/auth';
const GOOGLE_TOKEN_URL = 'https://oauth2.googleapis.com/token';
const GOOGLE_USERINFO_URL = 'https://www.googleapis.com/oauth2/v2/userinfo';
const GOOGLE_CALENDAR_URL = 'https://www.googleapis.com/calendar/v3';
const GOOGLE_DRIVE_URL = 'https://www.googleapis.com/drive/v3';

// Store temporal de sesiones (en producción usar Redis o DB)
const sessions = new Map();

/**
 * Endpoint 1: Obtener URL de autenticación
 * El frontend redirige al usuario aquí
 */
app.get('/auth/google/login', (req, res) => {
    const scopes = [
        'https://www.googleapis.com/auth/calendar.readonly',
        'https://www.googleapis.com/auth/calendar.events',
        'https://www.googleapis.com/auth/drive.readonly',
        'https://www.googleapis.com/auth/drive.file',
        'https://www.googleapis.com/auth/userinfo.email',
        'https://www.googleapis.com/auth/userinfo.profile'
    ];

    const authUrl = `${GOOGLE_AUTH_URL}?` +
        `client_id=${GOOGLE_CLIENT_ID}&` +
        `redirect_uri=${GOOGLE_REDIRECT_URI}&` +
        `response_type=code&` +
        `scope=${scopes.join(' ')}&` +
        `access_type=offline`;

    res.json({ authUrl });
});

/**
 * Endpoint 2: Callback de Google (maneja el código de autorización)
 */
app.get('/auth/google/callback', async (req, res) => {
    const { code } = req.query;

    if (!code) {
        return res.status(400).json({ error: 'No authorization code provided' });
    }

    try {
        // Intercambiar código por token
        const tokenResponse = await axios.post(GOOGLE_TOKEN_URL, {
            client_id: GOOGLE_CLIENT_ID,
            client_secret: GOOGLE_CLIENT_SECRET,
            code: code,
            grant_type: 'authorization_code',
            redirect_uri: GOOGLE_REDIRECT_URI
        });

        const { access_token, refresh_token, expires_in } = tokenResponse.data;

        // Obtener información del usuario
        const userResponse = await axios.get(GOOGLE_USERINFO_URL, {
            headers: { Authorization: `Bearer ${access_token}` }
        });

        const user = userResponse.data;
        const sessionId = generateSessionId();

        // Guardar sesión
        sessions.set(sessionId, {
            accessToken: access_token,
            refreshToken: refresh_token,
            expiresAt: Date.now() + (expires_in * 1000),
            user: {
                id: user.id,
                email: user.email,
                name: user.name,
                picture: user.picture
            },
            createdAt: Date.now()
        });

        // Redirigir de vuelta a la app con sesión
        const redirectUrl = `${req.headers.referer || 'http://localhost:8100'}?session=${sessionId}`;
        res.redirect(redirectUrl);

    } catch (error) {
        console.error('OAuth Error:', error);
        res.status(500).json({ error: 'Authentication failed', details: error.message });
    }
});

/**
 * Endpoint 3: Obtener token de acceso (verificando la sesión)
 */
app.post('/auth/google/token', (req, res) => {
    const { sessionId } = req.body;

    if (!sessionId || !sessions.has(sessionId)) {
        return res.status(401).json({ error: 'Invalid session' });
    }

    const session = sessions.get(sessionId);

    // Verificar si el token expiró
    if (Date.now() > session.expiresAt) {
        sessions.delete(sessionId);
        return res.status(401).json({ error: 'Session expired' });
    }

    res.json({
        accessToken: session.accessToken,
        user: session.user
    });
});

/**
 * Endpoint 4: Refrescar token
 */
app.post('/auth/google/refresh', async (req, res) => {
    const { sessionId } = req.body;

    if (!sessionId || !sessions.has(sessionId)) {
        return res.status(401).json({ error: 'Invalid session' });
    }

    const session = sessions.get(sessionId);

    try {
        const tokenResponse = await axios.post(GOOGLE_TOKEN_URL, {
            client_id: GOOGLE_CLIENT_ID,
            client_secret: GOOGLE_CLIENT_SECRET,
            refresh_token: session.refreshToken,
            grant_type: 'refresh_token'
        });

        const { access_token, expires_in } = tokenResponse.data;

        // Actualizar sesión
        session.accessToken = access_token;
        session.expiresAt = Date.now() + (expires_in * 1000);

        res.json({ accessToken: access_token });

    } catch (error) {
        console.error('Refresh Error:', error);
        res.status(500).json({ error: 'Token refresh failed' });
    }
});

/**
 * Endpoint 5: Logout
 */
app.post('/auth/google/logout', (req, res) => {
    const { sessionId } = req.body;

    if (sessionId && sessions.has(sessionId)) {
        sessions.delete(sessionId);
    }

    res.json({ success: true });
});

/**
 * Endpoint 6: Obtener perfil de usuario
 */
app.get('/auth/profile', (req, res) => {
    const sessionId = req.headers['x-session-id'];

    if (!sessionId || !sessions.has(sessionId)) {
        return res.status(401).json({ error: 'Unauthorized' });
    }

    const session = sessions.get(sessionId);
    res.json({ user: session.user });
});

/**
 * Endpoint 7: Obtener eventos del calendario
 * GET /calendar/events?sessionId=...&timeMin=...&timeMax=...&maxResults=50
 */
app.get('/calendar/events', async (req, res) => {
    try {
        const { sessionId, timeMin, timeMax, maxResults = 50 } = req.query;
        const session = sessions.get(sessionId);
        
        if (!session || !session.tokens.access_token) {
            return res.status(401).json({ error: 'Unauthorized' });
        }

        const response = await axios.get(`${GOOGLE_CALENDAR_URL}/calendars/primary/events`, {
            headers: {
                'Authorization': `Bearer ${session.tokens.access_token}`,
                'Content-Type': 'application/json'
            },
            params: {
                timeMin: timeMin || new Date().toISOString(),
                timeMax: timeMax,
                maxResults: maxResults,
                singleEvents: true,
                orderBy: 'startTime'
            }
        });

        res.json(response.data);
    } catch (error) {
        console.error('Calendar error:', error.message);
        res.status(error.response?.status || 500).json({ 
            error: 'Failed to fetch calendar events',
            message: error.message 
        });
    }
});

/**
 * Endpoint 8: Crear evento en calendario
 * POST /calendar/events
 */
app.post('/calendar/events', async (req, res) => {
    try {
        const { sessionId, event } = req.body;
        const session = sessions.get(sessionId);
        
        if (!session || !session.tokens.access_token) {
            return res.status(401).json({ error: 'Unauthorized' });
        }

        const response = await axios.post(
            `${GOOGLE_CALENDAR_URL}/calendars/primary/events`,
            event,
            {
                headers: {
                    'Authorization': `Bearer ${session.tokens.access_token}`,
                    'Content-Type': 'application/json'
                }
            }
        );

        res.json(response.data);
    } catch (error) {
        console.error('Calendar create error:', error.message);
        res.status(error.response?.status || 500).json({ 
            error: 'Failed to create calendar event',
            message: error.message 
        });
    }
});

/**
 * Endpoint 9: Obtener archivos de Google Drive
 * GET /drive/files?sessionId=...&query=...&pageSize=10
 */
app.get('/drive/files', async (req, res) => {
    try {
        const { sessionId, query = "trashed=false", pageSize = 10 } = req.query;
        const session = sessions.get(sessionId);
        
        if (!session || !session.tokens.access_token) {
            return res.status(401).json({ error: 'Unauthorized' });
        }

        const response = await axios.get(`${GOOGLE_DRIVE_URL}/files`, {
            headers: {
                'Authorization': `Bearer ${session.tokens.access_token}`,
                'Content-Type': 'application/json'
            },
            params: {
                q: query,
                spaces: 'drive',
                pageSize: pageSize,
                fields: 'files(id, name, mimeType, webViewLink, createdTime, modifiedTime, size)'
            }
        });

        res.json(response.data);
    } catch (error) {
        console.error('Drive error:', error.message);
        res.status(error.response?.status || 500).json({ 
            error: 'Failed to fetch drive files',
            message: error.message 
        });
    }
});

/**
 * Endpoint 10: Subir archivo a Google Drive
 * POST /drive/upload - multipart/form-data
 */
app.post('/drive/upload', async (req, res) => {
    try {
        const { sessionId, fileName, mimeType = 'application/json' } = req.body;
        const fileContent = req.body.content;
        
        const session = sessions.get(sessionId);
        
        if (!session || !session.tokens.access_token) {
            return res.status(401).json({ error: 'Unauthorized' });
        }

        // Crear metadata del archivo
        const metadata = {
            name: fileName,
            mimeType: mimeType
        };

        const response = await axios.post(
            `${GOOGLE_DRIVE_URL}/files?uploadType=multipart`,
            {
                metadata: metadata,
                content: fileContent
            },
            {
                headers: {
                    'Authorization': `Bearer ${session.tokens.access_token}`,
                    'Content-Type': 'application/json'
                }
            }
        );

        res.json(response.data);
    } catch (error) {
        console.error('Drive upload error:', error.message);
        res.status(error.response?.status || 500).json({ 
            error: 'Failed to upload file to drive',
            message: error.message 
        });
    }
});

/**
 * Endpoint 11: Auto-sincronizar datos (Calendar + Drive)
 * POST /sync/auto - Sincroniza automáticamente al login
 */
app.post('/sync/auto', async (req, res) => {
    try {
        const { sessionId } = req.body;
        const session = sessions.get(sessionId);
        
        if (!session || !session.tokens.access_token) {
            return res.status(401).json({ error: 'Unauthorized' });
        }

        // 1. Obtener eventos del calendario de los próximos 30 días
        const now = new Date();
        const thirtyDaysLater = new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000);

        const calendarResponse = await axios.get(
            `${GOOGLE_CALENDAR_URL}/calendars/primary/events`,
            {
                headers: {
                    'Authorization': `Bearer ${session.tokens.access_token}`,
                    'Content-Type': 'application/json'
                },
                params: {
                    timeMin: now.toISOString(),
                    timeMax: thirtyDaysLater.toISOString(),
                    maxResults: 100,
                    singleEvents: true,
                    orderBy: 'startTime'
                }
            }
        );

        // 2. Obtener archivos de Google Drive (últimos 10)
        const driveResponse = await axios.get(
            `${GOOGLE_DRIVE_URL}/files`,
            {
                headers: {
                    'Authorization': `Bearer ${session.tokens.access_token}`,
                    'Content-Type': 'application/json'
                },
                params: {
                    q: "trashed=false and (mimeType contains 'spreadsheet' or name contains 'backup')",
                    spaces: 'drive',
                    pageSize: 10,
                    orderBy: 'modifiedTime desc',
                    fields: 'files(id, name, mimeType, webViewLink, createdTime, modifiedTime)'
                }
            }
        );

        res.json({
            success: true,
            calendar: {
                events: calendarResponse.data.items || [],
                count: (calendarResponse.data.items || []).length
            },
            drive: {
                files: driveResponse.data.files || [],
                count: (driveResponse.data.files || []).length
            },
            syncTime: new Date().toISOString()
        });
    } catch (error) {
        console.error('Auto sync error:', error.message);
        res.status(error.response?.status || 500).json({ 
            error: 'Failed to auto-sync data',
            message: error.message 
        });
    }
});

// Utilidad para generar IDs de sesión
function generateSessionId() {
    return 'session_' + Math.random().toString(36).substr(2, 9) + '_' + Date.now();
}

// Health check
app.get('/health', (req, res) => {
    res.json({ status: 'OK', timestamp: new Date().toISOString() });
});

// Iniciar servidor
const PORT = process.env.BACKEND_PORT || 3001;
app.listen(PORT, () => {
    console.log(`🔐 OAuth Backend Server running on http://localhost:${PORT}`);
    console.log(`📋 Google OAuth configured for: ${GOOGLE_REDIRECT_URI}`);
});
