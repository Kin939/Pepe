package ru.pepe.autosell.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.pepe.autosell.Pepe;

@Mixin(Gui.class)
abstract class GuiMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void pepe$hideMerchant(Screen screen, CallbackInfo ci) {
        if (Pepe.interceptScreen(screen)) ci.cancel();
    }
}
