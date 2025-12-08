// Gestión de datos
class PeluqueriaCanina {
    constructor() {
        this.citas = this.cargarDatos('citas') || [];
        this.clientes = this.cargarDatos('clientes') || [];
        this.servicios = this.cargarDatos('servicios') || this.serviciosDefault();
        this.razas = this.cargarDatos('razas') || this.razasDefault();
        
        // Limpiar servicios con estructura antigua
        this.servicios = this.servicios.filter(s => {
            if (s.tipo === 'porRaza') {
                // Eliminar servicios con estructura antigua (precios) o sin longitud de pelo en combinaciones
                if (s.precios && !s.preciosCombinaciones) return false;
                if (s.preciosCombinaciones && s.preciosCombinaciones.some(c => !c.longitudPelo)) return false;
            }
            return true;
        });
        this.guardarDatos('servicios', this.servicios);
        
        // Si no hay razas guardadas, guardar las razas por defecto
        if (!this.cargarDatos('razas')) {
            this.guardarDatos('razas', this.razas);
        }
        
        this.googleConfig = this.cargarDatos('googleConfig') || { clientId: '', apiKey: '' };
        this.googleCalendarToken = this.cargarDatos('googleToken') || null;
        
        // Configuración de Google Drive
        this.driveConfig = this.cargarDatos('driveConfig') || { clientId: '', apiKey: '', autoSync: false };
        this.driveToken = null;
        this.driveFileId = this.cargarDatos('driveFileId') || null;
        this.lastSync = this.cargarDatos('lastSync') || null;
        
        this.inicializar();
    }

    inicializar() {
        this.configurarFormulario();
        this.configurarFormularioServicio();
        this.configurarFormularioCliente();
        this.configurarFormularioRaza();
        this.configurarBusqueda();
        this.cargarServicios();
        this.cargarClientesEnSelect();
        this.cargarRazasEnSelects();
        this.cargarTamanosEnSelects(); // Cargar tamaños dinámicamente
        // this.cargarConfigGoogle(); // OBSOLETO - ya no se usa
        this.mostrarServicios();
        this.mostrarAgenda('todas'); // Mostrar todas las citas por defecto
        this.mostrarClientes();
        this.mostrarRazas();
        this.actualizarEstadisticas();
        this.preciosCombinacionesTemp = []; // Array temporal para combinaciones de precios
        this.perrosTemp = []; // Array temporal para perros del cliente
        
        // Establecer fecha mínima a hoy
        document.getElementById('fecha').min = new Date().toISOString().split('T')[0];
        
        // Sincronización automática al inicio si está configurado Drive
        this.sincronizarAlInicio();
    }

    // Razas por defecto
    razasDefault() {
        return [
            { id: 1, nombre: 'Affenpinscher' },
            { id: 2, nombre: 'Akita' },
            { id: 3, nombre: 'Basset Hound' },
            { id: 4, nombre: 'Beagle' },
            { id: 5, nombre: 'Bichón Frisé' },
            { id: 6, nombre: 'Bichón Maltés' },
            { id: 7, nombre: 'Border Collie' },
            { id: 8, nombre: 'Boston Terrier' },
            { id: 9, nombre: 'Boxer' },
            { id: 10, nombre: 'Bulldog Francés' },
            { id: 11, nombre: 'Bulldog Inglés' },
            { id: 12, nombre: 'Bull Terrier' },
            { id: 13, nombre: 'Caniche/Poodle Toy' },
            { id: 14, nombre: 'Caniche/Poodle Miniatura' },
            { id: 15, nombre: 'Caniche/Poodle Estándar' },
            { id: 16, nombre: 'Carlino/Pug' },
            { id: 17, nombre: 'Chihuahua' },
            { id: 18, nombre: 'Chow Chow' },
            { id: 19, nombre: 'Cocker Spaniel' },
            { id: 20, nombre: 'Collie' },
            { id: 21, nombre: 'Dálmata' },
            { id: 22, nombre: 'Doberman' },
            { id: 23, nombre: 'Dogo Argentino' },
            { id: 24, nombre: 'Fox Terrier' },
            { id: 25, nombre: 'Galgo' },
            { id: 26, nombre: 'Golden Retriever' },
            { id: 27, nombre: 'Gran Danés' },
            { id: 28, nombre: 'Husky Siberiano' },
            { id: 29, nombre: 'Jack Russell Terrier' },
            { id: 30, nombre: 'Labrador Retriever' },
            { id: 31, nombre: 'Mastín' },
            { id: 32, nombre: 'Pastor Alemán' },
            { id: 33, nombre: 'Pastor Belga' },
            { id: 34, nombre: 'Pequinés' },
            { id: 35, nombre: 'Pomerania' },
            { id: 36, nombre: 'Rottweiler' },
            { id: 37, nombre: 'San Bernardo' },
            { id: 38, nombre: 'Schnauzer Miniatura' },
            { id: 39, nombre: 'Schnauzer Gigante' },
            { id: 40, nombre: 'Setter Irlandés' },
            { id: 41, nombre: 'Shar Pei' },
            { id: 42, nombre: 'Shiba Inu' },
            { id: 43, nombre: 'Shih Tzu' },
            { id: 44, nombre: 'Springer Spaniel' },
            { id: 45, nombre: 'Staffordshire Bull Terrier' },
            { id: 46, nombre: 'Terranova' },
            { id: 47, nombre: 'Weimaraner' },
            { id: 48, nombre: 'West Highland White Terrier' },
            { id: 49, nombre: 'Yorkshire Terrier' },
            { id: 50, nombre: 'Mestizo' }
        ];
    }

    // Servicios por defecto
    serviciosDefault() {
        return [
            { 
                id: 1, 
                nombre: 'Baño', 
                tipo: 'porRaza',
                precios: { mini: 15, pequeno: 20, mediano: 25, grande: 30, gigante: 35 },
                descripcion: 'Baño completo con champú y secado según raza y tamaño' 
            },
            { 
                id: 2, 
                nombre: 'Corte', 
                tipo: 'porRaza',
                precios: { mini: 20, pequeno: 25, mediano: 30, grande: 35, gigante: 40 },
                descripcion: 'Corte de pelo según raza y tamaño' 
            },
            { 
                id: 3, 
                nombre: 'Baño + Corte', 
                tipo: 'porRaza',
                precios: { mini: 30, pequeno: 40, mediano: 50, grande: 60, gigante: 70 },
                descripcion: 'Servicio completo según raza y tamaño' 
            },
            { 
                id: 4, 
                nombre: 'Deslanado', 
                tipo: 'porRaza',
                precios: { mini: 25, pequeno: 30, mediano: 35, grande: 40, gigante: 45 },
                descripcion: 'Eliminación de pelo muerto según raza y tamaño' 
            },
            { 
                id: 5, 
                nombre: 'Corte de Uñas', 
                tipo: 'fijo',
                precio: 10, 
                descripcion: 'Corte y limado de uñas' 
            },
            { 
                id: 6, 
                nombre: 'Limpieza de Oídos', 
                tipo: 'fijo',
                precio: 8, 
                descripcion: 'Limpieza profunda de oídos' 
            },
            { 
                id: 7, 
                nombre: 'Vaciado de Glándulas', 
                tipo: 'fijo',
                precio: 12, 
                descripcion: 'Vaciado de glándulas anales' 
            }
        ];
    }

    // Guardar y cargar datos
    guardarDatos(clave, datos) {
        localStorage.setItem(clave, JSON.stringify(datos));
        
        // Actualizar timestamp de última modificación para datos importantes
        if (['citas', 'clientes', 'servicios', 'razas'].includes(clave)) {
            localStorage.setItem('ultimaModificacion', new Date().toISOString());
        }
    }

    cargarDatos(clave) {
        const datos = localStorage.getItem(clave);
        return datos ? JSON.parse(datos) : null;
    }

    // Configurar formulario de citas
    configurarFormulario() {
        const form = document.getElementById('citaForm');
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            this.agregarCita();
        });

        // Configurar cambio de cliente
        const clienteSelect = document.getElementById('clienteSelect');
        clienteSelect.addEventListener('change', (e) => {
            this.onClienteChange(e.target.value);
        });

        // Configurar cambio de perro
        const perroSelect = document.getElementById('perroSelect');
        perroSelect.addEventListener('change', (e) => {
            this.onPerroChange(e.target.value);
        });
    }

    // Cargar clientes en el select
    cargarClientesEnSelect() {
        const select = document.getElementById('clienteSelect');
        const currentValue = select.value;
        
        select.innerHTML = '<option value="">Seleccionar cliente...</option>';
        select.innerHTML += '<option value="nuevo">➕ Nuevo cliente</option>';
        
        this.clientes.forEach(cliente => {
            const option = document.createElement('option');
            option.value = cliente.id;
            option.textContent = `${cliente.nombre} - ${cliente.telefono}`;
            option.dataset.cliente = JSON.stringify(cliente);
            select.appendChild(option);
        });

        if (currentValue) {
            select.value = currentValue;
        }
    }

    // Cuando cambia el cliente seleccionado
    onClienteChange(value) {
        const perroSelect = document.getElementById('perroSelect');
        const camposNuevo = document.getElementById('camposNuevoCliente');
        
        if (value === 'nuevo') {
            // Mostrar campos para nuevo cliente
            camposNuevo.style.display = 'block';
            perroSelect.disabled = true;
            perroSelect.innerHTML = '<option value="">Primero completa los datos del cliente</option>';
            
            // Hacer campos requeridos
            document.getElementById('clienteNombre').required = true;
            document.getElementById('telefono').required = true;
            document.getElementById('perroNombre').required = true;
            document.getElementById('raza').required = true;
            document.getElementById('tamanoPerroNuevo').required = true;
            document.getElementById('longitudPeloNuevo').required = true;
        } else if (value === '') {
            // No hay selección
            camposNuevo.style.display = 'none';
            perroSelect.disabled = true;
            perroSelect.innerHTML = '<option value="">Primero selecciona un cliente</option>';
        } else {
            // Cliente existente seleccionado
            camposNuevo.style.display = 'none';
            perroSelect.disabled = false;
            
            // Hacer campos no requeridos
            document.getElementById('clienteNombre').required = false;
            document.getElementById('telefono').required = false;
            document.getElementById('perroNombre').required = false;
            document.getElementById('raza').required = false;
            
            // Cargar perros del cliente
            const cliente = this.clientes.find(c => c.id == value);
            if (cliente) {
                perroSelect.innerHTML = '<option value="">Seleccionar perro...</option>';
                cliente.perros.forEach((perro, index) => {
                    const option = document.createElement('option');
                    option.value = index;
                    const pelolabel = perro.longitudPelo ? ` - ${this.nombreLongitudPelo(perro.longitudPelo)}` : '';
                    option.textContent = `${perro.nombre} (${perro.raza} - ${this.nombreTamano(perro.tamano || 'mediano')}${pelolabel})`;
                    option.dataset.perro = JSON.stringify(perro);
                    perroSelect.appendChild(option);
                });
                
                // Guardar datos del cliente en campos ocultos
                document.getElementById('clienteNombreHidden').value = cliente.nombre;
                document.getElementById('telefonoHidden').value = cliente.telefono;
            }
        }
    }

    // Cuando cambia el perro seleccionado
    onPerroChange(value) {
        const perroSelect = document.getElementById('perroSelect');
        const selectedOption = perroSelect.selectedOptions[0];
        
        if (selectedOption && selectedOption.dataset.perro) {
            const perro = JSON.parse(selectedOption.dataset.perro);
            document.getElementById('perroNombreHidden').value = perro.nombre;
            document.getElementById('razaHidden').value = perro.raza;
            document.getElementById('tamanoPerroHidden').value = perro.tamano || '';
            document.getElementById('longitudPeloHidden').value = perro.longitudPelo || '';
            
            // Recalcular precio total con los servicios seleccionados
            this.calcularPrecioTotal();
        }
    }

    // Cargar servicios en el select
    cargarServicios() {
        const select = document.getElementById('servicio');
        select.innerHTML = '';
        this.servicios.forEach(servicio => {
            const option = document.createElement('option');
            option.value = servicio.id;
            option.dataset.servicio = JSON.stringify(servicio);
            
            if (servicio.tipo === 'fijo') {
                option.textContent = `${servicio.nombre} - ${(servicio.precio || 0).toFixed(2)}€`;
            } else {
                option.textContent = `${servicio.nombre} (${servicio.preciosCombinaciones?.length || 0} combinaciones)`;
            }
            
            select.appendChild(option);
        });

        // Manejar cambio de servicios (múltiples)
        select.addEventListener('change', () => {
            this.calcularPrecioTotal();
        });
    }

    // Calcular precio total de servicios seleccionados
    calcularPrecioTotal() {
        const select = document.getElementById('servicio');
        const selectedOptions = Array.from(select.selectedOptions);
        const razaPerro = document.getElementById('razaHidden').value;
        const tamanoPerro = document.getElementById('tamanoPerroHidden').value;
        const longitudPelo = document.getElementById('longitudPeloHidden').value;
        
        console.log('🔍 Calculando precio total...');
        console.log('Datos del perro:', { razaPerro, tamanoPerro, longitudPelo });
        console.log('Servicios seleccionados:', selectedOptions.length);
        
        let precioTotal = 0;
        let todosTienenPrecio = true;

        selectedOptions.forEach(option => {
            if (option.dataset.servicio) {
                const servicio = JSON.parse(option.dataset.servicio);
                console.log('Procesando servicio:', servicio.nombre, 'Tipo:', servicio.tipo);
                
                if (servicio.tipo === 'fijo') {
                    precioTotal += servicio.precio;
                    console.log('  ✅ Precio fijo:', servicio.precio);
                } else {
                    // Precio por raza/tamaño/longitud
                    console.log('  🔎 Buscando combinación:', { razaPerro, tamanoPerro, longitudPelo });
                    console.log('  Combinaciones disponibles:', servicio.preciosCombinaciones);
                    
                    if (razaPerro && tamanoPerro && longitudPelo) {
                        const combinacion = servicio.preciosCombinaciones?.find(c => 
                            c.raza === razaPerro && c.tamano === tamanoPerro && c.longitudPelo === longitudPelo
                        );
                        if (combinacion) {
                            precioTotal += combinacion.precio;
                            console.log('  ✅ Combinación encontrada, precio:', combinacion.precio);
                        } else {
                            todosTienenPrecio = false;
                            console.log('  ❌ No se encontró combinación para:', { razaPerro, tamanoPerro, longitudPelo });
                        }
                    } else {
                        todosTienenPrecio = false;
                        console.log('  ⚠️ Datos del perro incompletos');
                    }
                }
            }
        });

        const precioInput = document.getElementById('precio');
        if (selectedOptions.length === 0) {
            precioInput.value = '';
            precioInput.placeholder = '0.00';
            console.log('💰 Precio final: Sin servicios seleccionados');
        } else if (todosTienenPrecio) {
            precioInput.value = precioTotal.toFixed(2);
            console.log('💰 Precio final:', precioTotal.toFixed(2) + '€');
        } else {
            precioInput.value = '';
            precioInput.placeholder = 'Selecciona un perro con datos completos';
            console.log('💰 Precio final: No se pudo calcular (datos incompletos o combinación no encontrada)');
        }
    }

    // Cuando cambia el servicio seleccionado (obsoleto, ahora usa calcularPrecioTotal)
    onServicioChange(servicio) {
        const precioInput = document.getElementById('precio');
        const razaPerro = document.getElementById('razaHidden').value;
        const tamanoPerro = document.getElementById('tamanoPerroHidden').value;
        const longitudPelo = document.getElementById('longitudPeloHidden').value;

        if (servicio.tipo === 'fijo') {
            // Precio fijo - no depende de raza ni tamaño
            precioInput.value = (servicio.precio || 0).toFixed(2);
        } else {
            // Precio por raza/tamaño/longitud - buscar combinación exacta
            if (razaPerro && tamanoPerro && longitudPelo) {
                const combinacion = servicio.preciosCombinaciones?.find(c => 
                    c.raza === razaPerro && c.tamano === tamanoPerro && c.longitudPelo === longitudPelo
                );
                if (combinacion) {
                    precioInput.value = (combinacion.precio || 0).toFixed(2);
                } else {
                    precioInput.value = '';
                    precioInput.placeholder = 'No hay precio para esta combinación';
                }
            } else {
                precioInput.value = '';
                precioInput.placeholder = 'Selecciona primero un perro con raza/tamaño/pelo definido';
            }
        }
    }

    // Actualizar precio según tamaño seleccionado
    actualizarPrecioPorTamano() {
        // Esta función ya no se usa porque el tamaño viene del perro
        // Se mantiene por compatibilidad
    }

    // Configurar formulario de servicios
    configurarFormularioServicio() {
        const form = document.getElementById('servicioForm');
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            this.guardarServicio();
        });
        
        // Cargar razas en el select de precios al inicio
        setTimeout(() => {
            this.cargarRazasEnSelectPrecio();
        }, 100);
    }

    // Configurar formulario de clientes
    configurarFormularioCliente() {
        const form = document.getElementById('clienteForm');
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            this.guardarCliente();
        });
        
        // Preview de foto
        document.getElementById('fotoPerro').addEventListener('change', (e) => {
            const file = e.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = (event) => {
                    document.getElementById('previewFoto').innerHTML = `
                        <img src="${event.target.result}" style="max-width: 150px; max-height: 150px; border-radius: 8px; object-fit: cover;">
                    `;
                };
                reader.readAsDataURL(file);
            }
        });
        
        // Inicializar array temporal de perros
        this.perrosTemp = [];
    }

    agregarPerroACliente() {
        const nombrePerro = document.getElementById('nombrePerro').value.trim();
        const razaPerro = document.getElementById('razaPerro').value;
        const tamanoPerro = document.getElementById('tamanoPerroCliente').value;
        const longitudPelo = document.getElementById('longitudPeloCliente').value;
        const edadPerro = document.getElementById('edadPerro').value;
        const fotoInput = document.getElementById('fotoPerro');
        const perroEditIndex = document.getElementById('perroEditIndex').value;

        if (!nombrePerro || !razaPerro || !tamanoPerro || !longitudPelo) {
            this.mostrarNotificacion('⚠️ Completa todos los datos del perro');
            return;
        }

        const perro = {
            nombre: nombrePerro,
            raza: razaPerro,
            tamano: tamanoPerro,
            longitudPelo,
            edad: edadPerro ? parseInt(edadPerro) : null,
            foto: null
        };

        // Procesar foto si existe
        if (fotoInput.files[0]) {
            const reader = new FileReader();
            reader.onload = (event) => {
                perro.foto = event.target.result;
                this.finalizarAgregarPerro(perro, perroEditIndex);
            };
            reader.readAsDataURL(fotoInput.files[0]);
        } else {
            this.finalizarAgregarPerro(perro, perroEditIndex);
        }
    }

    finalizarAgregarPerro(perro, editIndex) {
        console.log('🐕 Finalizando agregar perro...');
        console.log('Perro:', perro);
        console.log('Edit index:', editIndex);
        console.log('perrosTemp antes:', [...this.perrosTemp]);
        
        if (editIndex !== '') {
            // Editar perro existente
            this.perrosTemp[parseInt(editIndex)] = perro;
            this.mostrarNotificacion('✅ Perro actualizado');
        } else {
            // Agregar nuevo perro
            this.perrosTemp.push(perro);
            this.mostrarNotificacion('✅ Perro agregado a la lista');
        }

        console.log('perrosTemp después:', [...this.perrosTemp]);

        // Limpiar formulario de perro y ocultarlo
        this.ocultarFormPerro();
        this.mostrarPerrosTemp();
    }

    mostrarPerrosTemp() {
        const lista = document.getElementById('listaPerrosCliente');
        if (this.perrosTemp.length === 0) {
            lista.innerHTML = '';
            return;
        }

        lista.innerHTML = `
            <div style="background: #f5f5f5; padding: 15px; border-radius: 8px;">
                <h4 style="margin: 0 0 10px 0; color: #666;">Perros agregados (${this.perrosTemp.length})</h4>
                ${this.perrosTemp.map((perro, index) => `
                    <div style="display: flex; align-items: center; gap: 10px; padding: 10px; background: white; border-radius: 5px; margin-bottom: 8px;">
                        ${perro.foto ? `<img src="${perro.foto}" style="width: 50px; height: 50px; border-radius: 50%; object-fit: cover;">` : '🐕'}
                        <div style="flex: 1;">
                            <strong>${perro.nombre}</strong> - ${perro.raza}
                            <br><small style="color: #666;">${this.nombreTamano(perro.tamano)} | ${this.nombreLongitudPelo(perro.longitudPelo)}${perro.edad ? ` | ${perro.edad} años` : ''}</small>
                        </div>
                        <button type="button" class="btn btn-small btn-edit" onclick="app.editarPerroTemp(${index})">✏️</button>
                        <button type="button" class="btn btn-small btn-danger" onclick="app.eliminarPerroTemp(${index})">🗑️</button>
                    </div>
                `).join('')}
            </div>
        `;
    }

    editarPerroTemp(index) {
        const perro = this.perrosTemp[index];
        document.getElementById('nombrePerro').value = perro.nombre;
        document.getElementById('razaPerro').value = perro.raza;
        document.getElementById('tamanoPerroCliente').value = perro.tamano;
        document.getElementById('longitudPeloCliente').value = perro.longitudPelo;
        document.getElementById('edadPerro').value = perro.edad || '';
        document.getElementById('perroEditIndex').value = index;
        document.getElementById('btnTextPerro').textContent = '✓ Actualizar Perro';
        
        if (perro.foto) {
            document.getElementById('previewFoto').innerHTML = `
                <img src="${perro.foto}" style="max-width: 150px; max-height: 150px; border-radius: 8px; object-fit: cover;">
            `;
        }

        // Mostrar formulario de perro
        document.getElementById('formPerroContainer').style.display = 'block';
        document.getElementById('btnMostrarFormPerro').style.display = 'none';

        // Scroll al formulario
        document.getElementById('formPerroContainer').scrollIntoView({ behavior: 'smooth' });
    }

    eliminarPerroTemp(index) {
        this.perrosTemp.splice(index, 1);
        this.mostrarPerrosTemp();
        this.mostrarNotificacion('🗑️ Perro eliminado de la lista');
    }

    // Guardar cliente
    guardarCliente() {
        const editId = document.getElementById('clienteEditId').value;
        const nombre = document.getElementById('nombreCliente').value.trim();
        const telefono = document.getElementById('telefonoCliente').value.trim();

        console.log('💾 Guardando cliente...');
        console.log('Edit ID:', editId);
        console.log('Nombre:', nombre);
        console.log('Teléfono:', telefono);
        console.log('Perros en perrosTemp:', this.perrosTemp);

        if (!nombre || !telefono) {
            this.mostrarNotificacion('⚠️ Completa nombre y teléfono del cliente');
            return;
        }

        // Permitir guardar sin perros, se pueden agregar después
        if (this.perrosTemp.length === 0) {
            if (!confirm('¿Guardar cliente sin perros? Podrás agregarlos después editando el cliente.')) {
                return;
            }
        }

        if (editId) {
            // Editar cliente existente
            const cliente = this.clientes.find(c => c.id == editId);
            if (cliente) {
                console.log('Cliente antes de actualizar:', JSON.parse(JSON.stringify(cliente)));
                cliente.nombre = nombre;
                cliente.telefono = telefono;
                cliente.perros = [...this.perrosTemp];
                console.log('Cliente después de actualizar:', JSON.parse(JSON.stringify(cliente)));
                this.mostrarNotificacion('✅ Cliente actualizado');
            }
        } else {
            // Crear nuevo cliente
            const nuevoCliente = {
                id: Date.now(),
                nombre,
                telefono,
                perros: [...this.perrosTemp],
                ultimaVisita: new Date().toISOString().split('T')[0]
            };
            console.log('Nuevo cliente a crear:', nuevoCliente);
            this.clientes.push(nuevoCliente);
            this.mostrarNotificacion('✅ Cliente agregado correctamente');
        }

        this.guardarDatos('clientes', this.clientes);
        console.log('Clientes guardados:', this.clientes);
        
        // Auto-backup en segundo plano
        setTimeout(() => {
            this.sincronizarConDrive(true).catch(err => console.log('Auto-backup Drive:', err));
        }, 500);
        
        document.getElementById('clienteForm').reset();
        document.getElementById('clienteEditId').value = '';
        document.getElementById('btnGuardarCliente').textContent = 'Guardar Cliente';
        document.getElementById('btnCancelarCliente').style.display = 'none';
        this.perrosTemp = [];
        this.mostrarPerrosTemp();
        this.ocultarFormPerro();
        
        this.mostrarClientes();
        this.cargarClientesEnSelect(); // Actualizar select de citas
    }

    toggleFormPerro() {
        const formContainer = document.getElementById('formPerroContainer');
        const btnMostrar = document.getElementById('btnMostrarFormPerro');
        
        if (formContainer.style.display === 'none') {
            formContainer.style.display = 'block';
            btnMostrar.style.display = 'none';
            // Verificar que hay datos de cliente
            const nombre = document.getElementById('nombreCliente').value.trim();
            const telefono = document.getElementById('telefonoCliente').value.trim();
            if (!nombre || !telefono) {
                this.mostrarNotificacion('⚠️ Completa primero los datos del cliente (nombre y teléfono)');
                formContainer.style.display = 'none';
                btnMostrar.style.display = 'block';
            }
        } else {
            this.ocultarFormPerro();
        }
    }

    ocultarFormPerro() {
        document.getElementById('formPerroContainer').style.display = 'none';
        document.getElementById('btnMostrarFormPerro').style.display = 'block';
        this.cancelarPerro();
    }

    cancelarPerro() {
        document.getElementById('nombrePerro').value = '';
        document.getElementById('razaPerro').value = '';
        document.getElementById('tamanoPerroCliente').value = '';
        document.getElementById('longitudPeloCliente').value = '';
        document.getElementById('edadPerro').value = '';
        document.getElementById('fotoPerro').value = '';
        document.getElementById('previewFoto').innerHTML = '';
        document.getElementById('perroEditIndex').value = '';
        document.getElementById('btnTextPerro').textContent = '✓ Guardar Perro';
    }

    // Editar cliente desde la lista
    editarClienteDirecto(id) {
        const cliente = this.clientes.find(c => c.id === id);
        if (!cliente) return;

        // Cargar datos en el formulario
        document.getElementById('nombreCliente').value = cliente.nombre;
        document.getElementById('telefonoCliente').value = cliente.telefono;
        
        // Cargar todos los perros en el array temporal
        this.perrosTemp = cliente.perros.map(perro => ({...perro}));
        this.mostrarPerrosTemp();
        
        document.getElementById('clienteEditId').value = cliente.id;
        document.getElementById('btnGuardarCliente').textContent = 'Actualizar Cliente';
        document.getElementById('btnCancelarCliente').style.display = 'inline-block';

        // Scroll al formulario
        document.getElementById('clienteForm').scrollIntoView({ behavior: 'smooth' });
    }

    cancelarEdicionCliente() {
        document.getElementById('clienteForm').reset();
        document.getElementById('clienteEditId').value = '';
        document.getElementById('perroEditIndex').value = '';
        document.getElementById('btnGuardarCliente').textContent = 'Guardar Cliente';
        document.getElementById('btnCancelarCliente').style.display = 'none';
        document.getElementById('previewFoto').innerHTML = '';
        this.perrosTemp = [];
        this.mostrarPerrosTemp();
        this.ocultarFormPerro();
    }

    // Gestión de Razas
    configurarFormularioRaza() {
        const form = document.getElementById('razaForm');
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            this.guardarRaza();
        });
    }

    guardarRaza() {
        const editId = document.getElementById('razaEditId').value;
        const nombre = document.getElementById('nombreRaza').value.trim();

        if (!nombre) {
            this.mostrarNotificacion('⚠️ Ingresa un nombre de raza');
            return;
        }

        if (editId) {
            // Editar raza existente
            const raza = this.razas.find(r => r.id == editId);
            if (raza) {
                raza.nombre = nombre;
                this.mostrarNotificacion('✅ Raza actualizada');
            }
        } else {
            // Verificar si la raza ya existe
            const razaExistente = this.razas.find(r => r.nombre.toLowerCase() === nombre.toLowerCase());
            if (razaExistente) {
                this.mostrarNotificacion('⚠️ Esta raza ya existe');
                return;
            }

            // Agregar nueva raza
            this.razas.push({
                id: Date.now(),
                nombre
            });
            this.mostrarNotificacion('✅ Raza agregada');
        }

        this.guardarDatos('razas', this.razas);
        document.getElementById('razaForm').reset();
        document.getElementById('razaEditId').value = '';
        document.getElementById('btnGuardarRaza').textContent = 'Agregar Raza';
        document.getElementById('btnCancelarRaza').style.display = 'none';
        
        this.mostrarRazas();
        this.cargarRazasEnSelects(); // Actualizar los selects de razas
    }

    // Cargar razas en los selects
    cargarRazasEnSelects() {
        const selectRazaCliente = document.getElementById('razaPerro');
        const selectRazaCita = document.getElementById('raza');
        
        // Ordenar razas alfabéticamente
        const razasOrdenadas = [...this.razas].sort((a, b) => a.nombre.localeCompare(b.nombre));
        
        const opciones = '<option value="">Seleccionar raza...</option>' +
            razasOrdenadas.map(raza => 
                `<option value="${raza.nombre}">${raza.nombre}</option>`
            ).join('');
        
        if (selectRazaCliente) selectRazaCliente.innerHTML = opciones;
        if (selectRazaCita) selectRazaCita.innerHTML = opciones;
    }

    cargarTamanosEnSelects() {
        const tamanos = [
            { valor: 'mini', texto: 'Mini (< 5kg)' },
            { valor: 'pequeno', texto: 'Pequeño (5-10kg)' },
            { valor: 'mediano', texto: 'Mediano (10-25kg)' },
            { valor: 'grande', texto: 'Grande (25-45kg)' },
            { valor: 'gigante', texto: 'Gigante (> 45kg)' }
        ];
        
        const opciones = '<option value="">Seleccionar tamaño...</option>' +
            tamanos.map(t => 
                `<option value="${t.valor}">${t.texto}</option>`
            ).join('');
        
        // Actualizar todos los selects de tamaño
        const selectTamanoNuevo = document.getElementById('tamanoPerroNuevo');
        const selectTamanoCliente = document.getElementById('tamanoPerroCliente');
        
        if (selectTamanoNuevo) selectTamanoNuevo.innerHTML = opciones;
        if (selectTamanoCliente) selectTamanoCliente.innerHTML = opciones;
    }



    mostrarRazas() {
        const lista = document.getElementById('listaRazas');
        
        console.log('mostrarRazas() llamado');
        console.log('Elemento listaRazas:', lista);
        console.log('Número de razas:', this.razas.length);
        console.log('Razas:', this.razas);
        
        if (this.razas.length === 0) {
            lista.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">🐕</div>
                    <div class="empty-state-text">No hay razas registradas</div>
                </div>
            `;
            return;
        }

        // Ordenar alfabéticamente
        const razasOrdenadas = [...this.razas].sort((a, b) => a.nombre.localeCompare(b.nombre));

        lista.innerHTML = razasOrdenadas.map(raza => `
            <div class="item-card">
                <div class="item-header">
                    <div>
                        <div class="item-title">🐕 ${raza.nombre}</div>
                    </div>
                    <div class="item-actions">
                        <button class="btn btn-small btn-edit" onclick="app.editarRaza(${raza.id})">
                            ✏️ Editar
                        </button>
                        <button class="btn btn-small btn-danger" onclick="app.eliminarRaza(${raza.id})">
                            🗑️ Eliminar
                        </button>
                    </div>
                </div>
            </div>
        `).join('');
        
        console.log('Razas mostradas en HTML');
    }

    editarRaza(id) {
        const raza = this.razas.find(r => r.id === id);
        if (!raza) return;

        document.getElementById('nombreRaza').value = raza.nombre;
        document.getElementById('razaEditId').value = raza.id;
        document.getElementById('btnGuardarRaza').textContent = 'Actualizar Raza';
        document.getElementById('btnCancelarRaza').style.display = 'inline-block';

        // Scroll al formulario
        document.getElementById('razaForm').scrollIntoView({ behavior: 'smooth' });
    }

    cancelarEdicionRaza() {
        document.getElementById('razaForm').reset();
        document.getElementById('razaEditId').value = '';
        document.getElementById('btnGuardarRaza').textContent = 'Agregar Raza';
        document.getElementById('btnCancelarRaza').style.display = 'none';
    }

    eliminarRaza(id) {
        if (confirm('¿Estás seguro de que quieres eliminar esta raza?')) {
            this.razas = this.razas.filter(r => r.id !== id);
            this.guardarDatos('razas', this.razas);
            this.mostrarRazas();
            this.cargarRazasEnSelects();
            this.mostrarNotificacion('🗑️ Raza eliminada');
        }
    }

    // Configuración Google Calendar (OBSOLETO - ahora se maneja con OAuth)
    cargarConfigGoogle() {
        // Función obsoleta - ya no se usa
        // Los elementos googleClientId y googleApiKey ya no existen en el HTML
    }

    guardarConfigGoogle() {
        // Función obsoleta - ya no se usa
        // La configuración ahora se maneja directamente en oauth-manager-v2.js
    }

    // Sincronización con Google Calendar

    verInstruccionesGoogle() {
        window.open('GOOGLE_CALENDAR_SETUP.md', '_blank');
        this.mostrarNotificacion('📖 Se abrió la guía de configuración');
    }

    testearConexionGoogle() {
        const clientId = this.googleConfig.clientId;
        const apiKey = this.googleConfig.apiKey;

        if (!clientId || !apiKey) {
            this.mostrarEstadoGoogle('error', '❌ Configura primero Client ID y API Key');
            return;
        }

        this.mostrarEstadoGoogle('info', '🔄 Testeando conexión...');
        
        // Simular testeo (en producción, aquí iría la llamada real a la API)
        setTimeout(() => {
            this.mostrarEstadoGoogle('success', '✅ Configuración válida. Haz clic en "Sincronizar Google Calendar" en la pestaña Agenda para autorizar.');
        }, 1500);
    }

    mostrarEstadoGoogle(tipo, mensaje) {
        const estado = document.getElementById('estadoGoogle');
        estado.style.display = 'block';
        estado.className = 'alert-' + tipo;
        estado.textContent = mensaje;
    }

    // ==================== GOOGLE DRIVE SYNC ====================
    
    guardarConfigDrive() {
        this.driveConfig = {
            clientId: document.getElementById('gdriveClientId').value.trim(),
            apiKey: document.getElementById('gdriveApiKey').value.trim(),
            autoSync: document.getElementById('autoSyncDrive').checked
        };

        this.guardarDatos('driveConfig', this.driveConfig);
        this.mostrarNotificacion('✅ Configuración de Google Drive guardada');
        this.actualizarEstadoDrive();
    }

    cargarConfigDrive() {
        // Función obsoleta - la configuración ahora se maneja en oauth-manager-v2.js
    }

    toggleAutoSync(enabled) {
        this.driveConfig.autoSync = enabled;
        this.guardarDatos('driveConfig', this.driveConfig);
        this.mostrarNotificacion(enabled ? '✅ Sincronización automática activada' : '⚠️ Sincronización automática desactivada');
    }

    async conectarDrive() {
        if (!this.driveConfig.clientId || !this.driveConfig.apiKey) {
            this.mostrarNotificacion('⚠️ Configura primero el Client ID y API Key');
            return;
        }

        try {
            // Cargar Google API
            await this.cargarGoogleAPI();
            
            // Inicializar el cliente
            await gapi.client.init({
                apiKey: this.driveConfig.apiKey,
                clientId: this.driveConfig.clientId,
                discoveryDocs: ['https://www.googleapis.com/discovery/v1/apis/drive/v3/rest'],
                scope: 'https://www.googleapis.com/auth/drive.file'
            });

            // Autenticar
            const authInstance = gapi.auth2.getAuthInstance();
            if (!authInstance.isSignedIn.get()) {
                await authInstance.signIn();
            }

            this.driveToken = authInstance.currentUser.get().getAuthResponse().access_token;
            this.mostrarNotificacion('✅ Conectado a Google Drive');
            this.actualizarEstadoDrive('Conectado ✓');
            
            // Sincronizar automáticamente después de conectar
            await this.sincronizarConDrive();
            
        } catch (error) {
            console.error('Error conectando con Drive:', error);
            this.mostrarNotificacion('❌ Error al conectar con Google Drive: ' + error.message);
            this.actualizarEstadoDrive('Error de conexión');
        }
    }

    cargarGoogleAPI() {
        return new Promise((resolve, reject) => {
            if (typeof gapi !== 'undefined') {
                resolve();
                return;
            }

            const script = document.createElement('script');
            script.src = 'https://apis.google.com/js/api.js';
            script.onload = () => {
                gapi.load('client:auth2', resolve);
            };
            script.onerror = reject;
            document.head.appendChild(script);
        });
    }

    async sincronizarConDrive() {
        if (!this.driveToken) {
            await this.conectarDrive();
            return;
        }

        try {
            const datosLocales = this.obtenerTodosDatos();
            // Usar el timestamp de última modificación local
            const lastLocalUpdate = localStorage.getItem('lastLocalUpdate');
            const timestampLocal = lastLocalUpdate ? parseInt(lastLocalUpdate) : Date.now();

            // Buscar o crear archivo en Drive
            if (!this.driveFileId) {
                await this.buscarOCrearArchivoDrive();
            }

            // Obtener datos de Drive
            const datosDrive = await this.obtenerDatosDrive();

            if (datosDrive) {
                const timestampDrive = datosDrive.timestamp || 0;
                
                console.log('Comparando versiones:');
                console.log('Local:', new Date(timestampLocal).toLocaleString());
                console.log('Drive:', new Date(timestampDrive).toLocaleString());

                if (timestampLocal > timestampDrive) {
                    // Local es más reciente, subir a Drive
                    await this.subirDatosADrive(datosLocales, timestampLocal);
                    this.mostrarNotificacion('☁️ Datos locales subidos a Drive');
                } else if (timestampDrive > timestampLocal) {
                    // Drive es más reciente, descargar de Drive
                    await this.descargarDatosDeDrive(datosDrive);
                    this.mostrarNotificacion('⬇️ Datos descargados de Drive');
                } else {
                    this.mostrarNotificacion('✅ Datos sincronizados (misma versión)');
                }
            } else {
                // No hay datos en Drive, subir los locales
                await this.subirDatosADrive(datosLocales, timestampLocal);
                this.mostrarNotificacion('☁️ Copia inicial subida a Drive');
            }

            this.lastSync = new Date().toISOString();
            this.guardarDatos('lastSync', this.lastSync);
            this.actualizarInfoSync();

        } catch (error) {
            console.error('Error en sincronización:', error);
            this.mostrarNotificacion('❌ Error al sincronizar: ' + error.message);
        }
    }

    async sincronizarAlInicio() {
        // Detectar si estamos en Capacitor (app nativa)
        const esCapacitor = typeof window.Capacitor !== 'undefined' && window.Capacitor.isNativePlatform();
        
        if (esCapacitor) {
            console.log('App nativa detectada - Drive sync no disponible en APK');
            return;
        }
        
        // Solo sincronizar si Drive está configurado
        if (!this.driveConfig.clientId || !this.driveConfig.apiKey) {
            console.log('Drive no configurado, omitiendo sincronización automática');
            return;
        }
        
        // Verificar que gapi esté disponible
        if (typeof gapi === 'undefined' || typeof google === 'undefined') {
            console.log('Google API no disponible, omitiendo sincronización');
            return;
        }

        try {
            // Cargar el API de Google Drive
            await this.cargarAPIGoogle();
            
            // Intentar autenticación silenciosa (sin popup)
            const tokenClient = google.accounts.oauth2.initTokenClient({
                client_id: this.driveConfig.clientId,
                scope: 'https://www.googleapis.com/auth/drive.file',
                callback: async (response) => {
                    if (response.error) {
                        console.log('No hay sesión activa de Drive, sincronización omitida');
                        return;
                    }
                    
                    this.driveToken = response;
                    
                    // Buscar archivo en Drive
                    if (!this.driveFileId) {
                        await this.buscarOCrearArchivoDrive();
                    }
                    
                    if (!this.driveFileId) {
                        console.log('No hay archivo de backup en Drive');
                        return;
                    }
                    
                    // Obtener datos de Drive
                    const datosDrive = await this.obtenerDatosDrive();
                    
                    if (!datosDrive) {
                        console.log('No hay datos en Drive para comparar');
                        return;
                    }
                    
                    // Obtener timestamp local
                    const lastLocalUpdate = localStorage.getItem('lastLocalUpdate');
                    const timestampLocal = lastLocalUpdate ? parseInt(lastLocalUpdate) : 0;
                    const timestampDrive = datosDrive.timestamp || 0;
                    
                    console.log('🔄 Comprobando sincronización al inicio:');
                    console.log('  Última modificación local:', timestampLocal ? new Date(timestampLocal).toLocaleString() : 'Nunca');
                    console.log('  Última modificación Drive:', timestampDrive ? new Date(timestampDrive).toLocaleString() : 'Nunca');
                    
                    // Si Drive tiene datos más recientes, sincronizar automáticamente
                    if (timestampDrive > timestampLocal) {
                        console.log('📥 Drive tiene datos más recientes, sincronizando...');
                        await this.descargarDatosDeDrive(datosDrive);
                        this.mostrarNotificacion('📥 Datos actualizados desde Drive', 'success');
                        this.lastSync = new Date().toISOString();
                        this.guardarDatos('lastSync', this.lastSync);
                        this.actualizarInfoSync();
                    } else if (timestampLocal > timestampDrive) {
                        console.log('📤 Datos locales más recientes que Drive');
                    } else {
                        console.log('✅ Datos sincronizados (misma versión)');
                    }
                }
            });
            
            // Intentar obtener token de forma silenciosa
            tokenClient.requestAccessToken({ prompt: '' });
            
        } catch (error) {
            console.log('Error en sincronización automática al inicio:', error.message);
            // No mostrar notificación de error al usuario para no interrumpir el inicio
        }
    }

    async buscarOCrearArchivoDrive() {
        const fileName = 'peluqueria_canina_backup.json';
        
        try {
            // Buscar archivo existente
            const response = await gapi.client.drive.files.list({
                q: `name='${fileName}' and trashed=false`,
                fields: 'files(id, name)',
                spaces: 'drive'
            });

            if (response.result.files && response.result.files.length > 0) {
                this.driveFileId = response.result.files[0].id;
                console.log('Archivo encontrado:', this.driveFileId);
            } else {
                // Crear nuevo archivo
                const fileMetadata = {
                    name: fileName,
                    mimeType: 'application/json'
                };
                
                const createResponse = await gapi.client.drive.files.create({
                    resource: fileMetadata,
                    fields: 'id'
                });
                
                this.driveFileId = createResponse.result.id;
                console.log('Archivo creado:', this.driveFileId);
            }
            
            this.guardarDatos('driveFileId', this.driveFileId);
            
        } catch (error) {
            console.error('Error buscando/creando archivo:', error);
            throw error;
        }
    }

    async obtenerDatosDrive() {
        if (!this.driveFileId) return null;

        try {
            const response = await gapi.client.drive.files.get({
                fileId: this.driveFileId,
                alt: 'media'
            });

            return response.result;
        } catch (error) {
            if (error.status === 404) {
                // Archivo no existe, retornar null
                this.driveFileId = null;
                this.guardarDatos('driveFileId', null);
                return null;
            }
            console.error('Error obteniendo datos de Drive:', error);
            throw error;
        }
    }

    async subirDatosADrive(datos, timestamp) {
        const contenido = {
            timestamp,
            datos,
            version: '1.0'
        };

        const boundary = '-------314159265358979323846';
        const delimiter = "\r\n--" + boundary + "\r\n";
        const close_delim = "\r\n--" + boundary + "--";

        const metadata = {
            name: 'peluqueria_canina_backup.json',
            mimeType: 'application/json'
        };

        const multipartRequestBody =
            delimiter +
            'Content-Type: application/json\r\n\r\n' +
            JSON.stringify(metadata) +
            delimiter +
            'Content-Type: application/json\r\n\r\n' +
            JSON.stringify(contenido) +
            close_delim;

        const method = this.driveFileId ? 'PATCH' : 'POST';
        const path = this.driveFileId 
            ? `/upload/drive/v3/files/${this.driveFileId}`
            : '/upload/drive/v3/files';

        const response = await gapi.client.request({
            path: path,
            method: method,
            params: { uploadType: 'multipart' },
            headers: {
                'Content-Type': 'multipart/related; boundary="' + boundary + '"'
            },
            body: multipartRequestBody
        });

        if (!this.driveFileId) {
            this.driveFileId = response.result.id;
            this.guardarDatos('driveFileId', this.driveFileId);
        }

        console.log('Datos subidos a Drive correctamente');
    }

    async descargarDatosDeDrive(datosDrive) {
        if (!datosDrive || !datosDrive.datos) {
            console.warn('No hay datos válidos en Drive');
            return;
        }

        // Restaurar datos
        const { citas, clientes, servicios, razas } = datosDrive.datos;
        
        if (citas) {
            this.citas = citas;
            this.guardarDatos('citas', this.citas);
        }
        
        if (clientes) {
            this.clientes = clientes;
            this.guardarDatos('clientes', this.clientes);
        }
        
        if (servicios) {
            this.servicios = servicios;
            this.guardarDatos('servicios', this.servicios);
        }
        
        if (razas) {
            this.razas = razas;
            this.guardarDatos('razas', this.razas);
        }
        
        // Actualizar el timestamp local con el de Drive
        if (datosDrive.timestamp) {
            localStorage.setItem('lastLocalUpdate', datosDrive.timestamp.toString());
        }

        // Actualizar vistas
        this.mostrarAgenda();
        this.mostrarClientes();
        this.mostrarServicios();
        this.mostrarRazas();
        this.cargarClientesEnSelect();
        this.cargarServicios();
        this.actualizarEstadisticas();

        console.log('Datos restaurados desde Drive');
    }

    obtenerTodosDatos() {
        return {
            citas: this.citas,
            clientes: this.clientes,
            servicios: this.servicios,
            razas: this.razas
        };
    }

    sincronizarManual() {
        // Usar el nuevo sistema OAuth
        if (typeof oauthIntegration !== 'undefined' && oauthIntegration) {
            const estado = oauthIntegration.obtenerEstado();
            
            if (!estado.autenticado) {
                const conectar = confirm('🔐 Necesitas conectarte con Google primero.\n\n¿Quieres conectarte ahora?');
                if (conectar && typeof oauthManager !== 'undefined') {
                    oauthManager.iniciarLoginGoogle();
                }
                return;
            }
            
            // Realizar sincronización completa
            this.sincronizarConDrive();
        } else {
            this.mostrarNotificacion('⚠️ Sistema OAuth no disponible. Recarga la página.');
        }
    }

    desconectarDrive() {
        this.driveToken = null;
        this.driveFileId = null;
        this.guardarDatos('driveFileId', null);
        this.actualizarInfoSync();
        this.mostrarNotificacion('🚪 Desconectado de Google Drive');
    }

    actualizarEstadoDrive(mensaje = null) {
        const estadoDiv = document.getElementById('estadoDrive');
        if (!estadoDiv) return;

        if (mensaje) {
            estadoDiv.innerHTML = `<strong>Estado:</strong> ${mensaje}`;
        } else {
            const configurado = this.driveConfig.clientId && this.driveConfig.apiKey;
            const conectado = this.driveToken !== null;
            
            if (conectado) {
                estadoDiv.innerHTML = '<strong>Estado:</strong> <span style="color: green;">✓ Conectado</span>';
                estadoDiv.style.background = '#d4edda';
            } else if (configurado) {
                estadoDiv.innerHTML = '<strong>Estado:</strong> Configurado (no conectado)';
                estadoDiv.style.background = '#fff3cd';
            } else {
                estadoDiv.innerHTML = '<strong>Estado:</strong> No configurado';
                estadoDiv.style.background = '#f8d7da';
            }
        }
    }

    actualizarInfoSync() {
        const infoDiv = document.getElementById('infoSync');
        if (!infoDiv) return;

        if (this.lastSync) {
            const fecha = new Date(this.lastSync);
            infoDiv.innerHTML = `
                <p style="margin: 5px 0;"><strong>Fecha:</strong> ${this.formatearFecha(fecha.toISOString().split('T')[0])}</p>
                <p style="margin: 5px 0;"><strong>Hora:</strong> ${fecha.toLocaleTimeString()}</p>
                <p style="margin: 5px 0; color: green;">✓ Sincronizado correctamente</p>
            `;
        } else {
            infoDiv.innerHTML = '<p style="margin: 5px 0; color: #666;">Nunca sincronizado</p>';
        }
    }

    // Agregar nueva cita
    agregarCita() {
        const clienteSelectValue = document.getElementById('clienteSelect').value;
        let clienteNombre, telefono, perroNombre, raza, tamanoPerro, longitudPelo;

        if (clienteSelectValue === 'nuevo') {
            // Datos del nuevo cliente
            clienteNombre = document.getElementById('clienteNombre').value;
            telefono = document.getElementById('telefono').value;
            perroNombre = document.getElementById('perroNombre').value;
            raza = document.getElementById('raza').value;
            tamanoPerro = document.getElementById('tamanoPerroNuevo').value;
            longitudPelo = document.getElementById('longitudPeloNuevo').value;
        } else {
            // Datos del cliente existente
            clienteNombre = document.getElementById('clienteNombreHidden').value;
            telefono = document.getElementById('telefonoHidden').value;
            perroNombre = document.getElementById('perroNombreHidden').value;
            raza = document.getElementById('razaHidden').value;
            tamanoPerro = document.getElementById('tamanoPerroHidden').value;
            longitudPelo = document.getElementById('longitudPeloHidden').value;
        }

        // Obtener servicios seleccionados
        const servicioSelect = document.getElementById('servicio');
        const serviciosSeleccionados = Array.from(servicioSelect.selectedOptions).map(option => {
            const servicio = JSON.parse(option.dataset.servicio);
            return servicio.nombre;
        });

        if (serviciosSeleccionados.length === 0) {
            this.mostrarNotificacion('⚠️ Debes seleccionar al menos un servicio');
            return;
        }

        const cita = {
            id: Date.now(),
            clienteNombre,
            telefono,
            perroNombre,
            raza,
            tamanoPerro,
            longitudPelo,
            fecha: document.getElementById('fecha').value,
            hora: document.getElementById('hora').value,
            servicio: serviciosSeleccionados.join(', '),
            servicios: serviciosSeleccionados,
            precio: parseFloat(document.getElementById('precio').value),
            notas: document.getElementById('notas').value,
            completada: false,
            fechaCreacion: new Date().toISOString()
        };

        this.citas.push(cita);
        this.guardarDatos('citas', this.citas);

        // Agregar o actualizar cliente
        this.actualizarCliente(cita);

        // Limpiar formulario
        document.getElementById('citaForm').reset();
        document.getElementById('camposNuevoCliente').style.display = 'none';
        document.getElementById('perroSelect').disabled = true;
        document.getElementById('perroSelect').innerHTML = '<option value="">Primero selecciona un cliente</option>';

        // Recargar clientes en el select
        this.cargarClientesEnSelect();

        // Mostrar confirmación
        this.mostrarNotificacion('✅ Cita guardada correctamente');

        // Auto-sincronizar en segundo plano (crear evento en Calendar + backup en Drive)
        setTimeout(() => {
            this.sincronizarCambiosAutomaticos('crear', cita).catch(err => console.log('Auto-sync:', err));
        }, 500);

        // Actualizar vistas
        this.mostrarAgenda();
        this.mostrarClientes();
        this.actualizarEstadisticas();
    }

    // Actualizar citas futuras del mismo perro cuando cambian sus datos
    actualizarCitasDelMismoPerro(citaEditada) {
        const hoy = new Date().toISOString().split('T')[0];
        let citasActualizadas = 0;
        
        // Buscar todas las citas futuras del mismo perro y cliente
        this.citas.forEach(cita => {
            // No actualizar la cita que ya estamos editando
            if (cita.id === citaEditada.id) return;
            
            // Solo citas futuras del mismo perro y cliente
            if (cita.fecha >= hoy && 
                cita.telefono === citaEditada.telefono && 
                cita.perroNombre === citaEditada.perroNombre) {
                
                // Actualizar datos del perro
                cita.raza = citaEditada.raza;
                cita.tamanoPerro = citaEditada.tamanoPerro;
                cita.longitudPelo = citaEditada.longitudPelo;
                
                // Si tiene evento en Calendar, actualizarlo
                if (cita.googleEventId) {
                    setTimeout(() => {
                        this.sincronizarCambiosAutomaticos('editar', cita).catch(err => 
                            console.log('Error actualizando cita relacionada:', err)
                        );
                    }, 1000 + citasActualizadas * 500); // Espaciar las actualizaciones
                }
                
                citasActualizadas++;
            }
        });
        
        if (citasActualizadas > 0) {
            this.guardarDatos('citas', this.citas);
            console.log(`✅ ${citasActualizadas} cita(s) futura(s) del mismo perro actualizadas`);
        }
    }

    // Actualizar información del cliente
    actualizarCliente(cita) {
        const clienteExistente = this.clientes.find(c => 
            c.telefono === cita.telefono
        );

        if (clienteExistente) {
            // Actualizar información del cliente
            clienteExistente.nombre = cita.clienteNombre;
            
            // Buscar si el perro ya existe
            const perroExistente = clienteExistente.perros.find(p => p.nombre === cita.perroNombre);
            
            if (perroExistente) {
                // Actualizar datos del perro existente
                perroExistente.raza = cita.raza;
                perroExistente.tamano = cita.tamanoPerro;
                perroExistente.longitudPelo = cita.longitudPelo;
            } else {
                // Agregar nuevo perro
                clienteExistente.perros.push({
                    nombre: cita.perroNombre,
                    raza: cita.raza,
                    tamano: cita.tamanoPerro,
                    longitudPelo: cita.longitudPelo
                });
            }
            
            clienteExistente.ultimaVisita = cita.fecha;
        } else {
            // Crear nuevo cliente
            this.clientes.push({
                id: Date.now(),
                nombre: cita.clienteNombre,
                telefono: cita.telefono,
                perros: [{
                    nombre: cita.perroNombre,
                    raza: cita.raza,
                    tamano: cita.tamanoPerro,
                    longitudPelo: cita.longitudPelo
                }],
                ultimaVisita: cita.fecha
            });
        }

        this.guardarDatos('clientes', this.clientes);
    }

    // Mostrar agenda
    mostrarAgenda(filtro = 'todas') {
        const listaAgenda = document.getElementById('listaAgenda');
        let citasFiltradas = [...this.citas];

        // Aplicar filtro
        const hoy = new Date().toISOString().split('T')[0];
        
        if (filtro === 'hoy') {
            citasFiltradas = citasFiltradas.filter(c => c.fecha === hoy);
        } else if (filtro === 'semana') {
            const hoyDate = new Date();
            const unaSemana = new Date(hoyDate.getTime() + 7 * 24 * 60 * 60 * 1000);
            citasFiltradas = citasFiltradas.filter(c => {
                const fechaCita = new Date(c.fecha);
                return fechaCita >= hoyDate && fechaCita <= unaSemana;
            });
        }

        // Ordenar por fecha y hora
        citasFiltradas.sort((a, b) => {
            const fechaA = new Date(`${a.fecha}T${a.hora}`);
            const fechaB = new Date(`${b.fecha}T${b.hora}`);
            return fechaB - fechaA;
        });

        if (citasFiltradas.length === 0) {
            listaAgenda.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">📅</div>
                    <div class="empty-state-text">No hay citas programadas</div>
                </div>
            `;
            return;
        }

        listaAgenda.innerHTML = citasFiltradas.map(cita => `
            <div class="item-card ${cita.completada ? 'completada' : ''}">
                <div class="item-header">
                    <div>
                        <div class="item-title">🐕 ${cita.perroNombre}</div>
                        <div class="item-subtitle">${cita.clienteNombre} - ${cita.telefono}</div>
                    </div>
                    <div class="item-actions">
                        ${!cita.completada ? `
                            <button class="btn btn-small btn-complete" onclick="app.completarCita(${cita.id})">
                                ✓ Completar
                            </button>
                        ` : ''}
                        <button class="btn btn-small btn-edit" onclick="app.editarCita(${cita.id})">
                            ✏️ Editar
                        </button>
                        <button class="btn btn-small btn-danger" onclick="app.eliminarCita(${cita.id})">
                            🗑️ Eliminar
                        </button>
                    </div>
                </div>
                <div class="item-details">
                    <div class="detail-item">
                        <span class="detail-label">📅 Fecha:</span>
                        <span class="detail-value">${this.formatearFecha(cita.fecha)}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">🕐 Hora:</span>
                        <span class="detail-value">${cita.hora}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">🐾 Raza:</span>
                        <span class="detail-value">${cita.raza}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">✂️ Servicios:</span>
                        <span class="badge badge-info">${cita.servicio}${cita.tamanoPerro ? ` (${this.nombreTamano(cita.tamanoPerro)})` : ''}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">💰 Precio:</span>
                        <span class="detail-value">${(cita.precio || 0).toFixed(2)}€</span>
                    </div>
                    ${cita.completada ? `
                        <div class="detail-item">
                            <span class="badge badge-success">Completada</span>
                        </div>
                    ` : ''}
                </div>
                ${cita.notas ? `
                    <div style="margin-top: 15px; padding-top: 15px; border-top: 1px solid #eee;">
                        <span class="detail-label">📝 Notas:</span>
                        <p style="margin-top: 5px; color: #666;">${cita.notas}</p>
                    </div>
                ` : ''}
            </div>
        `).join('');
    }

    // Mostrar clientes
    mostrarClientes(busqueda = '') {
        const listaClientes = document.getElementById('listaClientes');
        let clientesFiltrados = [...this.clientes];

        // Aplicar búsqueda
        if (busqueda) {
            const termino = busqueda.toLowerCase();
            clientesFiltrados = clientesFiltrados.filter(c => 
                c.nombre.toLowerCase().includes(termino) ||
                c.telefono.includes(termino) ||
                c.perros.some(p => p.nombre.toLowerCase().includes(termino))
            );
        }

        // Ordenar por última visita
        clientesFiltrados.sort((a, b) => new Date(b.ultimaVisita) - new Date(a.ultimaVisita));

        if (clientesFiltrados.length === 0) {
            listaClientes.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">👥</div>
                    <div class="empty-state-text">
                        ${busqueda ? 'No se encontraron clientes' : 'No hay clientes registrados'}
                    </div>
                </div>
            `;
            return;
        }

        listaClientes.innerHTML = clientesFiltrados.map(cliente => {
            const numCitas = this.citas.filter(c => c.telefono === cliente.telefono).length;
            const citasCompletadas = this.citas.filter(c => 
                c.telefono === cliente.telefono && c.completada
            ).length;

            return `
                <div class="item-card">
                    <div class="item-header">
                        <div>
                            <div class="item-title">👤 ${cliente.nombre}</div>
                            <div class="item-subtitle">📞 ${cliente.telefono}</div>
                        </div>
                    </div>
                    <div class="item-details">
                        <div class="detail-item">
                            <span class="detail-label">🐕 Perros (${cliente.perros.length}):</span>
                            <div style="margin-top: 10px; display: flex; flex-wrap: wrap; gap: 10px;">
                                ${cliente.perros.map(p => `
                                    <div style="display: flex; align-items: center; gap: 8px; padding: 8px; background: #f5f5f5; border-radius: 8px; flex: 0 0 auto;">
                                        ${p.foto ? 
                                            `<img src="${p.foto}" style="width: 40px; height: 40px; border-radius: 50%; object-fit: cover;">` : 
                                            '<span style="width: 40px; height: 40px; border-radius: 50%; background: #ddd; display: flex; align-items: center; justify-content: center; font-size: 20px;">🐕</span>'
                                        }
                                        <div>
                                            <strong>${p.nombre}</strong><br>
                                            <small style="color: #666;">${p.raza}</small><br>
                                            <small style="color: #999;">${this.nombreTamano(p.tamano || 'mediano')} | ${this.nombreLongitudPelo(p.longitudPelo || 'corto')}${p.edad ? ` | ${p.edad} años` : ''}</small>
                                        </div>
                                    </div>
                                `).join('')}
                            </div>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">📅 Última visita:</span>
                            <span class="detail-value">${this.formatearFecha(cliente.ultimaVisita)}</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">📊 Citas:</span>
                            <span class="badge badge-info">${citasCompletadas}/${numCitas}</span>
                        </div>
                    </div>
                    <div class="item-actions" style="margin-top: 15px;">
                        <button class="btn btn-small btn-edit" onclick="app.editarClienteDirecto(${cliente.id})">
                            ✏️ Editar
                        </button>
                        <button class="btn btn-small btn-danger" onclick="app.eliminarCliente(${cliente.id})">
                            🗑️ Eliminar
                        </button>
                    </div>
                </div>
            `;
        }).join('');
    }

    // Configurar búsqueda
    configurarBusqueda() {
        const buscarInput = document.getElementById('buscarCliente');
        buscarInput.addEventListener('input', (e) => {
            this.mostrarClientes(e.target.value);
        });
    }

    // Completar cita
    completarCita(id) {
        const cita = this.citas.find(c => c.id === id);
        if (cita) {
            cita.completada = true;
            this.guardarDatos('citas', this.citas);
            this.mostrarAgenda();
            this.actualizarEstadisticas();
            this.mostrarNotificacion('✅ Cita marcada como completada');
        }
    }

    // Eliminar cita
    eliminarCita(id) {
        if (confirm('¿Estás seguro de que quieres eliminar esta cita?')) {
            const cita = this.citas.find(c => c.id === id);
            
            this.citas = this.citas.filter(c => c.id !== id);
            this.guardarDatos('citas', this.citas);
            
            // Auto-sincronizar (eliminar de Calendar + backup Drive)
            setTimeout(() => {
                this.sincronizarCambiosAutomaticos('eliminar', cita).catch(err => console.log('Auto-sync:', err));
            }, 500);
            
            this.mostrarAgenda();
            this.mostrarClientes();
            this.actualizarEstadisticas();
            this.mostrarNotificacion('🗑️ Cita eliminada');
        }
    }

    // Editar cita
    editarCita(id) {
        const cita = this.citas.find(c => c.id === id);
        if (!cita) return;

        const modalBody = document.getElementById('modalBody');
        modalBody.innerHTML = `
            <div class="form-row">
                <div class="form-group">
                    <label>Cliente</label>
                    <input type="text" id="edit_clienteNombre" value="${cita.clienteNombre}" required>
                </div>
                <div class="form-group">
                    <label>Teléfono</label>
                    <input type="tel" id="edit_telefono" value="${cita.telefono}" required>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Nombre del Perro</label>
                    <input type="text" id="edit_perroNombre" value="${cita.perroNombre}" required>
                </div>
                <div class="form-group">
                    <label>Raza</label>
                    <select id="edit_raza" required>
                        <option value="">Seleccionar raza...</option>
                        ${this.razas.sort((a, b) => a.nombre.localeCompare(b.nombre)).map(raza => 
                            `<option value="${raza.nombre}" ${cita.raza === raza.nombre ? 'selected' : ''}>${raza.nombre}</option>`
                        ).join('')}
                    </select>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Tamaño del Perro</label>
                    <select id="edit_tamano" required>
                        <option value="">Seleccionar tamaño...</option>
                        <option value="mini" ${cita.tamanoPerro === 'mini' ? 'selected' : ''}>Mini (&lt; 5kg)</option>
                        <option value="pequeno" ${cita.tamanoPerro === 'pequeno' ? 'selected' : ''}>Pequeño (5-10kg)</option>
                        <option value="mediano" ${cita.tamanoPerro === 'mediano' ? 'selected' : ''}>Mediano (10-25kg)</option>
                        <option value="grande" ${cita.tamanoPerro === 'grande' ? 'selected' : ''}>Grande (25-45kg)</option>
                        <option value="gigante" ${cita.tamanoPerro === 'gigante' ? 'selected' : ''}>Gigante (&gt; 45kg)</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Longitud del Pelo</label>
                    <select id="edit_longitudPelo" required>
                        <option value="">Seleccionar longitud...</option>
                        <option value="corto" ${cita.longitudPelo === 'corto' ? 'selected' : ''}>Corto (&lt; 2 cm)</option>
                        <option value="medio" ${cita.longitudPelo === 'medio' ? 'selected' : ''}>Medio (2-5 cm)</option>
                        <option value="largo" ${cita.longitudPelo === 'largo' ? 'selected' : ''}>Largo (&gt; 5 cm)</option>
                    </select>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Fecha</label>
                    <input type="date" id="edit_fecha" value="${cita.fecha}" required>
                </div>
                <div class="form-group">
                    <label>Hora</label>
                    <input type="time" id="edit_hora" value="${cita.hora}" required>
                </div>
            </div>
            <div class="form-group">
                <label>Servicios (mantén Ctrl/Cmd para seleccionar múltiples)</label>
                <select id="edit_servicio" multiple size="5" required>
                    ${this.servicios.map(s => {
                        const isSelected = cita.servicios && cita.servicios.includes(s.nombre);
                        return `
                            <option value="${s.id}" data-servicio='${JSON.stringify(s)}' ${isSelected ? 'selected' : ''}>
                                ${s.nombre}${s.tipo === 'porRaza' ? ` (${s.preciosCombinaciones.length} combinaciones)` : ` - ${(s.precio || 0).toFixed(2)}€`}
                            </option>
                        `;
                    }).join('')}
                </select>
            </div>
            <div class="form-group">
                <label>Precio Total (€)</label>
                <input type="number" id="edit_precio" step="0.01" value="${cita.precio}" readonly style="background-color: #f0f0f0;">
            </div>
            <div class="form-group">
                <label>Notas</label>
                <textarea id="edit_notas" rows="3">${cita.notas || ''}</textarea>
            </div>
        `;

        document.getElementById('modalTitulo').textContent = 'Editar Cita';
        document.getElementById('modalEdicion').style.display = 'flex';

        // Función para calcular precio total en el modal de edición
        const calcularPrecioEdicion = () => {
            const servicioSelect = document.getElementById('edit_servicio');
            const razaInput = document.getElementById('edit_raza').value;
            const tamanoSelect = document.getElementById('edit_tamano')?.value;
            const longitudSelect = document.getElementById('edit_longitudPelo')?.value;
            
            let total = 0;
            Array.from(servicioSelect.selectedOptions).forEach(option => {
                const servicio = JSON.parse(option.dataset.servicio);
                
                if (servicio.tipo === 'fijo') {
                    total += servicio.precio;
                } else if (servicio.tipo === 'porRaza' && razaInput && tamanoSelect && longitudSelect) {
                    const combo = servicio.preciosCombinaciones.find(
                        pc => pc.raza === razaInput && 
                              pc.tamano === tamanoSelect && 
                              pc.longitudPelo === longitudSelect
                    );
                    if (combo) {
                        total += combo.precio;
                    }
                }
            });
            
            document.getElementById('edit_precio').value = total.toFixed(2);
        };

        // Añadir event listeners para recalcular precio
        setTimeout(() => {
            document.getElementById('edit_servicio').addEventListener('change', calcularPrecioEdicion);
            const razaInput = document.getElementById('edit_raza');
            const tamanoSelect = document.getElementById('edit_tamano');
            const longitudSelect = document.getElementById('edit_longitudPelo');
            if (razaInput) razaInput.addEventListener('change', calcularPrecioEdicion);
            if (tamanoSelect) tamanoSelect.addEventListener('change', calcularPrecioEdicion);
            if (longitudSelect) longitudSelect.addEventListener('change', calcularPrecioEdicion);
        }, 100);

        const form = document.getElementById('modalForm');
        form.onsubmit = (e) => {
            e.preventDefault();
            
            const servicioSelect = document.getElementById('edit_servicio');
            const serviciosSeleccionados = Array.from(servicioSelect.selectedOptions).map(option => {
                const servicio = JSON.parse(option.dataset.servicio);
                return servicio.nombre;
            });

            if (serviciosSeleccionados.length === 0) {
                this.mostrarNotificacion('⚠️ Debes seleccionar al menos un servicio');
                return;
            }
            
            cita.clienteNombre = document.getElementById('edit_clienteNombre').value;
            cita.telefono = document.getElementById('edit_telefono').value;
            cita.perroNombre = document.getElementById('edit_perroNombre').value;
            const nuevaRaza = document.getElementById('edit_raza').value;
            const nuevoTamano = document.getElementById('edit_tamano').value;
            const nuevaLongitud = document.getElementById('edit_longitudPelo').value;
            
            // Verificar si cambiaron datos del perro
            const cambioEnPerro = (
                cita.raza !== nuevaRaza ||
                cita.tamanoPerro !== nuevoTamano ||
                cita.longitudPelo !== nuevaLongitud
            );
            
            cita.raza = nuevaRaza;
            cita.tamanoPerro = nuevoTamano;
            cita.longitudPelo = nuevaLongitud;
            cita.fecha = document.getElementById('edit_fecha').value;
            cita.hora = document.getElementById('edit_hora').value;
            cita.servicio = serviciosSeleccionados.join(', ');
            cita.servicios = serviciosSeleccionados;
            cita.precio = parseFloat(document.getElementById('edit_precio').value);
            cita.notas = document.getElementById('edit_notas').value;

            this.guardarDatos('citas', this.citas);
            this.actualizarCliente(cita);
            
            // Si cambiaron datos del perro, actualizar todas las citas futuras del mismo perro
            if (cambioEnPerro) {
                this.actualizarCitasDelMismoPerro(cita);
            }
            
            // Auto-sincronizar cambios (actualizar Calendar + backup Drive)
            setTimeout(() => {
                this.sincronizarCambiosAutomaticos('editar', cita).catch(err => console.log('Auto-sync:', err));
            }, 500);
            
            this.mostrarAgenda();
            this.mostrarClientes();
            this.cerrarModal();
            this.mostrarNotificacion('✅ Cita actualizada');
        };
    }

    // Editar cliente
    editarCliente(id) {
        const cliente = this.clientes.find(c => c.id === id);
        if (!cliente) return;

        const modalBody = document.getElementById('modalBody');
        modalBody.innerHTML = `
            <div class="form-group">
                <label>Nombre del Cliente</label>
                <input type="text" id="edit_clienteNombre" value="${cliente.nombre}" required>
            </div>
            <div class="form-group">
                <label>Teléfono</label>
                <input type="tel" id="edit_telefono" value="${cliente.telefono}" required>
            </div>
            <div class="form-group">
                <label>Perros (separados por coma: Nombre-Raza, Nombre-Raza)</label>
                <textarea id="edit_perros" rows="3" required>${cliente.perros.map(p => `${p.nombre}-${p.raza}`).join(', ')}</textarea>
            </div>
        `;

        document.getElementById('modalTitulo').textContent = 'Editar Cliente';
        document.getElementById('modalEdicion').style.display = 'flex';

        const form = document.getElementById('modalForm');
        form.onsubmit = (e) => {
            e.preventDefault();
            
            cliente.nombre = document.getElementById('edit_clienteNombre').value;
            cliente.telefono = document.getElementById('edit_telefono').value;
            
            const perrosText = document.getElementById('edit_perros').value;
            cliente.perros = perrosText.split(',').map(p => {
                const [nombre, raza] = p.trim().split('-');
                return { nombre: nombre.trim(), raza: raza.trim() };
            });

            this.guardarDatos('clientes', this.clientes);
            this.mostrarClientes();
            this.cerrarModal();
            this.mostrarNotificacion('✅ Cliente actualizado');
        };
    }

    // Eliminar cliente
    eliminarCliente(id) {
        if (confirm('¿Estás seguro de que quieres eliminar este cliente? Se eliminarán también sus citas.')) {
            const cliente = this.clientes.find(c => c.id === id);
            
            // Eliminar citas del cliente
            this.citas = this.citas.filter(c => c.telefono !== cliente.telefono);
            this.guardarDatos('citas', this.citas);
            
            // Eliminar cliente
            this.clientes = this.clientes.filter(c => c.id !== id);
            this.guardarDatos('clientes', this.clientes);
            
            // Auto-backup en segundo plano
            setTimeout(() => {
                this.sincronizarConDrive(true).catch(err => console.log('Auto-backup Drive:', err));
            }, 500);
            
            this.mostrarClientes();
            this.mostrarAgenda();
            this.actualizarEstadisticas();
            this.mostrarNotificacion('🗑️ Cliente eliminado');
            this.cargarClientesEnSelect(); // Actualizar select de citas
        }
    }

    // Cerrar modal
    cerrarModal() {
        document.getElementById('modalEdicion').style.display = 'none';
    }

    // Gestión de Servicios
    guardarServicio() {
        const editId = document.getElementById('servicioEditId').value;
        const nombre = document.getElementById('nombreServicio').value;
        const tipo = document.getElementById('tipoServicio').value;
        const descripcion = document.getElementById('descripcionServicio').value;

        let servicioData = {
            nombre,
            tipo,
            descripcion
        };

        if (tipo === 'fijo') {
            servicioData.precio = parseFloat(document.getElementById('precioFijo').value);
        } else {
            // Usar las combinaciones temporales guardadas
            servicioData.preciosCombinaciones = [...this.preciosCombinacionesTemp];
            if (servicioData.preciosCombinaciones.length === 0) {
                this.mostrarNotificacion('⚠️ Debes agregar al menos una combinación de raza/tamaño con precio');
                return;
            }
        }

        if (editId) {
            // Editar servicio existente
            const servicio = this.servicios.find(s => s.id == editId);
            if (servicio) {
                Object.assign(servicio, servicioData);
                this.mostrarNotificacion('✅ Servicio actualizado');
            }
        } else {
            // Agregar nuevo servicio
            servicioData.id = Date.now();
            this.servicios.push(servicioData);
            this.mostrarNotificacion('✅ Servicio agregado');
        }

        this.guardarDatos('servicios', this.servicios);
        
        // Auto-backup en segundo plano
        setTimeout(() => {
            this.sincronizarConDrive(true).catch(err => console.log('Auto-backup Drive:', err));
        }, 500);
        
        document.getElementById('servicioForm').reset();
        document.getElementById('servicioEditId').value = '';
        document.getElementById('btnGuardarServicio').textContent = 'Agregar Servicio';
        document.getElementById('btnCancelarServicio').style.display = 'none';
        document.getElementById('preciosPorRazaContainer').style.display = 'none';
        document.getElementById('precioFijoContainer').style.display = 'block';
        this.preciosCombinacionesTemp = [];
        document.getElementById('listaPreciosCombinaciones').innerHTML = '';
        
        this.mostrarServicios();
        this.cargarServicios();
    }

    cargarRazasEnSelectPrecio() {
        const selectRazaPrecio = document.getElementById('razaPrecio');
        if (!selectRazaPrecio) {
            console.error('No se encontró el select razaPrecio');
            return;
        }
        
        // Asegurar que las razas estén disponibles
        if (!this.razas || this.razas.length === 0) {
            console.error('No hay razas disponibles');
            this.razas = this.razasDefault();
            this.guardarDatos('razas', this.razas);
        }
        
        const razasOrdenadas = [...this.razas].sort((a, b) => a.nombre.localeCompare(b.nombre));
        
        const opciones = '<option value="">Seleccionar raza...</option>' +
            razasOrdenadas.map(raza => 
                `<option value="${raza.nombre}">${raza.nombre}</option>`
            ).join('');
        
        selectRazaPrecio.innerHTML = opciones;
        console.log('✅ Razas cargadas en select de precios:', razasOrdenadas.length);
    }

    agregarPrecioCombinacion() {
        const raza = document.getElementById('razaPrecio').value;
        const tamano = document.getElementById('tamanoPrecio').value;
        const longitudPelo = document.getElementById('longitudPeloPrecio').value;
        const precio = parseFloat(document.getElementById('precioCombinacion').value);

        console.log('Valores capturados:', { raza, tamano, longitudPelo, precio });

        if (!raza || !tamano || !longitudPelo || !precio || precio <= 0) {
            this.mostrarNotificacion('⚠️ Debes seleccionar raza, tamaño, longitud de pelo y un precio válido');
            console.error('Validación fallida:', { 
                raza: !raza ? 'VACÍO' : raza, 
                tamano: !tamano ? 'VACÍO' : tamano, 
                longitudPelo: !longitudPelo ? 'VACÍO' : longitudPelo, 
                precio: (!precio || precio <= 0) ? 'INVÁLIDO' : precio 
            });
            return;
        }

        // Verificar si ya existe esta combinación
        const existe = this.preciosCombinacionesTemp.find(c => 
            c.raza === raza && c.tamano === tamano && c.longitudPelo === longitudPelo
        );
        if (existe) {
            this.mostrarNotificacion('⚠️ Ya existe un precio para esta combinación');
            return;
        }

        this.preciosCombinacionesTemp.push({ raza, tamano, longitudPelo, precio });
        
        // Limpiar campos
        document.getElementById('razaPrecio').value = '';
        document.getElementById('tamanoPrecio').value = '';
        document.getElementById('longitudPeloPrecio').value = '';
        document.getElementById('precioCombinacion').value = '';
        
        this.mostrarPreciosCombinaciones();
        this.mostrarNotificacion('✅ Combinación agregada');
    }

    mostrarPreciosCombinaciones() {
        const lista = document.getElementById('listaPreciosCombinaciones');
        
        if (this.preciosCombinacionesTemp.length === 0) {
            lista.innerHTML = '';
            return;
        }

        lista.innerHTML = this.preciosCombinacionesTemp.map((combo, index) => `
            <div class="item-card" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; padding: 10px;">
                <div>
                    <strong>${combo.raza}</strong> - ${this.nombreTamano(combo.tamano)} - ${this.nombreLongitudPelo(combo.longitudPelo)}
                    <span style="color: #4caf50; font-weight: bold; margin-left: 10px;">${(combo.precio || 0).toFixed(2)}€</span>
                </div>
                <button type="button" class="btn-delete" onclick="app.eliminarPrecioCombinacion(${index})">🗑️</button>
            </div>
        `).join('');
    }

    eliminarPrecioCombinacion(index) {
        this.preciosCombinacionesTemp.splice(index, 1);
        this.mostrarPreciosCombinaciones();
        this.mostrarNotificacion('✅ Combinación eliminada');
    }

    nombreTamano(codigo) {
        const nombres = {
            mini: 'Mini',
            pequeno: 'Pequeño',
            mediano: 'Mediano',
            grande: 'Grande',
            gigante: 'Gigante'
        };
        return nombres[codigo] || codigo;
    }

    nombreLongitudPelo(codigo) {
        const nombres = {
            corto: 'Pelo Corto',
            medio: 'Pelo Medio',
            largo: 'Pelo Largo'
        };
        return nombres[codigo] || codigo;
    }

    mostrarServicios() {
        const lista = document.getElementById('listaServicios');
        
        if (this.servicios.length === 0) {
            lista.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">✂️</div>
                    <div class="empty-state-text">No hay servicios registrados</div>
                </div>
            `;
            return;
        }

        lista.innerHTML = this.servicios.map(servicio => `
            <div class="item-card">
                <div class="item-header">
                    <div>
                        <div class="item-title">✂️ ${servicio.nombre}</div>
                        <div class="item-subtitle">${servicio.descripcion || 'Sin descripción'}</div>
                    </div>
                    <div class="item-actions">
                        ${servicio.tipo === 'fijo' ? 
                            `<span class="badge badge-success" style="font-size: 1.2em;">${(servicio.precio || 0).toFixed(2)}€</span>` :
                            `<div style="text-align: right;">
                                <div class="badge badge-info" style="font-size: 0.9em;">Por raza/tamaño</div>
                                <div style="font-size: 0.85em; margin-top: 5px; color: #666;">
                                    ${servicio.preciosCombinaciones?.length || 0} combinaciones
                                </div>
                            </div>`
                        }
                        <button class="btn btn-small btn-edit" onclick="app.editarServicio(${servicio.id})">
                            ✏️ Editar
                        </button>
                        <button class="btn btn-small btn-danger" onclick="app.eliminarServicio(${servicio.id})">
                            🗑️ Eliminar
                        </button>
                    </div>
                </div>
                ${servicio.tipo === 'porRaza' ? `
                    <div class="item-details" style="margin-top: 15px;">
                        <div style="font-weight: bold; margin-bottom: 10px; color: #666;">Precios por Raza/Tamaño/Pelo:</div>
                        ${servicio.preciosCombinaciones?.map(c => `
                            <div class="detail-item" style="display: flex; justify-content: space-between; padding: 5px 0;">
                                <span class="detail-label">${c.raza} - ${this.nombreTamano(c.tamano)} - ${this.nombreLongitudPelo(c.longitudPelo)}</span>
                                <span class="detail-value" style="color: #4caf50; font-weight: bold;">${(c.precio || 0).toFixed(2)}€</span>
                            </div>
                        `).join('') || '<div style="color: #999;">No hay combinaciones definidas</div>'}
                    </div>
                ` : ''}
            </div>
        `).join('');
    }

    editarServicio(id) {
        const servicio = this.servicios.find(s => s.id === id);
        if (!servicio) return;

        document.getElementById('nombreServicio').value = servicio.nombre;
        document.getElementById('tipoServicio').value = servicio.tipo;
        document.getElementById('descripcionServicio').value = servicio.descripcion || '';
        document.getElementById('servicioEditId').value = servicio.id;

        if (servicio.tipo === 'fijo') {
            document.getElementById('precioFijo').value = servicio.precio;
            document.getElementById('precioFijoContainer').style.display = 'block';
            document.getElementById('preciosPorRazaContainer').style.display = 'none';
        } else {
            this.preciosCombinacionesTemp = [...(servicio.preciosCombinaciones || [])];
            this.cargarRazasEnSelectPrecio();
            this.mostrarPreciosCombinaciones();
            document.getElementById('precioFijoContainer').style.display = 'none';
            document.getElementById('preciosPorRazaContainer').style.display = 'block';
        }

        document.getElementById('btnGuardarServicio').textContent = 'Actualizar Servicio';
        document.getElementById('btnCancelarServicio').style.display = 'inline-block';

        // Scroll al formulario
        document.getElementById('servicioForm').scrollIntoView({ behavior: 'smooth' });
    }

    cancelarEdicionServicio() {
        document.getElementById('servicioForm').reset();
        document.getElementById('servicioEditId').value = '';
        document.getElementById('btnGuardarServicio').textContent = 'Agregar Servicio';
        document.getElementById('btnCancelarServicio').style.display = 'none';
        document.getElementById('precioFijoContainer').style.display = 'block';
        document.getElementById('preciosPorRazaContainer').style.display = 'none';
        this.preciosCombinacionesTemp = [];
        document.getElementById('listaPreciosCombinaciones').innerHTML = '';
    }

    eliminarServicio(id) {
        if (confirm('¿Estás seguro de que quieres eliminar este servicio?')) {
            this.servicios = this.servicios.filter(s => s.id !== id);
            this.guardarDatos('servicios', this.servicios);
            
            // Auto-backup en segundo plano
            setTimeout(() => {
                this.sincronizarConDrive(true).catch(err => console.log('Auto-backup Drive:', err));
            }, 500);
            
            this.mostrarServicios();
            this.cargarServicios();
            this.mostrarNotificacion('🗑️ Servicio eliminado');
        }
    }

    // Google Calendar Integration
    async sincronizarConGoogle(silencioso = false) {
        try {
            // Usar la nueva integración OAuth
            if (typeof oauthIntegration !== 'undefined' && oauthIntegration) {
                const estado = oauthIntegration.obtenerEstado();
                
                if (!estado.autenticado) {
                    if (!silencioso) {
                        const conectar = confirm('🔐 Necesitas conectarte con Google primero.\n\n¿Quieres conectarte ahora?');
                        if (conectar && typeof oauthManager !== 'undefined') {
                            await oauthManager.iniciarLoginGoogle();
                        }
                    }
                    return;
                }
                
                // Sincronizar todas las citas pendientes
                const citasPendientes = this.citas.filter(c => !c.googleEventId && !c.completada);
                
                if (citasPendientes.length === 0) {
                    if (!silencioso) this.mostrarNotificacion('ℹ️ No hay citas nuevas para sincronizar');
                    return;
                }

                let sincronizadas = 0;
                for (const cita of citasPendientes) {
                    try {
                        const resultado = await oauthIntegration.exportarCitaACalendar(cita);
                        if (resultado) {
                            sincronizadas++;
                        }
                    } catch (error) {
                        console.error('Error al sincronizar cita:', cita, error);
                    }
                }

                if (sincronizadas === 0) {
                    if (!silencioso) this.mostrarNotificacion('⚠️ No se pudo sincronizar ninguna cita. Verifica la consola para más detalles.');
                    return;
                }

                if (sincronizadas > 0) {
                    if (!silencioso) this.mostrarNotificacion(`✅ ${sincronizadas} cita${sincronizadas > 1 ? 's' : ''} sincronizada${sincronizadas > 1 ? 's' : ''} con Google Calendar`);
                } else {
                    if (!silencioso) this.mostrarNotificacion('ℹ️ Todas las citas ya estaban en Google Calendar');
                }
            } else {
                if (!silencioso) this.mostrarNotificacion('⚠️ Sistema OAuth no disponible. Recarga la página.');
            }
        } catch (error) {
            console.error('Error al sincronizar:', error);
            if (!silencioso) this.mostrarNotificacion('❌ Error al sincronizar con Google Calendar. Verifica tu conexión.');
        }
    }

    // Google Drive Backup
    async sincronizarConDrive(silencioso = false) {
        try {
            if (typeof oauthIntegration !== 'undefined' && oauthIntegration) {
                // Usar estaAutenticadoGeneral() en lugar de obtenerEstado()
                if (!oauthIntegration.estaAutenticadoGeneral()) {
                    if (!silencioso) {
                        const conectar = confirm('🔐 Necesitas conectarte con Google primero.\n\n¿Quieres conectarte ahora?');
                        if (conectar) {
                            // Usar autenticación nativa o web según la plataforma
                            if (oauthIntegration.isNativeApp) {
                                await oauthIntegration.loginNativo();
                            } else if (typeof oauthManager !== 'undefined') {
                                await oauthManager.iniciarLoginGoogle();
                            }
                        }
                    }
                    return;
                }
                
                if (!silencioso) this.mostrarNotificacion('💾 Creando backup en Google Drive...');
                
                const resultado = await oauthIntegration.hacerBackup();
                
                if (resultado) {
                    if (!silencioso) this.mostrarNotificacion('✅ Backup guardado en Google Drive');
                } else {
                    if (!silencioso) this.mostrarNotificacion('⚠️ No se pudo crear el backup');
                }
            } else {
                this.mostrarNotificacion('⚠️ Sistema OAuth no disponible. Recarga la página.');
            }
        } catch (error) {
            console.error('Error al sincronizar con Drive:', error);
            this.mostrarNotificacion('❌ Error al crear backup en Drive: ' + error.message);
        }
    }

    async autorizarGoogleCalendar() {
        const CLIENT_ID = 'TU_CLIENT_ID_AQUI'; // El usuario debe configurar esto
        const API_KEY = 'TU_API_KEY_AQUI'; // El usuario debe configurar esto
        const SCOPES = 'https://www.googleapis.com/auth/calendar.events';

        // Mostrar instrucciones al usuario
        const configurar = confirm(
            '🔧 CONFIGURACIÓN NECESARIA\n\n' +
            'Para sincronizar con Google Calendar necesitas:\n\n' +
            '1. Crear un proyecto en Google Cloud Console\n' +
            '2. Habilitar la API de Google Calendar\n' +
            '3. Crear credenciales OAuth 2.0\n' +
            '4. Agregar tu CLIENT_ID y API_KEY al código\n\n' +
            '¿Quieres ver las instrucciones detalladas?'
        );

        if (configurar) {
            window.open('https://developers.google.com/calendar/api/quickstart/js', '_blank');
            this.mostrarNotificacion('📖 Se abrió la guía de configuración de Google Calendar API');
        }

        // Simulación de autorización (el usuario debe implementar OAuth real)
        this.mostrarNotificacion('⚠️ Configura primero CLIENT_ID y API_KEY en app.js línea 400');
    }

    async agregarAGoogleCalendar(cita) {
        if (!this.googleCalendarToken) return;

        // Aquí iría la llamada real a la API de Google Calendar
        // Por ahora, simulamos que se guardó
        cita.googleEventId = `simulado_${cita.id}`;
        this.guardarDatos('citas', this.citas);
    }

    async actualizarEnGoogleCalendar(cita) {
        if (!cita.googleEventId) return;
        
        try {
            if (typeof oauthIntegration !== 'undefined' && oauthIntegration) {
                const estado = oauthIntegration.obtenerEstado();
                if (!estado.autenticado) return;
                
                const evento = oauthIntegration.convertirCitaAEvento(cita);
                await oauthIntegration.oauth.actualizarEventoCalendar(cita.googleEventId, evento);
                console.log('✅ Evento actualizado en Calendar:', cita.googleEventId);
            }
        } catch (error) {
            console.error('❌ Error al actualizar evento en Calendar:', error);
        }
    }

    async eliminarDeGoogleCalendar(eventId) {
        if (!eventId) return;
        
        try {
            if (typeof oauthIntegration !== 'undefined' && oauthIntegration) {
                const estado = oauthIntegration.obtenerEstado();
                if (!estado.autenticado) return;
                
                await oauthIntegration.oauth.eliminarEventoCalendar(eventId);
                console.log('✅ Evento eliminado de Calendar:', eventId);
            }
        } catch (error) {
            console.error('❌ Error al eliminar evento de Calendar:', error);
        }
    }

    // Sincronización automática completa (crear/actualizar/eliminar)
    async sincronizarCambiosAutomaticos(operacion, cita = null) {
        try {
            if (typeof oauthIntegration === 'undefined' || !oauthIntegration) return;
            
            // Usar estaAutenticadoGeneral() para verificar autenticación
            if (!oauthIntegration.estaAutenticadoGeneral()) return;
            
            // Sincronizar con Calendar según la operación
            if (operacion === 'crear' && cita) {
                // Crear nuevo evento si no tiene googleEventId
                if (!cita.googleEventId) {
                    const resultado = await oauthIntegration.exportarCitaACalendar(cita);
                    if (resultado) {
                        console.log('✅ Auto-sync: Cita creada en Calendar');
                    }
                }
            } else if (operacion === 'editar' && cita) {
                // Actualizar evento existente
                console.log('🔍 Editando cita, googleEventId:', cita.googleEventId);
                if (cita.googleEventId) {
                    await this.actualizarEnGoogleCalendar(cita);
                    console.log('✅ Auto-sync: Cita actualizada en Calendar');
                } else {
                    // Si no tiene eventId, crear uno nuevo
                    console.log('⚠️ Cita editada sin googleEventId, creando nuevo evento');
                    const resultado = await oauthIntegration.exportarCitaACalendar(cita);
                    if (resultado) {
                        console.log('✅ Auto-sync: Cita creada en Calendar (faltaba eventId)');
                    }
                }
            } else if (operacion === 'eliminar' && cita) {
                // Eliminar evento de Calendar
                if (cita.googleEventId) {
                    await this.eliminarDeGoogleCalendar(cita.googleEventId);
                    console.log('✅ Auto-sync: Cita eliminada de Calendar');
                }
            }
            
            // Siempre hacer backup en Drive
            console.log('💾 Iniciando backup automático en Drive...');
            await this.sincronizarConDrive(true);
            console.log('✅ Backup en Drive completado');
            
        } catch (error) {
            console.error('Error en sincronización automática:', error);
        }
    }

    // Actualizar estadísticas
    actualizarEstadisticas() {
        const totalCitas = this.citas.length;
        const totalIngresos = this.citas
            .filter(c => c.completada)
            .reduce((sum, c) => sum + c.precio, 0);

        document.getElementById('totalCitas').textContent = totalCitas;
        document.getElementById('totalIngresos').textContent = `${totalIngresos.toFixed(2)}€`;
    }

    // Formatear fecha
    formatearFecha(fecha) {
        const f = new Date(fecha + 'T00:00:00');
        const opciones = { day: '2-digit', month: '2-digit', year: 'numeric' };
        return f.toLocaleDateString('es-ES', opciones);
    }

    // Mostrar notificación
    mostrarNotificacion(mensaje) {
        // Crear elemento de notificación
        const notif = document.createElement('div');
        notif.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: #4CAF50;
            color: white;
            padding: 15px 25px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.3);
            z-index: 1000;
            animation: slideIn 0.3s ease;
        `;
        notif.textContent = mensaje;

        // Agregar animación
        const style = document.createElement('style');
        style.textContent = `
            @keyframes slideIn {
                from { transform: translateX(400px); opacity: 0; }
                to { transform: translateX(0); opacity: 1; }
            }
        `;
        document.head.appendChild(style);

        document.body.appendChild(notif);

        // Eliminar después de 3 segundos
        setTimeout(() => {
            notif.style.animation = 'slideIn 0.3s ease reverse';
            setTimeout(() => notif.remove(), 300);
        }, 3000);
    }
}

// Funciones globales para las pestañas
function showTab(tabName, btnElement) {
    // Ocultar todos los contenidos
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });

    // Desactivar todos los botones
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    // Activar el contenido y botón seleccionado
    document.getElementById(tabName).classList.add('active');
    if (btnElement) {
        btnElement.classList.add('active');
    }
    
    // Si se muestra la pestaña de servicios, asegurar que las razas estén cargadas
    if (tabName === 'servicios' && app && typeof app.cargarRazasEnSelectPrecio === 'function') {
        const tipoServicio = document.getElementById('tipoServicio');
        if (tipoServicio && tipoServicio.value === 'porRaza') {
            app.cargarRazasEnSelectPrecio();
        }
    }
}

// Función global para cambiar tipo de precio
function cambiarTipoPrecio() {
    const tipo = document.getElementById('tipoServicio').value;
    const precioFijoContainer = document.getElementById('precioFijoContainer');
    const preciosPorRazaContainer = document.getElementById('preciosPorRazaContainer');

    if (tipo === 'fijo') {
        precioFijoContainer.style.display = 'block';
        preciosPorRazaContainer.style.display = 'none';
        document.getElementById('precioFijo').required = true;
    } else {
        precioFijoContainer.style.display = 'none';
        preciosPorRazaContainer.style.display = 'block';
        document.getElementById('precioFijo').required = false;
        
        // Cargar razas en el select cuando se muestra - sin delay
        if (app && typeof app.cargarRazasEnSelectPrecio === 'function') {
            app.cargarRazasEnSelectPrecio();
        }
    }
}

// Función global para filtrar agenda
function filtrarAgenda(filtro) {
    if (!window.app) {
        console.warn('⚠️ App aún no está inicializada');
        return;
    }
    app.mostrarAgenda(filtro);
}

// Función global para toggle de lista de razas
function toggleListaRazas() {
    const lista = document.getElementById('listaRazas');
    const btn = document.getElementById('btnToggleRazas');
    
    if (lista.style.display === 'none') {
        lista.style.display = 'block';
        btn.innerHTML = '▲ Ocultar lista de razas';
    } else {
        lista.style.display = 'none';
        btn.innerHTML = '▼ Mostrar lista de razas';
    }
}

// Inicializar la aplicación
let app;
window.app = null; // Declarar globalmente desde el inicio

document.addEventListener('DOMContentLoaded', () => {
    app = new PeluqueriaCanina();
    window.app = app; // Hacer disponible globalmente para OAuth
    
    console.log('✅ App inicializada:', {
        citas: app.citas.length,
        clientes: app.clientes.length,
        servicios: app.servicios.length
    });
});
