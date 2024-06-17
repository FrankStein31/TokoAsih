package com.mobile.tokoasih

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.mobile.tokoasih.databinding.FragmentPesananBinding
import com.mobile.uts_dyah.PhotoHelper
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class FragmentPesanan :Fragment(), View.OnClickListener {
    lateinit var b:FragmentPesananBinding
    lateinit var thisParent: MainActivity
    lateinit var dialog: AlertDialog.Builder
    lateinit var fragmentPesanan: FragmentPesanan
    var selectedKategori: String = ""
    private lateinit var currentPhotoPath: String
    lateinit var v:View

    lateinit var photoHelper: PhotoHelper
    var imStr = ""
    var namaFile = ""
    var fileUri = Uri.parse("")

    val url = "http://192.168.0.56/web_service_tokoasih/crud.php"

    val ktgSpinner = mutableListOf<String>()
    lateinit var strKategori: ArrayAdapter<String>

    val data = mutableListOf<HashMap<String,String>>()
    lateinit var adapter: AdapterPesanan

    var id = ""

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnInsertpsn ->{
                AlertDialog.Builder(v.context)
                    .setIcon(android.R.drawable.stat_sys_warning)
                    .setTitle("Konfirmasi!")
                    .setMessage("Apakah Anda sudah yakin ingin mengirim data?")
                    .setPositiveButton("Ya", DialogInterface.OnClickListener { dialogInterface, i ->
                        insert_update("insert")
                        thisParent.recreate()
                    })
                    .setNegativeButton("Tidak", DialogInterface.OnClickListener { dialogInterface, i ->
                        Toast.makeText(v.context,"Berhasil Membatalkan!", Toast.LENGTH_SHORT).show()
                    })
                    .show()
                true
            }
            R.id.btnDeletepsn ->{
                AlertDialog.Builder(v.context)
                .setIcon(android.R.drawable.ic_input_get)
                .setTitle("Peringatan")
                .setMessage("Apakah Anda ingin menghapus data ini?")
                .setPositiveButton("Ya", DialogInterface.OnClickListener { dialogInterface, i ->
                    delete(id)
                })
                .setNegativeButton("Tidak", DialogInterface.OnClickListener { dialogInterface, i ->
                })
                .show()
            true
            }
            R.id.btnUpdatepsn ->{
                AlertDialog.Builder(v.context)
                    .setIcon(android.R.drawable.stat_sys_warning)
                    .setTitle("Konfirmasi!")
                    .setMessage("Apakah Anda sudah yakin ingin mengedit data?")
                    .setPositiveButton("Ya", DialogInterface.OnClickListener { dialogInterface, i ->
                        insert_update("edit")
                        thisParent.recreate()
                    })
                    .setNegativeButton("Tidak", DialogInterface.OnClickListener { dialogInterface, i ->
                        Toast.makeText(v.context,"Berhasil Membatalkan!", Toast.LENGTH_SHORT).show()
                    })
                    .show()
                true
            }
            R.id.btnhitung -> {
                val a = b.edBerat.text.toString()
                val kategori = selectedKategori
                val harga = mapKategoriHarga[kategori]
                if (harga != null) {
                    val hasil = a.toInt() * harga.toInt()
                    b.edHarga.setText(hasil.toString())
                } else {
                    Toast.makeText(v.context, "Harga tidak ditemukan untuk kategori ini", Toast.LENGTH_SHORT).show()
                }
            }

        }
        true
    }

    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        thisParent = activity as MainActivity
        b = FragmentPesananBinding.inflate(layoutInflater)
        v = b.root

//        if (ktgSpinner.isNotEmpty()) {
//            selectedKategori = ktgSpinner[0]
//            b.spKategori.setText(selectedKategori, false)
//        }
        if (ktgSpinner.isNotEmpty()) {
            for (kategori in ktgSpinner) {
                b.spKategori.setText(kategori, false)
            }
        }

        photoHelper = PhotoHelper()
        adapter = AdapterPesanan(data, this)
        b.recyclerView.layoutManager = LinearLayoutManager(v.context)
        b.recyclerView.adapter = adapter

        try {
            val m = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")
            m.invoke(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        b.btnChoose.setOnClickListener {
            val options = arrayOf("Kamera", "File Manager")
            val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            builder.setTitle("Pilih Gambar Dari")
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openFileManager()
                }
            }
            builder.show()
        }

        b.btnDeletepsn.setOnClickListener(this)
        b.btnInsertpsn.setOnClickListener(this)
        b.btnUpdatepsn.setOnClickListener(this)
        b.btnhitung.setOnClickListener(this)

        strKategori = ArrayAdapter(v.context, android.R.layout.simple_list_item_1, ktgSpinner)
        b.spKategori.setAdapter(strKategori)
        b.spKategori.setOnItemClickListener { parent, view, position, id ->
            selectedKategori = parent.getItemAtPosition(position) as String
        }
        b.spKategori.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedKategori = parent?.getItemAtPosition(position).toString()
                adapter.notifyDataSetChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Nothing to do here
            }
        }
        b.spKategori.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val enteredText = b.spKategori.text.toString()
                if (!ktgSpinner.contains(enteredText)) {
                    b.spKategori.setText(selectedKategori, false)
                }
            }
        }

        return v
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = requireContext().getExternalFilesDir(null)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }
    private fun openFileManager() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onStart() {
        super.onStart()
        getBarang()
        showData()
    }

    fun openCamera() = runWithPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA ) {
        fileUri = photoHelper.getOutputMediaFileUri()
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        startActivityForResult(intent, photoHelper.getRcCamera())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            photoHelper.getRcCamera() -> {
                when (resultCode) {
                    AppCompatActivity.RESULT_OK -> {
                        imStr = photoHelper.getBitMapToString(b.imageView, fileUri)
                        namaFile = photoHelper.getMyFileName()
                        Toast.makeText(v.context, "Berhasil upload foto", Toast.LENGTH_SHORT).show()
                    }
                    AppCompatActivity.RESULT_CANCELED -> {
                        // kode untuk kondisi kedua jika dibatalkan
                    }
                }
            }
            REQUEST_IMAGE_PICK -> {
                if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    val selectedImageUri: Uri? = data.data
                    if (selectedImageUri != null) {
                        try {
                            val bitmap = MediaStore.Images.Media.getBitmap(v.context.contentResolver, selectedImageUri)
                            b.imageView.setImageBitmap(bitmap)
                            imStr = photoHelper.convertBitmapToString(bitmap)  // Mengubah bitmap menjadi string
                            namaFile = photoHelper.getFileNameFromUri(selectedImageUri, v.context.contentResolver)  // Mendapatkan nama file dari URI
                            fileUri = selectedImageUri  // Menyimpan URI file yang dipilih
                            Toast.makeText(v.context, "Berhasil memilih gambar", Toast.LENGTH_SHORT).show()
                        } catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(v.context, "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    fun insert_update(mode: String) {
        val request = object : StringRequest(
            Method.POST,url,
            Response.Listener { response ->
                Log.d("Response", response) // Tambahkan logging respons
                val filteredResponse = response.replace("<br>", "").replace("<br/>", "")
                try {
                    val jsonObject = JSONObject(filteredResponse)
                    val respon = jsonObject.getString("respon")
                    if (respon == "1") {
                        Toast.makeText(v.context, "Berhasil mengirim data", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(v.context, "Kesalahan parsing data JSON", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(v.context,"Tidak dapat terhubung ke server", Toast.LENGTH_LONG).show()
            }){
            override fun getParams(): MutableMap<String, String>? {
                val hm = HashMap<String, String>()
                when(mode) {
                    "insert" -> {
                        hm["mode"] = "insert"
                        hm["nama_pelanggan"] = b.edPelanggan.text.toString()
                        hm["alamat"] = b.edAlamat.text.toString()
                        hm["barang"] = selectedKategori
                        hm["jumlah"] = b.edBerat.text.toString()
                        hm["harga"] = b.edHarga.text.toString()
                        hm["image"] = imStr
                        hm["file"] = namaFile
                    }
                    "edit" -> {
                        hm["mode"] = "edit"
                        hm["id"] = id
                        hm["nama_pelanggan"] = b.edPelanggan.text.toString()
                        hm["alamat"] = b.edAlamat.text.toString()
                        hm["barang"] = selectedKategori
                        hm["jumlah"] = b.edBerat.text.toString()
                        hm["harga"] = b.edHarga.text.toString()
                        hm["image"] = imStr
                        hm["file"] = namaFile
                    }
                }
                return hm
            }
        }
        val queue = Volley.newRequestQueue(v.context)
        queue.add(request)
    }

    val mapKategoriHarga = HashMap<String, String>()
    fun getBarang() {
        val request = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                ktgSpinner.clear()
                val jsonArray = JSONArray(response)
                for (x in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(x)
                    val barang = jsonObject.getString("barang")
                    val harga = jsonObject.getString("harga")

                    ktgSpinner.add(barang)

                    // Simpan harga pada map kategori dan harga
                    mapKategoriHarga[barang] = harga
                }
                strKategori.notifyDataSetChanged()
                if (selectedKategori.isEmpty() && ktgSpinner.isNotEmpty()) {
                    selectedKategori = ktgSpinner[0]
                    b.spKategori.setText(selectedKategori, false)
                }
            },
            Response.ErrorListener { error ->
                // Handle error response
            }) {
            override fun getParams(): MutableMap<String, String>? {
                val params = HashMap<String, String>()
                params["mode"] = "get_barang"
                return params
            }
        }
        val queue = Volley.newRequestQueue(v.context)
        queue.add(request)
    }

    private fun showData() {
        val request = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                try {
                    Log.d("Response", response) // Tambahkan logging respons
                    data.clear()
                    val jsonArray = JSONArray(response)
                    for (x in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(x)
                        val frm = HashMap<String, String>()
                        frm["id"] = jsonObject.getString("id")
                        frm["nama_pelanggan"] = jsonObject.getString("nama_pelanggan")
                        frm["alamat"] = jsonObject.getString("alamat")
                        frm["barang"] = jsonObject.getString("barang")
                        frm["jumlah"] = jsonObject.getString("jumlah")
                        frm["harga"] = jsonObject.getString("harga")
                        frm["img"] = jsonObject.getString("img")
                        frm["tanggal_input"] = jsonObject.getString("tanggal_input")

                        data.add(frm)
                    }
                    adapter.notifyDataSetChanged()
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(v.context, "Kesalahan parsing data JSON", Toast.LENGTH_LONG).show()
                }
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

    fun delete(id: String) {
        val request = object : StringRequest(
            Method.POST,url,
            Response.Listener { response ->
                val jsonObject = JSONObject(response)
                val respon = jsonObject.getString("respon")
                if (respon.equals("1")) {
                    Toast.makeText(v.context, "Berhasil menghapus data!", Toast.LENGTH_SHORT).show()
                    thisParent.recreate()
                }
            },
            Response.ErrorListener { error ->
                val errorCode = error.networkResponse?.statusCode ?: -1
                Toast.makeText(v.context,"Tidak dapat terhubung ke server", Toast.LENGTH_LONG).show()
                Log.e("Error","Error : $errorCode")
            }){
            override fun getParams(): MutableMap<String, String>? {
                val hm = java.util.HashMap<String, String>()
                hm.put("mode", "delete")
                hm.put("id", id)

                return hm
            }
        }
        val  queue = Volley.newRequestQueue(v.context)
        queue.add(request)
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
        private const val REQUEST_IMAGE_PICK = 100
        private const val REQUEST_IMAGE_CAPTURE = 101
    }
}