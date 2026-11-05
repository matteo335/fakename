package net.matteo.fakename.utils;

import static net.matteo.fakename.utils.Variables.*;

import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Files;
import java.util.*;

@SuppressWarnings({"resource", "DataFlowIssue"})
public class Utils {

    public static String name(Player player) {
        if (!Variables.memoryCache.containsKey(player.getUUID())) {
            String name = player.getName().getString();

            try {
                if (Variables.fileCache == null) {
                    Variables.fileCache = player.level().getServer().getWorldPath(LevelResource.ROOT).resolve("data").resolve("fakename");
                    Files.createDirectories(Variables.fileCache);
                }


                if (!Variables.fileCache.resolve(name + ".txt").toFile().exists()) {
                    Files.writeString(Variables.fileCache.resolve(name + ".txt").toFile().toPath(), name);
                }

                Variables.memoryCache.put(player.getUUID(), Files.readString(Variables.fileCache.resolve(name + ".txt")));

            } catch (Throwable exception) {
                logger.error("Fakename exception, please report using the github or the discord: https://github.com/matteo335/fakename https://discord.gg/2EyYeFPqRK");
                //noinspection CallToPrintStackTrace
                exception.printStackTrace();
            }
        }

        return Variables.memoryCache.get(player.getUUID());
    }

    public static void refresh(ServerPlayer target) {
        target.level().getServer().getPlayerList().broadcastAll(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(target)));

        String name = DisplayName.desired(target);
        if (name == null) {
            DisplayName.remove(target);
        } else {
            DisplayName.set(target, name);
        }
    }
}
