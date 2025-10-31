package com.gtaoodevelopers.qrcodescannerexample

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.google.zxing.BarcodeFormat

class CustomScannerActivity : AppCompatActivity() {

    private lateinit var barcodeView: DecoratedBarcodeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_scanner)

        // Get reference to scanner
        barcodeView = findViewById(R.id.barcode_scanner)

        // Set QR code only (optional)
        val formats = listOf(BarcodeFormat.QR_CODE)
        barcodeView.barcodeView.decoderFactory = DefaultDecoderFactory(formats)

        // Stop button
        val stopButton: Button = findViewById(R.id.stop_button)
        stopButton.setOnClickListener {
            finish()
        }

        // Start scanning
        barcodeView.resume()
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }
}
