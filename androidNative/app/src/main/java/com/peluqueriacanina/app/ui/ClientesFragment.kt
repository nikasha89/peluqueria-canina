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
                            longitudPelo = pelo
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
                perros.joinToString("\n") { "• ${it.nombre} (${it.raza})" }
            }

            AlertDialog.Builder(requireContext())
                .setTitle(cliente.nombre)
                .setMessage("""
                    📞 ${cliente.telefono.ifEmpty { "Sin teléfono" }}
                    ✉️ ${cliente.email.ifEmpty { "Sin email" }}
                    
                    🐕 Perros:
                    $perrosText
                """.trimIndent())
                .setPositiveButton("Cerrar", null)
                .setNeutralButton("Añadir perro") { _, _ ->
                    showAddPerroDialog(cliente)
                }
                .setNegativeButton("Eliminar") { _, _ ->
                    confirmDelete(cliente)
                }
                .show()
        }
    }

    private fun showAddPerroDialog(cliente: Cliente) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_perro, null)
        val inputNombre = dialogView.findViewById<EditText>(R.id.inputNombrePerroDialog)
        val inputRaza = dialogView.findViewById<EditText>(R.id.inputRazaDialog)
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
                    longitudPelo = pelo
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
