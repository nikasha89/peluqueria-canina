package com.peluqueriacanina.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.peluqueriacanina.app.R
import com.peluqueriacanina.app.data.Servicio

class ServicioAdapter(
    private val onServicioClick: (Servicio) -> Unit
) : ListAdapter<Servicio, ServicioAdapter.ServicioViewHolder>(ServicioDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_servicio, parent, false)
        return ServicioViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServicioViewHolder, position: Int) {
        val servicio = getItem(position)
        holder.bind(servicio)
    }

    inner class ServicioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtNombre: TextView = itemView.findViewById(R.id.txtNombre)
        private val txtDescripcion: TextView = itemView.findViewById(R.id.txtDescripcion)
        private val txtPrecio: TextView = itemView.findViewById(R.id.txtPrecio)
        private val chipTipoPrecio: Chip = itemView.findViewById(R.id.chipTipoPrecio)

        fun bind(servicio: Servicio) {
            txtNombre.text = servicio.nombre
            txtDescripcion.text = servicio.descripcion.ifEmpty { "Sin descripción" }
            txtPrecio.text = "${servicio.precioBase}€"
            
            chipTipoPrecio.text = if (servicio.tipoPrecio == "fijo") "Precio fijo" else "Precio por tamaño"

            itemView.setOnClickListener {
                onServicioClick(servicio)
            }
        }
    }

    class ServicioDiffCallback : DiffUtil.ItemCallback<Servicio>() {
        override fun areItemsTheSame(oldItem: Servicio, newItem: Servicio): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Servicio, newItem: Servicio): Boolean {
            return oldItem == newItem
        }
    }
}
