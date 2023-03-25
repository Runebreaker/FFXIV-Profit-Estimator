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
import com.example.ffxivprofitestimator.UniversalisAPI.DataCenter
import com.example.ffxivprofitestimator.UniversalisAPI.World
import com.example.ffxivprofitestimator.UniversalisAPI.getWorlds
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    GreetingView(App().greet())
                }
            }
        }
    }
}

@Composable
fun MenuView(servers: List<DataCenter>, worlds: Map<Int, World>)
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
                var dcExpanded by remember { mutableStateOf(false) }
                Button(onClick = { dcExpanded = true },
                    shape = CutCornerShape(30),
                    border = BorderStroke(Dp(2f), Color.Black),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Red,
                        contentColor = Color.White,
                        disabledBackgroundColor = Color.Gray,
                        disabledContentColor = Color.DarkGray
                    )
                ) {
                    Text(text = chosenDC?.name ?: "Choose a Datacenter",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold
                    )
                    DropdownMenu(expanded = dcExpanded,
                        onDismissRequest = { dcExpanded = false }
                    ) {
                        servers.forEach { 
                            DropdownMenuItem(onClick = {
                                chosenDC = it
                                dcExpanded = false
                            }) {
                                Text(it.name ?: "Error",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(Dp(10f)))
                var worldExpanded by remember { mutableStateOf(false) }
                Button(onClick = { worldExpanded = true },
                    shape = CutCornerShape(30),
                    border = BorderStroke(Dp(2f), Color.Black),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Red,
                        contentColor = Color.White,
                        disabledBackgroundColor = Color.Gray,
                        disabledContentColor = Color.DarkGray
                    ),
                    enabled = chosenDC != null
                ) {
                    Text(text = chosenWorld?.name ?: "Choose a World...")
                    DropdownMenu(expanded = worldExpanded, onDismissRequest = { worldExpanded = false }) {
                        chosenDC?.worlds?.forEach { id ->
                            DropdownMenuItem(onClick = {
                                chosenWorld = worlds[id]
                                worldExpanded = false
                            }) {
                                Text(text = worlds[id]?.name ?: "Error")
                            }
                        }
                    }
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
    val worlds: Map<Int, World> = mapOf(
        45 to World(45, "Test")
    )
    MyApplicationTheme {
        MenuView(servers = dcs, worlds = worlds)
    }
}
