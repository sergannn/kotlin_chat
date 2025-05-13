package com.example.myapplication


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MessengerApiService {
    @GET("mipt_network/chat")
    suspend fun getChatMessages(@Query("id") chatId: Int): Response<ChatDetails>

    @POST("mipt_network/msg")
    suspend fun sendMessage(
        @Query("id") chatId: Int,
        @Query("text") text: String
    ): Response<MessageResponse> // Assuming API returns wrapped response

    @POST("mipt_network/create_chat")
    suspend fun createChat(@Query("name") name: String):Response<ChatsResponse> // Updated!
    @GET("mipt_network/chats")
    suspend fun getChats(): Response<ChatsResponse> // Updated!
/*
    @GET("mipt_network/chat")
    suspend fun getChatMessages(@Query("id") chatId: Int): Response<ChatDetails>

    @POST("mipt_network/msg")
    suspend fun sendMessage(
        @Query("id") chatId: Int,
        @Query("text") text: String
    ): Response<List<Message>>

    @POST("mipt_network/create_chat")
    suspend fun createChat(@Query("name") name: String): Response<List<Chat>>*/
}

object ApiClient {
    private const val BASE_URL = "http://emil-international.ru/"
    private const val OAUTH_TOKEN = "0123456789" // Замените на реальный токен
//OAuth
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("OAuth", "0123456789")
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val messengerApiService: MessengerApiService = retrofit.create(MessengerApiService::class.java)
}
class MessengerRepository {
    private val apiService = ApiClient.messengerApiService

    suspend fun getChats(): ApiResponse<List<Chat>> {
        return try {
            val response = apiService.getChats()
            if (response.isSuccessful) {
                ApiResponse(true, response.body()?.chats) // Extract the list
            } else {
                ApiResponse(false, null, "Failed to load chats")
            }
        } catch (e: Exception) {
            ApiResponse(false, null, e.message ?: "Unknown error")
        }
    }

    suspend fun getChatMessages(chatId: Int): ApiResponse<ChatDetails> {
        return try {
            val response = apiService.getChatMessages(chatId)
            if (response.isSuccessful) {
                ApiResponse(true, response.body())
            } else {
                ApiResponse(false, null, "Failed to load messages")
            }
        } catch (e: Exception) {
            ApiResponse(false, null, e.message ?: "Unknown error")
        }
    }
    suspend fun sendMessage(chatId: Int, text: String): ApiResponse<Message> {
        return try {
            val response = apiService.sendMessage(chatId, text)
            if (response.isSuccessful) {
                response.body()?.let {
                    ApiResponse(
                        success = true,
                        data = it.message  // Extract the Message object from wrapper
                    )
                } ?: ApiResponse(false, null, "Empty response body")
            } else {
                ApiResponse(
                    false,
                    null,
                    "Error ${response.code()}: ${response.errorBody()?.string()}"
                )
            }
        } catch (e: Exception) {
            ApiResponse(false, null, e.message ?: "Unknown error")
        }
    }

    suspend fun createChat(name: String):  ApiResponse<List<Chat>> {
        return try {
            val response = apiService.createChat(name)
            if (response.isSuccessful) {
                ApiResponse(true, response.body()?.chats // Extract the list
                )
            } else {
                ApiResponse(
                    success = false,
                    error = "Failed to create chat: ${response.code()}",
                    data = TODO()
                )
            }
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = e.localizedMessage ?: "Unknown error",
                data = TODO()
            )
        }
    }
}