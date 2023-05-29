package com.jsogaard.minecplusplus.portals

import com.jsogaard.minecplusplus.CubematicPlugin
import com.jsogaard.minecplusplus.facingBlock
import com.jsogaard.minecplusplus.toDispenser
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Container
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.meta.CompassMeta
import java.util.logging.Logger


class PortalTestAspect(private val plugin: CubematicPlugin): Listener {
    private val debug = true
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

    @EventHandler(ignoreCancelled = true)
    fun onEvent(e: PlayerPortalEvent) {
        if(e.cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            val playerLocation = e.player.location

            val netherPortalStart = if(playerLocation.block.type == Material.NETHER_PORTAL) {
                playerLocation.block
            } else {
                val list = (-1..1).flatMap {
                    flatNeighbors.map { playerLocation.block.getRelative(it) }
                }

                list.firstOrNull { it.type == Material.NETHER_PORTAL }
            }

            if(netherPortalStart == null)
                return

            val portalBlocks = getConnectedblocks(netherPortalStart)

            val compass = findPortalCompass(portalBlocks)
                ?: return

            processPortal(e, compass, portalBlocks)
        }
    }

    private fun processPortal(e: PlayerPortalEvent, compass: CompassMeta, portalBlocks: Set<Block>) {
        e.isCancelled = true
        val dest = compass.lodestone
            ?: return

        e.player.teleport(dest.add(0.5,1.0,0.5))

        portalBlocks.forEach { it.type = Material.AIR }
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

    fun getConnectedblocks(start: Block): Set<Block> {
        val results = mutableSetOf<Block>()
        val checked = mutableSetOf<Block>()
        val todo = mutableSetOf<Block>()
        val targetType = start.type

        todo.add(start)
        results.add(start)

        do {
            val newBlocks = todo.flatMap { current ->
                if(checked.contains(current)) return@flatMap listOf()

                adjacentFaces.mapNotNull { face ->
                    val candidate = current.getRelative(face)
                    checked.add(candidate)

                    when(candidate.type) {
                        targetType -> candidate
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