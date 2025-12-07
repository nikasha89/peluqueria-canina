/**
 * OAuth Manager v3 - Sistema de autenticación con Google (SIN BACKEND)
 * Maneja la autenticación, tokens y sincronización con Google Calendar y Drive
 * directamente desde el navegador usando la API de Google
 */

class OAuthManager {
    constructor() {
        // Cargar configuración desde config.js (o usar fallback)
        const externalConfig = window.APP_CONFIG?.google;
        
        this.config = this.cargarConfig() || externalConfig || {
            clientId: 'TU_CLIENT_ID_AQUI.apps.googleusercontent.com',
            apiKey: 'TU_API_KEY_AQUI',
            discoveryDocs: [
                'https://www.googleapis.com/discovery/v1/apis/calendar/v3/rest',
                'https://www.googleapis.com/discovery/v1/apis/drive/v3/rest'
            ],
            scopes: [
                'https://www.googleapis.com/auth/calendar.readonly',
                'https://www.googleapis.com/auth/calendar.events',
                'https://www.googleapis.com/auth/drive.file',
                'https://www.googleapis.com/auth/userinfo.email',
                'https://www.googleapis.com/auth/userinfo.profile'
            ].join(' ')
        };
        
        // Verificar que las credenciales están configuradas
        if (this.config.clientId.includes('TU_CLIENT_ID') || this.config.apiKey.includes('TU_API_KEY')) {
            console.warn('⚠️ CREDENCIALES NO CONFIGURADAS: Copia config.sample.js a config.js y añade tus credenciales');
        }
        
        this.tokenClient = null;
        this.gapiInited = false;
        this.gisInited = false;
        this.accessToken = null;
        
        this.datosCache = {
            calendar: { eventos: [], count: 0 },
            drive: { archivos: [], count: 0 },
            usuario: null
        };
        this.sincronizacionActiva = false;
        this.intervaloSincronizacion = null;
        
        // Inicializar Google API
        this.inicializarGoogleAPI();
    }
    
    // ========== INICIALIZACIÓN DE GOOGLE API ==========
    
    async inicializarGoogleAPI() {
        console.log('🚀 Inicializando Google API...');
        
        // Esperar a que los scripts de Google estén disponibles
        await this.esperarScriptsGoogle();
        
        // Detectar si estamos en Capacitor (móvil)
        const esCapacitor = typeof window.Capacitor !== 'undefined';
        const redirectUri = esCapacitor 
            ? window.location.origin  // En móvil usa el origin de Capacitor
            : window.location.origin; // En web usa el origin actual
        
        console.log('🔧 Inicializando Google API - Entorno:', esCapacitor ? 'Móvil (Capacitor)' : 'Web');
        console.log('🔧 Redirect URI:', redirectUri);
        
        // Inicializar GAPI
        console.log('📦 Intentando cargar cliente GAPI...');
        gapi.load('client', async () => {
            console.log('📦 Callback de gapi.load ejecutado - Cargando cliente GAPI...');
            try {
                await gapi.client.init({
                    apiKey: this.config.apiKey,
                    discoveryDocs: this.config.discoveryDocs
                });
                this.gapiInited = true;
                console.log('✅ GAPI Client inicializado - gapiInited =', this.gapiInited);
                this.verificarInicializacion();
            } catch (error) {
                console.error('❌ Error inicializando GAPI Client:', error);
            }
        });
        
        // Inicializar Google Identity Services
        console.log('🔐 Inicializando Google Identity Services...');
        this.tokenClient = google.accounts.oauth2.initTokenClient({
            client_id: this.config.clientId,
            scope: this.config.scopes,
            callback: (response) => {
                if (response.error !== undefined) {
                    console.error('❌ Error en autenticación:', response);
                    this.emitirEvento('oauthError', { error: response.error });
                    alert('❌ Error en autenticación: ' + response.error);
                    return;
                }
                
                console.log('✅ Token recibido');
                this.accessToken = response.access_token;
                gapi.client.setToken({ access_token: response.access_token });
                
                // Guardar token
                this.guardarToken(response.access_token);
                
                // Obtener información del usuario
                this.obtenerInfoUsuario().then(() => {
                    console.log('✅ Usuario autenticado:', this.datosCache.usuario);
                    // Sincronización manual - no automática
                    this.emitirEvento('oauthLoginCompleto', { usuario: this.datosCache.usuario });
                });
            }
        });
        this.gisInited = true;
        console.log('✅ Google Identity Services inicializado');
        this.verificarInicializacion();
    }
    
    async esperarScriptsGoogle() {
        console.log('⏳ Esperando a que los scripts de Google estén disponibles...');
        return new Promise((resolve) => {
            const checkScripts = () => {
                if (typeof gapi !== 'undefined' && typeof google !== 'undefined' && typeof google.accounts !== 'undefined') {
                    console.log('✅ Scripts de Google disponibles');
                    resolve();
                } else {
                    console.log('⏳ Scripts aún no disponibles, reintentando...');
                    setTimeout(checkScripts, 100);
                }
            };
            checkScripts();
        });
    }
    
    
    verificarInicializacion() {
        console.log('🔍 Verificando inicialización - GAPI:', this.gapiInited, 'GIS:', this.gisInited);
        if (this.gapiInited && this.gisInited) {
            console.log('✅ Google API inicializada correctamente');
            
            // Verificar si hay token guardado
            const tokenGuardado = this.cargarToken();
            if (tokenGuardado) {
                console.log('🔑 Token guardado encontrado, verificando...');
                this.accessToken = tokenGuardado;
                gapi.client.setToken({ access_token: tokenGuardado });
                
                // Verificar si el token sigue válido
                this.obtenerInfoUsuario().then(() => {
                    console.log('✅ Sesión restaurada');
                    // Sincronización manual - no automática
                    this.emitirEvento('oauthLoginCompleto', { usuario: this.datosCache.usuario });
                }).catch(() => {
                    // Token expirado, limpiar
                    console.log('⚠️ Token expirado, limpiando sesión');
                    this.limpiarSesion();
                });
            } else {
                console.log('ℹ️ No hay sesión guardada');
            }
        }
    }
    
    // ========== GESTIÓN DE CONFIGURACIÓN ==========
    
    cargarConfig() {
        const config = localStorage.getItem('google_oauth_config');
        return config ? JSON.parse(config) : null;
    }
    
    guardarConfig(config) {
        this.config = { ...this.config, ...config };
        localStorage.setItem('google_oauth_config', JSON.stringify(this.config));
    }
    
    actualizarCredenciales(clientId, apiKey) {
        this.guardarConfig({ clientId, apiKey });
        // Reinicializar con nuevas credenciales
        location.reload();
    }
    
    // ========== GESTIÓN DE TOKENS ==========
    
    cargarToken() {
        return localStorage.getItem('google_access_token') || null;
    }
    
    guardarToken(token) {
        this.accessToken = token;
        localStorage.setItem('google_access_token', token);
    }
    
    limpiarToken() {
        this.accessToken = null;
        localStorage.removeItem('google_access_token');
    }
    
    // ========== GESTIÓN DE SESIÓN ==========
    
    limpiarSesion() {
        this.limpiarToken();
        this.datosCache = {
            calendar: { eventos: [], count: 0 },
            drive: { archivos: [], count: 0 },
            usuario: null
        };
        if (gapi.client.getToken()) {
            gapi.client.setToken(null);
        }
    }
    
    // ========== AUTENTICACIÓN ==========
    
    async iniciarLoginGoogle() {
        console.log('🔐 Intentando iniciar login...', {
            gisInited: this.gisInited,
            gapiInited: this.gapiInited,
            tokenClient: !!this.tokenClient,
            tokenClientType: typeof this.tokenClient
        });
        
        if (!this.gisInited || !this.tokenClient) {
            console.error('❌ Google Identity Services no está listo:', {
                gisInited: this.gisInited,
                tokenClient: this.tokenClient,
                googleAccountsExists: typeof google !== 'undefined' && typeof google.accounts !== 'undefined'
            });
            alert('⏳ Espera un momento, Google está inicializándose...\n\nRevisa la consola (F12) para más detalles.');
            return;
        }
        
        if (!this.gapiInited) {
            console.error('❌ Google API Client no está listo:', {
                gapiInited: this.gapiInited,
                gapiExists: typeof gapi !== 'undefined',
                gapiClientExists: typeof gapi !== 'undefined' && typeof gapi.client !== 'undefined'
            });
            alert('⏳ Espera un momento, Google API está inicializándose...');
            return;
        }
        
        try {
            console.log('✅ Solicitando token de acceso...');
            console.log('📋 Configuración:', {
                clientId: this.config.clientId,
                scopes: this.config.scopes
            });
            // Solicitar token de acceso
            this.tokenClient.requestAccessToken({ prompt: 'consent' });
            console.log('🚀 Ventana de autorización debería aparecer ahora');
        } catch (error) {
            console.error('❌ Error en login Google:', error);
            this.emitirEvento('oauthError', { error: error.message });
            alert('❌ Error al iniciar sesión con Google: ' + error.message);
        }
    }
    
    async obtenerInfoUsuario() {
        try {
            // Usar People API v1 en lugar del endpoint obsoleto de userinfo
            const response = await gapi.client.request({
                path: 'https://people.googleapis.com/v1/people/me',
                params: {
                    personFields: 'names,emailAddresses,photos'
                }
            });
            
            console.log('📋 Respuesta de People API:', response);
            
            const result = response.result;
            this.datosCache.usuario = {
                id: result.resourceName || result.names?.[0]?.metadata?.source?.id || 'unknown',
                email: result.emailAddresses?.[0]?.value || '',
                name: result.names?.[0]?.displayName || 'Usuario',
                picture: result.photos?.[0]?.url || ''
            };
            
            console.log('✅ Usuario obtenido:', this.datosCache.usuario);
            return this.datosCache.usuario;
        } catch (error) {
            console.error('❌ Error al obtener info de usuario:', error);
            throw error;
        }
    }
    
    async logout() {
        try {
            // Revocar token si existe
            if (this.accessToken) {
                google.accounts.oauth2.revoke(this.accessToken, () => {
                    console.log('Token revocado');
                });
            }
        } catch (error) {
            console.error('Error al hacer logout:', error);
        }
        
        // Detener sincronización
        this.detenerSincronizacionAutomatica();
        
        // Limpiar datos locales
        this.limpiarSesion();
        
        // Emitir evento
        this.emitirEvento('oauthLoggedOut', {});
    }
    
    // ========== SINCRONIZACIÓN ==========
    
    async sincronizarDrive() {
        if (!this.accessToken) {
            console.warn('No hay token de acceso para sincronizar');
            return null;
        }
        
        if (this.sincronizacionActiva) {
            console.log('Ya hay una sincronización en progreso');
            return null;
        }
        
        this.sincronizacionActiva = true;
        
        try {
            console.log('🔄 Sincronizando con Google Drive...');
            
            const archivosDrive = await this.obtenerArchivosDrive();
            
            // Actualizar caché de Drive
            this.datosCache.drive = {
                archivos: archivosDrive,
                count: archivosDrive.length
            };
            
            console.log('✅ Drive sincronizado:', {
                archivos: archivosDrive.length
            });
            
            return {
                drive: archivosDrive
            };
            
        } catch (error) {
            console.error('Error en sincronización Drive:', error);
            throw error;
        } finally {
            this.sincronizacionActiva = false;
        }
    }
    
    async obtenerEventosCalendar() {
        try {
            const now = new Date();
            const thirtyDaysLater = new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000);
            
            const response = await gapi.client.calendar.events.list({
                calendarId: 'primary',
                timeMin: now.toISOString(),
                timeMax: thirtyDaysLater.toISOString(),
                showDeleted: false,
                singleEvents: true,
                maxResults: 100,
                orderBy: 'startTime'
            });
            
            return response.result.items || [];
        } catch (error) {
            console.error('Error al obtener eventos del calendario:', error);
            return [];
        }
    }
    
    async obtenerArchivosDrive() {
        try {
            const response = await gapi.client.drive.files.list({
                pageSize: 10,
                fields: 'files(id, name, mimeType, webViewLink, createdTime, modifiedTime)',
                orderBy: 'modifiedTime desc'
            });
            
            return response.result.files || [];
        } catch (error) {
            console.error('Error al obtener archivos de Drive:', error);
            return [];
        }
    }
    
    // Sincronización manual - no automática
    // Para sincronizar, el usuario debe hacer clic en los botones correspondientes
    
    // ========== GOOGLE CALENDAR ==========
    
    async crearEventoCalendar(evento) {
        if (!this.accessToken) {
            throw new Error('No hay sesión activa');
        }
        
        try {
            const response = await gapi.client.calendar.events.insert({
                calendarId: 'primary',
                resource: evento
            });
            
            // Actualizar caché
            this.datosCache.calendar.eventos.push(response.result);
            this.datosCache.calendar.count++;
            
            this.emitirEvento('eventoCreado', { evento: response.result });
            
            console.log('✅ Evento creado en Calendar:', response.result);
            
            return response.result;
            
        } catch (error) {
            console.error('Error al crear evento:', error);
            throw error;
        }
    }
    
    async obtenerEventoCalendar(eventId) {
        if (!this.accessToken) {
            throw new Error('No hay sesión activa');
        }
        
        try {
            const response = await gapi.client.calendar.events.get({
                calendarId: 'primary',
                eventId: eventId
            });
            
            return response.result;
            
        } catch (error) {
            // Si el evento no existe, retornar null
            if (error.status === 404) {
                return null;
            }
            console.error('Error al obtener evento:', error);
            throw error;
        }
    }
    
    // ========== GOOGLE DRIVE ==========
    
    async subirArchivoDrive(nombreArchivo, contenido) {
        if (!this.accessToken) {
            throw new Error('No hay sesión activa');
        }
        
        try {
            const boundary = '-------314159265358979323846';
            const delimiter = "\r\n--" + boundary + "\r\n";
            const close_delim = "\r\n--" + boundary + "--";
            
            const metadata = {
                name: nombreArchivo,
                mimeType: 'application/json'
            };
            
            const multipartRequestBody =
                delimiter +
                'Content-Type: application/json\r\n\r\n' +
                JSON.stringify(metadata) +
                delimiter +
                'Content-Type: application/json\r\n\r\n' +
                (typeof contenido === 'string' ? contenido : JSON.stringify(contenido)) +
                close_delim;
            
            const response = await gapi.client.request({
                path: '/upload/drive/v3/files',
                method: 'POST',
                params: { uploadType: 'multipart' },
                headers: {
                    'Content-Type': 'multipart/related; boundary="' + boundary + '"'
                },
                body: multipartRequestBody
            });
            
            this.emitirEvento('archivoSubido', { archivo: response.result });
            
            console.log('✅ Archivo subido a Drive:', response.result);
            
            return response.result;
            
        } catch (error) {
            console.error('Error al subir archivo:', error);
            throw error;
        }
    }
    
    async buscarArchivosDrive(nombreArchivo) {
        if (!this.accessToken) {
            throw new Error('No hay sesión activa');
        }
        
        try {
            const response = await gapi.client.drive.files.list({
                q: `name='${nombreArchivo}' and trashed=false`,
                fields: 'files(id, name, createdTime, modifiedTime)',
                spaces: 'drive'
            });
            
            return response.result.files;
            
        } catch (error) {
            console.error('Error al buscar archivos:', error);
            throw error;
        }
    }
    
    async actualizarArchivoDrive(fileId, contenido) {
        if (!this.accessToken) {
            throw new Error('No hay sesión activa');
        }
        
        try {
            const response = await gapi.client.request({
                path: `/upload/drive/v3/files/${fileId}`,
                method: 'PATCH',
                params: { uploadType: 'media' },
                headers: {
                    'Content-Type': 'application/json'
                },
                body: typeof contenido === 'string' ? contenido : JSON.stringify(contenido)
            });
            
            console.log('✅ Archivo actualizado en Drive:', response.result);
            
            return response.result;
            
        } catch (error) {
            console.error('Error al actualizar archivo:', error);
            throw error;
        }
    }
    
    async descargarArchivoDrive(fileId) {
        if (!this.accessToken) {
            throw new Error('No hay sesión activa');
        }
        
        try {
            const response = await gapi.client.drive.files.get({
                fileId: fileId,
                alt: 'media'
            });
            
            return response.body;
            
        } catch (error) {
            console.error('Error al descargar archivo:', error);
            throw error;
        }
    }
    
    // ========== UTILIDADES ==========
    
    estaAutenticado() {
        return !!this.accessToken && !!gapi.client.getToken();
    }
    
    obtenerDatosEnCache() {
        return {
            ...this.datosCache,
            autenticado: this.estaAutenticado()
        };
    }
    
    obtenerUsuario() {
        return this.datosCache.usuario;
    }
    
    emitirEvento(nombre, detalle) {
        const evento = new CustomEvent(nombre, { detail: detalle });
        window.dispatchEvent(evento);
        document.dispatchEvent(evento);
    }
}

// Crear instancia global
window.oauthManager = new OAuthManager();
