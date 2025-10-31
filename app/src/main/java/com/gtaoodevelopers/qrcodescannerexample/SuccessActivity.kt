package com.gtaoodevelopers.qrcodescannerexample

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SuccessActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MESSAGE = "success_message"
        const val EXTRA_ACTION_TYPE = "action_type"
        const val EXTRA_BOOKING_ID = "booking_id"
    }

    private lateinit var tvSuccessMessage: TextView
    private lateinit var btnOk: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)

        initializeViews()
        setupData()
        setupClickListeners()
    }

    private fun initializeViews() {
        tvSuccessMessage = findViewById(R.id.tvSuccessMessage)
        btnOk = findViewById(R.id.btnOk)
    }

    private fun setupData() {
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Successfully Updated"
        tvSuccessMessage.text = message
    }

    private fun setupClickListeners() {
        btnOk.setOnClickListener {
            val actionType = intent.getStringExtra(EXTRA_ACTION_TYPE) ?: "attendance"

            when (actionType) {
                "attendance" -> {
                    // Go back to main screen with 2 buttons visible
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                }
                "kit" -> {
                    // Go back to scan QR code screen
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    intent.putExtra("reset_scan", true)
                    startActivity(intent)
                }
            }
            finish()
        }
    }
}