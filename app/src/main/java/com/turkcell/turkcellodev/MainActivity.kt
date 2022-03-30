package com.turkcell.turkcellodev

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.turkcell.turkcellodev.databinding.ActivityMainBinding
import com.turkcell.turkcellodev.databinding.SelectImageLocationPopupBinding
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity(), ImageClick {

    private lateinit var binding: ActivityMainBinding
    private val REQ_CODE_CAMERA = 0
    private lateinit var imagePath: String
    private lateinit var imageUri: Uri
    private lateinit var adapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ImageAdapter(this)
        adapter.imageList = imageListInGeneral

        binding.recyclerView.adapter = adapter

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
            checkCameraPermission()
            dialog.dismiss()
        }

        binding.buttonGallery.setOnClickListener {

        }

        dialog.show()
    }

    private fun checkCameraPermission() {
        val requestList = ArrayList<String>()
        var permissionState = ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (!permissionState) {
            requestList.add(Manifest.permission.CAMERA)
        }

        permissionState = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        if (!permissionState) {
            requestList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        permissionState = ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        if (!permissionState) {
            requestList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (requestList.size == 0) {
            // izin var
            openCamera()
        } else {
            requestPermissions(requestList.toTypedArray(), REQ_CODE_CAMERA)
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file = createImageFile()
        imageUri = FileProvider.getUriForFile(this, packageName, file)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraResultLauncher.launch(intent)
    }

    private var cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                addNewImageToImageList(imageUri)
                println("Resim cekildi $imageUri")
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        var isItApproved = true
        for (gr in grantResults) {
            if (gr != PackageManager.PERMISSION_GRANTED) {
                isItApproved = false
                break
            }
        }

        if (!isItApproved) {
            var dontShowAgain = false
            for (permission in permissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    // reddedilmistir -> Sadece reddet denmistir tamamen reddet degil
                } else if (ContextCompat.checkSelfPermission(this,
                        permission) == PackageManager.PERMISSION_GRANTED
                ) {
                    // onaylandi
                } else {
                    // Tekrar gosterme demek
                    dontShowAgain = true
                    break
                }
            }

            if (dontShowAgain) {
                val adb = AlertDialog.Builder(this)
                adb.setTitle(getString(R.string.need_permission))
                    .setMessage(getString(R.string.go_settings_and_allow_permission))
                    .setPositiveButton(getString(R.string.settings)) { _, _ ->
                        openSettings()
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
            }

        } else {
            when (requestCode) {
                REQ_CODE_CAMERA -> {
                    openCamera()
                }
            }
        }
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile("resim", ".jpg", dir).apply {
            imagePath = absolutePath
        }
    }

    private fun addNewImageToImageList(uri: Uri) {
        val lastItemId = imageListInGeneral.size
        println("addNewImageToImageList lastItemId ${imageListInGeneral.size}")
        imageListInGeneral.add(Image(lastItemId + 1, uri))
        adapter.imageList = imageListInGeneral
    }

    override fun onImageClick(image: Image) {
        // todo we will update here
        Toast.makeText(this, "Default click toast message", Toast.LENGTH_SHORT).show()
    }

}