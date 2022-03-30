package com.turkcell.turkcellodev

import android.net.Uri

data class Image(
    val id: Int,
    val uri: Uri
)

val imageListInGeneral: ArrayList<Image> = ArrayList()
