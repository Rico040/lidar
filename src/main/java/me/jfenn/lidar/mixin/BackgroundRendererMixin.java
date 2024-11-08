package me.jfenn.lidar.mixin;

import me.jfenn.lidar.Lidar;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {

    @Shadow
    private static float red;

    @Shadow
    private static float green;

    @Shadow
    private static float blue;

    @Inject(method = "render(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/world/ClientWorld;IF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/BackgroundRenderer;lastWaterFogColorUpdateTime:J", ordinal = 6))
    private static void modifyColors(Camera camera, float tickDelta, ClientWorld world, int i, float f, CallbackInfo ci) {
        if (Lidar.INSTANCE.getConfig().isActive()) {
            red = 0f;
            green = 0f;
            blue = 0f;
        }
    }

    @ModifyConstant(
            method = "render",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/util/CubicSampler;sampleColor(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/CubicSampler$RgbFetcher;)Lnet/minecraft/util/math/Vec3d;")),
            constant = @Constant(intValue = 4, ordinal = 0)
    )
    private static int renderSkyColor(int original) {
        if (Lidar.INSTANCE.getConfig().isActive()) {
            return 0;
        } else {
            return original;
        }
    }
}
