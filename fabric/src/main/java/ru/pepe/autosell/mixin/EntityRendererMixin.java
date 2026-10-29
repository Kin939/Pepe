package ru.pepe.autosell.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.pepe.autosell.Pepe;

@Mixin(EntityRenderer.class)
abstract class EntityRendererMixin {
    @Inject(method = "finalizeRenderState", at = @At("TAIL"))
    private void pepe$tradeOutline(Entity entity, EntityRenderState state, CallbackInfo ci) {
        if (entity instanceof Villager) {
            int color = Pepe.outline(entity.getUUID());
            if (color != 0) state.outlineColor = color;
        }
    }
}
