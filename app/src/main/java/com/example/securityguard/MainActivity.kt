package com.example.securityguard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etPackage = findViewById<EditText>(R.id.etTargetPackage)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnPermission = findViewById<Button>(R.id.btnPermission)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        etPackage.setText(prefs.getString("target_package", "qoriqchi"))

        btnSave.setOnClickListener {
            val pkg = etPackage.text.toString().trim()
            prefs.edit().putString("target_package", pkg).apply()
            Toast.makeText(this, "Nishon ilova saqlandi: $pkg", Toast.LENGTH_SHORT).show()
        }

        btnPermission.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }
}
