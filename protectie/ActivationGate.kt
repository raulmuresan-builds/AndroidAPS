package app.aaps.activation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import java.security.MessageDigest

/**
 * Phone-bound activation for AAPS Master (raulmuresan-builds).
 *
 * Code = first 8 characters (hex, upper case) of SHA-256("<secret>:<id>"),
 * where <id> = the phone ANDROID_ID, lower case, without dashes.
 * The check runs only when the main screen opens. It never touches the loop.
 */
object ActivationGate {

    private const val PREFS = "aaps_activare"
    private const val KEY_CODE = "cod_activare"

    @SuppressLint("HardwareIds")
    fun deviceId(context: Context): String =
        (Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "")
            .lowercase()
            .filter { it.isLetterOrDigit() }

    /** The id as shown to the user, in groups of 4: a1b2-c3d4-e5f6-a7b8 */
    fun formattedId(context: Context): String = deviceId(context).chunked(4).joinToString("-")

    fun expectedCode(id: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("${ActivationSecret.VALUE}:$id".toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }.take(8).uppercase()
    }

    fun normalize(code: String): String = code.uppercase().filter { it.isLetterOrDigit() }

    fun isValid(context: Context, code: String): Boolean {
        val id = deviceId(context)
        return id.isNotEmpty() && normalize(code) == expectedCode(id)
    }

    fun isActivated(context: Context): Boolean {
        val stored = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_CODE, null)
            ?: return false
        return isValid(context, stored)
    }

    fun save(context: Context, code: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_CODE, normalize(code))
            .commit()
    }

    fun open(activity: Activity) {
        activity.startActivity(Intent(activity, ActivationActivity::class.java))
    }
}
