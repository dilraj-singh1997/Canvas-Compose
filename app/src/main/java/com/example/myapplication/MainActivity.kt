package com.example.myapplication

import android.annotation.SuppressLint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextPaint
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Animation
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.DurationBasedAnimationSpec
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FloatAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TargetBasedAnimation
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VectorizedAnimationSpec
import androidx.compose.animation.core.VectorizedDurationBasedAnimationSpec
import androidx.compose.animation.core.VectorizedFloatAnimationSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.compose.type_safe_args.annotation.ArgumentProvider
import com.compose.type_safe_args.annotation.ComposeDestination
import com.compose.type_safe_args.annotation.HasDefaultValue
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.io.Serializable
import java.util.*
import kotlin.math.sin
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MyApplicationTheme {
                ProvideWindowInsets {
                    Box(Modifier
                        .fillMaxSize()
                        .background(Color.DarkGray))
                    {
                        Screen()
                    }
                }
            }
        }
    }
}

@Composable
fun Screen() {

    val hearts = remember {
        mutableStateOf<List<ItemState>>(mutableListOf())
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Heart(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            horizontalPadding = 24,
            bottomMargin = 110,
            items = hearts.value
        )

        val width =
            with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
        val height =
            with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }

        val channel = remember {
            Channel<List<ItemState>>(Channel.UNLIMITED)
        }

        val channelState = remember {
            Channel<MutableList<ItemState>>(Channel.UNLIMITED)
        }

        var job: Job? = remember {
            null
        }

        val x = animateDpAsState(targetValue = 1.dp, animationSpec = tween(1))
        var playTime by remember { mutableStateOf(0L) }

        LaunchedEffect(true) {
            val x = Animatable(initialValue = 0.0f)
            val y = TargetBasedAnimation(
                animationSpec = tween(5500, easing = LinearEasing),
                typeConverter = Float.VectorConverter,
                initialValue = 0f,
                targetValue = height
            )
//            val y = VectorizedSpringSpec<AnimationVector1D>(dampingRatio = Spring.DampingRatioHighBouncy)
//            val y = VectorizedTweenSpec<AnimationVector1D>(durationMillis = 1000, delayMillis = 0, easing = FastOutSlowInEasing)

            var time = 0L
            val startTime = System.nanoTime()

            while (x.value <= 100f) {
                time += 100
                playTime = System.nanoTime() - startTime
                val z = ((x.value) * 5500_000_000).toLong()
//                Log.d("dilraj", "${y.durationNanos} time = $time, time*1000 = ${z}, playTime = $playTime, x = ${x.value}, y = ${y.getValueFromNanos(
//                    playTimeNanos = playTime
//                )}")
                x.animateTo(x.value + 0.1f)
                delay(100)
            }
        }

        LaunchedEffect(true) {
            for (item in channelState) {
                job?.cancel()
                job = launch(Dispatchers.IO) {
                    while (true) {
                        withFrameMillis {
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
                                if (itemState.y <= 0 || itemState.alpha < 0.05f) {
                                    removal.add(itemState)
                                }
                            }
                            item.removeAll {
                                removal.any { removed -> removed.id == it.id }
                            }
                            hearts.value = item.toMutableList()
                            if (hearts.value.isEmpty()) {
                                cancel()
                            }
                        }
                    }
                }
            }
        }

        LaunchedEffect(true) {
            launch(Dispatchers.IO) {
                for (_item in channel) {
                    channelState.trySend(hearts.value.toMutableList().apply {
                        addAll(_item)
                    })
                }
            }
        }

        Button(
            onClick = {
                channel.trySend(List(1) {
                    ItemState(
                        x = Random.nextInt(0, (width).toInt()).toFloat(),
                        y = (height - 200f),//Random.nextInt(0, (height).toInt()).toFloat()
                        xAnimation =
                        SinWaveAnimation<AnimationVector1D>(
                            animationSpec = SinWaveTweenSpec<Float>(durationMillis = 3500),
                            typeConverter = Float.VectorConverter,
                            initialValue = Random.nextInt(0, (width).toInt()).toFloat(),
                        ),
                        yAnimation = TargetBasedAnimation(
                            animationSpec =
                            //spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMediumLow)
                            tween(durationMillis = 3500, easing = FastOutSlowInEasing),
                            typeConverter = Float.VectorConverter,
                            initialValue = height,
                            targetValue = height / 2
                        ),
                        alphaAnimation = TargetBasedAnimation(
                            animationSpec =
                            //spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMediumLow)
                            tween(durationMillis = 5500, easing = LinearEasing),
                            typeConverter = Float.VectorConverter,
                            initialValue = 1f,
                            targetValue = 0f
                        ),
                        angle = 0f,
                        angleAnimation = TargetBasedAnimation(
                            animationSpec =
                            //spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMediumLow)
                            tween(durationMillis = 2500, easing = FastOutSlowInEasing),
                            typeConverter = Float.VectorConverter,
                            initialValue = 0f,
                            targetValue = 1440f
                        ),
                    )
                }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .wrapContentHeight()
                .wrapContentWidth()
        ) {
            Text(
                text = "Like",
                color = Color.White
            )
        }

    }
}

// 0, 0.1, 0.2, ..., 0.9, 1, 0.9, 0.8, 0.7, ..., 0.2, 0.1
data class ItemState(
    val id: String = UUID.randomUUID().toString(),
    val x: Float,
    val y: Float,
    val alpha: Float = 1.0f,
    val xAnimation: Animation<Float, AnimationVector1D>,
    val yAnimation: Animation<Float, AnimationVector1D> = TargetBasedAnimation(
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        typeConverter = Float.VectorConverter,
        initialValue = 0f,
        targetValue = 1f
    ),
    val alphaAnimation: Animation<Float, AnimationVector1D> = TargetBasedAnimation(
        animationSpec = tween(durationMillis = 1500, easing = LinearEasing),
        typeConverter = Float.VectorConverter,
        initialValue = 1f,
        targetValue = 0f
    ),
    val angle: Float,
    val angleAnimation: Animation<Float, AnimationVector1D> = TargetBasedAnimation(
        animationSpec = tween(durationMillis = 1500, easing = LinearEasing),
        typeConverter = Float.VectorConverter,
        initialValue = 1f,
        targetValue = 0f
    ),
    val time: Long = System.nanoTime(),
)

class SinWaveAnimation<V : AnimationVector> private constructor(
    val animationSpec: VectorizedAnimationSpec<V>,
    override val typeConverter: TwoWayConverter<Float, V>,
    val initialValue: Float,
    override val targetValue: Float,
    initialVelocityVector: V? = null,
) : Animation<Float, V> {

    constructor(
        animationSpec: AnimationSpec<Float>,
        typeConverter: TwoWayConverter<Float, V>,
        initialValue: Float,
        initialVelocityVector: V? = null,
    ) : this(
        animationSpec.vectorize(typeConverter),
        typeConverter,
        initialValue,
        initialValue,
        initialVelocityVector
    )

    private val initialValueVector = typeConverter.convertToVector(initialValue)
    private val targetValueVector = typeConverter.convertToVector(targetValue)
    private val initialVelocityVector =
        initialVelocityVector ?: typeConverter.convertToVector(initialValue)

    override val isInfinite: Boolean get() = animationSpec.isInfinite
    override fun getValueFromNanos(playTimeNanos: Long): Float {
        return if (!isFinishedFromNanos(playTimeNanos)) {
            initialValue + typeConverter.convertFromVector(
                animationSpec.getValueFromNanos(
                    playTimeNanos, initialValueVector,
                    targetValueVector, initialVelocityVector
                )
            )
        } else {
            targetValue
        }
    }

    @get:Suppress("MethodNameUnits")
    override val durationNanos: Long = animationSpec.getDurationNanos(
        initialValue = initialValueVector,
        targetValue = targetValueVector,
        initialVelocity = this.initialVelocityVector
    )

    private val endVelocity = animationSpec.getEndVelocity(
        initialValueVector,
        targetValueVector,
        this.initialVelocityVector
    )

    override fun getVelocityVectorFromNanos(playTimeNanos: Long): V {
        return if (!isFinishedFromNanos(playTimeNanos)) {
            animationSpec.getVelocityFromNanos(
                playTimeNanos,
                initialValueVector,
                targetValueVector,
                initialVelocityVector
            )
        } else {
            endVelocity
        }
    }

    override fun toString(): String {
        return "TargetBasedAnimation: $initialValue -> $targetValue," +
                "initial velocity: $initialVelocityVector, duration: $durationMillis ms"
    }
}

val Animation<*, *>.durationMillis: Long
    get() = durationNanos / MillisToNanos

@Immutable
class SinWaveTweenSpec<T>(
    val durationMillis: Int = AnimationConstants.DefaultDurationMillis,
    val delay: Int = 0,
    val easing: Easing = FastOutSlowInEasing,
) : DurationBasedAnimationSpec<T> {

    override fun <V : AnimationVector> vectorize(converter: TwoWayConverter<T, V>) =
        SinWaveSpec<V>(durationMillis, delay, easing)

    override fun equals(other: Any?): Boolean =
        if (other is TweenSpec<*>) {
            other.durationMillis == this.durationMillis &&
                    other.delay == this.delay &&
                    other.easing == this.easing
        } else {
            false
        }

    override fun hashCode(): Int {
        return (durationMillis * 31 + easing.hashCode()) * 31 + delay
    }
}

class SinWaveSpec<V : AnimationVector>(
    override val durationMillis: Int = AnimationConstants.DefaultDurationMillis,
    override val delayMillis: Int = 0,
    val easing: Easing = FastOutSlowInEasing,
) : VectorizedDurationBasedAnimationSpec<V> {

    private val anim = VectorizedFloatAnimationSpec<V>(
        SinWaveInterpolater(durationMillis, delayMillis, easing)
    )

    override fun getValueFromNanos(
        playTimeNanos: Long,
        initialValue: V,
        targetValue: V,
        initialVelocity: V,
    ): V {
        return anim.getValueFromNanos(playTimeNanos, initialValue, targetValue, initialVelocity)
    }

    override fun getVelocityFromNanos(
        playTimeNanos: Long,
        initialValue: V,
        targetValue: V,
        initialVelocity: V,
    ): V {
        return anim.getVelocityFromNanos(playTimeNanos, initialValue, targetValue, initialVelocity)
    }
}

val MillisToNanos: Long = 1_000_000L

class SinWaveInterpolater(
    val duration: Int = AnimationConstants.DefaultDurationMillis,
    val delay: Int = 0,
    private val easing: Easing = FastOutSlowInEasing,
) : FloatAnimationSpec {

    override fun getValueFromNanos(
        playTimeNanos: Long,
        initialValue: Float,
        targetValue: Float,
        initialVelocity: Float,
    ): Float {
        val playTimeMillis = playTimeNanos / 1_000_000L
        val clampedPlayTime = clampPlayTime(playTimeMillis)
        val rawFraction = if (duration == 0) 1f else clampedPlayTime / duration.toFloat()
        val fraction = easing.transform(rawFraction.coerceIn(0f, 1f))
        Log.d("dilraj", "cos value is ${sin(fraction * 2 * Math.PI).toFloat()}")
        return 100 * (sin(fraction * 2 * Math.PI).toFloat())
    }

    private fun clampPlayTime(playTime: Long): Long {
        return (playTime - delay).coerceIn(0, duration.toLong())
    }

    override fun getDurationNanos(
        initialValue: Float,
        targetValue: Float,
        initialVelocity: Float,
    ): Long {
        return (delay + duration) * 1_000_000L
    }

    override fun getVelocityFromNanos(
        playTimeNanos: Long,
        initialValue: Float,
        targetValue: Float,
        initialVelocity: Float,
    ): Float {
        return 0f
    }
}


@Composable
fun Heart(modifier: Modifier, horizontalPadding: Int, bottomMargin: Int, items: List<ItemState>) {
    Log.d("dilraj", "recomposing canvas")

    //TODO- drawBehind {  } check once
    Canvas(modifier = modifier,
        onDraw = {
            for (item in items) {
                val path = Path().apply {
                    heartPath(Size(120f, 120f))
                }
                translate(top = item.y, left = item.x) {
                    drawContext.canvas.nativeCanvas.apply {
                        val textPaint = TextPaint().apply {
                            color = android.graphics.Color.WHITE
                            isAntiAlias = true
                            textSize = 48f
                            alpha = (item.alpha * 255).toInt()
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        }

                        val emojiPaint = TextPaint().apply {
                            color = android.graphics.Color.GREEN
                            isAntiAlias = true
                            textSize = 124f
                            alpha = (item.alpha * 255).toInt()
                        }

                        val text = "@ayushnasa"
                        val emoji =
                            //"\uD83D\uDD34"
                            "\uD83D\uDD25"
                        drawText(text, 0f, 0f, textPaint)

                        val textBounds = Rect()
                        textPaint.getTextBounds(text, 0, text.length, textBounds)

                        val emojiBounds = Rect()
                        emojiPaint.getTextBounds(emoji, 0, emoji.length, emojiBounds)

                        drawText(emoji,
                            (textBounds.width() - emojiBounds.width()) / 2f,
                            (textPaint.textSize) * 1f,
                            emojiPaint)
                    }
                    rotate(item.angle, pivot = Offset(
                        x = (120f / 2),
                        y = (120f / 2)
                    )) {
                        drawPath(
                            path = path,
                            color = Color.Red.copy(alpha = item.alpha),
                        )
                    }
                }
            }
        }
    )
}

//@Composable
//fun Heart() {
//    Canvas(modifier = Modifier
//        .fillMaxSize(),
//        onDraw = {
//            val path = Path().apply {
//                heartPath(Size(120f, 120f))
//            }
//
//            drawPath(
//                path = path,
//                color = Color.Red,
//            )
//        }
//    )
//}

fun Path.heartPath(size: Size): Path {
    //the logic is taken from StackOverFlow [answer](https://stackoverflow.com/a/41251829/5348665)and converted into extension function

    val width: Float = size.width
    val height: Float = size.height

    // Starting point
    moveTo(width / 2, height / 5)

    // Upper left path
    cubicTo(
        5 * width / 14, 0f,
        0f, height / 15,
        width / 28, 2 * height / 5
    )

    // Lower left path
    cubicTo(
        width / 14, 2 * height / 3,
        3 * width / 7, 5 * height / 6,
        width / 2, height
    )

    // Lower right path
    cubicTo(
        4 * width / 7, 5 * height / 6,
        13 * width / 14, 2 * height / 3,
        27 * width / 28, 2 * height / 5
    )

    // Upper right path
    cubicTo(
        width, height / 15,
        9 * width / 14, 0f,
        width / 2, height / 5
    )
    return this
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SubTopics() {
    Scaffold(
        backgroundColor = Color.Black,
        topBar = {
            Text(modifier = Modifier.height(30.dp), text = "app bar", color = Color.White)
        }
    ) {
        val array = remember {
            arrayListOf(
                "Funny",
                "Joke",
                "Sarcasm",
                "Yolo",
                "sdasdadssd",
                "dasdasddss",
                "sdasdsdfergfs",
                "dasddsdsdsasd",
                "dasdasdasdad",
                "adasdasdada",
                "adasdasdasdas",
                "adasdasdsdsdsas",
                "sadasdasdasda",
                "dasdfgfhcvcasd",
                "dsbffgd",
                "adasdasdsdssdasda",
                "adasdacbvfbvsdadsads",
                "asdasdascvcvcbvbdasdas"
            )
        }
        Box {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(array, key = { index, item ->
                    item
                }) { index, string ->


                    Column {

                        Box(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = string,
                                modifier = Modifier.padding(
                                    top = 19.dp,
                                    start = 16.dp,
                                    bottom = 19.dp
                                ),
                                color = Color.White
                            )
                            Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                                Text(
                                    text = "${index}00k Views",
                                    color = Color.LightGray,
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                )
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = "",
                                    tint = Color.LightGray,
                                    modifier = Modifier.rotate(180f)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                        }
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(1.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items((1..10).toList()) {
                                Box(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .aspectRatio(3 / 4f)
                                        .background(Color.Green)
                                ) {
                                    val startColor = Color.Transparent
                                    val endColor = Color(0f, 0f, 0f, 0.5f)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .background(
                                                Brush.linearGradient(
                                                    0f to startColor,
                                                    1f to endColor,
                                                    start = Offset.Zero,
                                                    end = Offset(0f, Float.POSITIVE_INFINITY)
                                                )
                                            )
                                            .align(Alignment.BottomCenter)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

//enum class T : Parcelable {
//    A,
//    B;
//
//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeInt(this.ordinal)
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    companion object CREATOR : Parcelable.Creator<T> {
//        override fun createFromParcel(parcel: Parcel): T {
//            return when (parcel.readInt()) {
//                0 -> A
//                else -> B
//            }
//        }
//
//        override fun newArray(size: Int): Array<T?> {
//            return arrayOfNulls(size)
//        }
//    }
//}
//
@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}


@ComposeDestination
interface UserPage {
    @HasDefaultValue
    val userId: String?
    val uniqueUser: User

    @ArgumentProvider
    companion object : IUserPageProvider {
        override val userId: String?
            get() = null

    }
}

data class User(val name: String, val age: Int) : Serializable

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun getKeyboardVisibility(): Boolean {

    val windowInsets = LocalWindowInsets.current

    var keyboardVisibleState by remember {
        mutableStateOf(false)
    }

    val keyboard by remember {
        mutableStateOf(windowInsets.ime)
    }

    LaunchedEffect(Unit) {
        snapshotFlow { keyboard.isVisible }.distinctUntilChanged().collect {
            keyboardVisibleState = it
        }
    }
    return keyboardVisibleState
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun KeyboardVisibilityListener(keyboardVisibilityChanged: (isKeyboardVisible: Boolean) -> Unit) {

    val windowInsets = LocalWindowInsets.current

    val keyboard by remember {
        mutableStateOf(windowInsets.ime)
    }

    val keyboardVisibilityChangedUpdated by rememberUpdatedState(newValue = keyboardVisibilityChanged)

    LaunchedEffect(Unit) {
        snapshotFlow { keyboard.isVisible }.distinctUntilChanged().collect {
            keyboardVisibilityChangedUpdated(it)
        }
    }
}

fun <T> List<T>.safeGet(index: Int?): T? {
    return if (index != null && index >= 0 && index <= lastIndex) {
        this[index]
    } else {
        null
    }
}

fun <T> MutableList<T>.safeSet(index: Int?, update: (value: T) -> T): Boolean {
    return if (index != null && index >= 0 && index <= lastIndex) {
        this[index] = update(this[index])
        true
    } else {
        false
    }
}

fun <T> Iterator<T>.forEachIndexed(block: (Int, T) -> Unit) {
    var index = 0
    while (hasNext()) {
        block(index, next())
        index++
    }
}
