package codes.domino.passwordchests.manager;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PasswordManager {

    private static PasswordManager INSTANCE;
    private final Map<String, String> passwords = new HashMap<>();

    private PasswordManager() {
        INSTANCE = this;
    }

    public static PasswordManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PasswordManager();
        }
        return INSTANCE;
    }

    private static String locationToString(final Location loc) {
        return loc.getWorld().getName() + "/" + loc.getX() + "/" + loc.getY() + "/" + (loc.getZ());
    }

    private static Location stringToLocation(final String string) {
        final String[] split = string.split("/");
        return new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]));
    }

    public boolean hasPassword(Location block) {

        return passwords.containsKey(locationToString(block));
    }

    private String getPassword(Location block) {

        return passwords.get(locationToString(block));
    }

    private void setPassword(Location block, String password) {

        passwords.put(locationToString(block), password);
    }

    private void setPassword(String block, String password) {
        setPassword(stringToLocation(block), password);
    }

    public boolean isPasswordCorrect(Location block, String password) {
        return password.equals(getPassword(block));
    }

    public boolean requestPasswordAssignment(Player player, Location block, String password) {
        if (isAllowedByWorldGuard(player, block)) {
            setPassword(block, password);
            return true;
        }
        return false;
    }

    public boolean requestPasswordAssignment(Player player, Location block, Location password) {
        if (isAllowedByWorldGuard(player, block)) {
            setPassword(block, getPassword(password));
            return true;
        }

        return false;
    }

    public boolean isAllowedByWorldGuard(Player player, Location block) {
        if (Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            return true;
        }
        WorldGuardPlugin wg = WorldGuardPlugin.inst();
        WorldGuard worldGuard = WorldGuard.getInstance();
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(block);

        if (!worldGuard.getPlatform().getSessionManager()
                .hasBypass(wg.wrapPlayer(player), BukkitAdapter.adapt(block.getWorld()))) {
            return query.testState(loc, wg.wrapPlayer(player), Flags.BLOCK_PLACE);
        } else {
            return true;
        }
    }

    public void onDisable(FileConfiguration config) {
        for (String key : config.getKeys(false)) {
            if (!passwords.containsKey(key.replace("#", "."))) {
                config.set(key, null);
            }
        }
        for (Map.Entry<String, String> entry : passwords.entrySet()) {
            config.set(entry.getKey().replace(".", "#"), entry.getValue());
        }

    }

    public void onEnable(FileConfiguration config) {
        for (String key : config.getKeys(false)) {
            setPassword(key.replace("#", "."), config.getString(key));
        }
    }

    public void removePassword(Location location) {
        passwords.remove(locationToString(location));
    }
}