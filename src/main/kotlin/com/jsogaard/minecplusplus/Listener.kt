package com.jsogaard.minecplusplus

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.LazyMetadataValue
import org.bukkit.metadata.MetadataValue


class PluginListener(private val plugin: Plugin): Listener {
    @EventHandler
    fun onMoveInventoryEvent(event: InventoryMoveItemEvent) {
        if(event.initiator.type == InventoryType.HOPPER && event.destination.type == InventoryType.DROPPER) {
            val hopper = event.source
            val dropper = event.destination
            val itemType = event.item.type

            val dropperBlock = dropper.location?.block?.let {
                if(it.type == Material.DROPPER) {
                    it
                } else null
            }

            val targetSlot = when(val metaData = dropperBlock?.getMetadata("nextSlot")) {
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

            dropperBlock?.setMetadata("nextSlot", FixedMetadataValue(plugin, targetSlot))

            // When executing this code, the item is "up in the air" and can't be subtracted from the source...?
            // it appears to be overwritten with original stack after cancelling the event.
            // So we tell the server to cancel event and queue our custom transaction asap instead.
            plugin.scheduleRun {
                val targetStack = dropper.getItem(targetSlot)
                val newStack = if (targetStack == null || targetStack.amount == 0) {
                    event.item
                } else {
                    targetStack.combineOrNull(event.item)
                }

                if (newStack != null) {
                    dropper.setItem(targetSlot, newStack)

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