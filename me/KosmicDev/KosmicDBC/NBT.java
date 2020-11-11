package me.KosmicDev.KosmicDBC;

import net.minecraft.server.v1_7_R4.*;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class NBT {
    public static void SetInt(final Player arg1, final String Key, final Integer Value) {
        final NBTTagCompound nbtTagCompound = new NBTTagCompound();
        ((CraftPlayer) arg1).getHandle().e(nbtTagCompound);
        nbtTagCompound.getCompound("ForgeData").getCompound("PlayerPersisted").set(Key, new NBTTagInt(Value));
        ((CraftPlayer) arg1).getHandle().a(nbtTagCompound);
    }

    public static void SetString(final Player arg1, final String Key, final String Value) {
        final NBTTagCompound nbtTagCompound = new NBTTagCompound();
        ((CraftPlayer) arg1).getHandle().e(nbtTagCompound);
        nbtTagCompound.getCompound("ForgeData").getCompound("PlayerPersisted").set(Key, new NBTTagString(Value));
        ((CraftPlayer) arg1).getHandle().a(nbtTagCompound);
    }

    public static void SetDouble(final Player player, final String string, final double d) {
        final NBTTagCompound nbtTagCompound = new NBTTagCompound();
        ((CraftPlayer) player).getHandle().e(nbtTagCompound);
        nbtTagCompound.getCompound("ForgeData").getCompound("PlayerPersisted").set(string, new NBTTagDouble(d));
        ((CraftPlayer) player).getHandle().a(nbtTagCompound);
    }

    public static void SetFloat(final Player player, final String string, final float d) {
        final NBTTagCompound nbtTagCompound = new NBTTagCompound();
        ((CraftPlayer) player).getHandle().e(nbtTagCompound);
        nbtTagCompound.getCompound("ForgeData").getCompound("PlayerPersisted").set(string, new NBTTagFloat(d));
        ((CraftPlayer) player).getHandle().a(nbtTagCompound);
    }

    public static Integer GetInt(final Player player, final String Key) {
        final NBTTagCompound nbtTagCompound = new NBTTagCompound();
        ((CraftPlayer) player).getHandle().e(nbtTagCompound);
        return nbtTagCompound.getCompound("ForgeData").getCompound("PlayerPersisted").getInt(Key);
    }

    public static String GetString(final Player player, final String Key) {
        final NBTTagCompound nbtTagCompound = new NBTTagCompound();
        ((CraftPlayer) player).getHandle().e(nbtTagCompound);
        return nbtTagCompound.getCompound("ForgeData").getCompound("PlayerPersisted").getString(Key);
    }

    public static Double GetDouble(final Player player, final String Key) {
        final NBTTagCompound nbtTagCompound = new NBTTagCompound();
        ((CraftPlayer) player).getHandle().e(nbtTagCompound);
        return nbtTagCompound.getCompound("ForgeData").getCompound("PlayerPersisted").getDouble(Key);
    }

    public static Float GetFloat(final Player player, final String Key) {
        final NBTTagCompound nbtTagCompound = new NBTTagCompound();
        ((CraftPlayer) player).getHandle().e(nbtTagCompound);
        return nbtTagCompound.getCompound("ForgeData").getCompound("PlayerPersisted").getFloat(Key);
    }
}