package codes.domino.passwordchests.listener;

import codes.domino.passwordchests.manager.PasswordManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.getBlock().getType().name().endsWith("CHEST")) return;
        if (!PasswordManager.getInstance().hasPassword(event.getBlock().getLocation())) return;
        if (event.isCancelled()) return;
        PasswordManager.getInstance().removePassword(event.getBlock().getLocation());
    }
}
