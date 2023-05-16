package com.jsogaard.minecplusplus.crafting

import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.inventory.ItemStack

private val honeyBottlesToHoneyBlock = (1..4).map { Material.HONEY_BOTTLE }

class CraftingResolver(private val server: Server) {
    fun craft(input: List<ItemStack?>, world: World): List<ItemStack>? {
        val result = server.getCraftingRecipe(input.toTypedArray() as Array<out ItemStack>, world)?.result
            ?: return null

        return when (countMaterials(input)) {
            honeyBottlesToHoneyBlock -> return listOf(ItemStack(Material.HONEY_BLOCK), ItemStack(Material.GLASS_BOTTLE, 4))
            else -> listOf(result)
        }
    }

    private fun countMaterials(input: List<ItemStack?>): List<Material> {
        return input.mapNotNull {
            when(it?.type) {
                null, Material.AIR -> null
                else -> it.type
            }
        }
    }
}