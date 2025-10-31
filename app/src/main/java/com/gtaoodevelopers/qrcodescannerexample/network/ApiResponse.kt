package com.gtaoodevelopers.qrcodescannerexample.network


data class ApiResponse(
    val message: String? = null,
    val success: Boolean? = null,
    val data: Any? = null,
    val error: String? = null
    // Add any other fields that might be in the response
)