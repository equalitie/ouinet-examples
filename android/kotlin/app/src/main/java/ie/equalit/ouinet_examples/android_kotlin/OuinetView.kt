package ie.equalit.ouinet_examples.android_kotlin

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable

private val PADDING = 6.dp

@Serializable
object OuinetStatusRoute

@Composable
fun OuinetView(viewModel: OuinetViewModel) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = OuinetStatusRoute,
    ) {
        composable<OuinetStatusRoute> { OuinetStatusScreen(viewModel) }
    }
}

@Composable
fun OuinetStatusScreen(viewModel: OuinetViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    Scaffold(
        topBar = { TopBar("Ouinet Tester") },
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(PADDING),
            modifier = Modifier.padding(padding)
        ) {
            Log.d("OuinetView", "Created column?")
            CenterAlignedRow {
                Text(text = "State: ${uiState.state}")
            }
            if (uiState.state != "Started") {
                CenterAlignedRow {
                    TextButton(
                        onClick = {
                            viewModel.start(context)
                        }
                    ) {
                        Text(text = "Start")
                    }
                }
            }
            else {
                CenterAlignedRow {
                    TextField(
                        value = uiState.currentUrl,
                        label = { Text("Request") },
                        onValueChange = { value ->
                            uiState.currentUrl = value
                        }
                    )
                    TextButton(
                        onClick = {
                            viewModel.getUrl(context)
                        }
                    ) {
                        Text(text = "Go")
                    }
                }
            }
            CenterAlignedRow {
                Text(text = "Groups count: ${uiState.groupsCount}")
            }
            CenterAlignedRow {
                Text(text = "Cache size: ${uiState.cacheSize}")
            }
            CenterAlignedRow {
                TextButton(
                    onClick = {
                        viewModel.restart(context)
                    }
                ) {
                    Text(text = "Restart")
                }
            }
            CenterAlignedRow {
                TextButton(
                    onClick = {
                        viewModel.clearCache(context)
                    }
                ) {
                    Text(text = "Clear")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(title: String, navController: NavController? = null) {
    TopAppBar(
        title = {
            Text(
                title,
                // TODO: Use StartEllipsis or MiddleEllipsis when it becomes available
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        navigationIcon = {
            if (navController != null) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            }
        },
    )
}

@Composable
fun CenterAlignedRow(composable: @Composable () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(PADDING).fillMaxWidth(),
    ) {
        composable.invoke()
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(widthDp = 360, heightDp = 720)
@Composable
fun ComposablePreview() {
    MaterialTheme { OuinetView(OuinetViewModel(null, null)) }
}
