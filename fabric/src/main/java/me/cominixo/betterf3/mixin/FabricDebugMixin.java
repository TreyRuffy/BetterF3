package me.cominixo.betterf3.mixin;

import java.util.Collections;
import java.util.List;
import me.cominixo.betterf3.config.GeneralOptions;
import me.cominixo.betterf3.utils.DebugRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Debug Lambda Mixin.
 */
@Mixin(DebugScreenOverlay.class)
@SuppressWarnings("NullAway.Init")
public abstract class FabricDebugMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private Font font;

    /**
     * Renders the text on either the left or right side of the screen, depending on the {@code bl} parameter.
     *
     * @param graphics    Draw Context
     * @param lines       List of strings
     * @param alignLeft   If {@code true}, renders on the left side; if {@code false}, renders on the right side.
     * @param ci          Callback info
     */
    @Inject(method = "extractLines", at = @At("HEAD"), cancellable = true)
    public void drawText(
            final GuiGraphicsExtractor graphics,
            final List<String> lines,
            final boolean alignLeft,
            final CallbackInfo ci) {

        if (GeneralOptions.disableMod || !this.minecraft.debugEntries.isOverlayVisible()) {
            return;
        }

        if (alignLeft) {
            final List<Component> leftList =
                    DebugRenderer.newText(this.minecraft, true, lines, Collections.emptyList());
            DebugRenderer.drawLeftText(leftList, graphics, this.minecraft, this.font, null);
        } else {
            final List<Component> rightList =
                    DebugRenderer.newText(this.minecraft, false, Collections.emptyList(), lines);
            DebugRenderer.drawRightText(rightList, graphics, this.minecraft, this.font, null);
        }

        ci.cancel();
    }
}
