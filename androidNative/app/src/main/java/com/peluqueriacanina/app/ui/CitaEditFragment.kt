package com.peluqueriacanina.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.peluqueriacanina.app.R
import com.peluqueriacanina.app.data.Cita
import com.peluqueriacanina.app.data.Cliente
import com.peluqueriacanina.app.data.Perro
import com.peluqueriacanina.app.data.Servicio
import com.peluqueriacanina.app.viewmodel.CitaViewModel
import com.peluqueriacanina.app.viewmodel.ClienteViewModel
import com.peluqueriacanina.app.viewmodel.ServicioViewModel
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CitaEditFragment : DialogFragment() {

    private val citaViewModel: CitaViewModel by activityViewModels()
    private val clienteViewModel: ClienteViewModel by activityViewModels()
    private val servicioViewModel: ServicioViewModel by activityViewModels()
    
    private var cita: Cita? = null
    private var perro: Perro? = null
    private var servicios: List<Servicio> = emptyList()
    private var selectedFechaTimestamp: Long? = null
    private var selectedHora: String? = null
    
    // Para selección de cliente/perro
    private var clientes: List<Cliente> = emptyList()
    private var perros: List<Perro> = emptyList()
    private var selectedCliente: Cliente? = null
    private var selectedPerro: Perro? = null
    
    // Map para cachear precios calculados por servicio
    private val preciosCalculados = mutableMapOf<Long, Double>()

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    companion object {
        private const val ARG_CITA_ID = "cita_id"
        
        fun newInstance(citaId: Long): CitaEditFragment {
            return CitaEditFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_CITA_ID, citaId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_PeluqueriaCanina_FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cita_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val spinnerCliente = view.findViewById<AutoCompleteTextView>(R.id.spinnerCliente)
        val spinnerPerro = view.findViewById<AutoCompleteTextView>(R.id.spinnerPerro)
        val inputFecha = view.findViewById<TextInputEditText>(R.id.inputFecha)
        val inputHora = view.findViewById<TextInputEditText>(R.id.inputHora)
        val chipGroupServicios = view.findViewById<ChipGroup>(R.id.chipGroupServicios)
        val txtPrecioTotal = view.findViewById<TextView>(R.id.txtPrecioTotal)
        val inputNotas = view.findViewById<TextInputEditText>(R.id.inputNotas)
        val btnGuardar = view.findViewById<MaterialButton>(R.id.btnGuardar)

        // Cerrar con X
        toolbar.setNavigationOnClickListener {
            dismiss()
        }

        val citaId = arguments?.getLong(ARG_CITA_ID) ?: return

        // Date picker
        inputFecha.setOnClickListener {
            val calendar = Calendar.getInstance()
            selectedFechaTimestamp?.let { calendar.timeInMillis = it }
            
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedFechaTimestamp = calendar.timeInMillis
                    inputFecha.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Time picker
        inputHora.setOnClickListener {
            val parts = selectedHora?.split(":") ?: listOf("10", "00")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 10
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
            
            TimePickerDialog(
                requireContext(),
                { _, selectedHour, selectedMinute ->
                    selectedHora = String.format("%02d:%02d", selectedHour, selectedMinute)
                    inputHora.setText(selectedHora)
                },
                hour,
                minute,
                true
            ).show()
        }
        
        // Cargar clientes
        clienteViewModel.allClientes.observe(viewLifecycleOwner) { clienteList ->
            clientes = clienteList
            val nombres = clienteList.map { it.nombre }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, nombres)
            spinnerCliente.setAdapter(adapter)
            
            spinnerCliente.setOnItemClickListener { _, _, position, _ ->
                selectedCliente = clientes[position]
                loadPerrosForCliente(spinnerPerro, chipGroupServicios, txtPrecioTotal)
            }
        }

        // Cargar servicios
        servicioViewModel.allServicios.observe(viewLifecycleOwner) { servicioList ->
            servicios = servicioList.filter { it.activo }
            
            // Cargar la cita y popular los chips
            citaViewModel.allCitas.observe(viewLifecycleOwner) { citas ->
                cita = citas.find { it.id == citaId }
                cita?.let { c ->
                    // Cargar datos del cliente y perro
                    lifecycleScope.launch {
                        val cliente = citaViewModel.getClienteById(c.clienteId)
                        perro = citaViewModel.getPerroById(c.perroId)
                        
                        // Seleccionar cliente en spinner
                        cliente?.let { cl ->
                            selectedCliente = cl
                            spinnerCliente.setText(cl.nombre, false)
                            
                            // Cargar perros del cliente
                            loadPerrosForClienteInitial(c.perroId, spinnerPerro, chipGroupServicios, txtPrecioTotal)
                        }
                        
                        selectedPerro = perro
                        
                        // Calcular precios basándose en el perro
                        perro?.let { p ->
                            preciosCalculados.clear()
                            servicios.forEach { servicio ->
                                val precio = servicioViewModel.calcularPrecioParaPerro(
                                    servicioId = servicio.id,
                                    raza = p.raza,
                                    tamano = p.tamano,
                                    longitudPelo = p.longitudPelo
                                )
                                preciosCalculados[servicio.id] = precio
                            }
                            
                            // Actualizar chips con precios calculados
                            updateChipsConPrecios(chipGroupServicios)
                            updatePrecioTotal(chipGroupServicios, txtPrecioTotal)
                        }
                    }

                    // Fecha y hora
                    selectedFechaTimestamp = c.fecha
                    selectedHora = c.hora
                    inputFecha.setText(dateFormat.format(Date(c.fecha)))
                    inputHora.setText(c.hora)
                    
                    // Notas
                    inputNotas.setText(c.notas)

                    // Parse servicios actuales
                    val currentServiciosIds = mutableListOf<Long>()
                    try {
                        val idsArray = JSONArray(c.serviciosIds)
                        for (i in 0 until idsArray.length()) {
                            currentServiciosIds.add(idsArray.getLong(i))
                        }
                    } catch (e: Exception) { }

                    // Crear chips
                    chipGroupServicios.removeAllViews()
                    servicios.forEach { servicio ->
                        val precioTexto = if (servicio.tipoPrecio == "fijo") {
                            "${servicio.precioBase}€"
                        } else {
                            "Variable"
                        }
                        val chip = Chip(requireContext()).apply {
                            text = "${servicio.nombre} - $precioTexto"
                            isCheckable = true
                            isChecked = currentServiciosIds.contains(servicio.id)
                            tag = servicio
                            setOnCheckedChangeListener { _, _ ->
                                updatePrecioTotal(chipGroupServicios, txtPrecioTotal)
                            }
                        }
                        chipGroupServicios.addView(chip)
                    }

                    updatePrecioTotal(chipGroupServicios, txtPrecioTotal)
                }
            }
        }

        // Guardar cambios
        btnGuardar.setOnClickListener {
            if (selectedCliente == null) {
                Toast.makeText(context, "Selecciona un cliente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedPerro == null) {
                Toast.makeText(context, "Selecciona un perro", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedFechaTimestamp == null) {
                Toast.makeText(context, "Selecciona una fecha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedHora == null) {
                Toast.makeText(context, "Selecciona una hora", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val serviciosSeleccionados = getSelectedServicios(chipGroupServicios)
            if (serviciosSeleccionados.isEmpty()) {
                Toast.makeText(context, "Selecciona al menos un servicio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val serviciosIds = serviciosSeleccionados.map { it.id }
            val serviciosJson = JSONArray(serviciosIds).toString()
            
            // Calcular precio total usando los precios calculados para el perro
            var precioTotal = 0.0
            serviciosSeleccionados.forEach { servicio ->
                val precio = if (preciosCalculados.containsKey(servicio.id)) {
                    preciosCalculados[servicio.id] ?: 0.0
                } else if (servicio.tipoPrecio == "fijo") {
                    servicio.precioBase
                } else {
                    0.0
                }
                precioTotal += precio
            }

            cita?.let { c ->
                val updated = c.copy(
                    clienteId = selectedCliente!!.id,
                    perroId = selectedPerro!!.id,
                    fecha = selectedFechaTimestamp!!,
                    hora = selectedHora!!,
                    serviciosIds = serviciosJson,
                    precioTotal = precioTotal,
                    notas = inputNotas.text?.toString() ?: ""
                )
                citaViewModel.updateCita(updated)
                Toast.makeText(context, "Cita actualizada", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
    }

    private fun getSelectedServicios(chipGroup: ChipGroup): List<Servicio> {
        val selected = mutableListOf<Servicio>()
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as? Chip
            if (chip?.isChecked == true && chip.tag is Servicio) {
                selected.add(chip.tag as Servicio)
            }
        }
        return selected
    }
    
    private fun updateChipsConPrecios(chipGroup: ChipGroup) {
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as? Chip
            val servicio = chip?.tag as? Servicio
            if (servicio != null) {
                val precio = preciosCalculados[servicio.id] ?: servicio.precioBase
                chip.text = "${servicio.nombre} - ${String.format("%.2f", precio)}€"
            }
        }
    }

    private fun updatePrecioTotal(chipGroup: ChipGroup, txtPrecio: TextView) {
        val serviciosSeleccionados = getSelectedServicios(chipGroup)
        var total = 0.0
        
        serviciosSeleccionados.forEach { servicio ->
            val precio = if (preciosCalculados.containsKey(servicio.id)) {
                preciosCalculados[servicio.id] ?: 0.0
            } else if (servicio.tipoPrecio == "fijo") {
                servicio.precioBase
            } else {
                0.0
            }
            total += precio
        }
        
        txtPrecio.text = "Total: ${String.format("%.2f", total)}€"
    }
    
    private fun loadPerrosForCliente(
        spinnerPerro: AutoCompleteTextView,
        chipGroup: ChipGroup,
        txtPrecio: TextView
    ) {
        selectedCliente?.let { cliente ->
            clienteViewModel.selectCliente(cliente)
            clienteViewModel.perrosDelCliente.observe(viewLifecycleOwner) { perroList ->
                perros = perroList
                val nombres = perroList.map { it.nombre }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, nombres)
                spinnerPerro.setAdapter(adapter)
                spinnerPerro.text.clear()
                selectedPerro = null
                
                spinnerPerro.setOnItemClickListener { _, _, position, _ ->
                    selectedPerro = perros[position]
                    recalcularPreciosServicios(chipGroup, txtPrecio)
                }
            }
        }
    }
    
    private fun loadPerrosForClienteInitial(
        perroIdInicial: Long,
        spinnerPerro: AutoCompleteTextView,
        chipGroup: ChipGroup,
        txtPrecio: TextView
    ) {
        selectedCliente?.let { cliente ->
            clienteViewModel.selectCliente(cliente)
            clienteViewModel.perrosDelCliente.observe(viewLifecycleOwner) { perroList ->
                perros = perroList
                val nombres = perroList.map { it.nombre }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, nombres)
                spinnerPerro.setAdapter(adapter)
                
                // Seleccionar el perro inicial
                val perroInicial = perroList.find { it.id == perroIdInicial }
                perroInicial?.let {
                    selectedPerro = it
                    spinnerPerro.setText(it.nombre, false)
                }
                
                spinnerPerro.setOnItemClickListener { _, _, position, _ ->
                    selectedPerro = perros[position]
                    recalcularPreciosServicios(chipGroup, txtPrecio)
                }
            }
        }
    }
    
    private fun recalcularPreciosServicios(chipGroup: ChipGroup, txtPrecio: TextView) {
        val perro = selectedPerro ?: return
        
        viewLifecycleOwner.lifecycleScope.launch {
            preciosCalculados.clear()
            
            servicios.forEach { servicio ->
                val precio = servicioViewModel.calcularPrecioParaPerro(
                    servicioId = servicio.id,
                    raza = perro.raza,
                    tamano = perro.tamano,
                    longitudPelo = perro.longitudPelo
                )
                preciosCalculados[servicio.id] = precio
            }
            
            updateChipsConPrecios(chipGroup)
            updatePrecioTotal(chipGroup, txtPrecio)
        }
    }
}
