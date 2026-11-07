package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.util.regex.Pattern

enum class SmartActionType {
    OPEN_URL,
    SEND_EMAIL,
    DIAL_PHONE,
    COPY_OTP,
    VIEW_MAP
}

data class SmartClipAction(
    val type: SmartActionType,
    val label: String,
    val iconEmoji: String,
    val actionText: String
)

object SmartClipboardAnalyzer {
    private val URL_PATTERN = Pattern.compile(
        "(?:https?://|www\\.)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]",
        Pattern.CASE_INSENSITIVE
    )
    private val EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}",
        Pattern.CASE_INSENSITIVE
    )
    private val PHONE_PATTERN = Pattern.compile(
        "(?:\\+?\\d{1,3}[- ]?)?(?:\\(?\\d{2,5}\\)?[- ]?)(?:\\d[- ]?){5,12}\\d"
    )
    private val OTP_PATTERN = Pattern.compile(
        "(?:otp|code|pin|verification)\\s*(?:is|:|-)?\\s*(\\d{4,8})",
        Pattern.CASE_INSENSITIVE
    )

    fun analyze(text: String): SmartClipAction? {
        val trimmed = text.trim()
        if (trimmed.length < 3) return null

        // 1. Check OTP / Verification code
        val otpMatcher = OTP_PATTERN.matcher(trimmed)
        if (otpMatcher.find()) {
            val code = otpMatcher.group(1) ?: ""
            if (code.isNotBlank()) {
                return SmartClipAction(
                    type = SmartActionType.COPY_OTP,
                    label = "Code: $code",
                    iconEmoji = "🔑",
                    actionText = code
                )
            }
        }

        // 2. Check URL / Link
        val urlMatcher = URL_PATTERN.matcher(trimmed)
        if (urlMatcher.find()) {
            var url = urlMatcher.group(0) ?: ""
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://$url"
            }
            return SmartClipAction(
                type = SmartActionType.OPEN_URL,
                label = "Open Link",
                iconEmoji = "🌐",
                actionText = url
            )
        }

        // 3. Check Email
        val emailMatcher = EMAIL_PATTERN.matcher(trimmed)
        if (emailMatcher.find()) {
            val email = emailMatcher.group(0) ?: ""
            return SmartClipAction(
                type = SmartActionType.SEND_EMAIL,
                label = "Email: $email",
                iconEmoji = "✉️",
                actionText = email
            )
        }

        // 4. Check Phone number
        val phoneMatcher = PHONE_PATTERN.matcher(trimmed)
        if (phoneMatcher.find() && trimmed.length < 120) {
            val phone = phoneMatcher.group(0)?.trim() ?: ""
            val digits = phone.filter { it.isDigit() }
            if (digits.length in 7..15) {
                return SmartClipAction(
                    type = SmartActionType.DIAL_PHONE,
                    label = "Call $phone",
                    iconEmoji = "📞",
                    actionText = phone
                )
            }
        }

        return null
    }

    fun executeAction(context: Context, action: SmartClipAction) {
        try {
            when (action.type) {
                SmartActionType.OPEN_URL -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(action.actionText)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
                SmartActionType.SEND_EMAIL -> {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${action.actionText}")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
                SmartActionType.DIAL_PHONE -> {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${action.actionText}")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
                SmartActionType.COPY_OTP -> {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                    cm?.setPrimaryClip(android.content.ClipData.newPlainText("OTP", action.actionText))
                    Toast.makeText(context, "Code copied: ${action.actionText}", Toast.LENGTH_SHORT).show()
                }
                SmartActionType.VIEW_MAP -> {}
            }
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open action", Toast.LENGTH_SHORT).show()
        }
    }
}
