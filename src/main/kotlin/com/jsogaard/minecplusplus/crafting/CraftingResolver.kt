package com.jsogaard.minecplusplus.crafting

import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.inventory.ItemStack

class CraftingResolver(private val server: Server) {
    fun craft(input: List<ItemStack?>, world: World): List<ItemStack>? {
        val result = server.getCraftingRecipe(input.toTypedArray() as Array<out ItemStack>, world)?.result
            ?: return null

        val remainingMaterials = filterMaterials(input).mapNotNull {
            if (it.isItem) it.craftingRemainingItem
            else null
        }.groupingBy { it }
            .eachCount()
            .map { ItemStack(it.key, it.value) }

        return listOf(result) + remainingMaterials
    }

    private fun filterMaterials(input: List<ItemStack?>): List<Material> {
        return input.mapNotNull {
            when(it?.type) {
                null, Material.AIR -> null
                else -> it.type
            }
        }
    }
}