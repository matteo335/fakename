package net.matteo.fakename.mixins;

import static net.matteo.fakename.Utils.*;

import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.chat.Component;

import com.mojang.authlib.GameProfile;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@SuppressWarnings({"rawtypes", "DataFlowIssue"})
@Mixin(ClientboundPlayerInfoUpdatePacket.class)
public class ClientboundPlayerInfoUpdatePacketMixin {

    @Mutable @Shadow @Final
    private List<ClientboundPlayerInfoUpdatePacket.Entry> entries;

    @Inject(method = "<init>(Ljava/util/EnumSet;Ljava/util/Collection;)V", at = @At("RETURN"))
    private void init$fakename(EnumSet actions, Collection players, CallbackInfo ci) {
        List<ClientboundPlayerInfoUpdatePacket.Entry> newEntries = new ArrayList<>(entries);

        for (int i = 0; i < newEntries.size(); i++) {
            ClientboundPlayerInfoUpdatePacket.Entry entry = newEntries.get(i);

            String fakename = memoryCache.get(entry.profileId());

            GameProfile old = entry.profile();
            GameProfile profile = (old == null || !fakeProfile) ? old
                    : new GameProfile(old.id(), profileName(fakename), old.properties());

            if (fakename != null) {
                newEntries.set(i, new ClientboundPlayerInfoUpdatePacket.Entry(
                        entry.profileId(),
                        profile,
                        entry.listed(),
                        entry.latency(),
                        entry.gameMode(),
                        Component.literal(profileName(fakename)),
                        entry.showHat(),
                        entry.listOrder(),
                        entry.chatSession()
                ));
            }

        }

        entries = newEntries;
    }
}