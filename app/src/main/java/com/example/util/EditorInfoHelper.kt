package com.example.util

import android.text.InputType
import android.view.inputmethod.EditorInfo

/**
 * Helper to inspect Android EditorInfo flags for sensitive inputs:
 * - IME_FLAG_NO_PERSONALIZED_LEARNING (incognito tabs, private browsing, private mode)
 * - Password input types (text password, web password, visible password, number PIN)
 */
object EditorInfoHelper {

    fun isNoLearningRequested(info: EditorInfo?): Boolean {
        if (info == null) return false
        return (info.imeOptions and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING) != 0
    }

    fun isPasswordField(info: EditorInfo?): Boolean {
        if (info == null) return false
        val clazz = info.inputType and InputType.TYPE_MASK_CLASS
        val variation = info.inputType and InputType.TYPE_MASK_VARIATION
        val isTextPassword = clazz == InputType.TYPE_CLASS_TEXT && (
            variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
        )
        val isNumberPassword = clazz == InputType.TYPE_CLASS_NUMBER && (
            variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
        )
        return isTextPassword || isNumberPassword
    }

    fun isPrivateOrSensitive(info: EditorInfo?): Boolean {
        return isNoLearningRequested(info) || isPasswordField(info)
    }
}
