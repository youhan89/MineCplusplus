import org.bukkit.plugin.java.JavaPlugin

class Plugin: JavaPlugin() {
    override fun onEnable() {
        super.onEnable()
        server.pluginManager.registerEvents(PluginListener(), this)
    }
}