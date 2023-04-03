package com.example.ffxivprofitestimator.android

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ffxivprofitestimator.App
import com.example.ffxivprofitestimator.XIVAPI
import com.example.ffxivprofitestimator.android.ui.theme.AppTheme
import com.jetbrains.handson.kmm.shared.cache.DatabaseDriverFactory
import com.jetbrains.handson.kmm.shared.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
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
                    color = MaterialTheme.colors.surface,
                    contentColor = MaterialTheme.colors.onSurface
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
        drawerBackgroundColor = MaterialTheme.colors.surface,
        drawerContentColor = MaterialTheme.colors.onSurface,
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
                val buttonColors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary,
                    disabledBackgroundColor = MaterialTheme.colors.surface,
                    disabledContentColor = MaterialTheme.colors.onSurface
                )
                val borderColor = MaterialTheme.colors.surface
                val ddbColor = MaterialTheme.colors.surface
                val ddcColor = MaterialTheme.colors.onSurface
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
                    buttonColors = buttonColors,
                    borderColor = borderColor,
                    dropdownBackgroundColor = ddbColor,
                    dropdownContentColor = ddcColor
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
                    buttonColors = buttonColors,
                    borderColor = borderColor,
                    dropdownBackgroundColor = ddbColor,
                    dropdownContentColor = ddcColor
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
                    modifier = Modifier
                        .padding(5.dp)
                )
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { onThemeChange() },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MaterialTheme.colors.onSurface,
                        checkedThumbColor = MaterialTheme.colors.onSurface,
                        uncheckedTrackColor = MaterialTheme.colors.onSurface,
                        uncheckedThumbColor = MaterialTheme.colors.onSurface,
                    ),
                    modifier = Modifier
                        .padding(5.dp)
                )
            }
        }
    ) { padding ->
        val padding = padding
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .padding(padding)
        ) {
            composable("home") {
                Column(
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = {
                        coroutineScope.launch {
                            XIVAPI.getItem(app.rinascitaSwordID)
                            navController.navigate("item_info/${app.rinascitaSwordID}")
                        }
                    }) {
                        Text("Test item 1!")
                    }
                    Button(onClick = {
                        coroutineScope.launch {
                            XIVAPI.getItem(app.rinascitaSwordID + 1)
                            navController.navigate("item_info/${app.rinascitaSwordID + 1}")
                        }
                    }) {
                        Text("Test item 2!")
                    }
                }
            }
            composable("settings") {  }
            composable(
                "item_info/{item_id}",
                arguments = listOf(navArgument("item_id") { type = NavType.IntType })
            ) { navBackStackEntry ->
                navBackStackEntry.arguments?.getInt("item_id")?.let { itemId ->
                    val itemCache = XIVAPI.getItemCache().getEntries()
                    val iconCache = XIVAPI.getIconCache().getEntries()
                    if (itemCache.containsKey(itemId)) ItemScreen(
                        selectedItemId = itemId
                    )
                    else ErrorScreen(errorText = "Oops! That item doesn't exist!")
                }
            }
        }
    }
}

@Composable
fun ErrorScreen(errorText: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize(0.8f)
            .background(MaterialTheme.colors.error)
            .clip(RoundedCornerShape(30))
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = "ErrorIcon",
                tint = MaterialTheme.colors.onError
            )
            Text(
                text = errorText,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colors.onError
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemScreen(
    selectedItemId: Int
) {
    var id by remember { mutableStateOf(selectedItemId) }
    XIVAPI.getItemCache().moveToFront(id)
    XIVAPI.getIconCache().moveToFront(id)
    val itemCache = XIVAPI.getItemCache().getEntries()
    val iconCache = XIVAPI.getIconCache().getEntries()
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10))
                .background(MaterialTheme.colors.secondary)
                .weight(1f)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(5.dp)
            ) {
                itemCache[id]?.let {
                    CacheItem(
                        item = it,
                        icon = iconCache[it.id],
                        contentColor = MaterialTheme.colors.onPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.2f)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colors.primary)
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10))
                .background(MaterialTheme.colors.secondary)
                .weight(1f)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(5.dp)
            ) {
                itemCache.entries.reversed().forEach { entry ->
                    item(
                        key = entry.key,
                        contentType = Item
                    ) {
                        CacheItem(
                            item = entry.value,
                            icon = iconCache[entry.key],
                            contentColor = MaterialTheme.colors.onPrimary,
                            withButton = true,
                            onNavigate = { itemId ->
                                id = itemId
                            },
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .fillParentMaxHeight(0.2f)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colors.primary)
                                .animateItemPlacement()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemInfo(item: Item, icon: ByteArray?, contentColor: Color, modifier: Modifier) {

}

@Composable
fun CacheItem(
    item: Item,
    icon: ByteArray?,
    contentColor: Color,
    modifier: Modifier,
    withButton: Boolean = false,
    onNavigate: (itemId: Int) -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
    ) {
        icon?.let {
            ImageFromByteArray(
                byteArray = it,
                scale = ContentScale.FillHeight,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(2.dp)
                    .clip(CircleShape)
                    .border(2.dp, contentColor, CircleShape)
            )
        } ?: Icon(Icons.Default.List, contentDescription = "DefaultIcon")
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text(
                text = item.name,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier
            )
            Text(
                text = item.recipeIngredients.joinToString(),
                color = contentColor,
                modifier = Modifier
            )
        }
        if (withButton) IconButton(
            onClick = { onNavigate(item.id) },
            modifier = Modifier
                .padding(2.dp)
                .align(Alignment.CenterVertically)
        ) {
            Icon(
                Icons.Default.PlayArrow,
                tint = contentColor,
                contentDescription = "ShowArrow"
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
    borderColor: Color,
    buttonColors: ButtonColors,
    dropdownBackgroundColor: Color,
    dropdownContentColor: Color,
    onActiveElementChange: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Button(
        onClick = { expanded = true },
        shape = CutCornerShape(30),
        border = BorderStroke(Dp(2f), borderColor),
        colors = buttonColors,
        modifier = modifier,
        enabled = enabled,
        elevation = ButtonDefaults.elevation(
            defaultElevation = 10.dp,
            disabledElevation = (-5).dp,
            focusedElevation = 15.dp,
            hoveredElevation = 15.dp,
            pressedElevation = 0.dp,
        )
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
                .background(dropdownBackgroundColor)
        ) {
            elements.forEach {
                DropdownMenuItem(
                    onClick = {
                        onActiveElementChange(it)
                        expanded = false
                    },
                    modifier = Modifier
                        .background(dropdownBackgroundColor)
                ) {
                    Text(
                        it?.toString() ?: defaultItemString,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = dropdownContentColor
                    )
                }
            }
        }
    }
}
