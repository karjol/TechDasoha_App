package com.gtaoodevelopers.qrcodescannerexample

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.gtaoodevelopers.qrcodescannerexample.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var btnScan: Button
    private lateinit var btnUpdateAttendance: Button
    private lateinit var btnUpdateKit: Button
    private lateinit var updateButtonsContainer: LinearLayout
    public var scannedData: String? = null

    // Track update status per QR code using the booking ID with SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences
    private val updatedBookingIds = mutableSetOf<String>()
    private var currentBookingId: String? = null

    companion object {
        private const val TAG = "MainActivity"
        private const val PREFS_NAME = "QRCodeUpdates"
        private const val UPDATED_IDS_KEY = "updated_booking_ids"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        loadUpdatedBookingIds()

        initializeViews()
        setupClickListeners()

        updateButtonsContainer.visibility = LinearLayout.GONE
    }

    private fun initializeViews() {
        btnScan = findViewById(R.id.btnScan)
        btnUpdateAttendance = findViewById(R.id.btnUpdateAttendance)
        btnUpdateKit = findViewById(R.id.btnUpdateKit)
        updateButtonsContainer = findViewById(R.id.updateButtonsContainer)
    }

    private fun setupClickListeners() {
        btnScan.setOnClickListener {
            startQRScan()
        }

        btnUpdateAttendance.setOnClickListener {
            currentBookingId?.let { bookingId ->
                // Check with server if already updated before showing message
                checkIfAlreadyUpdated("attendance", bookingId)
            } ?: run {
                Toast.makeText(this, "Please scan a QR code first", Toast.LENGTH_SHORT).show()
            }
        }

        btnUpdateKit.setOnClickListener {
            currentBookingId?.let { bookingId ->
                // Check with server if already updated before showing message
                checkIfAlreadyUpdated("kit", bookingId)
            } ?: run {
                Toast.makeText(this, "Please scan a QR code first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // NEW METHOD: Check with server if already updated
    private fun checkIfAlreadyUpdated(type: String, bookingId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // First check local storage for quick response
                val localKey = "${bookingId}_$type"
                if (updatedBookingIds.contains(localKey)) {
                    // Verify with server
                    val isActuallyUpdated = withContext(Dispatchers.IO) {
                        verifyWithServer(type, bookingId)
                    }

                    if (isActuallyUpdated) {
                        Toast.makeText(
                            this@MainActivity,
                            if (type == "attendance") "Attendance is already updated for this ticket!"
                            else "Kit status is already updated for this ticket!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Server says not updated, so allow update
                        if (type == "attendance") callFirstUrl() else callSecondUrl()
                    }
                } else {
                    // Not in local storage, allow update
                    if (type == "attendance") callFirstUrl() else callSecondUrl()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking update status: ${e.message}")
                // If error checking with server, allow update to be safe
                if (type == "attendance") callFirstUrl() else callSecondUrl()
            }
        }
    }

    // NEW METHOD: Verify with server if actually updated
    private suspend fun verifyWithServer(type: String, bookingId: String): Boolean {
        return try {
            // You need to add API endpoints to check status
            // For now, we'll assume it's updated if in local storage
            // TODO: Add actual API call to verify status
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun startQRScan() {
        val integrator = IntentIntegrator(this)
        integrator.setOrientationLocked(true)
        integrator.setPrompt("Scan QR Code")
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                scannedData = result.contents
                currentBookingId = extractBookingIdFromUrl(scannedData ?: "")

                Toast.makeText(this, "Scanned: $scannedData", Toast.LENGTH_LONG).show()
                btnScan.visibility = Button.GONE
                updateButtonsContainer.visibility = LinearLayout.VISIBLE

                Log.d(TAG, "Scanned QR with booking ID: $currentBookingId")
                Log.d(TAG, "Current updated IDs: $updatedBookingIds")

                // Check if this QR code was already processed
                checkAndUpdateButtonStates()

            } else {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun checkAndUpdateButtonStates() {
        currentBookingId?.let { bookingId ->
            val isAttendanceUpdated = updatedBookingIds.contains("${bookingId}_attendance")
            val isKitUpdated = updatedBookingIds.contains("${bookingId}_kit")

            Log.d(TAG, "Checking booking ID: $bookingId - Attendance: $isAttendanceUpdated, Kit: $isKitUpdated")

            if (isAttendanceUpdated && isKitUpdated) {
                // Both already updated - show message immediately
                Toast.makeText(this, "Both attendance and kit status are already updated for this ticket!", Toast.LENGTH_LONG).show()
            } else if (isAttendanceUpdated) {
                Toast.makeText(this, "Attendance is already updated for this ticket!", Toast.LENGTH_SHORT).show()
            } else if (isKitUpdated) {
                Toast.makeText(this, "Kit status is already updated for this ticket!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Load previously updated booking IDs from SharedPreferences
    private fun loadUpdatedBookingIds() {
        try {
            val savedIds = sharedPreferences.getStringSet(UPDATED_IDS_KEY, mutableSetOf())
            savedIds?.let {
                updatedBookingIds.clear()
                updatedBookingIds.addAll(it)
                Log.d(TAG, "Loaded updated IDs from SharedPreferences: $updatedBookingIds")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading updated IDs: ${e.message}")
        }
    }

    // Save updated booking IDs to SharedPreferences
    private fun saveUpdatedBookingIds() {
        try {
            val editor = sharedPreferences.edit()
            // Create a new set to avoid the issue with SharedPreferences and mutable sets
            val setToSave = HashSet(updatedBookingIds)
            editor.putStringSet(UPDATED_IDS_KEY, setToSave)
            val success = editor.commit() // Use commit() for immediate result

            if (success) {
                Log.d(TAG, "Successfully saved updated IDs: $updatedBookingIds")
            } else {
                Log.e(TAG, "Failed to save updated IDs")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving updated IDs: ${e.message}")
        }
    }

    // First Button - First URL: https://seellab.com/techdasoha/api/attendance/update
    private fun callFirstUrl() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val scannedText = scannedData ?: "default_booking_id"
                val bookingId = extractBookingIdFromUrl(scannedText)

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.updateAttendance(bookingId)
                }

                if (response.isSuccessful) {
                    // Mark this booking ID's attendance as updated and save persistently
                    val updateKey = "${bookingId}_attendance"
                    updatedBookingIds.add(updateKey)
                    saveUpdatedBookingIds()

                    Log.d(TAG, "Added to updated IDs: $updateKey")
                    Log.d(TAG, "Current updated IDs after addition: $updatedBookingIds")

                    // Show success screen for NEW updates
                    showSuccessScreen(
                        message = "Successfully Updated",
                        actionType = "attendance",
                        bookingId = bookingId
                    )

                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Second Button - Second URL: https://seellab.com/techdasoha/api/attendance/update-kit
    private fun callSecondUrl() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val scannedText = scannedData ?: "default_booking_id"
                val bookingId = extractBookingIdFromUrl(scannedText)

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.updateKit(bookingId)
                }

                if (response.isSuccessful) {
                    // Mark this booking ID's kit as updated and save persistently
                    val updateKey = "${bookingId}_kit"
                    updatedBookingIds.add(updateKey)
                    saveUpdatedBookingIds()

                    Log.d(TAG, "Added to updated IDs: $updateKey")
                    Log.d(TAG, "Current updated IDs after addition: $updatedBookingIds")

                    // Show success screen for NEW updates
                    showSuccessScreen(
                        message = "Successfully Updated",
                        actionType = "kit",
                        bookingId = bookingId
                    )

                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Kit update failed: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Add this new method to show success screen
    private fun showSuccessScreen(message: String, actionType: String, bookingId: String) {
        val intent = Intent(this, SuccessActivity::class.java)
        intent.putExtra(SuccessActivity.EXTRA_MESSAGE, message)
        intent.putExtra(SuccessActivity.EXTRA_ACTION_TYPE, actionType)
        intent.putExtra(SuccessActivity.EXTRA_BOOKING_ID, bookingId)
        startActivity(intent)
    }

    private fun extractBookingIdFromUrl(url: String): String {
        return try {
            url.substringAfterLast("/")
        } catch (e: Exception) {
            "default_id"
        }
    }

    // Add this method to clear all stored data (for testing)
    private fun clearAllStoredData() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        updatedBookingIds.clear()
        Log.d(TAG, "Cleared all stored data")
    }
}