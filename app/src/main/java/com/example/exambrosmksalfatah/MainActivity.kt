package com.example.exambrosmksalfatah

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val exitCode = "123456"
    private var isExamStarted = false
    private var lastPauseTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            /* 🔒 Cegah screenshot & tampil di recent apps */
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )

            setContentView(R.layout.activity_main)
            setupFullscreen()

            webView = findViewById(R.id.webView)
            webView.settings.javaScriptEnabled = true
            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return false
                }
            }

            handleIntent(intent)

            // Handle tombol back
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        showExitCodeDialog()
                    }
                }
            })

        } catch (t: Throwable) {
            val errorWebView = WebView(this)
            setContentView(errorWebView)
            errorWebView.loadData("<h2>Error Sistem</h2><pre>${t.message}</pre>", "text/html", "UTF-8")
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val url = intent?.getStringExtra("EXTRA_URL")?.trim()
        if (!url.isNullOrEmpty()) {
            webView.loadUrl(url)
            isExamStarted = true
        } else {
            webView.loadData("<h3>❌ Error: URL tidak valid</h3>", "text/html", "UTF-8")
        }
    }

    private fun setupFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
        }
    }

    override fun onPause() {
        super.onPause()
        lastPauseTime = System.currentTimeMillis()
    }

    override fun onResume() {
        super.onResume()
        // Jika ujian sudah dimulai, periksa apakah pengguna keluar terlalu lama
        if (isExamStarted) {
            val timeAway = System.currentTimeMillis() - lastPauseTime
            // Jika lebih dari 1 detik di luar → anggap pelanggaran
            if (timeAway > 1000) {
                handleExamViolation()
                return
            }
        }
        hideSystemUI()
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
        }
    }

    private fun handleExamViolation() {
        AlertDialog.Builder(this)
            .setTitle("🚨 Pelanggaran Ujian!")
            .setMessage("Anda meninggalkan aplikasi selama ujian. Sesi dianggap tidak sah dan akan diakhiri.")
            .setPositiveButton("Mengerti") { _, _ ->
                finishAffinity()
            }
            .setCancelable(false)
            .show()
    }

    private fun showExitCodeDialog() {
        val input = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            hint = "Masukkan 123456"
            textSize = 16f
        }

        AlertDialog.Builder(this)
            .setTitle("🔐 Kode Akses Keluar")
            .setMessage("Masukkan kode rahasia untuk mengakhiri ujian")
            .setView(input)
            .setPositiveButton("Verifikasi") { _, _ ->
                if (input.text.toString() == exitCode) {
                    finishAffinity()
                } else {
                    AlertDialog.Builder(this)
                        .setTitle("❌ Kode Salah!")
                        .setMessage("Kode tidak valid. Ujian tetap berjalan.")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
            .setNegativeButton("Batal", null)
            .setCancelable(false)
            .show()
    }
}