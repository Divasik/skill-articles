package ru.skillbranch.skillarticles.data.delegates

import ru.skillbranch.skillarticles.data.local.PrefManager
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PrefDelegate<T>(private val defaultValue: T) : ReadWriteProperty<PrefManager, T?> {

    override fun getValue(thisRef: PrefManager, property: KProperty<*>): T? {
        return with (thisRef.preferences) {
            val propName = property.name
            when (defaultValue) {
                is Boolean -> getBoolean(propName, defaultValue) as T
                is String -> getString(propName, defaultValue) as T
                is Float -> getFloat(propName, defaultValue) as T
                is Int -> getInt(propName, defaultValue) as T
                is Long -> getLong(propName, defaultValue) as T
                else -> throw java.lang.ClassCastException("PrefManager getPropertyValue type illegal")
            }
        }
    }

    override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T?) {
        with (thisRef.preferences.edit()) {
            val propName = property.name
            when (value) {
                is Boolean -> putBoolean(propName, value)
                is String -> putString(propName, value)
                is Float -> putFloat(propName, value)
                is Int -> putInt(propName, value)
                is Long -> putLong(propName, value)
                else -> throw java.lang.ClassCastException("PrefManager setPropertyValue type illegal")
            }
            .apply()
        }
    }
}