/**
 * EJEMPLO: Cómo Integrar Google Calendar y Drive en la app Peluquería Canina
 * 
 * Este archivo muestra ejemplos prácticos de cómo usar OAuth automáticamente
 * desde la clase PeluqueriaCanina
 */

// ============================================================================
// EJEMPLO 1: Detectar cuando hay sesión activa
// ============================================================================

function verificarAutenticacion() {
    if (typeof oauthIntegration === 'undefined') {
        console.warn('OAuth no está disponible');
        return false;
    }

    const estado = oauthIntegration.obtenerEstado();
    
    if (estado.autenticado) {
        console.log(`✅ Autenticado como: ${estado.usuario.name}`);
        console.log(`📅 ${estado.eventosCalendar} eventos sincronizados`);
        console.log(`📂 ${estado.archivosGDrive} archivos en Drive`);
        return true;
    } else {
        console.log('❌ No hay sesión activa');
        return false;
    }
}

// ============================================================================
// EJEMPLO 2: Guardar una cita automáticamente en Google Calendar
// ============================================================================

async function guardarCitaEnGoogle(cita) {
    if (!verificarAutenticacion()) {
        alert('Por favor, autentica con Google primero');
        return;
    }

    try {
        // La cita viene de app.js con esta estructura:
        // {
        //   cliente: "Juan García",
        //   perro: "Max",
        //   fecha: "2025-01-10",
        //   hora: "14:30",
        //   servicio: "Baño y Corte",
        //   observaciones: "Corte estilo teddy",
        //   precio: 500
        // }

        console.log('📅 Guardando cita en Google Calendar...');
        
        const resultado = await oauthIntegration.agregarCitaAlCalendar(cita);
        
        if (resultado) {
            console.log('✅ Cita guardada en Google Calendar');
            console.log('ID del evento:', resultado.id);
            
            // Guardar el ID de Google en la cita local
            cita.googleEventId = resultado.id;
        }
    } catch (error) {
        console.error('❌ Error guardando cita:', error);
    }
}

// ============================================================================
// EJEMPLO 3: Cuando se crea una NUEVA CITA, sincronizarla automáticamente
// ============================================================================

// En el método guardarCita() de PeluqueriaCanina, agregar:

function guardarCitaMejorado(cita) {
    // Código original de guardarDatos...
    app.citas.push(cita);
    app.guardarDatos('citas', app.citas);
    
    // NUEVO: Sincronizar con Google si está autenticado
    if (typeof oauthIntegration !== 'undefined' && 
        oauthIntegration.obtenerEstado().autenticado) {
        
        guardarCitaEnGoogle(cita);
    }
}

// ============================================================================
// EJEMPLO 4: Importar eventos de Google Calendar a la app
// ============================================================================

async function importarEventosDelCalendar() {
    if (!verificarAutenticacion()) return;

    try {
        console.log('📥 Importando eventos del calendario...');
        
        // Los eventos ya vienen sincronizados automáticamente
        const eventos = oauthIntegration.oauth.calendarEvents;
        
        console.log(`Encontrados ${eventos.length} eventos`);
        
        eventos.forEach(evento => {
            const inicio = new Date(evento.start.dateTime || evento.start.date);
            const fecha = inicio.toISOString().split('T')[0];
            const hora = inicio.toLocaleTimeString('es-ES', { 
                hour: '2-digit', 
                minute: '2-digit' 
            });
            
            console.log(`- ${evento.summary} (${fecha} ${hora})`);
            
            // Crear cita de importación
            const citaImportada = {
                cliente: evento.summary?.split('-')[0]?.trim() || 'Importado',
                perro: evento.summary?.split('-')[1]?.trim() || '',
                fecha: fecha,
                hora: hora,
                servicio: 'Importado de Calendar',
                observaciones: evento.description || '',
                precio: 0,
                googleEventId: evento.id,
                sincronizado: true
            };
            
            // Agregar solo si no existe
            const yaExiste = app.citas.some(c => c.googleEventId === evento.id);
            if (!yaExiste) {
                app.citas.push(citaImportada);
                console.log(`✅ Importado: ${evento.summary}`);
            }
        });
        
        app.guardarDatos('citas', app.citas);
        app.mostrarAgenda('semana');
        
    } catch (error) {
        console.error('❌ Error importando eventos:', error);
    }
}

// ============================================================================
// EJEMPLO 5: Hacer backup automático de todos los datos
// ============================================================================

async function hacerBackupAutomatico() {
    if (!verificarAutenticacion()) {
        console.warn('⚠️ No se puede hacer backup sin autenticación');
        return;
    }

    try {
        console.log('💾 Iniciando backup automático...');
        
        // Preparar datos para backup
        const datosBackup = {
            timestamp: new Date().toISOString(),
            version: '1.0',
            citas: app.citas || [],
            clientes: app.clientes || [],
            servicios: app.servicios || [],
            razas: app.razas || [],
            estadisticas: {
                totalCitas: app.citas?.length || 0,
                totalClientes: app.clientes?.length || 0,
                serviciosActivos: app.servicios?.length || 0
            }
        };
        
        // Subir a Google Drive
        await oauthIntegration.hacerBackupAutomatico(datosBackup);
        
        console.log('✅ Backup completado');
        
    } catch (error) {
        console.error('❌ Error en backup:', error);
    }
}

// ============================================================================
// EJEMPLO 6: Obtener lista de backups disponibles
// ============================================================================

async function listarBackupsDisponibles() {
    if (!verificarAutenticacion()) return [];

    try {
        console.log('📦 Obteniendo lista de backups...');
        
        const backups = await oauthIntegration.obtenerBackupsDisponibles();
        
        backups.forEach(backup => {
            console.log(`- ${backup.name} (${backup.modifiedTime})`);
            console.log(`  📎 Link: ${backup.webViewLink}`);
        });
        
        return backups;
        
    } catch (error) {
        console.error('❌ Error listando backups:', error);
        return [];
    }
}

// ============================================================================
// EJEMPLO 7: Restaurar datos desde un backup
// ============================================================================

async function restaurarDesdeBackup(backupId) {
    if (!verificarAutenticacion()) return;

    try {
        console.log(`📥 Restaurando desde backup ${backupId}...`);
        
        // En una implementación real, aquí descargarías el archivo de Drive
        // y reemplazarías los datos locales
        
        await oauthIntegration.oauth.restaurarDesdeBackup(backupId);
        
        console.log('✅ Backup restaurado');
        
    } catch (error) {
        console.error('❌ Error restaurando backup:', error);
    }
}

// ============================================================================
// EJEMPLO 8: Escuchar eventos de sincronización en tiempo real
// ============================================================================

function configurarListenadoresOAuth() {
    // Cuando se completa una sincronización
    window.addEventListener('syncCompleto', (e) => {
        const { calendar, drive, syncTime } = e.detail;
        
        console.log('🔄 Sincronización completada');
        console.log(`   📅 ${calendar.count} eventos`);
        console.log(`   📂 ${drive.count} archivos`);
        
        // Actualizar indicador en la interfaz
        const statusEl = document.getElementById('oauth-status');
        if (statusEl) {
            statusEl.textContent = `✓ Últimas ${calendar.count} eventos`;
        }
        
        // Recargar agenda
        if (typeof app !== 'undefined' && app.mostrarAgenda) {
            app.mostrarAgenda('semana');
        }
    });

    // Cuando hay error en sincronización
    window.addEventListener('syncError', (e) => {
        console.error('❌ Error en sincronización:', e.detail.error);
        alert('Error sincronizando con Google. Intenta más tarde.');
    });

    // Cuando se crea un evento
    window.addEventListener('eventCreated', (e) => {
        console.log('📅 Nuevo evento creado:', e.detail);
    });

    // Cuando se sube un archivo
    window.addEventListener('fileUploaded', (e) => {
        console.log('📂 Archivo subido:', e.detail);
    });

    // Cuando se hace un backup
    window.addEventListener('backupAutomatico', (e) => {
        console.log(`💾 Backup realizado: ${e.detail.fileName}`);
    });
}

// ============================================================================
// EJEMPLO 9: Agregar botón de Login en la interfaz
// ============================================================================

function crearBotonLogin() {
    const boton = document.createElement('button');
    boton.textContent = '🔗 Conectar con Google';
    boton.style.cssText = `
        position: fixed;
        top: 10px;
        right: 10px;
        padding: 10px 20px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        border: none;
        border-radius: 6px;
        cursor: pointer;
        font-weight: bold;
        z-index: 1000;
    `;
    
    boton.onclick = async () => {
        if (oauthIntegration.obtenerEstado().autenticado) {
            if (confirm('¿Cerrar sesión?')) {
                await oauthIntegration.oauth.logout();
                boton.textContent = '🔗 Conectar con Google';
            }
        } else {
            boton.disabled = true;
            boton.textContent = '⏳ Conectando...';
            try {
                await oauthIntegration.oauth.iniciarLoginGoogle();
            } catch (error) {
                console.error('Error:', error);
                boton.textContent = '🔗 Conectar con Google';
                boton.disabled = false;
            }
        }
    };
    
    document.body.appendChild(boton);
}

// ============================================================================
// EJEMPLO 10: Inicializar todo al cargar la página
// ============================================================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('🚀 Inicializando integración OAuth...');
    
    // 1. Crear botón de login
    crearBotonLogin();
    
    // 2. Configurar listeners de eventos
    configurarListenadoresOAuth();
    
    // 3. Verificar autenticación
    setTimeout(() => {
        if (verificarAutenticacion()) {
            // 4. Si está autenticado, importar eventos
            importarEventosDelCalendar();
            
            // 5. Hacer backup cada 1 hora
            setInterval(() => {
                hacerBackupAutomatico();
            }, 60 * 60 * 1000);
        }
    }, 2000);
});

// ============================================================================
// INTEGRACIONES CON MÉTODOS EXISTENTES DE PeluqueriaCanina
// ============================================================================

// En el método constructor de PeluqueriaCanina:
/*
    constructor() {
        // ... código existente ...
        
        // NUEVO: Integración OAuth
        if (typeof oauthIntegration !== 'undefined') {
            // Escuchar cambios de sincronización
            window.addEventListener('syncCompleto', () => {
                this.mostrarAgenda('semana');
            });
        }
    }
*/

// En el método guardarCita:
/*
    guardarCita(cita) {
        // ... código existente ...
        
        // NUEVO: Sincronizar con Google
        if (typeof oauthIntegration !== 'undefined' && 
            oauthIntegration.obtenerEstado().autenticado) {
            guardarCitaEnGoogle(cita);
        }
    }
*/

// En el método eliminarCita:
/*
    eliminarCita(id) {
        // ... código existente ...
        
        // NUEVO: Limpiar eventos de Google si existen
        const cita = this.citas.find(c => c.id === id);
        if (cita && cita.googleEventId) {
            console.log('Evento de Google eliminado:', cita.googleEventId);
        }
    }
*/

console.log('✅ Ejemplos de integración OAuth cargados');
