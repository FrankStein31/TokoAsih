package com.mobile.tokoasih

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder

class AdapterKategori(val dataktg: List<HashMap<String,String>>, val parent: FragmentKtgri) :
    RecyclerView.Adapter<AdapterKategori.HolderDataAdapter>(){
    var filteredDataktg = ArrayList<HashMap<String, String>>(dataktg)
    class HolderDataAdapter(v : View) : RecyclerView.ViewHolder(v) {
        val ktg = v.findViewById<TextView>(R.id.ktg_nama)
        val hrg = v.findViewById<TextView>(R.id.ktg_harga)
        val qr = v.findViewById<TextView>(R.id.qrcode)
        val qrCodeImageView = v.findViewById<ImageView>(R.id.imageViewQRCode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderDataAdapter {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_kategori,parent,false)
        return HolderDataAdapter(v)
    }

    override fun getItemCount(): Int {
        return filteredDataktg.size
    }

    override fun onBindViewHolder(holder: HolderDataAdapter, position: Int) {
        val item = filteredDataktg[position]
        holder.ktg.text = item["barang"]
        holder.hrg.text = "Harga : Rp." + item["harga"]
        holder.qr.text = item["qrcode"]
        // Generate QR Code from data
        try {
            val qrCodeText = item["qrcode"] ?: ""
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(qrCodeText, BarcodeFormat.QR_CODE, 400, 400)
            holder.qrCodeImageView.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }

        holder.itemView.setOnClickListener {
            val kategori = item["barang"]
            val harga = item["harga"]

            // Set data ke EditText edNamaKategori dan edHargaKategori
            parent.id_kategori = item["id_barang"].toString()
            parent.binding.edNamaKategori.setText(kategori)
            parent.binding.edHargaKategori.setText(harga)
        }
    }

    fun filter(query: String) {
        val lowerCaseQuery = query.toLowerCase()
        filteredDataktg.clear()
        if (lowerCaseQuery.isEmpty()) {
            filteredDataktg.addAll(dataktg)
        } else {
            for (item in dataktg) {
                val barang = item["barang"]?.toLowerCase()
                if (barang != null && barang.contains(lowerCaseQuery)) {
                    filteredDataktg.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }
}
