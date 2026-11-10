package net.matteo.custom_name.mixins;

import static net.matteo.custom_name.utils.Utils.*;
import static net.matteo.custom_name.utils.Variables.*;

import net.matteo.custom_name.JsonConfig;

import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.Commands.argument;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.Permission;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Files;
import java.io.File;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("DataFlowIssue")
@Mixin(Commands.class)
public abstract class CommandsMixin {

    @Shadow
    public abstract CommandDispatcher<CommandSourceStack> getDispatcher();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init$fakename(Commands.CommandSelection commandSelection, CommandBuildContext context, CallbackInfo ci) {

        LiteralCommandNode<CommandSourceStack> builder = getDispatcher().register(literal("customname")

                .then(literal("real").then(argument("fakename", StringArgumentType.string()).suggests((command, suggestionsBuilder) -> {
                    for (ServerPlayer entry : command.getSource().getServer().getPlayerList().getPlayers()) {
                        suggestionsBuilder.suggest(entry.getName().getString());
                    }

                    for (String entry : memoryCache.values()) {
                        SharedSuggestionProvider.suggest(("\"" + entry + "\"").lines(), suggestionsBuilder);
                    }

                    return suggestionsBuilder.buildFuture();
                }).executes(command -> handleRealname$fakename(command.getSource(), command.getArgument("fakename", String.class).replace("\"", "")))))

                .then(literal("clear").then(argument("target", EntityArgument.players())
                                .executes(command -> handleClear$fakename(command.getSource(), EntityArgument.getPlayer(command, "target"))))
                        .executes(command -> handleClear$fakename(command.getSource(), command.getSource().getPlayerOrException())))

                .then(literal("set")
                        .then(argument("fakename", StringArgumentType.string())
                                .executes(command -> handleSetname$fakename(command.getSource(), command.getSource().getPlayerOrException(), StringArgumentType.getString(command, "fakename")))

                                .then(argument("target", EntityArgument.players())
                                        .executes((command) -> {
                                            handleSetname$fakename(command.getSource(), EntityArgument.getPlayer(command, "target"), StringArgumentType.getString(command, "fakename"));
                                            return 1;
                                        }))
                        ))

                .then(literal("config").requires(Commands.hasPermission(new PermissionCheck.Require(new Permission.HasCommandLevel(PermissionLevel.ADMINS))))
                        .then(literal("set")
                                .then(literal("operator").executes(command -> {
                                    setPermission = new PermissionCheck.Require(new Permission.HasCommandLevel(PermissionLevel.ADMINS));
                                    command.getSource().sendSystemMessage(Component.literal("Changed set requirement to operators only"));

                                    if (!JsonConfig.tryUpdate()) {
                                        command.getSource().sendSystemMessage(Component.literal("Error while trying to update config, your change will not survive server restart"));
                                    }
                                    return 1;
                                }))

                                .then(literal("self").executes(command -> {
                                    setPermission = null;

                                    command.getSource().sendSystemMessage(Component.literal("Changed set requirement to self-change and operators"));
                                    if (!JsonConfig.tryUpdate()) {
                                        command.getSource().sendSystemMessage(Component.literal("Error while trying to update config, your change will not survive server restart"));
                                    }
                                    return 1;
                                }))

                                .then(literal("everyone").executes(command -> {
                                    setPermission = new PermissionCheck.Require(new Permission.HasCommandLevel(PermissionLevel.ALL));

                                    command.getSource().sendSystemMessage(Component.literal("Removed set requirement. Every players can now change the name of everyone else"));
                                    if (!JsonConfig.tryUpdate()) {
                                        command.getSource().sendSystemMessage(Component.literal("Error while trying to update config, your change will not survive server restart"));
                                    }
                                    return 1;
                                }))
                        )


                        .then(literal("clear")
                                .then(literal("operator").executes(command -> {
                                    clearPermission = new PermissionCheck.Require(new Permission.HasCommandLevel(PermissionLevel.ADMINS));

                                    command.getSource().sendSystemMessage(Component.literal("Changed clear requirement to operators only"));
                                    if (!JsonConfig.tryUpdate()) {
                                        command.getSource().sendSystemMessage(Component.literal("Error while trying to update config, your change will not survive server restart"));
                                    }
                                    return 1;
                                }))

                                .then(literal("self").executes(command -> {
                                    clearPermission = null;

                                    command.getSource().sendSystemMessage(Component.literal("Changed clear requirement to self-change and operators"));
                                    if (!JsonConfig.tryUpdate()) {
                                        command.getSource().sendSystemMessage(Component.literal("Error while trying to update config, your change will not survive server restart"));
                                    }
                                    return 1;
                                }))

                                .then(literal("everyone").executes(command -> {
                                    clearPermission = new PermissionCheck.Require(new Permission.HasCommandLevel(PermissionLevel.ALL));

                                    command.getSource().sendSystemMessage(Component.literal("Removed clear requirement. Every players can now clear the name of everyone else"));
                                    if (!JsonConfig.tryUpdate()) {
                                        command.getSource().sendSystemMessage(Component.literal("Error while trying to update config, your change will not survive server restart"));
                                    }
                                    return 1;
                                }))
                        )

                        .then(literal("real")
                                .then(literal("operator").executes(command -> {
                                    realPermission = new PermissionCheck.Require(new Permission.HasCommandLevel(PermissionLevel.ADMINS));

                                    command.getSource().sendSystemMessage(Component.literal("Changed real requirement to operators only"));
                                    if (!JsonConfig.tryUpdate()) {
                                        command.getSource().sendSystemMessage(Component.literal("Error while trying to update config, your change will not survive server restart"));
                                    }
                                    return 1;
                                }))

                                .then(literal("self").executes(command -> {
                                    realPermission = null;

                                    command.getSource().sendSystemMessage(Component.literal("Changed real requirement to self-change and operators"));
                                    if (!JsonConfig.tryUpdate()) {
                                        command.getSource().sendSystemMessage(Component.literal("Error while trying to update config, your change will not survive server restart"));
                                    }
                                    return 1;
                                }))

                                .then(literal("everyone").executes(command -> {
                                    realPermission = new PermissionCheck.Require(new Permission.HasCommandLevel(PermissionLevel.ALL));

                                    command.getSource().sendSystemMessage(Component.literal("Removed real requirement. Every players can now see the real name of everyone else"));
                                    if (!JsonConfig.tryUpdate()) {
                                        command.getSource().sendSystemMessage(Component.literal("Error while trying to update config, your change will not survive server restart"));
                                    }
                                    return 1;
                                }))
                        )

                        .then(literal("grant_permission")
                                .then(argument("player", EntityArgument.player()).executes(command -> {
                                    String name = EntityArgument.getPlayer(command, "player").getName().getString();
                                    playerPermissionList.add(name);

                                    command.getSource().sendSystemMessage(Component.literal("Added " + name + " to the command permission list"));
                                    if (!JsonConfig.tryUpdate()) {
                                        command.getSource().sendFailure(Component.literal("Error while trying to update config, your change will not survive server restart"));
                                    }

                                    return 1;
                                })))

                        .then(literal("remove_permission")
                                .then(argument("player", EntityArgument.player()).executes(command -> {
                                    String name = EntityArgument.getPlayer(command, "player").getName().getString();
                                    playerPermissionList.remove(name);

                                    command.getSource().sendSystemMessage(Component.literal("Removed " + name + " from the command permission list"));
                                    if (!JsonConfig.tryUpdate()) {
                                        command.getSource().sendFailure(Component.literal("Error while trying to update config, your change will not survive server restart"));
                                    }

                                    return 1;
                                })))

                        .then(literal("reload").executes(command -> {
                            playerPermissionList.clear();
                            JsonConfig.load();
                            command.getSource().sendSystemMessage(Component.literal("Reloaded config from file"));
                            return 1;
                        }))
                )
        );

        getDispatcher().register(literal("fakename").executes(builder.getCommand()).redirect(builder));
    }

    @Unique
    private static int handleSetname$fakename(CommandSourceStack source, ServerPlayer player, String string) {
        if (!hasSetPermission$fakename(source, source.isPlayer() && source.getPlayer().equals(player))) {
            source.sendFailure(Component.literal("You do not have the permission to perform this command"));
            return 0;
        }


        if (string.replaceAll("&.?", "").length() > 16) {
            source.sendFailure(Component.literal("Vanilla allow a maximum of 16 characters!"));
            return 0;
        }


        string = string.replace("&", "§");
        MutableComponent component = Component.empty();
        Style hover = Style.EMPTY.withHoverEvent(new HoverEvent.ShowText(Component.literal(player.getName().getString())));
        Style color = Style.EMPTY.withColor(ChatFormatting.GREEN);

        component.append(Component.literal(player.getDisplayName().getString()).withStyle(hover).withStyle(color).withStyle(Style.EMPTY.withUnderlined(true)));
        component.append(Component.literal("'s name is now " + string));

        source.sendSystemMessage(component);
        memoryCache.put(player.getUUID(), string);
        refresh(player);

        if (!source.isPlayer() || !source.getPlayer().equals(player)) {
            player.sendSystemMessage(Component.literal("Fakename mod: Your name has been changed to " + string));
        }

        try {
            Files.writeString(fileCache.resolve(player.getName().getString() + ".txt").toFile().toPath(), string);
        } catch (Throwable exception) {
            try {
                source.sendFailure(Component.literal("An error happened! Please report the issue in the github or discord: https://github.com/matteo335/fakename https://discord.gg/2EyYeFPqRK" + exception));
            } catch (Throwable throwable) {
                throwable.fillInStackTrace();
                source.sendFailure(Component.literal("Unable to create file. The new username will not survive server restart"));
            }
        }

        return 1;
    }

    @Unique
    private static int handleClear$fakename(CommandSourceStack source, ServerPlayer player) {
        if (!hasClearPermission$fakename(source, source.isPlayer() && source.getPlayer().equals(player))) {
            source.sendFailure(Component.literal("You do not have the permission to perform this command"));
            return 0;
        }

        //noinspection ResultOfMethodCallIgnored
        fileCache.resolve(player.getName().getString() + ".txt").toFile().delete();
        memoryCache.put(player.getUUID(), player.getName().getString());
        refresh(player);

        source.sendSystemMessage(Component.literal(player.getName().getString() + "'s fake name was cleared!"));

        return 1;
    }

    @Unique
    private static int handleRealname$fakename(CommandSourceStack source, String string) {
        ServerPlayer player = source.getServer().getPlayerList().getPlayer(string);

        for (Map.Entry<UUID, String> entry : memoryCache.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(string)) {

                if (source.getServer().getPlayerList().getPlayer(entry.getKey()) == null) {
                    //noinspection DataFlowIssue
                    for (File file : fileCache.toFile().listFiles()) {
                        try {
                            if (Files.readString(file.toPath()).equals(string)) {

                                if (source.isPlayer() && !hasRealPermission$fakename(source)) {
                                    source.sendFailure(Component.literal("You do not have the permission to perform this command"));
                                    return 0;
                                }

                                source.sendSystemMessage(Component.literal(string + "'s real name is " + file.getName()));
                                return 1;
                            }
                        } catch (Throwable exception) {
                            source.sendSystemMessage(Component.literal("The command errored! Please send the server log in\nhttps://github.com/matteo335/fakename or https://discord.gg/2EyYeFPqRK"));
                            logger.error("Fakename exception, please report using the github or discord: https://github.com/matteo335/fakename https://discord.gg/2EyYeFPqRK\n" + exception.fillInStackTrace());
                        }
                    }
                } else {
                    String name = source.getServer().getPlayerList().getPlayerByName(entry.getValue()).getGameProfile().name();

                    if (source.isPlayer() && !source.getPlayer().getGameProfile().name().equalsIgnoreCase(name) && !hasRealPermission$fakename(source)) {
                        source.sendFailure(Component.literal("You do not have the permission to perform this command"));
                        return 0;
                    }

                    source.sendSystemMessage(Component.literal(string + "'s real name is " + name));
                    return 1;
                }
            }

            if (player != null && memoryCache.containsKey(player.getUUID())) {
                source.sendSystemMessage(Component.literal(string + "'s fakename is " + memoryCache.get(player.getUUID())));
                return 1;
            }
        }

        source.sendFailure(Component.literal("No player with that name was found!"));
        return 0;
    }

    @Unique
    private static boolean hasClearPermission$fakename(CommandSourceStack source, boolean self) {
        boolean isAdmin = source.getPlayer().permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.ADMINS));
        boolean inPlayerList = playerPermissionList != null && source.isPlayer() && playerPermissionList.stream().anyMatch(name -> name.equalsIgnoreCase(source.getPlayer().getGameProfile().name()));

        return self && clearPermission == null || isAdmin || inPlayerList || clearPermission != null && Commands.hasPermission(clearPermission).test(source);
    }

    @Unique
    private static boolean hasSetPermission$fakename(CommandSourceStack source, boolean self) {
        boolean isAdmin = source.getPlayer().permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.ADMINS));
        boolean inPlayerList = playerPermissionList != null && source.isPlayer() && playerPermissionList.stream().anyMatch(name -> name.equalsIgnoreCase(source.getPlayer().getGameProfile().name()));

        return self && setPermission == null || isAdmin || inPlayerList || setPermission != null && Commands.hasPermission(setPermission).test(source);
    }

    @Unique @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean hasRealPermission$fakename(CommandSourceStack source) {
        boolean isAdmin = source.getPlayer().permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.ADMINS));
        boolean inPlayerList = playerPermissionList != null && source.isPlayer() && playerPermissionList.stream().anyMatch(name -> name.equalsIgnoreCase(source.getPlayer().getGameProfile().name()));

        return isAdmin || inPlayerList || realPermission != null && Commands.hasPermission(realPermission).test(source);
    }
}
