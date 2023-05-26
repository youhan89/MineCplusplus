package com.jsogaard.minecplusplus.effects

import com.jsogaard.minecplusplus.CubematicPlugin
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.block.Block

object Sfx {
    fun blockBreak(block: Block) {
        try {
            block.world.playSound(block.location, block.blockData.soundGroup.breakSound, 1f, 1f)
        } catch (e: Exception) {
            //OK to fail
        }
    }

    fun blockPlaced(block: Block) {
        try {
            block.world.playSound(block.location, block.blockData.soundGroup.placeSound, 1f, 1f)
        } catch (e: Exception) {
            //OK to fail
        }
    }

    fun blockToSequential(location: Location, plugin: CubematicPlugin) {
        val sound = Sound.BLOCK_DISPENSER_DISPENSE
        val world = location.world
            ?: return

        world.playSound(location, sound, 1f, 0.15f)
        (1..8).forEach {
            plugin.scheduleRun(it.toLong() * 3) {
                world.playSound(location, sound, 1f, 0.1f * it + 0.15f)
            }
        }
    }
    fun blockToSequential2(location: Location, plugin: CubematicPlugin) {
        val sound = Sound.BLOCK_NOTE_BLOCK_BIT
        val world = location.world
            ?: return

        world.playSound(location, sound, 1f, 0.5f)

        plugin.scheduleRun(9) {
            world.playSound(location, sound, 1f, 0.75f)
        }

        plugin.scheduleRun(18) {
            world.playSound(location, sound, 1f, 1f)
        }
    }
}