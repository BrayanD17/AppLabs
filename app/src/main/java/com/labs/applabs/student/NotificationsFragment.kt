package com.labs.applabs.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.firebase.Provider
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var noNotificationsText: TextView

    private val provider = Provider()
    private val userId = "gfTos90dNJeX8kkffqIo"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.without_notification_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val backButton = view.findViewById<ImageView>(R.id.backView)
        recyclerView = view.findViewById(R.id.recyclerView)
        noNotificationsText = view.findViewById(R.id.noNotificationsText)

        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        loadNotifications()
    }

    private fun loadNotifications() {
        lifecycleScope.launch {
            val messages = provider.getUserMessages(userId)

            if (messages.isEmpty()) {
                recyclerView.visibility = View.GONE
                noNotificationsText.visibility = View.VISIBLE
            } else {
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = NotificationAdapter(messages)
                recyclerView.visibility = View.VISIBLE
                noNotificationsText.visibility = View.GONE
            }
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            provider.markMessagesAsSeen(userId)
        }
    }

    companion object {
        fun newInstance() = NotificationsFragment()
    }
}
