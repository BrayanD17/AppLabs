package com.labs.applabs.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.labs.applabs.R


class NotificationsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.without_notification_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val backButton = view.findViewById<ImageView>(R.id.backView)
        // Configurar el botón de retroceso
        backButton.setOnClickListener {
            // Regresar a la actividad anterior
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Aquí puedes configurar el RecyclerView si hay notificaciones
        // checkNotifications()
    }

    /*
    private fun checkNotifications() {
        // Lógica para verificar si hay notificaciones
        val hasNotifications = false // Cambiar según tu lógica

        if (hasNotifications) {
            withoutMessage.visibility = View.GONE
            noNotificationsText.visibility = View.GONE
            messageNotification.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            // Configurar el adaptador del RecyclerView
        }
    }
    */

    companion object {
        fun newInstance() = NotificationsFragment()
    }
}