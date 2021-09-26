package io.openim.demo.home.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cn.alvince.zanpakuto.databinding.FragmentBinding
import cn.alvince.zanpakuto.databinding.FragmentBindingHolder
import io.openim.demo.R
import io.openim.demo.databinding.MainHomeMessageFragmentBinding

/**
 * Created by alvince on 2021/9/27
 *
 * @author alvince.zy@gmail.com
 */
class HomeMessageFragment : Fragment(), FragmentBindingHolder<MainHomeMessageFragmentBinding> by FragmentBinding(R.layout.main_home_message_fragment) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflate(inflater, container) {

        }
}
