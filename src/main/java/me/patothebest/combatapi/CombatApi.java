package me.patothebest.combatapi;

import me.patothebest.combatapi.internal.CombatManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class CombatApi extends JavaPlugin {

    private CombatManager combatManager;

    @Override
    public void onEnable() {
        combatManager = new CombatManager(this);
    }

    @Override
    public void onDisable() {
        combatManager.destroy();
    }
}
