package com.example.myapplication.ui.theme

import androidx.compose.ui.graphics.Color
import com.compose.type_safe_args.annotation.ComposeDestination
import com.example.myapplication.User

val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)

@ComposeDestination
interface UserPage {
    val userId: String
    val uniqueUser: User

    companion object
}
