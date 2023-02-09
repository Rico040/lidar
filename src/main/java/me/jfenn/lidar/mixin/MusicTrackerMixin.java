package me.jfenn.lidar.mixin;

import me.jfenn.lidar.Lidar;
import me.jfenn.lidar.config.LidarConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MusicTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicTracker.class)
public abstract class MusicTrackerMixin {

    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    public void tick(CallbackInfo ci) {
        LidarConfig config = Lidar.INSTANCE.getConfig();
        // if the mod is enabled & the player *is not* in the main menu...
        if (config.isActive() && MinecraftClient.getInstance().world != null) {
            // don't play any automatic music
            ci.cancel();
        }
    }

}
