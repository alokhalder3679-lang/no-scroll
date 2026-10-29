package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.newswarajya.noswipe.reelshortblocker.data.datasource.PlatformConfigDataSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context and verify 34 platforms catalog`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NoScroll", appName)

        val platforms = PlatformConfigDataSource(context).loadPlatforms()
        assertEquals(34, platforms.size)
        assertTrue(platforms.any { it.id == "youtube_shorts" })
        assertTrue(platforms.any { it.id == "tiktok" })
    }
}
