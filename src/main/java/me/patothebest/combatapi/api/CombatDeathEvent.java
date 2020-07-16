package me.patothebest.combatapi.api;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * Event called when a player is about to die, this is called
 * on the last damage event where it is certain the death event
 * will be called. This is useful for cancelling the death without
 * having any side effects of setting the player's health to 20
 *
 * If the event is cancelled, the {@link PlayerDeathEvent} will not
 * be called but the items of {@link CombatDeathEvent#getDrops()} will
 * be dropped on the location of the death. You can safely clear the list
 * if you want to not have drops. If the event is not cancelled, the
 * {@link PlayerDeathEvent} will be called by bukkit/spigot and the
 * drops from this event will be ignored.
 *
 */
public class CombatDeathEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final List<ItemStack> drops;
    private final CombatEntry lastDamage;
    private final LinkedList<CombatEntry> combatEntries;
    private final ItemStack itemKilledWith;
    private final Entity killer;
    private final Player killerPlayer;
    private String deathMessage = "";
    private boolean cancelled = false;

    public CombatDeathEvent(Player player, List<ItemStack> drops, CombatEntry lastDamage, LinkedList<CombatEntry> combatEntries, ItemStack itemKilledWith, Entity killer, Player killerPlayer, String deathMessage) {
        super(player);
        this.drops = drops;
        this.lastDamage = lastDamage;
        this.combatEntries = combatEntries;
        this.itemKilledWith = itemKilledWith;
        this.killer = killer;
        this.killerPlayer = killerPlayer;
        this.deathMessage = deathMessage;
    }

    /**
     * Gets the player's death cause. This is the list of
     * the official minecraft death causes taken from the
     * list of death messages.
     *
     * @return the player's death cause
     */
    public DeathCause getDeathCause() {
        if (lastDamage == null) {
            return DeathCause.GENERIC;
        }

        return lastDamage.getDeathCause();
    }

    /**
     * Gets the item the player was killed with. This can be
     * be null
     *
     * @return the item the player was killed with
     */
    @Nullable
    public ItemStack getItemKilledWith() {
        return itemKilledWith;
    }

    /**
     * Gets the entity that killed the player
     * <p />
     * <strong>NOTE:</strong> can be different form {@link CombatDeathEvent#getKillerPlayer()}
     *
     * @return the entity that killed the player
     */
    @Nullable
    public Entity getKiller() {
        return killer;
    }

    /**
     * This will return the player if the killer is a player,
     * or will return the entity owner (pet owner, shooter)
     *
     * @return the player killer
     */
    @Nullable
    public Player getKillerPlayer() {
        return killerPlayer;
    }

    /**
     * You can cancel this event safely and the player death
     * event will not be called. This is useful for handling
     * players that you want to make them spectator on death
     * or teleport them without having the respawn screen or
     * any other weirdness Minecraft introduces
     *
     * @param cancel whether this event is cancelled or not
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the death message for this event
     * <p />
     * <strong>NOTE:</strong> the death message won't be
     * broadcasted. You must manually broadcast it if you need.
     * Useful for having custom death messages inside an arena.
     *
     * @param deathMessage the new death message
     */
    public void setDeathMessage(String deathMessage) {
        this.deathMessage = deathMessage;
    }

    /**
     * Gets the death message and sets it for this event
     *
     * @return the death message
     */
    public String getDeathMessage() {
        return deathMessage;
    }

    /**
     * Gets the player's inventory items
     *
     * @return the player's drops
     */
    public List<ItemStack> getDrops() {
        return drops;
    }

    /**
     * Gets the last damage that killed the player
     * <p />
     * The {@link CombatDeathEvent#getKillerPlayer()} could be present
     * while the {@link CombatEntry#getPlayerKiller()} could not because
     * the last damage could be fire but the fire was caused by a player
     * (ex. pushed into lava)
     *
     * @return the last damage
     */
    public CombatEntry getLastDamage() {
        return lastDamage;
    }

    /**
     * Gets all the Combat events leading to the player's death
     *
     * @return a list of the combat events
     */
    public LinkedList<CombatEntry> getCombatEvents() {
        return combatEntries;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
