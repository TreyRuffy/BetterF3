package me.cominixo.betterf3.mixin;

import java.util.Collections;
import java.util.List;
import me.cominixo.betterf3.config.GeneralOptions;
import me.cominixo.betterf3.utils.DebugRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
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
   * @param guiGraphics Draw Context
   * @param list        List of strings
   * @param bl          If {@code true}, renders on the left side; if {@code false}, renders on the right side.
   * @param ci          Callback info
   */
  @Inject(method = "renderLines", at = @At("HEAD"), cancellable = true)
  public void drawText(final GuiGraphics guiGraphics, final List<String> list, final boolean bl, final CallbackInfo ci) {

    if (GeneralOptions.disableMod || !this.minecraft.debugEntries.isF3Visible()) {
      return;
    }

    if (bl) {
      final List<Component> leftList = DebugRenderer.newText(this.minecraft, true, Collections.emptyList(), Collections.emptyList());
      DebugRenderer.drawLeftText(leftList, guiGraphics, this.minecraft, this.font, null);
    } else {
      final List<Component> rightList = DebugRenderer.newText(this.minecraft, false, Collections.emptyList(), Collections.emptyList());
      DebugRenderer.drawRightText(rightList, guiGraphics, this.minecraft, this.font, null);
    }

    ci.cancel();
  }
}
