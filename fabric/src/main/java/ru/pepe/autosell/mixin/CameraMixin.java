package ru.pepe.autosell.mixin;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.pepe.autosell.Pepe;

/** A temporary multiplier, never writes the saved FOV option. */
@Mixin(Camera.class)
abstract class CameraMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void pepe$breathingZoom(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(Pepe.adjustFov(cir.getReturnValue()));
    }
}
