package com.jsogaard.minecplusplus

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDispenseEvent

val fillerItems = setOf(
    Material.BLUE_STAINED_GLASS,
    Material.BLACK_STAINED_GLASS_PANE,
    Material.BROWN_STAINED_GLASS_PANE,
    Material.CYAN_STAINED_GLASS_PANE,
    Material.GREEN_STAINED_GLASS_PANE,
    Material.BROWN_STAINED_GLASS_PANE,
    Material.LIGHT_BLUE_STAINED_GLASS_PANE,
    Material.LIGHT_GRAY_STAINED_GLASS_PANE,
    Material.LIME_STAINED_GLASS_PANE,
    Material.MAGENTA_STAINED_GLASS_PANE,
    Material.ORANGE_STAINED_GLASS_PANE,
    Material.PINK_STAINED_GLASS_PANE,
    Material.RED_STAINED_GLASS_PANE,
    Material.PURPLE_STAINED_GLASS_PANE,
    Material.WHITE_STAINED_GLASS_PANE,
    Material.YELLOW_STAINED_GLASS_PANE,
)

class DispenseCraftingTableAspect(private val plugin: Plugin): Listener {
    @EventHandler
    fun onEvent(event: BlockDispenseEvent) {
        plugin.server.broadcastMessage("Dispense a ${event.item.type}")

        if(event.item.type != Material.CRAFTING_TABLE)
            return

        val dispenser = event.block.toDispenser()
        val facingBlock = dispenser.facingBlock()

        if(facingBlock.type != Material.DROPPER)
            return

        val dropper = facingBlock.toDropper()
        val dropperInventory = dropper.inventory

        event.isCancelled = true

        val pattern = dropperInventory.contents.map { item ->
            when {
                item == null -> null
                fillerItems.contains(item.type)  -> null
                else -> item
            }
        }

        val recipe = plugin.server.getCraftingRecipe(pattern.toTypedArray(), event.block.world)
        val result = recipe?.result ?: run {
            plugin.server.broadcastMessage("No recipe found for crafting matrix...")
            return
        }

        val removedRecipeInventoryMap = dropperInventory.contents.map {
            when {
                it == null -> null
                fillerItems.contains(it.type) -> it
                it.amount > 1 -> {
                    it.amount--
                    it
                }
                else -> null
            }
        }

        dropperInventory.contents = removedRecipeInventoryMap.toTypedArray()

        val temp = dropperInventory.contents
        dropperInventory.contents = arrayOfNulls(9)
        dropperInventory.addItem(result)
        while(!dropperInventory.isEmpty) {
            dropper.drop()
        }
        dropperInventory.contents = temp
    }
}

