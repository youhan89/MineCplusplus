package com.jsogaard.minecplusplus

import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Dropper
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * experimental "modify dropper" insert order by putting a soul campfire directly underneath it
 *
 *    (hopper)
 *   (dropper)
 * (soul campfire)
 */
class SmokedDropperAspect(private val plugin: Plugin): Listener {
    @EventHandler
    fun onMoveInventoryEvent(event: InventoryMoveItemEvent) {
        if(event.destination.type == InventoryType.DROPPER) {
            val source = event.source
            val dropperInventory = event.destination
            val dropper = (dropperInventory.holder as Dropper)

            if(!isSmoked(dropper)) {
                return
            } else {
                //Cancel the native move, this returns the item to the source.
                event.isCancelled = true
            }

            var targetSlot = when(val last = dropper.getDropSlot(plugin)) {
                null -> 0
                else -> {
                    if(last.coerceIn(0,8) + 1 <= 8)
                        last + 1
                    else 0
                }
            }

            var triesLeft = 9
            var canStack = false
            while(triesLeft > 0) {
                val slot = dropperInventory.contents[targetSlot]
                if (slot.canStackWith(event.item)) {
                    canStack = true
                    break
                } else {
                    triesLeft--
                    targetSlot = if(targetSlot == 8) 0 else targetSlot + 1
                }
            }

            if(!canStack) {
                //Can't place item - dropper full
                return
            }

            dropper.setDropSlot(plugin, targetSlot.toByte())

            //Make sure the target can receive the item
            if(incrementedStackOrNull(dropperInventory, targetSlot, event.item.type) != null) {
                // When executing this code, the item is "up in the air" and can't be subtracted from the source...?
                // it appears to be overwritten with original stack after cancelling the event.
                // So we tell the server to cancel event and queue our custom transaction asap instead.
                plugin.scheduleRun {
                    transferOne(source, dropperInventory, event.item.type, targetSlot)
                }
            }
        }
    }

    private fun isSmoked(dropper: Dropper): Boolean {
        val below = dropper.block.getRelative(BlockFace.DOWN)
        return below.type == Material.SOUL_CAMPFIRE
                || below.type == Material.SOUL_TORCH
                || below.type == Material.SOUL_WALL_TORCH
    }

    private fun incrementedStackOrNull(target: Inventory, targetSlot: Int, type: Material): ItemStack? {
        val transferStack = ItemStack(type, 1)
        val targetStack = target.getItem(targetSlot)
        return if (targetStack == null || targetStack.amount == 0) {
            transferStack
        } else {
            targetStack.stackWithOrNull(transferStack)
        }
    }
    private fun transferOne(source: Inventory, target: Inventory, type: Material, targetSlot: Int) {
        val finalStack = incrementedStackOrNull(target, targetSlot, type)

        if (finalStack != null) {
            val slot = source.contents.indexOfFirst { itemStack ->
                ItemStack(type, 1).isSimilar(itemStack)
            }

            if(slot != -1) {
                val stack = source.getItem(slot)!!

                source.clear(slot)
                source.setItem(slot, stack - 1)

                target.setItem(targetSlot, finalStack)
            }
        }
    }

}