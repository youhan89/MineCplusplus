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

    val PICKAXES = listOf(
        Material.WOODEN_PICKAXE,
        Material.STONE_PICKAXE,
        Material.IRON_PICKAXE,
        Material.GOLDEN_PICKAXE,
        Material.DIAMOND_PICKAXE,
        Material.NETHERITE_PICKAXE,
    )

    val AXES = listOf(
        Material.WOODEN_AXE,
        Material.STONE_AXE,
        Material.IRON_AXE,
        Material.GOLDEN_AXE,
        Material.DIAMOND_AXE,
        Material.NETHERITE_AXE,
    )

    val SHOVELS = listOf(
        Material.WOODEN_SHOVEL,
        Material.STONE_SHOVEL,
        Material.IRON_SHOVEL,
        Material.GOLDEN_SHOVEL,
        Material.DIAMOND_SHOVEL,
        Material.NETHERITE_SHOVEL,
    )

    val HOES = listOf(
        Material.WOODEN_HOE,
        Material.STONE_HOE,
        Material.IRON_HOE,
        Material.GOLDEN_HOE,
        Material.DIAMOND_HOE,
        Material.NETHERITE_HOE,
    )

    val ALL_TOOLS = PICKAXES + HOES + AXES + SHOVELS

    val BLOCK_BREAK_ENABLERS = ALL_TOOLS
}