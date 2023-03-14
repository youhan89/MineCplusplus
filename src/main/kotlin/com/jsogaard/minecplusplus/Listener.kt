package com.jsogaard.minecplusplus

import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Dropper
import org.bukkit.block.data.Directional
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.block.BlockRedstoneEvent
import org.bukkit.event.weather.WeatherChangeEvent


class FirstTestAspect(private val plugin: Plugin): Listener {
    private var stopTheDrop = false

    @EventHandler
    fun onBlockPower(event: BlockRedstoneEvent) {
        if(event.newCurrent > 0 && event.oldCurrent <= 0) {
            //plugin.server.broadcastMessage("Foo: ${event.block.type.name}")
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

}
