package com.plantsnap.data.wikipedia

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface WikipediaApi {
    @Headers("User-Agent: PlantSnap-Android/1.0 (https://github.com/PhongLe7de/PlantSnap)")
    @GET("api/rest_v1/page/summary/{title}")
    suspend fun summary(@Path("title") title: String): WikipediaSummaryDto
}
