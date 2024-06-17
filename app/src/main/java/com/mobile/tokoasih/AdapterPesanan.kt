package com.mobile.tokoasih

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class AdapterPesanan(val data: List<HashMap<String, String>>, val parent: FragmentPesanan) :
    RecyclerView.Adapter<AdapterPesanan.HolderDataAdapter>() {

    class HolderDataAdapter(v: View) : RecyclerView.ViewHolder(v) {
        val nm = v.findViewById<TextView>(R.id.itemNama)
        val ktg = v.findViewById<TextView>(R.id.itemKategori)
        val alm = v.findViewById<TextView>(R.id.itemAlamat)
        val brt = v.findViewById<TextView>(R.id.itemBerat)
        val hrg = v.findViewById<TextView>(R.id.itemHarga)
        val btn = v.findViewById<TextView>(R.id.btnLokasi)
        val img = v.findViewById<ImageView>(R.id.itemImage)
        val tgl = v.findViewById<TextView>(R.id.txttgl)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderDataAdapter {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_pesanan, parent, false)
        return HolderDataAdapter(v)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: HolderDataAdapter, position: Int) {
        val item = data[position]
        holder.nm.text = item["nama_pelanggan"]
        holder.ktg.text = "Barang : ${item["barang"]}"
        holder.alm.text = "Alamat : ${item["alamat"]}"
        holder.brt.text = "Jumlah : ${item["jumlah"]} pcs"
        holder.hrg.text = "Harga : Rp.${item["harga"]}"
        Picasso.get().load(item["img"]).into(holder.img)
        holder.tgl.text = "Last Modified : ${item["tanggal_input"]}"

        holder.btn.setOnClickListener {
            val intent = Intent(it.context, MapsActivity::class.java)
            intent.putExtra("alm", item["alamat"].toString())
            it.context.startActivity(intent)
        }

        holder.itemView.setOnClickListener {
            parent.id = item["id"].toString()
            parent.b.edPelanggan.setText(item["nama_pelanggan"].toString())

            // Set nilai barang pada spKategori
            val barang = item["barang"].toString()
            parent.b.spKategori.setText(barang)

            // Pastikan spKategori memiliki adapter dan daftar barang yang sesuai
            val adapter = parent.b.spKategori.adapter as ArrayAdapter<String>
            val posisiBarang = adapter.getPosition(barang)
            if (posisiBarang >= 0) {
                parent.b.spKategori.setSelection(posisiBarang)
            }

            parent.b.edAlamat.setText(item["alamat"].toString())
            parent.b.edBerat.setText(item["jumlah"].toString())
            parent.b.edHarga.setText(item["harga"].toString())
            Picasso.get().load(item["img"]).into(parent.b.imageView)
        }
    }
}