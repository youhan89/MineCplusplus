package com.jsogaard.minecplusplus

import com.jsogaard.minecplusplus.breaking.BreakerAspect
import com.jsogaard.minecplusplus.crafting.DispenseCraftingTableAspect
import com.jsogaard.minecplusplus.crafting.SequenceInputDropperAspect
import com.jsogaard.minecplusplus.placing.PlacingAspect
import com.jsogaard.minecplusplus.portals.PortalTestAspect
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

class CubematicPlugin: JavaPlugin() {
    val namespaceKeys = Namespaces(
        craftingDropper = createNamespacedKey("crafting_dropper"),
        dropSlot = createNamespacedKey("drop_slot"),
        placerBlockTag = createNamespacedKey("placer")
    )
    override fun onEnable() {
        super.onEnable()
        //server.pluginManager.registerEvents(DebugAspect(this), this)

        server.pluginManager.registerEvents(DispenseCraftingTableAspect(this), this)
        server.pluginManager.registerEvents(SequenceInputDropperAspect(this), this)

        server.pluginManager.registerEvents(BreakerAspect(this), this)
        server.pluginManager.registerEvents(PlacingAspect(this), this)

        // teleport: server.pluginManager.registerEvents(PortalTestAspect(this), this)

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

    private fun createNamespacedKey(entry: String)
            = NamespacedKey.fromString("$namespace:$entry".lowercase(), this)!!
    companion object {
        const val namespace = "cubematic"
    }
}

data class Namespaces(
    val craftingDropper: NamespacedKey,
    val dropSlot: NamespacedKey,
    val placerBlockTag: NamespacedKey,
)