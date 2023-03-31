package com.example.ffxivprofitestimator.android

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ffxivprofitestimator.App
import com.example.ffxivprofitestimator.XIVAPI
import com.example.ffxivprofitestimator.android.ui.theme.AppTheme
import com.jetbrains.handson.kmm.shared.cache.DatabaseDriverFactory
import com.jetbrains.handson.kmm.shared.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

lateinit var app: App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = App(DatabaseDriverFactory(this))

        setContent {
            var darkTheme by remember { mutableStateOf(true) }
            AppTheme(useDarkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    LaunchedEffect(app) {
                        app.updateDB()
                    }
                    MenuView(
                        servers = app.getDatacenters(),
                        isDarkMode = darkTheme
                    ) {
                        darkTheme = !darkTheme
                    }
                    //TestIconCreation()
                }
            }
        }
    }
}

@Composable
fun MenuView(servers: List<DataCenter>, isDarkMode: Boolean, onThemeChange: () -> Unit) {
    val scaffoldState: ScaffoldState = rememberScaffoldState()
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    var chosenDC: DataCenter? by remember { mutableStateOf(null) }
    var chosenWorld: World? by remember { mutableStateOf(null) }
    var searchBar: Boolean by remember { mutableStateOf(false) }
    var searchContent: TextFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    Scaffold(
        scaffoldState = scaffoldState,
        backgroundColor = MaterialTheme.colors.background,
        contentColor = MaterialTheme.colors.onBackground,
        drawerBackgroundColor = MaterialTheme.colors.primary,
        drawerContentColor = MaterialTheme.colors.onPrimary,
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.onBackground,
                title = {
                    if (!searchBar) {
                        Text(
                            text = "FFXIV Profit Estimator"
                        )
                    } else {
                        TextField(
                            value = searchContent,
                            onValueChange = {
                                searchContent = it
                            },
                            placeholder = {
                                Text("Search for an item...")
                            },
                            label = {
                                Text("Item search")
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.List,
                                    contentDescription = "List"
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Begin search"
                                )
                            },
                            colors = TextFieldDefaults.textFieldColors(
                                backgroundColor = MaterialTheme.colors.primary,
                                textColor = MaterialTheme.colors.onPrimary,
                                cursorColor = MaterialTheme.colors.onPrimary
                            ),
                            shape = RectangleShape
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            scaffoldState.drawerState.apply {
                                if (isClosed) open() else close()
                            }
                        }
                    }) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Menu"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        searchBar = !searchBar
                    }) {
                        Icon(
                            if (!searchBar) Icons.Default.Search else Icons.Default.Close,
                            contentDescription = "Search"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.onBackground
            ) {
                SelectionButton(
                    modifier = Modifier
                        .weight(1f)
                        .padding(5.dp),
                    activeElement = chosenDC,
                    elements = servers,
                    onActiveElementChange = {
                        chosenDC = it
                        chosenWorld = null
                    },
                    defaultDropdownString = "Datacenters...",
                    defaultItemString = "Unnamed DC",
                )
                SelectionButton(
                    modifier = Modifier
                        .weight(1f)
                        .padding(5.dp),
                    activeElement = chosenWorld,
                    elements = chosenDC?.name?.let { app.getWorldsOfDatacenter(it) } ?: emptyList(),
                    onActiveElementChange = { chosenWorld = it },
                    enabled = chosenDC != null,
                    defaultDropdownString = "Worlds...",
                    defaultItemString = "Unnamed World",
                )
            }
        },
        drawerContent = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    "Dark mode",
                    color = MaterialTheme.colors.onPrimary,
                    modifier = Modifier
                        .padding(5.dp)
                )
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { onThemeChange() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colors.onPrimary,
                        checkedTrackColor = MaterialTheme.colors.onPrimary,
                        uncheckedThumbColor = MaterialTheme.colors.onPrimary,
                        uncheckedTrackColor = MaterialTheme.colors.onPrimary
                    ),
                    modifier = Modifier
                        .padding(5.dp)
                )
            }
        }
    ) {
        Box(
            modifier = Modifier.padding(it)
        ) {
            Text("Ayyy lmao")
        }
    }
}

@Preview
@Composable
fun TestIconCreation() {
    var item: Item? by remember { mutableStateOf(null) }
    LaunchedEffect(true) {
        item = XIVAPI.getItem(app.rinascitaSwordID)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
    ) {
        item?.let {
            CacheItem(item = it)
        }
    }
}

@Composable
fun CacheItem(item: Item) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.1f)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colors.primary)
    ) {
        XIVAPI.getCachedIcon(item.id)?.let {
            ImageFromByteArray(
                byteArray = it,
                scale = ContentScale.FillHeight,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 0.dp, bottom = 0.dp, start = 0.dp, end = 5.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colors.secondary, CircleShape)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(5.dp)
        ) {
            Text(
                text = item.name,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(5.dp)
            )
            Text(
                text = item.recipeIngredients.joinToString(),
                modifier = Modifier
                    .weight(1f)
                    .padding(5.dp)
            )
        }
    }
}

@Composable
fun ImageFromByteArray(byteArray: ByteArray, modifier: Modifier, scale: ContentScale) {
    val bm = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size).asImageBitmap()
    Image(
        bitmap = bm,
        contentDescription = "ItemIcon",
        contentScale = scale,
        modifier = modifier,
    )
}

@Composable
fun <T> SelectionButton(
    modifier: Modifier,
    enabled: Boolean = true,
    defaultDropdownString: String = "Unnamed",
    defaultItemString: String = "Unnamed",
    activeElement: T,
    elements: List<T>,
    onActiveElementChange: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Button(
        onClick = { expanded = true },
        shape = CutCornerShape(30),
        border = BorderStroke(Dp(2f), MaterialTheme.colors.onSecondary),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.secondary,
            contentColor = MaterialTheme.colors.onSecondary,
            disabledBackgroundColor = Color.Gray,
            disabledContentColor = Color.DarkGray
        ),
        modifier = modifier,
        enabled = enabled
    ) {
        Text(
            text = activeElement?.toString() ?: defaultDropdownString,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(MaterialTheme.colors.background)
        ) {
            elements.forEach {
                DropdownMenuItem(
                    onClick = {
                        onActiveElementChange(it)
                        expanded = false
                    },
                    modifier = Modifier
                        .background(MaterialTheme.colors.secondary)
                ) {
                    Text(
                        it?.toString() ?: defaultItemString,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onSecondary
                    )
                }
            }
        }
    }
}
