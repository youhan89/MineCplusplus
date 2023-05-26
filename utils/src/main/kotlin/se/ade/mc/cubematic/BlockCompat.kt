package se.ade.mc.cubematic

import cubematic.paper.PaperApis
import org.bukkit.Material
import org.bukkit.block.Block

object BlockCompat {
    private val REPLACEABLE_BLOCKS = listOf(
        Material.AIR,
        Material.WATER,
        Material.LAVA,
        Material.GRASS,
        //TODO -> Flowers? more stuff.
    )

    private val isPaper: Boolean by lazy {
        ClassLoader.getSystemClassLoader().let {
            it.getResource("io/papermc/paper") != null
                    || it.getResource("io/papermc/paperclip") != null
                    || it.getResource("com/destroystokyo/paper") != null
                    || it.getResource("com/destroystokyo/paperclip") != null
        }
    }
    fun isReplaceable(block: Block): Boolean {
        return if(isPaper) {
            PaperApis.isBlockReplaceable(block)
        } else {
            block.type in REPLACEABLE_BLOCKS
        }
    }
}