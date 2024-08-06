package codes.domino.passwordchests.listener;

import codes.domino.passwordchests.PasswordChests;
import codes.domino.passwordchests.manager.PasswordManager;
import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIBuilder;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Collections;

public class PasswordAttemptListener implements Listener {

    private final SignGUIBuilder signInputBuilder;

    public PasswordAttemptListener() {
        signInputBuilder = SignGUI.builder()
                .setLines(null, "§4^^^^^^^^^^^^^^^", "§4Insert Password")
                .setType(Material.BIRCH_SIGN)
                .setColor(DyeColor.BLACK);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRightClick(final PlayerInteractEvent event) {
        if (event.getPlayer().isSneaking()) {
            return;
        }
        if (!(event.hasBlock() && event.getAction().name().startsWith("RIGHT"))) {
            return;
        }
        if (!event.getClickedBlock().getType().name().endsWith("CHEST")) {
            return;
        }
        if (event.getItem() != null && Material.TRIPWIRE_HOOK == event.getItem().getType()) {
            return;
        }
        if (event.useInteractedBlock() == Event.Result.DENY) {
            return;
        }
        PasswordManager manager = PasswordManager.getInstance();
        Location blockLocation = event.getClickedBlock().getLocation();
        if (manager.hasPassword(blockLocation)) {
            event.setCancelled(true);
            Location finalBlockLocation = blockLocation;
            signInputBuilder.setHandler((player, result) -> {
                String password = result.getLine(0);
                if (password.isEmpty()) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Your password is empty.");
                    return Collections.emptyList();
                }
                if (manager.isPasswordCorrect(finalBlockLocation, password)) {
                    event.getPlayer().sendMessage(ChatColor.GREEN + "Password was correct.");
                    Bukkit.getScheduler().runTask(PasswordChests.getInstance(), () -> {
                        event.getPlayer().openInventory(((Chest) event.getClickedBlock().getState()).getInventory());
                    });
                } else {
                    event.getPlayer().sendMessage(ChatColor.RED + "Password was incorrect.");
                }
                return Collections.emptyList();
            }).build().open(event.getPlayer());
        }
    }
}
