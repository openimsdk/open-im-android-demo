package io.openim.demo.home.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cn.alvince.zanpakuto.databinding.FragmentBinding
import cn.alvince.zanpakuto.databinding.FragmentBindingHolder
import cn.alvince.zanpakuto.lifecycle.appViewModel
import io.openim.demo.R
import io.openim.demo.databinding.MainHomeFragmentBinding
import io.openim.demo.home.viewmodel.MainHostViewModel

/**
 * Home host container [Fragment]
 *
 * Created by alvince on 2021/9/27
 *
 * @author alvince.zy@gmail.com
 */
class HomeHostFragment : Fragment(), FragmentBindingHolder<MainHomeFragmentBinding> by FragmentBinding(R.layout.main_home_fragment) {

    private val viewModel: MainHostViewModel by appViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflate(inflater, container) {
            viewModel.bindView(it)
        }
}
