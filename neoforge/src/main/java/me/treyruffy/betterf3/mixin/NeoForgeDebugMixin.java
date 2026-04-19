package me.treyruffy.betterf3.mixin;

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
 * The Debug Screen Overlay.
 */
@Mixin(DebugScreenOverlay.class)
@SuppressWarnings("NullAway.Init")
public abstract class NeoForgeDebugMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private Font font;

    /**
     * Renders the text on the screen.
     *
     * @param graphics    the draw context
     * @param lines       the list of strings
     * @param alignLeft   the left side
     * @param ci          the callback info
     */
    @Inject(method = "extractLines", at = @At(value = "HEAD"), cancellable = true, order = 2000)
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
