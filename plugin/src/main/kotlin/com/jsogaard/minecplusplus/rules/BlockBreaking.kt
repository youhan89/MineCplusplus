package com.jsogaard.minecplusplus.rules

import com.jsogaard.minecplusplus.nms.destroyspeed.DestroySpeedPolyfill
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import kotlin.math.ceil

object BlockBreaking {
    fun getBreakTimeTicks(tool: ItemStack, block: Block): Int {
        val canHarvest = !block.blockData.requiresCorrectToolForDrops() || block.blockData.isPreferredTool(tool)

        val efficiencyLevel = tool.itemMeta?.enchants?.firstNotNullOfOrNull {
            if(it.key == Enchantment.DIG_SPEED)
                it.value
            else null
        } ?: 0

        return getBreakTimeTicks(
            toolMultiplier = DestroySpeedPolyfill.get(block, tool),
            canHarvest = canHarvest,
            efficiencyLevel = efficiencyLevel,
            blockHardness = block.type.hardness
        )
    }

    /**
     * @param toolMultiplier: The used tools multiplier on the specified block (or the default 1.0)
     * @param canHarvest: If the current tool can harvest the item (e.g. an axe can't harvest a furnace, but a pickaxe can)
     * @param efficiencyLevel: Enchanted level of "Efficiency" or 0
     * @param blockHardness: The blocks (material) hardness property
     *
     * As specified here, minus some player buffs/debuffs: https://minecraft.fandom.com/wiki/Breaking
     */
    fun getBreakTimeTicks(
        toolMultiplier: Float,
        canHarvest: Boolean,
        efficiencyLevel: Int,
        blockHardness: Float
    ): Int {
        val speed = when {
            toolMultiplier > 1f && efficiencyLevel >= 1
                -> toolMultiplier + (efficiencyLevel * efficiencyLevel) + 1f
            else
                -> toolMultiplier
        }

        val baseDamage = speed / blockHardness

        val damage = when {
            canHarvest -> baseDamage / 30
            else -> baseDamage / 100
        }

        if (damage > 1f) {
            // Instant breaking
            return 0
        }

        // In ticks:
        return ceil(1 / damage).toInt()
    }
}