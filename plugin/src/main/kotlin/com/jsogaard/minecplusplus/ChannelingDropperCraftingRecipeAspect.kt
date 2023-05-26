package com.jsogaard.minecplusplus

import com.jsogaard.minecplusplus.crafting.setCraftingDropper
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.meta.EnchantmentStorageMeta

class ChannelingDropperCraftingRecipeAspect(private val plugin: CubematicPlugin): Listener {
    init {
        val item = ItemStack(Material.DROPPER, 1).also { stack ->
            stack.itemMeta = stack.itemMeta?.also { meta ->
                meta.addEnchant(Enchantment.CHANNELING,1,true)
                meta.persistentDataContainer.setCraftingDropper(true, plugin)
            }
        }

        val book = ItemStack(Material.ENCHANTED_BOOK, 1)
        book.itemMeta = (book.itemMeta as EnchantmentStorageMeta).also {
            it.addStoredEnchant(Enchantment.CHANNELING, 1, true)
        }

        val recipe = ShapedRecipe(plugin.namespaceKeys.craftingDropper, item).shape("DB")
        recipe.setIngredient('D', Material.DROPPER)
        recipe.setIngredient('B', RecipeChoice.ExactChoice(book))

        plugin.server.addRecipe(recipe)
    }
}