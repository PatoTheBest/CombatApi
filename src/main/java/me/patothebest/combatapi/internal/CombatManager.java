package me.patothebest.combatapi.internal;

import me.patothebest.combatapi.api.CombatDeathEvent;
import me.patothebest.combatapi.api.DamageCause;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class CombatManager extends BukkitRunnable implements Listener {

    private final Map<Player, CombatTracker> combatTrackers = new HashMap<>();
    private final Plugin plugin;
    private final Map<Player, CombatDeathEvent> deathEvents = new HashMap<>();

    public CombatManager(Plugin plugin) {
        this.plugin = plugin;
        runTaskTimer(plugin, 1L, 1L);
    }

    public void destroy() {
        cancel();
    }

    @Override
    public void run() {
        combatTrackers.values().forEach(CombatTracker::tick);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        combatTrackers.put(event.getPlayer(), new CombatTracker());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerQuitEvent event) {
        combatTrackers.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        combatTrackers.get(player).trackDamage(event, false);

        if (event.getFinalDamage() >= (player).getHealth()) {
            killPlayer(player, true);
            event.setDamage(0);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        CombatDeathEvent combatDeathEvent = deathEvents.remove(event.getEntity());
        if (combatDeathEvent == null) {
            plugin.getLogger().log(Level.SEVERE, "Unhandled death event! Entity: " + event.getEntity() + ", Cause:" + event.getDeathMessage());
            return;
        }

        event.setDeathMessage(combatDeathEvent.getDeathMessage());
        event.getDrops().removeIf(itemStack -> !combatDeathEvent.getDrops().contains(itemStack));
    }

    public void damagePlayer(Player player, DamageCause damageCause, double damage) {
        combatTrackers.get(player).trackDamage(new EntityDamageEvent(player, DamageCause.getCause(damageCause), damage), true);

        if (player.getHealth() <= damage) {
            killPlayer(player, true);
        } else {
            player.damage(damage);
        }
    }

    public void killPlayer(Player player, boolean delegateEvent) {
        Location playerLocation = player.getLocation();
        CombatTracker combatTracker = combatTrackers.get(player);
        CombatDeathEvent combatDeathEvent = combatTracker.onDeath(player);
        plugin.getServer().getPluginManager().callEvent(combatDeathEvent);
        combatTracker.reset();

        if (!combatDeathEvent.isCancelled() && delegateEvent) {
            deathEvents.put(player, combatDeathEvent);
            player.setHealth(0);
        } else {
            for (ItemStack drop : combatDeathEvent.getDrops()) {
                if(drop != null) {
                    player.getWorld().dropItemNaturally(playerLocation, drop);
                }
            }
        }
    }
}
