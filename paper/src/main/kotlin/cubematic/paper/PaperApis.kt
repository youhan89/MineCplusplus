package cubematic.paper

import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

object PaperApis {
    fun getBlockDestroySpeed(block: Block, tool: ItemStack): Float = block.getDestroySpeed(tool)
    fun isBlockReplaceable(block: Block) = block.isReplaceable
}