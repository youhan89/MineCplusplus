package com.jsogaard.minecplusplus

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack


class PluginListener(val plugin: Plugin): Listener {
    @EventHandler
    fun onMoveInventoryEvent(event: InventoryMoveItemEvent) {
        if(event.initiator.type == InventoryType.HOPPER && event.destination.type == InventoryType.DROPPER) {
            val hopper = event.source
            val itemType = event.item.type
            val targetSlot = 5

            val targetStack = event.destination.getItem(targetSlot)
            val newStack = if(targetStack == null || targetStack.amount == 0) {
                event.item
            } else {
                targetStack.combineOrNull(event.item)
            }

            if(newStack != null) {
                event.destination.setItem(targetSlot, newStack)

                //When executing this code, the item is "up in the air" and can't be subtracted
                //from the source...? appears to be overwritten with original stack after cancelling the event.
                //So we tell the server to queue this subtraction asap.
                //TODO Do addition here as well!

                plugin.scheduleRun {
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

operator fun ItemStack.minus(other: Int): ItemStack {
    return ItemStack(this.type, this.amount - other)
}
operator fun ItemStack.plus(other: ItemStack?): ItemStack {
    if(other == null)
        return this

    return ItemStack(
        this.type,
        (this.amount + other.amount).coerceAtMost(this.type.maxStackSize)
    )
}

private fun ItemStack.canStack(other: ItemStack): Boolean {
    return this.type == other.type
            && (this.amount + other.amount) <= this.type.maxStackSize
}

private fun ItemStack.combineOrNull(other: ItemStack): ItemStack? {
    return if(this.canStack(other))
        this + other
    else
        null
}