package com.example.ffxivprofitestimator.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.example.ffxivprofitestimator.App
import com.jetbrains.handson.kmm.shared.cache.DatabaseDriverFactory
import com.jetbrains.handson.kmm.shared.entity.*

lateinit var app: App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = App(DatabaseDriverFactory(this))

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    LaunchedEffect(app) {
                        app.updateDB()
                    }
                    MenuView(servers = app.getDatacenters())
                }
            }
        }
    }
}

@Composable
fun MenuView(servers: List<DataCenter>)
{
    var chosenDC: DataCenter? by remember { mutableStateOf(null) }
    var chosenWorld: World? by remember { mutableStateOf(null) }
    Column(modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.TopStart)
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.05f)
            .background(Color.White)
        ) {
            Text(text = "FFXIV Profit Estimator",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }
        Column(verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.TopStart)
                .padding(Dp(10f))
        )
        {
            Row(horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
            ) {
                SelectionButton(
                    modifier = Modifier.weight(1f),
                    activeElement = chosenDC,
                    elements = servers,
                    onActiveElementChange = { chosenDC = it },
                )
                SelectionButton(
                    modifier = Modifier.weight(1f),
                    activeElement = chosenWorld,
                    elements = chosenDC?.name?.let { app.getWorldsOfDatacenter(it) } ?: emptyList(),
                    onActiveElementChange = { chosenWorld = it },
                )
            }
        }
    }
}

@Composable
fun <T> SelectionButton(modifier: Modifier, enabled: Boolean = true, activeElement: T, elements: List<T>, onActiveElementChange: (T) -> Unit)
{
    var expanded by remember { mutableStateOf(false) }
    Button(onClick = { expanded = true },
        shape = CutCornerShape(30),
        border = BorderStroke(Dp(2f), Color.Black),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.Red,
            contentColor = Color.White,
            disabledBackgroundColor = Color.Gray,
            disabledContentColor = Color.DarkGray
        ),
        modifier = modifier,
        enabled = enabled
    ) {
        Text(text = activeElement.toString(),
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold
        )
        DropdownMenu(expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            elements.forEach {
                DropdownMenuItem(onClick = {
                    onActiveElementChange(it)
                    expanded = false
                }) {
                    Text(it.toString(),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun GreetingView(text: String) {
    Text(text = text)
}

@Preview
@Composable
fun DefaultPreview() {
    val dcs: List<DataCenter> = listOf(
        DataCenter(
            name = "Elemental",
            region = "Japan",
            worlds = listOf(45, 49, 50, 58, 68, 72, 90, 94)
        ),
        DataCenter(
            name = "Gaia",
            region = "Japan",
            worlds = listOf(43, 46, 51, 59, 69, 76, 92, 98)
        )
    )
    MyApplicationTheme {
        MenuView(servers = dcs)
    }
}
