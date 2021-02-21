package lq2007.mod.strangegenerator.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class NBTUtils {

    public static ListNBT writeBlockPos(Set<BlockPos> pos) {
        ListNBT list = new ListNBT();
        for (BlockPos p : pos) {
            list.add(net.minecraft.nbt.NBTUtil.writeBlockPos(p));
        }
        return list;
    }

    public static void readBlockPos(CompoundNBT nbt, String key, Set<BlockPos> list, boolean clear) {
        readBlockPos(nbt.getList(key, Constants.NBT.TAG_COMPOUND), list, clear);
    }

    public static void readBlockPos(ListNBT nbtList, Set<BlockPos> list, boolean clear) {
        if (list == null) list = new HashSet<>();
        else if (clear) list.clear();
        for (int i = 0; i < nbtList.size(); i++) {
            list.add(net.minecraft.nbt.NBTUtil.readBlockPos(nbtList.getCompound(i)));
        }
    }

    public static ListNBT writeUUIDs(Collection<UUID> uuids) {
        ListNBT list = new ListNBT();
        for (UUID uuid : uuids) {
            list.add(net.minecraft.nbt.NBTUtil.func_240626_a_(uuid));
        }
        return list;
    }

    public static void readUUIDs(CompoundNBT nbt, String key, Collection<UUID> list, boolean clear) {
        readUUIDs(nbt.getList(key, Constants.NBT.TAG_INT_ARRAY), list, clear);
    }

    public static void readUUIDs(ListNBT nbtList, Collection<UUID> list, boolean clear) {
        if (list == null) list = new HashSet<>();
        else if (clear) list.clear();
        for (INBT inbt : nbtList) {
            list.add(net.minecraft.nbt.NBTUtil.readUniqueId(inbt));
        }
    }

    public static void save(CompoundNBT nbt, File file) throws IOException {
        if (file.exists()) {
            FileUtils.deleteQuietly(file);
        }
        if (!file.getParentFile().exists()) {
            if (!file.mkdirs()) {
                throw new IOException("Cannot mkdirs " + file);
            }
        }
        if (file.createNewFile()) {
            CompressedStreamTools.writeCompressed(nbt, file);
        }
    }

    public static CompoundNBT read(File file) throws IOException {
        if (file.isFile()) {
            return CompressedStreamTools.readCompressed(file);
        }
        return new CompoundNBT();
    }
}
