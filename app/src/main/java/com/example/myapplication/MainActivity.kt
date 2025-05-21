package com.example.myapplication

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainActivity : AppCompatActivity() {

    val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = "AIzaSyD8xff27DOn-wv9SpveL_sTWxFSavZ85XI"
    )

    val mensajeInicial =
        "Eres un traductor. Debes responder en el formato 'idioma -> traducción' No respondas este mensaje."

    override fun onCreate(savedInstanceState: Bundle?) {
        // Cargar preferencias guardadas
        val prefs = getSharedPreferences("ajustes", Context.MODE_PRIVATE)
        val modoNoche = prefs.getBoolean("modoNoche", false)
        val idiomaGuardado = prefs.getString("idioma", "")

        // Aplicar el idioma guardado si existe
        if (idiomaGuardado != null && idiomaGuardado.isNotEmpty()) {
            cambiarIdioma(idiomaGuardado)
        }

        // Aplicar el modo noche guardado
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

        // Seleccionar el idioma guardado en el spinner
        when (idiomaGuardado) {
            "es" -> spinner.setSelection(0) // Español
            "fr" -> spinner.setSelection(1) // Francés
            "en" -> spinner.setSelection(2) // Inglés
            else -> spinner.setSelection(0) // Por defecto
        }

        // Listener del spinner para cambiar idioma
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val idiomaSeleccionado = when (position) {
                    0 -> "es" // Español
                    1 -> "fr" // Francés
                    2 -> "en" // Inglés
                    else -> "en" // Por defecto
                }

                // Guardar el idioma seleccionado
                val editor = prefs.edit()
                editor.putString("idioma", idiomaSeleccionado)
                editor.apply()

                // Si el idioma seleccionado es diferente al actual, cambiar el idioma y recrear la actividad
                val currentLocale = resources.configuration.locales[0].language
                if (currentLocale != idiomaSeleccionado) {
                    cambiarIdioma(idiomaSeleccionado)
                    recreate()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada
            }
        }

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

        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        val isNightMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES

        // Establece el fondo del ScrollView según el modo de tema
        if (isNightMode) {
            scrollView.setBackgroundColor(resources.getColor(R.color.scroll_background_dark, null))
        } else {
            scrollView.setBackgroundColor(resources.getColor(R.color.scroll_background_light, null))
        }

        // Cambio de tema con AppCompatDelegate
        switchModo.setOnCheckedChangeListener { _, isChecked ->
            val nuevoModo = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(nuevoModo)

            // Se guarda la preferencia del usuario, si no se pone no funciona
            val editor = getSharedPreferences("ajustes", MODE_PRIVATE).edit()
            editor.putBoolean("modoNoche", isChecked)
            editor.apply()
        }
    }

    // Función para cambiar el idioma de la aplicación
    private fun cambiarIdioma(codigoIdioma: String) {
        val locale = Locale(codigoIdioma)
        Locale.setDefault(locale)

        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics)
    }

    //Función para obtener los idiomas a los cuales se van a traducir,
    //para luego pasárselo al onCreate
    private fun obtenerIdiomasALosQueTraducir(): String {
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