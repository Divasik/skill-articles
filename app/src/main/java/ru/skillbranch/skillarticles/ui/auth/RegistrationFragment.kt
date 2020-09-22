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
            viewModel.handleRegister(
                    et_name.text.toString(),
                    et_login.text.toString(),
                    et_password.text.toString(),
                    if(args.privateDestination == -1) null else args.privateDestination
            )
        }
    }

}
