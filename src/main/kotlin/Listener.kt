import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent


class PluginListener: Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent?) {
        Bukkit.broadcastMessage("Welcome to the server ${event?.player?.name}!")
    }
}