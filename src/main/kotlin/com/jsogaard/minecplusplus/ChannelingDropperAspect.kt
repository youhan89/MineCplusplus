package com.jsogaard.minecplusplus

import org.bukkit.block.Dropper
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class ChannelingDropperAspect(private val plugin: Plugin): Listener {
    @EventHandler
    fun onMoveInventoryEvent(event: InventoryMoveItemEvent) {
        if(event.initiator.type == InventoryType.HOPPER && event.destination.type == InventoryType.DROPPER) {
            val hopper = event.source
            val dropperInventory = event.destination
            val itemType = event.item.type

            val dropper = (dropperInventory.holder as Dropper)

            if(!dropper.persistentDataContainer.isCraftingDropper(plugin)) {
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

            // When executing this code, the item is "up in the air" and can't be subtracted from the source...?
            // it appears to be overwritten with original stack after cancelling the event.
            // So we tell the server to cancel event and queue our custom transaction asap instead.
            plugin.scheduleRun {
                val targetStack = dropperInventory.getItem(targetSlot)
                val newStack = if (targetStack == null || targetStack.amount == 0) {
                    event.item
                } else {
                    targetStack.stackWithOrNull(event.item)
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

    @EventHandler
    fun onEvent(event: BlockDropItemEvent) {
        (event.blockState as? Dropper)?.let {
            if(it.isCraftingDropper(plugin)) {
                val stack = event.items[0].itemStack
                stack.itemMeta = stack.itemMeta?.also { meta ->
                    meta.addEnchant(Enchantment.CHANNELING,1,true)
                    meta.persistentDataContainer.setCraftingDropper(true, plugin)
                }
            }
        }
    }

    @EventHandler
    fun onEvent(event: BlockPlaceEvent) {
        val meta = event.itemInHand.itemMeta
            ?: return

        if(meta.persistentDataContainer.isCraftingDropper(plugin) || meta.hasEnchant(Enchantment.CHANNELING)) {
            (event.blockPlaced.state as? Dropper)?.setCraftingDropper(true, plugin)
        }
    }
}

fun Dropper.getDropSlot(plugin: Plugin) = this.persistentDataContainer.getDropSlot(plugin)
fun Dropper.setDropSlot(plugin: Plugin, slot: Byte) {
    this.persistentDataContainer.setDropSlot(plugin, slot)
    this.update()
}
fun PersistentDataContainer.setDropSlot(plugin: Plugin, slot: Byte) {
    set(plugin.namespaceKeys.dropSlot, PersistentDataType.BYTE, slot)
}

fun PersistentDataContainer.getDropSlot(plugin: Plugin): Byte? {
    return get(plugin.namespaceKeys.dropSlot, PersistentDataType.BYTE)
}

fun Dropper.isCraftingDropper(plugin: Plugin): Boolean = this.persistentDataContainer.isCraftingDropper(plugin)
fun Dropper.setCraftingDropper(isCraftingDropper: Boolean, plugin: Plugin) {
    this.persistentDataContainer.setCraftingDropper(isCraftingDropper, plugin)
    this.update()
}

fun PersistentDataContainer.setCraftingDropper(isCraftingDropper: Boolean, plugin: Plugin) {
    if(isCraftingDropper) {
        set(plugin.namespaceKeys.craftingDropper, PersistentDataType.BYTE, 1)
    } else {
        this.remove(plugin.namespaceKeys.craftingDropper)
    }
}

fun PersistentDataContainer.isCraftingDropper(plugin: Plugin): Boolean {
    val data = this.get(plugin.namespaceKeys.craftingDropper, PersistentDataType.BYTE)
    return data == 1.toByte()
}