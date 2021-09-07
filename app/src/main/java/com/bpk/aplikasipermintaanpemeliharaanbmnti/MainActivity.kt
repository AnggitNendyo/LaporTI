package com.bpk.aplikasipermintaanpemeliharaanbmnti

import android.content.*
import android.net.ConnectivityManager
import android.net.http.SslError
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.webkit.*
import android.widget.EditText
import android.widget.ProgressBar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val url =
            "https://portal.bpk.go.id/sites/DIY/Internal/Pages/Pelaporan-Permintaan-Pemeliharaan-BMN-TI-User.aspx"
        const val MAX_PROGRESS = 100
    }

    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val notConnected = intent.getBooleanExtra(
                ConnectivityManager
                    .EXTRA_NO_CONNECTIVITY, false
            )
            if (notConnected) {
                disconnected()
            } else {
                connected()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initWebView()
        setWebViewClient()
        setWebChromeClient()
        handlePullToRefresh()
    }

    private fun initWebView() {
        webView.settings.javaScriptEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.domStorageEnabled = true
        webView.settings.builtInZoomControls = true
        CookieManager.getInstance().setAcceptCookie(true);
    }

    private fun setWebViewClient() {
        webView.webViewClient = object : WebViewClient() {
            override
            fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed()
            }
            override fun onReceivedHttpAuthRequest(
                view: WebView?,
                handler: HttpAuthHandler,
                host: String?,
                realm: String?
            ) {
                alertDialog(handler)
            }
        }
    }

    fun alertDialog(httpAuthHandler: HttpAuthHandler) {
        val dialogBuilder: android.app.AlertDialog.Builder =
            android.app.AlertDialog.Builder(this@MainActivity)
        val inflater: LayoutInflater = this@MainActivity.getLayoutInflater()

        val dialogView: View = inflater.inflate(R.layout.login_layout, null)
        val txtUsername = dialogView.findViewById<View>(R.id.dauth_userinput) as EditText
        val txtPassword = dialogView.findViewById<View>(R.id.dauth_passinput) as EditText
        dialogBuilder.setView(dialogView)

        dialogBuilder.setPositiveButton("Login",
            DialogInterface.OnClickListener { dialog, whichButton ->
                httpAuthHandler.proceed(
                    txtUsername.text.toString(),
                    txtPassword.text.toString()
                )
            }
        )
        dialogBuilder.setCancelable(false)
        val alertDialog: android.app.AlertDialog? = dialogBuilder.create()
        alertDialog?.show()
    }

    fun setWebChromeClient(){
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(
                view: WebView,
                newProgress: Int
            ) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
                if (newProgress < MAX_PROGRESS && progressBar.visibility == ProgressBar.GONE) {
                    progressBar.visibility = ProgressBar.VISIBLE
                }
                if (newProgress == MAX_PROGRESS) {
                    progressBar.visibility = ProgressBar.GONE
                }
            }
        }
    }

    private fun handlePullToRefresh() {
        itemsswipetorefresh.setOnRefreshListener {
            webView.reload()
            itemsswipetorefresh.isRefreshing = false
            progressBar.visibility = ProgressBar.GONE
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Check if the key event was the Back button and if there's history
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        // If it wasn't the Back key or there's no web page history, exit the activity)
        return super.onKeyDown(keyCode, event)
    }

    private fun loadUrl() {
        webView.loadUrl(url)
    }

    private fun disconnected() {
        webView.visibility = View.INVISIBLE
        iv_no_internet.visibility = View.VISIBLE

    }

    private fun connected() {
        webView.visibility = View.VISIBLE
        iv_no_internet.visibility = View.VISIBLE
        loadUrl()
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(broadcastReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(broadcastReceiver)
    }
}