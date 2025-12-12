package com.peluqueriacanina.app.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.chip.Chip
import com.peluqueriacanina.app.R
import com.peluqueriacanina.app.data.Cita
import com.peluqueriacanina.app.data.CitaConDetalles
import com.peluqueriacanina.app.ui.adapter.CitaConDetallesAdapter
import com.peluqueriacanina.app.viewmodel.CitaViewModel

class AgendaFragment : Fragment() {

    private val citaViewModel: CitaViewModel by activityViewModels()
    
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CitaConDetallesAdapter
    private lateinit var emptyState: View
    private lateinit var txtCitasHoy: TextView
    private lateinit var txtIngresosHoy: TextView
    private lateinit var chipTodas: Chip
    private lateinit var chipHoy: Chip
    private lateinit var chipSemana: Chip
    
    private var currentFilter = "todas"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_agenda, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        recyclerView = view.findViewById(R.id.recyclerCitas)
        emptyState = view.findViewById(R.id.emptyState)
        txtCitasHoy = view.findViewById(R.id.txtCitasHoy)
        txtIngresosHoy = view.findViewById(R.id.txtIngresosHoy)
        chipTodas = view.findViewById(R.id.chipTodas)
        chipHoy = view.findViewById(R.id.chipHoy)
        chipSemana = view.findViewById(R.id.chipSemana)

        adapter = CitaConDetallesAdapter(
            onCitaClick = { citaConDetalles ->
                // Abrir pantalla completa de detalle
                val detailFragment = CitaDetailFragment.newInstance(citaConDetalles.cita.id)
                detailFragment.show(parentFragmentManager, "cita_detail")
            },
            onCitaLongClick = { citaConDetalles ->
                showQuickActions(citaConDetalles)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        swipeRefresh.setOnRefreshListener {
            citaViewModel.loadCitasHoy()
            citaViewModel.loadCitasSemana()
            swipeRefresh.isRefreshing = false
        }

        // Setup filter chips
        chipTodas.setOnClickListener { 
            currentFilter = "todas"
            selectChip(chipTodas)
        }
        chipHoy.setOnClickListener { 
            currentFilter = "hoy"
            selectChip(chipHoy)
        }
        chipSemana.setOnClickListener { 
            currentFilter = "semana"
            selectChip(chipSemana)
        }

        // Observe citas y cargar detalles
        citaViewModel.allCitas.observe(viewLifecycleOwner) { citas ->
            if (currentFilter == "todas") {
                citaViewModel.loadCitasConDetalles(citas)
                updateStats(citas)
            }
        }
        
        citaViewModel.citasHoy.observe(viewLifecycleOwner) { citas ->
            if (currentFilter == "hoy") {
                citaViewModel.loadCitasConDetalles(citas)
                updateStats(citas)
            }
        }
        
        citaViewModel.citasSemana.observe(viewLifecycleOwner) { citas ->
            if (currentFilter == "semana") {
                citaViewModel.loadCitasConDetalles(citas)
                updateStats(citas)
            }
        }
        
        // Observe citas con detalles
        citaViewModel.citasConDetalles.observe(viewLifecycleOwner) { citasConDetalles ->
            adapter.submitList(citasConDetalles)
            emptyState.visibility = if (citasConDetalles.isEmpty()) View.VISIBLE else View.GONE
            recyclerView.visibility = if (citasConDetalles.isEmpty()) View.GONE else View.VISIBLE
        }

        // Load initial data - "Todas" is selected by default
        selectChip(chipTodas)
    }
    
    private fun selectChip(selected: Chip) {
        chipTodas.isChecked = (selected == chipTodas)
        chipHoy.isChecked = (selected == chipHoy)
        chipSemana.isChecked = (selected == chipSemana)
        
        // Recargar según filtro
        when (selected) {
            chipTodas -> citaViewModel.allCitas.value?.let { 
                citaViewModel.loadCitasConDetalles(it)
                updateStats(it)
            }
            chipHoy -> citaViewModel.citasHoy.value?.let { 
                citaViewModel.loadCitasConDetalles(it)
                updateStats(it)
            }
            chipSemana -> citaViewModel.citasSemana.value?.let { 
                citaViewModel.loadCitasConDetalles(it)
                updateStats(it)
            }
        }
    }
    
    private fun updateStats(citas: List<Cita>) {
        txtCitasHoy.text = citas.size.toString()
        val ingresos = citas.sumOf { it.precioTotal }
        txtIngresosHoy.text = String.format("%.2f€", ingresos)
    }

    private fun showQuickActions(citaConDetalles: CitaConDetalles) {
        val cita = citaConDetalles.cita
        val options = mutableListOf<String>()
        
        if (cita.estado == "pendiente") {
            options.add("✅ Marcar como completada")
            options.add("❌ Cancelar cita")
        }
        options.add("🗑️ Eliminar cita")

        AlertDialog.Builder(requireContext())
            .setTitle("${citaConDetalles.perroNombre} - ${cita.hora}")
            .setItems(options.toTypedArray()) { _, which ->
                when (options[which]) {
                    "✅ Marcar como completada" -> {
                        citaViewModel.completarCita(cita)
                        Toast.makeText(context, "Cita completada", Toast.LENGTH_SHORT).show()
                    }
                    "❌ Cancelar cita" -> {
                        citaViewModel.cancelarCita(cita)
                        Toast.makeText(context, "Cita cancelada", Toast.LENGTH_SHORT).show()
                    }
                    "🗑️ Eliminar cita" -> confirmDeleteCita(cita)
                }
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    private fun confirmDeleteCita(cita: Cita) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar cita")
            .setMessage("¿Estás seguro de que quieres eliminar esta cita?")
            .setPositiveButton("Eliminar") { _, _ ->
                citaViewModel.deleteCita(cita)
                Toast.makeText(context, "Cita eliminada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
