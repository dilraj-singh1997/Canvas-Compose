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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class MainViewModel : ViewModel() {

    private val _stateFlow: MutableStateFlow<List<ItemStateHolder>> = MutableStateFlow(emptyList())
    val stateFlow: Flow<List<ItemState>>
        get() = _stateFlow.asStateFlow().map {
            it.flatMap { itemStateHolder ->
                itemStateHolder.items
            }
        }

    private val channelItemClicked = Channel<ItemStateHolder>(Channel.UNLIMITED)

    private val frameTicker = Channel<Unit>(Channel.CONFLATED)

    private var continuation: Continuation<Unit>? = null

    private val downloadChannel = Channel<ImageKey>(Channel.UNLIMITED)

    private val leftItems: MutableList<ItemStateHolder> = mutableListOf()

    private val fullScaleBitmaps: MutableMap<ImageKey, Bitmap> = mutableMapOf()
    private val bitmaps: MutableMap<Pair<ImageKey, Size>, Bitmap> = mutableMapOf()

    private val jobs: MutableMap<ImageKey, Job?> = mutableMapOf()

    private val bitmapDownloadChannel = Channel<Pair<ImageKey, Bitmap>>(Channel.UNLIMITED)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            for (imageKey in downloadChannel) {
                if (fullScaleBitmaps.containsKey(imageKey)) {
                    bitmapDownloadChannel.trySend(Pair(imageKey, fullScaleBitmaps[imageKey]!!))
                    continue
                }
                if (jobs[imageKey] != null)
                    continue
                jobs[imageKey] = viewModelScope.launch {
                    MyApp.instance?.let {
                        Glide
                            .with(it.applicationContext)
                            .asBitmap()
                            .let {
                                when (imageKey) {
                                    is ImageKey.Drawable -> it.load(imageKey.id)
                                    is ImageKey.URL -> it.load(imageKey.url)
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
                                        bitmapDownloadChannel.trySend(Pair(imageKey, it))
                                    }
                                    return true
                                }

                            })
                            .submit()
                    }
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            for ((id, bitmap) in bitmapDownloadChannel) {
                fullScaleBitmaps[id] = bitmap
                leftItems
                    .filter { item ->
                        item.items.any { it.itemToDraw is CanvasImage && it.itemToDraw.imageKey == id }
                    }
                    .also {
                        leftItems.removeAll { holder ->
                            it.any { it.id == holder.id }
                        }
                        Log.d("dilraj", "sending items ${it.joinToString { it.toString() }}")

                        it.forEach {
                            it.items.forEach {
                                if (it.itemToDraw is CanvasImage) {
                                    if (!bitmaps.containsKey(Pair(it.itemToDraw.imageKey, it.itemToDraw.size))) {

                                        Log.d("dilraj", "creating scaled bitmap ${it.itemToDraw.size.width.toInt()} -- ${bitmap.width}")
                                        bitmaps[Pair(it.itemToDraw.imageKey, it.itemToDraw.size)] = Bitmap.createScaledBitmap(
                                            bitmap,
                                            it.itemToDraw.size.width.toInt(),
                                            it.itemToDraw.size.height.toInt(),
                                            true
                                        ).also {
                                            Log.d("dilraj", "creating scaled new bitmap ${bitmap.width} -- ${it.width}")
                                        }
                                    }
                                }
                            }
                        }

                        sendItemClick(
                            it.flatMap {
                                val list = it.items
                                val x = list.toMutableList().map {
                                    it.copy(
                                        itemToDraw = (it.itemToDraw as CanvasImage).copy(bitmap = bitmaps[Pair(it.itemToDraw.imageKey, it.itemToDraw.size)]),
                                        time = System.nanoTime()
                                    )
                                }
                                Log.d("dilraj", "sending new list ${x.joinToString { it.toString() }}")
                                x
                            }
                        )
                    }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            for (itemStateHolder in channelItemClicked) {
                Log.d("debugdilrajissue", "got new item for channel")
                _stateFlow.updateAndGet {
                    Log.d("debugdilrajissue", "got new item for channel adding items")
                    it.toMutableList().apply {
                        val additionalItems = itemStateHolder.items.toMutableList()
                        additionalItems.iterator().forEachIndexed { i, itemState ->
                            additionalItems.safeSet(i) {
                                it.copy(time = System.nanoTime())
                            }
                        }
                        add(itemStateHolder.copy(items = additionalItems))
                    }
                }.also {
                    Log.d(
                        "debugdilrajissue",
                        "list after adding items is ${it.size} ${it.joinToString { it.items.joinToString { it.toString() } }}"
                    )
                    Log.d(
                        "debugdilrajissue",
                        "list after adding items from value getter is ${_stateFlow.value.size} ${_stateFlow.value.joinToString { it.items.joinToString { it.toString() } }}"
                    )
                }
                Log.d("debugdilrajissue", "resuming continuation $continuation")
                runCatching {
                    continuation?.resume(Unit)
                }
                continuation = null
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            for (frameUpdate in frameTicker) {
                val itemStateHolderList = _stateFlow.value.toMutableList()
                Log.d("debugdilrajissue", "got update in frame ticker ${itemStateHolderList.size}")
                if (itemStateHolderList.isEmpty()) {
                    suspendCancellableCoroutine {
                        continuation = it
                    }
                } else {
                    val iterHolder = itemStateHolderList.iterator()
                    val removal = mutableListOf<ItemState>()
                    val removalHolder = mutableListOf<ItemStateHolder>()
                    iterHolder.forEachIndexed { index, itemStateHolder ->

                        val iter = itemStateHolder.items.iterator()

                        iter.forEachIndexed { index1, itemState ->
                            if (itemState.itemToDraw is CanvasImage && !bitmaps.containsKey(Pair(itemState.itemToDraw.imageKey, itemState.itemToDraw.size))) {
                                Log.d("debugdilrajissue", "inside the block to remove to left items")
                                leftItems.add(itemStateHolder)
                                removalHolder.add(itemStateHolder)
                                loadImage(itemState.itemToDraw)
                                return@forEachIndexed
                            } else {
                                if (itemState.itemToDraw is CanvasImage && itemState.itemToDraw.bitmap == null) {
                                    Log.d("debugdilrajissue", "setting bitmaps")

                                    itemStateHolderList.safeSet(index) {
                                        val list = it.items.toMutableList()
                                        list.safeSet(index1) {
                                            it.copy(
                                                itemToDraw = (it.itemToDraw as CanvasImage).copy(bitmap = bitmaps[Pair(itemState.itemToDraw.imageKey, itemState.itemToDraw.size)])
                                            )
                                        }
                                        it.copy(items = list)
                                    }
                                    Log.d("debugdilrajissue", "setting bitmaps ${itemStateHolderList.joinToString { it.items.joinToString { it.itemToDraw.toString() } }}")
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

                                itemStateHolderList.safeSet(index) {
                                    val list = it.items.toMutableList()
                                    list.safeSet(index1) {
                                        it.copy(
                                            x = xTicker,
                                            y = yTicker,
                                            alpha = (alphaTicker).coerceAtLeast(0.01f),
                                            angle = angleTicker,
                                            color = colorTicker,
                                            scale = scaleTicker
                                        )
                                    }
                                    it.copy(items = list)
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
                    }

                    itemStateHolderList.iterator().forEachIndexed { i, itemStateHolder ->
                        itemStateHolderList.safeSet(i) {
                            val list = it.items.toMutableList()
                            list.removeAll {
                                removal.any { removed -> removed.id == it.id }
                            }
                            it.copy(items = list)
                        }
                    }

                    _stateFlow.update {
                        Log.d("debugdilrajissue", "setting value")
                        val _new = it.toMutableList()
                        _new.iterator().forEachIndexed { i, itemStateHolder ->
                            _new.safeSet(i) {
                                itemStateHolderList.firstOrNull {
                                    it.id == itemStateHolder.id
                                } ?: it
                            }
                        }
                        _new.removeAll {
                            removalHolder.any { it.id == it.id }
                        }
                        _new.removeAll {
                            it.items.isEmpty()
                        }
                        _new.toMutableList()
                    }
                }
            }
        }
    }

    private fun loadImage(canvasImage: CanvasImage) {
        downloadChannel.trySend(canvasImage.imageKey)
    }

    fun onFrameAvailable() {
        frameTicker.trySend(Unit)
    }

    fun sendItemClick(items: List<ItemState>) {
        channelItemClicked.trySend(ItemStateHolder(items = items.toMutableList()))
    }
}
