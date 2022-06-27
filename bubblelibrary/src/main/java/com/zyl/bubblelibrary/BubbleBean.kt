package com.zyl.bubblelibrary

import android.graphics.Bitmap
import org.jbox2d.common.Vec2

data class BubbleBean(
    val id: Int?,
    val position: Vec2,
    val type: BubbleWorldType,
    var isSelector: Boolean = false,
    var isAnim: Boolean = false,
)


data class BodyBitmapInfo(
    var id: Int,
    var notesType: Int,
    var notesExplain: String,
    var bitmap: Bitmap
)