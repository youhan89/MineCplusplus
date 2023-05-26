package com.jsogaard.minecplusplus.nms.destroyspeed

import cubematic.paper.PaperApis
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
                return PaperApis.getBlockDestroySpeed(block, tool)
            } catch (e: Throwable) {
                failedNative = true
            }
        }

        return shim.getDestroySpeed(block, tool)
    }
}