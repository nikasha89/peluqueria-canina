/**
 * OAuth Integration - Integración de OAuth con la aplicación Peluquería Canina
 * Conecta el sistema OAuth (sin backend) con las funcionalidades de la app
 * Soporta autenticación nativa en Android via Capacitor
 */

class OAuthIntegration {
    constructor(oauthManager) {
        this.oauth = oauthManager;
        this.isNativeApp = this.detectarPlataforma();
        this.googleAuth = null;
        this.nativeUser = null;
        
        this.inicializarAutenticacionNativa();
        this.configurarEventosOAuth();
        this.actualizarUI();
    }
    
    // Detectar si estamos en una app nativa (Capacitor)
    detectarPlataforma() {
        return window.Capacitor !== undefined && window.Capacitor.isNativePlatform();
    }
    
    // Inicializar Google Auth nativo
    async inicializarAutenticacionNativa() {
        if (!this.isNativeApp) {
            console.log('📱 Ejecutando en navegador web');
            return;
        }
        
        try {
            console.log('🔧 Buscando plugin GoogleAuth en Capacitor...');
            
            // En Android, el plugin está disponible de varias formas
            // Intentar obtenerlo del registro de Capacitor
            if (window.CapacitorGoogleAuth) {
                this.googleAuth = window.CapacitorGoogleAuth;
                console.log('✅ Plugin encontrado en window.CapacitorGoogleAuth');
            } else if (window.Capacitor && window.Capacitor.Plugins && window.Capacitor.Plugins.GoogleAuth) {
                this.googleAuth = window.Capacitor.Plugins.GoogleAuth;
                console.log('✅ Plugin encontrado en window.Capacitor.Plugins.GoogleAuth');
            } else {
                console.error('❌ Plugin GoogleAuth no encontrado');
                console.log('Capacitor disponible:', !!window.Capacitor);
                console.log('Capacitor.Plugins:', window.Capacitor?.Plugins);
                return;
            }
            
            console.log('✅ Google Auth nativo listo');
            
            // Verificar si hay una sesión activa
            await this.verificarSesionNativa();
            
        } catch (error) {
            console.error('❌ Error al inicializar Google Auth nativo:', error);
            console.error('Stack:', error.stack);
        }
    }
    
    // Verificar si hay una sesión nativa activa
    async verificarSesionNativa() {
        if (!this.isNativeApp || !this.googleAuth) return;
        
        try {
            const user = await this.googleAuth.refresh();
            if (user && user.authentication) {
                this.nativeUser = user;
                console.log('✅ Sesión nativa restaurada:', user.email);
                
                // Emitir evento de login
                window.dispatchEvent(new CustomEvent('oauthLoginCompleto', {
                    detail: {
                        usuario: {
                            nombre: user.givenName || user.displayName,
                            email: user.email,
                            foto: user.imageUrl
                        }
                    }
                }));
            }
        } catch (error) {
            console.log('ℹ️ No hay sesión nativa activa');
        }
    }
    
    // Login con autenticación nativa
    async loginNativo() {
        if (!this.isNativeApp || !this.googleAuth) {
            console.error('❌ Autenticación nativa no disponible');
            console.log('Debug:', { isNativeApp: this.isNativeApp, hasGoogleAuth: !!this.googleAuth });
            throw new Error('Autenticación nativa no disponible. Asegúrate de que el plugin esté instalado.');
        }
        
        try {
            console.log('🔐 Iniciando login nativo...');
            console.log('📋 Llamando a googleAuth.signIn()...');
            
            const user = await this.googleAuth.signIn();
            
            console.log('📦 Respuesta de signIn():', user);
            
            if (!user || !user.authentication) {
                console.error('❌ Usuario o authentication no válido:', user);
                throw new Error('No se pudo obtener información de usuario');
            }
            
            this.nativeUser = user;
            
            console.log('✅ Login nativo exitoso:', user.email);
            
            // Emitir evento de login completo
            window.dispatchEvent(new CustomEvent('oauthLoginCompleto', {
                detail: {
                    usuario: {
                        nombre: user.givenName || user.displayName,
                        email: user.email,
                        foto: user.imageUrl
                    },
                    accessToken: user.authentication.accessToken
                }
            }));
            
            return user;
            
        } catch (error) {
            console.error('❌ Error en login nativo:', error);
            console.error('Tipo de error:', error.constructor.name);
            console.error('Mensaje:', error.message);
            console.error('Stack:', error.stack);
            throw error;
        }
    }
    
    // Logout nativo
    async logoutNativo() {
        if (!this.isNativeApp || !this.googleAuth) return;
        
        try {
            await this.googleAuth.signOut();
            this.nativeUser = null;
            console.log('👋 Logout nativo exitoso');
            
            window.dispatchEvent(new CustomEvent('oauthLoggedOut'));
            
        } catch (error) {
            console.error('❌ Error en logout nativo:', error);
        }
    }
    
    // Verificar si está autenticado (web o nativo)
    estaAutenticadoGeneral() {
        if (this.isNativeApp) {
            return this.nativeUser !== null && this.nativeUser.authentication !== null;
        }
        return this.oauth.estaAutenticado();
    }
    
    // Obtener token de acceso (web o nativo)
    async obtenerAccessToken() {
        if (this.isNativeApp && this.nativeUser) {
            // Verificar si el token está expirado y refrescar si es necesario
            try {
                const user = await this.googleAuth.refresh();
                this.nativeUser = user;
                return user.authentication.accessToken;
            } catch (error) {
                console.error('Error al refrescar token:', error);
                return null;
            }
        }
        
        // Para web, obtener el token del gapi (Google API Client)
        if (typeof gapi !== 'undefined' && gapi.client && gapi.client.getToken()) {
            return gapi.client.getToken().access_token;
        }
        
        console.warn('⚠️ No se pudo obtener el token de acceso');
        return null;
    }
    
    // Obtener app dinámicamente cuando se necesite
    getApp() {
        if (typeof window.app !== 'undefined') {
            return window.app;
        }
        console.warn('⚠️ App aún no está disponible');
        return null;
    }
    
    // ========== CONFIGURACIÓN DE EVENTOS ==========
    
    configurarEventosOAuth() {
        // Evento de login completo
        window.addEventListener('oauthLoginCompleto', async (e) => {
            console.log('✅ Usuario autenticado:', e.detail.usuario);
            this.actualizarUI();
            
            // Cargar automáticamente el backup de Google Drive si existe
            await this.cargarBackupAutomatico();
        });
        
        // Evento de logout
        window.addEventListener('oauthLoggedOut', () => {
            console.log('👋 Usuario desconectado');
            this.actualizarUI();
        });
        
        // Evento de sincronización completa
        window.addEventListener('syncCompleto', (e) => {
            console.log('🔄 Sincronización completa:', e.detail);
            this.procesarEventosCalendar(e.detail.calendar.eventos);
            // No llamar a actualizarUI aquí - solo cuando cambia el estado de autenticación
        });
        
        // Evento de error
        window.addEventListener('syncError', (e) => {
            console.error('❌ Error en sincronización:', e.detail.error);
        });
    }
    
    // ========== SINCRONIZACIÓN AUTOMÁTICA ==========
    
    async cargarBackupAutomatico() {
        if (!this.estaAutenticadoGeneral()) {
            return;
        }
        
        const app = this.getApp();
        if (!app) {
            console.log('⏳ App no disponible aún para cargar backup');
            return;
        }
        
        try {
            console.log('🔍 Buscando backup en Google Drive...');
            
            // Buscar el archivo de backup
            const nombreArchivo = 'peluqueria-canina-backup.json';
            const archivos = await this.buscarArchivosDriveGeneral(nombreArchivo);
            
            if (!archivos || archivos.length === 0) {
                console.log('ℹ️ No hay backup en Google Drive');
                return;
            }
            
            const archivoBackup = archivos[0];
            console.log('📦 Backup encontrado:', archivoBackup.name);
            
            // Descargar y restaurar el backup automáticamente
            const contenido = await this.descargarArchivoDriveGeneral(archivoBackup.id);
            const backup = JSON.parse(contenido);
            
            console.log('📥 Restaurando backup desde Google Drive...');
            await this.restaurarBackupDirecto(backup);
            app.mostrarNotificacion('✅ Datos restaurados desde Google Drive');
            
        } catch (error) {
            console.error('❌ Error en sincronización automática:', error);
            // No mostrar error al usuario, es un proceso en segundo plano
        }
    }
    
    async restaurarBackupDirecto(backup) {
        const app = this.getApp();
        if (!app) return false;
        
        try {
            // Restaurar datos
            app.citas = backup.datos.citas || [];
            app.clientes = backup.datos.clientes || [];
            app.servicios = backup.datos.servicios || [];
            app.razas = backup.datos.razas || [];
            
            console.log('📦 Datos restaurados del backup:', {
                citas: app.citas.length,
                clientes: app.clientes.length,
                servicios: app.servicios.length,
                razas: app.razas.length
            });
            
            // Guardar en localStorage
            app.guardarDatos('citas', app.citas);
            app.guardarDatos('clientes', app.clientes);
            app.guardarDatos('servicios', app.servicios);
            app.guardarDatos('razas', app.razas);
            
            // Guardar marca de tiempo de sincronización
            localStorage.setItem('ultimaModificacion', backup.fecha || new Date().toISOString());
            
            // Actualizar la interfaz
            app.cargarServicios();
            app.cargarClientesEnSelect();
            app.cargarRazasEnSelects();
            app.mostrarServicios();
            app.mostrarAgenda('todas');
            app.mostrarClientes();
            app.mostrarRazas();
            app.actualizarEstadisticas();
            
            console.log('✅ Backup restaurado correctamente');
            return true;
            
        } catch (error) {
            console.error('Error al restaurar backup:', error);
            return false;
        }
    }
    
    // ========== INTEGRACIÓN CON CALENDAR ==========
    
    async procesarEventosCalendar(eventos) {
        if (!eventos || eventos.length === 0) {
            console.log('No hay eventos para procesar');
            return;
        }
        
        console.log(`📅 Procesando ${eventos.length} eventos del calendario...`);
        
        // Filtrar solo eventos futuros y relevantes
        const ahora = new Date();
        const eventosFuturos = eventos.filter(evento => {
            const inicio = new Date(evento.start?.dateTime || evento.start?.date);
            return inicio >= ahora;
        });
        
        console.log(`✅ ${eventosFuturos.length} eventos futuros encontrados`);
        
        // Aquí podrías importar automáticamente las citas si lo deseas
        // Por ahora solo mostramos la información
    }
    
    convertirCitaAEvento(cita) {
        // Convertir cita a formato de evento de Google Calendar
        const nombreCliente = cita.clienteNombre || 'Cliente';
        const nombrePerro = cita.perroNombre || 'Perro';
        const raza = cita.raza || 'Sin raza';
        const servicios = cita.servicios || [cita.servicio] || ['Servicio'];
        const servicioTexto = Array.isArray(servicios) ? servicios.join(', ') : servicios;
        const precio = cita.precio || 0;
        
        const fechaHora = new Date(`${cita.fecha}T${cita.hora}`);
        const duracion = 60; // duración por defecto: 60 min
        const fechaFin = new Date(fechaHora.getTime() + duracion * 60000);
        
        return {
            summary: `🐕 ${nombrePerro} - ${servicioTexto}`,
            description: `Cliente: ${nombreCliente}\nTeléfono: ${cita.telefono || 'N/A'}\nPerro: ${nombrePerro} (${raza})\nServicios: ${servicioTexto}\nPrecio: ${precio}€\n\nNotas: ${cita.notas || 'Sin notas'}`,
            start: {
                dateTime: fechaHora.toISOString(),
                timeZone: 'Europe/Madrid'
            },
            end: {
                dateTime: fechaFin.toISOString(),
                timeZone: 'Europe/Madrid'
            },
            reminders: {
                useDefault: false,
                overrides: [
                    { method: 'popup', minutes: 60 },
                    { method: 'popup', minutes: 1440 } // 1 día antes
                ]
            }
        };
    }
    
    async exportarCitaACalendar(cita) {
        if (!this.estaAutenticadoGeneral()) {
            alert('⚠️ Debes autenticarte con Google primero');
            return null;
        }
        
        const app = this.getApp();
        if (!app) {
            const error = new Error('App no está disponible. Espera a que la página cargue completamente.');
            console.error('❌', error.message);
            throw error;
        }
        
        try {
            // Verificar si la cita ya tiene un evento en Google Calendar
            if (cita.googleEventId) {
                console.log('ℹ️ Esta cita ya tiene un evento en Calendar:', cita.googleEventId);
                
                // Verificar si el evento todavía existe en Google
                try {
                    const eventoExistente = await this.obtenerEventoCalendarGeneral(cita.googleEventId);
                    if (eventoExistente) {
                        console.log('✅ El evento ya existe en Google Calendar, no se duplicará');
                        return eventoExistente;
                    }
                } catch (error) {
                    // Si el evento no existe (fue eliminado), continuar para crear uno nuevo
                    console.log('⚠️ El evento fue eliminado de Calendar, se creará uno nuevo');
                    cita.googleEventId = null;
                }
            }
            
            // Las citas tienen los datos directamente, no necesitamos buscar en clientes/servicios
            const nombreCliente = cita.clienteNombre || 'Cliente';
            const nombrePerro = cita.perroNombre || 'Perro';
            const raza = cita.raza || 'Sin raza';
            const servicios = cita.servicios || [cita.servicio] || ['Servicio'];
            const servicioTexto = Array.isArray(servicios) ? servicios.join(', ') : servicios;
            const precio = cita.precio || 0;
            
            // Crear evento para Google Calendar
            const fechaHora = new Date(`${cita.fecha}T${cita.hora}`);
            const duracion = 60; // duración por defecto: 60 min
            const fechaFin = new Date(fechaHora.getTime() + duracion * 60000);
            
            const evento = {
                summary: `🐕 ${nombrePerro} - ${servicioTexto}`,
                description: `Cliente: ${nombreCliente}\nTeléfono: ${cita.telefono || 'N/A'}\nPerro: ${nombrePerro} (${raza})\nServicios: ${servicioTexto}\nPrecio: ${precio}€\n\nNotas: ${cita.notas || 'Sin notas'}`,
                start: {
                    dateTime: fechaHora.toISOString(),
                    timeZone: 'Europe/Madrid'
                },
                end: {
                    dateTime: fechaFin.toISOString(),
                    timeZone: 'Europe/Madrid'
                },
                reminders: {
                    useDefault: false,
                    overrides: [
                        { method: 'popup', minutes: 60 },
                        { method: 'popup', minutes: 1440 } // 1 día antes
                    ]
                }
            };
            
            const resultado = await this.crearEventoCalendarGeneral(evento);
            
            // Guardar el ID del evento en la cita
            cita.googleEventId = resultado.id;
            
            // Buscar la cita en el array y actualizar su googleEventId por si acaso
            const citaEnApp = app.citas.find(c => c.id === cita.id);
            if (citaEnApp) {
                citaEnApp.googleEventId = resultado.id;
            }
            
            app.guardarDatos('citas', app.citas);
            
            console.log('✅ Cita exportada a Google Calendar:', resultado);
            
            return resultado;
            
        } catch (error) {
            console.error('Error al exportar cita a Calendar:', error);
            alert('❌ Error al exportar a Google Calendar: ' + error.message);
            return null;
        }
    }
    
    // Métodos generales para API de Google (soportan web y nativo)
    async crearEventoCalendarGeneral(evento) {
        const token = await this.obtenerAccessToken();
        if (!token) {
            throw new Error('No hay token de acceso disponible');
        }
        
        const response = await fetch('https://www.googleapis.com/calendar/v3/calendars/primary/events', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(evento)
        });
        
        if (!response.ok) {
            throw new Error(`Error al crear evento: ${response.statusText}`);
        }
        
        return await response.json();
    }
    
    async obtenerEventoCalendarGeneral(eventId) {
        const token = await this.obtenerAccessToken();
        if (!token) {
            throw new Error('No hay token de acceso disponible');
        }
        
        const response = await fetch(
            `https://www.googleapis.com/calendar/v3/calendars/primary/events/${eventId}`,
            {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            }
        );
        
        if (!response.ok) {
            if (response.status === 404) {
                return null;
            }
            throw new Error(`Error al obtener evento: ${response.statusText}`);
        }
        
        return await response.json();
    }
    
    async buscarArchivosDriveGeneral(nombreArchivo) {
        const token = await this.obtenerAccessToken();
        if (!token) {
            throw new Error('No hay token de acceso disponible');
        }
        
        const query = `name='${nombreArchivo}' and trashed=false`;
        const response = await fetch(
            `https://www.googleapis.com/drive/v3/files?q=${encodeURIComponent(query)}&fields=files(id,name,modifiedTime)`,
            {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            }
        );
        
        if (!response.ok) {
            throw new Error(`Error al buscar archivos: ${response.statusText}`);
        }
        
        const data = await response.json();
        return data.files || [];
    }
    
    async descargarArchivoDriveGeneral(fileId) {
        const token = await this.obtenerAccessToken();
        if (!token) {
            throw new Error('No hay token de acceso disponible');
        }
        
        const response = await fetch(
            `https://www.googleapis.com/drive/v3/files/${fileId}?alt=media`,
            {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            }
        );
        
        if (!response.ok) {
            throw new Error(`Error al descargar archivo: ${response.statusText}`);
        }
        
        return await response.text();
    }
    
    async subirArchivoDriveGeneral(nombreArchivo, contenido) {
        const token = await this.obtenerAccessToken();
        if (!token) {
            throw new Error('No hay token de acceso disponible');
        }
        
        const metadata = {
            name: nombreArchivo,
            mimeType: 'application/json'
        };
        
        const formData = new FormData();
        formData.append('metadata', new Blob([JSON.stringify(metadata)], { type: 'application/json' }));
        formData.append('file', new Blob([contenido], { type: 'application/json' }));
        
        const response = await fetch(
            'https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart',
            {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                body: formData
            }
        );
        
        if (!response.ok) {
            throw new Error(`Error al subir archivo: ${response.statusText}`);
        }
        
        return await response.json();
    }
    
    async actualizarArchivoDriveGeneral(fileId, contenido) {
        const token = await this.obtenerAccessToken();
        if (!token) {
            throw new Error('No hay token de acceso disponible');
        }
        
        const response = await fetch(
            `https://www.googleapis.com/upload/drive/v3/files/${fileId}?uploadType=media`,
            {
                method: 'PATCH',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: contenido
            }
        );
        
        if (!response.ok) {
            throw new Error(`Error al actualizar archivo: ${response.statusText}`);
        }
        
        return await response.json();
    }
    
    // ========== INTEGRACIÓN CON DRIVE ==========
    
    async hacerBackup() {
        if (!this.estaAutenticadoGeneral()) {
            alert('⚠️ Debes autenticarte con Google primero');
            return null;
        }
        
        const app = this.getApp();
        if (!app) {
            alert('⚠️ App no disponible aún');
            return null;
        }
        
        try {
            // Crear backup completo
            const fechaBackup = new Date().toISOString();
            const backup = {
                version: '1.0',
                fecha: fechaBackup,
                datos: {
                    citas: app.citas,
                    clientes: app.clientes,
                    servicios: app.servicios,
                    razas: app.razas
                }
            };
            
            const contenido = JSON.stringify(backup, null, 2);
            // Usar siempre el mismo nombre de archivo para sobreescribir
            const nombreArchivo = 'peluqueria-canina-backup.json';
            
            // Buscar si ya existe un backup anterior
            const archivosExistentes = await this.buscarArchivosDriveGeneral(nombreArchivo);
            
            let resultado;
            if (archivosExistentes && archivosExistentes.length > 0) {
                // Actualizar el archivo existente
                console.log('📝 Actualizando backup existente...');
                resultado = await this.actualizarArchivoDriveGeneral(archivosExistentes[0].id, contenido);
            } else {
                // Crear nuevo archivo
                console.log('📝 Creando nuevo backup...');
                resultado = await this.subirArchivoDriveGeneral(nombreArchivo, contenido);
            }
            
            // Guardar marca de tiempo local
            localStorage.setItem('ultimaModificacion', fechaBackup);
            
            console.log('✅ Backup guardado en Google Drive:', resultado);
            
            return resultado;
            
        } catch (error) {
            console.error('Error al hacer backup:', error);
            throw error;
        }
    }
    
    async restaurarBackup(fileId, yaConfirmado = false) {
        if (!this.estaAutenticadoGeneral()) {
            alert('⚠️ Debes autenticarte con Google primero');
            return false;
        }
        
        const app = this.getApp();
        if (!app) {
            alert('⚠️ App no disponible aún');
            return false;
        }
        
        try {
            const contenido = await this.descargarArchivoDriveGeneral(fileId);
            const backup = JSON.parse(contenido);
            
            // Confirmar restauración solo si no fue confirmado previamente
            if (!yaConfirmado) {
                if (!confirm('⚠️ ¿Estás seguro de que quieres restaurar este backup? Se sobrescribirán todos los datos actuales.')) {
                    return false;
                }
            }
            
            // Restaurar datos
            app.citas = backup.datos.citas || [];
            app.clientes = backup.datos.clientes || [];
            app.servicios = backup.datos.servicios || [];
            app.razas = backup.datos.razas || [];
            
            console.log('📦 Datos restaurados del backup:', {
                citas: app.citas.length,
                clientes: app.clientes.length,
                servicios: app.servicios.length,
                razas: app.razas.length
            });
            
            // Guardar en localStorage
            app.guardarDatos('citas', app.citas);
            app.guardarDatos('clientes', app.clientes);
            app.guardarDatos('servicios', app.servicios);
            app.guardarDatos('razas', app.razas);
            
            // Actualizar la interfaz
            app.cargarServicios();
            app.cargarClientesEnSelect();
            app.cargarRazasEnSelects();
            app.mostrarServicios();
            app.mostrarAgenda('todas');
            app.mostrarClientes();
            app.mostrarRazas();
            app.actualizarEstadisticas();
            
            console.log('✅ Backup restaurado correctamente');
            
            if (!yaConfirmado) {
                alert('✅ Backup restaurado exitosamente');
            }
            
            return true;
            
        } catch (error) {
            console.error('Error al restaurar backup:', error);
            alert('❌ Error al restaurar backup: ' + error.message);
            return false;
        }
    }
    
    // ========== ACTUALIZACIÓN DE UI ==========
    
    actualizarUI() {
        const estado = this.obtenerEstado();
        
        // Emitir evento personalizado para que index.html actualice la UI
        window.dispatchEvent(new CustomEvent('oauthStateChanged', { detail: estado }));
        
        // También llamar directamente a la función si existe
        if (typeof window.updateUserUI === 'function') {
            window.updateUserUI();
        }
    }
    
    obtenerEstado() {
        if (this.isNativeApp && this.nativeUser) {
            // Estado para app nativa
            return {
                autenticado: true,
                usuario: {
                    nombre: this.nativeUser.givenName || this.nativeUser.displayName,
                    email: this.nativeUser.email,
                    foto: this.nativeUser.imageUrl
                },
                eventosCalendar: 0,
                archivosGDrive: 0,
                ultimaSincronizacion: null,
                plataforma: 'nativa'
            };
        }
        
        // Estado para web
        const datosCache = this.oauth.obtenerDatosEnCache();
        
        return {
            autenticado: this.oauth.estaAutenticado(),
            usuario: datosCache.usuario,
            eventosCalendar: datosCache.calendar.count,
            archivosGDrive: datosCache.drive.count,
            ultimaSincronizacion: this.obtenerUltimaSincronizacion(),
            plataforma: 'web'
        };
    }
    
    obtenerUltimaSincronizacion() {
        try {
            const cache = localStorage.getItem('oauth_cache');
            if (cache) {
                const data = JSON.parse(cache);
                return data.timestamp ? new Date(data.timestamp) : null;
            }
        } catch (error) {
            console.error('Error al obtener última sincronización:', error);
        }
        return null;
    }
}

// Inicializar integración cuando la app esté lista
window.addEventListener('DOMContentLoaded', () => {
    // Esperar a que oauthManager esté disponible
    const inicializarIntegracion = () => {
        if (typeof window.oauthManager !== 'undefined') {
            // Crear integración (app se obtendrá dinámicamente cuando se necesite)
            window.oauthIntegration = new OAuthIntegration(window.oauthManager);
            console.log('✅ OAuth Integration inicializado');
            
            // Actualizar UI inicial
            if (typeof window.updateUserUI === 'function') {
                window.updateUserUI();
            }
        } else {
            // Reintentar en 50ms
            setTimeout(inicializarIntegracion, 50);
        }
    };
    
    inicializarIntegracion();
});
