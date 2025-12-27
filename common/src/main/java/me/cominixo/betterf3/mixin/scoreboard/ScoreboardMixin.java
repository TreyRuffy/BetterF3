package me.cominixo.betterf3.mixin.scoreboard;

import me.cominixo.betterf3.config.GeneralOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to cancel sidebar rendering during F3.
 */
@Mixin(Gui.class)
public class ScoreboardMixin {

  @Shadow @Final private Minecraft minecraft;

  /**
   * Cancels sidebar rendering if the {@link GeneralOptions#hideSidebar} setting is true.
   *
   * @param info Callback info
   */
  @Inject(at = @At("HEAD"), method = "displayScoreboardSidebar", cancellable = true)
  public void init(final CallbackInfo info) {
    if (!GeneralOptions.disableMod && GeneralOptions.hideSidebar && this.minecraft.debugEntries.isOverlayVisible()) {
      info.cancel();
    }
  }
}
