package com.peluqueriacanina.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.peluqueriacanina.app.R
import com.peluqueriacanina.app.data.Cliente

class ClienteAdapter(
    private val onClienteClick: (Cliente) -> Unit,
    private val onCallClick: (Cliente) -> Unit
) : ListAdapter<Cliente, ClienteAdapter.ClienteViewHolder>(ClienteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cliente, parent, false)
        return ClienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = getItem(position)
        holder.bind(cliente)
    }

    inner class ClienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtAvatar: TextView = itemView.findViewById(R.id.txtAvatar)
        private val txtNombre: TextView = itemView.findViewById(R.id.txtNombre)
        private val txtTelefono: TextView = itemView.findViewById(R.id.txtTelefono)
        private val txtPerros: TextView = itemView.findViewById(R.id.txtPerros)
        private val btnCall: ImageButton = itemView.findViewById(R.id.btnCall)

        fun bind(cliente: Cliente) {
            txtAvatar.text = cliente.nombre.firstOrNull()?.uppercase() ?: "?"
            txtNombre.text = cliente.nombre
            txtTelefono.text = "📞 ${cliente.telefono.ifEmpty { "Sin teléfono" }}"
            txtPerros.text = "-" // This would need to be populated separately

            itemView.setOnClickListener {
                onClienteClick(cliente)
            }

            btnCall.setOnClickListener {
                if (cliente.telefono.isNotEmpty()) {
                    onCallClick(cliente)
                }
            }
        }
    }

    class ClienteDiffCallback : DiffUtil.ItemCallback<Cliente>() {
        override fun areItemsTheSame(oldItem: Cliente, newItem: Cliente): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Cliente, newItem: Cliente): Boolean {
            return oldItem == newItem
        }
    }
}
