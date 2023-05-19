package com.jsogaard.minecplusplus.nms.destroyspeed

import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

object DestroySpeedPolyfill {
    val shim by lazy {
        DestroySpeedShimFactory.create()
    }

    private var failedNative = false

    fun get(block: Block, tool: ItemStack): Float {
        if(!failedNative) {
            try {
                return block.getDestroySpeed(tool)
            } catch (e: Throwable) {
                failedNative = true
            }
        }

        return shim.getDestroySpeed(block, tool)
    }
}