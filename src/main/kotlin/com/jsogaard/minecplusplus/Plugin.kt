package com.jsogaard.minecplusplus

import org.bukkit.plugin.java.JavaPlugin

class Plugin: JavaPlugin() {
    override fun onEnable() {
        super.onEnable()
        server.pluginManager.registerEvents(PluginListener(this), this)
        server.pluginManager.registerEvents(DispenserPistonCraftAspect(this), this)
    }

    fun scheduleRun(delayTicks: Long = 1L, block: () -> Unit) {
        server.scheduler.runTaskLater(this, Runnable { block() }, delayTicks)
    }
}