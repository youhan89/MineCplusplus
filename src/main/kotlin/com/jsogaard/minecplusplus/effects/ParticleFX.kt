package com.jsogaard.minecplusplus.effects

import com.jsogaard.minecplusplus.CubematicPlugin
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
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

    fun convertToPlacerBlock(location: Location) {
        location.allFaceCenters().forEach {
            location.world?.spawnParticle(Particle.END_ROD, it, 16, 0.25, 0.25, 0.25, 0.025)
        }
    }

    fun convertToSequential(location: Location, blockFace: BlockFace, plugin: CubematicPlugin) {
        val r1 = faceOrigin(blockFace).add(faceUp(blockFace).multiply(0.75))
        val r2 = faceOrigin(blockFace).add(faceUp(blockFace).multiply(0.5))
        val r3 = faceOrigin(blockFace).add(faceUp(blockFace).multiply(0.25))

        val m2 = listOf(
            r1.clone().add(faceRight(blockFace).multiply(0.25)),
            r1.clone().add(faceRight(blockFace).multiply(0.5)),
            r1.clone().add(faceRight(blockFace).multiply(0.75)),
            r2.clone().add(faceRight(blockFace).multiply(0.25)),
            r2.clone().add(faceRight(blockFace).multiply(0.5)),
            r2.clone().add(faceRight(blockFace).multiply(0.75)),
            r3.clone().add(faceRight(blockFace).multiply(0.25)),
            r3.clone().add(faceRight(blockFace).multiply(0.5)),
            r3.clone().add(faceRight(blockFace).multiply(0.75)),
        )

        var delay = 2L
        m2.forEach {
            plugin.scheduleRun(delay) {
                val local = it.toLocation(location.world).add(location).add(0.0, 0.0, 0.0)
                location.world?.spawnParticle(Particle.DRIPPING_DRIPSTONE_LAVA, local, 1, 0.0, 0.0, 0.0, 0.0)
            }
            delay += 3
        }
    }
}

private fun faceOrigin(blockFace: BlockFace): Vector = when(blockFace) {
    BlockFace.NORTH -> Vector(1.0, 0.0, -0.1)
    BlockFace.SOUTH -> Vector(0.0, 0.0, 1.1)
    BlockFace.WEST -> Vector(-0.1, 0.0, 0.0)
    BlockFace.EAST -> Vector(1.1, 0.0, 1.0)
    BlockFace.UP -> Vector(0.0, 1.1, 1.0)
    else -> Vector(0.0, -0.2, 1.0)
}
private fun faceUp(blockFace: BlockFace): Vector = when(blockFace) {
    BlockFace.NORTH,
    BlockFace.EAST,
    BlockFace.WEST,
    BlockFace.SOUTH -> Vector(0.0, 1.0, 0.0)
    else -> Vector(0.0, 0.0, -1.0)
}

private fun faceRight(blockFace: BlockFace): Vector = when(blockFace) {
    BlockFace.NORTH -> Vector(-1.0, 0.0, 0.0)
    BlockFace.SOUTH -> Vector(1.0, 0.0, 0.0)
    BlockFace.EAST -> Vector(0.0, 0.0, -1.0)
    BlockFace.WEST -> Vector(0.0, 0.0, 1.0)
    else -> Vector(1.0, 0.0, 0.0)
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