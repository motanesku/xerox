package com.example.phaserhelper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.phaserhelper.databinding.ActivityMainBinding
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val prefs by lazy { getSharedPreferences("phaser_helper", Context.MODE_PRIVATE) }

    private val pickPdfLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            printPdf(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val savedAddress = prefs.getString(KEY_PRINTER_ADDRESS, "") ?: ""
        binding.printerAddressInput.setText(savedAddress)
        refreshStatus()

        binding.saveButton.setOnClickListener {
            val address = binding.printerAddressInput.text?.toString()?.trim().orEmpty()
            if (address.isBlank()) {
                toast("Introdu adresa imprimantei.")
                return@setOnClickListener
            }
            prefs.edit().putString(KEY_PRINTER_ADDRESS, address).apply()
            refreshStatus()
            toast("Imprimanta a fost salvată.")
        }

        binding.openPrinterPageButton.setOnClickListener {
            val address = binding.printerAddressInput.text?.toString()?.trim().orEmpty()
            if (address.isBlank()) {
                toast("Salvează mai întâi IP-ul imprimantei.")
                return@setOnClickListener
            }
            openPrinterWebPage(address)
        }

        binding.openWifiButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }

        binding.choosePdfButton.setOnClickListener {
            pickPdfLauncher.launch(arrayOf("application/pdf"))
        }
    }

    private fun refreshStatus() {
        val printer = prefs.getString(KEY_PRINTER_ADDRESS, null)
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ssid = wifiInfo?.ssid?.removePrefix("\"")?.removeSuffix("\"") ?: "necunoscut"

        val printerText = if (printer.isNullOrBlank()) "nesetată" else printer
        binding.statusText.text = "Status: imprimantă $printerText | Wi‑Fi curent: $ssid"
    }

    private fun openPrinterWebPage(address: String) {
        val url = when {
            address.startsWith("http://") || address.startsWith("https://") -> address
            else -> "http://$address"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun printPdf(uri: Uri) {
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "Phaser3020-${System.currentTimeMillis()}"
        val adapter: PrintDocumentAdapter = PdfUriPrintAdapter(this, uri, jobName)
        printManager.print(
            jobName,
            adapter,
            PrintAttributes.Builder().build()
        )
        toast("S-a deschis dialogul de print.")
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val KEY_PRINTER_ADDRESS = "printer_address"
    }
}

private class PdfUriPrintAdapter(
    private val context: Context,
    private val uri: Uri,
    private val jobName: String
) : PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: android.os.CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }

        val info = PrintDocumentInfo.Builder(jobName)
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .build()
        callback.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<out android.print.PageRange>?,
        destination: android.os.ParcelFileDescriptor,
        cancellationSignal: android.os.CancellationSignal?,
        callback: WriteResultCallback
    ) {
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { inputPfd ->
                FileInputStream(inputPfd.fileDescriptor).use { input ->
                    FileOutputStream(destination.fileDescriptor).use { output ->
                        input.copyTo(output)
                    }
                }
            } ?: throw IllegalStateException("Nu pot deschide PDF-ul selectat.")

            if (cancellationSignal?.isCanceled == true) {
                callback.onWriteCancelled()
                return
            }

            callback.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
        } catch (e: Exception) {
            callback.onWriteFailed(e.message ?: "Eroare la pregătirea PDF-ului pentru print.")
        }
    }
}
