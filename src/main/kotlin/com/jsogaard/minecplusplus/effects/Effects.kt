package com.jsogaard.minecplusplus.effects

import org.bukkit.Location
import org.bukkit.Particle

object Effects {
    fun fizzle(from: Location, to: Location) {
        val x = from.x + 0.5
        val y = from.y + 0.5
        val z = from.z + 0.5
        val ox = to.x + 0.5
        val oy = to.y + 0.5
        val oz = to.z + 0.5

        val newLocation = Location(
            from.world,
            x + (ox - x) / 2,
            y + (oy - y) / 2,
            z + (oz - z) / 2
        )

        from.world?.spawnParticle(Particle.SMOKE_NORMAL, newLocation, 16, 0.1, 0.1, 0.1, 0.01)

    }
}