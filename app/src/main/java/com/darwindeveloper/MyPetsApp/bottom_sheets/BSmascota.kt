package com.darwindeveloper.MyPetsApp.bottom_sheets

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.darwindeveloper.MyPetsApp.R
import com.darwindeveloper.MyPetsApp.adapters.EstablecimientosAdapter
import com.darwindeveloper.MyPetsApp.adapters.EventosMascotaAdapter
import com.darwindeveloper.MyPetsApp.api.Constants
import com.darwindeveloper.MyPetsApp.api.WebApiClient
import com.darwindeveloper.MyPetsApp.api.WebService
import com.darwindeveloper.MyPetsApp.api.modelos.Cita
import com.darwindeveloper.MyPetsApp.api.modelos.Establecimiento
import com.darwindeveloper.MyPetsApp.api.modelos.Mascota
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.bottom_sheet_mascota.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by DARWIN MOROCHO on 24/8/2017.
 */
class BSmascota : BottomSheetDialogFragment() {

    private var mascota_id: String? = null
    private var rootView: View? = null

    private var vivo = true

    private var dataPetTask: DataPet? = null
    private var mLoadDatatask: LoadData? = null
    private var mLoadcDatatask: LoadcTask? = null

    private val establecimientos: ArrayList<Establecimiento> = ArrayList()
    private val eventos: ArrayList<Cita> = ArrayList()
    private var adapterEstablecimientos: EstablecimientosAdapter? = null
    private var adapterEvents: EventosMascotaAdapter? = null


    private val tamanios = arrayOf("grande", "mediano", "pequeño", "miniatura")
    private val sexos = arrayOf("macho", "hembra")
    private val estados = arrayOf("vivo", "muerto")
    private val esterilizados = arrayOf("NO", "SI")
    private val microchips = arrayOf("NO", "SI")

    companion object {
        const val MASCOTA_ID = "BSmascota.mascota_id"

        fun newInstance(args: Bundle): BSmascota {
            val bsm = BSmascota()
            bsm.arguments = args
            return bsm
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mascota_id = arguments[MASCOTA_ID] as String
    }


    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog?, style: Int) {
        super.setupDialog(dialog, style)
        rootView = View.inflate(context, R.layout.bottom_sheet_mascota, null)
        dialog?.setContentView(rootView)


        adapterEstablecimientos = EstablecimientosAdapter(context, establecimientos)
        adapterEvents = EventosMascotaAdapter(context, eventos)
        rootView!!.bsm_establecimientos.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rootView!!.bsm_list_eventos.layoutManager = LinearLayoutManager(context)
        rootView!!.bsm_establecimientos.adapter = adapterEstablecimientos
        rootView!!.bsm_list_eventos.adapter = adapterEvents


        val adapterSpinnertamanios = ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, tamanios)
        adapterSpinnertamanios.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootView!!.bsm_spinner_tamanio.adapter = adapterSpinnertamanios

        val adapterSpinnerSexos = ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, sexos)
        adapterSpinnerSexos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootView!!.bsm_spinner_sexo.adapter = adapterSpinnerSexos

        val adapterSpinnerEstado = ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, estados)
        adapterSpinnerEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootView!!.bsm_spinner_estado.adapter = adapterSpinnerEstado


        val adapterSpinnerMicrochip = ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, microchips)
        adapterSpinnerMicrochip.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootView!!.bsm_spinner_microchip.adapter = adapterSpinnerMicrochip


        val adapterSpinnerEsterilizado = ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, esterilizados)
        adapterSpinnerEsterilizado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootView!!.bsm_spinner_esterilizado.adapter = adapterSpinnerEsterilizado


        rootView!!.bsm_spinner_estado.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position == 0) {
                    vivo = true;
                } else {
                    vivo = false;
                }

                if (!vivo) {
                    rootView!!.bsm_input_muerte.visibility = View.VISIBLE
                } else {
                    rootView!!.bsm_input_muerte.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        })


        rootView!!.bsm_fab_edit.setOnClickListener() {
            showEdit()
        }

        if (dataPetTask != null)
            dataPetTask = null
        dataPetTask = DataPet()
        dataPetTask?.execute()


        if (mLoadDatatask != null)
            mLoadDatatask = null
        mLoadDatatask = LoadData()
        mLoadDatatask?.execute()


        if (mLoadcDatatask != null)
            mLoadcDatatask = null
        mLoadcDatatask = LoadcTask()
        mLoadcDatatask?.execute()


    }


    private fun showEdit() {
        rootView!!.bsm_fab_edit.visibility = View.GONE
        rootView!!.bsm_btns.visibility = View.VISIBLE

        rootView!!.bsm_input_nombre.visibility = View.VISIBLE
        rootView!!.bsm_nombre.visibility = View.GONE
        rootView!!.bsm_input_especie.visibility = View.VISIBLE
        rootView!!.bsm_especie.visibility = View.GONE
        rootView!!.bsm_input_tamanio.visibility = View.VISIBLE
        rootView!!.bsm_tamanio.visibility = View.GONE
        rootView!!.bsm_input_raza.visibility = View.VISIBLE
        rootView!!.bsm_raza.visibility = View.GONE
        rootView!!.bsm_input_nacimiento.visibility = View.VISIBLE
        rootView!!.bsm_nacimiento.visibility = View.GONE

        if (!vivo) {
            rootView!!.bsm_muerte.visibility = View.GONE
            rootView!!.bsm_input_muerte.visibility = View.VISIBLE
        }

        rootView!!.bsm_input_sexo.visibility = View.VISIBLE
        rootView!!.bsm_sexo.visibility = View.GONE
        rootView!!.bsm_input_color.visibility = View.VISIBLE
        rootView!!.bsm_color.visibility = View.GONE
        rootView!!.bsm_input_estado.visibility = View.VISIBLE
        rootView!!.bsm_estado.visibility = View.GONE
        rootView!!.bsm_input_alimentacion.visibility = View.VISIBLE
        rootView!!.bsm_alimentacion.visibility = View.GONE
        rootView!!.bsm_input_microchip.visibility = View.VISIBLE
        rootView!!.bsm_microchip.visibility = View.GONE
        rootView!!.bsm_input_esterilizado.visibility = View.VISIBLE
        rootView!!.bsm_esterilizado.visibility = View.GONE
        rootView!!.bsm_edit_obs.visibility = View.VISIBLE
        rootView!!.bsm_obs.visibility = View.GONE
    }


    internal inner class DataPet : AsyncTask<Void, Void, Void>(), Callback<Mascota> {
        override fun onResponse(call: Call<Mascota>?, response: Response<Mascota>?) {

            val pet = response?.body()
            if (pet != null) {
                rootView!!.bsm_id.text = "MASCOTA ID: " + pet.mascota_id
                rootView!!.bsm_nombre.text = "Nombre: " + pet.nombre
                rootView!!.bsm_edit_nombre.setText(pet.nombre)
                rootView!!.bsm_especie.text = "Especie: " + pet.especie
                rootView!!.bsm_edit_especie.setText(pet.especie)
                rootView!!.bsm_raza.text = "Raza: " + pet.raza
                rootView!!.bsm_edit_raza.setText(pet.raza)
                rootView!!.bsm_color.text = "Color: " + pet.color
                rootView!!.bsm_edit_color.setText(pet.color)
                rootView!!.bsm_sexo.text = "Sexo: " + pet.sexo
                rootView!!.bsm_tamanio.text = "Tamaño: " + pet.tamanio
                rootView!!.bsm_nacimiento.text = "Fecha nacimiento: " + pet.fecha_nacimiento

                val arr1 = pet.fecha_nacimiento?.split("-")

                rootView!!.bsm_datePicker1.updateDate(Integer.parseInt(arr1!![0]), Integer.parseInt(arr1[1]) - 1, Integer.parseInt(arr1[2]))


                rootView!!.bsm_estado.text = "Estado: " + pet.estado
                if (pet.fecha_muerte != null) {
                    vivo = false
                    rootView!!.bsm_spinner_estado.setSelection(1)
                    rootView!!.bsm_muerte.text = "Fecha muerte: " + pet.fecha_muerte
                    val arr2 = pet.fecha_muerte.split("-")
                    rootView!!.bsm_datePicker1.updateDate(Integer.parseInt(arr2[0]), Integer.parseInt(arr2[1]) - 1, Integer.parseInt(arr2[2]))

                } else {
                    rootView!!.bsm_muerte?.visibility = View.GONE
                }

                rootView!!.bsm_esterilizado.text = "Esterilizado: " + pet.esterilizado
                rootView!!.bsm_alimentacion.text = "Esterilizado: " + pet.alimentacion
                rootView!!.bsm_edit_alimentacion.setText(pet.alimentacion)
                rootView!!.bsm_microchip.text = "Microchip: " + pet.microchip
                rootView!!.bsm_obs.text = pet.observaciones
                rootView!!.bsm_edit_obs.setText(pet.observaciones)

                if (pet.foto != null) {
                    Picasso.with(context)
                            .load(Constants.WEB_URL + pet.foto)
                            .into(rootView!!.bsm_foto)
                }
            }
        }

        override fun onFailure(call: Call<Mascota>?, t: Throwable?) {
            Toast.makeText(context, "Error: no se pudo cargar los datos de la mascota", Toast.LENGTH_SHORT).show()
        }

        override fun doInBackground(vararg p0: Void?): Void? {

            val ws = WebApiClient.client!!.create(WebService::class.java)
            val call = ws.mascota(mascota_id)
            call.enqueue(this)

            return null
        }


        override fun onCancelled() {
            dataPetTask = null
        }

    }


    private inner class LoadData : AsyncTask<Void, Void, Void>(), Callback<List<Establecimiento>> {

        override fun onPreExecute() {

        }

        override fun doInBackground(vararg voids: Void): Void? {

            val webService = WebApiClient.client!!.create(WebService::class.java)
            val call = webService.establecimientos_mascota(mascota_id)
            call.enqueue(this)

            return null
        }

        override fun onResponse(call: Call<List<Establecimiento>>, response: Response<List<Establecimiento>>) {


            val tmp = response.body()

            if (tmp != null) {


                for (i in tmp.indices) {
                    establecimientos.add(tmp.get(i))
                    adapterEstablecimientos?.notifyItemInserted(establecimientos.size)
                    // FirebaseMessaging.getInstance().subscribeToTopic("platform-" + tmp.get(i).establecimiento_id!!)
                }

            } else {
                Toast.makeText(context, "null", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onFailure(call: Call<List<Establecimiento>>, t: Throwable) {
            mLoadDatatask = null
            Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
        }

        override fun onCancelled() {
            mLoadDatatask = null
        }
    }


    internal inner class LoadcTask : AsyncTask<Void, Void, Void>(), Callback<MutableList<Cita>> {
        override fun onFailure(call: Call<MutableList<Cita>>?, t: Throwable?) {
            Log.i("eventos", "fallo")
        }

        override fun onResponse(call: Call<MutableList<Cita>>?, response: Response<MutableList<Cita>>?) {
            if (response?.body() != null) {

                if (response.body().size > 0) {
                    rootView!!.bsm_list_eventos.visibility = View.VISIBLE
                    rootView!!.bsm_cardView.visibility = View.GONE
                    eventos.addAll(response.body())
                    adapterEvents?.notifyItemRangeInserted(0, eventos.size)
                    adapterEvents?.notifyDataSetChanged()

                }

            } else
                Log.i("eventos", "null")
        }

        override fun onPreExecute() {
            // Toast.makeText(context, "cargando eventos...", Toast.LENGTH_SHORT).show()
        }

        override fun doInBackground(vararg p0: Void?): Void? {

            val ws = WebApiClient.client!!.create(WebService::class.java)
            val call = ws.citas_mascota(mascota_id)
            call.enqueue(this)


            return null
        }

        override fun onCancelled() {
            mLoadcDatatask = null
        }

    }


}