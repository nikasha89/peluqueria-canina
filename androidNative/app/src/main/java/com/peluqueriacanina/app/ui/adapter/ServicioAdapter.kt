package com.peluqueriacanina.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.peluqueriacanina.app.R
import com.peluqueriacanina.app.data.PrecioServicio
import com.peluqueriacanina.app.data.Servicio

class ServicioAdapter(
    private val onServicioClick: (Servicio) -> Unit
) : ListAdapter<Servicio, ServicioAdapter.ServicioViewHolder>(ServicioDiffCallback()) {

    private var preciosMap: Map<Long, List<PrecioServicio>> = emptyMap()
    private val expandedItems = mutableSetOf<Long>()

    fun setPreciosMap(precios: Map<Long, List<PrecioServicio>>) {
        this.preciosMap = precios
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_servicio, parent, false)
        return ServicioViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServicioViewHolder, position: Int) {
        val servicio = getItem(position)
        val precios = preciosMap[servicio.id] ?: emptyList()
        holder.bind(servicio, precios)
    }

    inner class ServicioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtNombre: TextView = itemView.findViewById(R.id.txtNombre)
        private val txtDescripcion: TextView = itemView.findViewById(R.id.txtDescripcion)
        private val txtPrecio: TextView = itemView.findViewById(R.id.txtPrecio)
        private val chipTipoPrecio: Chip = itemView.findViewById(R.id.chipTipoPrecio)
        private val layoutCombinacionesPreview: LinearLayout = itemView.findViewById(R.id.layoutCombinacionesPreview)
        private val txtCombinacionesResumen: TextView = itemView.findViewById(R.id.txtCombinacionesResumen)
        private val txtVerMas: TextView = itemView.findViewById(R.id.txtVerMas)

        fun bind(servicio: Servicio, precios: List<PrecioServicio>) {
            txtNombre.text = servicio.nombre
            txtDescripcion.text = servicio.descripcion.ifEmpty { "Sin descripción" }
            
            if (servicio.tipoPrecio == "fijo") {
                txtPrecio.text = "${servicio.precioBase}€"
                chipTipoPrecio.text = "Precio fijo"
                layoutCombinacionesPreview.visibility = View.GONE
            } else {
                // Precio variable - mostrar rango
                if (precios.isNotEmpty()) {
                    val minPrecio = precios.minOf { it.precio }
                    val maxPrecio = precios.maxOf { it.precio }
                    txtPrecio.text = if (minPrecio == maxPrecio) "${minPrecio}€" else "${minPrecio}-${maxPrecio}€"
                } else {
                    txtPrecio.text = "${servicio.precioBase}€"
                }
                chipTipoPrecio.text = "Precio por tamaño"
                
                // Mostrar combinaciones
                if (precios.isNotEmpty()) {
                    layoutCombinacionesPreview.visibility = View.VISIBLE
                    val isExpanded = expandedItems.contains(servicio.id)
                    val maxVisible = 3
                    
                    val combinacionesTexto = if (isExpanded || precios.size <= maxVisible) {
                        precios.joinToString("\n") { formatCombinacion(it) }
                    } else {
                        precios.take(maxVisible).joinToString("\n") { formatCombinacion(it) }
                    }
                    txtCombinacionesResumen.text = combinacionesTexto
                    
                    if (precios.size > maxVisible) {
                        txtVerMas.visibility = View.VISIBLE
                        txtVerMas.text = if (isExpanded) "▲ Ver menos" else "▼ Ver ${precios.size - maxVisible} más"
                        txtVerMas.setOnClickListener {
                            if (isExpanded) {
                                expandedItems.remove(servicio.id)
                            } else {
                                expandedItems.add(servicio.id)
                            }
                            notifyItemChanged(adapterPosition)
                        }
                    } else {
                        txtVerMas.visibility = View.GONE
                    }
                } else {
                    layoutCombinacionesPreview.visibility = View.GONE
                }
            }

            itemView.setOnClickListener {
                onServicioClick(servicio)
            }
        }
        
        private fun formatCombinacion(precio: PrecioServicio): String {
            val raza = precio.raza?.let { "$it · " } ?: ""
            val tamano = precio.tamano.replaceFirstChar { it.uppercase() }
            val pelo = precio.longitudPelo
            return "• $raza$tamano · Pelo $pelo → ${precio.precio}€"
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
