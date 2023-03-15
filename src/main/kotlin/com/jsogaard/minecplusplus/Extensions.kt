package com.jsogaard.minecplusplus

import org.bukkit.block.Block
import org.bukkit.block.Dispenser
import org.bukkit.block.Dropper
import org.bukkit.block.data.Directional
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

fun Block.toDispenser() = this.state as Dispenser
fun Block.toDropper() = this.state as Dropper

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

operator fun ItemStack.minus(other: Int): ItemStack? {
    val newAmount = this.amount - other
    return when {
        newAmount < 0 -> throw IllegalStateException()
        newAmount == 0 -> null
        else -> ItemStack(this.type, this.amount - other)
    }
}
operator fun ItemStack.plus(other: ItemStack?): ItemStack {
    if(other == null)
        return this

    return ItemStack(
        this.type,
        (this.amount + other.amount).coerceAtMost(this.type.maxStackSize)
    )
}

fun ItemStack?.canStackWith(other: ItemStack): Boolean {
    if(this == null)
        return true

    return this.isSimilar(other)
            && (this.amount + other.amount) <= this.type.maxStackSize
}

fun ItemStack.stackWithOrNull(other: ItemStack): ItemStack? {
    return if(this.canStackWith(other))
        this + other
    else
        null
}