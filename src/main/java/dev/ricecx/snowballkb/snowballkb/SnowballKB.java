package dev.ricecx.snowballkb.snowballkb;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class SnowballKB extends JavaPlugin implements Listener {
    public static SnowballKB plugin;
    public static FileConfiguration config;
    public final Table<Player, String, Long> cooldowns = HashBasedTable.create();

    public void onEnable() {
        SnowballKB.plugin = this;
        SnowballKB.config = this.getConfig();
        SnowballKB.config.options().copyDefaults(true);
        this.saveConfig();
        this.getCommand("reloadconfig").setExecutor(this);
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
    }

    public void reloadConfiguration() {
        this.reloadConfig();
        SnowballKB.config = this.getConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
            reloadConfiguration();
            sender.sendMessage(ChatColor.GREEN + "[SnowballKB] Config has been reloaded!");
        } catch (NullPointerException e) {
            sender.sendMessage(ChatColor.DARK_RED + "[SnowballKB] Error! Check console for more details.");
        }
        return true;
    }

    @EventHandler
    public void onEntityDamageEntity(final EntityDamageByEntityEvent event) {

        final boolean isCooldown = SnowballKB.config.getBoolean("cooldown.enabled");
        final int cooldownTime = SnowballKB.config.getInt("cooldown.timer");
        final boolean cooldownMsgEnabled = SnowballKB.config.getBoolean("cooldown.message_config.enabled");
        final String cooldownString = SnowballKB.config.getString("cooldown.message_config.title");
        final double verticalModifier = SnowballKB.config.getDouble("kbModifier.vertical");
        final double horizontalModifier = SnowballKB.config.getDouble("kbModifier.horizontal");

        if (event.getDamager().getType() == EntityType.SNOWBALL && event.getEntity() instanceof Player) {
            final Player player = (Player) ((Projectile) event.getDamager()).getShooter();
            if (isCooldown) {
                if (this.cooldowns.contains(player, "cooldown")) {
                    long secondsLeft = ((this.cooldowns.get(player, "cooldown") / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
                    if (secondsLeft > 0) {
                        if (cooldownMsgEnabled)
                            player.sendMessage(cooldownString);
                        event.setCancelled(true);
                        return;
                    } else {
                        this.cooldowns.remove(player, "cooldown");
                    }
                }
                this.cooldowns.put(player, "cooldown", System.currentTimeMillis());
            }
            // if no cooldown, continue
            Bukkit.getScheduler().scheduleSyncDelayedTask(SnowballKB.plugin, () -> {
                final Player player1 = (Player) event.getEntity();
                final Vector plrV = player1.getVelocity();
                final Vector velocity = new Vector(plrV.getX() * horizontalModifier, plrV.getY() * verticalModifier, plrV.getZ() * horizontalModifier);
                player1.setVelocity(velocity);
            }, 0L);

        }
    }
}
