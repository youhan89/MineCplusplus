package com.jsogaard.minecplusplus.crafting

import com.jsogaard.minecplusplus.*
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.block.Dropper
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class DispenseCraftingTableAspect(private val plugin: CubematicPlugin): Listener {
    private val craftingResolver = CraftingResolver(plugin.server)

    @EventHandler
    fun onEvent(event: BlockDispenseEvent) {
        if(event.block.type != Material.DISPENSER || event.item.type != Material.CRAFTING_TABLE)
            return

        val dispenser = event.block.toDispenser()
        val dropperBlock = dispenser.facingBlock()

        if(dropperBlock.type != Material.DROPPER)
            return

        val dropper = dropperBlock.toDropper()
        val dropperInventory = dropper.inventory

        event.isCancelled = true

        val pattern = dropperInventory.contents.map { item ->
            when {
                item == null -> null
                fillerItems.contains(item.type) -> null
                else -> item
            }
        }

        val result = craftingResolver.craft(pattern, event.block.world) ?: run {
            //plugin.server.broadcastMessage("No recipe found for crafting matrix...")
            //Play fizzle effect
            val location = dropper.block.location
            dropper.world.playEffect(location, Effect.SMOKE, 1);
            return
        }

        //The dropper is either facing another container, or something else. Make sure we can actually put the items there.
        //If it's not an inventory, it'll just drop into the world, which is always safe
        val dropTargetBlock = dropperBlock.facingBlock()!!
        val chainedInventory = dropTargetBlock.inventory()
        result.forEach {
            //TODO Effect here?
            if(chainedInventory?.canReceive(it) == false) {
                return
            }
        }

        //This next part removes a set of ingredients from the inventory
        val contentsAfterCrafting = dropperInventory.contents.map {
            when {
                it == null -> null
                fillerItems.contains(it.type) -> it
                it.amount > 1 -> {
                    it.amount--
                    it
                }
                else -> null
            }
        }.toTypedArray()

        dropperInventory.contents = arrayOfNulls(9)
        var previousAmount = 0
        result.forEach {
            dropperInventory.addItem(it)
            previousAmount += it.amount
        }

        while(!dropperInventory.isEmpty) {
            dropper.drop()

            //As an additional safeguard, make sure we actually dropped an item
            val newAmount = dropperInventory.storageContents.sumOf { it?.amount ?: 0 }
            if(previousAmount == newAmount) {
                //If this happens, we must fix our logic earlier in this method that checks if we can drop all items.
                plugin.server.broadcastMessage("Error! Safeguard triggered, aborting drop! Output has been lost!")

                //Revert dropper contents to the state it would have as if we would have succeeded dropping everything
                //This is important, so we don't create a dupe bug if one or more items have already been dropped.
                dropperInventory.contents = contentsAfterCrafting
                return
            }

            previousAmount = newAmount
        }
        dropperInventory.contents = contentsAfterCrafting

        playCraftEffect(dropper, dropTargetBlock)
    }

    private fun playCraftEffect(dropper: Dropper, dropTargetBlock: Block) {
        val x = dropper.x + 0.5
        val y = dropper.y + 0.5
        val z = dropper.z + 0.5
        val ox = dropTargetBlock.location.x + 0.5
        val oy = dropTargetBlock.location.y + 0.5
        val oz = dropTargetBlock.location.z + 0.5

        val newLocation = Location(
            dropper.world,
            x + (ox - x) / 2,
            y + (oy - y) / 2,
            z + (oz - z) / 2
        )

        dropper.world.spawnParticle(Particle.SMALL_FLAME, newLocation, 16, 0.1, 0.1, 0.1, 0.01)
    }
}

private fun Inventory.canReceive(item: ItemStack): Boolean {
    val single = item.clone().also { it.amount = 1 }

    val canStackAmount = this.storageContents.sumOf {
        if(it.canStackWith(single)) 1 as Int else 0
    }

    return canStackAmount >= item.amount
}

private fun Block.inventory(): Inventory? {
    return (this.state as? Container)?.inventory
}
