package com.peluqueriacanina.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.peluqueriacanina.app.R
import com.peluqueriacanina.app.data.CitaConDetalles
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CitaConDetallesAdapter(
    private val onCitaClick: (CitaConDetalles) -> Unit,
    private val onCitaLongClick: (CitaConDetalles) -> Unit
) : ListAdapter<CitaConDetalles, CitaConDetallesAdapter.CitaViewHolder>(CitaDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cita, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val citaConDetalles = getItem(position)
        holder.bind(citaConDetalles)
    }

    inner class CitaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtHora: TextView = itemView.findViewById(R.id.txtHora)
        private val txtFecha: TextView = itemView.findViewById(R.id.txtFecha)
        private val txtPerro: TextView = itemView.findViewById(R.id.txtPerro)
        private val txtCliente: TextView = itemView.findViewById(R.id.txtCliente)
        private val txtServicios: TextView = itemView.findViewById(R.id.txtServicios)
        private val txtPrecio: TextView = itemView.findViewById(R.id.txtPrecio)

        fun bind(citaConDetalles: CitaConDetalles) {
            val cita = citaConDetalles.cita
            
            txtHora.text = cita.hora
            txtFecha.text = dateFormat.format(Date(cita.fecha))
            txtPerro.text = "🐕 ${citaConDetalles.perroNombre}"
            txtCliente.text = citaConDetalles.clienteNombre
            
            // Mostrar servicios resumidos
            val serviciosText = when {
                citaConDetalles.serviciosNombres.isEmpty() -> "Sin servicios"
                citaConDetalles.serviciosNombres.size == 1 -> citaConDetalles.serviciosNombres[0]
                citaConDetalles.serviciosNombres.size <= 2 -> citaConDetalles.serviciosNombres.joinToString(" + ")
                else -> "${citaConDetalles.serviciosNombres.take(2).joinToString(" + ")} +${citaConDetalles.serviciosNombres.size - 2}"
            }
            txtServicios.text = serviciosText
            txtPrecio.text = "${cita.precioTotal}€"
            
            // Color según estado
            when (cita.estado) {
                "completada" -> {
                    itemView.alpha = 0.6f
                    txtHora.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                }
                "cancelada" -> {
                    itemView.alpha = 0.5f
                    txtHora.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
                }
                else -> {
                    itemView.alpha = 1.0f
                    txtHora.setTextColor(itemView.context.getColor(R.color.primary))
                }
            }

            itemView.setOnClickListener {
                onCitaClick(citaConDetalles)
            }
            
            itemView.setOnLongClickListener {
                onCitaLongClick(citaConDetalles)
                true
            }
        }
    }

    class CitaDiffCallback : DiffUtil.ItemCallback<CitaConDetalles>() {
        override fun areItemsTheSame(oldItem: CitaConDetalles, newItem: CitaConDetalles): Boolean {
            return oldItem.cita.id == newItem.cita.id
        }

        override fun areContentsTheSame(oldItem: CitaConDetalles, newItem: CitaConDetalles): Boolean {
            return oldItem == newItem
        }
    }
}
