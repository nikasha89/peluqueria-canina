/**
 * OAuth Integration - Integración de OAuth con la aplicación Peluquería Canina
 * Conecta el sistema OAuth (sin backend) con las funcionalidades de la app
 */

class OAuthIntegration {
    constructor(oauthManager) {
        this.oauth = oauthManager;
        
        this.configurarEventosOAuth();
        this.actualizarUI();
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
        window.addEventListener('oauthLoginCompleto', (e) => {
            console.log('✅ Usuario autenticado:', e.detail.usuario);
            this.actualizarUI();
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
    
    async exportarCitaACalendar(cita) {
        if (!this.oauth.estaAutenticado()) {
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
            
            const resultado = await this.oauth.crearEventoCalendar(evento);
            
            // Guardar el ID del evento en la cita
            cita.googleCalendarEventId = resultado.id;
            app.guardarDatos('citas', app.citas);
            
            console.log('✅ Cita exportada a Google Calendar:', resultado);
            
            return resultado;
            
        } catch (error) {
            console.error('Error al exportar cita a Calendar:', error);
            alert('❌ Error al exportar a Google Calendar: ' + error.message);
            return null;
        }
    }
    
    // ========== INTEGRACIÓN CON DRIVE ==========
    
    async hacerBackup() {
        if (!this.oauth.estaAutenticado()) {
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
            const backup = {
                version: '1.0',
                fecha: new Date().toISOString(),
                datos: {
                    citas: app.citas,
                    clientes: app.clientes,
                    servicios: app.servicios,
                    razas: app.razas
                }
            };
            
            const contenido = JSON.stringify(backup, null, 2);
            const nombreArchivo = `peluqueria-backup-${new Date().toISOString().split('T')[0]}.json`;
            
            const resultado = await this.oauth.subirArchivoDrive(nombreArchivo, contenido);
            
            console.log('✅ Backup guardado en Google Drive:', resultado);
            alert('✅ Backup guardado exitosamente en Google Drive');
            
            return resultado;
            
        } catch (error) {
            console.error('Error al hacer backup:', error);
            alert('❌ Error al guardar backup: ' + error.message);
            return null;
        }
    }
    
    async restaurarBackup(fileId) {
        if (!this.oauth.estaAutenticado()) {
            alert('⚠️ Debes autenticarte con Google primero');
            return false;
        }
        
        const app = this.getApp();
        if (!app) {
            alert('⚠️ App no disponible aún');
            return false;
        }
        
        try {
            const contenido = await this.oauth.descargarArchivoDrive(fileId);
            const backup = JSON.parse(contenido);
            
            // Confirmar restauración
            if (!confirm('⚠️ ¿Estás seguro de que quieres restaurar este backup? Se sobrescribirán todos los datos actuales.')) {
                return false;
            }
            
            // Restaurar datos
            app.citas = backup.datos.citas || [];
            app.clientes = backup.datos.clientes || [];
            app.servicios = backup.datos.servicios || [];
            app.razas = backup.datos.razas || [];
            
            // Guardar en localStorage
            app.guardarDatos('citas', app.citas);
            app.guardarDatos('clientes', app.clientes);
            app.guardarDatos('servicios', app.servicios);
            app.guardarDatos('razas', app.razas);
            
            // Actualizar la interfaz
            app.mostrarAgenda('semana');
            app.mostrarClientes();
            app.mostrarServicios();
            app.mostrarRazas();
            app.actualizarEstadisticas();
            
            console.log('✅ Backup restaurado correctamente');
            alert('✅ Backup restaurado exitosamente');
            
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
        const datosCache = this.oauth.obtenerDatosEnCache();
        
        return {
            autenticado: this.oauth.estaAutenticado(),
            usuario: datosCache.usuario,
            eventosCalendar: datosCache.calendar.count,
            archivosGDrive: datosCache.drive.count,
            ultimaSincronizacion: this.obtenerUltimaSincronizacion()
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
