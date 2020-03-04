package ru.skillbranch.skillarticles.viewmodels.base

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ViewModelDelegate<T : ViewModel>(private val clazz: Class<T>, private val arg: Any?)
    : ReadOnlyProperty<FragmentActivity, T> {

    private lateinit var value: T
    override fun getValue(thisRef: FragmentActivity, property: KProperty<*>): T {
        if(!::value.isInitialized) value = when (arg) {
            null -> ViewModelProviders.of(thisRef).get(clazz)
            else ->  ViewModelProviders.of(thisRef, BaseViewModelFactory(arg)).get(clazz)
        }
        return value
    }
}

class BaseViewModelFactory(private val params: Any?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val constr = modelClass.getConstructor(params?.javaClass)
        return constr.newInstance(params)
    }
}