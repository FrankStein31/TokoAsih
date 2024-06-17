package com.example.ust_laundry

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.StrictMode
import android.text.Editable
import android.text.TextWatcher
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
import java.lang.reflect.Method
import java.util.HashMap

class FragmentKtgri : Fragment(), View.OnClickListener {
    lateinit var binding: FragmentKtgriBinding
    lateinit var fragKategori: FragmentKtgri
    lateinit var thisParent: MainActivity
    lateinit var v: View
    val dataktgri = mutableListOf<HashMap<String, String>>()
    val filteredData = mutableListOf<HashMap<String, String>>()
    lateinit var ktgadapter: AdapterKategori

    override fun onStart() {
        super.onStart()
        showData()
    }

    var id_kategori = ""
    val url = "http://192.168.0.56/web_service_tokoasih/crud_ktgri.php"

    private fun showData() {
        val request = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                dataktgri.clear()
                val jsonArray = JSONArray(response)
                for (x in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(x)
                    val frm = HashMap<String, String>()
                    frm["id_barang"] = jsonObject.getString("id_barang")
                    frm["barang"] = jsonObject.getString("barang")
                    frm["harga"] = jsonObject.getString("harga")
                    frm["qrcode"] = jsonObject.getString("qrcode")
                    dataktgri.add(frm)
                }
                filteredData.clear()
                filteredData.addAll(dataktgri)
                ktgadapter = AdapterKategori(filteredData, this)
                binding.recycleviewktg.adapter = ktgadapter
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

    private fun filterData(query: String) {
        ktgadapter.filter(query)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnInsrtktg -> {
                AlertDialog.Builder(v.context)
                    .setIcon(android.R.drawable.stat_sys_warning)
                    .setTitle("Konfirmasi!")
                    .setMessage("Apakah Anda sudah yakin ingin mengirim data?")
                    .setPositiveButton("Ya") { dialogInterface, i ->
                        insert_update_delete("insert")
                        thisParent.recreate()
                    }
                    .setNegativeButton("Tidak") { dialogInterface, i ->
                        Toast.makeText(v.context, "Berhasil Membatalkan!", Toast.LENGTH_SHORT).show()
                    }
                    .show()
            }
            R.id.btnDeltKtg -> {
                AlertDialog.Builder(v.context)
                    .setIcon(android.R.drawable.ic_input_get)
                    .setTitle("Peringatan")
                    .setMessage("Apakah Anda ingin menghapus data ini?")
                    .setPositiveButton("Ya") { dialogInterface, i ->
                        insert_update_delete("delete")
                        thisParent.recreate()
                    }
                    .setNegativeButton("Tidak", null)
                    .show()
            }
            R.id.btnUpdtktg -> {
                AlertDialog.Builder(v.context)
                    .setIcon(android.R.drawable.stat_sys_warning)
                    .setTitle("Konfirmasi!")
                    .setMessage("Apakah Anda sudah yakin ingin mengedit data?")
                    .setPositiveButton("Ya") { dialogInterface, i ->
                        insert_update_delete("edit")
                        thisParent.recreate()
                    }
                    .setNegativeButton("Tidak") { dialogInterface, i ->
                        Toast.makeText(v.context, "Berhasil Membatalkan!", Toast.LENGTH_SHORT).show()
                    }
                    .show()
            }
        }
    }

    fun insert_update_delete(mode: String) {
        val request = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                val jsonObject = JSONObject(response)
                val respon = jsonObject.getString("respon")
                if (respon == "1") {
                    Toast.makeText(v.context, "Berhasil mengirim data", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(v.context, "Tidak dapat terhubung ke server", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String>? {
                val hm = HashMap<String, String>()
                when (mode) {
                    "insert" -> {
                        hm["mode"] = "insert"
                        hm["barang"] = binding.edNamaKategori.text.toString()
                        hm["harga"] = binding.edHargaKategori.text.toString()
                    }
                    "edit" -> {
                        hm["mode"] = "edit"
                        hm["id_barang"] = id_kategori
                        hm["barang"] = binding.edNamaKategori.text.toString()
                        hm["harga"] = binding.edHargaKategori.text.toString()
                    }
                    "delete" -> {
                        hm["mode"] = "delete"
                        hm["id_barang"] = id_kategori
                    }
                }
                return hm
            }
        }
        val queue = Volley.newRequestQueue(v.context)
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

        ktgadapter = AdapterKategori(filteredData, this)
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

        // Tambahkan TextWatcher untuk pencarian
        binding.edCariBarang.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterData(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Tidak diperlukan
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Tidak diperlukan
            }
        })

        return v
    }
}
