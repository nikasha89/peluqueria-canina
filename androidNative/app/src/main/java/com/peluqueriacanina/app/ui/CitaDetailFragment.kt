package com.peluqueriacanina.app.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.peluqueriacanina.app.R
import com.peluqueriacanina.app.data.Cita
import com.peluqueriacanina.app.viewmodel.CitaViewModel
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CitaDetailFragment : DialogFragment() {

    private val citaViewModel: CitaViewModel by activityViewModels()
    
    private var cita: Cita? = null

    companion object {
        private const val ARG_CITA_ID = "cita_id"
        
        fun newInstance(citaId: Long): CitaDetailFragment {
            return CitaDetailFragment().apply {
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
        return inflater.inflate(R.layout.fragment_cita_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val cardEstado = view.findViewById<MaterialCardView>(R.id.cardEstado)
        val txtEstadoEmoji = view.findViewById<TextView>(R.id.txtEstadoEmoji)
        val txtEstado = view.findViewById<TextView>(R.id.txtEstado)
        val txtFechaHora = view.findViewById<TextView>(R.id.txtFechaHora)
        val txtPrecioTotal = view.findViewById<TextView>(R.id.txtPrecioTotal)
        val txtClienteNombre = view.findViewById<TextView>(R.id.txtClienteNombre)
        val txtClienteTelefono = view.findViewById<TextView>(R.id.txtClienteTelefono)
        val txtPerroNombre = view.findViewById<TextView>(R.id.txtPerroNombre)
        val txtPerroInfo = view.findViewById<TextView>(R.id.txtPerroInfo)
        val layoutServicios = view.findViewById<LinearLayout>(R.id.layoutServicios)
        val txtNotas = view.findViewById<TextView>(R.id.txtNotas)
        val btnEditar = view.findViewById<MaterialButton>(R.id.btnEditar)
        val btnCompletar = view.findViewById<MaterialButton>(R.id.btnCompletar)
        val btnCancelar = view.findViewById<MaterialButton>(R.id.btnCancelar)
        val btnEliminar = view.findViewById<MaterialButton>(R.id.btnEliminar)

        // Cerrar con X
        toolbar.setNavigationOnClickListener {
            dismiss()
        }

        val citaId = arguments?.getLong(ARG_CITA_ID) ?: return

        // Cargar cita y datos relacionados
        citaViewModel.allCitas.observe(viewLifecycleOwner) { citas ->
            cita = citas.find { it.id == citaId }
            cita?.let { c ->
                val dateFormat = SimpleDateFormat("EEEE dd 'de' MMMM", Locale("es", "ES"))
                val fechaStr = dateFormat.format(Date(c.fecha))

                // Estado
                when (c.estado) {
                    "pendiente" -> {
                        txtEstadoEmoji.text = "⏳"
                        txtEstado.text = "Pendiente"
                        cardEstado.setCardBackgroundColor(requireContext().getColor(android.R.color.white))
                    }
                    "completada" -> {
                        txtEstadoEmoji.text = "✅"
                        txtEstado.text = "Completada"
                        cardEstado.setCardBackgroundColor(requireContext().getColor(android.R.color.holo_green_light))
                    }
                    "cancelada" -> {
                        txtEstadoEmoji.text = "❌"
                        txtEstado.text = "Cancelada"
                        cardEstado.setCardBackgroundColor(requireContext().getColor(android.R.color.holo_red_light))
                    }
                }

                txtFechaHora.text = "$fechaStr a las ${c.hora}"
                txtPrecioTotal.text = "${c.precioTotal}€"
                txtNotas.text = c.notas.ifEmpty { "Sin notas" }

                toolbar.title = "Cita - ${c.hora}"

                // Cargar datos relacionados
                lifecycleScope.launch {
                    val cliente = citaViewModel.getClienteById(c.clienteId)
                    val perro = citaViewModel.getPerroById(c.perroId)

                    txtClienteNombre.text = cliente?.nombre ?: "Cliente desconocido"
                    txtClienteTelefono.text = "📞 ${cliente?.telefono ?: "Sin teléfono"}"
                    
                    // Hacer clickable para llamar
                    val telefono = cliente?.telefono
                    if (!telefono.isNullOrEmpty()) {
                        txtClienteTelefono.setOnClickListener {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$telefono")
                            }
                            startActivity(intent)
                        }
                        txtClienteTelefono.setTextColor(requireContext().getColor(android.R.color.holo_blue_dark))
                    }

                    txtPerroNombre.text = perro?.nombre ?: "Perro desconocido"
                    val perroDetails = listOfNotNull(
                        perro?.raza?.takeIf { it.isNotEmpty() },
                        perro?.tamano?.replaceFirstChar { it.uppercase() },
                        perro?.longitudPelo?.let { "Pelo $it" }
                    ).joinToString(" • ")
                    txtPerroInfo.text = perroDetails.ifEmpty { "Sin información" }

                    // Cargar servicios
                    layoutServicios.removeAllViews()
                    try {
                        val serviciosIds = JSONArray(c.serviciosIds)
                        for (i in 0 until serviciosIds.length()) {
                            val servicioId = serviciosIds.getLong(i)
                            val servicio = citaViewModel.getServicioById(servicioId)
                            servicio?.let { s ->
                                val servicioView = TextView(requireContext()).apply {
                                    text = "• ${s.nombre}"
                                    textSize = 16f
                                    setPadding(0, 4, 0, 4)
                                }
                                layoutServicios.addView(servicioView)
                            }
                        }
                    } catch (e: Exception) {
                        val errorView = TextView(requireContext()).apply {
                            text = "Sin servicios"
                            textSize = 14f
                        }
                        layoutServicios.addView(errorView)
                    }

                    if (layoutServicios.childCount == 0) {
                        val noServView = TextView(requireContext()).apply {
                            text = "Sin servicios programados"
                            textSize = 14f
                        }
                        layoutServicios.addView(noServView)
                    }
                }

                // Actualizar botones según estado
                btnEditar.isEnabled = c.estado == "pendiente"
                btnCompletar.isEnabled = c.estado == "pendiente"
                btnCancelar.isEnabled = c.estado == "pendiente"
            }
        }

        // Acciones
        btnEditar.setOnClickListener {
            cita?.let { c ->
                dismiss() // Cerrar el detalle
                val editFragment = CitaEditFragment.newInstance(c.id)
                editFragment.show(parentFragmentManager, "cita_edit")
            }
        }

        btnCompletar.setOnClickListener {
            cita?.let { c ->
                citaViewModel.completarCita(c)
                Toast.makeText(context, "Cita completada", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancelar.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Cancelar cita")
                .setMessage("¿Estás seguro de que quieres cancelar esta cita?")
                .setPositiveButton("Cancelar cita") { _, _ ->
                    cita?.let { c ->
                        citaViewModel.cancelarCita(c)
                        Toast.makeText(context, "Cita cancelada", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Volver", null)
                .show()
        }

        btnEliminar.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Eliminar cita")
                .setMessage("¿Estás seguro de que quieres eliminar esta cita? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar") { _, _ ->
                    cita?.let { c ->
                        citaViewModel.deleteCita(c)
                        Toast.makeText(context, "Cita eliminada", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
    }
}
