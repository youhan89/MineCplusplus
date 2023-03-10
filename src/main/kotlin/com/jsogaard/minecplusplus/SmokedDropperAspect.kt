package com.jsogaard.minecplusplus

import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Dropper
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryType

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
                if (slot.canStack(event.item)) {
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

            // When executing this code, the item is "up in the air" and can't be subtracted from the source...?
            // it appears to be overwritten with original stack after cancelling the event.
            // So we tell the server to cancel event and queue our custom transaction asap instead.
            plugin.scheduleRun {
                val targetStack = dropperInventory.getItem(targetSlot)
                val newStack = if (targetStack == null || targetStack.amount == 0) {
                    event.item
                } else {
                    targetStack.combineOrNull(event.item)
                }

                if (newStack != null) {
                    val slot = source.contents.indexOfFirst { itemStack ->
                        event.item.isSimilar(itemStack)
                    }

                    if(slot != -1) {
                        val stack = source.getItem(slot)!!
                        source.clear(slot)
                        source.setItem(slot, stack - 1)

                        dropperInventory.setItem(targetSlot, newStack)
                    }
                }
            }

            event.isCancelled = true
        }
    }

    private fun isSmoked(dropper: Dropper): Boolean {
        return dropper.block.getRelative(BlockFace.DOWN).type == Material.SOUL_CAMPFIRE
    }

}