package com.newswarajya.noswipe.reelshortblocker.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class EmojiBandTest {

    @Test
    fun `verifies emoji band classification across all minute ranges`() {
        // 0 min -> 🛑 Total Lockdown (Monk Mode)
        assertEquals(EmojiBand.MONK_MODE, EmojiBand.forMinutes(0))
        assertEquals("🛑 Total Lockdown (Monk Mode)", EmojiBand.forMinutes(0).formattedDisplay)

        // 1–30 min -> 🚀 Laser Focus
        assertEquals(EmojiBand.LASER_FOCUS, EmojiBand.forMinutes(1))
        assertEquals(EmojiBand.LASER_FOCUS, EmojiBand.forMinutes(15))
        assertEquals(EmojiBand.LASER_FOCUS, EmojiBand.forMinutes(30))
        assertEquals("🚀 Laser Focus", EmojiBand.forMinutes(20).formattedDisplay)

        // 31–120 min -> ⚖️ Healthy Balance
        assertEquals(EmojiBand.HEALTHY_BALANCE, EmojiBand.forMinutes(31))
        assertEquals(EmojiBand.HEALTHY_BALANCE, EmojiBand.forMinutes(60))
        assertEquals(EmojiBand.HEALTHY_BALANCE, EmojiBand.forMinutes(120))
        assertEquals("⚖️ Healthy Balance", EmojiBand.forMinutes(90).formattedDisplay)

        // 121–240 min -> 🕰️ Slipping Away...
        assertEquals(EmojiBand.SLIPPING_AWAY, EmojiBand.forMinutes(121))
        assertEquals(EmojiBand.SLIPPING_AWAY, EmojiBand.forMinutes(180))
        assertEquals(EmojiBand.SLIPPING_AWAY, EmojiBand.forMinutes(240))
        assertEquals("🕰️ Slipping Away...", EmojiBand.forMinutes(200).formattedDisplay)

        // 241+ min -> 💀 Dopamine Overload
        assertEquals(EmojiBand.DOPAMINE_OVERLOAD, EmojiBand.forMinutes(241))
        assertEquals(EmojiBand.DOPAMINE_OVERLOAD, EmojiBand.forMinutes(360))
        assertEquals("💀 Dopamine Overload", EmojiBand.forMinutes(300).formattedDisplay)
    }
}
