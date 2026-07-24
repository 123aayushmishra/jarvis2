package com.jarvis.assistant

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var statusText: TextView

    private val fieldIds = listOf(
        "num_chaitanya", "num_gauri", "num_tanni",
        "num_mummy", "num_papa", "num_shankar", "spotify_link"
    )

    private val permissionsNeeded = mutableListOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CALL_PHONE
    ).apply {
        if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
    }.toTypedArray()

    private val permissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Kuch permissions nahi mili — call/mic feature kaam nahi karega", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = getSharedPreferences("jarvis_prefs", MODE_PRIVATE)
        statusText = findViewById(R.id.statusText)

        fieldIds.forEach { id ->
            val editText = findViewById<EditText>(resources.getIdentifier(id, "id", packageName))
            editText.setText(prefs.getString(id, ""))
        }

        findViewById<Button>(R.id.saveBtn).setOnClickListener {
            val editor = prefs.edit()
            fieldIds.forEach { id ->
                val editText = findViewById<EditText>(resources.getIdentifier(id, "id", packageName))
                editor.putString(id, editText.text.toString().trim())
            }
            editor.apply()
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.startBtn).setOnClickListener {
            requestPermissionsIfNeeded()
            val intent = Intent(this, JarvisService::class.java)
            ContextCompat.startForegroundService(this, intent)
            statusText.text = "Jarvis chal raha hai — background me bhi sunega"
        }

        findViewById<Button>(R.id.stopBtn).setOnClickListener {
            stopService(Intent(this, JarvisService::class.java))
            statusText.text = "Jarvis band hai"
        }

        requestPermissionsIfNeeded()
    }

    private fun requestPermissionsIfNeeded() {
        val missing = permissionsNeeded.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 101)
        }
    }
}
