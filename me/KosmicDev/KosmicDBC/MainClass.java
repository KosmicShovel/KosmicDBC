package me.KosmicDev.KosmicDBC;

import net.minecraft.server.v1_7_R4.EnumChatFormat;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityDestroy;
import org.apache.commons.lang.math.IntRange;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftSnowball;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;

import java.util.*;

public class MainClass extends JavaPlugin implements Listener {
    public static HashMap<String, Integer> playerHPs = new HashMap<>();
    public static Map<String, String> projectileUUID = new HashMap<>();
    public static WeakHashMap<Entity, EntityData> shotprojectiledata = new WeakHashMap<>();
    public static ArrayList<Transformations> transformationsLoaded = new ArrayList<>();
    public static Map<String, Transformations> playersState = new HashMap<>();
    static Random rand = new Random();
    static Thread MainLogic;
    static boolean shouldRun;
    static Long cachedTime;

    public static void initThread() {
        try {
            MainLogic = new Thread(() -> {
                while (true) {
                    if (shouldRun) {
                        for (Player player : Bukkit.getServer().getOnlinePlayers()) {

                            String statusEff;
                            try {
                                statusEff = NBT.GetString(player, "jrmcStatusEff");
                            } catch (Exception e) {
                                statusEff = "";
                            }

                            for (Transformations state : transformationsLoaded) {
                                if (state.id == 0) {
                                    continue;
                                }
                                transformationsLoaded.get(0).functions.run(player);
                            }

                            Boolean transformedMUI = NBT.GetInt(player, "kState") == 1;
                            Boolean transformedGOD = NBT.GetInt(player, "kState") == 2;

                            if (transformedMUI) {
                                transformationsLoaded.get(0).functions.run(player);
                            }

                            if (transformedGOD) {
                                transformationsLoaded.get(1).functions.run(player);
                            }

                            if (cachedTime == null || cachedTime == 0) {
                                cachedTime = System.currentTimeMillis();
                            }

                            if (cachedTime >= System.currentTimeMillis()) {
                                if (statusEff.contains("A") && NBT.GetInt(player, "jrmcRelease") >= 100 && NBT.GetInt(player, "jrmcRelease") < 125
                                        && NBT.GetString(player, "jrmcSSlts").contains("UI")) {
                                    NBT.SetInt(player, "jrmcRelease", NBT.GetInt(player, "jrmcRelease") + 5);
                                }
                                cachedTime = System.currentTimeMillis() + 1500;
                            }

                            if (NBT.GetInt(player, "jrmcSaiRg") >= 80) {
                                if (Yamls.PlayerData.getPlayerData().getBoolean("PlayerData." + player.getUniqueId() + ".MUI.Active")) {
                                    Transform(player);
                                    Yamls.PlayerData.getPlayerData().set("PlayerData." + player.getUniqueId() + ".MUI.Transformed", true);
                                    Yamls.PlayerData.savePlayerData();

                                    String dns = NBT.GetString(player, "jrmcDNS");
                                    NBT.SetString(player, "jrmcDNS", Methods.dnsHairCSet(dns, 0xf0ffff));
                                }

                                if (Yamls.PlayerData.getPlayerData().getBoolean("PlayerData." + player.getUniqueId() + ".GOD.Active")) {
                                    Transform(player);
                                    Yamls.PlayerData.getPlayerData().set("PlayerData." + player.getUniqueId() + ".GOD.Transformed", true);
                                    Yamls.PlayerData.savePlayerData();

                                    String dns = NBT.GetString(player, "jrmcDNS");
                                    dns = Methods.dnsEyeC1Set(dns, 0x6600ff);
                                    dns = Methods.dnsEyeC2Set(dns, 0x6600ff);
                                    dns = Methods.dnsHairCSet(dns, 0x9500ff);

                                    NBT.SetInt(player, "jrmcAura", 0x9500ff);
                                    NBT.SetString(player, "jrmcDNS", dns);
                                }
                            }

                            if (transformedGOD) {
                                String dns = NBT.GetString(player, "jrmcDNS");
                                dns = Methods.dnsEyeC1Set(dns, ColorManager.GODColorsE.get(player.getTicksLived() % ColorManager.GODColorsE.size()));
                                dns = Methods.dnsEyeC2Set(dns, ColorManager.GODColorsE.get(player.getTicksLived() % ColorManager.GODColorsE.size()));
                                dns = Methods.dnsHairCSet(dns, ColorManager.GODColorsH.get(player.getTicksLived() % ColorManager.GODColorsH.size()));

                                NBT.SetString(player, "jrmcDNS", dns);

                                if (!statusEff.contains("V")) {
                                    NBT.SetString(player, "jrmcStatusEff", statusEff + "V");
                                }

                                final NBTTagCompound nbtTagCompound = new NBTTagCompound();
                                ((CraftPlayer) player).getHandle().e(nbtTagCompound);
                                final Integer a2 = nbtTagCompound.getCompound("JRMCEP").getInt("blocking");
                                if (a2 >= 1 && player.getInventory().getHeldItemSlot() == 8) {
                                    if (player.hasPotionEffect(PotionEffectType.WATER_BREATHING)) {
                                        break;
                                    }
                                    if (!player.getInventory().getItemInHand().getType().equals((Object) Material.AIR)) {
                                        break;
                                    }

                                    final Snowball projectile = player.launchProjectile(Snowball.class);
                                    final String snowballUUID = projectile.getUniqueId().toString();
                                    projectileUUID.put(snowballUUID, player.getName());
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 120, 9));
                                    NBT.SetDouble(player, "jrmcEnrgy", NBT.GetInt(player, "jrmcEnrgy") - 50000);
                                    final EntityData data = new EntityData(projectile.getLocation(), 20, NBT.GetInt(player, "jrmcWilI") * 10 * 7 * 2.85);
                                    shotprojectiledata.put(projectile, data);
                                    final Location location = player.getEyeLocation();
                                    final BlockIterator blocksToAdd = new BlockIterator(location, 0.0, 35);
                                    while (blocksToAdd.hasNext()) {
                                        final Location blockToAdd = blocksToAdd.next().getLocation();
                                        if (blockToAdd.getBlock().getType() != Material.AIR) {
                                            break;
                                        }
                                        if (!shotprojectiledata.containsKey(projectile)) {
                                            break;
                                        }
                                        player.getWorld().playEffect(blockToAdd, Effect.STEP_SOUND, (Object) Material.PORTAL);
                                    }
                                    final List<Entity> near = player.getNearbyEntities(16.0, 16.0, 16.0);
                                    for (int io464 = 0; io464 < near.size(); ++io464) {
                                        if (near.get(io464) instanceof Player) {
                                            final Player p = (Player) near.get(io464);
                                            ((CraftPlayer) p).getHandle().playerConnection
                                                    .sendPacket(new PacketPlayOutEntityDestroy(new int[]{((CraftSnowball) projectile).getHandle().getId()}));
                                            p.playSound(player.getLocation(), "jinryuudragonbc:DBC2.blast", 1.0f, 1.0f);
                                        }
                                    }
                                    break;
                                }
                            }

                            if (player.getGameMode() != GameMode.CREATIVE) {
                                int bdy = NBT.GetInt(player, "jrmcBdy");
                                if (transformedMUI || transformedGOD) {
                                    if (!MainClass.playerHPs.containsKey(player.getName())) {
                                        MainClass.playerHPs.put(player.getName(), bdy);
                                    } else {
                                        if (bdy < MainClass.playerHPs.get(player.getName())) {
                                            int damage = MainClass.playerHPs.get(player.getName()) - NBT.GetInt(player, "jrmcBdy");
                                            double x2 = damage * 0.5;
                                            IntRange x3 = new IntRange(0, ((NBT.GetInt(player, "jrmcDexI") / 2000 + 50) * 0.3 - 0.01));
                                            int x4 = MainClass.rand.nextInt(100);
                                            if (x3.containsInteger(x4) && transformedMUI) {
                                                NBT.SetInt(player, "jrmcBdy", MainClass.playerHPs.get(player.getName()));
                                                if (damage > 500) {
                                                    player.sendMessage("" + ChatColor.GRAY + ChatColor.ITALIC + "Attack Dodged");
                                                }
                                            } else {
                                                NBT.SetInt(player, "jrmcBdy", (int) (MainClass.playerHPs.get(player.getName()) - x2));
                                            }
                                            MainClass.playerHPs.remove(player.getName());
                                            MainClass.playerHPs.put(player.getName(), NBT.GetInt(player, "jrmcBdy"));
                                        }
                                        MainClass.playerHPs.remove(player.getName());
                                        MainClass.playerHPs.put(player.getName(), NBT.GetInt(player, "jrmcBdy"));
                                    }
                                } else {
                                    MainClass.playerHPs.remove(player.getName());
                                    MainClass.playerHPs.put(player.getName(), bdy);
                                }
                            }

                            if (transformedMUI) {
                                if (NBT.GetInt(player, "jrmcEf8slc") > 0) {
                                    NBT.SetInt(player, "jrmcEf8slc", 0);
                                }

                                if (!statusEff.contains("N")) {
                                    NBT.SetString(player, "jrmcStatusEff", statusEff + "N");
                                }
                                if (!statusEff.contains("V")) {
                                    NBT.SetString(player, "jrmcStatusEff", statusEff + "V");
                                }
                                if (!statusEff.contains("B")) {
                                    NBT.SetString(player, "jrmcStatusEff", statusEff + "B");
                                }
                            }
                        }
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, "KosmicDBC-MainLogic");

            MainLogic.setDaemon(true);
            MainLogic.setPriority(5);
            MainLogic.start();
        } catch (ConcurrentModificationException e) {
        }
    }

    private static void Transform(Player player) {
        NBT.SetInt(player, "jrmcSaiRg", 0);

        Yamls.PlayerData.getPlayerData().set("PlayerData." + player.getUniqueId() + ".DNS", NBT.GetString(player, "jrmcDNS"));
        Yamls.PlayerData.getPlayerData().set("PlayerData." + player.getUniqueId() + ".StatusEff", NBT.GetString(player, "jrmcStatusEff"));
        Yamls.PlayerData.getPlayerData().set("PlayerData." + player.getUniqueId() + ".State", NBT.GetInt(player, "jrmcState"));
        Yamls.PlayerData.getPlayerData().set("PlayerData." + player.getUniqueId() + ".AuraColor", NBT.GetInt(player, "jrmcAuraColor"));
    }

    public static void openMenu(Player player) {
        Inventory inv = Bukkit.createInventory((InventoryHolder) null, 27, ChatColor.BLUE + "New DBC Forms");
        ItemStack MUI = Methods.createItemStack(Material.STAINED_GLASS_PANE, 0, "" + ChatColor.WHITE + ChatColor.BOLD + "Ultra Instinct ~Mastered~",
                "" + ChatColor.WHITE + ChatColor.BOLD + "TP: " + ChatColor.RESET + ChatColor.WHITE + "1,000,000,000", "" + ChatColor.WHITE + ChatColor.BOLD + "Right Click to Activate/Deactivate");

        if (Yamls.PlayerData.getPlayerData().getBoolean("PlayerData." + player.getUniqueId() + ".MUI.Active")) {
            Methods.addGlow(MUI);
        }

        ItemStack GOD = Methods.createItemStack(Material.STAINED_GLASS_PANE, 10, "" + ChatColor.DARK_PURPLE + "G.O.D",
                ChatColor.DARK_PURPLE + "TP: " + ChatColor.RESET + ChatColor.DARK_PURPLE + "750,000,000", "" + ChatColor.DARK_PURPLE + "Right Click to Activate/Deactivate");

        if (Yamls.PlayerData.getPlayerData().getBoolean("PlayerData." + player.getUniqueId() + ".GOD.Active")) {
            Methods.addGlow(GOD);
        }

        addBorder(inv);
        inv.setItem(11, MUI);
        inv.setItem(15, GOD);
        player.openInventory(inv);
    }

    public static void addBorder(final Inventory menu) {
        ItemStack glass2 = Methods.createItemStack(Material.STAINED_GLASS_PANE, 7, " ", " ");
        ItemStack glass = Methods.createItemStack(Material.STAINED_GLASS_PANE, 15, " ", " ");
        for (int i = 0; i < 27; ++i) {
            if (i % 1 == 0)
                menu.setItem(i, glass);
            else
                menu.setItem(i, glass2);
        }
    }

    public void onEnable() {
        shouldRun = true;
        initThread();
        loadConfigManager();
        getServer().getPluginManager().registerEvents(this, this);
        transformationsLoaded.add(new Transformations("Base", EnumChatFormat.AQUA + "Base", player -> {

        }, 0F, 0F, 0F));

        transformationsLoaded.add(new Transformations("MUI", EnumChatFormat.WHITE + "M.U.I", player -> {
            if (playersState.get(player.getName()).id == 1)
                NBT.SetInt(player, "jrmcEnrgy", NBT.GetInt(player, "jrmcEnrgy") - 5000);
        }, 50000f, 0.8f, 0.8f));
        transformationsLoaded.add(new Transformations("GOD", EnumChatFormat.DARK_PURPLE + "G.O.D", player -> {
            if (playersState.get(player.getName()).id == 2)
                NBT.SetInt(player, "jrmcEnrgy", NBT.GetInt(player, "jrmcEnrgy") - 15000);

        }, 50000f, 0.8f, 0.8f));
    }

    public void onDisable() {
        shouldRun = false;
    }

    public void loadConfigManager() {
        (Yamls.PlayerData = new Yamls()).setup();
        Yamls.PlayerData.savePlayerData();
        Yamls.PlayerData.reloadPlayerData();
    }

    @EventHandler
    public void onHit(final EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Snowball) {
            if (event.getDamager() instanceof Snowball) {
                final String uuid = (event.getDamager()).getUniqueId().toString();
                if (projectileUUID.containsKey(uuid)) {
                    ((Damageable) event.getEntity()).damage(shotprojectiledata.get(event.getDamager()).damage);
                    projectileUUID.remove(uuid);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryCLick(final InventoryClickEvent e) {

        if (e.getInventory().getName().equals(ChatColor.BLUE + "New DBC Forms")) {
            e.setCancelled(true);
            Player player = (Player) e.getWhoClicked();

            // Left Click
            if (e.getClick() == ClickType.LEFT) {
                if (e.getCurrentItem() != null) {
                    if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("" + ChatColor.WHITE + ChatColor.BOLD + "Ultra Instinct ~Mastered~")) {
                        if (Yamls.PlayerData.getPlayerData().getBoolean("PlayerData." + player.getUniqueId() + ".MUI.Bought")) {
                            player.sendMessage(ChatColor.RED + "Already Bought!");
                        } else if (NBT.GetString(player, "jrmcSSlts").contains("UI")) {
                            if (NBT.GetInt(player, "jrmcTpint") >= 1000000000) {
                                NBT.SetInt(player, "jrmcTpint", NBT.GetInt(player, "jrmcTpint") - 1000000000);
                                Yamls.PlayerData.getPlayerData().set("PlayerData." + player.getUniqueId() + ".MUI.Bought", (Object) true);
                                Yamls.PlayerData.savePlayerData();
                                player.sendMessage(ChatColor.AQUA + "You have successfully bought " + ChatColor.BLUE + "Ultra Instinct ~Mastered~" + ChatColor.AQUA + "!");
                                openMenu(player);
                            } else {
                                player.sendMessage(ChatColor.RED + "Insufficient TP!");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "You need Ultra Instinct for this!");
                        }
                    }

                    if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("" + ChatColor.DARK_PURPLE + "G.O.D")) {
                        if (Yamls.PlayerData.getPlayerData().getBoolean("PlayerData." + player.getUniqueId() + ".GOD.Bought")) {
                            player.sendMessage(ChatColor.RED + "Already Bought!");
                        } else if (NBT.GetString(player, "jrmcSSlts").contains("GF")) {
                            if (NBT.GetInt(player, "jrmcTpint") >= 750000000) {
                                NBT.SetInt(player, "jrmcTpint", NBT.GetInt(player, "jrmcTpint") - 750000000);
                                Yamls.PlayerData.getPlayerData().set("PlayerData." + player.getUniqueId() + ".GOD.Bought", (Object) true);
                                Yamls.PlayerData.savePlayerData();
                                player.sendMessage(ChatColor.AQUA + "You have successfully bought " + ChatColor.DARK_PURPLE + "G.O.D" + ChatColor.AQUA + "!");
                                openMenu(player);
                            } else {
                                player.sendMessage(ChatColor.RED + "Insufficient TP!");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "You need God Form for this!");
                        }
                    }
                }
            }

            // Right Click
            if (e.getClick() == ClickType.RIGHT) {
                if (e.getCurrentItem() != null) {
                    if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("" + ChatColor.WHITE + ChatColor.BOLD + "Ultra Instinct ~Mastered~")) {
                        if (Yamls.PlayerData.getPlayerData().getBoolean("PlayerData." + player.getUniqueId() + ".MUI.Bought")) {
                            if (Yamls.PlayerData.getPlayerData().getBoolean("PlayerData." + player.getUniqueId() + ".MUI.Active")
                                    || Yamls.PlayerData.getPlayerData().getBoolean("PlayerData." + player.getUniqueId() + ".HasActiveForm")) {
                                player.sendMessage(ChatColor.RED + "You have deselected " + Yamls.PlayerData.getPlayerData().getString("PlayerData." + player.getUniqueId() + ".ActiveForm") + "!");
                                Yamls.PlayerData.getPlayerData().set("PlayerData." + player.getUniqueId() + ".MUI.Active", false);
                                Yamls.PlayerData.getPlayerData().set("PlayerData." + player.getUniqueId() + ".MUI.Transformed", false);
                                Yamls.PlayerData.getPlayerData().set("PlayerData." + player.getUniqueId() + ".HasActiveForm", false);
                                Yamls.PlayerData.getPlayerData().set("PlayerData." + player.getUniqueId() + ".ActiveForm", "");
                                NBT.SetInt(player, "jrmcState", Yamls.PlayerData.getPlayerData().getInt("PlayerData." + player.getUniqueId() + ".State"));
                                NBT.SetInt(player, "jrmcAuraColor", Yamls.PlayerData.getPlayerData().getInt("PlayerData." + player.getUniqueId() + ".AuraColor"));
                                NBT.SetString(player, "jrmcStatusEff",
                                        Yamls.PlayerData.getPlayerData().getString("PlayerData." + player.getUniqueId() + ".StatusEff").replace("G", "").replace("R", ""));
                                NBT.SetString(player, "jrmcDNS", Yamls.PlayerData.getPlayerData().getString("PlayerData." + player.getUniqueId() + ".DNS"));
                                Yamls.PlayerData.savePlayerData();
                                openMenu(player);
                            } else {
                                Yamls.PlayerData.getPlayerData().set("PlayerData." + player.getUniqueId() + ".HasActiveForm", true);
                                Yamls.PlayerData.getPlayerData().set("PlayerData." + player.getUniqueId() + ".ActiveForm", "Ultra Instinct ~Mastered~");
                                Yamls.PlayerData.getPlayerData().set("PlayerData." + player.getUniqueId() + ".MUI.Active", true);
                                Yamls.PlayerData.savePlayerData();
                                player.sendMessage(ChatColor.GREEN + "You have selected Ultra Instinct ~Mastered~!");

                                //Gamer

                                openMenu(player);
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "You need to have bought it first!");
                        }
                    }

                    if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("" + ChatColor.DARK_PURPLE + "G.O.D")) {
                        if (Yamls.PlayerData.getPlayerData().getBoolean("PlayerData." + player.getUniqueId() + ".GOD.Bought")) {
                            if (Yamls.PlayerData.getPlayerData().getBoolean("PlayerData." + player.getUniqueId() + ".GOD.Active")
                                    || Yamls.PlayerData.getPlayerData().getBoolean("PlayerData." + player.getUniqueId() + ".HasActiveForm")) {
                                player.sendMessage(ChatColor.RED + "You have deselected " + Yamls.PlayerData.getPlayerData().getString("PlayerData." + player.getUniqueId() + ".ActiveForm") + "!");
                                Yamls.PlayerData.getPlayerData().set("PlayerData." + player.getUniqueId() + ".GOD.Active", false);
                                Yamls.PlayerData.getPlayerData().set("PlayerData." + player.getUniqueId() + ".GOD.Transformed", false);
                                Yamls.PlayerData.getPlayerData().set("PlayerData." + player.getUniqueId() + ".HasActiveForm", false);
                                Yamls.PlayerData.getPlayerData().set("PlayerData." + player.getUniqueId() + ".ActiveForm", "");
                                NBT.SetInt(player, "jrmcState", Yamls.PlayerData.getPlayerData().getInt("PlayerData." + player.getUniqueId() + ".State"));
                                NBT.SetInt(player, "jrmcAuraColor", Yamls.PlayerData.getPlayerData().getInt("PlayerData." + player.getUniqueId() + ".AuraColor"));
                                NBT.SetString(player, "jrmcStatusEff",
                                        Yamls.PlayerData.getPlayerData().getString("PlayerData." + player.getUniqueId() + ".StatusEff").replace("G", "").replace("R", ""));
                                NBT.SetString(player, "jrmcDNS", Yamls.PlayerData.getPlayerData().getString("PlayerData." + player.getUniqueId() + ".DNS"));
                                Yamls.PlayerData.savePlayerData();
                                openMenu(player);
                            } else {
                                Yamls.PlayerData.getPlayerData().set("PlayerData." + player.getUniqueId() + ".HasActiveForm", true);
                                Yamls.PlayerData.getPlayerData().set("PlayerData." + player.getUniqueId() + ".ActiveForm", "G.O.D");
                                Yamls.PlayerData.getPlayerData().set("PlayerData." + player.getUniqueId() + ".GOD.Active", true);
                                Yamls.PlayerData.savePlayerData();
                                player.sendMessage(ChatColor.GREEN + "You have selected G.O.D!");
                                openMenu(player);
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "You need to have bought it first!");
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onAsyncPlayerChat(final AsyncPlayerChatEvent e) {
        if (e.getMessage().toLowerCase().equals("?kmenu") || e.getMessage().toLowerCase().equals("?km")) {
            e.setCancelled(true);
            openMenu(e.getPlayer());
        }
        if (e.getMessage().toLowerCase().contains("@everyone") && e.getPlayer().isOp()) {
            e.setMessage(Methods.lowerCaseString(e.getMessage(), "@everyone").replaceAll("@everyone",
                    new StringBuilder().append(ChatColor.BLUE).append(ChatColor.UNDERLINE).append("@everyone").append(ChatColor.RESET).toString()));
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                if (player.isOnline() && !player.getPlayer().getName().equals(e.getPlayer().getName())) {
                    player.sendMessage(new StringBuilder().append(ChatColor.BLUE).append(ChatColor.BOLD).append("You are mentioned by ").append(ChatColor.YELLOW).append(ChatColor.BOLD)
                            .append(e.getPlayer().getName()).append(ChatColor.BLUE).append(ChatColor.BOLD).append("!").toString());
                    player.playNote(e.getPlayer().getLocation(), (byte) 4, (byte) 7);
                    player.playNote(e.getPlayer().getLocation(), (byte) 4, (byte) 10);
                }
            }
        }
        if (e.getMessage().toLowerCase().contains("@here") && e.getPlayer().isOp()) {
            e.setMessage(Methods.lowerCaseString(e.getMessage(), "@here").replaceAll("@here",
                    new StringBuilder().append(ChatColor.BLUE).append(ChatColor.UNDERLINE).append("@here").append(ChatColor.RESET).toString()));
            for (Player player2 : Bukkit.getServer().getOnlinePlayers()) {
                if (!player2.getPlayer().getName().equals(e.getPlayer().getName())) {
                    player2.sendMessage(new StringBuilder().append(ChatColor.BLUE).append(ChatColor.BOLD).append("You are mentioned by ").append(ChatColor.YELLOW).append(ChatColor.BOLD)
                            .append(e.getPlayer().getName()).append(ChatColor.BLUE).append(ChatColor.BOLD).append("!").toString());
                    player2.playNote(e.getPlayer().getLocation(), (byte) 4, (byte) 7);
                    player2.playNote(e.getPlayer().getLocation(), (byte) 4, (byte) 10);
                }
            }
        }
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (e.getMessage().toLowerCase().contains("@" + player.getName().toLowerCase())) {
                e.setMessage(Methods.lowerCaseString(e.getMessage(), "@" + player.getName()).replaceAll("@" + player.getName(),
                        new StringBuilder().append(ChatColor.BLUE).append(ChatColor.UNDERLINE).append("@").append(player.getName()).append(ChatColor.RESET).toString()));
                if (player.isOnline() && !player.getPlayer().getName().equals(e.getPlayer().getName())) {
                    player.sendMessage(new StringBuilder().append(ChatColor.BLUE).append(ChatColor.BOLD).append("You are mentioned by ").append(ChatColor.YELLOW).append(ChatColor.BOLD)
                            .append(e.getPlayer().getName()).append(ChatColor.BLUE).append(ChatColor.BOLD).append("!").toString());
                    player.playNote(e.getPlayer().getLocation(), (byte) 4, (byte) 7);
                    player.playNote(e.getPlayer().getLocation(), (byte) 4, (byte) 10);
                }
            }
        }
    }

    public static class EntityData {
        private Location firedfrom;
        private Integer range;
        private Double damage;

        public EntityData(final Location loc, final Integer range, final Double damage) {
            this.firedfrom = loc;
            this.range = range;
            this.damage = damage;
        }

        public Location getFiredFrom() {
            return this.firedfrom;
        }

        public Integer getRange() {
            return this.range;
        }

        public Double getDamage() {
            return this.damage;
        }
    }

    public static class ColorManager {
        public static ArrayList<Integer> GODColorsH = new ArrayList<>();
        public static ArrayList<Integer> GODColorsE = new ArrayList<>();

        static {
            GODColorsE.add(0x0000ff);
            GODColorsE.add(0x0000ff);
            GODColorsE.add(0x0001ff);
            GODColorsE.add(0x0001ff);
            GODColorsE.add(0x0002ff);
            GODColorsE.add(0x0002ff);
            GODColorsE.add(0x0003ff);
            GODColorsE.add(0x0003ff);
            GODColorsE.add(0x0004ff);
            GODColorsE.add(0x0004ff);
            GODColorsE.add(0x0005ff);
            GODColorsE.add(0x0005ff);
            GODColorsE.add(0x0006ff);
            GODColorsE.add(0x0006ff);
            GODColorsE.add(0x0007ff);
            GODColorsE.add(0x0007ff);
            GODColorsE.add(0x0008ff);
            GODColorsE.add(0x0008ff);
            GODColorsE.add(0x0009ff);
            GODColorsE.add(0x0009ff);
            GODColorsE.add(0x000aff);
            GODColorsE.add(0x000aff);
            GODColorsE.add(0x000bff);
            GODColorsE.add(0x000bff);
            GODColorsE.add(0x000cff);
            GODColorsE.add(0x000cff);
            GODColorsE.add(0x000dff);
            GODColorsE.add(0x000dff);
            GODColorsE.add(0x000eff);
            GODColorsE.add(0x000eff);
            GODColorsE.add(0x000fff);
            GODColorsE.add(0x000fff);
            GODColorsE.add(0x0010ff);
            GODColorsE.add(0x0010ff);
            GODColorsE.add(0x0011ff);
            GODColorsE.add(0x0011ff);
            GODColorsE.add(0x0012ff);
            GODColorsE.add(0x0012ff);
            GODColorsE.add(0x0013ff);
            GODColorsE.add(0x0013ff);
            GODColorsE.add(0x0014ff);
            GODColorsE.add(0x0014ff);
            GODColorsE.add(0x0015ff);
            GODColorsE.add(0x0015ff);
            GODColorsE.add(0x0016ff);
            GODColorsE.add(0x0016ff);
            GODColorsE.add(0x0017ff);
            GODColorsE.add(0x0017ff);
            GODColorsE.add(0x0018ff);
            GODColorsE.add(0x0018ff);
            GODColorsE.add(0x0019ff);
            GODColorsE.add(0x0019ff);
            GODColorsE.add(0x001aff);
            GODColorsE.add(0x001aff);
            GODColorsE.add(0x001bff);
            GODColorsE.add(0x001bff);
            GODColorsE.add(0x001cff);
            GODColorsE.add(0x001cff);
            GODColorsE.add(0x001dff);
            GODColorsE.add(0x001dff);
            GODColorsE.add(0x001eff);
            GODColorsE.add(0x001eff);
            GODColorsE.add(0x001fff);
            GODColorsE.add(0x001fff);
            GODColorsE.add(0x0020ff);
            GODColorsE.add(0x0020ff);
            GODColorsE.add(0x0021ff);
            GODColorsE.add(0x0021ff);
            GODColorsE.add(0x0022ff);
            GODColorsE.add(0x0022ff);
            GODColorsE.add(0x0023ff);
            GODColorsE.add(0x0023ff);
            GODColorsE.add(0x0024ff);
            GODColorsE.add(0x0024ff);
            GODColorsE.add(0x0025ff);
            GODColorsE.add(0x0025ff);
            GODColorsE.add(0x0026ff);
            GODColorsE.add(0x0026ff);
            GODColorsE.add(0x0027ff);
            GODColorsE.add(0x0027ff);
            GODColorsE.add(0x0028ff);
            GODColorsE.add(0x0028ff);
            GODColorsE.add(0x0029ff);
            GODColorsE.add(0x0029ff);
            GODColorsE.add(0x002aff);
            GODColorsE.add(0x002aff);
            GODColorsE.add(0x002bff);
            GODColorsE.add(0x002bff);
            GODColorsE.add(0x002cff);
            GODColorsE.add(0x002cff);
            GODColorsE.add(0x002dff);
            GODColorsE.add(0x002dff);
            GODColorsE.add(0x002eff);
            GODColorsE.add(0x002eff);
            GODColorsE.add(0x002fff);
            GODColorsE.add(0x002fff);
            GODColorsE.add(0x0030ff);
            GODColorsE.add(0x0030ff);
            GODColorsE.add(0x0031ff);
            GODColorsE.add(0x0031ff);
            GODColorsE.add(0x0032ff);
            GODColorsE.add(0x0032ff);
            GODColorsE.add(0x0033ff);
            GODColorsE.add(0x0033ff);
            GODColorsE.add(0x0034ff);
            GODColorsE.add(0x0034ff);
            GODColorsE.add(0x0035ff);
            GODColorsE.add(0x0035ff);
            GODColorsE.add(0x0036ff);
            GODColorsE.add(0x0036ff);
            GODColorsE.add(0x0037ff);
            GODColorsE.add(0x0037ff);
            GODColorsE.add(0x0038ff);
            GODColorsE.add(0x0038ff);
            GODColorsE.add(0x0039ff);
            GODColorsE.add(0x0039ff);
            GODColorsE.add(0x003aff);
            GODColorsE.add(0x003aff);
            GODColorsE.add(0x003bff);
            GODColorsE.add(0x003bff);
            GODColorsE.add(0x003cff);
            GODColorsE.add(0x003cff);
            GODColorsE.add(0x003dff);
            GODColorsE.add(0x003dff);
            GODColorsE.add(0x003eff);
            GODColorsE.add(0x003eff);
            GODColorsE.add(0x003fff);
            GODColorsE.add(0x003fff);
            GODColorsE.add(0x0040ff);
            GODColorsE.add(0x0040ff);
            GODColorsE.add(0x0041ff);
            GODColorsE.add(0x0041ff);
            GODColorsE.add(0x0042ff);
            GODColorsE.add(0x0042ff);
            GODColorsE.add(0x0043ff);
            GODColorsE.add(0x0043ff);
            GODColorsE.add(0x0044ff);
            GODColorsE.add(0x0044ff);
            GODColorsE.add(0x0045ff);
            GODColorsE.add(0x0045ff);
            GODColorsE.add(0x0046ff);
            GODColorsE.add(0x0046ff);
            GODColorsE.add(0x0047ff);
            GODColorsE.add(0x0047ff);
            GODColorsE.add(0x0048ff);
            GODColorsE.add(0x0048ff);
            GODColorsE.add(0x0049ff);
            GODColorsE.add(0x0049ff);
            GODColorsE.add(0x004aff);
            GODColorsE.add(0x004aff);
            GODColorsE.add(0x004bff);
            GODColorsE.add(0x004bff);
            GODColorsE.add(0x004cff);
            GODColorsE.add(0x004cff);
            GODColorsE.add(0x004dff);
            GODColorsE.add(0x004dff);
            GODColorsE.add(0x004eff);
            GODColorsE.add(0x004eff);
            GODColorsE.add(0x004fff);
            GODColorsE.add(0x004fff);
            GODColorsE.add(0x0050ff);
            GODColorsE.add(0x0050ff);
            GODColorsE.add(0x0051ff);
            GODColorsE.add(0x0051ff);
            GODColorsE.add(0x0052ff);
            GODColorsE.add(0x0052ff);
            GODColorsE.add(0x0053ff);
            GODColorsE.add(0x0053ff);
            GODColorsE.add(0x0054ff);
            GODColorsE.add(0x0054ff);
            GODColorsE.add(0x0055ff);
            GODColorsE.add(0x0055ff);
            GODColorsE.add(0x0056ff);
            GODColorsE.add(0x0056ff);
            GODColorsE.add(0x0057ff);
            GODColorsE.add(0x0057ff);
            GODColorsE.add(0x0058ff);
            GODColorsE.add(0x0058ff);
            GODColorsE.add(0x0059ff);
            GODColorsE.add(0x0059ff);
            GODColorsE.add(0x005aff);
            GODColorsE.add(0x005aff);
            GODColorsE.add(0x005bff);
            GODColorsE.add(0x005bff);
            GODColorsE.add(0x005cff);
            GODColorsE.add(0x005cff);
            GODColorsE.add(0x005dff);
            GODColorsE.add(0x005dff);
            GODColorsE.add(0x005eff);
            GODColorsE.add(0x005eff);
            GODColorsE.add(0x005fff);
            GODColorsE.add(0x005fff);
            GODColorsE.add(0x0060ff);
            GODColorsE.add(0x0060ff);
            GODColorsE.add(0x0061ff);
            GODColorsE.add(0x0061ff);
            GODColorsE.add(0x0062ff);
            GODColorsE.add(0x0062ff);
            GODColorsE.add(0x0063ff);
            GODColorsE.add(0x0063ff);
            GODColorsE.add(0x0064ff);
            GODColorsE.add(0x0064ff);
            GODColorsE.add(0x0065ff);
            GODColorsE.add(0x0065ff);
            GODColorsE.add(0x0066ff);
            GODColorsE.add(0x0066ff);
            GODColorsE.add(0x0067ff);
            GODColorsE.add(0x0067ff);
            GODColorsE.add(0x0068ff);
            GODColorsE.add(0x0068ff);
            GODColorsE.add(0x0069ff);
            GODColorsE.add(0x0069ff);
            GODColorsE.add(0x006aff);
            GODColorsE.add(0x006aff);
            GODColorsE.add(0x006bff);
            GODColorsE.add(0x006bff);
            GODColorsE.add(0x006cff);
            GODColorsE.add(0x006cff);
            GODColorsE.add(0x006dff);
            GODColorsE.add(0x006dff);
            GODColorsE.add(0x006eff);
            GODColorsE.add(0x006eff);
            GODColorsE.add(0x006fff);
            GODColorsE.add(0x006fff);
            GODColorsE.add(0x0070ff);
            GODColorsE.add(0x0070ff);
            GODColorsE.add(0x0071ff);
            GODColorsE.add(0x0071ff);
            GODColorsE.add(0x0072ff);
            GODColorsE.add(0x0072ff);
            GODColorsE.add(0x0073ff);
            GODColorsE.add(0x0073ff);
            GODColorsE.add(0x0074ff);
            GODColorsE.add(0x0074ff);
            GODColorsE.add(0x0075ff);
            GODColorsE.add(0x0075ff);
            GODColorsE.add(0x0076ff);
            GODColorsE.add(0x0076ff);
            GODColorsE.add(0x0077ff);
            GODColorsE.add(0x0077ff);
            GODColorsE.add(0x0078ff);
            GODColorsE.add(0x0078ff);
            GODColorsE.add(0x0079ff);
            GODColorsE.add(0x0079ff);
            GODColorsE.add(0x007aff);
            GODColorsE.add(0x007aff);
            GODColorsE.add(0x007bff);
            GODColorsE.add(0x007bff);
            GODColorsE.add(0x007cff);
            GODColorsE.add(0x007cff);
            GODColorsE.add(0x007dff);
            GODColorsE.add(0x007dff);
            GODColorsE.add(0x007eff);
            GODColorsE.add(0x007eff);
            GODColorsE.add(0x007fff);
            GODColorsE.add(0x007fff);
            GODColorsE.add(0x0080ff);
            GODColorsE.add(0x0080ff);
            GODColorsE.add(0x0081ff);
            GODColorsE.add(0x0081ff);
            GODColorsE.add(0x0082ff);
            GODColorsE.add(0x0082ff);
            GODColorsE.add(0x0083ff);
            GODColorsE.add(0x0083ff);
            GODColorsE.add(0x0084ff);
            GODColorsE.add(0x0084ff);
            GODColorsE.add(0x0085ff);
            GODColorsE.add(0x0085ff);
            GODColorsE.add(0x0086ff);
            GODColorsE.add(0x0086ff);
            GODColorsE.add(0x0087ff);
            GODColorsE.add(0x0087ff);
            GODColorsE.add(0x0088ff);
            GODColorsE.add(0x0088ff);
            GODColorsE.add(0x0089ff);
            GODColorsE.add(0x0089ff);
            GODColorsE.add(0x008aff);
            GODColorsE.add(0x008aff);
            GODColorsE.add(0x008bff);
            GODColorsE.add(0x008bff);
            GODColorsE.add(0x008cff);
            GODColorsE.add(0x008cff);
            GODColorsE.add(0x008dff);
            GODColorsE.add(0x008dff);
            GODColorsE.add(0x008eff);
            GODColorsE.add(0x008eff);
            GODColorsE.add(0x008fff);
            GODColorsE.add(0x008fff);

            GODColorsE.add(0x008fff);

            GODColorsE.add(0x008fff);
            GODColorsE.add(0x008fff);
            GODColorsE.add(0x008eff);
            GODColorsE.add(0x008eff);
            GODColorsE.add(0x008dff);
            GODColorsE.add(0x008dff);
            GODColorsE.add(0x008cff);
            GODColorsE.add(0x008cff);
            GODColorsE.add(0x008bff);
            GODColorsE.add(0x008bff);
            GODColorsE.add(0x008aff);
            GODColorsE.add(0x008aff);
            GODColorsE.add(0x0089ff);
            GODColorsE.add(0x0089ff);
            GODColorsE.add(0x0088ff);
            GODColorsE.add(0x0088ff);
            GODColorsE.add(0x0087ff);
            GODColorsE.add(0x0087ff);
            GODColorsE.add(0x0086ff);
            GODColorsE.add(0x0086ff);
            GODColorsE.add(0x0085ff);
            GODColorsE.add(0x0085ff);
            GODColorsE.add(0x0084ff);
            GODColorsE.add(0x0084ff);
            GODColorsE.add(0x0083ff);
            GODColorsE.add(0x0083ff);
            GODColorsE.add(0x0082ff);
            GODColorsE.add(0x0082ff);
            GODColorsE.add(0x0081ff);
            GODColorsE.add(0x0081ff);
            GODColorsE.add(0x0080ff);
            GODColorsE.add(0x0080ff);
            GODColorsE.add(0x007fff);
            GODColorsE.add(0x007fff);
            GODColorsE.add(0x007eff);
            GODColorsE.add(0x007eff);
            GODColorsE.add(0x007dff);
            GODColorsE.add(0x007dff);
            GODColorsE.add(0x007cff);
            GODColorsE.add(0x007cff);
            GODColorsE.add(0x007bff);
            GODColorsE.add(0x007bff);
            GODColorsE.add(0x007aff);
            GODColorsE.add(0x007aff);
            GODColorsE.add(0x0079ff);
            GODColorsE.add(0x0079ff);
            GODColorsE.add(0x0078ff);
            GODColorsE.add(0x0078ff);
            GODColorsE.add(0x0077ff);
            GODColorsE.add(0x0077ff);
            GODColorsE.add(0x0076ff);
            GODColorsE.add(0x0076ff);
            GODColorsE.add(0x0075ff);
            GODColorsE.add(0x0075ff);
            GODColorsE.add(0x0074ff);
            GODColorsE.add(0x0074ff);
            GODColorsE.add(0x0073ff);
            GODColorsE.add(0x0073ff);
            GODColorsE.add(0x0072ff);
            GODColorsE.add(0x0072ff);
            GODColorsE.add(0x0071ff);
            GODColorsE.add(0x0071ff);
            GODColorsE.add(0x0070ff);
            GODColorsE.add(0x0070ff);
            GODColorsE.add(0x006fff);
            GODColorsE.add(0x006fff);
            GODColorsE.add(0x006eff);
            GODColorsE.add(0x006eff);
            GODColorsE.add(0x006dff);
            GODColorsE.add(0x006dff);
            GODColorsE.add(0x006cff);
            GODColorsE.add(0x006cff);
            GODColorsE.add(0x006bff);
            GODColorsE.add(0x006bff);
            GODColorsE.add(0x006aff);
            GODColorsE.add(0x006aff);
            GODColorsE.add(0x0069ff);
            GODColorsE.add(0x0069ff);
            GODColorsE.add(0x0068ff);
            GODColorsE.add(0x0068ff);
            GODColorsE.add(0x0067ff);
            GODColorsE.add(0x0067ff);
            GODColorsE.add(0x0066ff);
            GODColorsE.add(0x0066ff);
            GODColorsE.add(0x0065ff);
            GODColorsE.add(0x0065ff);
            GODColorsE.add(0x0064ff);
            GODColorsE.add(0x0064ff);
            GODColorsE.add(0x0063ff);
            GODColorsE.add(0x0063ff);
            GODColorsE.add(0x0062ff);
            GODColorsE.add(0x0062ff);
            GODColorsE.add(0x0061ff);
            GODColorsE.add(0x0061ff);
            GODColorsE.add(0x0060ff);
            GODColorsE.add(0x0060ff);
            GODColorsE.add(0x005fff);
            GODColorsE.add(0x005fff);
            GODColorsE.add(0x005eff);
            GODColorsE.add(0x005eff);
            GODColorsE.add(0x005dff);
            GODColorsE.add(0x005dff);
            GODColorsE.add(0x005cff);
            GODColorsE.add(0x005cff);
            GODColorsE.add(0x005bff);
            GODColorsE.add(0x005bff);
            GODColorsE.add(0x005aff);
            GODColorsE.add(0x005aff);
            GODColorsE.add(0x0059ff);
            GODColorsE.add(0x0059ff);
            GODColorsE.add(0x0058ff);
            GODColorsE.add(0x0058ff);
            GODColorsE.add(0x0057ff);
            GODColorsE.add(0x0057ff);
            GODColorsE.add(0x0056ff);
            GODColorsE.add(0x0056ff);
            GODColorsE.add(0x0055ff);
            GODColorsE.add(0x0055ff);
            GODColorsE.add(0x0054ff);
            GODColorsE.add(0x0054ff);
            GODColorsE.add(0x0053ff);
            GODColorsE.add(0x0053ff);
            GODColorsE.add(0x0052ff);
            GODColorsE.add(0x0052ff);
            GODColorsE.add(0x0051ff);
            GODColorsE.add(0x0051ff);
            GODColorsE.add(0x0050ff);
            GODColorsE.add(0x0050ff);
            GODColorsE.add(0x004fff);
            GODColorsE.add(0x004fff);
            GODColorsE.add(0x004eff);
            GODColorsE.add(0x004eff);
            GODColorsE.add(0x004dff);
            GODColorsE.add(0x004dff);
            GODColorsE.add(0x004cff);
            GODColorsE.add(0x004cff);
            GODColorsE.add(0x004bff);
            GODColorsE.add(0x004bff);
            GODColorsE.add(0x004aff);
            GODColorsE.add(0x004aff);
            GODColorsE.add(0x0049ff);
            GODColorsE.add(0x0049ff);
            GODColorsE.add(0x0048ff);
            GODColorsE.add(0x0048ff);
            GODColorsE.add(0x0047ff);
            GODColorsE.add(0x0047ff);
            GODColorsE.add(0x0046ff);
            GODColorsE.add(0x0046ff);
            GODColorsE.add(0x0045ff);
            GODColorsE.add(0x0045ff);
            GODColorsE.add(0x0044ff);
            GODColorsE.add(0x0044ff);
            GODColorsE.add(0x0043ff);
            GODColorsE.add(0x0043ff);
            GODColorsE.add(0x0042ff);
            GODColorsE.add(0x0042ff);
            GODColorsE.add(0x0041ff);
            GODColorsE.add(0x0041ff);
            GODColorsE.add(0x0040ff);
            GODColorsE.add(0x0040ff);
            GODColorsE.add(0x003fff);
            GODColorsE.add(0x003fff);
            GODColorsE.add(0x003eff);
            GODColorsE.add(0x003eff);
            GODColorsE.add(0x003dff);
            GODColorsE.add(0x003dff);
            GODColorsE.add(0x003cff);
            GODColorsE.add(0x003cff);
            GODColorsE.add(0x003bff);
            GODColorsE.add(0x003bff);
            GODColorsE.add(0x003aff);
            GODColorsE.add(0x003aff);
            GODColorsE.add(0x0039ff);
            GODColorsE.add(0x0039ff);
            GODColorsE.add(0x0038ff);
            GODColorsE.add(0x0038ff);
            GODColorsE.add(0x0037ff);
            GODColorsE.add(0x0037ff);
            GODColorsE.add(0x0036ff);
            GODColorsE.add(0x0036ff);
            GODColorsE.add(0x0035ff);
            GODColorsE.add(0x0035ff);
            GODColorsE.add(0x0034ff);
            GODColorsE.add(0x0034ff);
            GODColorsE.add(0x0033ff);
            GODColorsE.add(0x0033ff);
            GODColorsE.add(0x0032ff);
            GODColorsE.add(0x0032ff);
            GODColorsE.add(0x0031ff);
            GODColorsE.add(0x0031ff);
            GODColorsE.add(0x0030ff);
            GODColorsE.add(0x0030ff);
            GODColorsE.add(0x002fff);
            GODColorsE.add(0x002fff);
            GODColorsE.add(0x002eff);
            GODColorsE.add(0x002eff);
            GODColorsE.add(0x002dff);
            GODColorsE.add(0x002dff);
            GODColorsE.add(0x002cff);
            GODColorsE.add(0x002cff);
            GODColorsE.add(0x002bff);
            GODColorsE.add(0x002bff);
            GODColorsE.add(0x002aff);
            GODColorsE.add(0x002aff);
            GODColorsE.add(0x0029ff);
            GODColorsE.add(0x0029ff);
            GODColorsE.add(0x0028ff);
            GODColorsE.add(0x0028ff);
            GODColorsE.add(0x0027ff);
            GODColorsE.add(0x0027ff);
            GODColorsE.add(0x0026ff);
            GODColorsE.add(0x0026ff);
            GODColorsE.add(0x0025ff);
            GODColorsE.add(0x0025ff);
            GODColorsE.add(0x0024ff);
            GODColorsE.add(0x0024ff);
            GODColorsE.add(0x0023ff);
            GODColorsE.add(0x0023ff);
            GODColorsE.add(0x0022ff);
            GODColorsE.add(0x0022ff);
            GODColorsE.add(0x0021ff);
            GODColorsE.add(0x0021ff);
            GODColorsE.add(0x0020ff);
            GODColorsE.add(0x0020ff);
            GODColorsE.add(0x001fff);
            GODColorsE.add(0x001fff);
            GODColorsE.add(0x001eff);
            GODColorsE.add(0x001eff);
            GODColorsE.add(0x001dff);
            GODColorsE.add(0x001dff);
            GODColorsE.add(0x001cff);
            GODColorsE.add(0x001cff);
            GODColorsE.add(0x001bff);
            GODColorsE.add(0x001bff);
            GODColorsE.add(0x001aff);
            GODColorsE.add(0x001aff);
            GODColorsE.add(0x0019ff);
            GODColorsE.add(0x0019ff);
            GODColorsE.add(0x0018ff);
            GODColorsE.add(0x0018ff);
            GODColorsE.add(0x0017ff);
            GODColorsE.add(0x0017ff);
            GODColorsE.add(0x0016ff);
            GODColorsE.add(0x0016ff);
            GODColorsE.add(0x0015ff);
            GODColorsE.add(0x0015ff);
            GODColorsE.add(0x0014ff);
            GODColorsE.add(0x0014ff);
            GODColorsE.add(0x0013ff);
            GODColorsE.add(0x0013ff);
            GODColorsE.add(0x0012ff);
            GODColorsE.add(0x0012ff);
            GODColorsE.add(0x0011ff);
            GODColorsE.add(0x0011ff);
            GODColorsE.add(0x0010ff);
            GODColorsE.add(0x0010ff);
            GODColorsE.add(0x000fff);
            GODColorsE.add(0x000fff);
            GODColorsE.add(0x000eff);
            GODColorsE.add(0x000eff);
            GODColorsE.add(0x000dff);
            GODColorsE.add(0x000dff);
            GODColorsE.add(0x000cff);
            GODColorsE.add(0x000cff);
            GODColorsE.add(0x000bff);
            GODColorsE.add(0x000bff);
            GODColorsE.add(0x000aff);
            GODColorsE.add(0x000aff);
            GODColorsE.add(0x0009ff);
            GODColorsE.add(0x0009ff);
            GODColorsE.add(0x0008ff);
            GODColorsE.add(0x0008ff);
            GODColorsE.add(0x0007ff);
            GODColorsE.add(0x0007ff);
            GODColorsE.add(0x0006ff);
            GODColorsE.add(0x0006ff);
            GODColorsE.add(0x0005ff);
            GODColorsE.add(0x0005ff);
            GODColorsE.add(0x0004ff);
            GODColorsE.add(0x0004ff);
            GODColorsE.add(0x0003ff);
            GODColorsE.add(0x0003ff);
            GODColorsE.add(0x0002ff);
            GODColorsE.add(0x0002ff);
            GODColorsE.add(0x0001ff);
            GODColorsE.add(0x0001ff);
            GODColorsE.add(0x0000ff);
            GODColorsE.add(0x0000ff);

            GODColorsH.add(0x8800ff);
            GODColorsH.add(0x8900ff);
            GODColorsH.add(0x8a00ff);
            GODColorsH.add(0x8b00ff);
            GODColorsH.add(0x8c00ff);
            GODColorsH.add(0x8d00ff);
            GODColorsH.add(0x8e00ff);
            GODColorsH.add(0x8d00ff);
            GODColorsH.add(0x8f00ff);
            GODColorsH.add(0x9000ff);
            GODColorsH.add(0x9100ff);
            GODColorsH.add(0x9200ff);
            GODColorsH.add(0x9300ff);
            GODColorsH.add(0x9400ff);
            GODColorsH.add(0x9500ff);
            GODColorsH.add(0x9600ff);
            GODColorsH.add(0x9700ff);
            GODColorsH.add(0x9800ff);
            GODColorsH.add(0x9900ff);
            GODColorsH.add(0x9a00ff);
            GODColorsH.add(0x9b00ff);
            GODColorsH.add(0x9c00ff);
            GODColorsH.add(0x9e00ff);
            GODColorsH.add(0x9f00ff);
            GODColorsH.add(0xa000ff);
            GODColorsH.add(0xa100ff);
            GODColorsH.add(0xa200ff);
            GODColorsH.add(0xa300ff);
            GODColorsH.add(0xa400ff);
            GODColorsH.add(0xa500ff);
            GODColorsH.add(0xa600ff);
            GODColorsH.add(0xa700ff);
            GODColorsH.add(0xa800ff);
            GODColorsH.add(0xa900ff);
            GODColorsH.add(0xaa00ff);
            GODColorsH.add(0xab00ff);
            GODColorsH.add(0xac00ff);
            GODColorsH.add(0xad00ff);
            GODColorsH.add(0xae00ff);
            GODColorsH.add(0xaf00ff);
            GODColorsH.add(0xb000ff);
            GODColorsH.add(0xb100ff);
            GODColorsH.add(0xb200ff);
            GODColorsH.add(0xb300ff);
            GODColorsH.add(0xb400ff);
            GODColorsH.add(0xb500ff);
            GODColorsH.add(0xb600ff);
            GODColorsH.add(0xb700ff);
            GODColorsH.add(0xb800ff);
            GODColorsH.add(0xb900ff);
            GODColorsH.add(0xba00ff);
            GODColorsH.add(0xbb00ff);
            GODColorsH.add(0xbc00ff);
            GODColorsH.add(0xbd00ff);
            GODColorsH.add(0xbe00ff);
            GODColorsH.add(0xbf00ff);
            GODColorsH.add(0xc000ff);
            GODColorsH.add(0xc100ff);
            GODColorsH.add(0xc200ff);
            GODColorsH.add(0xc300ff);
            GODColorsH.add(0xc400ff);
            GODColorsH.add(0xc500ff);
            GODColorsH.add(0xc600ff);
            GODColorsH.add(0xc700ff);
            GODColorsH.add(0xc800ff);
            GODColorsH.add(0xc900ff);
            GODColorsH.add(0xca00ff);
            GODColorsH.add(0xcb00ff);
            GODColorsH.add(0xcc00ff);
            GODColorsH.add(0xcd00ff);
            GODColorsH.add(0xce00ff);
            GODColorsH.add(0xcf00ff);
            GODColorsH.add(0xd000ff);
            GODColorsH.add(0xd100ff);
            GODColorsH.add(0xd200ff);
            GODColorsH.add(0xd300ff);
            GODColorsH.add(0xd400ff);
            GODColorsH.add(0xd500ff);
            GODColorsH.add(0xd600ff);
            GODColorsH.add(0xd700ff);
            GODColorsH.add(0xd800ff);
            GODColorsH.add(0xd900ff);
            GODColorsH.add(0xda00ff);
            GODColorsH.add(0xdb00ff);
            GODColorsH.add(0xdc00ff);
            GODColorsH.add(0xdd00ff);
            GODColorsH.add(0xde00ff);
            GODColorsH.add(0xdf00ff);
            GODColorsH.add(0xf000ff);
            GODColorsH.add(0xf100ff);
            GODColorsH.add(0xf200ff);
            GODColorsH.add(0xf300ff);
            GODColorsH.add(0xf400ff);
            GODColorsH.add(0xf500ff);
            GODColorsH.add(0xf600ff);
            GODColorsH.add(0xf700ff);
            GODColorsH.add(0xf800ff);
            GODColorsH.add(0xf900ff);
            GODColorsH.add(0xfa00ff);
            GODColorsH.add(0xfb00ff);
            GODColorsH.add(0xfc00ff);
            GODColorsH.add(0xfd00ff);
            GODColorsH.add(0xfe00ff);
            GODColorsH.add(0xff00ff);

            GODColorsH.add(0xff00ff);

            GODColorsH.add(0xff00ff);
            GODColorsH.add(0xfe00ff);
            GODColorsH.add(0xfd00ff);
            GODColorsH.add(0xfc00ff);
            GODColorsH.add(0xfb00ff);
            GODColorsH.add(0xfa00ff);
            GODColorsH.add(0xf900ff);
            GODColorsH.add(0xf800ff);
            GODColorsH.add(0xf700ff);
            GODColorsH.add(0xf600ff);
            GODColorsH.add(0xf500ff);
            GODColorsH.add(0xf400ff);
            GODColorsH.add(0xf300ff);
            GODColorsH.add(0xf200ff);
            GODColorsH.add(0xf100ff);
            GODColorsH.add(0xf000ff);
            GODColorsH.add(0xdf00ff);
            GODColorsH.add(0xde00ff);
            GODColorsH.add(0xdd00ff);
            GODColorsH.add(0xdc00ff);
            GODColorsH.add(0xdb00ff);
            GODColorsH.add(0xda00ff);
            GODColorsH.add(0xd900ff);
            GODColorsH.add(0xd800ff);
            GODColorsH.add(0xd700ff);
            GODColorsH.add(0xd600ff);
            GODColorsH.add(0xd500ff);
            GODColorsH.add(0xd400ff);
            GODColorsH.add(0xd300ff);
            GODColorsH.add(0xd200ff);
            GODColorsH.add(0xd100ff);
            GODColorsH.add(0xd000ff);
            GODColorsH.add(0xcf00ff);
            GODColorsH.add(0xce00ff);
            GODColorsH.add(0xcd00ff);
            GODColorsH.add(0xcc00ff);
            GODColorsH.add(0xcb00ff);
            GODColorsH.add(0xca00ff);
            GODColorsH.add(0xc900ff);
            GODColorsH.add(0xc800ff);
            GODColorsH.add(0xc700ff);
            GODColorsH.add(0xc600ff);
            GODColorsH.add(0xc500ff);
            GODColorsH.add(0xc400ff);
            GODColorsH.add(0xc300ff);
            GODColorsH.add(0xc200ff);
            GODColorsH.add(0xc100ff);
            GODColorsH.add(0xc000ff);
            GODColorsH.add(0xbf00ff);
            GODColorsH.add(0xbe00ff);
            GODColorsH.add(0xbd00ff);
            GODColorsH.add(0xbc00ff);
            GODColorsH.add(0xbb00ff);
            GODColorsH.add(0xba00ff);
            GODColorsH.add(0xb900ff);
            GODColorsH.add(0xb800ff);
            GODColorsH.add(0xb700ff);
            GODColorsH.add(0xb600ff);
            GODColorsH.add(0xb500ff);
            GODColorsH.add(0xb400ff);
            GODColorsH.add(0xb300ff);
            GODColorsH.add(0xb200ff);
            GODColorsH.add(0xb100ff);
            GODColorsH.add(0xb000ff);
            GODColorsH.add(0xaf00ff);
            GODColorsH.add(0xae00ff);
            GODColorsH.add(0xad00ff);
            GODColorsH.add(0xac00ff);
            GODColorsH.add(0xab00ff);
            GODColorsH.add(0xaa00ff);
            GODColorsH.add(0xa900ff);
            GODColorsH.add(0xa800ff);
            GODColorsH.add(0xa700ff);
            GODColorsH.add(0xa600ff);
            GODColorsH.add(0xa500ff);
            GODColorsH.add(0xa400ff);
            GODColorsH.add(0xa300ff);
            GODColorsH.add(0xa200ff);
            GODColorsH.add(0xa100ff);
            GODColorsH.add(0xa000ff);
            GODColorsH.add(0x9f00ff);
            GODColorsH.add(0x9e00ff);
            GODColorsH.add(0x9c00ff);
            GODColorsH.add(0x9b00ff);
            GODColorsH.add(0x9a00ff);
            GODColorsH.add(0x9900ff);
            GODColorsH.add(0x9800ff);
            GODColorsH.add(0x9700ff);
            GODColorsH.add(0x9600ff);
            GODColorsH.add(0x9500ff);
            GODColorsH.add(0x9400ff);
            GODColorsH.add(0x9300ff);
            GODColorsH.add(0x9200ff);
            GODColorsH.add(0x9100ff);
            GODColorsH.add(0x9000ff);
            GODColorsH.add(0x8f00ff);
            GODColorsH.add(0x8d00ff);
            GODColorsH.add(0x8e00ff);
            GODColorsH.add(0x8d00ff);
            GODColorsH.add(0x8c00ff);
            GODColorsH.add(0x8b00ff);
            GODColorsH.add(0x8a00ff);
            GODColorsH.add(0x8900ff);
            GODColorsH.add(0x8800ff);
        }

        private Player player;

        public ColorManager(Player player) {
            this.player = player;
        }

        public int getColor(ArrayList<Integer> arrayList) {
            return arrayList.get(player.getTicksLived() % arrayList.size());
        }
    }
}
