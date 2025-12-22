package com.example.heaveyequpments.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

// binding.progressBar.visible()
fun View.visible() { visibility = View.VISIBLE }
fun View.gone() { visibility = View.GONE }

// Simplified Snackbar
fun View.showSnackbar(message: String, length: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(this, message, length).show()
}