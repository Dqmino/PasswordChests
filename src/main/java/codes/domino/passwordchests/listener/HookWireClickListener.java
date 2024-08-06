package codes.domino.passwordchests.listener;

import codes.domino.passwordchests.PasswordChests;
import codes.domino.passwordchests.manager.PasswordManager;
import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;

import java.util.Collections;

public class HookWireClickListener implements Listener {

    private final SignGUIBuilder signInputBuilder;

    public HookWireClickListener() {
        signInputBuilder = SignGUI.builder()
                .setLines(null, "§4^^^^^^^^^^^^^^^", "§4Assign Password")
                .setType(Material.BIRCH_SIGN)
                .setColor(DyeColor.BLACK);
    }

    private static Location getOtherChestBlock(Block block) {
        BlockState chestState = block.getState();
        Location otherSideLocation = null;
        if (chestState instanceof Chest) {
            Chest chest = (Chest) chestState;
            Inventory inventory = chest.getInventory();
            if (inventory instanceof DoubleChestInventory) {
                DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
                if (doubleChest.getLeftSide() == null || doubleChest.getRightSide() == null) {
                    return null;
                }
                otherSideLocation = new Location(block.getWorld(), doubleChest.getX(), doubleChest.getY(), doubleChest.getZ()).multiply(2)
                        .subtract(block.getLocation());
            }
        }
        return otherSideLocation;
    }

    @EventHandler
    public void onRightClick(final PlayerInteractEvent event) {
        if (event.getPlayer().isSneaking()) {
            return;
        }
        if (!(event.getItem() != null && event.getItem().getType() == Material.TRIPWIRE_HOOK && !event.getItem().getItemMeta().hasDisplayName())) {
            return;
        }
        if (!(event.hasBlock() && event.getAction().name().startsWith("RIGHT"))) {
            return;
        }
        if (!event.getClickedBlock().getType().name().endsWith("CHEST")) {
            return;
        }
        event.setCancelled(true);
        PasswordManager manager = PasswordManager.getInstance();
        Location blockLocation = event.getClickedBlock().getLocation();
        if (manager.hasPassword(blockLocation)) {
            event.getPlayer().sendMessage(ChatColor.RED + "This chest already has a password assigned to it.");
            return;
        }
        Location otherSideLocation = getOtherChestBlock(event.getClickedBlock());
        signInputBuilder.setHandler(((player, result) -> {
                    String password = result.getLine(0);

                    if (password.isEmpty()) {
                        player.sendMessage(ChatColor.RED + "This password is empty.");
                        return Collections.emptyList();
                    }
                    if (manager.isAllowedByWorldGuard(player, blockLocation)) {

                        if (otherSideLocation == null) {
                            manager.requestPasswordAssignment(player, blockLocation, password);
                            player.sendMessage(ChatColor.GREEN + "Password has been assigned.");
                        } else if (manager.isAllowedByWorldGuard(player, otherSideLocation) && !manager.hasPassword(otherSideLocation)) {

                            manager.requestPasswordAssignment(player, blockLocation, password);
                            manager.requestPasswordAssignment(player, otherSideLocation, password);

                            player.sendMessage(ChatColor.GREEN + "Password for 2 blocks has been assigned.");
                        } else {
                            player.sendMessage(ChatColor.RED + "Failed.");
                        }

                    } else {
                        player.sendMessage(ChatColor.RED + "Password has not been assigned, You lack world guard permissions.");
                    }
                    return Collections.emptyList();
                })).build().open(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDoubleChestFormation(BlockPlaceEvent event) {
        if (!event.getBlock().getType().name().endsWith("CHEST")) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }

        Block newBlock = event.getBlock();
        Bukkit.getScheduler().runTask(PasswordChests.getInstance(), () -> {
            Location alreadyPlacedBlock = getOtherChestBlock(newBlock);
            if (alreadyPlacedBlock == null) {
                // no double chest was formed
                return;
            }
            PasswordManager manager = PasswordManager.getInstance();
            if (!manager.hasPassword(alreadyPlacedBlock)) {
                return;
            }
            PasswordManager.getInstance().requestPasswordAssignment(event.getPlayer(), newBlock.getLocation(), alreadyPlacedBlock);
        });

    }

}
