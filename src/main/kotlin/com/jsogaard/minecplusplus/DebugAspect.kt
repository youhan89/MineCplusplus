package com.jsogaard.minecplusplus

import com.jsogaard.minecplusplus.rules.BlockBreaking
import com.jsogaard.minecplusplus.rules.Rules
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class DebugAspect(private val plugin: Plugin): Listener {
    @EventHandler
    fun onEvent(event: PlayerInteractEvent) {
        if(event.clickedBlock != null && event.item?.type == Material.STICK) {
            val type = event.clickedBlock?.type
            val loot = event.clickedBlock?.getDrops(ItemStack(Material.WOODEN_PICKAXE))
            val time = event.clickedBlock?.getDestroySpeed(ItemStack(Material.WOODEN_PICKAXE))

            plugin.server.broadcastMessage("Hardness ${type?.hardness}, requireSpecial: ${event.clickedBlock?.blockData?.requiresCorrectToolForDrops()}, loot: $loot, time: $time")
        }

        if(event.clickedBlock != null && event.item?.type in Rules.ALL_TOOLS) {
            val block = event.clickedBlock!!
            val tool = event.item!!

            val breakTime = BlockBreaking.getBreakTimeTicks(event.item!!, event.clickedBlock!!) / 20 //to seconds
            val requireSpecial = event.clickedBlock?.blockData?.requiresCorrectToolForDrops()
            val isPreferred = event.clickedBlock!!.isPreferredTool(event.item!!)

            val toolMultiplier = block.getDestroySpeed(tool)
            val canHarvest = !block.blockData.requiresCorrectToolForDrops() || block.blockData.isPreferredTool(tool)
            val efficiencyLevel = tool.itemMeta.enchants.firstNotNullOfOrNull {
                if(it.key == Enchantment.DIG_SPEED)
                    it.value
                else null
            } ?: 0
            val blockHardness = block.type.hardness

            plugin.server.broadcastMessage("Breaktime: $breakTime. requireSpecial: $requireSpecial, isPreferred: $isPreferred, canHarvest: $canHarvest, hardness: $blockHardness, toolMultiplier: $toolMultiplier, efficiency: $efficiencyLevel")
        }
    }
}