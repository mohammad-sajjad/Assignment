package com.assignment

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class HomeExtra (val userName: String? = null, val image: Uri? = null, val email: String? = null): Parcelable