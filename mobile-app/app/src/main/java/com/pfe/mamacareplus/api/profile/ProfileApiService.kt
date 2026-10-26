package com.pfe.mamacareplus.api.profile

import com.pfe.mamacareplus.model.request.profile.ChangePasswordRequest
import com.pfe.mamacareplus.model.request.profile.FemmeProfileRequest
import com.pfe.mamacareplus.model.response.auth.MessageResponse
import com.pfe.mamacareplus.model.response.profile.FemmeProfileResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Part

interface ProfileApiService {

    @GET("api/femmes/{femmeId}")
    suspend fun getProfile(
        @Path("femmeId") femmeId: Int
    ): Response<FemmeProfileResponse>

    @GET("api/femmes/user/{userId}")
    suspend fun getProfileByUserId(
        @Path("userId") userId: Int
    ): Response<FemmeProfileResponse>

    @PUT("api/femmes/{femmeId}")
    suspend fun updateProfile(
        @Path("femmeId") femmeId: Int,
        @Body request: FemmeProfileRequest
    ): Response<FemmeProfileResponse>

    @Multipart
    @POST("api/femmes/{femmeId}/avatar")
    suspend fun uploadAvatar(
        @Path("femmeId") femmeId: Int,
        @Part avatar: MultipartBody.Part
    ): Response<FemmeProfileResponse>

    @PUT("api/femmes/{femmeId}/change-password")
    suspend fun changePassword(
        @Path("femmeId") femmeId: Int,
        @Body request: ChangePasswordRequest
    ): Response<MessageResponse>
}
