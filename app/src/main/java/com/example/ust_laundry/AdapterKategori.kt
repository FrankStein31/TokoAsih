package com.example.ust_laundry

import android.content.Intent
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class AdapterKategori(val dataktg: List<HashMap<String,String>>, val parent: FragmentKtgri) :
    RecyclerView.Adapter<AdapterKategori.HolderDataAdapter>(){
    class HolderDataAdapter(v : View) : RecyclerView.ViewHolder(v) {
        val ktg = v.findViewById<TextView>(R.id.ktg_nama)
        val hrg = v.findViewById<TextView>(R.id.ktg_harga)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderDataAdapter {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_kategori,parent,false)
        return HolderDataAdapter(v)
    }

    override fun getItemCount(): Int {
        return dataktg.size
    }

    override fun onBindViewHolder(holder: HolderDataAdapter, position: Int) {
        val item = dataktg[position]
        holder.ktg.text = item["kategori"]
        holder.hrg.text = "Harga : Rp." + item["harga"]

        holder.itemView.setOnClickListener {
            val kategori = item["kategori"]
            val harga = item["harga"]

            // Set data ke EditText edNamaKategori dan edHargaKategori
            parent.id_kategori = item.get("id_kategori").toString()
            parent.binding.edNamaKategori.setText(kategori)
            parent.binding.edHargaKategori.setText(harga)
        }
    }


}