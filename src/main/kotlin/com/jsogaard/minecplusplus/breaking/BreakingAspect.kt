package com.jsogaard.minecplusplus.breaking

import com.jsogaard.minecplusplus.Plugin
import com.jsogaard.minecplusplus.effects.Effects
import com.jsogaard.minecplusplus.facingBlock
import com.jsogaard.minecplusplus.rules.BlockBreaking
import com.jsogaard.minecplusplus.rules.Rules
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class BreakingAspect(private val plugin: Plugin): Listener {
    private val debug = false

    @EventHandler
    fun onEvent(event: BlockDispenseEvent) {
        if (event.item.type in Rules.BLOCK_BREAK_ENABLERS) {
            val targetBlock = event.block.facingBlock()
                ?: return

            if(targetBlock.isEmpty || targetBlock.isLiquid) {
                event.isCancelled = true
                Effects.fizzle(event.block.location, event.block.getRelative(BlockFace.UP).location)
                return
            }

            if (targetBlock.type !in Rules.CANT_BE_BROKEN_BY_PLAYERS) {
                targetBlock.breakNaturally(event.item)
                event.isCancelled = true

                //TODO -> SFX
                //TODO -> Consume durability

                //TODO -> IDEA: Require multiple activations - or long activation?
                //TODO -> Consider block break time. Look up item.type.hardness

            } else {
                Effects.fizzle(event.block.location, event.block.getRelative(BlockFace.UP).location)
            }
        }
    }

    @EventHandler
    fun onEvent(event: PlayerInteractEvent) {
        if(!debug) return

        if(event.clickedBlock != null && event.item?.type == Material.STICK) {
            val type = event.clickedBlock?.type
            val loot = event.clickedBlock?.getDrops(ItemStack(Material.WOODEN_PICKAXE))
            val time = event.clickedBlock?.getDestroySpeed(ItemStack(Material.WOODEN_PICKAXE))

            plugin.server.broadcastMessage("Hardness ${type?.hardness}, requireSpecial: ${event.clickedBlock?.blockData?.requiresCorrectToolForDrops()}, loot: $loot, time: $time")
        }

        if(event.clickedBlock != null && event.item?.type in Rules.ALL_TOOLS) {
            val block = event.clickedBlock!!
            val tool = event.item!!

            val breakTime = BlockBreaking.getBreakTimeSeconds(event.item!!, event.clickedBlock!!)
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