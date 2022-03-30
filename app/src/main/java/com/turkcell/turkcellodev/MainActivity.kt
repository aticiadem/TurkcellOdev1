package com.turkcell.turkcellodev

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.turkcell.turkcellodev.databinding.ActivityMainBinding
import com.turkcell.turkcellodev.databinding.SelectImageLocationPopupBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonAddImage.setOnClickListener {
            showSelectImagePopup()
        }
    }

    private fun showSelectImagePopup() {
        val dialog = Dialog(this)
        val binding = SelectImageLocationPopupBinding.inflate(
            LayoutInflater.from(this)
        )

        dialog.setContentView(binding.root)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        binding.buttonCamera.setOnClickListener {

        }

        binding.buttonGallery.setOnClickListener {

        }

        dialog.show()
    }

}