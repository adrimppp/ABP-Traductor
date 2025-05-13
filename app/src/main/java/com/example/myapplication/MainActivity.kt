package com.example.myapplication

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.myapplication.ui.theme.MyApplicationTheme

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
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
    val mensajeInicial = "Eres un traductor. Debes responder en el formato 'idioma -> traducción' No respondas este mensaje."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Referencias a vistas
        val layoutPrincipal = findViewById<ConstraintLayout>(R.id.main)
        val switchModo = findViewById<Switch>(R.id.switch1)
        val btnEnviar = findViewById<Button>(R.id.btnEnviar)
        val editText = findViewById<EditText>(R.id.etNombre)

        // Ejemplo: Acceso a los CheckBox e idioma seleccionado del Spinner
        val checkIngles = findViewById<CheckBox>(R.id.checkBox)
        val checkItaliano = findViewById<CheckBox>(R.id.checkBox2)
        val checkChino = findViewById<CheckBox>(R.id.checkBox3)
        val checkHungaro = findViewById<CheckBox>(R.id.checkBox4)

        // Opcional: Configurar contenido del Spinner (idiomas ejemplo)
        val spinner = findViewById<Spinner>(R.id.spinner)

        // Crear el adaptador desde el recurso XML
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.idiomas_aplicacion,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter


        // Listener del botón
        btnEnviar.setOnClickListener {
            val editText=findViewById<EditText>(R.id.etNombre)
            val idiomasALosQueTraducir=obtenerIdiomasALosQueTraducir()
            if(idiomasALosQueTraducir!="" && editText.toString()!="") {
                val texto = editText.text.toString()
                var prompt:String= "$mensajeInicial Traduce esta cadena $texto a los siguientes idiomas: $idiomasALosQueTraducir" ;
                println(prompt)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = generativeModel.generateContent(prompt)
                        val resultado = response.text ?: "Sin respuesta"

                        withContext(Dispatchers.Main) {
                            val mostrarResultado= findViewById<TextView>(R.id.tvResultado)
                            mostrarResultado.text=resultado
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                Toast.makeText(this, "Texto enviado: $texto", Toast.LENGTH_SHORT).show()
            } else{
                Toast.makeText(this, "Selecciona al menos un idioma y pon algo", Toast.LENGTH_SHORT).show()
            }
        }

        switchModo.setOnCheckedChangeListener { _, isChecked ->
            val colorRes = if (isChecked) R.color.fondoNoche else R.color.fondoDia
            layoutPrincipal.setBackgroundColor(ContextCompat.getColor(this, colorRes))
        }
    }

    fun obtenerIdiomasALosQueTraducir(): String {
        val checkIngles = findViewById<CheckBox>(R.id.checkBox)
        val checkItaliano = findViewById<CheckBox>(R.id.checkBox2)
        val checkChino = findViewById<CheckBox>(R.id.checkBox3)
        val checkHungaro = findViewById<CheckBox>(R.id.checkBox4)

        val idiomasSeleccionados = mutableListOf<String>()

        if (checkIngles.isChecked) idiomasSeleccionados.add("Inglés")
        if (checkItaliano.isChecked) idiomasSeleccionados.add("Italiano")
        if (checkChino.isChecked) idiomasSeleccionados.add("Chino")
        if (checkHungaro.isChecked) idiomasSeleccionados.add("Húngaro")

        return if (idiomasSeleccionados.isNotEmpty()) {
            "Idiomas seleccionados: ${idiomasSeleccionados.joinToString(", ")}"
        } else {
            ""
        }
    }



}