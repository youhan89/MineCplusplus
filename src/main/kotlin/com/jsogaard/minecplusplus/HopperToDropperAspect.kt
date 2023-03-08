package com.jsogaard.minecplusplus

import org.bukkit.block.Dropper
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.metadata.FixedMetadataValue

class HopperToDropperAspect(private val plugin: Plugin): Listener {
    @EventHandler
    fun onMoveInventoryEvent(event: InventoryMoveItemEvent) {
        if(event.initiator.type == InventoryType.HOPPER && event.destination.type == InventoryType.DROPPER) {
            val hopper = event.source
            val dropperInventory = event.destination
            val itemType = event.item.type

            val dropper = (dropperInventory.holder as Dropper)
            val dropperBlock = dropper.block

            var targetSlot = when(val metaData = dropperBlock.getMetadata("nextSlot")) {
                null -> 0
                else -> {
                    if(metaData.isEmpty()) {
                        0
                    } else {
                        val last = (metaData[0]?.value() as? Int) ?: 0
                        if(last + 1 <= 8) last + 1 else 0
                    }
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

            dropperBlock.setMetadata("nextSlot", FixedMetadataValue(plugin, targetSlot))

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
                    dropperInventory.setItem(targetSlot, newStack)

                    val slot = hopper.first(itemType)
                    val stack = hopper.getItem(slot)!!
                    hopper.clear(slot)
                    hopper.setItem(slot, stack - 1)
                }
            }

            event.isCancelled = true
        }
    }
}