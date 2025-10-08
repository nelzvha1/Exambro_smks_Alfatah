package com.example.exambrosmksalfatah

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        try {
            val urlEditText = findViewById<EditText>(R.id.urlEditText)
            val enterButton = findViewById<Button>(R.id.enterButton)

            // Periksa apakah komponen UI ditemukan. Jika tidak, ini adalah penyebab crash.
            if (urlEditText == null || enterButton == null) {
                Toast.makeText(this, "Error Kritis: Komponen UI di activity_start.xml tidak ditemukan.", Toast.LENGTH_LONG).show()
                return
            }

            fun launchWebView() {
                try {
                    var url = urlEditText.text.toString().trim()

                    if (url.isEmpty()) {
                        Toast.makeText(this@StartActivity, "Alamat URL tidak boleh kosong", Toast.LENGTH_SHORT).show()
                        return
                    }

                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "https://$url"
                    }

                    val intent = Intent(this@StartActivity, MainActivity::class.java)
                    intent.putExtra("EXTRA_URL", url)
                    startActivity(intent)

                } catch (t: Throwable) {
                    // Menampilkan error jika ada masalah saat mencoba memulai MainActivity
                    Toast.makeText(this@StartActivity, "Gagal memulai WebView: ${t.message}", Toast.LENGTH_LONG).show()
                }
            }

            enterButton.setOnClickListener {
                launchWebView()
            }

            urlEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                    launchWebView()
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }

        } catch (t: Throwable) {
            // Menangkap semua error lain yang mungkin terjadi di StartActivity
            Toast.makeText(this, "Error Fatal di StartActivity: ${t.message}", Toast.LENGTH_LONG).show()
        }
    }
}