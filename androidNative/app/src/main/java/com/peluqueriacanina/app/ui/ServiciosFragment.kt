package com.peluqueriacanina.app.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.peluqueriacanina.app.R
import com.peluqueriacanina.app.data.PrecioServicio
import com.peluqueriacanina.app.data.Servicio
import com.peluqueriacanina.app.ui.adapter.ServicioAdapter
import com.peluqueriacanina.app.viewmodel.ServicioViewModel

data class CombinacionPrecio(
    val raza: String,
    val tamano: String,
    val longitudPelo: String,
    val precio: Double
)

class ServiciosFragment : Fragment() {

    private val servicioViewModel: ServicioViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ServicioAdapter
    private lateinit var fab: FloatingActionButton
    
    private val combinaciones = mutableListOf<CombinacionPrecio>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_servicios, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerServicios)
        fab = view.findViewById(R.id.fabAddServicio)

        adapter = ServicioAdapter(
            onServicioClick = { servicio ->
                // Abrir pantalla completa de detalle
                val detailFragment = ServicioDetailFragment.newInstance(servicio.id)
                detailFragment.show(parentFragmentManager, "servicio_detail")
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        servicioViewModel.allServicios.observe(viewLifecycleOwner) { servicios ->
            adapter.submitList(servicios)
        }
        
        // Observar precios para mostrar en la lista
        servicioViewModel.allPrecios.observe(viewLifecycleOwner) { precios ->
            val preciosMap = precios.groupBy { it.servicioId }
            adapter.setPreciosMap(preciosMap)
        }

        fab.setOnClickListener {
            showAddServicioDialog()
        }
    }

    private fun showAddServicioDialog() {
        combinaciones.clear()
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_servicio, null)
        
        val inputNombre = dialogView.findViewById<EditText>(R.id.inputNombreServicio)
        val inputPrecio = dialogView.findViewById<EditText>(R.id.inputPrecio)
        val inputDescripcion = dialogView.findViewById<EditText>(R.id.inputDescripcion)
        val spinnerTipoPrecio = dialogView.findViewById<Spinner>(R.id.spinnerTipoPrecio)
        val layoutPrecioFijo = dialogView.findViewById<LinearLayout>(R.id.layoutPrecioFijo)
        val layoutPrecioVariable = dialogView.findViewById<LinearLayout>(R.id.layoutPrecioVariable)
        val spinnerRazaCombo = dialogView.findViewById<Spinner>(R.id.spinnerRazaCombo)
        val spinnerTamanoCombo = dialogView.findViewById<Spinner>(R.id.spinnerTamanoCombo)
        val spinnerPeloCombo = dialogView.findViewById<Spinner>(R.id.spinnerPeloCombo)
        val inputPrecioCombo = dialogView.findViewById<EditText>(R.id.inputPrecioCombo)
        val btnAddCombo = dialogView.findViewById<Button>(R.id.btnAddCombo)
        val txtCombinacionesLabel = dialogView.findViewById<TextView>(R.id.txtCombinacionesLabel)
        val layoutCombinaciones = dialogView.findViewById<LinearLayout>(R.id.layoutCombinaciones)

        // Setup tipo de precio spinner
        val tiposPrecio = listOf("Fijo", "Por Raza/Tamaño")
        spinnerTipoPrecio.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tiposPrecio)
        
        spinnerTipoPrecio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    layoutPrecioFijo.visibility = View.VISIBLE
                    layoutPrecioVariable.visibility = View.GONE
                } else {
                    layoutPrecioFijo.visibility = View.GONE
                    layoutPrecioVariable.visibility = View.VISIBLE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Setup raza spinner from database (sin opción "Todas las razas")
        val razasList = mutableListOf<String>()
        servicioViewModel.allRazas.observe(viewLifecycleOwner) { razas ->
            razasList.clear()
            razasList.addAll(razas.map { it.nombre })
            spinnerRazaCombo.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, razasList)
        }

        // Setup combinacion spinners
        val tamanos = listOf("mini", "pequeno", "mediano", "grande", "gigante")
        val pelos = listOf("corto", "medio", "largo")
        spinnerTamanoCombo.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tamanos)
        spinnerPeloCombo.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, pelos)

        // Add combination button
        btnAddCombo.setOnClickListener {
            val raza = spinnerRazaCombo.selectedItem?.toString() ?: ""
            val tamano = spinnerTamanoCombo.selectedItem.toString()
            val pelo = spinnerPeloCombo.selectedItem.toString()
            val precioStr = inputPrecioCombo.text.toString().trim()
            val precio = precioStr.toDoubleOrNull()
            
            if (precio == null || precio <= 0) {
                Toast.makeText(context, "Introduce un precio válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Check if combination already exists
            val exists = combinaciones.any { it.raza == raza && it.tamano == tamano && it.longitudPelo == pelo }
            if (exists) {
                Toast.makeText(context, "Esta combinación ya existe", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            combinaciones.add(CombinacionPrecio(raza, tamano, pelo, precio))
            updateCombinacionesUI(layoutCombinaciones, txtCombinacionesLabel)
            inputPrecioCombo.text.clear()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Nuevo Servicio")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = inputNombre.text.toString().trim()
                val descripcion = inputDescripcion.text.toString().trim()
                val tipoPrecio = if (spinnerTipoPrecio.selectedItemPosition == 0) "fijo" else "variable"

                if (nombre.isEmpty()) {
                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (tipoPrecio == "fijo") {
                    val precioStr = inputPrecio.text.toString().trim()
                    if (precioStr.isEmpty()) {
                        Toast.makeText(context, "El precio es obligatorio", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    
                    val servicio = Servicio(
                        nombre = nombre,
                        descripcion = descripcion,
                        tipoPrecio = "fijo",
                        precioBase = precioStr.toDoubleOrNull() ?: 0.0,
                        activo = true
                    )
                    servicioViewModel.insertServicio(servicio)
                } else {
                    if (combinaciones.isEmpty()) {
                        Toast.makeText(context, "Añade al menos una combinación de precio", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    
                    val servicio = Servicio(
                        nombre = nombre,
                        descripcion = descripcion,
                        tipoPrecio = "variable",
                        precioBase = combinaciones.firstOrNull()?.precio ?: 0.0,
                        activo = true
                    )
                    
                    servicioViewModel.insertServicioConPrecios(servicio, combinaciones.map { combo ->
                        PrecioServicio(
                            servicioId = 0, // Will be set in ViewModel
                            raza = combo.raza.ifBlank { null },
                            tamano = combo.tamano,
                            longitudPelo = combo.longitudPelo,
                            precio = combo.precio
                        )
                    })
                }
                Toast.makeText(context, "Servicio añadido correctamente", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun updateCombinacionesUI(layout: LinearLayout, label: TextView) {
        layout.removeAllViews()
        label.visibility = if (combinaciones.isEmpty()) View.GONE else View.VISIBLE
        
        combinaciones.forEachIndexed { index, combo ->
            val razaText = if (combo.raza.isBlank()) "" else "${combo.raza} - "
            val itemView = TextView(requireContext()).apply {
                text = "• $razaText${combo.tamano} / ${combo.longitudPelo}: ${combo.precio}€"
                textSize = 14f
                setPadding(0, 4, 0, 4)
                setOnClickListener {
                    combinaciones.removeAt(index)
                    updateCombinacionesUI(layout, label)
                }
            }
            layout.addView(itemView)
        }
    }

    private fun showEditServicioDialog(servicio: Servicio) {
        combinaciones.clear()
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_servicio, null)
        
        val inputNombre = dialogView.findViewById<EditText>(R.id.inputNombreServicio)
        val inputPrecio = dialogView.findViewById<EditText>(R.id.inputPrecio)
        val inputDescripcion = dialogView.findViewById<EditText>(R.id.inputDescripcion)
        val spinnerTipoPrecio = dialogView.findViewById<Spinner>(R.id.spinnerTipoPrecio)
        val layoutPrecioFijo = dialogView.findViewById<LinearLayout>(R.id.layoutPrecioFijo)
        val layoutPrecioVariable = dialogView.findViewById<LinearLayout>(R.id.layoutPrecioVariable)
        val spinnerRazaCombo = dialogView.findViewById<Spinner>(R.id.spinnerRazaCombo)
        val spinnerTamanoCombo = dialogView.findViewById<Spinner>(R.id.spinnerTamanoCombo)
        val spinnerPeloCombo = dialogView.findViewById<Spinner>(R.id.spinnerPeloCombo)
        val inputPrecioCombo = dialogView.findViewById<EditText>(R.id.inputPrecioCombo)
        val btnAddCombo = dialogView.findViewById<Button>(R.id.btnAddCombo)
        val txtCombinacionesLabel = dialogView.findViewById<TextView>(R.id.txtCombinacionesLabel)
        val layoutCombinaciones = dialogView.findViewById<LinearLayout>(R.id.layoutCombinaciones)

        // Pre-fill values
        inputNombre.setText(servicio.nombre)
        inputPrecio.setText(servicio.precioBase.toString())
        inputDescripcion.setText(servicio.descripcion)

        // Setup tipo de precio spinner
        val tiposPrecio = listOf("Fijo", "Por Raza/Tamaño")
        spinnerTipoPrecio.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tiposPrecio)
        spinnerTipoPrecio.setSelection(if (servicio.tipoPrecio == "fijo") 0 else 1)
        
        // Setup raza spinner from database (sin opción "Todas las razas")
        val razasList = mutableListOf<String>()
        servicioViewModel.allRazas.observe(viewLifecycleOwner) { razas ->
            razasList.clear()
            razasList.addAll(razas.map { it.nombre })
            spinnerRazaCombo.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, razasList)
        }
        
        // Show correct layout based on tipo
        if (servicio.tipoPrecio == "variable") {
            layoutPrecioFijo.visibility = View.GONE
            layoutPrecioVariable.visibility = View.VISIBLE
            
            // Load existing combinations
            servicioViewModel.getPreciosForServicioLive(servicio.id).observe(viewLifecycleOwner) { precios ->
                combinaciones.clear()
                precios.forEach { precio ->
                    combinaciones.add(CombinacionPrecio("", precio.tamano, precio.longitudPelo, precio.precio))
                }
                updateCombinacionesUI(layoutCombinaciones, txtCombinacionesLabel)
            }
        }
        
        spinnerTipoPrecio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    layoutPrecioFijo.visibility = View.VISIBLE
                    layoutPrecioVariable.visibility = View.GONE
                } else {
                    layoutPrecioFijo.visibility = View.GONE
                    layoutPrecioVariable.visibility = View.VISIBLE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Setup combinacion spinners
        val tamanos = listOf("mini", "pequeno", "mediano", "grande", "gigante")
        val pelos = listOf("corto", "medio", "largo")
        spinnerTamanoCombo.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tamanos)
        spinnerPeloCombo.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, pelos)

        // Add combination button
        btnAddCombo.setOnClickListener {
            val raza = spinnerRazaCombo.selectedItem?.toString() ?: ""
            val tamano = spinnerTamanoCombo.selectedItem.toString()
            val pelo = spinnerPeloCombo.selectedItem.toString()
            val precioStr = inputPrecioCombo.text.toString().trim()
            val precio = precioStr.toDoubleOrNull()
            
            if (precio == null || precio <= 0) {
                Toast.makeText(context, "Introduce un precio válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val exists = combinaciones.any { it.raza == raza && it.tamano == tamano && it.longitudPelo == pelo }
            if (exists) {
                Toast.makeText(context, "Esta combinación ya existe", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            combinaciones.add(CombinacionPrecio(raza, tamano, pelo, precio))
            updateCombinacionesUI(layoutCombinaciones, txtCombinacionesLabel)
            inputPrecioCombo.text.clear()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Servicio")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = inputNombre.text.toString().trim()
                val descripcion = inputDescripcion.text.toString().trim()
                val tipoPrecio = if (spinnerTipoPrecio.selectedItemPosition == 0) "fijo" else "variable"

                if (nombre.isEmpty()) {
                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (tipoPrecio == "fijo") {
                    val precioStr = inputPrecio.text.toString().trim()
                    val updated = servicio.copy(
                        nombre = nombre,
                        tipoPrecio = "fijo",
                        precioBase = precioStr.toDoubleOrNull() ?: servicio.precioBase,
                        descripcion = descripcion
                    )
                    servicioViewModel.updateServicio(updated)
                } else {
                    if (combinaciones.isEmpty()) {
                        Toast.makeText(context, "Añade al menos una combinación de precio", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    
                    val updated = servicio.copy(
                        nombre = nombre,
                        tipoPrecio = "variable",
                        precioBase = combinaciones.firstOrNull()?.precio ?: servicio.precioBase,
                        descripcion = descripcion
                    )
                    
                    servicioViewModel.updateServicioConPrecios(updated, combinaciones.map { combo ->
                        PrecioServicio(
                            servicioId = servicio.id,
                            raza = combo.raza.ifBlank { null },
                            tamano = combo.tamano,
                            longitudPelo = combo.longitudPelo,
                            precio = combo.precio
                        )
                    })
                }
                Toast.makeText(context, "Servicio actualizado", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Eliminar") { _, _ ->
                confirmDelete(servicio)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmDelete(servicio: Servicio) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar servicio")
            .setMessage("¿Estás seguro de que quieres eliminar ${servicio.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                servicioViewModel.deleteServicio(servicio)
                Toast.makeText(context, "Servicio eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
