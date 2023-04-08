package com.jsogaard.minecplusplus

import org.bukkit.Material
import org.bukkit.block.Dispenser
import org.bukkit.block.data.Directional

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.inventory.ItemStack

/**
 * Crafts an item from a dispenser when a piston holding a crafting table is pulled from it
 * Schematic:
 *
 * [ STICKY PISTON ]
 * [ CRAFTINGTABLE ]
 * [  pushed onto  ]
 * [ DISPENSER ]
 *
 * crafts when retracted
 */
class DispenserPistonCraftAspect(val plugin: Plugin): Listener {
    @EventHandler
    fun onEvent(event: BlockPistonRetractEvent) {
        if(event.blocks.size == 0) {
            plugin.server.broadcastMessage("No blocks")
        }
        val lastBlock = event.blocks.last()

        if(lastBlock.type == Material.CRAFTING_TABLE) {
            val dispenserBlock = lastBlock
                .getRelative(event.direction.oppositeFace).let {

                if(it.type == Material.DISPENSER)
                    it
                else {
                    plugin.server.broadcastMessage("Dispenser not found...")
                    return
                }
            }

            val dispenserFacing = (dispenserBlock.blockData as? Directional)?.facing ?: kotlin.run {
                plugin.server.broadcastMessage("No facing...")
                return
            }

            val dispenser = dispenserBlock.state as Dispenser
            val recipe = plugin.server.getCraftingRecipe(dispenser.inventory.contents as Array<out ItemStack>, event.block.world)
            val result = recipe?.result ?: run {
                plugin.server.broadcastMessage("No recipe found for crafting matrix...")
                return
            }

            val facingCenter = dispenserBlock.getRelative(dispenserFacing).location.add(0.5, 0.5, 0.5)
            val dispenserCenter = dispenserBlock.location.add(0.5, 0.5, 0.5)
            val midPoint = facingCenter.toVector().midpoint(dispenserCenter.toVector())
            val spawnPoint = midPoint.toLocation(event.block.world)

            plugin.scheduleRun(2) {
                val item = event.block.world.dropItem(spawnPoint, result)
                item.velocity = dispenserFacing.direction.multiply(0.2f)
            }
        }
    }
}