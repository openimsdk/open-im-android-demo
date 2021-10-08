package cn.mtjsoft.inputview.entity

import androidx.annotation.DrawableRes
import java.io.Serializable

data class FunctionEntity(@DrawableRes val imgResId: Int, val name: String) : Serializable
