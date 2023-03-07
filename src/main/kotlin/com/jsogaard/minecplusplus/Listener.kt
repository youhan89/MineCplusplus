package com.jsogaard.minecplusplus

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Dropper
import org.bukkit.block.data.Directional
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.block.BlockRedstoneEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue


class PluginListener(private val plugin: Plugin): Listener {
    private var stopTheDrop = false

    @EventHandler
    fun onBlockPower(event: BlockRedstoneEvent) {
        if(event.newCurrent > 0 && event.oldCurrent <= 0) {
            plugin.server.broadcastMessage("Foo: ${event.block.type.name}")
        }
    }

    @EventHandler
    fun onBlockDispense(event: BlockDispenseEvent) {
        if(!stopTheDrop
            && event.block.type == Material.DROPPER
            && event.block.getRelative(BlockFace.DOWN).type == Material.CRAFTING_TABLE) {

            //plugin.server.broadcastMessage("Dropper wanna drop: ${event.item.type.name}")

            val locationOfDropper = event.block.location
            event.isCancelled = true

            plugin.scheduleRun {
                val dropper = locationOfDropper.block.state as? Dropper ?: kotlin.run {
                    plugin.server.broadcastMessage("Not a dropper...")
                    return@scheduleRun
                }

                val facing = (locationOfDropper.block.blockData as? Directional)?.facing ?: kotlin.run {
                    plugin.server.broadcastMessage("No facing...")
                    return@scheduleRun
                }
                val outputBlock = dropper.block.getRelative(facing)

                val recipe = plugin.server.getCraftingRecipe(dropper.inventory.contents, event.block.world)
                val result = recipe?.result ?: run {
                    plugin.server.broadcastMessage("Nothing matches the recipe...")
                    return@scheduleRun
                }

                //event.block.world.dropItem(outputBlock.location, result)

                val oldInventory = dropper.inventory.contents.clone()
                dropper.inventory.contents = listOf(result).toTypedArray()
                stopTheDrop = true
                repeat((0 .. result.amount).count()) {
                    dropper.drop()
                }
                stopTheDrop = false
                dropper.inventory.contents = oldInventory
            }
        }
    }

    @EventHandler
    fun onWeather(event: WeatherChangeEvent) {
        //NO rain
        if(event.toWeatherState()) event.isCancelled = true
    }

    @EventHandler
    fun onBlockDropItem(event: BlockDropItemEvent) {

    }

    @EventHandler
    fun onMoveInventoryEvent(event: InventoryMoveItemEvent) {
        if(event.initiator.type == InventoryType.HOPPER && event.destination.type == InventoryType.DROPPER) {
            val hopper = event.source
            val dropper = event.destination
            val itemType = event.item.type

            val dropperBlock = dropper.location?.block?.let {
                if(it.type == Material.DROPPER) {
                    it
                } else null
            }

            val targetSlot = when(val metaData = dropperBlock?.getMetadata("nextSlot")) {
                null -> 0
                else -> {
                    if(metaData.isEmpty()) {
                        0
                    } else {
                        val last = (metaData[0]?.value() as? Int) ?: 0
                        if(last + 1 <= 8) last + 1 else 0
                    }
                }
            }

            dropperBlock?.setMetadata("nextSlot", FixedMetadataValue(plugin, targetSlot))

            // When executing this code, the item is "up in the air" and can't be subtracted from the source...?
            // it appears to be overwritten with original stack after cancelling the event.
            // So we tell the server to cancel event and queue our custom transaction asap instead.
            plugin.scheduleRun {
                val targetStack = dropper.getItem(targetSlot)
                val newStack = if (targetStack == null || targetStack.amount == 0) {
                    event.item
                } else {
                    targetStack.combineOrNull(event.item)
                }

                if (newStack != null) {
                    dropper.setItem(targetSlot, newStack)

                    val slot = hopper.first(itemType)
                    val stack = hopper.getItem(slot)!!
                    hopper.clear(slot)
                    hopper.setItem(slot, stack - 1)
                }
            }

            event.isCancelled = true
        }
    }
}

operator fun ItemStack.minus(other: Int): ItemStack {
    return ItemStack(this.type, this.amount - other)
}
operator fun ItemStack.plus(other: ItemStack?): ItemStack {
    if(other == null)
        return this

    return ItemStack(
        this.type,
        (this.amount + other.amount).coerceAtMost(this.type.maxStackSize)
    )
}

private fun ItemStack.canStack(other: ItemStack): Boolean {
    return this.type == other.type
            && (this.amount + other.amount) <= this.type.maxStackSize
}

private fun ItemStack.combineOrNull(other: ItemStack): ItemStack? {
    return if(this.canStack(other))
        this + other
    else
        null
}