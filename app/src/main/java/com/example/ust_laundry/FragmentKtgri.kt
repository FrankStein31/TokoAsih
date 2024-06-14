package com.example.ust_laundry

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.ust_laundry.databinding.FragmentKtgriBinding
import org.json.JSONArray
import org.json.JSONObject
import java.util.HashMap

class FragmentKtgri : Fragment(), View.OnClickListener {
    lateinit var binding: FragmentKtgriBinding
    lateinit var thisParent: MainActivity
    lateinit var v:View
    val dataktgri = mutableListOf<HashMap<String,String>>()
    lateinit var ktgadapter : AdapterKategori

    override fun onStart() {
        super.onStart()
        showData()
    }
    var id_kategori = ""
    val url = "http://192.168.18.40/web_service_tokoasih/crud_ktgri.php"

    private fun showData() {
        val request = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                dataktgri.clear()
                val jsonArray = JSONArray(response)
                for (x in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(x)
                    val frm = HashMap<String, String>()
                    frm.put("id_barang",jsonObject.getString("id_barang"))
                    frm["barang"] = jsonObject.getString("barang")
                    frm["harga"] = jsonObject.getString("harga")
                    dataktgri.add(frm)
                }
                ktgadapter = AdapterKategori(dataktgri, this)
                getFragmentKtgriBinding().recycleviewktg.adapter = ktgadapter // Menetapkan adapter ke RecyclerView
                ktgadapter.notifyDataSetChanged()
            },
            Response.ErrorListener { error ->
                Toast.makeText(v.context, "Tidak dapat terhubung ke server", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String>? {
                val hm = HashMap<String, String>()
                hm["mode"] = "show_data"
                return hm
            }
        }
        val queue = Volley.newRequestQueue(v.context)
        queue.add(request)
    }

    private fun getFragmentKtgriBinding(): FragmentKtgriBinding {
        return binding ?: throw IllegalStateException("View binding belum diinisialisasi")
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnInsrtktg -> {
                AlertDialog.Builder(v.context)
                    .setIcon(android.R.drawable.stat_sys_warning)
                    .setTitle("Konfirmasi!")
                    .setMessage("Apakah Anda sudah yakin ingin mengirim data?")
                    .setPositiveButton("Ya", DialogInterface.OnClickListener { dialogInterface, i ->
                        insert_update_delete("insert")
                        thisParent.recreate()
                    })
                    .setNegativeButton("Tidak", DialogInterface.OnClickListener { dialogInterface, i ->
                        Toast.makeText(v.context, "Berhasil Membatalkan!", Toast.LENGTH_SHORT).show()
                    })
                    .show()
            }
            R.id.btnDeltKtg -> {
                AlertDialog.Builder(v.context)
                    .setIcon(android.R.drawable.ic_input_get)
                    .setTitle("Peringatan")
                    .setMessage("Apakah Anda ingin menghapus data ini?")
                    .setPositiveButton("Ya", DialogInterface.OnClickListener { dialogInterface, i ->
                        insert_update_delete("delete")
                        thisParent.recreate()
                    })
                    .setNegativeButton("Tidak", DialogInterface.OnClickListener { dialogInterface, i ->
                        // Tidak ada tindakan yang dilakukan saat tombol "Tidak" diklik
                    })
                    .show()
            }

            R.id.btnUpdtktg ->{
                AlertDialog.Builder(v.context)
                    .setIcon(android.R.drawable.stat_sys_warning)
                    .setTitle("Konfirmasi!")
                    .setMessage("Apakah Anda sudah yakin ingin mengedit data?")
                    .setPositiveButton("Ya", DialogInterface.OnClickListener { dialogInterface, i ->
                        insert_update_delete("edit")
                        thisParent.recreate()
                    })
                    .setNegativeButton("Tidak", DialogInterface.OnClickListener { dialogInterface, i ->
                        Toast.makeText(v.context,"Berhasil Membatalkan!", Toast.LENGTH_SHORT).show()
                    })
                    .show()
            }

        }
    }

    fun insert_update_delete(mode: String) {
        val request = object : StringRequest(
            Method.POST,url,
            Response.Listener { response ->
                val jsonObject = JSONObject(response)
                val respon = jsonObject.getString("respon")
                if (respon.equals("1")) {
                    Toast.makeText(v.context, "Berhasil mengirim data", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(v.context,"Tidak dapat terhubung ke server", Toast.LENGTH_LONG).show()
            }){
            override fun getParams(): MutableMap<String, String>? {
                val hm = HashMap<String, String>()
                when(mode) {
                    "insert" -> {
                        hm.put("mode", "insert")
                        hm.put("barang", binding.edNamaKategori.text.toString())
                        hm.put("harga", binding.edHargaKategori.text.toString())
                    }
                    "edit" -> {
                        hm.put("mode", "edit")
                        hm.put("id_barang", id_kategori)
                        hm.put("barang", binding.edNamaKategori.text.toString())
                        hm.put("harga", binding.edHargaKategori.text.toString())
                    }
                    "delete" -> {
                        hm.put("mode", "delete")
                        hm.put("id_barang", id_kategori)
                        hm.put("barang", binding.edNamaKategori.text.toString())
                        hm.put("harga", binding.edHargaKategori.text.toString())
                    }
                }
                return hm
            }
        }
        val  queue = Volley.newRequestQueue(v.context)
        queue.add(request)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        thisParent = activity as MainActivity
        binding = FragmentKtgriBinding.inflate(inflater, container, false)
        v = binding.root

        // Tambahkan kode inisialisasi adapter dan layoutManager di sini
        ktgadapter = AdapterKategori(dataktgri, this)
        binding.recycleviewktg.adapter = ktgadapter
        binding.recycleviewktg.layoutManager = LinearLayoutManager(requireContext())

        try {
            val m = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")
            m.invoke(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        binding.btnInsrtktg.setOnClickListener(this)
        binding.btnUpdtktg.setOnClickListener(this)
        binding.btnDeltKtg.setOnClickListener(this)
        return v
    }
}
