package com.newswarajya.noswipe.reelshortblocker.data.datasource

import android.content.Context
import com.example.R
import com.newswarajya.noswipe.reelshortblocker.domain.model.BlockMode
import com.newswarajya.noswipe.reelshortblocker.domain.model.DetectorType
import com.newswarajya.noswipe.reelshortblocker.domain.model.Platform
import com.newswarajya.noswipe.reelshortblocker.domain.model.PlatformDetector
import org.json.JSONArray

data class MindfulQuote(
    val quote: String,
    val author: String,
    val category: String
)

/**
 * Loads the 34 monitored short-video platforms and view detectors from res/raw/platforms_config.json
 * and motivational quotes from res/raw/mindful_timer_quotes.json.
 */
class PlatformConfigDataSource(private val context: Context) {

    fun loadPlatforms(): List<Platform> {
        return try {
            val jsonText = context.resources
                .openRawResource(R.raw.platforms_config)
                .bufferedReader()
                .use { it.readText() }
            parsePlatformsJson(jsonText)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loadMindfulQuotes(): List<MindfulQuote> {
        return try {
            val jsonText = context.resources
                .openRawResource(R.raw.mindful_timer_quotes)
                .bufferedReader()
                .use { it.readText() }
            val array = JSONArray(jsonText)
            val quotes = mutableListOf<MindfulQuote>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                quotes.add(
                    MindfulQuote(
                        quote = obj.optString("quote"),
                        author = obj.optString("author"),
                        category = obj.optString("category", "Mindfulness")
                    )
                )
            }
            quotes
        } catch (e: Exception) {
            listOf(
                MindfulQuote(
                    quote = "Your attention is the most precious currency you possess.",
                    author = "NoScroll Philosophy",
                    category = "Deep Focus"
                )
            )
        }
    }

    companion object {
        fun parsePlatformsJson(jsonText: String): List<Platform> {
            val jsonArray = JSONArray(jsonText)
            val result = mutableListOf<Platform>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val id = item.getString("id")
                val name = item.getString("name")
                val category = item.optString("category", "Short Video")
                val packageName = item.getString("packageName")
                val defaultMode = BlockMode.fromName(item.optString("defaultBlockMode", "PRESS_BACK"))
                val enabledByDefault = item.optBoolean("enabledByDefault", true)

                val detectorsJson = item.optJSONArray("detectors") ?: JSONArray()
                val detectors = mutableListOf<PlatformDetector>()
                for (j in 0 until detectorsJson.length()) {
                    val dObj = detectorsJson.getJSONObject(j)
                    detectors.add(
                        PlatformDetector(
                            type = DetectorType.fromName(dObj.optString("type", "FINDBYID")),
                            value = dObj.optString("value", "")
                        )
                    )
                }

                result.add(
                    Platform(
                        id = id,
                        name = name,
                        category = category,
                        packageName = packageName,
                        defaultBlockMode = defaultMode,
                        currentBlockMode = defaultMode,
                        isEnabled = enabledByDefault,
                        detectors = detectors
                    )
                )
            }
            return result
        }
    }
}
