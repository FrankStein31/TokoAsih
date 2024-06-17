package com.mobile.tokoasih

import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentTransaction
import com.mobile.tokoasih.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var b: ActivityMainBinding
    lateinit var fragKategori: FragmentKtgri
    lateinit var fragmentPesanan: FragmentPesanan
    lateinit var fragmentProfile: FragmentProfile
    lateinit var fragmentHome: fragmenthome
    lateinit var ft: FragmentTransaction

    lateinit var preferences: SharedPreferences
    val PREF_NAME = "akun"
    val NAMA = "nama"
    val DEF_NAMA = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        supportActionBar?.hide()

        fragKategori = FragmentKtgri()
        fragmentPesanan = FragmentPesanan()
        fragmentHome = fragmenthome()
        fragmentProfile = FragmentProfile()

        // Tampilkan FragmentHome secara default saat pertama kali aplikasi dijalankan
        ft = supportFragmentManager.beginTransaction()
        fragmentHome = fragmenthome() // Inisialisasi ulang objek fragment
        ft.replace(R.id.frameLayout, fragmentHome)
        ft.commit()
        b.frameLayout.setBackgroundColor(Color.argb(245, 255, 255, 225))
        b.frameLayout.visibility = View.VISIBLE

        b.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.itemKatgeori -> {
                    ft = supportFragmentManager.beginTransaction()
                    fragKategori = FragmentKtgri() // Inisialisasi ulang objek fragment
                    ft.replace(R.id.frameLayout, fragKategori)
                    ft.commit()
                    b.frameLayout.setBackgroundColor(Color.argb(245, 255, 255, 225))
                    b.frameLayout.visibility = View.VISIBLE
                }
                R.id.itemPesanan -> {
                    ft = supportFragmentManager.beginTransaction()
                    fragmentPesanan = FragmentPesanan() // Inisialisasi ulang objek fragment
                    ft.replace(R.id.frameLayout, fragmentPesanan)
                    ft.commit()
                    b.frameLayout.setBackgroundColor(Color.argb(245, 255, 255, 225))
                    b.frameLayout.visibility = View.VISIBLE
                }
                R.id.itemHome -> {
                    ft = supportFragmentManager.beginTransaction()
                    fragmentHome = fragmenthome() // Inisialisasi ulang objek fragment
                    ft.replace(R.id.frameLayout, fragmentHome)
                    ft.commit()
                    b.frameLayout.setBackgroundColor(Color.argb(245, 255, 255, 225))
                    b.frameLayout.visibility = View.VISIBLE
                }
                R.id.itemProfile -> {
                    ft = supportFragmentManager.beginTransaction()
                    fragmentProfile = FragmentProfile() // Inisialisasi ulang objek fragment
                    ft.replace(R.id.frameLayout, fragmentProfile)
                    ft.commit()
                    b.frameLayout.setBackgroundColor(Color.argb(245, 255, 255, 225))
                    b.frameLayout.visibility = View.VISIBLE
                }
            }
            true
        }
    }
}
