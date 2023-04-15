package com.jsogaard.minecplusplus.effects

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
}