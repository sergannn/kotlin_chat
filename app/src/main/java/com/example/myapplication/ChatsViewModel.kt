import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ApiResponse
import com.example.myapplication.Chat
import com.example.myapplication.ChatDetails
import com.example.myapplication.MessengerRepository
import kotlinx.coroutines.launch

class ChatsViewModel : ViewModel() {
    private val repository = MessengerRepository()
    private val _chats = MutableLiveData<ApiResponse<List<Chat>>>()
    val chats: LiveData<ApiResponse<List<Chat>>> = _chats


    fun loadChats() {
        viewModelScope.launch {
            _chats.value = ApiResponse(true, null) // loading state
            _chats.value = repository.getChats()
        }
    }

    fun createChat(name: String) {
        viewModelScope.launch {
            _chats.value = ApiResponse(true, null) // loading state
            val result = repository.createChat(name)
            if (result.success) {
                loadChats() // Refresh the list
            } else {
                _chats.value = result
            }
        }
    }
}

class MessagesViewModel : ViewModel() {
    private val repository = MessengerRepository()
    private val _messages = MutableLiveData<ApiResponse<ChatDetails>>()
    val messages: LiveData<ApiResponse<ChatDetails>> = _messages

    fun loadMessages(chatId: Int) {
        viewModelScope.launch {
            _messages.value = ApiResponse(true, null) // loading state
            _messages.value = repository.getChatMessages(chatId)
        }
    }

    fun sendMessage(chatId: Int, text: String) {
        viewModelScope.launch {
            _messages.value = ApiResponse(true, null) // loading state
            val result = repository.sendMessage(chatId, text)
            if (result.success) {
                loadMessages(chatId) // Refresh the messages
            } else {
                _messages.value = ApiResponse(false, null, result.error)
            }
        }
    }
}