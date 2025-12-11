package com.peluqueriacanina.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.chip.Chip
import com.peluqueriacanina.app.R
import com.peluqueriacanina.app.ui.adapter.CitaAdapter
import com.peluqueriacanina.app.viewmodel.CitaViewModel

class AgendaFragment : Fragment() {

    private val citaViewModel: CitaViewModel by activityViewModels()
    
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CitaAdapter
    private lateinit var emptyState: View
    private lateinit var txtCitasHoy: TextView
    private lateinit var txtIngresosHoy: TextView
    private lateinit var chipTodas: Chip
    private lateinit var chipHoy: Chip
    private lateinit var chipSemana: Chip

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

        adapter = CitaAdapter(
            onCitaClick = { cita ->
                // Handle cita click - show details dialog
            },
            onCitaComplete = { cita ->
                citaViewModel.completarCita(cita)
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
            selectChip(chipTodas)
            loadAllCitas() 
        }
        chipHoy.setOnClickListener { 
            selectChip(chipHoy)
            loadCitasHoy() 
        }
        chipSemana.setOnClickListener { 
            selectChip(chipSemana)
            loadCitasSemana() 
        }

        // Load initial data - "Todas" is selected by default
        selectChip(chipTodas)
        loadAllCitas()
        
        // Observe stats
        citaViewModel.citasHoy.observe(viewLifecycleOwner) { citas ->
            txtCitasHoy.text = citas.size.toString()
            val ingresos = citas.sumOf { it.precioTotal }
            txtIngresosHoy.text = "${ingresos}€"
        }
    }

    private fun loadAllCitas() {
        citaViewModel.allCitas.observe(viewLifecycleOwner) { citas ->
            updateUI(citas)
        }
    }

    private fun loadCitasHoy() {
        citaViewModel.citasHoy.observe(viewLifecycleOwner) { citas ->
            updateUI(citas)
        }
    }

    private fun loadCitasSemana() {
        citaViewModel.citasSemana.observe(viewLifecycleOwner) { citas ->
            updateUI(citas)
        }
    }

    private fun updateUI(citas: List<com.peluqueriacanina.app.data.Cita>) {
        adapter.submitList(citas)
        emptyState.visibility = if (citas.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (citas.isEmpty()) View.GONE else View.VISIBLE
    }
    
    private fun selectChip(selected: Chip) {
        chipTodas.isChecked = (selected == chipTodas)
        chipHoy.isChecked = (selected == chipHoy)
        chipSemana.isChecked = (selected == chipSemana)
    }
}
