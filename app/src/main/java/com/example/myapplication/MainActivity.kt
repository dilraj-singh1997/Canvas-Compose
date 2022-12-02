package com.example.myapplication

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.animation.VectorConverter
import androidx.compose.animation.core.Animation
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.DurationBasedAnimationSpec
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FloatAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.TargetBasedAnimation
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VectorizedDurationBasedAnimationSpec
import androidx.compose.animation.core.VectorizedFloatAnimationSpec
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.spring
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compose.type_safe_args.annotation.ArgumentProvider
import com.compose.type_safe_args.annotation.ComposeDestination
import com.compose.type_safe_args.annotation.HasDefaultValue
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import kotlinx.coroutines.flow.distinctUntilChanged
import java.io.Serializable
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

const val G = -9.8f

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
fun Screen(viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {

    val items by viewModel.stateFlow.collectAsState(emptyList())

    Box(modifier = Modifier.fillMaxSize()) {
        Heart(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            _items = { items }
        )

        LaunchedEffect(true) {
            while (true) {
                withFrameNanos {
                    viewModel.onFrameAvailable()
                }
            }
        }

        val width =
            with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
        val height =
            with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }

        Button(
            onClick = {
                val direction = if (Random.nextFloat() < 0.5) -1 else 1
                Log.d("debugdilraj", "button clicked")
                viewModel.sendItemClick(
                    List(6) {
//                        getItemCircleLoader(width, height, (it * 120))
                        getHeartAnimation(width, height, (it * 25), direction)
//                        getParabolaAnimation(width, height)
//                        getHeartPNGAnimation(width, height, (it * 100))
                    }.reversed()
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

fun getParabolaAnimation(width: Float, height: Float, initialDelay: Int = 0): ItemState {
    val xVelocity = Random.nextInt(-50, 50)
    val yVelocity = 170//Random.nextInt(0, 200)
    val rnd = Random.nextInt(0, 5)

    return ItemStateBuilder(
        composeCanvasDrawItem = getTextCanvasObject(when (rnd % 4) {
            0 -> "\uD83E\uDD23"
            1 -> "\uD83D\uDCA9"
            2 -> "\uD83E\uDD2F"
            3 -> "\uD83D\uDE34"
            else -> "\uD83E\uDD24"
        }),
        initialX = width / 2,
        initialY = (height - 500f),
    )
        .animateX {
            object : Animation<Float, AnimationVector1D> {
                override val durationNanos: Long
                    get() = Long.MAX_VALUE
                override val isInfinite: Boolean
                    get() = true
                override val targetValue: Float
                    get() = 0f
                override val typeConverter: TwoWayConverter<Float, AnimationVector1D>
                    get() = Float.VectorConverter

                override fun getValueFromNanos(playTimeNanos: Long): Float {
                    return initialX + (xVelocity * playTimeNanos / 100_000_000).toFloat()
                }

                override fun getVelocityVectorFromNanos(playTimeNanos: Long): AnimationVector1D {
                    return Float.VectorConverter.convertToVector(targetValue)
                }
            }
        }
        .animateY {
            object : Animation<Float, AnimationVector1D> {
                override val durationNanos: Long
                    get() = Long.MAX_VALUE
                override val isInfinite: Boolean
                    get() = true
                override val targetValue: Float
                    get() = 0f
                override val typeConverter: TwoWayConverter<Float, AnimationVector1D>
                    get() = Float.VectorConverter

                override fun getValueFromNanos(playTimeNanos: Long): Float {
                    val time = playTimeNanos / 100_000_000f
                    return initialY - ((yVelocity * time) + (0.5 * G * (time * time))).toFloat()
                }

                override fun getVelocityVectorFromNanos(playTimeNanos: Long): AnimationVector1D {
                    return Float.VectorConverter.convertToVector(targetValue)
                }
            }
        }
        .terminalCondition { _, interpolatedY, _, _, _, _, _ ->
            interpolatedY > height
        }
        .build()
}

//fun getHighReaction(width: Float, height: Float, initialDelay: Int = 0, direction: Int): ItemState {
//
//}

fun getHeartAnimation(width: Float, height: Float, initialDelay: Int = 0, direction: Int): ItemState {
    val time = 1500
    val fadeTime = 1500
    val xDelta = 100f * direction
    val angle = 25f * direction

    return ItemStateBuilder(
        composeCanvasDrawItem = getImageCanvasObject(Size(220f, 200f), ImageKey.Drawable(R.drawable.hearts_100)),//ImageKey.URL("https://i.picsum.photos/id/548/200/300.jpg?hmac=dXVAc-s_U8QgoYUrMld43VmrOby1cluk-akWgxY6b9Y")),
        initialX = (width / 2f),//Random.nextInt(0, (width).toInt()).toFloat(),
        initialY = (height - 200f),
        initialAngle = 0f,
    )
        .animateX(
            to = {
                initialX
            },
            animationSpec = {
                repeatable(
                    iterations = 1,
                    initialStartOffset = StartOffset(initialDelay),
                    animation = SinWaveAnimationSpec(
                        durationMillis = time,
                        delta = xDelta,
                        easing = FastOutSlowInEasing
                    )
                )
//                SinWaveAnimationSpec(durationMillis = 3500, multiplier = 100)
            }
        )
        .animateY(
            to = {
                height / 2
            },
            animationSpec = {
                repeatable(
                    iterations = 1,
                    initialStartOffset = StartOffset(initialDelay),
                    animation = tween(durationMillis = time, easing = FastOutSlowInEasing)
                )
//                tween(durationMillis = 3500, easing = FastOutSlowInEasing)
            }
        )
        .animateAlpha(
            to = {
                0f
            },
            animationSpec = {
                tween(durationMillis = fadeTime, easing = LinearEasing)
            }
        )
        .animateAngle(
            to = {
                angle
            },
            animationSpec = {
                repeatable(
                    iterations = 1,
                    initialStartOffset = StartOffset(initialDelay),
                    animation = CosineWaveAnimationSpec(durationMillis = time,
                        delta = angle,
                        easing = FastOutSlowInEasing),
//                    animation = keyframes {
//                        durationMillis = 4000
//                        45f at 0 with inverseCosineEasing(FastOutSlowInEasing)
//                        0f at 1000 with sineEasing(FastOutSlowInEasing)
//                        -45f at 2000 with inverseCosineEasing(FastOutSlowInEasing)
//                        0f at 3000 with sineEasing(FastOutSlowInEasing)
//                        45f at 4000 with inverseCosineEasing(FastOutSlowInEasing)
////                        45f at 3600 with SineEasing
//                    }
                )
//                tween(durationMillis = 2500, easing = FastOutSlowInEasing)
            }
        )
//        .animateColor(
//            to = {
//                Color.Green
//            },
//            animationSpec = {
//                tween(durationMillis = 2000)
//            }
//        )
        .animateSize(
            to = {
                1.5f
            },
            animationSpec = {
                spring(dampingRatio = Spring.DampingRatioHighBouncy)
            }
        )
        .build()
}

fun getHeartPNGAnimation(width: Float, height: Float, initialDelay: Int = 0) =
    ItemStateBuilder(
        composeCanvasDrawItem = getCustomCanvasObject(),//getTextCanvasObject("debugdilraj"),//getImageCanvasObject(R.drawable.heart_stack),
        initialX = Random.nextInt(0, (width).toInt()).toFloat(),
        initialY = (height - 200f),
    )
//        .animateX(
//            to = {
//                initialX
//            },
//            animationSpec = {
//                SinWaveAnimationSpec(durationMillis = 3500, multiplier = 100)
//            }
//        )
        .animateColor(
            to = {
                Color.Green
            },
            animationSpec = {
                keyframes {
                    durationMillis = 2000
                    Color.White at 0 with LinearEasing
                    Color.Red at 1000 with LinearEasing
                    Color.Green at 2000 with LinearEasing
                }
            }
        )
        .animateY(
            to = {
                height / 2
            },
            animationSpec = {
                tween(durationMillis = 3500, easing = FastOutSlowInEasing)
            }
        )
        .animateAngle(
            to = {
                360f
            },
            animationSpec = {
                tween(durationMillis = 2500, easing = FastOutSlowInEasing)
            }
        )
        .animateAlpha(
            to = {
                0f
            },
            animationSpec = {
                tween(durationMillis = 5500, easing = LinearEasing)
            }
        )
        .animateSize(
            to = {
                0.1f
            },
            animationSpec = {
                tween(
                    durationMillis = 3500,
                    easing = { fraction ->
                        val startTime = 0.6f
                        if (fraction < startTime) {
                            0f
                        } else {
                            (fraction - startTime) * (1 / (1 - startTime))
                        }
                    }
                )
            }
        )
        .build()

fun getItemCircleLoader(width: Float, height: Float, initialDelay: Int = 0): ItemState {
    val time = 2000
    val iterations = 1000
    return ItemStateBuilder(
        composeCanvasDrawItem = getPathCanvasObject(30f),
        initialX = (width / 2),
        initialY = (height / 2),
    )
        .animateX(
            to = {
                initialX - 400f
            },
            animationSpec = {
                repeatable(
                    iterations = iterations,
                    animation =
                        CosineWaveAnimationSpec(
                            durationMillis = time,
                            delta = -400f
                        )
//                    keyframes {
//                        durationMillis = 8000
//                        (width / 2) - 400f at 0 with inverseCosineEasing()
//                        (width / 2) at 1000 with sineEasing()
//                        (width / 2) + 400f at 2000 with inverseCosineEasing()
//                        (width / 2) at 3000 with sineEasing()
//                        (width / 2) - 400f at 4000 with inverseCosineEasing()
//                        (width / 2) at 5000 with sineEasing()
//                        (width / 2) + 400f at 6000 with inverseCosineEasing()
//                        (width / 2) at 7000 with sineEasing()
//                        (width / 2) - 400f at 8000 with inverseCosineEasing()
//                    }
                    ,
                    initialStartOffset = StartOffset(initialDelay)
                )
            }
        )
        .animateY(
            to = {
                initialY
            },
            animationSpec = {
                repeatable(
                    iterations = iterations,
                    animation =
                        SinWaveAnimationSpec(
                            durationMillis = time,
                            delta = -400f,
                        )
//                    keyframes {
//                        durationMillis = 8000
//                        (height / 2) at 0 with sineEasing()
//                        (height / 2) - 400f at 1000 with inverseCosineEasing()
//                        (height / 2) at 2000 with sineEasing()
//                        (height / 2) + 400f at 3000 with inverseCosineEasing()
//                        (height / 2) at 4000 with sineEasing()
//                    }
                    ,
                    initialStartOffset = StartOffset(initialDelay)
                )
            }
        )
        .animateAngle(
            to = {
                360f
            },
            animationSpec = {
                repeatable(
                    iterations = iterations,
                    animation = keyframes {
                        durationMillis = time
                        0f at 0 with FastOutSlowInEasing
                        360f at time with FastOutSlowInEasing
                    },
                    initialStartOffset = StartOffset(initialDelay)
                )
            }
        )
        .terminalCondition { _, _, _, _, _, _, elapsedTimeMillis ->
            elapsedTimeMillis > time * iterations + initialDelay
        }
        .animateColor(
            to = {
                Color.Green
            },
            animationSpec = {
                repeatable(
                    iterations = iterations,
                    animation = keyframes {
                        durationMillis = time
                        Color.Green at time / 2 with FastOutSlowInEasing
                        initialColor at time with FastOutSlowInEasing
//                        Color.Red at 6000 with FastOutSlowInEasing
//                        initialColor at 8000 with FastOutSlowInEasing
                    },
                    initialStartOffset = StartOffset(initialDelay)
                )
            }
        )
        .build()
}

data class ItemStateHolder(
    val items: List<ItemState> = mutableListOf()
)

// 0, 0.1, 0.2, ..., 0.9, 1, 0.9, 0.8, 0.7, ..., 0.2, 0.1
data class ItemState constructor(
    val id: String = UUID.randomUUID().toString(),
    val x: Float,
    val y: Float,
    val alpha: Float,
    val scale: Float,
    val xAnimation: Animation<Float, AnimationVector1D>,
    val yAnimation: Animation<Float, AnimationVector1D>,
    val alphaAnimation: Animation<Float, AnimationVector1D>,
    val angle: Float,
    val angleAnimation: Animation<Float, AnimationVector1D>,
    val color: Color,
    val colorAnimation: Animation<Color, AnimationVector4D>,
    val scaleAnimation: Animation<Float, AnimationVector1D>,
    val time: Long = System.nanoTime(),
    val itemToDraw: ComposeCanvasDrawItem,
    val terminalCondition: (
        interpolatedX: Float,
        interpolatedY: Float,
        interpolatedAlpha: Float,
        interpolatedAngle: Float,
        interpolatedColor: Color,
        interpolatedScale: Float,
        elapsedTimeMillis: Float,
    ) -> Boolean = { interpolatedX, interpolatedY, interpolatedAlpha, _, _, _, _ ->
        interpolatedX < 0 || interpolatedY < 0 || interpolatedAlpha < 0.05
    },
)

fun sineEasing(internalEasing: Easing = LinearEasing) = Easing { fraction -> sin(internalEasing.transform(fraction) * 2 * Math.PI / 4).toFloat() }

fun inverseCosineEasing(internalEasing: Easing = LinearEasing) = Easing { fraction -> 1 - cos(internalEasing.transform(fraction) * 2 * Math.PI / 4).toFloat() }

sealed class ComposeCanvasDrawItem

data class CanvasPath(
    val path: Path,
) : ComposeCanvasDrawItem()

data class CanvasText(val text: String, val style: () -> TextStyle) :
    ComposeCanvasDrawItem()

data class CanvasImage(val size: Size, val imageKey: ImageKey, val bitmap: Bitmap? = null) :
    ComposeCanvasDrawItem()

data class CanvasObject @OptIn(ExperimentalTextApi::class) constructor(val objectToDraw: DrawScope.(textMeasurer: TextMeasurer, alpha: Float, angle: Float, color: Color, scale: Float) -> Unit) :
    ComposeCanvasDrawItem()

fun getPathCanvasObject(size: Float = 120f) = CanvasPath(
    path = Path().apply {
        heartPath(Size(size, size))
    },
)

fun getTextCanvasObject(text: String = "hi") =
    CanvasText(text) {
        TextStyle.Default.copy(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }

fun getImageCanvasObject(size: Size, imageKey: ImageKey) =
    CanvasImage(size, imageKey)

@OptIn(ExperimentalTextApi::class)
fun getCustomCanvasObject() = CanvasObject(
    objectToDraw = { textMeasurer, alpha, angle, _, _ ->
        val text = "@ayushnasa"
        val emoji =
            //"\uD83D\uDD34"
            "\uD83D\uDD25"

        val textResult = textMeasurer.measure(AnnotatedString(text))

        val emojiTextResult = textMeasurer.measure(
            text = AnnotatedString(emoji),
            style = TextStyle(
                fontSize = 30.sp
            )
        )

        drawText(
            textLayoutResult = textResult,
            brush = SolidColor(Color.White),
            topLeft = Offset(
                x = 0f,
                y = (emojiTextResult.size.height - textResult.size.height) / 2f,
            ),
        )

        drawText(
            textLayoutResult = emojiTextResult,
            brush = SolidColor(Color.Green),
            topLeft = Offset(
                x = textResult.size.width.toFloat(),
                y = 0f
            ),
        )

        val path = Path().apply {
            heartPath(Size(120f, 120f))
        }
        rotate(
            degrees = angle,
            pivot = Offset(
                x = 120f / 2,
                y = 120f / 2
            )
        ) {
            drawPath(path = path, color = Color.Red.copy(alpha = alpha))
        }
    }
)

class ItemStateBuilder(
    val composeCanvasDrawItem: ComposeCanvasDrawItem,
    val initialX: Float,
    val initialY: Float,
    val initialAlpha: Float = 1.0f,
    val initialAngle: Float = 0.0f,
    val initialColor: Color = Color.White,
    val initialScale: Float = 1.0f,
) {
    internal var xAnimation: Animation<Float, AnimationVector1D>? = null
    internal var yAnimation: Animation<Float, AnimationVector1D>? = null
    internal var alphaAnimation: Animation<Float, AnimationVector1D>? = null
    internal var angleAnimation: Animation<Float, AnimationVector1D>? = null
    internal var colorAnimation: Animation<Color, AnimationVector4D>? = null
    internal var scaleAnimation: Animation<Float, AnimationVector1D>? = null

    internal var terminalCondition: ((
        interpolatedX: Float,
        interpolatedY: Float,
        interpolatedAlpha: Float,
        interpolatedAngle: Float,
        interpolatedColor: Color,
        interpolatedScale: Float,
        elapsedTimeMillis: Float,
    ) -> Boolean)? = null
}

fun ItemStateBuilder.build(): ItemState {
    return ItemState(
        id = UUID.randomUUID().toString(),
        x = initialX,
        y = initialY,
        alpha = initialAlpha,
        xAnimation = xAnimation ?: EmptyFloatAnimation(initialX),
        yAnimation = yAnimation ?: EmptyFloatAnimation(initialY),
        alphaAnimation = alphaAnimation ?: EmptyFloatAnimation(initialAlpha),
        angle = initialAngle,
        angleAnimation = angleAnimation ?: EmptyFloatAnimation(initialAngle),
        time = System.nanoTime(),
        itemToDraw = composeCanvasDrawItem,
        color = initialColor,
        colorAnimation = colorAnimation ?: EmptyColorAnimation(initialColor),
        scale = initialAlpha,
        scaleAnimation = scaleAnimation ?: EmptyFloatAnimation(initialScale)
    ).let { itemState ->
        terminalCondition?.let {
            itemState.copy(terminalCondition = it)
        } ?: itemState
    }
}

fun ItemStateBuilder.animateY(
    to: ItemStateBuilder.() -> Float,
    animationSpec: ItemStateBuilder.() -> AnimationSpec<Float>,
): ItemStateBuilder {
    yAnimation = TargetBasedAnimation(
        animationSpec = animationSpec(),
        typeConverter = Float.VectorConverter,
        initialValue = initialY,
        targetValue = to()
    )
    return this
}

fun ItemStateBuilder.animateX(
    to: ItemStateBuilder.() -> Float,
    animationSpec: ItemStateBuilder.() -> AnimationSpec<Float>,
): ItemStateBuilder {
    xAnimation = TargetBasedAnimation(
        animationSpec = animationSpec(),
        typeConverter = Float.VectorConverter,
        initialValue = initialX,
        targetValue = to()
    )
    return this
}

fun ItemStateBuilder.animateX(
    animation: ItemStateBuilder.() -> Animation<Float, AnimationVector1D>,
): ItemStateBuilder {
    xAnimation = animation()
    return this
}

fun ItemStateBuilder.animateY(
    animation: ItemStateBuilder.() -> Animation<Float, AnimationVector1D>,
): ItemStateBuilder {
    yAnimation = animation()
    return this
}

fun ItemStateBuilder.animateSize(
    animation: ItemStateBuilder.() -> Animation<Float, AnimationVector1D>,
): ItemStateBuilder {
    scaleAnimation = animation()
    return this
}

fun ItemStateBuilder.animateColor(
    animation: ItemStateBuilder.() -> Animation<Color, AnimationVector4D>,
): ItemStateBuilder {
    colorAnimation = animation()
    return this
}

fun ItemStateBuilder.animateAlpha(
    animation: ItemStateBuilder.() -> Animation<Float, AnimationVector1D>,
): ItemStateBuilder {
    alphaAnimation = animation()
    return this
}

fun ItemStateBuilder.animateAngle(
    animation: ItemStateBuilder.() -> Animation<Float, AnimationVector1D>,
): ItemStateBuilder {
    angleAnimation = animation()
    return this
}

fun ItemStateBuilder.animateSize(
    to: ItemStateBuilder.() -> Float,
    animationSpec: ItemStateBuilder.() -> AnimationSpec<Float>,
): ItemStateBuilder {
    scaleAnimation = TargetBasedAnimation(
        animationSpec = animationSpec(),
        typeConverter = Float.VectorConverter,
        initialValue = 1f,
        targetValue = to()
    )
    return this
}

fun ItemStateBuilder.animateAngle(
    to: ItemStateBuilder.() -> Float,
    animationSpec: ItemStateBuilder.() -> AnimationSpec<Float>,
): ItemStateBuilder {
    angleAnimation = TargetBasedAnimation(
        animationSpec = animationSpec(),
        typeConverter = Float.VectorConverter,
        initialValue = initialAngle,
        targetValue = to()
    )
    return this
}

fun ItemStateBuilder.animateColor(
    to: ItemStateBuilder.() -> Color,
    animationSpec: ItemStateBuilder.() -> AnimationSpec<Color>,
): ItemStateBuilder {
    val target = to()
    colorAnimation = TargetBasedAnimation(
        animationSpec = animationSpec(),
        typeConverter = Color.VectorConverter(target.colorSpace),
        initialValue = initialColor,
        targetValue = target
    )
    return this
}

fun ItemStateBuilder.animateAlpha(
    to: ItemStateBuilder.() -> Float,
    animationSpec: ItemStateBuilder.() -> AnimationSpec<Float>,
): ItemStateBuilder {
    alphaAnimation = TargetBasedAnimation(
        animationSpec = animationSpec(),
        typeConverter = Float.VectorConverter,
        initialValue = initialAlpha,
        targetValue = to()
    )
    return this
}

fun ItemStateBuilder.terminalCondition(
    terminalCondition: (
        interpolatedX: Float,
        interpolatedY: Float,
        interpolatedAlpha: Float,
        interpolatedAngle: Float,
        interpolatedColor: Color,
        interpolatedScale: Float,
        elapsedTimeMillis: Float,
    ) -> Boolean,
): ItemStateBuilder {
    this.terminalCondition = terminalCondition
    return this
}

class EmptyFloatAnimation private constructor(
    override val targetValue: Float,
    override val typeConverter: TwoWayConverter<Float, AnimationVector1D>,
) : Animation<Float, AnimationVector1D> {

    constructor(targetValue: Float) : this(targetValue, Float.VectorConverter)

    override val isInfinite: Boolean get() = false
    override fun getValueFromNanos(playTimeNanos: Long): Float {
        return targetValue
    }

    @get:Suppress("MethodNameUnits")
    override val durationNanos: Long = 0

    override fun getVelocityVectorFromNanos(playTimeNanos: Long): AnimationVector1D {
        return typeConverter.convertToVector(targetValue)
    }

    override fun toString(): String {
        return "EmptyFloatAnimation: $targetValue,"
    }
}

class EmptyColorAnimation private constructor(
    override val targetValue: Color,
    override val typeConverter: TwoWayConverter<Color, AnimationVector4D>,
) : Animation<Color, AnimationVector4D> {

    constructor(targetValue: Color) : this(targetValue,
        Color.VectorConverter(targetValue.colorSpace))

    override val isInfinite: Boolean get() = false
    override fun getValueFromNanos(playTimeNanos: Long): Color {
        return targetValue
    }

    @get:Suppress("MethodNameUnits")
    override val durationNanos: Long = 0

    override fun getVelocityVectorFromNanos(playTimeNanos: Long): AnimationVector4D {
        return typeConverter.convertToVector(targetValue)
    }

    override fun toString(): String {
        return "EmptyFloatAnimation: $targetValue,"
    }
}

class SinWaveAnimationSpec(
    val durationMillis: Int = AnimationConstants.DefaultDurationMillis,
    val delay: Int = 0,
    val easing: Easing = FastOutSlowInEasing,
    val delta: Float = 100f,
) : DurationBasedAnimationSpec<Float> {

    override fun <V : AnimationVector> vectorize(converter: TwoWayConverter<Float, V>): VectorizedDurationBasedAnimationSpec<V> =
        object : VectorizedDurationBasedAnimationSpec<V> {
            private val anim = VectorizedFloatAnimationSpec<V>(
                SinWaveSpec(durationMillis, delayMillis, easing, delta)
            )

            override fun getValueFromNanos(
                playTimeNanos: Long,
                initialValue: V,
                targetValue: V,
                initialVelocity: V,
            ): V {
                return anim.getValueFromNanos(playTimeNanos,
                    initialValue,
                    targetValue,
                    initialVelocity)
            }

            override fun getVelocityFromNanos(
                playTimeNanos: Long,
                initialValue: V,
                targetValue: V,
                initialVelocity: V,
            ): V {
                return anim.getVelocityFromNanos(playTimeNanos,
                    initialValue,
                    targetValue,
                    initialVelocity)
            }

            override val delayMillis: Int
                get() = this@SinWaveAnimationSpec.delay
            override val durationMillis: Int
                get() = this@SinWaveAnimationSpec.durationMillis
        }

    override fun equals(other: Any?): Boolean =
        if (other is SinWaveAnimationSpec) {
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

class CosineWaveAnimationSpec(
    val durationMillis: Int = AnimationConstants.DefaultDurationMillis,
    val delay: Int = 0,
    val easing: Easing = FastOutSlowInEasing,
    val delta: Float = 100f,
) : DurationBasedAnimationSpec<Float> {

    override fun <V : AnimationVector> vectorize(converter: TwoWayConverter<Float, V>): VectorizedDurationBasedAnimationSpec<V> =
        object : VectorizedDurationBasedAnimationSpec<V> {
            private val anim = VectorizedFloatAnimationSpec<V>(
                CosineWaveSpec(durationMillis, delayMillis, easing, delta)
            )

            override fun getValueFromNanos(
                playTimeNanos: Long,
                initialValue: V,
                targetValue: V,
                initialVelocity: V,
            ): V {
                return anim.getValueFromNanos(playTimeNanos,
                    initialValue,
                    targetValue,
                    initialVelocity)
            }

            override fun getVelocityFromNanos(
                playTimeNanos: Long,
                initialValue: V,
                targetValue: V,
                initialVelocity: V,
            ): V {
                return anim.getVelocityFromNanos(playTimeNanos,
                    initialValue,
                    targetValue,
                    initialVelocity)
            }

            override val delayMillis: Int
                get() = this@CosineWaveAnimationSpec.delay
            override val durationMillis: Int
                get() = this@CosineWaveAnimationSpec.durationMillis
        }

    override fun equals(other: Any?): Boolean =
        if (other is SinWaveAnimationSpec) {
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

class SinWaveSpec(
    val duration: Int = AnimationConstants.DefaultDurationMillis,
    val delay: Int = 0,
    private val easing: Easing = FastOutSlowInEasing,
    private val delta: Float,
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
        return initialValue + delta * (sin(fraction * 2 * Math.PI).toFloat())
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

class CosineWaveSpec(
    val duration: Int = AnimationConstants.DefaultDurationMillis,
    val delay: Int = 0,
    private val easing: Easing = FastOutSlowInEasing,
    private val delta: Float,
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
        return initialValue + delta * (cos(fraction * 2 * Math.PI).toFloat())
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

@OptIn(ExperimentalTextApi::class)
@Composable
fun Heart(modifier: Modifier, _items: () -> List<ItemState>) {

    val items = _items()

//    Log.d("dilraj", "recomposing canvas ${items.joinToString { it.toString() }}")
    Log.d("dilraj", "recomposing canvas")

    val resources = LocalContext.current.resources

//    val bitmaps by remember {
//        derivedStateOf {
//            Log.d("dilraj", "recomposing canvas inside derived state ${items.joinToString { it.toString() }}")
//            _items()
//                .filter { it.itemToDraw is CanvasImage }
//                .map {
//                    (it.itemToDraw as CanvasImage).imageSrc
//                }
//                .associateWith { imageResource ->
//                val originalBitmap = BitmapFactory.decodeResource(
//                    resources,
//                    imageResource,
//                )
//                Bitmap.createScaledBitmap(
//                    originalBitmap,
//                    100,
//                    100 * originalBitmap.height / originalBitmap.width,
//                    true
//                )
//            }
//        }
//    }

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier,
        onDraw = drawScope@{
            for (item in items) {
                translate(top = item.y, left = item.x) {
                    drawContext.canvas.nativeCanvas.apply {
                        when (val itemToDraw = item.itemToDraw) {
                            is CanvasPath -> {
                                scale(
                                    scale = item.scale,
                                    pivot = Offset(
                                        x = itemToDraw.path.getBounds().width / 2f,
                                        y = itemToDraw.path.getBounds().height / 2f
                                    )
                                ) {
                                    rotate(
                                        degrees = item.angle,
                                        pivot = Offset(
                                            x = itemToDraw.path.getBounds().width / 2f,
                                            y = itemToDraw.path.getBounds().height / 2f
                                        )
                                    ) {
                                        drawPath(
                                            path = itemToDraw.path,
                                            color = item.color.copy(alpha = item.alpha)
                                        )
                                    }
                                }
                            }
                            is CanvasText -> {
                                val textResult = textMeasurer.measure(
                                    text = AnnotatedString(itemToDraw.text),
                                    style = itemToDraw.style()
                                )

                                scale(
                                    scale = item.scale,
                                    pivot = Offset(
                                        x = textResult.size.width / 2f,
                                        y = textResult.size.height / 2f
                                    )
                                ) {
                                    rotate(
                                        degrees = item.angle,
                                        pivot = Offset(
                                            x = textResult.size.width / 2f,
                                            y = textResult.size.height / 2f
                                        )
                                    ) {
                                        drawText(
                                            textLayoutResult = textResult,
                                            color = item.color,
                                            alpha = item.alpha,
                                        )
                                    }
                                }
                            }
                            is CanvasObject -> {
                                itemToDraw.objectToDraw(
                                    this@drawScope,
                                    textMeasurer,
                                    item.alpha,
                                    item.angle,
                                    item.color,
                                    item.scale
                                )
                            }
                            is CanvasImage -> {
                                val bitmap = itemToDraw.bitmap
                                bitmap ?: return@drawScope
                                val width = bitmap.width
                                val height = bitmap.height
                                scale(
                                    scale = item.scale,
                                    pivot = Offset(
                                        x = width / 2f,
                                        y = height / 2f
                                    )
                                ) {
                                    rotate(
                                        degrees = item.angle,
                                        pivot = Offset(
                                            x = width / 2f,
                                            y = height / 2f
                                        )
                                    ) {
                                        drawImage(
                                            image = bitmap.asImageBitmap(),
                                            topLeft = Offset.Zero,
                                            alpha = item.alpha,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

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

sealed class ImageKey {
    data class Drawable(@DrawableRes val id: Int) : ImageKey()
    data class URL(val url: String) : ImageKey()
}
