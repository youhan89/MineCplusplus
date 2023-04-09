package com.jsogaard.minecplusplus.effects

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.inventory.ItemStack
import kotlin.math.floor

object ParticleFX {
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

    fun crackAllSides(location: Location, material: Material) {
        val item = ItemStack(material)
        location.allFaceCenters().forEach {
            crack(it, item)
        }
    }
    private fun crack(location: Location, material: ItemStack) {
        location.world.spawnParticle(Particle.ITEM_CRACK, location, 2, 0.25, 0.25, 0.25, 0.05, material);
    }

    fun blockBroken(location: Location, material: ItemStack) {
        location.world.spawnParticle(Particle.ITEM_CRACK, location.toCenterLocation(), 32, 0.2, 0.2, 0.2, 0.1, material);
    }
}

private fun Location.allFaceCenters(): List<Location> {
    val x = floor(this.x)
    val y = floor(this.y)
    val z = floor(this.z)

    return listOf(
        this.clone().set(x + 0.5, y + 0.5, z), //Front
        this.clone().set(x, y + 0.5, z + 0.5), //Left
        this.clone().set(x + 1, y + 0.5, z + 0.5), //Right
        this.clone().set(x + 0.5, y + 0.5, z + 1), //Back
        this.clone().set(x + 0.5, y + 1, z + 0.5), //Top
        this.clone().set(x + 0.5, y, z + 0.5), //Bottom
    )
}