package net.matteo.fakename.utils;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionCheck;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.nio.file.Path;
import java.util.*;

public class Variables {
    public static Logger logger = LogManager.getLogger("fakename");

    public static Map<UUID, ServerEntity> trackers = new HashMap<>();
    public static Map<UUID, Set<ServerPlayer>> viewers = new HashMap<>();

    public static Map<UUID, String> memoryCache = new HashMap<>();
    public static Path fileCache = null; //set in Utils
    public static Path config = Path.of("config/fakename.json");

    //set in JsonConfig
    public static PermissionCheck clearPermission = null;
    public static PermissionCheck setPermission = null;
    public static PermissionCheck realPermission = null;
    public static List<String> playerPermissionList = new ArrayList<>(); //everyone in this list can perform every command they like

    public static boolean fakeProfile = false; //true only while building the fakename info packet
}
