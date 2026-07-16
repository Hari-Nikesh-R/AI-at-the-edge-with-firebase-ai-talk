package com.agenticedge.shopdemo.data.remote

import com.agenticedge.shopdemo.data.remote.dto.InsightRequest
import com.agenticedge.shopdemo.data.remote.dto.SessionSummaryRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface BackendApi {

    @POST("api/insights")
    suspend fun postInsight(@Body body: InsightRequest)

    @POST("api/session-summary")
    suspend fun postSessionSummary(@Body body: SessionSummaryRequest)
}
