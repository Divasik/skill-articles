package ru.skillbranch.skillarticles.viewmodels.auth

import androidx.lifecycle.SavedStateHandle
import ru.skillbranch.skillarticles.data.repositories.RootRepository
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand
import ru.skillbranch.skillarticles.viewmodels.base.Notify

class AuthViewModel(handle: SavedStateHandle) : BaseViewModel<AuthState>(handle, AuthState()), IAuthViewModel {

    private val repository = RootRepository

    init {
        subscribeOnDataSource(repository.isAuth()) { isAuth, state ->
            state.copy(isAuth = isAuth)
        }
    }

    override fun handleLogin(login: String, pass: String, dest: Int?) {
        launchSafety {
            repository.login(login, pass)
            navigate(NavigationCommand.FinishLogin(dest))
        }
    }

    override fun handleRegister(name: String, login: String, password: String, dest: Int?) {
        if(name.isEmpty() || login.isEmpty() || password.isEmpty()) {
            notify(Notify.ErrorMessage("Name, login, password it is required fields and not must be empty"))
            return
        }

        if(name.length < 3) {
            notify(Notify.ErrorMessage("The name must be at least 3 characters long and contain only letters and numbers and can also contain the characters \"-\" and \"_\""))
            return
        }

        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})".toRegex()
        if(!emailRegex.matches(login)) {
            notify(Notify.ErrorMessage("Incorrect Email entered"))
            return
        }

        val passRegex = "^[a-zA-Z0-9]{8,}$".toRegex()
        if(!passRegex.matches(password)) {
            notify(Notify.ErrorMessage("Password must be at least 8 characters long and contain only letters and numbers"))
            return
        }

        launchSafety {
            repository.register(name, login, password)
            navigate(NavigationCommand.FinishLogin(dest))
        }
    }
}

data class AuthState(
    val isAuth: Boolean = false
): IViewModelState