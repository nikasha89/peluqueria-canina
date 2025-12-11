package com.peluqueriacanina.app.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
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
import com.peluqueriacanina.app.ui.adapter.ClienteAdapter
import com.peluqueriacanina.app.viewmodel.ClienteViewModel

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
                showClienteDetails(cliente)
            },
            onCallClick = { cliente ->
                // Handle call - could open dialer
                Toast.makeText(context, "Llamando a ${cliente.telefono}", Toast.LENGTH_SHORT).show()
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
            showAddClienteDialog()
        }
    }

    private fun showAddClienteDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_cliente, null)
        val inputNombre = dialogView.findViewById<EditText>(R.id.inputNombreCliente)
        val inputTelefono = dialogView.findViewById<EditText>(R.id.inputTelefono)
        val inputEmail = dialogView.findViewById<EditText>(R.id.inputEmail)
        val inputPerro = dialogView.findViewById<EditText>(R.id.inputNombrePerro)
        val inputRaza = dialogView.findViewById<EditText>(R.id.inputRaza)
        val inputEdad = dialogView.findViewById<EditText>(R.id.inputEdadPerro)
        val spinnerTamano = dialogView.findViewById<Spinner>(R.id.spinnerTamano)
        val spinnerPelo = dialogView.findViewById<Spinner>(R.id.spinnerPelo)

        // Setup spinners
        val tamanos = listOf("mini", "pequeno", "mediano", "grande", "gigante")
        val pelos = listOf("corto", "medio", "largo")
        
        spinnerTamano?.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tamanos)
        spinnerPelo?.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, pelos)

        AlertDialog.Builder(requireContext())
            .setTitle("Nuevo Cliente")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = inputNombre.text.toString().trim()
                val telefono = inputTelefono.text.toString().trim()
                val email = inputEmail.text.toString().trim()
                val nombrePerro = inputPerro.text.toString().trim()
                val raza = inputRaza.text.toString().trim()
                val edadText = inputEdad?.text.toString().trim()
                val edad = edadText.toIntOrNull()
                val tamano = spinnerTamano?.selectedItem?.toString() ?: "mediano"
                val pelo = spinnerPelo?.selectedItem?.toString() ?: "medio"

                if (nombre.isEmpty()) {
                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val cliente = Cliente(
                    nombre = nombre,
                    telefono = telefono,
                    email = email
                )
                
                // Insert cliente and then perro
                clienteViewModel.insertCliente(cliente) { clienteId ->
                    if (nombrePerro.isNotEmpty()) {
                        val perro = Perro(
                            clienteId = clienteId,
                            nombre = nombrePerro,
                            raza = raza,
                            tamano = tamano,
                            longitudPelo = pelo,
                            edad = edad
                        )
                        clienteViewModel.insertPerro(perro)
                    }
                }
                Toast.makeText(context, "Cliente añadido correctamente", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showClienteDetails(cliente: Cliente) {
        clienteViewModel.selectCliente(cliente)
        clienteViewModel.perrosDelCliente.observe(viewLifecycleOwner) { perros ->
            val perrosText = if (perros.isEmpty()) {
                "Sin perros registrados"
            } else {
                perros.joinToString("\n") { perro ->
                    val edadText = perro.edad?.let { " - $it años" } ?: ""
                    "• ${perro.nombre} (${perro.raza})$edadText"
                }
            }

            val options = mutableListOf("✏️ Editar cliente", "➕ Añadir perro")
            perros.forEach { perro ->
                options.add("🐕 Editar ${perro.nombre}")
            }
            options.add("🗑️ Eliminar cliente")

            AlertDialog.Builder(requireContext())
                .setTitle(cliente.nombre)
                .setMessage("""
                    📞 ${cliente.telefono.ifEmpty { "Sin teléfono" }}
                    ✉️ ${cliente.email.ifEmpty { "Sin email" }}
                    
                    🐕 Perros:
                    $perrosText
                """.trimIndent())
                .setItems(options.toTypedArray()) { _, which ->
                    when {
                        which == 0 -> showEditClienteDialog(cliente)
                        which == 1 -> showAddPerroDialog(cliente)
                        which == options.size - 1 -> confirmDelete(cliente)
                        else -> {
                            val perroIndex = which - 2
                            if (perroIndex < perros.size) {
                                showEditPerroDialog(perros[perroIndex])
                            }
                        }
                    }
                }
                .setNegativeButton("Cerrar", null)
                .show()
        }
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
        val inputRaza = dialogView.findViewById<EditText>(R.id.inputRazaDialog)
        val inputEdad = dialogView.findViewById<EditText>(R.id.inputEdadDialog)
        val spinnerTamano = dialogView.findViewById<Spinner>(R.id.spinnerTamanoDialog)
        val spinnerPelo = dialogView.findViewById<Spinner>(R.id.spinnerPeloDialog)

        val tamanos = listOf("mini", "pequeno", "mediano", "grande", "gigante")
        val pelos = listOf("corto", "medio", "largo")
        
        spinnerTamano?.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tamanos)
        spinnerPelo?.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, pelos)
        
        // Pre-fill values
        inputNombre.setText(perro.nombre)
        inputRaza.setText(perro.raza)
        inputEdad?.setText(perro.edad?.toString() ?: "")
        spinnerTamano?.setSelection(tamanos.indexOf(perro.tamano).takeIf { it >= 0 } ?: 2)
        spinnerPelo?.setSelection(pelos.indexOf(perro.longitudPelo).takeIf { it >= 0 } ?: 1)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar ${perro.nombre}")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = inputNombre?.text.toString().trim()
                val raza = inputRaza?.text.toString().trim()
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
        val inputRaza = dialogView.findViewById<EditText>(R.id.inputRazaDialog)
        val inputEdad = dialogView.findViewById<EditText>(R.id.inputEdadDialog)
        val spinnerTamano = dialogView.findViewById<Spinner>(R.id.spinnerTamanoDialog)
        val spinnerPelo = dialogView.findViewById<Spinner>(R.id.spinnerPeloDialog)

        val tamanos = listOf("mini", "pequeno", "mediano", "grande", "gigante")
        val pelos = listOf("corto", "medio", "largo")
        
        spinnerTamano?.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tamanos)
        spinnerPelo?.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, pelos)

        AlertDialog.Builder(requireContext())
            .setTitle("Añadir perro a ${cliente.nombre}")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = inputNombre?.text.toString().trim()
                val raza = inputRaza?.text.toString().trim()
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
