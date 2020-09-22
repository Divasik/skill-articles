package ru.skillbranch.skillarticles.ui.auth

import androidx.annotation.VisibleForTesting
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.android.synthetic.main.fragment_registration.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.ui.RootActivity
import ru.skillbranch.skillarticles.ui.base.BaseFragment
import ru.skillbranch.skillarticles.viewmodels.auth.AuthViewModel

class RegistrationFragment() : BaseFragment<AuthViewModel>() {

    var _mockFactory: ((SavedStateRegistryOwner) -> ViewModelProvider.Factory)? = null

    override val viewModel: AuthViewModel by viewModels {
        _mockFactory?.invoke(this) ?: defaultViewModelProviderFactory
    }
    
    override val layout: Int = R.layout.fragment_registration
    private val args: RegistrationFragmentArgs by navArgs()

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    constructor(
            mockRoot: RootActivity,
            mockFactory: ((SavedStateRegistryOwner)-> ViewModelProvider.Factory)? = null
    ) : this() {
        _mockRoot = mockRoot
        _mockFactory = mockFactory
    }

    override fun setupViews() {
        btn_register.setOnClickListener {
            register()
        }
    }

    private fun register() {
        val name = et_name.text.toString()
        if(name.length < 3) {
            et_name.setError("Name should contain 3 or more characters")
            return
        }

        val login = et_login.text.toString()
        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})".toRegex()
        if(!emailRegex.matches(login)) {
            et_login.setError("Email invalid")
            return
        }

        val pass = et_password.text.toString()
        val passRegex = "^[a-zA-Z0-9]{8,}$".toRegex()
        if(!passRegex.matches(pass)) {
            et_password.setError("Password should contain 8 or more characters, digits and alphabets only")
            return
        }

        viewModel.handleRegister(
                name,
                login,
                pass,
                if(args.privateDestination == -1) null else args.privateDestination
        )
    }
}
