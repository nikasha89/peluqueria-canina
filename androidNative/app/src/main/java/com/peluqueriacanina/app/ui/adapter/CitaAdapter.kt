package com.peluqueriacanina.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.peluqueriacanina.app.R
import com.peluqueriacanina.app.data.Cita
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CitaAdapter(
    private val onCitaClick: (Cita) -> Unit,
    private val onCitaComplete: (Cita) -> Unit
) : ListAdapter<Cita, CitaAdapter.CitaViewHolder>(CitaDiffCallback()) {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cita, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val cita = getItem(position)
        holder.bind(cita)
    }

    inner class CitaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtHora: TextView = itemView.findViewById(R.id.txtHora)
        private val txtFecha: TextView = itemView.findViewById(R.id.txtFecha)
        private val txtPerro: TextView = itemView.findViewById(R.id.txtPerro)
        private val txtCliente: TextView = itemView.findViewById(R.id.txtCliente)
        private val txtServicios: TextView = itemView.findViewById(R.id.txtServicios)
        private val txtPrecio: TextView = itemView.findViewById(R.id.txtPrecio)

        fun bind(cita: Cita) {
            txtHora.text = cita.hora
            txtFecha.text = dateFormat.format(Date(cita.fecha))
            txtPerro.text = "🐕 Perro #${cita.perroId}"
            txtCliente.text = "Cliente #${cita.clienteId}"
            txtServicios.text = "Servicios programados"
            txtPrecio.text = "${cita.precioTotal}€"

            itemView.setOnClickListener {
                onCitaClick(cita)
            }
            
            itemView.setOnLongClickListener {
                onCitaComplete(cita)
                true
            }
        }
    }

    class CitaDiffCallback : DiffUtil.ItemCallback<Cita>() {
        override fun areItemsTheSame(oldItem: Cita, newItem: Cita): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Cita, newItem: Cita): Boolean {
            return oldItem == newItem
        }
    }
}
