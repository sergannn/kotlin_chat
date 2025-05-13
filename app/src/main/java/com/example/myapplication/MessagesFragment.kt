package com.example.myapplication

import MessagesViewModel
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentMessagesBinding

class MessagesFragment : Fragment() {
    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MessagesViewModel by viewModels()
    private var chatId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            chatId = it.getInt(ARG_CHAT_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupListeners()

        if (chatId != -1) {
            viewModel.loadMessages(chatId)
        }
    }

    private fun setupRecyclerView() {
        val adapter = MessagesAdapter()
        binding.messagesRecyclerView.adapter = adapter
        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.messages.observe(viewLifecycleOwner) { response ->
            when {
                response.success -> {
                    binding.progressBar.visibility = View.GONE
                    response.data?.let { chatDetails ->
                       // binding.chatName.text = chatDetails.name
                        adapter.submitList(chatDetails.messages)
                        binding.emptyState.visibility =
                            if (chatDetails.messages.isEmpty()) View.VISIBLE else View.GONE

                        // Прокрутка к последнему сообщению
                        if (chatDetails.messages.isNotEmpty()) {
                            binding.messagesRecyclerView.scrollToPosition(chatDetails.messages.size - 1)
                        }
                    }
                }
                response.error != null -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), response.error, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupObservers() {
        viewModel.messages.observe(viewLifecycleOwner) { response ->
            // Обработка состояния загрузки, ошибок и данных
        }
    }

    private fun setupListeners() {
        // Handle send icon click
        binding.messageInputLayout.setEndIconOnClickListener {
            sendMessage()
        }

        // Handle keyboard's send action
        binding.messageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }
    }

    private fun sendMessage() {
        val message = binding.messageInput.text?.toString()?.trim()
        if (!message.isNullOrEmpty()) {
            viewModel.sendMessage(chatId, message)
            binding.messageInput.text?.clear()
            binding.messagesRecyclerView.smoothScrollToPosition(viewModel.messages.value?.data?.messages?.size ?: 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CHAT_ID = "chat_id"

        fun newInstance(chatId: Int): MessagesFragment {
            return MessagesFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_CHAT_ID, chatId)
                }
            }
        }
    }
}