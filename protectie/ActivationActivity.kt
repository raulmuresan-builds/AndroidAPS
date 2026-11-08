package app.aaps.activation

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import app.aaps.ComposeMainActivity

/** Activation screen. Plain Android views, no link to the rest of AAPS. */
class ActivationActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (24 * resources.displayMetrics.density).toInt()
        val gap = (16 * resources.displayMetrics.density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        fun add(view: View) {
            root.addView(view, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = gap })
        }

        add(TextView(this).apply {
            text = "Activare AAPS"
            textSize = 24f
            setTypeface(typeface, Typeface.BOLD)
        })
        add(TextView(this).apply {
            text = "Această versiune AAPS trebuie activată pe acest telefon. " +
                "Trimite ID-ul de mai jos persoanei care ți-a instalat aplicația și introdu codul primit."
            textSize = 16f
        })
        val formatted = ActivationGate.formattedId(this)
        add(TextView(this).apply {
            text = "ID telefon:\n$formatted"
            textSize = 22f
            typeface = Typeface.MONOSPACE
            setTextIsSelectable(true)
        })
        add(Button(this).apply {
            text = "Copiază ID-ul"
            setOnClickListener {
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("ID AAPS", formatted))
                Toast.makeText(this@ActivationActivity, "ID copiat", Toast.LENGTH_SHORT).show()
            }
        })
        val input = EditText(this).apply {
            hint = "Cod de activare (8 caractere)"
            textSize = 22f
            typeface = Typeface.MONOSPACE
            gravity = Gravity.CENTER
            isSingleLine = true
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS or
                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            filters = arrayOf(InputFilter.LengthFilter(12), InputFilter.AllCaps())
        }
        add(input)
        val status = TextView(this).apply {
            textSize = 16f
            setTextColor(Color.rgb(198, 40, 40))
        }
        add(Button(this).apply {
            text = "Activează"
            setOnClickListener {
                val code = input.text.toString()
                if (ActivationGate.isValid(this@ActivationActivity, code)) {
                    ActivationGate.save(this@ActivationActivity, code)
                    startActivity(
                        Intent(this@ActivationActivity, ComposeMainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                    finish()
                } else {
                    status.text = "Cod invalid pentru acest telefon."
                }
            }
        })
        add(status)

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(root)
        }
        scroll.setOnApplyWindowInsetsListener { v, insets ->
            val bars = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        setContentView(scroll)
    }
}
