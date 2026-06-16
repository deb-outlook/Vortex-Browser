package com.example.data

import android.net.Uri

object AdBlocker {
    
    // Set of common advertising, tracking, malware and popup hosts for modern web adblocking
    private val standardBlockedHosts = setOf(
        "doubleclick.net",
        "googleads.g.doubleclick.net",
        "pagead2.googlesyndication.com",
        "adservice.google.com",
        "pubads.g.doubleclick.net",
        "adclick.g.doubleclick.net",
        "partnerad.l.doubleclick.net",
        "ads.youtube.com",
        "analytics.google.com",
        "google-analytics.com",
        "www.google-analytics.com",
        "ads.google.com",
        "admob.com",
        "fbcdn.net",
        "connect.facebook.net",
        "graph.facebook.com",
        "creative.ak.fbcdn.net",
        "ads.tiktok.com",
        "analytics.tiktok.com",
        "adnxs.com",
        "anonymizer.com",
        "criteo.com",
        "criteo.net",
        "quantserve.com",
        "quantcast.com",
        "scorecardresearch.com",
        "amazon-adsystem.com",
        "adnxs.com",
        "outbrain.com",
        "taboola.com",
        "hotjar.com",
        "mixpanel.com",
        "optimizely.com",
        "adroll.com",
        "adtech.de",
        "adcolony.com",
        "unityads.unity3d.com",
        "applovin.com",
        "vungle.com",
        "mopub.com",
        "adcraft.co",
        "addthis.com",
        "sharethis.com",
        "buysellads.com",
        "popads.net",
        "popcash.net",
        "exoclick.com",
        "yandex.ru/clck",
        "yandex.com/clck",
        "mc.yandex.ru"
    )

    private val strictBlockedHosts = standardBlockedHosts + setOf(
        "disqus.com",
        "gravatar.com",
        "cloudfront.net", // Block suspicious subdomains conditionally or flag
        "stats.g.doubleclick.net",
        "tracking.com",
        "telemetry.com",
        "beacons.gcp.gvt2.com"
    )

    enum class BlockLevel {
        DISABLED,
        STANDARD,
        STRICT
    }

    /**
     * Inspects a target URL to check if it matches blockable resource hosts.
     * Returns true if request should be intercepted/blocked.
     */
    fun shouldBlock(url: String, level: BlockLevel): Boolean {
        if (level == BlockLevel.DISABLED) return false
        
        val uri = try {
            Uri.parse(url)
        } catch (e: Exception) {
            return false
        }
        
        val host = uri.host?.lowercase() ?: return false
        
        val blockList = when (level) {
            BlockLevel.STRICT -> strictBlockedHosts
            else -> standardBlockedHosts
        }
        
        // Exact match or subdomain match checks
        for (blocked in blockList) {
            if (host == blocked || host.endsWith(".$blocked")) {
                return true
            }
        }
        
        // Block common paths / patterns
        val path = uri.path?.lowercase() ?: ""
        if (path.contains("/ads/") || path.contains("/adserver/") || path.contains("/telemetry") || path.contains("/tracking-code")) {
            return true
        }
        
        return false
    }
}
