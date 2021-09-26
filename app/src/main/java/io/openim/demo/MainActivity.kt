package io.openim.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.alvince.zanpakuto.databinding.ActivityBinding
import cn.alvince.zanpakuto.databinding.ActivityBindingHolder
import io.openim.demo.account.AccountFuncVisitor
import io.openim.demo.account.AccountManager
import io.openim.demo.databinding.MainActivityBinding
import io.openim.demo.home.fragment.HomeHostFragment

class MainActivity : AppCompatActivity(), ActivityBindingHolder<MainActivityBinding> by ActivityBinding(R.layout.main_activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflate {
            supportFragmentManager.beginTransaction()
                .add(R.id.fcv_container, HomeHostFragment())
                .commit()
        }
    }

    override fun onStart() {
        super.onStart()
        if (!AccountManager.logged) {
            AccountFuncVisitor.login(supportFragmentManager, R.id.fcv_container)
        }
    }
}
