package se.ade.mc.cubematic

import org.bukkit.Location

fun Location.getCenter(): Location {
    val centerLoc = clone()
    centerLoc.x = blockX + 0.5
    centerLoc.y = blockY + 0.5
    centerLoc.z = blockZ + 0.5
    return centerLoc
}

fun Location.setCompat(x: Double, y: Double, z: Double) = this.apply {
    this.x = x
    this.y = y
    this.z = z
}