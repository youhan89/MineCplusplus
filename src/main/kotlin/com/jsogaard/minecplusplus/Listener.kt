package com.jsogaard.minecplusplus

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack


class PluginListener: Listener {
    @EventHandler
    fun onMoveInventoryEvent(event: InventoryMoveItemEvent) {
        if(event.initiator.type == InventoryType.HOPPER && event.destination.type == InventoryType.DROPPER) {
            event.item = ItemStack(Material.DIAMOND, 1)
        }
    }
}