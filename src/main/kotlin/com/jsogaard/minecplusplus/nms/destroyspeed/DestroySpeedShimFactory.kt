package com.jsogaard.minecplusplus.nms.destroyspeed

import com.jsogaard.minecplusplus.nms.NmsUtil
import org.bukkit.inventory.ItemStack

object DestroySpeedShimFactory {
    /**
     * Replicates parts of Paper's Block.getDestroySpeed on Spigot.
     * Created for 1.19.4, may or may not work on later revisions.
     */
    fun create(): DestroySpeedShim {
        val nmsVersion = NmsUtil.NMS_VERSION
        val nmsIBlockData = Class.forName("net.minecraft.world.level.block.state.IBlockData")
        val nmsBlock = Class.forName("net.minecraft.world.level.block.Block")
        val nmsItemStackClass = Class.forName("net.minecraft.world.item.ItemStack")
        val nmsCraftBlockClass = Class.forName("org.bukkit.craftbukkit.$nmsVersion.block.CraftBlock")
        val nmsCraftItemStack = Class.forName("org.bukkit.craftbukkit.$nmsVersion.inventory.CraftItemStack")
        val nmsCraftItemStack_asNMSCopy = nmsCraftItemStack?.getDeclaredMethod("asNMSCopy", ItemStack::class.java)!!
        val nmsCraftBlock_getNMS = nmsCraftBlockClass?.getDeclaredMethod("getNMS")!!

        val nmsIBlockData_getBlock_candidates = nmsIBlockData.methods.filter {
            it.returnType == nmsBlock && it.parameterTypes.isEmpty()
        }

        val nmsIBlockData_getBlock = when(nmsIBlockData_getBlock_candidates.size) {
            1 -> nmsIBlockData_getBlock_candidates.first()
            else -> throw IllegalStateException("Found ${nmsIBlockData_getBlock_candidates.size} methods searching for nmsIBlockData_getBlock")
        }

        val defaultBlockState = nmsBlock.declaredMethods.filter {
            it.returnType == nmsIBlockData && it.parameterTypes.isEmpty()
        }

        val nmsBlock_defaultBlockState = when(defaultBlockState.size) {
            1 -> defaultBlockState.first()
            else -> throw IllegalStateException("Found ${defaultBlockState.size} methods searching for nmsBlock_defaultBlockState")
        }

        val matching = nmsItemStackClass?.declaredMethods?.filter {
            it.returnType == Float::class.java && it.parameterTypes.contentEquals(arrayOf(nmsIBlockData))
        }

        val nmsItemStack_getDestroySpeed = when (matching?.size) {
            1 -> matching.first()
            else -> throw IllegalStateException("Found ${matching?.size} methods searching for nmsItemStack_getDestroySpeed")
        }

        return DestroySpeedShim(
            nmsIBlockData_getBlock = nmsIBlockData_getBlock,
            nmsBlock_defaultBlockState = nmsBlock_defaultBlockState,
            nmsCraftBlock_getNMS = nmsCraftBlock_getNMS,
            nmsCraftItemStack_asNMSCopy = nmsCraftItemStack_asNMSCopy,
            nmsItemStack_getDestroySpeed = nmsItemStack_getDestroySpeed
        )
    }
}

