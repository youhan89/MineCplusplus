package com.jsogaard.minecplusplus

import org.bukkit.block.Block
import org.bukkit.block.Dispenser
import org.bukkit.block.Dropper
import org.bukkit.block.data.Directional
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

fun Block.toDispenser() = this.state as Dispenser
fun Block.toDropper() = this.state as Dropper
fun Block.asInventoryHolder() = this.state as? InventoryHolder

fun Dispenser.facingBlock(): Block {
    val facing = (this.blockData as Directional).facing
    return this.block.getRelative(facing)
}

private fun Inventory.copyAndMerge(result: ItemStack): Array<ItemStack> {
    return this.contents.map { currentItem ->
        when {
            currentItem == null -> {
                val claim = result.amount.coerceAtMost(result.type.maxStackSize)
                result.amount -= claim
                ItemStack(result.type, claim)
            }
            currentItem.type == result.type -> {
                val claim = result.amount.coerceAtMost(result.type.maxStackSize - currentItem.amount)
                result.amount -= claim
                ItemStack(result.type, claim + currentItem.amount)
            }
            else -> ItemStack(currentItem)
        }
    }.toTypedArray()
}