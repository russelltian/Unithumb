package com.example.hat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {
    var socket_obj: SocketClient? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        socket_obj = SocketClient(address = "10.0.2.2", port = 8000)
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_left_signal).setOnClickListener {
            socket_obj?.sendMessage("left")
        }
        view.findViewById<Button>(R.id.button_right_signal).setOnClickListener {
            socket_obj?.sendMessage("right")
        }
        view.findViewById<Button>(R.id.button_return_main).setOnClickListener {
            socket_obj?.close()
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
    }
}