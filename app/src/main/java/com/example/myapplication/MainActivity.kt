package com.example.myapplication

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.compose.type_safe_args.annotation.ArgumentProvider
import com.compose.type_safe_args.annotation.ComposeDestination
import com.compose.type_safe_args.annotation.HasDefaultValue
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import java.io.Serializable
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
    var heartCount by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        repeat(heartCount) {
            Heart(
                modifier = Modifier
//                    .fillMaxSize()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 36.dp),
                horizontalPadding = 24,
                bottomMargin = 110
            )
        }

        Button(
            onClick = {
                heartCount++
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

enum class HeartState {
    Show,
    Hide
}

@Composable
fun Heart(modifier: Modifier, horizontalPadding: Int, bottomMargin: Int) {
    val width = LocalConfiguration.current.screenWidthDp
    val height = LocalConfiguration.current.screenHeightDp - bottomMargin

    val yRandom = Random.nextInt(0, height / 2)
    val xRandom = Random.nextInt(horizontalPadding, (width - horizontalPadding))

    val heartState = remember {
        mutableStateOf(HeartState.Show)
    }

    val offsetYAnimation: Dp by animateDpAsState(
        targetValue = when (heartState.value) {
            HeartState.Show -> height.dp
            else -> yRandom.dp
        },
        animationSpec = tween(1000)
    )

    val offsetXAnimation: Dp by animateDpAsState(
        targetValue = when (heartState.value) {
            HeartState.Show -> (((width - (horizontalPadding * 2)) / 2) + 8).dp
            else -> xRandom.dp
        },
        animationSpec = tween(1000)
    )

    LaunchedEffect(key1 = heartState, block = {
        heartState.value = when (heartState.value) {
            HeartState.Show -> HeartState.Hide
            HeartState.Hide -> HeartState.Show
        }
    })

    AnimatedVisibility(
        visible = heartState.value == HeartState.Show,
        enter = fadeIn(animationSpec = tween(durationMillis = 250)),
        exit = fadeOut(animationSpec = tween(durationMillis = 700))
    ) {
        Canvas(modifier = modifier.background(Color.Green)
            .offset(y = offsetYAnimation, x = offsetXAnimation),
            onDraw = {
                val path = Path().apply {
                    heartPath(Size(120f, 120f))
                }
                drawContext.canvas.nativeCanvas.apply {
                    drawText("hello\uD83D\uDE0A", 0f, 0f, android.graphics.Paint().apply {
                        color = android.graphics.Color.GREEN
                        isAntiAlias = true
                        textSize = 124f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    })
                }
                drawPath(
                    path = path,
                    color = Color.Red,
                )
            }
        )
    }
}

@Composable
fun Heart() {
    Canvas(modifier = Modifier
        .fillMaxSize(),
        onDraw = {
            val path = Path().apply {
                heartPath(Size(120f, 120f))
            }

            drawPath(
                path = path,
                color = Color.Red,
            )
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

enum class T : Parcelable {
    A,
    B;

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(this.ordinal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<T> {
        override fun createFromParcel(parcel: Parcel): T {
            return when (parcel.readInt()) {
                0 -> A
                else -> B
            }
        }

        override fun newArray(size: Int): Array<T?> {
            return arrayOfNulls(size)
        }
    }
}

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
