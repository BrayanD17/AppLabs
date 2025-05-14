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
import com.labs.applabs.student.Adapter.NotificationAdapter
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var withoutMessage: ImageView
    private lateinit var noNotificationsText: TextView
    private lateinit var messageNotification: TextView

    private val provider = Provider()

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
        messageNotification = view.findViewById(R.id.messageNotification)
        withoutMessage = view.findViewById(R.id.withoutMessage)

        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        loadNotifications()
    }

    private fun loadNotifications() {
        lifecycleScope.launch {
            val messages = provider.getUserMessages()

            if (messages.isEmpty()) {
                recyclerView.visibility = View.GONE
                noNotificationsText.visibility = View.VISIBLE
                withoutMessage.visibility = View.VISIBLE
                messageNotification.visibility = View.VISIBLE
            } else {
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = NotificationAdapter(messages)
                recyclerView.visibility = View.VISIBLE
                noNotificationsText.visibility = View.GONE
                withoutMessage.visibility = View.GONE
                messageNotification.visibility = View.GONE

                provider.markMessagesAsSeen()
            }
        }
    }

    companion object {
        fun newInstance() = NotificationsFragment()
    }
}
