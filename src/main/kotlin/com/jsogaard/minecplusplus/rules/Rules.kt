package com.jsogaard.minecplusplus.rules

import org.bukkit.Material

object Rules {
    val CANT_BE_BROKEN_BY_PLAYERS = listOf(
        Material.BEDROCK,
        Material.END_PORTAL,
        Material.END_PORTAL_FRAME,
        Material.STRUCTURE_BLOCK,
        Material.BARRIER,
        Material.COMMAND_BLOCK,
    )

    val CAN_PLACE_BLOCK_IN = listOf(
        Material.AIR,
        Material.WATER,
        Material.LAVA,
        Material.GRASS,
        //TODO -> There are probably a lot more of these
    )

    val BLOCK_BREAK_ENABLERS = listOf(
        Material.WOODEN_PICKAXE,
        Material.STONE_PICKAXE,
        Material.IRON_PICKAXE,
        Material.GOLDEN_PICKAXE,
        Material.DIAMOND_PICKAXE,
        Material.NETHERITE_PICKAXE,
    )
}