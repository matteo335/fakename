package net.matteo.fakename.mixins;

import static net.matteo.fakename.Utils.*;

import static net.minecraft.commands.Commands.literal;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Collections;
import java.nio.file.Files;

@Mixin(Commands.class)
public abstract class CommandsMixin {

    @Shadow public abstract CommandDispatcher<CommandSourceStack> getDispatcher();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init$fakename(Commands.CommandSelection pSelection, CommandBuildContext pContext, CallbackInfo ci) {

        LiteralArgumentBuilder<CommandSourceStack> builder = literal("fakename")

                .then(
                        literal("real")
                                .then(
                                        Commands.argument("fakename", StringArgumentType.string())
                                                .executes((cmd) -> handleRealname$fakename(cmd.getSource(), cmd.getArgument("fakename", String.class)))
                                )
                )

                .then(
                        literal("clear")
                                .then(
                                        Commands.argument("target", EntityArgument.players())
                                                .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                                                .executes((cmd) -> handleClear$fakename(cmd.getSource(), EntityArgument.getPlayers(cmd, "target"))))
                                .executes((cmd) -> handleClear$fakename(cmd.getSource(), Collections.singleton(cmd.getSource().getPlayerOrException())))
                )

                .then(
                        literal("set")
                                .then(
                                        Commands.argument("fakename", StringArgumentType.string())
                                                .executes((cmd) -> handleSetname$fakename(cmd.getSource(), Collections.singleton(cmd.getSource().getPlayerOrException()), StringArgumentType.getString(cmd, "fakename")))
                                )
                                .then(
                                        Commands.argument("target", EntityArgument.players())
                                                .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                                                .then(
                                                        Commands.argument("fakename", StringArgumentType.string())
                                                                .executes((cmd) -> handleSetname$fakename(cmd.getSource(),EntityArgument.getPlayers(cmd, "target"),StringArgumentType.getString(cmd,"fakename")))
                                                )
                                )

                );

        getDispatcher().register(builder);
    }

    @Unique
    private static int handleSetname$fakename(CommandSourceStack source, Collection<ServerPlayer> players, String string) {
        string = string.replace("&", "§") + "§r";

        for (ServerPlayer player : players) {
            memoryCache.put(player.getUUID(), string);
            refresh(player);
            source.sendSystemMessage(Component.literal(player.getName().getString() + "'s name is now " + string));

            try {
                Files.writeString(fileCache.resolve(profileName(player.getName().getString()) + ".txt").toFile().toPath(), string);
            } catch (Throwable exception) {
                try {
                    Files.writeString(fileCache.getParent().getParent().resolve("logs").resolve("fakename.txt").toFile().toPath(), exception.toString());
                    source.sendSystemMessage(Component.literal("An error happened! Please report the issue to Mattéo"));
                } catch (Throwable throwable) {
                    throwable.fillInStackTrace();
                    source.sendSystemMessage(Component.literal("Unable to create file. The new username will not survive server restart"));
                }
            }
        }

        return 1;
    }

    @Unique
    private static int handleClear$fakename(CommandSourceStack source, Collection<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            //noinspection ResultOfMethodCallIgnored
            fileCache.resolve(profileName(player.getName().getString()) + ".txt").toFile().delete();
            memoryCache.put(player.getUUID(), player.getName().getString());
            refresh(player);

            source.sendSystemMessage(Component.literal(player.getName().getString() + "'s fake name was cleared!"));
        }

        return 1;
    }

    @Unique
    private static int handleRealname$fakename(CommandSourceStack source, String string) {
        PlayerList players = source.getServer().getPlayerList();
        boolean succ = false;
        for (ServerPlayer player : players.getPlayers()) {
            source.sendSystemMessage(Component.literal(string + "'s real name is " + player.getName().getString()));
            succ = true;
        }

        if(succ)
            return 1;

        source.sendFailure(Component.literal("No player with that name was found!"));
        return 0;
    }
}
