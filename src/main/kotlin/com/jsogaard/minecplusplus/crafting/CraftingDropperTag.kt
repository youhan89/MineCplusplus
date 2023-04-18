package com.jsogaard.minecplusplus.crafting

import com.jsogaard.minecplusplus.CubematicPlugin
import org.bukkit.block.Dropper
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

fun Dropper.isCraftingDropper(plugin: CubematicPlugin): Boolean = this.persistentDataContainer.isCraftingDropper(plugin)
fun Dropper.setCraftingDropper(isCraftingDropper: Boolean, plugin: CubematicPlugin) {
    this.persistentDataContainer.setCraftingDropper(isCraftingDropper, plugin)
    this.update()
}

fun PersistentDataContainer.setCraftingDropper(isCraftingDropper: Boolean, plugin: CubematicPlugin) {
    if(isCraftingDropper) {
        set(plugin.namespaceKeys.craftingDropper, PersistentDataType.BYTE, 1)
    } else {
        this.remove(plugin.namespaceKeys.craftingDropper)
    }
}

fun PersistentDataContainer.isCraftingDropper(plugin: CubematicPlugin): Boolean {
    val data = this.get(plugin.namespaceKeys.craftingDropper, PersistentDataType.BYTE)
    return data == 1.toByte()
}