package me.cominixo.betterf3.mixin;

import me.cominixo.betterf3.config.GeneralOptions;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.cominixo.betterf3.utils.Utils.START_X_POS;
import static me.cominixo.betterf3.utils.Utils.closingAnimation;
import static me.cominixo.betterf3.utils.Utils.xPos;

/**
 * Mixin for DebugScreenEntryList.
 */
@Mixin(DebugScreenEntryList.class)
public abstract class DebugScreenEntryListMixin {

  @Shadow
  public boolean isOverlayVisible;

  /**
   * Rebuilds the current list.
   */
  @Shadow
  public abstract void rebuildCurrentList();

  @Inject(method = "setOverlayVisible", at = @At("HEAD"), cancellable = true)
  private synchronized void onSetOverlayVisible(final boolean visible, final CallbackInfo ci) {
    if (GeneralOptions.disableMod) {
      return;
    }
    if (GeneralOptions.enableAnimations) {
      if (this.isOverlayVisible && !visible && !closingAnimation) {
        closingAnimation = true;
      } else if (!this.isOverlayVisible && visible) {
        closingAnimation = false;
        xPos = START_X_POS;
        this.isOverlayVisible = true;
        this.rebuildCurrentList();
      }
    } else {
      this.isOverlayVisible = visible;
      this.rebuildCurrentList();
    }
    ci.cancel();
  }
}
