package com.gtaoodevelopers.qrcodescannerexample.network

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {

    @FormUrlEncoded
    @POST("attendance/update")
    suspend fun updateAttendance(
        @Field("booking_id") bookingId: String
    ): Response<String>

    @FormUrlEncoded
    @POST("attendance/update-kit")
    suspend fun updateKit(
        @Field("booking_id") bookingId: String
    ): Response<String>
}