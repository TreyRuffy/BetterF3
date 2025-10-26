package me.treyruffy.betterf3.mixin;

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
 * The Debug Screen Overlay.
 */
@Mixin(DebugScreenOverlay.class)
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
   * @param guiGraphics the draw context
   * @param list        the list of strings
   * @param bl          the left side
   * @param ci          the callback info
   */
  @Inject(method = "renderLines", at = @At(value = "HEAD"), cancellable = true, order = 2000)
  public void drawText(final GuiGraphics guiGraphics, final List<String> list, final boolean bl, final CallbackInfo ci) {

    if (GeneralOptions.disableMod || !this.minecraft.debugEntries.isF3Visible()) {
      return;
    }

    if (bl) {
      final List<Component> leftList = DebugRenderer.newText(this.minecraft, true, Collections.emptyList(), Collections.emptyList());
      DebugRenderer.drawLeftText(leftList, guiGraphics, this.minecraft, this.font, Collections.emptyList());
    } else {
      final List<Component> rightList = DebugRenderer.newText(this.minecraft, false, Collections.emptyList(), Collections.emptyList());
      DebugRenderer.drawRightText(rightList, guiGraphics, this.minecraft, this.font, Collections.emptyList());
    }

    ci.cancel();
  }
}
