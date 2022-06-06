package com.staygrateful.app.server.extension

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

fun Context.isDarkMode(): Boolean {
    return resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES
}

fun Context.drawable(@DrawableRes drawableRes: Int): Drawable? =
        AppCompatResources.getDrawable(this, drawableRes)

fun Context.color(@ColorRes colorRes: Int): Int =
        ContextCompat.getColor(this, colorRes)

fun Context.px(value: Int): Int {
    return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), this.resources?.displayMetrics))
}

fun Any.getContext() : Context? {
    var context: Context? = null
    when (this) {
        is Context -> {
            context = this
        }
        is Activity -> {
            context = this
        }
        is Fragment -> {
            context = this.context
        }
        is View -> {
            context = this.context
        }
    }
    return context
}

fun Any.showToast(message: String?, duration: Int = Toast.LENGTH_SHORT) {
    val context = this.getContext()
    if (context != null) {
        Toast.makeText(context, message, duration).show()
    }
}

@ColorInt
fun Context.getColorInt(colorRes: Int) : Int {
    return ContextCompat.getColor(this, colorRes)
}

fun <T: Activity> Context?.startActivity(clazz: Class<T>) {
    if (this is Activity) {
        this.startActivity(Intent(this, clazz))
    }
}

fun Context.drawable(resourceName: String): Drawable? = ContextCompat.getDrawable(this, resources.getIdentifier(resourceName, "drawable", packageName))