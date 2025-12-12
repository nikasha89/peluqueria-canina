package com.peluqueriacanina.app.ui

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.exifinterface.media.ExifInterface
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
import java.io.ByteArrayOutputStream

class ClienteDetailFragment : DialogFragment() {

    private val clienteViewModel: ClienteViewModel by activityViewModels()
    
    private var cliente: Cliente? = null
    private var perros: List<Perro> = emptyList()
    private var razasList: List<Raza> = emptyList()
    
    // Para selección de foto
    private var currentFotoBase64: String? = null
    private var currentImgPreview: ImageView? = null
    
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { handleImageSelected(it) }
    }
    
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
            
            // Mostrar imagen del perro
            val imgPerro = perroView.findViewById<ImageView>(R.id.imgPerro)
            if (!perro.foto.isNullOrEmpty()) {
                try {
                    val imageBytes = Base64.decode(perro.foto, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    if (bitmap != null) {
                        imgPerro.setImageBitmap(bitmap)
                        imgPerro.setPadding(0, 0, 0, 0)
                    }
                } catch (e: Exception) {
                    // Usar imagen por defecto
                }
            }

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

    private fun handleImageSelected(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            var bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (bitmap != null) {
                // Corregir orientación EXIF
                bitmap = correctImageOrientation(uri, bitmap)
                
                // Comprimir la imagen a un tamaño manejable
                val maxSize = 400
                val ratio = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height, 1f)
                val width = (bitmap.width * ratio).toInt()
                val height = (bitmap.height * ratio).toInt()
                
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
                
                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                val byteArray = outputStream.toByteArray()
                
                currentFotoBase64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)
                
                // Mostrar preview
                currentImgPreview?.setImageBitmap(scaledBitmap)
                currentImgPreview?.visibility = View.VISIBLE
                
                if (bitmap != scaledBitmap) bitmap.recycle()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun correctImageOrientation(uri: Uri, bitmap: Bitmap): Bitmap {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                val exif = ExifInterface(stream)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                
                val rotation = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
                
                if (rotation != 0f) {
                    val matrix = Matrix()
                    matrix.postRotate(rotation)
                    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                }
            }
        } catch (e: Exception) {
            // Ignorar errores de EXIF
        }
        return bitmap
    }
    
    private fun showAddPerroDialog() {
        currentFotoBase64 = null
        
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_perro, null)

        val inputNombre = dialogView.findViewById<EditText>(R.id.inputNombrePerroDialog)
        val spinnerRaza = dialogView.findViewById<Spinner>(R.id.spinnerRazaDialog)
        val inputEdad = dialogView.findViewById<EditText>(R.id.inputEdadDialog)
        val spinnerTamano = dialogView.findViewById<Spinner>(R.id.spinnerTamanoDialog)
        val spinnerPelo = dialogView.findViewById<Spinner>(R.id.spinnerPeloDialog)
        val btnSelectFoto = dialogView.findViewById<MaterialButton>(R.id.btnSelectFotoDialog)
        val imgPreview = dialogView.findViewById<ImageView>(R.id.imgPreviewFotoDialog)
        
        currentImgPreview = imgPreview

        val razasNombres = razasList.map { it.nombre }
        val tamanos = listOf("mini", "pequeno", "mediano", "grande", "gigante")
        val pelos = listOf("corto", "medio", "largo")

        spinnerRaza?.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, razasNombres)
        spinnerTamano?.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tamanos)
        spinnerPelo?.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, pelos)

        spinnerTamano?.setSelection(2) // mediano
        spinnerPelo?.setSelection(1) // medio
        
        // Configurar botón de selección de foto
        btnSelectFoto?.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

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
                    edad = inputEdad.text.toString().toIntOrNull(),
                    foto = currentFotoBase64
                )
                clienteViewModel.insertPerro(perro)
                Toast.makeText(context, "Perro añadido", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditPerroDialog(perro: Perro) {
        currentFotoBase64 = perro.foto
        
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_perro, null)

        val inputNombre = dialogView.findViewById<EditText>(R.id.inputNombrePerroDialog)
        val spinnerRaza = dialogView.findViewById<Spinner>(R.id.spinnerRazaDialog)
        val inputEdad = dialogView.findViewById<EditText>(R.id.inputEdadDialog)
        val spinnerTamano = dialogView.findViewById<Spinner>(R.id.spinnerTamanoDialog)
        val spinnerPelo = dialogView.findViewById<Spinner>(R.id.spinnerPeloDialog)
        val btnSelectFoto = dialogView.findViewById<MaterialButton>(R.id.btnSelectFotoDialog)
        val imgPreview = dialogView.findViewById<ImageView>(R.id.imgPreviewFotoDialog)
        
        currentImgPreview = imgPreview

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
        
        // Mostrar foto existente si la hay
        if (!perro.foto.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(perro.foto, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                if (bitmap != null) {
                    imgPreview?.setImageBitmap(bitmap)
                    imgPreview?.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                // Ignorar error de decodificación
            }
        }
        
        // Configurar botón de selección de foto
        btnSelectFoto?.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

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
                    edad = inputEdad.text.toString().toIntOrNull(),
                    foto = currentFotoBase64
                )
                clienteViewModel.updatePerro(updated)
                Toast.makeText(context, "Perro actualizado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
