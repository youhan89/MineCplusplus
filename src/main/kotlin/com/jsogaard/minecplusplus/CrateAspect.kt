package com.jsogaard.minecplusplus

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.inventory.ItemStack

/**
 * unfinished
 */
class CreateAspect(private val plugin: Plugin): Listener {
    @EventHandler
    fun onEvent(event: BlockDispenseEvent) {
        //plugin.server.broadcastMessage("Dispense a ${event.item.type}")

        if(event.item.type != Material.CRAFTING_TABLE)
            return

        val dispenser = event.block.toDispenser()
        val facingBlock = dispenser.facingBlock()

        if(facingBlock.type != Material.DROPPER)
            return

        val dropper = facingBlock.toDropper()
        val dropperInventory = dropper.inventory

        event.isCancelled = true

        //For now, set any dropper dispensed to with a crafting table as a Channeling Dropper
        dropper.setCraftingDropper(true, plugin)

        val pattern = dropperInventory.contents.map { item ->
            when {
                item == null -> null
                fillerItems.contains(item.type)  -> null
                else -> item
            }
        }

        val recipe = plugin.server.getCraftingRecipe(pattern.toTypedArray() as Array<out ItemStack>?, event.block.world)
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
        dropperInventory.addItem(createOf(result))
        while(!dropperInventory.isEmpty) {
            dropper.drop()
        }
        dropperInventory.contents = temp
    }

    private fun createOf(result: ItemStack): ItemStack {
        return ItemStack(Material.STRIPPED_OAK_WOOD, 1).also {
            it.itemMeta = it.itemMeta.also { meta ->
                meta!!
                meta.setDisplayName("Crate")
                meta.lore = listOf(
                    result.type.key.toString(),
                    result.amount.toString()
                )
            }
        }
    }
}