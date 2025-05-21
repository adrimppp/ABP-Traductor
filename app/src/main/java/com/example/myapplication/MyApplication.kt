package com.example.myapplication

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Cargar el idioma guardado al iniciar la aplicación
        val prefs = getSharedPreferences("ajustes", Context.MODE_PRIVATE)
        val idiomaGuardado = prefs.getString("idioma", "")

        if (idiomaGuardado != null && idiomaGuardado.isNotEmpty()) {
            cambiarIdioma(idiomaGuardado)
        }
    }

    fun cambiarIdioma(codigoIdioma: String) {
        val locale = Locale(codigoIdioma)
        Locale.setDefault(locale)

        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics)
    }

    // Manejar posibles cambios en la configuración
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Cargar el idioma guardado cuando cambia la configuración
        val prefs = getSharedPreferences("ajustes", Context.MODE_PRIVATE)
        val idiomaGuardado = prefs.getString("idioma", "")

        if (idiomaGuardado != null && idiomaGuardado.isNotEmpty()) {
            cambiarIdioma(idiomaGuardado)
        }
    }
}