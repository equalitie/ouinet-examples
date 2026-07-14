package ie.equalit.ouinet_examples.android_compose

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable

private val PADDING = 4.dp

@Serializable object OuinetStatusRoute

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
    val defaultUrl = stringResource(R.string.url_example)
    var text by remember { mutableStateOf(defaultUrl) }
    var checked by remember { mutableStateOf(true) }
    val context = LocalContext.current
    Scaffold(
        topBar = { TopBar(stringResource(R.string.app_title)) },
        ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(PADDING),
            modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()),
            ) {
            CenterAlignedRow {
                Text(text = "State: ${uiState.state}")
            }
            if (uiState.state != "Started") {
                CenterAlignedRow {
                    Button(
                        onClick = {
                            viewModel.start(context)
                        },
                        modifier = Modifier.testTag("start_button")
                    ) {
                        Text(text = stringResource(R.string.start))
                    }
                }
            } else {
                CenterAlignedRow {
                    Button(
                        onClick = {
                            viewModel.restart(context)
                        }
                    ) {
                        Text(text = stringResource(R.string.restart))
                    }
                }
            }
            CenterAlignedRow {
                Text(text = "P: ${uiState.proxyPort} / F: ${uiState.frontendPort}")
            }
            CenterAlignedRow {
                TextField(
                    value = text,
                    label = { Text("Request") },
                    enabled = viewModel.isRequestEnabled(),
                    onValueChange = { value ->
                        text = value
                    }
                )
                Button(
                    shape = RectangleShape,
                    enabled = viewModel.isGetEnabled(),
                    onClick = {
                        uiState.currentUrl = text
                        viewModel.fetchUrl(context)
                    }
                ) {
                    Text(text = stringResource(R.string.get))
                }
            }
            CenterAlignedRow {
                Text(text = stringResource(R.string.groups_text, uiState.groupsCount))
            }
            CenterAlignedRow {
                Text(text = stringResource(R.string.cache_text,uiState.cacheSize))
            }
            CenterAlignedRow {
                Switch(
                    checked = checked,
                    enabled = viewModel.isPollingSwitchEnabled(),
                    onCheckedChange = {
                        checked = it
                        uiState.pollingEnabled = it
                    }
                )
                Text(
                    text = stringResource(R.string.frontend_polling),
                    modifier = Modifier.padding(9.dp)
                )
            }
            CenterAlignedRow {
                Button(
                    enabled = viewModel.isClearEnabled(),
                    onClick = {
                        viewModel.clearCache(context)
                    }
                ) {
                    Text(text = stringResource(R.string.clear))
                }
            }
            CenterAlignedRow {
                Button(
                    enabled = viewModel.isShutdownEnabled(),
                    onClick = {
                        viewModel.shutdown(context)
                    }
                ) {
                    Text(text = stringResource(R.string.shutdown))
                }
            }
            if (viewModel.isResponseHeaderReady()) {
                LeftAlignedRow {
                    Text(
                        text = stringResource(R.string.response_header),
                        fontWeight = Bold
                    )
                }
                LeftAlignedRow {
                    Text(text = uiState.response)
                }
            }
            if (viewModel.isResponseBodyReady()) {
                LeftAlignedRow {
                    Text(
                        text = stringResource(R.string.response_body),
                        fontWeight = Bold
                    )
                }
                LeftAlignedRow {
                    Text(text = uiState.body)
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
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                )
                },
        navigationIcon = {
            if (navController != null) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            }
        }
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

@Composable
fun LeftAlignedRow(composable: @Composable () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Start,
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
