package io.openim.android.demo.composeui.core

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

abstract class UIComponent<A: UIComponent.UIAction, S: UIComponent.UIState<A>> {
    protected val ioScope = CoroutineScope(Dispatchers.Default)
    @Composable
    fun Main() {
        val state = Presenter()
        UI(state)
    }

    @Composable
    protected abstract fun UI(state: S)

    @Composable
    protected abstract fun Presenter(): S


    abstract class UIState<A>(
        val action: (A) -> Unit
    )
    abstract class UIAction {

    }
}
