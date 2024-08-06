package codes.domino.passwordchests;

import codes.domino.passwordchests.listener.BlockBreakListener;
import codes.domino.passwordchests.listener.HookWireClickListener;
import codes.domino.passwordchests.listener.PasswordAttemptListener;
import codes.domino.passwordchests.manager.PasswordManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class PasswordChests extends JavaPlugin {

    private static JavaPlugin INSTANCE;

    public static JavaPlugin getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        saveDefaultConfig();
        PasswordManager.getInstance().onEnable(getConfig());
        getServer().getPluginManager().registerEvents(new HookWireClickListener(), this);
        getServer().getPluginManager().registerEvents(new PasswordAttemptListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
    }

    @Override
    public void onDisable() {
        PasswordManager.getInstance().onDisable(getConfig());
        saveConfig();
    }
}
