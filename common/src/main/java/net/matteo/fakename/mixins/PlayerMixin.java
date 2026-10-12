package net.matteo.fakename.mixins;

import static net.matteo.fakename.Utils.*;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void getDisplayName$fakename(CallbackInfoReturnable<Component> cir) {
        if ((Object) this instanceof Player player) {
            cir.setReturnValue(Component.literal(profileName(name(player))));
        }
    }


    @Inject(method = "getScoreboardName", at = @At("RETURN"), cancellable = true)
    private void getScoreboardName$fakename(CallbackInfoReturnable<String> cir) {
        if ((Object) this instanceof Player player) {
            cir.setReturnValue(profileName(name(player)));
        }
    }
}
