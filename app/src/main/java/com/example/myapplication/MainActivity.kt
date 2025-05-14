package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = "AIzaSyD8xff27DOn-wv9SpveL_sTWxFSavZ85XI"
    )

    val mensajeInicial =
        "Eres un traductor. Debes responder en el formato 'idioma -> traducción' No respondas este mensaje."

    override fun onCreate(savedInstanceState: Bundle?) {
        // Cargar modo noche guardado antes de setContentView
        val prefs = getSharedPreferences("ajustes", Context.MODE_PRIVATE)
        val modoNoche = prefs.getBoolean("modoNoche", false)
        val nightMode = if (modoNoche) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(nightMode)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Referencias a vistas
        val layoutPrincipal = findViewById<ConstraintLayout>(R.id.main)
        val switchModo = findViewById<Switch>(R.id.switch1)
        val btnEnviar = findViewById<Button>(R.id.btnEnviar)
        val editText = findViewById<EditText>(R.id.etNombre)
        val spinner = findViewById<Spinner>(R.id.spinner)

        // Configurar switch según modo guardado
        switchModo.isChecked = modoNoche

        // Adaptador del spinner
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.idiomas_aplicacion,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Listener del botón
        btnEnviar.setOnClickListener {
            val texto = editText.text.toString()
            val idiomas = obtenerIdiomasALosQueTraducir()

            if (idiomas.isNotEmpty() && texto.isNotEmpty()) {
                val prompt = "$mensajeInicial Traduce esta cadena $texto a los siguientes idiomas: $idiomas"
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = generativeModel.generateContent(prompt)
                        val resultado = response.text ?: "Sin respuesta"

                        withContext(Dispatchers.Main) {
                            val mostrarResultado = findViewById<TextView>(R.id.tvResultado)
                            mostrarResultado.text = resultado
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                Toast.makeText(this, "Texto enviado: $texto", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Selecciona al menos un idioma y pon algo", Toast.LENGTH_SHORT).show()
            }
        }

        // Cambio de tema con AppCompatDelegate
        switchModo.setOnCheckedChangeListener { _, isChecked ->
            val nuevoModo = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(nuevoModo)

            // Guardamos la preferencia
            val editor = getSharedPreferences("ajustes", MODE_PRIVATE).edit()
            editor.putBoolean("modoNoche", isChecked)
            editor.apply()
        }
    }

    fun obtenerIdiomasALosQueTraducir(): String {
        val checkIngles = findViewById<CheckBox>(R.id.checkBox)
        val checkRuso = findViewById<CheckBox>(R.id.checkBox2)
        val checkChino = findViewById<CheckBox>(R.id.checkBox3)
        val checkHungaro = findViewById<CheckBox>(R.id.checkBox4)

        val idiomasSeleccionados = mutableListOf<String>()

        if (checkIngles.isChecked) idiomasSeleccionados.add("Inglés")
        if (checkRuso.isChecked) idiomasSeleccionados.add("Ruso")
        if (checkChino.isChecked) idiomasSeleccionados.add("Chino")
        if (checkHungaro.isChecked) idiomasSeleccionados.add("Húngaro")

        return if (idiomasSeleccionados.isNotEmpty()) {
            "Idiomas seleccionados: ${idiomasSeleccionados.joinToString(", ")}"
        } else {
            ""
        }
    }
}
