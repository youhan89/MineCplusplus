package com.jsogaard.minecplusplus.placing

import com.jsogaard.minecplusplus.Plugin
import com.jsogaard.minecplusplus.facingBlock
import com.jsogaard.minecplusplus.rules.Rules
import com.jsogaard.minecplusplus.toDispenser
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Dispenser
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDispenseEvent

class PlacingAspect(private val plugin: Plugin) : Listener {

    @EventHandler
    fun onEvent(event: BlockDispenseEvent) {
        val item = event.item

        if (item.type.isBlock) {
            val dispenser = event.block.toDispenser()

            if(!isSmoked(dispenser))
                return

            val targetBlock = event.block.facingBlock()
                ?: return

            val allowed = targetBlock.canPlace(item.type.createBlockData())
            val unoccupied = targetBlock.type in Rules.CAN_PLACE_BLOCK_IN

            val noEntities = !item.type.isSolid ||
                event.block.world.getNearbyEntities(targetBlock.location.add(0.5, 0.5, 0.5), 0.5, 0.5, 0.5)
                    .filterIsInstance<LivingEntity>()
                    .isEmpty()

            if (allowed && unoccupied && noEntities) {
                targetBlock.type = item.type
                event.isCancelled = true

                val itemUpdated = event.item

                plugin.scheduleRun {
                    dispenser.inventory.removeItem(itemUpdated)
                }
            }
        }
    }

    private fun isSmoked(dispenser: Dispenser): Boolean {
        val below = dispenser.block.getRelative(BlockFace.DOWN)
        return below.type == Material.SOUL_CAMPFIRE
                || below.type == Material.SOUL_TORCH
                || below.type == Material.SOUL_WALL_TORCH
    }
}