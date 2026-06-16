package com.example.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.AdBlocker

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val currentUrl by viewModel.currentUrl.collectAsState()
    val loadingProgress by viewModel.loadingProgress.collectAsState()
    val isWebViewLoading by viewModel.webViewLoading.collectAsState()
    val accent by viewModel.accentColor.collectAsState()
    val blockLevel by viewModel.adBlockLevel.collectAsState()

    val accentColor = Color(accent.hex)

    BackHandler(enabled = true) {
        val handled = viewModel.handleBackPressed()
        if (!handled) {
            // Let system handle standard exit triggers or default
        }
    }

    Column(modifier = modifier.fillMaxSize().background(Color(0xFF090A0F))) {
        // Linear animated loading bar aligned with WebChrome loading progress
        if (isWebViewLoading) {
            LinearProgressIndicator(
                progress = { loadingProgress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = accentColor,
                trackColor = Color(0xFF131520)
            )
        } else {
            Spacer(modifier = Modifier.height(3.dp).background(Color(0xFF090A0F)))
        }

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        builtInZoomControls = true
                        displayZoomControls = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }

                    // Enable Cookie Managers
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, true)

                    webViewClient = object : WebViewClient() {
                        
                        // Active filtering / Adblock interception
                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            val url = request?.url?.toString() ?: return null
                            
                            if (AdBlocker.shouldBlock(url, blockLevel)) {
                                val host = request.url?.host ?: "blocked-request"
                                // Log the blocked request asynchronously
                                viewModel.triggerBlockedAd(host, currentUrl)
                                
                                // Return blank dummy response to bypass downloading this resource
                                return WebResourceResponse(
                                    "text/plain",
                                    "UTF-8",
                                    java.io.ByteArrayInputStream(ByteArray(0))
                                )
                            }
                            return super.shouldInterceptRequest(view, request)
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            url?.let {
                                viewModel.updateWebPageState(
                                    url = it,
                                    title = null,
                                    canBack = view?.canGoBack() ?: false,
                                    canForward = view?.canGoForward() ?: false
                                )
                            }
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            url?.let {
                                viewModel.updateWebPageState(
                                    url = it,
                                    title = view?.title,
                                    canBack = view?.canGoBack() ?: false,
                                    canForward = view?.canGoForward() ?: false
                                )
                            }
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            // Handle failures gracefully. Do not crash.
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            viewModel.updateLoadingProgress(newProgress)
                        }

                        override fun onReceivedTitle(view: WebView?, title: String?) {
                            super.onReceivedTitle(view, title)
                            title?.let {
                                viewModel.updateWebPageState(
                                    url = view?.url ?: currentUrl,
                                    title = it,
                                    canBack = view?.canGoBack() ?: false,
                                    canForward = view?.canGoForward() ?: false
                                )
                            }
                        }
                    }

                    // Save reference to viewmodel for navigation controls
                    viewModel.systemWebViewRef = this
                    
                    // Begin loading initial or requested page
                    if (currentUrl != "vortex://home") {
                        loadUrl(currentUrl)
                    }
                }
            },
            update = { webView ->
                // Guard: Only reload or update URL if the native WebView's active url does not match our state
                val webViewUrl = webView.url
                if (currentUrl != "vortex://home" && webViewUrl != currentUrl) {
                    webView.loadUrl(currentUrl)
                }
            }
        )
    }
}
