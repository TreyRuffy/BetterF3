package me.cominixo.betterf3.mixin;

import me.cominixo.betterf3.config.gui.ModConfigScreen;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Modifies the debug keys (f3 / f3 + m).
 */
@Mixin(KeyboardHandler.class)
@SuppressWarnings("NullAway.Init")
public abstract class KeyboardMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    protected abstract void debugFeedbackComponent(Component component);

    /**
     * Adds the config menu by pressing f3 + m.
     *
     * @param event the key event
     * @param cir Callback info
     */
    @Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
    public void processF3(final KeyEvent event, final CallbackInfoReturnable<Boolean> cir) {
        final int key = event.key();
        if (key == 77) { // Key m
            this.minecraft.setScreen(new ModConfigScreen(null));
            cir.setReturnValue(true);
        } else if (key == 70) {
            if (event.hasControlDown()) {
                this.minecraft
                        .options
                        .simulationDistance()
                        .set(Mth.clamp(
                                (this.minecraft.options.simulationDistance().get() + (event.hasShiftDown() ? -1 : 1)),
                                5,
                                32));
                this.debugFeedbackComponent(Component.translatable(
                        "debug.betterf3.cycle_simulationdistance.message",
                        this.minecraft.options.simulationDistance().get()));
            } else {
                this.minecraft
                        .options
                        .renderDistance()
                        .set(Mth.clamp(
                                (this.minecraft.options.renderDistance().get() + (event.hasShiftDown() ? -1 : 1)),
                                2,
                                32));
                this.debugFeedbackComponent(Component.translatable(
                        "debug.betterf3.cycle_renderdistance.message",
                        this.minecraft.options.renderDistance().get()));
            }
            cir.setReturnValue(true);
        }
    }
}
