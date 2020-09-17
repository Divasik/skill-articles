package ru.skillbranch.skillarticles.ui.profile

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import ru.skillbranch.skillarticles.R
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_profile.*
import ru.skillbranch.skillarticles.ui.base.BaseFragment
import ru.skillbranch.skillarticles.ui.base.Binding
import ru.skillbranch.skillarticles.ui.delegates.RenderProp
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.profile.ProfileState
import ru.skillbranch.skillarticles.viewmodels.profile.ProfileViewModel

class ProfileFragment : BaseFragment<ProfileViewModel>() {

    override val viewModel: ProfileViewModel by viewModels()
    override val layout: Int = R.layout.fragment_profile
    override val binding: Binding by lazy { ProfileBinding() }

    override fun setupViews() {
        iv_avatar.apply {
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setOval(0, 0, view.measuredWidth, view.measuredHeight)
                }
            }
            clipToOutline = true
        }
    }

    private fun updateAvatar(avatarUrl: String) {
        Glide.with(this)
                .load(avatarUrl)
                .into(iv_avatar)
    }

    inner class ProfileBinding : Binding() {

        var avatar by RenderProp("") {
            updateAvatar(it)
        }

        var name by RenderProp("") {
            tv_name.text = it
        }

        var about by RenderProp("") {
            tv_about.text = it
        }

        var rating by RenderProp(0) {
            tv_rating.text = "Rating: $it"
        }

        var respect by RenderProp(0) {
            tv_respect.text = "Respect: $it"
        }

        override fun bind(data: IViewModelState) {
            data as ProfileState

            if(data.avatar != null) avatar = data.avatar
            if(data.name != null) name = data.name
            if(data.about != null) about = data.about
            rating = data.rating
            respect = data.respect
        }

    }
}
