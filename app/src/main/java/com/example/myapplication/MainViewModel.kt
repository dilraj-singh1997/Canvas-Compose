package com.example.myapplication

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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

    private val leftItems: MutableList<ItemState> = mutableListOf()

    private val fullScaleBitmaps: MutableMap<ImageKey, Bitmap> = mutableMapOf()
    private val bitmaps: MutableMap<Pair<ImageKey, Size>, Bitmap> = mutableMapOf()

    private val jobs: MutableMap<ImageKey, Job?> = mutableMapOf()

    private val bitmapDownloadChannel = Channel<Pair<ImageKey, Bitmap>>(Channel.UNLIMITED)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            for ((id, bitmap) in bitmapDownloadChannel) {
                fullScaleBitmaps[id] = bitmap

                leftItems
                    .filter { item ->
                        item.itemToDraw is CanvasImage && item.itemToDraw.imageKey == id
                    }
                    .also {
                        leftItems.removeAll(it)
                        Log.d("dilraj", "sending items ${it.joinToString { it.toString() }}")
                        sendItemClick(
                            it
                                .map {
                                    it.copy(
                                        itemToDraw = (it.itemToDraw as CanvasImage).copy(bitmap = bitmap),
                                        time = System.nanoTime()
                                    )
                                }
                        )
                    }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            for (item in channelItemClicked) {
                Log.d("debugdilraj", "got new item for channel")
                _stateFlow.getAndUpdate {
                    Log.d("debugdilraj", "got new item for channel adding items")
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
                val item = stateFlow.value.toMutableList()
                Log.d("dilraj", "got update in frame ticker ${item.size}")
                if (item.isEmpty()) {
                    suspendCancellableCoroutine {
                        continuation = it
                    }
                } else {
                    val iter = item.iterator()
                    val removal = mutableListOf<ItemState>()
                    iter.forEachIndexed { index, itemState ->
                        if (itemState.itemToDraw is CanvasImage && !fullScaleBitmaps.containsKey(itemState.itemToDraw.imageKey)) {
                            Log.d("dilraj", "inside the block to remove to left items")
                            leftItems.add(itemState)
                            removal.add(itemState)
                            loadImage(itemState.itemToDraw)
                        } else {
                            if (itemState.itemToDraw is CanvasImage && itemState.itemToDraw.bitmap == null) {
                                Log.d("dilraj", "setting bitmaps")
                                item.safeSet(index) {
                                    it.copy(
                                        itemToDraw = (it.itemToDraw as CanvasImage).copy(bitmap = fullScaleBitmaps[itemState.itemToDraw.imageKey])
                                    )
                                }
                            }
                            val counter = System.nanoTime() - itemState.time
                            val yTicker =
                                itemState.yAnimation.getValueFromNanos(counter)
                            val xTicker =
                                itemState.xAnimation.getValueFromNanos(counter)
                            val alphaTicker =
                                itemState.alphaAnimation.getValueFromNanos(counter)
                            val angleTicker =
                                itemState.angleAnimation.getValueFromNanos(counter)
                            val colorTicker =
                                itemState.colorAnimation.getValueFromNanos(counter)
                            val scaleTicker =
                                itemState.scaleAnimation.getValueFromNanos(counter)
                            item.safeSet(index) {
                                it.copy(
                                    x = xTicker,
                                    y = yTicker,
                                    alpha = (alphaTicker).coerceAtLeast(0.01f),
                                    angle = angleTicker,
                                    color = colorTicker,
                                    scale = scaleTicker
                                )
                            }
                            if (
                                itemState.terminalCondition(
                                    xTicker,
                                    yTicker,
                                    alphaTicker,
                                    angleTicker,
                                    colorTicker,
                                    scaleTicker,
                                    (counter / 1_000_000).toFloat()
                                )
                            ) {
                                removal.add(itemState)
                            }
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

    private fun loadImage(canvasImage: CanvasImage) {
        if (jobs[canvasImage.imageKey] != null)
            return
        jobs[canvasImage.imageKey] = viewModelScope.launch {
            MyApp.instance?.let {
                Glide
                    .with(it.applicationContext)
                    .asBitmap()
                    .let {
                        when (canvasImage.imageKey) {
                            is ImageKey.Drawable -> it.load(canvasImage.imageKey.id)
                            is ImageKey.URL -> it.load(canvasImage.imageKey.url)
                        }
                    }
                    .listener(object : RequestListener<Bitmap> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Bitmap>?,
                            isFirstResource: Boolean,
                        ): Boolean {
                            return true
                        }

                        override fun onResourceReady(
                            resource: Bitmap?,
                            model: Any?,
                            target: Target<Bitmap>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean,
                        ): Boolean {
                            Log.d("dilraj", "bitmap ready")
                            resource?.let {
                                bitmapDownloadChannel.trySend(Pair(canvasImage.imageKey, it))
                            }
                            return true
                        }

                    })
                    .submit()
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
