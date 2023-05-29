package com.jsogaard.minecplusplus.portals

import com.jsogaard.minecplusplus.CubematicPlugin
import com.jsogaard.minecplusplus.facingBlock
import com.jsogaard.minecplusplus.toDispenser
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.entity.EntityPortalEnterEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.meta.CompassMeta
import se.ade.mc.cubematic.getCenter
import java.util.logging.Logger

private const val FOOD_BURN = 4

private val adjacentFaces = arrayOf(
    BlockFace.DOWN,
    BlockFace.UP,
    BlockFace.NORTH,
    BlockFace.EAST,
    BlockFace.SOUTH,
    BlockFace.WEST
)

private val flatNeighbors = arrayOf(
    BlockFace.NORTH,
    BlockFace.EAST,
    BlockFace.SOUTH,
    BlockFace.WEST,
    BlockFace.NORTH_EAST,
    BlockFace.NORTH_WEST,
    BlockFace.SOUTH_EAST,
    BlockFace.SOUTH_WEST,
)

private const val IMMEDIATE_MODE = false

class PortalTestAspect(private val plugin: CubematicPlugin): Listener {
    private val debug = true

    @EventHandler
    fun onEvent(e: EntityPortalEnterEvent) {
        if(IMMEDIATE_MODE && e.entity is Player && e.location.block.type == Material.NETHER_PORTAL) {
            onPortalActivated(e.location, e.entity as Player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onEvent(e: PlayerPortalEvent) {
        if(!IMMEDIATE_MODE && e.cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            e.isCancelled = onPortalActivated(e.player.location, e.player)
        }
    }

    private fun onPortalActivated(location: Location, player: Player): Boolean {
        val netherPortalStart = if(location.block.type == Material.NETHER_PORTAL) {
            location.block
        } else {
            location.block.getNeighborsCubic(1)
                .filter { it.type == Material.NETHER_PORTAL }
                .map { it.location.getCenter() }
                .nearestOrNull(location)
                ?.block
        }

        if(netherPortalStart == null)
            return false

        val portalBlocks = netherPortalStart.findAllChaining(Material.NETHER_PORTAL)

        val compass = findPortalCompass(portalBlocks)
            ?: return false

        val dest = compass.lodestone?.clone()?.add(0.5, 1.0, 0.5)
            ?: return false

        if(player.foodLevel >= FOOD_BURN + 1) {
            player.foodLevel -= FOOD_BURN

            plugin.scheduleRun {
                player.teleport(dest, PlayerTeleportEvent.TeleportCause.PLUGIN)
            }
        } else {
            player.playSound(player, Sound.ENTITY_PLAYER_BURP, 1f, 1f)
        }

        portalBlocks.forEach { it.type = Material.AIR }
        return true
    }

    private fun findPortalCompass(portalBlocks: Set<Block>): CompassMeta? {
        val portalCreatorCandidates = portalBlocks.flatMap { block ->
            adjacentFaces.map { face ->
                val rel = block.getRelative(face)
                if(rel.type == Material.DISPENSER || rel.type == Material.DROPPER) {
                    rel
                } else {
                    null
                }
            }
        }.filterNotNull()

        val connectedDispensers = portalCreatorCandidates.flatMap { block ->
            adjacentFaces.map { face ->
                val rel = block.getRelative(face)
                val facing = rel.facingBlock()

                if(rel.type == Material.DISPENSER
                    && rel in portalCreatorCandidates
                    && (facing?.type == Material.DROPPER || facing?.type == Material.DISPENSER)) {
                        rel
                } else {
                    null
                }
            }
        }.filterNotNull()

        val allToSearch = (portalCreatorCandidates + connectedDispensers).toSet()

        allToSearch.forEach { block ->
            adjacentFaces.forEach { face ->
                val rel = block.getRelative(face)
                if(rel.type == Material.DISPENSER) {
                    return rel.toDispenser().inventory.filterNotNull().filter {
                        it.type == Material.COMPASS && (it.itemMeta as CompassMeta).isLodestoneTracked
                    }.randomOrNull()?.itemMeta as? CompassMeta
                }
            }
        }

        return null
    }


    @EventHandler
    fun onEvent(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK)
            return

        val item = event.item ?: return
        val block = event.clickedBlock ?: return

        if(item.type == Material.GOLD_NUGGET && block.type == Material.GOLD_BLOCK) {
            block.getRelative(BlockFace.UP).type = Material.NETHER_PORTAL
        }

        if(item.type == Material.COMPASS && block.type == Material.LODESTONE) {
            plugin.scheduleRun {
                plugin.server.broadcastMessage(item.itemMeta.toString())
            }
        }
    }

    @EventHandler
    fun onEvent(event: BlockDispenseEvent) {
        val logger: Logger? = if(debug) plugin.logger else null

        if (event.block.type != Material.DISPENSER || event.item.type != Material.COMPASS)
            return

        val compass = event.item.itemMeta as CompassMeta
        if(!compass.isLodestoneTracked)
            return

        event.isCancelled = true

        val dispenser = event.block.toDispenser()
        val dispenserFacing = dispenser.facingBlock()

        if(dispenserFacing.type != Material.DROPPER && dispenserFacing.type != Material.DISPENSER) {
            return
        }

        val fuelContainer = dispenserFacing.state as? Container
            ?: return

        val targetBlock = fuelContainer.block.facingBlock()
            ?: return

        if(targetBlock.type == Material.NETHER_PORTAL)
            return

        if(targetBlock.type != Material.AIR) {
            logger?.info("Target block not AIR")
            return
        }

        val fruitIndex = fuelContainer.inventory.indexOfFirst { it.type == Material.POPPED_CHORUS_FRUIT }

        if(fruitIndex == -1) {
            return
        }

        val fuelStack = fuelContainer.inventory.getItem(fruitIndex)
            ?: return

        val newFuelStack = when(val amount = fuelStack.amount) {
            1 -> null
            else -> fuelStack.clone().also { it.amount = amount - 1 }
        }

        fuelContainer.inventory.setItem(fruitIndex, newFuelStack)
        targetBlock.type = Material.NETHER_PORTAL
    }

}

private fun List<Location>.nearestOrNull(location: Location): Location? {
    return if(this.isEmpty())
        null
    else
        this.minBy { location.distance(it) }
}

fun Block.getNeighborsCubic(distance: Int): List<Block> {
    val blocks = mutableListOf<Block>()
    (-distance..distance).forEach { x ->
        (-distance..distance).forEach { y ->
            (-distance..distance).forEach { z ->
                if(x != 0 || y != 0 || z != 0)
                    blocks.add(this.getRelative(x,y,z))
            }
        }
    }

    return blocks
}

fun Block.findAllChaining(type: Material): Set<Block> {
    val results = mutableSetOf<Block>()
    val checked = mutableSetOf<Block>()
    val todo = mutableSetOf<Block>()
    todo.add(this)
    results.add(this)
    do {
        val newBlocks = todo.flatMap { current ->
            if (checked.contains(current)) return@flatMap listOf<Block>()

            adjacentFaces.mapNotNull { face ->
                val candidate = current.getRelative(face)
                checked.add(candidate)

                when (candidate.type) {
                    type -> candidate
                    else -> null
                }
            }
        }

        todo.clear()
        results.addAll(newBlocks)
        todo.addAll(newBlocks)

    } while (todo.isNotEmpty())
    return results
}
