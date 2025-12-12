package com.peluqueriacanina.app.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.peluqueriacanina.app.R
import com.peluqueriacanina.app.data.Cliente
import com.peluqueriacanina.app.data.Perro
import com.peluqueriacanina.app.data.Raza
import com.peluqueriacanina.app.ui.adapter.ClienteAdapter
import com.peluqueriacanina.app.viewmodel.ClienteViewModel

// Data class para almacenar datos temporales de perro en el formulario
data class PerroFormData(
    var nombre: String = "",
    var raza: String = "",
    var tamano: String = "mediano",
    var longitudPelo: String = "medio",
    var edad: Int? = null,
    var foto: String? = null
)

class ClientesFragment : Fragment() {

    private val clienteViewModel: ClienteViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClienteAdapter
    private lateinit var emptyState: View
    private lateinit var etBuscar: TextInputEditText
    private lateinit var fab: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_clientes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerClientes)
        emptyState = view.findViewById(R.id.emptyState)
        etBuscar = view.findViewById(R.id.etBuscar)
        fab = view.findViewById(R.id.fabAddCliente)

        adapter = ClienteAdapter(
            onClienteClick = { cliente ->
                // Abrir pantalla completa de detalle
                val detailFragment = ClienteDetailFragment.newInstance(cliente.id)
                detailFragment.show(parentFragmentManager, "cliente_detail")
            },
            onCallClick = { cliente ->
                // Abrir marcador de teléfono
                if (cliente.telefono.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${cliente.telefono}")
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(context, "El cliente no tiene teléfono", Toast.LENGTH_SHORT).show()
                }
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        clienteViewModel.allClientes.observe(viewLifecycleOwner) { clientes ->
            adapter.submitList(clientes)
            emptyState.visibility = if (clientes.isEmpty()) View.VISIBLE else View.GONE
            recyclerView.visibility = if (clientes.isEmpty()) View.GONE else View.VISIBLE
        }

        etBuscar.addTextChangedListener { text ->
            val query = text?.toString() ?: ""
            if (query.isNotEmpty()) {
                clienteViewModel.searchClientes(query).observe(viewLifecycleOwner) { clientes ->
                    adapter.submitList(clientes)
                }
            } else {
                clienteViewModel.allClientes.observe(viewLifecycleOwner) { clientes ->
                    adapter.submitList(clientes)
                }
            }
        }

        fab.setOnClickListener {
            // Abrir pantalla de creación de cliente
            val createFragment = ClienteDetailFragment.newInstanceForCreate()
            createFragment.show(parentFragmentManager, "cliente_create")
        }
    }

    // Lista de razas cargadas de la BD
    private var razasList: List<Raza> = emptyList()

    private fun showAddClienteDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_cliente, null)
        val inputNombre = dialogView.findViewById<EditText>(R.id.inputNombreCliente)
        val inputTelefono = dialogView.findViewById<EditText>(R.id.inputTelefono)
        val inputEmail = dialogView.findViewById<EditText>(R.id.inputEmail)
        val layoutPerros = dialogView.findViewById<LinearLayout>(R.id.layoutPerros)
        val btnAddPerro = dialogView.findViewById<Button>(R.id.btnAddPerro)
        val txtNoPerros = dialogView.findViewById<TextView>(R.id.txtNoPerros)

        // Lista temporal de perros a añadir
        val perrosData = mutableListOf<PerroFormData>()
        val perroViews = mutableListOf<View>()

        // Cargar razas de la BD
        clienteViewModel.allRazas.observe(viewLifecycleOwner) { razas ->
            razasList = razas
        }

        fun updatePerrosUI() {
            txtNoPerros.visibility = if (perrosData.isEmpty()) View.VISIBLE else View.GONE
            // Actualizar números de perros
            perroViews.forEachIndexed { index, view ->
                view.findViewById<TextView>(R.id.txtPerroNumber).text = "🐕 Perro ${index + 1}"
            }
        }

        fun addPerroForm() {
            val perroData = PerroFormData()
            perrosData.add(perroData)
            
            val perroView = LayoutInflater.from(requireContext()).inflate(R.layout.item_perro_form, layoutPerros, false)
            perroViews.add(perroView)
            
            val inputNombrePerro = perroView.findViewById<EditText>(R.id.inputNombrePerro)
            val spinnerRaza = perroView.findViewById<Spinner>(R.id.spinnerRaza)
            val spinnerTamano = perroView.findViewById<Spinner>(R.id.spinnerTamano)
            val spinnerPelo = perroView.findViewById<Spinner>(R.id.spinnerPelo)
            val inputEdad = perroView.findViewById<EditText>(R.id.inputEdad)
            val btnRemove = perroView.findViewById<ImageButton>(R.id.btnRemovePerro)
            val btnSelectFoto = perroView.findViewById<Button>(R.id.btnSelectFoto)

            // Setup spinners
            val razasNombres = razasList.map { it.nombre }
            val tamanos = listOf("mini", "pequeno", "mediano", "grande", "gigante")
            val pelos = listOf("corto", "medio", "largo")
            
            spinnerRaza.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, razasNombres)
            spinnerTamano.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tamanos)
            spinnerPelo.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, pelos)
            
            // Set default selections
            spinnerTamano.setSelection(2) // mediano
            spinnerPelo.setSelection(1) // medio

            // Listeners para actualizar datos
            inputNombrePerro.addTextChangedListener { perroData.nombre = it.toString() }
            inputEdad.addTextChangedListener { perroData.edad = it.toString().toIntOrNull() }

            btnRemove.setOnClickListener {
                val index = perroViews.indexOf(perroView)
                if (index >= 0) {
                    perrosData.removeAt(index)
                    perroViews.removeAt(index)
                    layoutPerros.removeView(perroView)
                    updatePerrosUI()
                }
            }

            btnSelectFoto.setOnClickListener {
                Toast.makeText(context, "Función de foto próximamente", Toast.LENGTH_SHORT).show()
            }

            layoutPerros.addView(perroView)
            updatePerrosUI()
        }

        btnAddPerro.setOnClickListener {
            addPerroForm()
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Nuevo Cliente")
            .setView(dialogView)
            .setPositiveButton("Guardar", null) // Set null to override later
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val nombre = inputNombre.text.toString().trim()
                val telefono = inputTelefono.text.toString().trim()
                val email = inputEmail.text.toString().trim()

                if (nombre.isEmpty()) {
                    Toast.makeText(context, "El nombre del cliente es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Recoger datos de todos los perros
                perroViews.forEachIndexed { index, view ->
                    val perroData = perrosData[index]
                    perroData.nombre = view.findViewById<EditText>(R.id.inputNombrePerro).text.toString().trim()
                    perroData.raza = view.findViewById<Spinner>(R.id.spinnerRaza).selectedItem?.toString() ?: ""
                    perroData.tamano = view.findViewById<Spinner>(R.id.spinnerTamano).selectedItem?.toString() ?: "mediano"
                    perroData.longitudPelo = view.findViewById<Spinner>(R.id.spinnerPelo).selectedItem?.toString() ?: "medio"
                    perroData.edad = view.findViewById<EditText>(R.id.inputEdad).text.toString().toIntOrNull()
                }

                val cliente = Cliente(
                    nombre = nombre,
                    telefono = telefono,
                    email = email
                )
                
                clienteViewModel.insertCliente(cliente) { clienteId ->
                    perrosData.forEach { perroData ->
                        if (perroData.nombre.isNotEmpty()) {
                            val perro = Perro(
                                clienteId = clienteId,
                                nombre = perroData.nombre,
                                raza = perroData.raza,
                                tamano = perroData.tamano,
                                longitudPelo = perroData.longitudPelo,
                                edad = perroData.edad,
                                foto = perroData.foto
                            )
                            clienteViewModel.insertPerro(perro)
                        }
                    }
                }
                Toast.makeText(context, "Cliente añadido correctamente", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private var clienteDetailsDialog: AlertDialog? = null
    private var currentClienteId: Long = -1
    
    private fun showClienteDetails(cliente: Cliente) {
        // Dismiss any existing dialog
        clienteDetailsDialog?.dismiss()
        clienteDetailsDialog = null
        
        // Track current cliente to avoid duplicate dialogs
        if (currentClienteId == cliente.id) return
        currentClienteId = cliente.id
        
        // Use direct query instead of shared observable
        val liveData = clienteViewModel.getPerrosForCliente(cliente.id)
        val observer = object : androidx.lifecycle.Observer<List<Perro>> {
            override fun onChanged(perros: List<Perro>) {
                // Remove observer immediately
                liveData.removeObserver(this)
                currentClienteId = -1
                
                val perrosText = if (perros.isEmpty()) {
                    "Sin perros registrados"
                } else {
                    perros.joinToString("\n") { perro ->
                        val edadText = perro.edad?.let { " - $it años" } ?: ""
                        "• ${perro.nombre} (${perro.raza})$edadText"
                    }
                }

                // Build options list with info header
                val options = mutableListOf<String>()
                options.add("📞 ${cliente.telefono.ifEmpty { "Sin teléfono" }}")
                options.add("✉️ ${cliente.email.ifEmpty { "Sin email" }}")
                options.add("───────────────")
                options.add("✏️ Editar cliente")
                options.add("➕ Añadir perro")
                perros.forEachIndexed { index, perro ->
                    options.add("🐕 Editar: ${perro.nombre}")
                    options.add("🗑️ Eliminar: ${perro.nombre}")
                }
                options.add("🗑️ Eliminar cliente")

                clienteDetailsDialog = AlertDialog.Builder(requireContext())
                    .setTitle("${cliente.nombre}\n\n🐕 Perros: ${if (perros.isEmpty()) "Ninguno" else perros.joinToString(", ") { it.nombre }}")
                    .setItems(options.toTypedArray()) { _, which ->
                        when {
                            which < 3 -> { /* Info items, do nothing */ }
                            which == 3 -> showEditClienteDialog(cliente)
                            which == 4 -> showAddPerroDialog(cliente)
                            which == options.size - 1 -> confirmDelete(cliente)
                            else -> {
                                // Each perro has 2 options: edit and delete
                                val perroOptionIndex = which - 5
                                val perroIndex = perroOptionIndex / 2
                                val isDelete = perroOptionIndex % 2 == 1
                                
                                if (perroIndex >= 0 && perroIndex < perros.size) {
                                    if (isDelete) {
                                        confirmDeletePerro(perros[perroIndex])
                                    } else {
                                        showEditPerroDialog(perros[perroIndex])
                                    }
                                }
                            }
                        }
                    }
                    .setNegativeButton("Cerrar", null)
                    .setOnDismissListener { clienteDetailsDialog = null }
                    .create()
                
                clienteDetailsDialog?.show()
            }
        }
        
        liveData.observe(viewLifecycleOwner, observer)
    }
    
    private fun confirmDeletePerro(perro: Perro) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar perro")
            .setMessage("¿Estás seguro de que quieres eliminar a ${perro.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                clienteViewModel.deletePerro(perro)
                Toast.makeText(context, "Perro eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun showEditClienteDialog(cliente: Cliente) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_cliente, null)
        val inputNombre = dialogView.findViewById<EditText>(R.id.inputNombreCliente)
        val inputTelefono = dialogView.findViewById<EditText>(R.id.inputTelefono)
        val inputEmail = dialogView.findViewById<EditText>(R.id.inputEmail)
        val inputNotas = dialogView.findViewById<EditText>(R.id.inputNotas)
        
        // Pre-fill values
        inputNombre.setText(cliente.nombre)
        inputTelefono.setText(cliente.telefono)
        inputEmail.setText(cliente.email)
        inputNotas?.setText(cliente.notas)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Cliente")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = inputNombre.text.toString().trim()
                val telefono = inputTelefono.text.toString().trim()
                val email = inputEmail.text.toString().trim()
                val notas = inputNotas?.text.toString().trim() ?: ""

                if (nombre.isEmpty()) {
                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val updated = cliente.copy(
                    nombre = nombre,
                    telefono = telefono,
                    email = email,
                    notas = notas
                )
                clienteViewModel.updateCliente(updated)
                Toast.makeText(context, "Cliente actualizado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun showEditPerroDialog(perro: Perro) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_perro, null)
        val inputNombre = dialogView.findViewById<EditText>(R.id.inputNombrePerroDialog)
        val spinnerRaza = dialogView.findViewById<Spinner>(R.id.spinnerRazaDialog)
        val inputEdad = dialogView.findViewById<EditText>(R.id.inputEdadDialog)
        val spinnerTamano = dialogView.findViewById<Spinner>(R.id.spinnerTamanoDialog)
        val spinnerPelo = dialogView.findViewById<Spinner>(R.id.spinnerPeloDialog)

        val razasNombres = razasList.map { it.nombre }
        val tamanos = listOf("mini", "pequeno", "mediano", "grande", "gigante")
        val pelos = listOf("corto", "medio", "largo")
        
        spinnerRaza?.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, razasNombres)
        spinnerTamano?.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tamanos)
        spinnerPelo?.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, pelos)
        
        // Pre-fill values
        inputNombre.setText(perro.nombre)
        val razaIndex = razasNombres.indexOf(perro.raza)
        if (razaIndex >= 0) spinnerRaza?.setSelection(razaIndex)
        inputEdad?.setText(perro.edad?.toString() ?: "")
        spinnerTamano?.setSelection(tamanos.indexOf(perro.tamano).takeIf { it >= 0 } ?: 2)
        spinnerPelo?.setSelection(pelos.indexOf(perro.longitudPelo).takeIf { it >= 0 } ?: 1)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar ${perro.nombre}")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = inputNombre?.text.toString().trim()
                val raza = spinnerRaza?.selectedItem?.toString() ?: perro.raza
                val edadText = inputEdad?.text.toString().trim()
                val edad = edadText.toIntOrNull()
                val tamano = spinnerTamano?.selectedItem?.toString() ?: perro.tamano
                val pelo = spinnerPelo?.selectedItem?.toString() ?: perro.longitudPelo
                
                if (nombre.isEmpty()) {
                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val updated = perro.copy(
                    nombre = nombre,
                    raza = raza,
                    tamano = tamano,
                    longitudPelo = pelo,
                    edad = edad
                )
                clienteViewModel.updatePerro(updated)
                Toast.makeText(context, "Perro actualizado", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Eliminar") { _, _ ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar perro")
                    .setMessage("¿Estás seguro de que quieres eliminar a ${perro.nombre}?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        clienteViewModel.deletePerro(perro)
                        Toast.makeText(context, "Perro eliminado", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showAddPerroDialog(cliente: Cliente) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_perro, null)
        val inputNombre = dialogView.findViewById<EditText>(R.id.inputNombrePerroDialog)
        val spinnerRaza = dialogView.findViewById<Spinner>(R.id.spinnerRazaDialog)
        val inputEdad = dialogView.findViewById<EditText>(R.id.inputEdadDialog)
        val spinnerTamano = dialogView.findViewById<Spinner>(R.id.spinnerTamanoDialog)
        val spinnerPelo = dialogView.findViewById<Spinner>(R.id.spinnerPeloDialog)

        val razasNombres = razasList.map { it.nombre }
        val tamanos = listOf("mini", "pequeno", "mediano", "grande", "gigante")
        val pelos = listOf("corto", "medio", "largo")
        
        spinnerRaza?.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, razasNombres)
        spinnerTamano?.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tamanos)
        spinnerPelo?.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, pelos)

        AlertDialog.Builder(requireContext())
            .setTitle("Añadir perro a ${cliente.nombre}")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = inputNombre?.text.toString().trim()
                val raza = spinnerRaza?.selectedItem?.toString() ?: ""
                val edadText = inputEdad?.text.toString().trim()
                val edad = edadText.toIntOrNull()
                val tamano = spinnerTamano?.selectedItem?.toString() ?: "mediano"
                val pelo = spinnerPelo?.selectedItem?.toString() ?: "medio"
                
                if (nombre.isEmpty()) {
                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val perro = Perro(
                    clienteId = cliente.id,
                    nombre = nombre,
                    raza = raza,
                    tamano = tamano,
                    longitudPelo = pelo,
                    edad = edad
                )
                clienteViewModel.insertPerro(perro)
                Toast.makeText(context, "Perro añadido correctamente", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmDelete(cliente: Cliente) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar cliente")
            .setMessage("¿Estás seguro de que quieres eliminar a ${cliente.nombre}? Se eliminarán también sus perros y citas.")
            .setPositiveButton("Eliminar") { _, _ ->
                clienteViewModel.deleteCliente(cliente)
                Toast.makeText(context, "Cliente eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
