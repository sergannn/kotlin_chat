package com.example.myapplication

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.chats_container, ChatListFragment())
           //     setReorderingAllowed(true)

                    /*if (resources.getBoolean(R.bool.isTablet)) {
                    // Для планшета добавляем пустой фрагмент сообщений
                    replace(R.id.messages_container, PlaceholderFragment())
                }*/
            }
        }
    }
}

class PlaceholderFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return TextView(requireContext()).apply {
            text = "Select a chat to view messages"
            gravity = Gravity.CENTER
        }
    }
}
data class Chat(
    val id: Int,
    val name: String,
    val lastMessage: String? = null,
    val avatarUrl: String? = null // Для демонстрации Glide
)

data class Message(
    val id: Int,
    val text: String,
    val timestamp: Long,
    val sender: String? = "You",
    val senderAvatar: String? = null // Для демонстрации Glide
)

data class ChatDetails(
    val id: Int,
    val name: String,
    val messages: List<Message>
)

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: String? = null
)
data class ChatsResponse(
    val chats: List<Chat>
)
data class MessagesResponse(val messages: List<Message>) // If API returns wrapped messages
data class ChatCreationResponse(val chat: Chat) // If API returns created chat differently
data class MessageResponse(
    val message: Message
)