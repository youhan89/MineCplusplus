package com.jsogaard.minecplusplus

import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta

class CheatAspect(private val plugin: CubematicPlugin): Listener {

    class CheatCommandExecutor: CommandExecutor {
        override fun onCommand(
            sender: CommandSender,
            command: Command,
            label: String,
            args: Array<out String>
        ): Boolean {
            return when (command.name) {
                "give-channeling-book" -> {
                    val player = sender as? Player
                        ?: return false

                    val channelingBook = ItemStack(Material.ENCHANTED_BOOK, 1)
                    channelingBook.itemMeta = (channelingBook.itemMeta as EnchantmentStorageMeta).also {
                        it.addStoredEnchant(Enchantment.CHANNELING, 1, true)
                    }

                    player.inventory.addItem(channelingBook)
                    true
                }
                else -> false
            }
        }
    }
    init {
        plugin.getCommand("give-channeling-book")?.setExecutor(CheatCommandExecutor());
    }
}