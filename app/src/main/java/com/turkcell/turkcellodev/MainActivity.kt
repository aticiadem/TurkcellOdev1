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
import com.turkcell.turkcellodev.databinding.FullScreenImagePopupBinding
import com.turkcell.turkcellodev.databinding.SelectImageLocationPopupBinding
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity(), ImageClick {

    private lateinit var binding: ActivityMainBinding
    private val REQ_CODE_CAMERA = 0
    private val REQ_CODE_GALLERY = 1
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

    private fun checkGalleryPermission() {
        val requestList = ArrayList<String>()
        val permissionState = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        if (!permissionState) {
            requestList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (requestList.isEmpty()) {
            // izin var
            openGallery()
        } else {
            requestPermissions(requestList.toTypedArray(), REQ_CODE_GALLERY)
        }
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

    private var cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                addNewImageToImageList(imageUri)
            }
        }

    private var galleryResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    val imageData = intentFromResult.data
                    if (imageData != null) {
                        addNewImageToImageList(imageData)
                    }
                }
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
                REQ_CODE_GALLERY -> {
                    openGallery()
                }
            }
        }
    }

    override fun onImageClick(image: Image) {
        showImageInFullScreenMode(image)
    }

    override fun onImageLongClick(image: Image) {
        showImageLongClickAlert(image)
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file = createImageFile()
        imageUri = FileProvider.getUriForFile(this, packageName, file)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraResultLauncher.launch(intent)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryResultLauncher.launch(galleryIntent)
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile("resim", ".jpg", dir).apply {
            imagePath = absolutePath
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
            checkGalleryPermission()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addNewImageToImageList(uri: Uri) {
        imageListInGeneral.add(Image(uri))
        adapter.imageList = imageListInGeneral
        binding.recyclerView.adapter = adapter
    }

    private fun showImageInFullScreenMode(image: Image) {
        val dialog = Dialog(this)
        val binding = FullScreenImagePopupBinding.inflate(
            LayoutInflater.from(this)
        )

        dialog.setContentView(binding.root)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        binding.imageView.setImageURI(image.uri)

        dialog.show()
    }

    private fun showImageLongClickAlert(image: Image) {
        val adb = AlertDialog.Builder(this)
        adb.setTitle(getString(R.string.alert))
        adb.setMessage(getString(R.string.are_you_sure_delete_image))
        adb.setPositiveButton(R.string.yes) { _, _ ->
            deleteImage(image)
            Toast.makeText(this, getString(R.string.deleted), Toast.LENGTH_SHORT).show()
        }
        adb.setNegativeButton(getString(R.string.cancel)) { _, _ -> }
        adb.show()
    }

    private fun deleteImage(image: Image) {
        imageListInGeneral.remove(image)
        adapter.imageList = imageListInGeneral
        binding.recyclerView.adapter = adapter
    }

}