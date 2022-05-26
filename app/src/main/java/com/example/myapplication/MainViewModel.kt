package com.example.myapplication

import android.util.Log
import android.view.Choreographer
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.withFrameMillis
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class MainViewModel : ViewModel() {

    private val _stateFlow = MutableStateFlow(emptyList<ItemState>())
    val stateFlow = _stateFlow.asStateFlow()

    private val channelItemClicked = Channel<List<ItemState>>(Channel.UNLIMITED)

    private val frameTicker = Channel<Unit>(Channel.CONFLATED)

    private var continuation: Continuation<Unit>? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            for (item in channelItemClicked) {
                _stateFlow.getAndUpdate {
                    it.toMutableList().apply {
                        addAll(item)
                    }
                }
                Log.d("dilraj", "resuming continuation $continuation")
                continuation?.resume(Unit)
                continuation = null
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            for (frameUpdate in frameTicker) {
                Log.d("dilraj", "got update in frame ticker")
                val item = stateFlow.value.toMutableList()
                if (item.isEmpty()) {
                    suspendCancellableCoroutine {
                        continuation = it
                    }
                } else {
                    val iter = item.iterator()
                    val removal = mutableListOf<ItemState>()
                    iter.forEachIndexed { index, itemState ->
                        val counter = System.nanoTime() - itemState.time
                        val yTicker =
                            itemState.yAnimation.getValueFromNanos(counter)
                        val xTicker =
                            itemState.xAnimation.getValueFromNanos(counter)
                        val alphaTicker =
                            itemState.alphaAnimation.getValueFromNanos(counter)
                        val angleTicker =
                            itemState.angleAnimation.getValueFromNanos(counter)
                        item.safeSet(index) {
                            it.copy(
                                x = xTicker,
                                y = yTicker,
                                alpha = (alphaTicker).coerceAtLeast(0.01f),
                                angle = angleTicker
                            )
                        }
                        if (
                            itemState.terminalCondition(
                                xTicker,
                                yTicker,
                                alphaTicker,
                                angleTicker,
                                (counter / 1_000_000).toFloat()
                            )
                        ) {
                            removal.add(itemState)
                        }
                    }
                    item.removeAll {
                        removal.any { removed -> removed.id == it.id }
                    }
                    Log.d("dilraj", "setting value")
                    _stateFlow.update {
                        item.toMutableList()
                    }
                }
            }
        }
    }

    fun onFrameAvailable() {
        frameTicker.trySend(Unit)
    }

    fun sendItemClick(items: List<ItemState>) {
        channelItemClicked.trySend(items)
    }
}
