package com.peluqueriacanina.app.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.peluqueriacanina.app.R
import com.peluqueriacanina.app.data.Servicio
import com.peluqueriacanina.app.ui.adapter.ServicioAdapter
import com.peluqueriacanina.app.viewmodel.ServicioViewModel

class ServiciosFragment : Fragment() {

    private val servicioViewModel: ServicioViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ServicioAdapter
    private lateinit var fab: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_servicios, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerServicios)
        fab = view.findViewById(R.id.fabAddServicio)

        adapter = ServicioAdapter(
            onServicioClick = { servicio ->
                showEditServicioDialog(servicio)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        servicioViewModel.allServicios.observe(viewLifecycleOwner) { servicios ->
            adapter.submitList(servicios)
        }

        fab.setOnClickListener {
            showAddServicioDialog()
        }
    }

    private fun showAddServicioDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_servicio, null)
        val inputNombre = dialogView.findViewById<EditText>(R.id.inputNombreServicio)
        val inputPrecio = dialogView.findViewById<EditText>(R.id.inputPrecio)
        val inputDescripcion = dialogView.findViewById<EditText>(R.id.inputDescripcion)

        AlertDialog.Builder(requireContext())
            .setTitle("Nuevo Servicio")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = inputNombre.text.toString().trim()
                val precioStr = inputPrecio.text.toString().trim()
                val descripcion = inputDescripcion.text.toString().trim()

                if (nombre.isEmpty() || precioStr.isEmpty()) {
                    Toast.makeText(context, "Nombre y precio son obligatorios", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val servicio = Servicio(
                    nombre = nombre,
                    descripcion = descripcion,
                    tipoPrecio = "fijo",
                    precioBase = precioStr.toDoubleOrNull() ?: 0.0,
                    activo = true
                )
                
                servicioViewModel.insertServicio(servicio)
                Toast.makeText(context, "Servicio añadido correctamente", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditServicioDialog(servicio: Servicio) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_servicio, null)
        val inputNombre = dialogView.findViewById<EditText>(R.id.inputNombreServicio)
        val inputPrecio = dialogView.findViewById<EditText>(R.id.inputPrecio)
        val inputDescripcion = dialogView.findViewById<EditText>(R.id.inputDescripcion)

        // Pre-fill values
        inputNombre.setText(servicio.nombre)
        inputPrecio.setText(servicio.precioBase.toString())
        inputDescripcion.setText(servicio.descripcion)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Servicio")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = inputNombre.text.toString().trim()
                val precioStr = inputPrecio.text.toString().trim()
                val descripcion = inputDescripcion.text.toString().trim()

                if (nombre.isEmpty() || precioStr.isEmpty()) {
                    Toast.makeText(context, "Nombre y precio son obligatorios", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val updated = servicio.copy(
                    nombre = nombre,
                    precioBase = precioStr.toDoubleOrNull() ?: servicio.precioBase,
                    descripcion = descripcion
                )
                
                servicioViewModel.updateServicio(updated)
                Toast.makeText(context, "Servicio actualizado", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Eliminar") { _, _ ->
                confirmDelete(servicio)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmDelete(servicio: Servicio) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar servicio")
            .setMessage("¿Estás seguro de que quieres eliminar ${servicio.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                servicioViewModel.deleteServicio(servicio)
                Toast.makeText(context, "Servicio eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
