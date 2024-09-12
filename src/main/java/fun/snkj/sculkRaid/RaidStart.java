package fun.snkj.sculkRaid;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class RaidStart implements CommandExecutor, Listener {
    private final Plugin plugin;
    private BossBar bossBar;
    private int raidersCount;
    private int totalRaiders;
    private double progress;

    public RaidStart(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Эта команда доступна только игроку");
            return false;
        }
        Player player = (Player) commandSender;
        Location location = player.getLocation();
        raidersCount = 8;
        totalRaiders = raidersCount;

        bossBar = Bukkit.createBossBar("§fСкалковый рейд", BarColor.BLUE, BarStyle.SEGMENTED_10);
        Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);
        bossBar.setProgress(0.0);
        bossBar.show();
        World wplayer = player.getWorld();
        wplayer.playSound(location, Sound.ENTITY_WARDEN_AGITATED, 1, 0);
        new BukkitRunnable() {
            double progress = 0.0;
            public void run() {
                if (progress >= 1.0) {
                    Random random = new Random();
                    for (int i = 0; i < raidersCount; i++) {
                        Location spawnLocation = location.clone().add(random.nextFloat(-3, 3), 0, random.nextFloat(-3, 3));
                        Zombie raider = (Zombie) player.getWorld().spawnEntity(spawnLocation, EntityType.ZOMBIE);
                        raider.setBaby(true);
                        raider.getEquipment().setBoots(new ItemStack(Material.NETHERITE_BOOTS));
                        raider.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
                        raider.getEquipment().setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
                        raider.getEquipment().setHelmet(new ItemStack(Material.SCULK_SHRIEKER));
                        raider.getEquipment().setItemInMainHand(new ItemStack(Material.ECHO_SHARD));
                        raider.setCustomName("§3Странник");
                        raider.setCustomNameVisible(true);
                        raider.getPotionEffect(PotionEffectType.GLOWING);

                        PersistentDataContainer data = raider.getPersistentDataContainer();
                        NamespacedKey key = new NamespacedKey(plugin, "sculkraid");
                        data.set(key, PersistentDataType.STRING, "sculkraider");
                        World world = raider.getWorld();
                        world.spawnParticle(Particle.SHRIEK, raider.getLocation().add(0,0.5,0), 5, 0, 1, 0, 0.1, 1);
                    world.playSound(location, Sound.ITEM_TRIDENT_THUNDER, 3, 0.7f);
                    world.playSound(location, Sound.ENTITY_WARDEN_ROAR, 1, 0.8f);
                    }
                    cancel();
                } else {
                    progress += 0.005;
                    bossBar.setProgress(progress);
                }
            }
        }.runTaskTimer(plugin, 0, 1);
        return true;
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (bossBar != null)  {
            bossBar.addPlayer(event.getPlayer());
        }
    }
    @EventHandler
    private void dealDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        PersistentDataContainer data = damager.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "sculkraid");
        if (!data.has(key)) return;
        if (!data.get(key, PersistentDataType.STRING).equals("sculkraider")) return;
        Entity entity = event.getEntity();
        World world = entity.getWorld();
        world.spawnParticle(Particle.SCULK_CHARGE_POP, entity.getLocation().add(0,2,0), 20, 0f, 0f, 0f, 0.05f);
    }
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Zombie && "§3Странник".equals(event.getEntity().getCustomName())) {
            raidersCount--;
            event.getDrops().clear();
            bossBar.setProgress((double) raidersCount / totalRaiders);
            Entity damager = event.getEntity();
            PersistentDataContainer data = damager.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "sculkraid");
            if (!data.has(key)) return;
            if (!data.get(key, PersistentDataType.STRING).equals("sculkraider")) return;
            Entity entity = event.getEntity();
            World world = entity.getWorld();
            world.spawnParticle(Particle.SCULK_SOUL, entity.getLocation().add(0,1,0), 10, 0, 0, 0, 0.01);
            if (raidersCount <= 3) {
                bossBar.setTitle("§fСкалковый рейд - Осталось нападающих: " + raidersCount);
            }
            if (raidersCount == 0) {
                bossBar.setTitle("§fСкалковый рейд - Победа");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        bossBar.removeAll();
                        bossBar = null;
                    }
                }.runTaskLater(plugin, 100);
            }
        }
    }
}