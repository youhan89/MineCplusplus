package com.jsogaard.minecplusplus

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.ShulkerBox
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockCookEvent
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta

const val LORE_TAG = "Mold"
//TODO: Make mold unplaceable
/**
 * Crafting table can be dispensed onto a dropper, whos inventory will be used as a recipe pattern.
 * But instead of dropping out the crafted item, it drops out a mold that needs to be cooked in a furnace.
 */
class ItemMoldingAspect(private val plugin: Plugin): Listener {

    init {
        val r = FurnaceRecipe(
            NamespacedKey.fromString("dispensercraft:furnacetest", plugin)!!,
            ItemStack(Material.WHITE_SHULKER_BOX, 1),
            Material.BLACK_SHULKER_BOX,
            0f,
            500
        )

        plugin.server.addRecipe(r)
    }
    @EventHandler
    fun onEvent(event: BlockDispenseEvent) {
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

        val sbStack = shulkerBoxOf(result)
        sbStack.itemMeta = sbStack.itemMeta.also {
            it!!.setDisplayName(LORE_TAG)
            it.lore = listOf(LORE_TAG)
        }

        val temp = dropperInventory.contents
        dropperInventory.contents = arrayOfNulls(9)
        dropperInventory.addItem(sbStack)
        while(!dropperInventory.isEmpty) {
            dropper.drop()
        }
        dropperInventory.contents = temp
    }

    @EventHandler
    fun onEvent(event: BlockCookEvent) {
        if(event.source.type == Material.BLACK_SHULKER_BOX) {
            if(event.source.itemMeta?.lore?.any { it == LORE_TAG } != true) {
                event.isCancelled = true
                return
            }
            val bsm = event.source.itemMeta as BlockStateMeta
            val box = bsm.blockState as ShulkerBox
            val itemInside = box.inventory.contents.first()
            event.result = itemInside
        }
    }

    private fun shulkerBoxOf(itemStack: ItemStack): ItemStack {
        return ItemStack(Material.BLACK_SHULKER_BOX).also { sbStack ->
            val bsm = sbStack.itemMeta as BlockStateMeta
            val box = bsm.blockState as ShulkerBox
            val sbInventory = box.inventory
            sbInventory.addItem(itemStack)
            bsm.blockState = box
            sbStack.itemMeta = bsm
        }
    }
}