package com.jsogaard.minecplusplus.breaking

import com.jsogaard.minecplusplus.Plugin
import com.jsogaard.minecplusplus.facingBlock
import com.jsogaard.minecplusplus.rules.Rules
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDispenseEvent

class BreakingAspect(private val plugin: Plugin): Listener {
    @EventHandler
    fun onEvent(event: BlockDispenseEvent) {
        if (event.item.type in Rules.BLOCK_BREAK_ENABLERS) {
            val targetBlock = event.block.facingBlock()
                ?: return

            val loot = targetBlock.getDrops(event.item)

            if (!loot.isEmpty() && targetBlock.type !in Rules.CANT_BE_BROKEN_BY_PLAYERS) {
                targetBlock.breakNaturally(event.item)
                event.isCancelled = true
                //TODO -> SFX
                //TODO -> IDEA: Require multiple activations - or long activation?
                //TODO -> Consider block break time. Look up item.type.hardness
                //TODO -> Consume durability
            } else {
                //TODO -> Fizzle effect
            }
        }
    }
}