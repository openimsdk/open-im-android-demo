package io.openim.demo.account.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cn.alvince.zanpakuto.databinding.FragmentBinding
import cn.alvince.zanpakuto.databinding.FragmentBindingHolder
import cn.alvince.zanpakuto.lifecycle.appViewModel
import io.openim.demo.R
import io.openim.demo.account.viewmodel.LoginViewModel
import io.openim.demo.databinding.AccountLoginFragmentBinding

/**
 * Created by alvince on 2021/9/27
 *
 * @author alvince.zy@gmail.com
 */
class LoginFragment : Fragment(), FragmentBindingHolder<AccountLoginFragmentBinding> by FragmentBinding(R.layout.account_login_fragment) {

    private val viewModel: LoginViewModel by appViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflate(inflater, container) {

        }
}
