package me.KosmicDev.KosmicDBC;

import net.minecraft.server.v1_7_R4.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

public class Methods {
    static String[] ltnb = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u",
            "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    public static ItemStack createItemStack(final Material material, final int damage, final String name, String... lore) {
        final ItemStack itemStack = new ItemStack(material, 1, (short) damage);
        ItemMeta itemStackMeta = itemStack.getItemMeta();
        itemStackMeta.setDisplayName(name);
        itemStackMeta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(itemStackMeta);
        return itemStack;
    }

    public static ItemStack createItemStack(final int material, final int damage, final String name, final String lore) {
        final ItemStack itemStack = new ItemStack(material, 1, (short) damage);
        final ItemMeta itemStackMeta = itemStack.getItemMeta();
        itemStackMeta.setDisplayName(name);
        itemStackMeta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(itemStackMeta);
        return itemStack;
    }

    public static ItemStack createItemStack(final Material material, final int damage, final String name, final ItemMeta lore) {
        final ItemStack itemStack = new ItemStack(material, 1, (short) damage);
        final ItemMeta itemStackMeta = itemStack.getItemMeta();
        itemStackMeta.setDisplayName(name);
        itemStackMeta.setLore((List<String>) lore);
        itemStack.setItemMeta(itemStackMeta);
        return itemStack;
    }

    public static ItemStack createItemStackWithGlow(final Material material, final int damage, final String name, final String lore) {
        ItemStack itemStack = new ItemStack(material, 1, (short) damage);
        final ItemMeta itemStackMeta = itemStack.getItemMeta();
        itemStackMeta.setDisplayName(name);
        itemStackMeta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(itemStackMeta);
        itemStack = addGlow(itemStack);
        return itemStack;
    }

    public static ItemStack addGlow(final ItemStack item) {
        final net.minecraft.server.v1_7_R4.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = null;
        if (!nmsStack.hasTag()) {
            tag = new NBTTagCompound();
            nmsStack.setTag(tag);
        }
        if (tag == null) {
            tag = nmsStack.getTag();
        }
        final NBTTagList ench = new NBTTagList();
        tag.set("ench", ench);
        nmsStack.setTag(tag);
        return CraftItemStack.asCraftMirror(nmsStack);
    }

    public static double fusionHelp(final String form, final double dmg) {
        final double a = (dmg * 3.5 * 2.5) - 1.0;
        return a;
    }

    public static void sendActionBar(final Player player, final String string) {
        final String stringa = string.replace("_", " ");
        final String s = ChatColor.translateAlternateColorCodes('&', stringa);
        final IChatBaseComponent icbc = ChatSerializer.a("{\"text\": \"" + s + "\"}");
        final PacketPlayOutChat bar = new PacketPlayOutChat(icbc);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(bar);
    }

    public static String lowerCaseString(final String string, final String string2) {
        return (string.toLowerCase().indexOf(string2.toLowerCase()) == -1) ? string
                : string.replaceAll(string.substring(string.toLowerCase().indexOf(string2.toLowerCase()), string.toLowerCase().indexOf(string2.toLowerCase()) + string2.length()), string2);
    }

    public static String f_namegen(final String s1, final String s2) {
        return String.valueOf(s1.substring(0, s1.length() / 2)) + s2.substring(s2.length() / 2);
    }

    public static boolean getLookingCloselyAt(final Player player, final LivingEntity living) {
        final AxisAlignedBB box = ((CraftLivingEntity) living).getHandle().boundingBox;
        for (double y = box.b; y <= box.e; y += 0.3) {
            for (double x = box.a; x <= box.d; x += 0.3) {
                for (double z = box.c; z <= box.f; z += 0.3) {
                    final double d = new Location(living.getWorld(), x, y, z).toVector().subtract(player.getLocation().toVector()).normalize().dot(player.getLocation().getDirection());
                    if (d > 0.8 && d < 0.9) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean getLookingAt(final Player player, final LivingEntity living) {
        final AxisAlignedBB box = ((CraftLivingEntity) living).getHandle().boundingBox;
        for (double y = box.b; y <= box.e; y += 0.3) {
            for (double x = box.a; x <= box.d; x += 0.3) {
                for (double z = box.c; z <= box.f; z += 0.3) {
                    if (new Location(living.getWorld(), x, y, z).toVector().subtract(player.getLocation().toVector()).normalize().dot(player.getLocation().getDirection()) > 0.89) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static String sa(final String s1, final int s2) {
        return s1.charAt(s2) + "";
    }

    public static int dnsEyeC1(final String s) {
        final int i = 42;
        return (s != null && s.length() > i) ? letToNum5(sa(s, i), sa(s, i + 1), sa(s, i + 2), sa(s, i + 3), sa(s, i + 4)) : 0;
    }

    public static int dnsEyeC2(final String s) {
        final int i = 47;
        return (s != null && s.length() > i) ? letToNum5(sa(s, i), sa(s, i + 1), sa(s, i + 2), sa(s, i + 3), sa(s, i + 4)) : 0;
    }

    public static int dnsHairC(final String s) {
        final int i = 7;
        return (s != null && s.length() > i) ? letToNum5(sa(s, i), sa(s, i + 1), sa(s, i + 2), sa(s, i + 3), sa(s, i + 4)) : 0;
    }

    public static String dnsHairCSet(final String s, final int w) {
        final int i = 7;
        return (s != null && s.length() > i) ? (s.substring(0, i) + numToLet5(w) + s.substring(i + 5)) : "0";
    }

    public static String dnsEyeC1Set(final String s, final int w) {
        final int i = 42;
        return (s != null && s.length() > i) ? (s.substring(0, i) + numToLet5(w) + s.substring(i + 5)) : "0";
    }

    public static String dnsEyeC2Set(final String s, final int w) {
        final int i = 47;
        return (s != null && s.length() > i) ? (s.substring(0, i) + numToLet5(w) + s.substring(i + 5)) : "0";
    }

    public static int letToNum(final String s1, final String s2) {
        int i1 = 0;
        int i2 = 0;
        int j = 0;
        for (int f = 0; f < ltnb.length; ++f) {
            if (s1.equals(ltnb[f])) {
                i1 = f;
            }
            if (s2.equals(ltnb[f])) {
                i2 = f;
            }
        }
        j = i1 * ltnb.length + i2;
        return j;
    }

    public static String numToLet(final int i) {
        String s = "00";
        String s2 = "";
        String s3 = "";
        final int i2 = i / ltnb.length;
        final int i3 = i2 * ltnb.length;
        final int i4 = i - i3;
        for (int f = 0; f < ltnb.length; ++f) {
            if (i2 == f) {
                s2 = ltnb[f];
            }
            if (i4 == f) {
                s3 = ltnb[f];
            }
        }
        s = s2 + s3;
        return s;
    }

    public static int letToNum5(final String s1, final String s2, final String s3, final String s4, final String s5) {
        int i1 = 0;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        int j = 0;
        for (int f = 0; f < ltnb.length; ++f) {
            if (s1.equals(ltnb[f])) {
                i1 = f;
            }
            if (s2.equals(ltnb[f])) {
                i2 = f;
            }
            if (s3.equals(ltnb[f])) {
                i3 = f;
            }
            if (s4.equals(ltnb[f])) {
                i4 = f;
            }
            if (s5.equals(ltnb[f])) {
                i5 = f;
            }
        }
        j = (((i1 * ltnb.length + i2) * ltnb.length + i3) * ltnb.length + i4) * ltnb.length + i5;
        return j;
    }

    public static String numToLet5(final int i) {
        String s = "00";
        String s2 = "";
        String s3 = "";
        String s4 = "";
        String s5 = "";
        String s6 = "";
        final int i2 = i / ltnb.length;
        final int i3 = i2 * ltnb.length;
        final int i4 = i - i3;
        final int i5 = i2 / ltnb.length;
        final int i6 = i5 * ltnb.length;
        final int i7 = i2 - i6;
        final int i8 = i5 / ltnb.length;
        final int i9 = i8 * ltnb.length;
        final int i10 = i5 - i9;
        final int i11 = i8 / ltnb.length;
        final int i12 = i11 * ltnb.length;
        final int i13 = i8 - i12;
        for (int f = 0; f < ltnb.length; ++f) {
            if (i11 == f) {
                s2 = ltnb[f];
            }
            if (i13 == f) {
                s3 = ltnb[f];
            }
            if (i10 == f) {
                s4 = ltnb[f];
            }
            if (i7 == f) {
                s5 = ltnb[f];
            }
            if (i4 == f) {
                s6 = ltnb[f];
            }
        }
        s = s2 + s3 + s4 + s5 + s6;
        return s;
    }
}
