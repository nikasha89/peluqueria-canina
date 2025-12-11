package com.peluqueriacanina.app.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.peluqueriacanina.app.R
import com.peluqueriacanina.app.data.Cliente
import com.peluqueriacanina.app.data.Perro
import com.peluqueriacanina.app.data.Raza
import com.peluqueriacanina.app.viewmodel.ClienteViewModel

class ClienteDetailFragment : DialogFragment() {

    private val clienteViewModel: ClienteViewModel by activityViewModels()
    
    private var cliente: Cliente? = null
    private var perros: List<Perro> = emptyList()
    private var razasList: List<Raza> = emptyList()
    
    private lateinit var inputNombre: TextInputEditText
    private lateinit var inputTelefono: TextInputEditText
    private lateinit var inputNotas: TextInputEditText
    private lateinit var layoutPerros: LinearLayout
    private lateinit var txtNoPerros: TextView

    companion object {
        private const val ARG_CLIENTE_ID = "cliente_id"
        
        fun newInstance(clienteId: Long): ClienteDetailFragment {
            return ClienteDetailFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_CLIENTE_ID, clienteId)
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
        return inflater.inflate(R.layout.fragment_cliente_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        inputNombre = view.findViewById(R.id.inputNombre)
        inputTelefono = view.findViewById(R.id.inputTelefono)
        inputNotas = view.findViewById(R.id.inputNotas)
        layoutPerros = view.findViewById(R.id.layoutPerros)
        txtNoPerros = view.findViewById(R.id.txtNoPerros)
        val btnGuardar = view.findViewById<MaterialButton>(R.id.btnGuardarCliente)
        val btnEliminar = view.findViewById<MaterialButton>(R.id.btnEliminarCliente)
        val btnAddPerro = view.findViewById<MaterialButton>(R.id.btnAddPerro)

        // Cerrar con X
        toolbar.setNavigationOnClickListener {
            dismiss()
        }

        // Cargar razas
        clienteViewModel.allRazas.observe(viewLifecycleOwner) { razas ->
            razasList = razas
        }

        // Cargar cliente
        val clienteId = arguments?.getLong(ARG_CLIENTE_ID) ?: return
        
        clienteViewModel.allClientes.observe(viewLifecycleOwner) { clientes ->
            cliente = clientes.find { it.id == clienteId }
            cliente?.let { c ->
                toolbar.title = c.nombre
                inputNombre.setText(c.nombre)
                inputTelefono.setText(c.telefono)
                inputNotas.setText(c.notas)
            }
        }

        // Cargar perros
        clienteViewModel.getPerrosForCliente(clienteId).observe(viewLifecycleOwner) { perrosList ->
            perros = perrosList
            updatePerrosUI()
        }

        // Guardar cambios cliente
        btnGuardar.setOnClickListener {
            val nombre = inputNombre.text.toString().trim()
            if (nombre.isEmpty()) {
                Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            cliente?.let { c ->
                val updated = c.copy(
                    nombre = nombre,
                    telefono = inputTelefono.text.toString().trim(),
                    notas = inputNotas.text.toString().trim()
                )
                clienteViewModel.updateCliente(updated)
                Toast.makeText(context, "Cliente actualizado", Toast.LENGTH_SHORT).show()
            }
        }

        // Eliminar cliente
        btnEliminar.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Eliminar cliente")
                .setMessage("¿Estás seguro de que quieres eliminar a ${cliente?.nombre}? Se eliminarán también todos sus perros.")
                .setPositiveButton("Eliminar") { _, _ ->
                    cliente?.let { c ->
                        clienteViewModel.deleteCliente(c)
                        Toast.makeText(context, "Cliente eliminado", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // Añadir perro
        btnAddPerro.setOnClickListener {
            showAddPerroDialog()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
    }

    private fun updatePerrosUI() {
        layoutPerros.removeAllViews()
        txtNoPerros.visibility = if (perros.isEmpty()) View.VISIBLE else View.GONE

        perros.forEach { perro ->
            val perroView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_perro_detail, layoutPerros, false)

            perroView.findViewById<TextView>(R.id.txtNombrePerro).text = perro.nombre
            perroView.findViewById<TextView>(R.id.txtRazaPerro).text = perro.raza
            perroView.findViewById<TextView>(R.id.txtTamano).text = perro.tamano.replaceFirstChar { it.uppercase() }
            perroView.findViewById<TextView>(R.id.txtPelo).text = perro.longitudPelo.replaceFirstChar { it.uppercase() }
            perroView.findViewById<TextView>(R.id.txtEdad).text = perro.edad?.let { "$it años" } ?: "-"

            // Botón editar
            perroView.findViewById<ImageButton>(R.id.btnEditPerro).setOnClickListener {
                showEditPerroDialog(perro)
            }

            // Botón eliminar
            perroView.findViewById<ImageButton>(R.id.btnDeletePerro).setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar perro")
                    .setMessage("¿Eliminar a ${perro.nombre}?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        clienteViewModel.deletePerro(perro)
                        Toast.makeText(context, "Perro eliminado", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }

            layoutPerros.addView(perroView)
        }
    }

    private fun showAddPerroDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_perro, null)

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

        spinnerTamano?.setSelection(2) // mediano
        spinnerPelo?.setSelection(1) // medio

        AlertDialog.Builder(requireContext())
            .setTitle("Añadir perro")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = inputNombre.text.toString().trim()
                if (nombre.isEmpty()) {
                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val perro = Perro(
                    clienteId = cliente?.id ?: return@setPositiveButton,
                    nombre = nombre,
                    raza = spinnerRaza?.selectedItem?.toString() ?: "",
                    tamano = spinnerTamano?.selectedItem?.toString() ?: "mediano",
                    longitudPelo = spinnerPelo?.selectedItem?.toString() ?: "medio",
                    edad = inputEdad.text.toString().toIntOrNull()
                )
                clienteViewModel.insertPerro(perro)
                Toast.makeText(context, "Perro añadido", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditPerroDialog(perro: Perro) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_perro, null)

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

        // Pre-fill
        inputNombre.setText(perro.nombre)
        inputEdad.setText(perro.edad?.toString() ?: "")
        
        val razaIndex = razasNombres.indexOf(perro.raza)
        if (razaIndex >= 0) spinnerRaza?.setSelection(razaIndex)
        
        val tamanoIndex = tamanos.indexOf(perro.tamano)
        if (tamanoIndex >= 0) spinnerTamano?.setSelection(tamanoIndex)
        
        val peloIndex = pelos.indexOf(perro.longitudPelo)
        if (peloIndex >= 0) spinnerPelo?.setSelection(peloIndex)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar ${perro.nombre}")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = inputNombre.text.toString().trim()
                if (nombre.isEmpty()) {
                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val updated = perro.copy(
                    nombre = nombre,
                    raza = spinnerRaza?.selectedItem?.toString() ?: perro.raza,
                    tamano = spinnerTamano?.selectedItem?.toString() ?: perro.tamano,
                    longitudPelo = spinnerPelo?.selectedItem?.toString() ?: perro.longitudPelo,
                    edad = inputEdad.text.toString().toIntOrNull()
                )
                clienteViewModel.updatePerro(updated)
                Toast.makeText(context, "Perro actualizado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
