package net.matteo.custom_name;

import static net.matteo.custom_name.utils.Variables.*;

import com.google.gson.*;

import net.minecraft.server.permissions.*;

import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;

public class JsonConfig {

    public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static void load() {
        if (!config.toFile().exists()) {
            if (!Path.of("config").toFile().exists()) try {
                Files.createDirectory(Path.of("config"));
            } catch (IOException exception) {
                logger.fatal("Fakename failed to create config folder, this is likely due to the operating system denying permission. We cannot continue.");
            }

            try (FileWriter writer = new FileWriter(config.toFile())) {
                JsonObject object = new JsonObject();
                JsonArray array = new JsonArray();

                object.addProperty("Explanation", "Valid values: 'operator' only operators can use the command, 'self' everyone can use the command on themselves, 'everyone' everyone can use the command on everyone, 'player_name' the holder of the player original name will be able to use this command on everyone");
                object.addProperty("clearPermission", "operator");
                object.addProperty("setPermission", "operator");
                object.addProperty("realPermission", "everyone");

                array.add("dev");
                array.add("admin");
                object.add("player_name", array);

                gson.toJson(object, writer);
            } catch (Throwable exception) {
                logger.fatal("Unable to create config file, if any issue happens to Fakename afterward it is very likely caused by your system preventing Java from writing this file", exception);
            }
        }

        try (FileReader reader = new FileReader(config.toFile())) {
            JsonObject object = gson.fromJson(reader, JsonObject.class);

            //null will be assumed as self in CommandsMixin. This line is useless, it is just to provide understanding.
            switch (object.get("clearPermission").getAsString()) {
                case "operator" -> clearPermission = new PermissionCheck.Require(new Permission.HasCommandLevel(PermissionLevel.ADMINS));
                case "everyone" -> clearPermission = new PermissionCheck.Require(new Permission.HasCommandLevel(PermissionLevel.ALL));
                default -> clearPermission = null;
            }

            switch (object.get("setPermission").getAsString()) {
                case  "operator" -> setPermission = new PermissionCheck.Require(new Permission.HasCommandLevel(PermissionLevel.ADMINS));
                case "everyone" -> setPermission = new PermissionCheck.Require(new Permission.HasCommandLevel(PermissionLevel.ALL));
                default -> setPermission = null;
            }

            switch (object.get("realPermission").getAsString()) {
                case "operator" -> realPermission = new PermissionCheck.Require(new Permission.HasCommandLevel(PermissionLevel.ADMINS));
                case "everyone" -> realPermission = new PermissionCheck.Require(new Permission.HasCommandLevel(PermissionLevel.ALL));
                default -> realPermission = null;
            }

            for (JsonElement array : object.getAsJsonArray("player_name")) {
                playerPermissionList.add(array.getAsString());
            }
        } catch (Throwable exception) {
            logger.fatal("Unable to read config file, if any issue happens to Fakename afterward it is very likely caused by your system preventing Java from reading this file", exception);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean tryUpdate() {
        try (FileWriter writer = new FileWriter(config.toFile())) {
            JsonObject object = new JsonObject();
            JsonArray array = new JsonArray();

            object.addProperty("Explanation", "Valid values: 'operator' only operators can use the command, 'self' everyone can use the command on themselves, 'everyone' everyone can use the command on everyone, 'player_name' the holder of the player original name will be able to use this command on everyone");
            object.addProperty("clearPermission", clearPermission());
            object.addProperty("setPermission", setPermission());
            object.addProperty("realPermission", realPermission());

            for (String entry : playerPermissionList) {
                array.add(entry);
            }
            object.add("player_name", array);

            gson.toJson(object, writer);
            return true;
        } catch (Throwable exception) {
            logger.error("Fakename failed to update config", exception);
        }

        return false;
    }

    public static String clearPermission() {
        if (clearPermission == null) return "self";
        if (clearPermission.equals(new PermissionCheck.Require(new Permission.HasCommandLevel(PermissionLevel.ALL)))) return "everyone";
        return "operator";
    }

    public static String setPermission() {
        if (setPermission == null) return "self";
        if (setPermission.equals(new PermissionCheck.Require(new Permission.HasCommandLevel(PermissionLevel.ALL)))) return "everyone";
        return "operator";
    }

    public static String realPermission() {
        if (realPermission == null) return "self";
        if (realPermission.equals(new PermissionCheck.Require(new Permission.HasCommandLevel(PermissionLevel.ALL)))) return "everyone";
        return "operator";
    }
}
