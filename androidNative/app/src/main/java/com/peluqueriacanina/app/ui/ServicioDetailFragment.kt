package com.peluqueriacanina.app.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.peluqueriacanina.app.R
import com.peluqueriacanina.app.data.LongitudPelo
import com.peluqueriacanina.app.data.PrecioServicio
import com.peluqueriacanina.app.data.Raza
import com.peluqueriacanina.app.data.Servicio
import com.peluqueriacanina.app.data.Tamano
import com.peluqueriacanina.app.viewmodel.ServicioViewModel
import kotlinx.coroutines.launch

class ServicioDetailFragment : DialogFragment() {

    private val servicioViewModel: ServicioViewModel by activityViewModels()
    
    private var servicio: Servicio? = null
    private var precios: List<PrecioServicio> = emptyList()
    private var razasList: List<Raza> = emptyList()
    private var tamanosList: List<Tamano> = emptyList()
    private var pelosList: List<LongitudPelo> = emptyList()
    
    private lateinit var inputNombre: TextInputEditText
    private lateinit var inputDescripcion: TextInputEditText
    private lateinit var radioGroupTipoPrecio: RadioGroup
    private lateinit var radioFijo: RadioButton
    private lateinit var radioVariable: RadioButton
    private lateinit var layoutPrecioFijo: TextInputLayout
    private lateinit var inputPrecio: TextInputEditText
    private lateinit var cardCombinaciones: MaterialCardView
    private lateinit var layoutCombinaciones: LinearLayout
    private lateinit var txtNoCombinaciones: TextView

    companion object {
        private const val ARG_SERVICIO_ID = "servicio_id"
        
        fun newInstance(servicioId: Long): ServicioDetailFragment {
            return ServicioDetailFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_SERVICIO_ID, servicioId)
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
        return inflater.inflate(R.layout.fragment_servicio_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        inputNombre = view.findViewById(R.id.inputNombre)
        inputDescripcion = view.findViewById(R.id.inputDescripcion)
        radioGroupTipoPrecio = view.findViewById(R.id.radioGroupTipoPrecio)
        radioFijo = view.findViewById(R.id.radioFijo)
        radioVariable = view.findViewById(R.id.radioVariable)
        layoutPrecioFijo = view.findViewById(R.id.layoutPrecioFijo)
        inputPrecio = view.findViewById(R.id.inputPrecio)
        cardCombinaciones = view.findViewById(R.id.cardCombinaciones)
        layoutCombinaciones = view.findViewById(R.id.layoutCombinaciones)
        txtNoCombinaciones = view.findViewById(R.id.txtNoCombinaciones)
        val btnGuardar = view.findViewById<MaterialButton>(R.id.btnGuardarServicio)
        val btnEliminar = view.findViewById<MaterialButton>(R.id.btnEliminarServicio)
        val btnAddCombinacion = view.findViewById<MaterialButton>(R.id.btnAddCombinacion)

        // Cerrar con X
        toolbar.setNavigationOnClickListener {
            dismiss()
        }

        // Cargar razas
        servicioViewModel.allRazas.observe(viewLifecycleOwner) { razas ->
            razasList = razas
        }
        
        // Cargar tamaños
        servicioViewModel.allTamanos.observe(viewLifecycleOwner) { tamanos ->
            tamanosList = tamanos
        }
        
        // Cargar longitudes de pelo
        servicioViewModel.allLongitudesPelo.observe(viewLifecycleOwner) { pelos ->
            pelosList = pelos
        }

        // Radio group listener
        radioGroupTipoPrecio.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioFijo -> {
                    layoutPrecioFijo.visibility = View.VISIBLE
                    cardCombinaciones.visibility = View.GONE
                }
                R.id.radioVariable -> {
                    layoutPrecioFijo.visibility = View.GONE
                    cardCombinaciones.visibility = View.VISIBLE
                }
            }
        }

        // Cargar servicio
        val servicioId = arguments?.getLong(ARG_SERVICIO_ID) ?: return
        
        servicioViewModel.allServicios.observe(viewLifecycleOwner) { servicios ->
            servicio = servicios.find { it.id == servicioId }
            servicio?.let { s ->
                toolbar.title = s.nombre
                inputNombre.setText(s.nombre)
                inputDescripcion.setText(s.descripcion)
                
                if (s.tipoPrecio == "variable") {
                    radioVariable.isChecked = true
                    layoutPrecioFijo.visibility = View.GONE
                    cardCombinaciones.visibility = View.VISIBLE
                } else {
                    radioFijo.isChecked = true
                    inputPrecio.setText(s.precioBase.toString())
                    layoutPrecioFijo.visibility = View.VISIBLE
                    cardCombinaciones.visibility = View.GONE
                }
            }
        }

        // Cargar precios/combinaciones
        lifecycleScope.launch {
            precios = servicioViewModel.getPreciosForServicio(servicioId)
            updateCombinacionesUI()
        }

        // Guardar cambios
        btnGuardar.setOnClickListener {
            guardarServicio()
        }

        // Eliminar servicio
        btnEliminar.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Eliminar servicio")
                .setMessage("¿Estás seguro de que quieres eliminar ${servicio?.nombre}?")
                .setPositiveButton("Eliminar") { _, _ ->
                    servicio?.let { s ->
                        servicioViewModel.deleteServicio(s)
                        Toast.makeText(context, "Servicio eliminado", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // Añadir combinación
        btnAddCombinacion.setOnClickListener {
            showAddCombinacionDialog()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
    }

    private fun updateCombinacionesUI() {
        layoutCombinaciones.removeAllViews()
        txtNoCombinaciones.visibility = if (precios.isEmpty()) View.VISIBLE else View.GONE

        precios.forEach { precio ->
            val precioView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_combinacion_precio, layoutCombinaciones, false)

            val razaText = precio.raza?.let { "$it - " } ?: ""
            precioView.findViewById<TextView>(R.id.txtTamano).text = "$razaText${precio.tamano.replaceFirstChar { it.uppercase() }}"
            precioView.findViewById<TextView>(R.id.txtPelo).text = "Pelo ${precio.longitudPelo}"
            precioView.findViewById<TextView>(R.id.txtPrecio).text = "${precio.precio}€"

            // Botón editar
            precioView.findViewById<ImageButton>(R.id.btnEditCombinacion).setOnClickListener {
                showEditCombinacionDialog(precio)
            }

            // Botón eliminar
            precioView.findViewById<ImageButton>(R.id.btnDeleteCombinacion).setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar combinación")
                    .setMessage("¿Eliminar esta combinación de precio?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        lifecycleScope.launch {
                            servicioViewModel.deletePrecioServicio(precio)
                            precios = servicioViewModel.getPreciosForServicio(servicio?.id ?: 0)
                            updateCombinacionesUI()
                            Toast.makeText(context, "Combinación eliminada", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }

            layoutCombinaciones.addView(precioView)
        }
    }

    private fun guardarServicio() {
        val nombre = inputNombre.text.toString().trim()
        if (nombre.isEmpty()) {
            Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        val esVariable = radioVariable.isChecked
        val precio = if (esVariable) {
            precios.firstOrNull()?.precio ?: 0.0
        } else {
            inputPrecio.text.toString().toDoubleOrNull() ?: 0.0
        }

        servicio?.let { s ->
            val updated = s.copy(
                nombre = nombre,
                descripcion = inputDescripcion.text.toString().trim(),
                tipoPrecio = if (esVariable) "variable" else "fijo",
                precioBase = precio
            )
            servicioViewModel.updateServicio(updated)
            Toast.makeText(context, "Servicio actualizado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddCombinacionDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_combinacion, null)

        val spinnerRaza = dialogView.findViewById<Spinner>(R.id.spinnerRaza)
        val spinnerTamano = dialogView.findViewById<Spinner>(R.id.spinnerTamano)
        val spinnerPelo = dialogView.findViewById<Spinner>(R.id.spinnerPelo)
        val inputPrecioComb = dialogView.findViewById<EditText>(R.id.inputPrecioCombinacion)

        // Razas (obligatoria)
        val razasNombres = razasList.map { it.nombre }
        
        val tamanosNombres = tamanosList.map { it.nombre }
        val pelosNombres = pelosList.map { it.nombre }

        spinnerRaza.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, razasNombres)
        spinnerTamano.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tamanosNombres)
        spinnerPelo.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, pelosNombres)

        // Seleccionar valores por defecto (si existen)
        val medianoIndex = tamanosNombres.indexOf("mediano")
        val medioIndex = pelosNombres.indexOf("medio")
        if (medianoIndex >= 0) spinnerTamano.setSelection(medianoIndex)
        if (medioIndex >= 0) spinnerPelo.setSelection(medioIndex)

        AlertDialog.Builder(requireContext())
            .setTitle("Añadir combinación")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                if (razasNombres.isEmpty()) {
                    Toast.makeText(context, "Primero debes crear razas en Configuración", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                
                val raza = spinnerRaza.selectedItem?.toString()
                if (raza.isNullOrEmpty()) {
                    Toast.makeText(context, "Selecciona una raza", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                val tamano = spinnerTamano.selectedItem.toString()
                val pelo = spinnerPelo.selectedItem.toString()
                val precioVal = inputPrecioComb.text.toString().toDoubleOrNull()

                if (precioVal == null || precioVal <= 0) {
                    Toast.makeText(context, "Introduce un precio válido", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Verificar si ya existe
                val existe = precios.any { it.raza == raza && it.tamano == tamano && it.longitudPelo == pelo }
                if (existe) {
                    Toast.makeText(context, "Esta combinación ya existe", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val nuevoPrecio = PrecioServicio(
                    servicioId = servicio?.id ?: return@setPositiveButton,
                    raza = raza,
                    tamano = tamano,
                    longitudPelo = pelo,
                    precio = precioVal
                )
                
                lifecycleScope.launch {
                    servicioViewModel.insertPrecioServicio(nuevoPrecio)
                    precios = servicioViewModel.getPreciosForServicio(servicio?.id ?: 0)
                    updateCombinacionesUI()
                    Toast.makeText(context, "Combinación añadida", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditCombinacionDialog(precio: PrecioServicio) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_combinacion, null)

        val spinnerRaza = dialogView.findViewById<Spinner>(R.id.spinnerRaza)
        val spinnerTamano = dialogView.findViewById<Spinner>(R.id.spinnerTamano)
        val spinnerPelo = dialogView.findViewById<Spinner>(R.id.spinnerPelo)
        val inputPrecioComb = dialogView.findViewById<EditText>(R.id.inputPrecioCombinacion)

        // Razas (obligatoria)
        val razasNombres = razasList.map { it.nombre }
        
        val tamanosNombres = tamanosList.map { it.nombre }
        val pelosNombres = pelosList.map { it.nombre }

        spinnerRaza.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, razasNombres)
        spinnerTamano.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tamanosNombres)
        spinnerPelo.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, pelosNombres)

        // Pre-fill
        val razaIndex = razasNombres.indexOf(precio.raza)
        if (razaIndex >= 0) spinnerRaza.setSelection(razaIndex)
        
        val tamanoIndex = tamanosNombres.indexOf(precio.tamano)
        if (tamanoIndex >= 0) spinnerTamano.setSelection(tamanoIndex)
        
        val peloIndex = pelosNombres.indexOf(precio.longitudPelo)
        if (peloIndex >= 0) spinnerPelo.setSelection(peloIndex)
        
        inputPrecioComb.setText(precio.precio.toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Editar combinación")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val raza = spinnerRaza.selectedItem?.toString()
                if (raza.isNullOrEmpty()) {
                    Toast.makeText(context, "Selecciona una raza", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                val tamano = spinnerTamano.selectedItem.toString()
                val pelo = spinnerPelo.selectedItem.toString()
                val precioVal = inputPrecioComb.text.toString().toDoubleOrNull()

                if (precioVal == null || precioVal <= 0) {
                    Toast.makeText(context, "Introduce un precio válido", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val updated = precio.copy(
                    raza = raza,
                    tamano = tamano,
                    longitudPelo = pelo,
                    precio = precioVal
                )
                
                lifecycleScope.launch {
                    servicioViewModel.updatePrecioServicio(updated)
                    precios = servicioViewModel.getPreciosForServicio(servicio?.id ?: 0)
                    updateCombinacionesUI()
                    Toast.makeText(context, "Combinación actualizada", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
