package com.jsogaard.minecplusplus

import com.jsogaard.minecplusplus.breaking.BreakingAspect
import com.jsogaard.minecplusplus.placing.PlacingAspect
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

class Plugin: JavaPlugin() {
    val namespaceKeys = Namespaces(
        craftingDropper = createNamespacedKey("crafting_dropper"),
        dropSlot = createNamespacedKey("drop_slot")
    )
    override fun onEnable() {
        super.onEnable()
        server.pluginManager.registerEvents(DispenseCraftingTableAspect(this), this)
        server.pluginManager.registerEvents(SmokedDropperAspect(this), this)

        server.pluginManager.registerEvents(BreakingAspect(this), this)
        server.pluginManager.registerEvents(PlacingAspect(this), this)

        //server.pluginManager.registerEvents(FirstTestAspect(this), this)
        //server.pluginManager.registerEvents(DispenserPistonCraftAspect(this), this)
        //server.pluginManager.registerEvents(ChannelingDropperAspect(this), this)
        //server.pluginManager.registerEvents(CheatAspect(this), this)
        //server.pluginManager.registerEvents(ChannelingDropperCraftingRecipeAspect(this), this)
        //server.pluginManager.registerEvents(ItemMoldingAspect(this), this)
        //server.pluginManager.registerEvents(CreateAspect(this), this)
    }

    fun scheduleRun(delayTicks: Long = 0L, block: () -> Unit) {
        server.scheduler.runTaskLater(this, Runnable { block() }, delayTicks)
    }

    fun createNamespacedKey(entry: String)
            = NamespacedKey.fromString("$namespace:$entry".lowercase(), this)!!
    companion object {
        const val namespace = "dispensercraft"
    }
}

data class Namespaces(
    val craftingDropper: NamespacedKey,
    val dropSlot: NamespacedKey,
)