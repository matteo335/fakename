package net.matteo.fakename.mixins;

import static net.matteo.fakename.utils.Utils.*;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void getDisplayName(CallbackInfoReturnable<Component> cir) {
        //noinspection ConstantValue, resource
        if ((Object) this instanceof Player player && player.level().getServer() != null) {
            String name = name(player);
            if (name != null) cir.setReturnValue(Component.literal(name));
        }
    }


    @Inject(method = "getScoreboardName", at = @At("RETURN"), cancellable = true)
    private void getScoreboardName(CallbackInfoReturnable<String> cir) {
        //noinspection ConstantValue, resource
        if ((Object) this instanceof Player player && player.level().getServer() != null) {
            String name = name(player);
            //noinspection resource
            if (name != null && player.level().getServer() != null) cir.setReturnValue(name);
        }
    }
}
