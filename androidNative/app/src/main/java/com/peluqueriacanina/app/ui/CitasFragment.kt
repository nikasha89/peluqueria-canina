package com.peluqueriacanina.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CitasFragment : Fragment() {

    private val citaViewModel: CitaViewModel by activityViewModels()
    private val clienteViewModel: ClienteViewModel by activityViewModels()
    private val servicioViewModel: ServicioViewModel by activityViewModels()

    private lateinit var spinnerCliente: AutoCompleteTextView
    private lateinit var spinnerPerro: AutoCompleteTextView
    private lateinit var etFecha: TextInputEditText
    private lateinit var etHora: TextInputEditText
    private lateinit var chipGroupServicios: ChipGroup
    private lateinit var txtPrecioTotal: TextView
    private lateinit var etNotas: TextInputEditText
    private lateinit var btnGuardarCita: Button

    private var clientes: List<Cliente> = emptyList()
    private var perros: List<Perro> = emptyList()
    private var servicios: List<Servicio> = emptyList()
    private var selectedCliente: Cliente? = null
    private var selectedPerro: Perro? = null
    private var selectedFechaTimestamp: Long? = null
    private var selectedHora: String? = null
    private var selectedHourOfDay: Int = 10
    private var selectedMinuteOfHour: Int = 0

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val storageDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_citas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinnerCliente = view.findViewById(R.id.spinnerCliente)
        spinnerPerro = view.findViewById(R.id.spinnerPerro)
        etFecha = view.findViewById(R.id.etFecha)
        etHora = view.findViewById(R.id.etHora)
        chipGroupServicios = view.findViewById(R.id.chipGroupServicios)
        txtPrecioTotal = view.findViewById(R.id.txtPrecioTotal)
        etNotas = view.findViewById(R.id.etNotas)
        btnGuardarCita = view.findViewById(R.id.btnGuardarCita)

        setupDatePicker()
        setupTimePicker()
        loadClientes()
        loadServicios()

        btnGuardarCita.setOnClickListener {
            guardarCita()
        }
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        etFecha.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedFechaTimestamp = calendar.timeInMillis
                    etFecha.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupTimePicker() {
        etHora.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    selectedHourOfDay = hour
                    selectedMinuteOfHour = minute
                    selectedHora = String.format("%02d:%02d", hour, minute)
                    etHora.setText(selectedHora)
                },
                selectedHourOfDay,
                selectedMinuteOfHour,
                true
            ).show()
        }
    }

    private fun loadClientes() {
        clienteViewModel.allClientes.observe(viewLifecycleOwner) { clienteList ->
            clientes = clienteList
            val nombres = clienteList.map { it.nombre }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, nombres)
            spinnerCliente.setAdapter(adapter)

            spinnerCliente.setOnItemClickListener { _, _, position, _ ->
                selectedCliente = clientes[position]
                loadPerros()
            }
        }
    }

    private fun loadPerros() {
        selectedCliente?.let { cliente ->
            clienteViewModel.selectCliente(cliente)
            clienteViewModel.perrosDelCliente.observe(viewLifecycleOwner) { perroList ->
                perros = perroList
                val nombres = perroList.map { it.nombre }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, nombres)
                spinnerPerro.setAdapter(adapter)
                
                spinnerPerro.setOnItemClickListener { _, _, position, _ ->
                    selectedPerro = perros[position]
                }
            }
        }
    }

    private fun loadServicios() {
        servicioViewModel.allServicios.observe(viewLifecycleOwner) { servicioList ->
            servicios = servicioList.filter { it.activo }
            chipGroupServicios.removeAllViews()
            
            servicios.forEach { servicio ->
                val chip = Chip(requireContext()).apply {
                    text = "${servicio.nombre} - ${servicio.precioBase}€"
                    isCheckable = true
                    tag = servicio
                    setOnCheckedChangeListener { _, _ ->
                        updatePrecioTotal()
                    }
                }
                chipGroupServicios.addView(chip)
            }
        }
    }

    private fun getSelectedServicios(): List<Servicio> {
        val selected = mutableListOf<Servicio>()
        for (i in 0 until chipGroupServicios.childCount) {
            val chip = chipGroupServicios.getChildAt(i) as? Chip
            if (chip?.isChecked == true && chip.tag is Servicio) {
                selected.add(chip.tag as Servicio)
            }
        }
        return selected
    }

    private fun updatePrecioTotal() {
        val total = getSelectedServicios().sumOf { it.precioBase }
        txtPrecioTotal.text = String.format("%.2f €", total)
    }

    private fun guardarCita() {
        if (selectedCliente == null) {
            Toast.makeText(context, "Selecciona un cliente", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedPerro == null) {
            Toast.makeText(context, "Selecciona un perro", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedFechaTimestamp == null) {
            Toast.makeText(context, "Selecciona una fecha", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedHora == null) {
            Toast.makeText(context, "Selecciona una hora", Toast.LENGTH_SHORT).show()
            return
        }

        val serviciosSeleccionados = getSelectedServicios()
        if (serviciosSeleccionados.isEmpty()) {
            Toast.makeText(context, "Selecciona al menos un servicio", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert servicios list to JSON string
        val serviciosIds = serviciosSeleccionados.map { it.id }
        val serviciosJson = JSONArray(serviciosIds).toString()
        val precioTotal = serviciosSeleccionados.sumOf { it.precioBase }

        val cita = Cita(
            clienteId = selectedCliente!!.id,
            perroId = selectedPerro!!.id,
            fecha = selectedFechaTimestamp!!,
            hora = selectedHora!!,
            serviciosIds = serviciosJson,
            precioTotal = precioTotal,
            estado = "pendiente",
            notas = etNotas.text?.toString() ?: ""
        )

        citaViewModel.insertCita(cita)
        Toast.makeText(context, "Cita guardada correctamente", Toast.LENGTH_SHORT).show()
        
        // Clear form
        clearForm()
    }

    private fun clearForm() {
        spinnerCliente.text.clear()
        spinnerPerro.text.clear()
        etFecha.text?.clear()
        etHora.text?.clear()
        etNotas.text?.clear()
        
        for (i in 0 until chipGroupServicios.childCount) {
            (chipGroupServicios.getChildAt(i) as? Chip)?.isChecked = false
        }
        
        txtPrecioTotal.text = "0.00 €"
        selectedCliente = null
        selectedPerro = null
        selectedFechaTimestamp = null
        selectedHora = null
    }
}
