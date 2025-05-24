package com.example.newsaggregator.ui

import android.R.attr.text
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

import com.example.newsaggregator.ui.theme.NewsAggregatorTheme
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import nl.adaptivity.xmlutil.serialization.XML
import okhttp3.MediaType
import retrofit2.Retrofit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHost
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.newsaggregator.data.rss.dto.ItemDto

import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.newsaggregator.AppModule.RssFeed
import androidx.hilt.navigation.compose.hiltViewModel
import android.text.Html
import android.text.Spanned
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TextButton
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment



@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewsAggregatorTheme {
                val navController = rememberNavController()
                val viewModel: ViewModel = hiltViewModel()
                val snackbarHostState = remember { SnackbarHostState() }

                Scaffold(modifier = Modifier.fillMaxSize(),

                        snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState){
                        ErrorSnackbar(
                            onDismiss = { viewModel.loadRssFeed() },
                            snackbarHostState = snackbarHostState
                        )
                    }

                }

                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "list",
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        composable("list") {
                            Greeting(
                               // text = "Press me!",
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable("webview/{url}") { backStackEntry ->
                            val url = backStackEntry.arguments?.getString("url") ?: ""
                            WebViewScreen(url = url, navController = navController)
                        }

                    }

                }
            }
        }
    }


    /*@Composable
    fun Screen( viewModel: ViewModel,
                navController: NavController,
                snackbarHostState: SnackbarHostState) {
        val state by viewModel.state.collectAsState()
        when(state){
            is State.Completed -> Greeting(navController,   viewModel )
                is State.Wait -> LoadingIndicator()
            is State.Error -> {

                lifecycleScope.launchWhenStarted {
                    val result = snackbarHostState.showSnackbar(
                        message = "Ошибка подключения сети",
                        actionLabel = "ПОВТОРИТЬ",


                        duration = SnackbarDuration.Indefinite
                    )
                    when (result) {
                        SnackbarResult.ActionPerformed -> {
                            viewModel.start()
                        }

                        SnackbarResult.Dismissed -> {
                            //viewModel.start()
                        }
                }


                }
            }

            State.ColdStart -> LoadingIndicator()
        }


    }*/


    @Composable
    fun Greeting(
        navController: NavController,
       /* text: String,
        modifier: Modifier = Modifier,*/
        viewModel: ViewModel

        //feed: RssFeed,
    ) {


        val scope = rememberCoroutineScope()
        val rssFeed by viewModel.rssFeed.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.loadRssFeed()
        }

        /*Button(
            onClick = {
                Log.d("happy", "done")
                scope.launch {
                    val r = rssFeed
                    r?.channel?.items?.forEach {
                        Log.d("link", it.link)
                        Log.d("guid", it.guid)
                        Log.d("dcDate", it.dcDate)
                        Log.d("pubDate", it.pubDate)
                    }
                }
            }
        ) {
            Text(
                text = text,
                modifier = modifier,
            )

        }*/




        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                count = rssFeed?.channel?.items?.size ?: 0
            ) { index ->
                val item = rssFeed?.channel?.items?.get(index)
                item?.let {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable {
                                val encodedUrl = java.net.URLEncoder.encode(it.link, "UTF-8")
                                navController.navigate("webview/$encodedUrl")
                            },


                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = it.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            val a = it.contents[1].url
                            a.let { imageUrl ->
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Article image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop,
                                    /* placeholder = painterResource(id = R.drawable.ic_placeholder), // опционально
                                     error = painterResource(id = R.drawable.ic_error) // опционально*/
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Text(
                                maxLines = 5,
                                overflow = TextOverflow.Ellipsis,

                                text = Html.fromHtml(it.description, Html.FROM_HTML_MODE_LEGACY)
                                    .toString().trim(),
                                style = MaterialTheme.typography.bodyMedium,

                                )


                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun WebViewScreen(url: String, navController: NavController) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Полная новость") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            AndroidView(
                modifier = Modifier.padding(padding),
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = WebViewClient()

                        loadUrl(url)
                    }
                }
            )
        }
    }


    @Composable
    fun LoadingIndicator() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }

    @Composable
    fun ErrorSnackbar(

        onDismiss: () -> Unit,
        snackbarHostState: SnackbarHostState,

        ) {
        SnackbarHost(
            hostState = snackbarHostState,
            Modifier.fillMaxWidth(),
            snackbar = { data ->

                Snackbar(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp),
                    contentColor = MaterialTheme.colorScheme.secondary,
                    action = {
                        data?.  let { _ ->
                            TextButton(onClick = {

                                data.performAction()
                                onDismiss
                            }) {
                                Text(
                                    text = "ПОВТОРИТЬ",
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                ) {

                    Text(
                        text = "Ошибка подключения сети",
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {

    }
}