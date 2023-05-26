package com.jsogaard.minecplusplus.nms.destroyspeed

import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Method

class DestroySpeedShim(
    private val nmsIBlockData_getBlock: Method,
    private val nmsBlock_defaultBlockState: Method,
    private val nmsCraftBlock_getNMS: Method,
    private val nmsItemStack_getDestroySpeed: Method,
    private val nmsCraftItemStack_asNMSCopy: Method,
) {
    fun getDestroySpeed(block: Block, tool: ItemStack): Float {
        val nmsItemStack = nmsCraftItemStack_asNMSCopy.invoke(null, tool)
        val nmsIBlockData = nmsCraftBlock_getNMS.invoke(block)
        val nmsBlock = nmsIBlockData_getBlock.invoke(nmsIBlockData)
        val defaultBlockData = nmsBlock_defaultBlockState.invoke(nmsBlock)
        return nmsItemStack_getDestroySpeed.invoke(nmsItemStack, defaultBlockData) as Float
    }
}