package com.example.myapplication

import ChatsViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentChatListBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class ChatListFragment : Fragment() {
    private lateinit var binding: FragmentChatListBinding
    private val viewModel: ChatsViewModel by viewModels()
    private val isTabletMode by lazy { resources.getBoolean(R.bool.isTablet) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        viewModel.loadChats()

        // Handle initial state for tablets
        if (isTabletMode && viewModel.chats.value?.data?.isNotEmpty() == true) {
            showFirstChatInTabletMode()
        }
    }

    private fun setupRecyclerView() {
        val adapter = ChatsAdapter { chat ->
            if (isTabletMode) {
                // Tablet mode - show in right pane
                showChatInTabletMode(chat.id)
            } else {
                // Phone mode - full screen navigation
                navigateToMessagesScreen(chat.id)
            }
        }

        binding.chatsRecyclerView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.chats.observe(viewLifecycleOwner) { response ->
            when {
                response.success -> {
                    binding.progressBar.visibility = View.GONE
                    adapter.submitList(response.data)
                    binding.emptyState.visibility =
                        if (response.data.isNullOrEmpty()) View.VISIBLE else View.GONE

                    // Auto-show first chat in tablet mode
                    if (isTabletMode && response.data?.isNotEmpty() == true) {
                        showFirstChatInTabletMode()
                    }
                }
                response.error != null -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), response.error, Toast.LENGTH_SHORT).show()
                }
                else -> binding.progressBar.visibility = View.VISIBLE
            }
        }
    }

    private fun showFirstChatInTabletMode() {
        viewModel.chats.value?.data?.firstOrNull()?.let { firstChat ->
            showChatInTabletMode(firstChat.id)
        }
    }

    private fun showChatInTabletMode(chatId: Int) {
        parentFragmentManager.commit {
            replace(R.id.messages_container, MessagesFragment.newInstance(chatId))
            setReorderingAllowed(true)
            // Don't add to back stack for tablet mode
        }
    }

    private fun navigateToMessagesScreen(chatId: Int) {
        // Режим телефона - заменяем основной контейнер
        val activity = requireActivity() as MainActivity

        // Показываем контейнер сообщений
        activity.findViewById<FrameLayout>(R.id.messages_container).visibility = View.VISIBLE
        activity.findViewById<FrameLayout>(R.id.chats_container).visibility = View.GONE

        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.messages_container, MessagesFragment.newInstance(chatId))
            .addToBackStack("messages")
            .commit()
    }

    private fun setupListeners() {
        binding.fab.setOnClickListener {
            showCreateChatDialog()
        }
    }
    private fun showCreateChatDialog() {
        val textInputLayout = TextInputLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            hint = "Chat name"
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
        }

        val editText = TextInputEditText(requireContext()).apply {
            setSingleLine()
        }

        textInputLayout.addView(editText)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create new chat")
            .setView(textInputLayout)
            .setPositiveButton("Create") { _, _ ->
                val name = editText.text?.toString()?.trim()
                if (!name.isNullOrEmpty()) {
                    viewModel.createChat(name)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()

        editText.requestFocus()
    }
}