package com.jsogaard.minecplusplus

import org.bukkit.plugin.java.JavaPlugin

class Plugin: JavaPlugin() {
    override fun onEnable() {
        super.onEnable()
        server.pluginManager.registerEvents(PluginListener(this), this)
    }

    fun scheduleRun(delay: Long = 1L, block: () -> Unit) {
        server.scheduler.runTaskLater(this, Runnable { block() }, delay)
    }
}