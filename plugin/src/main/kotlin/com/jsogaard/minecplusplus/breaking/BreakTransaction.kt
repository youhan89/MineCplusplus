package com.jsogaard.minecplusplus.breaking

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

data class BreakTransaction(
    val id: Long,
    val durationTicks: Int,
    val breaker: Location,
    val breakee: Location,
    val breakeeType: Material,
    val tool: ItemStack,
)