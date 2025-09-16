package com.example.ocrapp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Receipt(val id: String?= null, val location: String? = null, val sum: Float? = null, val date: String? = null, val img: String? = null, val imgName: String? = null): Parcelable {

}
