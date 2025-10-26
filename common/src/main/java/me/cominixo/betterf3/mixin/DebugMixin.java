package me.cominixo.betterf3.mixin;

import java.util.Collection;
import java.util.List;
import me.cominixo.betterf3.config.GeneralOptions;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.debug.DebugEntryNoop;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.cominixo.betterf3.utils.Utils.START_X_POS;
import static me.cominixo.betterf3.utils.Utils.closingAnimation;
import static me.cominixo.betterf3.utils.Utils.lastAnimationUpdate;
import static me.cominixo.betterf3.utils.Utils.xPos;

/**
 * The Debug Screen Overlay.
 */
@Mixin(DebugScreenOverlay.class)
public abstract class DebugMixin {

  /**
   * Toggles the debug HUD.
   */
  @Final
  @Shadow private Minecraft minecraft;

  @Unique
  private static final ResourceLocation BETTERF3_RESOURCE = DebugScreenEntries.register("betterf3", new DebugEntryNoop());

  @Unique
  private static final List<ResourceLocation> BETTERF3_LIST = List.of(BETTERF3_RESOURCE);

  /**
   * Ensures that the TPS graph works.
   *
   * @param context Draw Context
   * @param ci Callback info
   */
  @Inject(method = "render", at = @At(value = "HEAD"))
  public void renderBefore(final GuiGraphics context, final CallbackInfo ci) {
    if (GeneralOptions.disableMod || !this.minecraft.debugEntries.isF3Visible()) {
      return;
    }
    context.pose().pushMatrix();
  }

  @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/debug/DebugScreenEntryList;getCurrentlyEnabled()Ljava/util/Collection;"))
  private Collection<ResourceLocation> currentlyEnabled(final DebugScreenEntryList instance) {
    if (!GeneralOptions.disableMod && this.minecraft.debugEntries.isF3Visible()) {
      return BETTERF3_LIST;
    }
    return instance.getCurrentlyEnabled();
  }

  @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/debug/DebugScreenEntryList;isF3Visible()Z"))
  private boolean isF3Visible(final DebugScreenEntryList instance) {
    if (!GeneralOptions.disableMod && this.minecraft.debugEntries.isF3Visible()) {
      return false;
    }
    return instance.isF3Visible();
  }

  /**
   * Modifies the font scale.
   *
   * @param context Draw Context
   * @param ci Callback info
   */
  @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/debug/DebugScreenEntryList;getCurrentlyEnabled()Ljava/util/Collection;", shift = At.Shift.AFTER))
  public void renderFontScaleBefore(final GuiGraphics context, final CallbackInfo ci) {
    if (!GeneralOptions.disableMod && this.minecraft.debugEntries.isF3Visible()) {
      context.pose().scale((float) GeneralOptions.fontScale, (float) GeneralOptions.fontScale);
    }
  }

  /**
   * Renders the animation.
   *
   * @param context Draw Context
   * @param ci Callback info
   */
  @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V"))
  public synchronized void renderAnimation(final GuiGraphics context, final CallbackInfo ci) {

    if (GeneralOptions.disableMod) {
      return;
    }
    if (!GeneralOptions.enableAnimations) {
      return;
    } // Only displays the animation if set to true

    final long time = Util.getMillis();
    if (time - lastAnimationUpdate >= 10 && (xPos != 0 || closingAnimation)) {

      int i = ((START_X_POS / 2 + xPos) / 10) - 9;

      if (xPos != 0 && !closingAnimation) {
        xPos = (int) (xPos / GeneralOptions.animationSpeed);
        xPos -= i;
      }

      if (i == 0) {
        i = 1;
      }

      if (closingAnimation) {

        xPos += i;
        xPos = (int) (xPos * GeneralOptions.animationSpeed);

        if (xPos >= 300) {
          this.minecraft.debugEntries.setF3Visible(false);
          closingAnimation = false;
        }

      }

      lastAnimationUpdate = time;
    }
  }

}
