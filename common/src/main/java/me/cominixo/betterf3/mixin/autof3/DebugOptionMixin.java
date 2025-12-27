package me.cominixo.betterf3.mixin.autof3;

import me.cominixo.betterf3.config.GeneralOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Sets automatically opening the debug screen in the options.
 */
@Mixin(DebugScreenOverlay.class)
public abstract class DebugOptionMixin {

  @Shadow
  public boolean renderFpsCharts;
  @Shadow
  public boolean renderProfilerChart;
  @Final
  @Shadow private Minecraft minecraft;

  /**
   * Toggles the packet size and ping charts.
   */
  @Shadow public abstract void toggleNetworkCharts();

  /**
   * Sets the debug option to true.
   *
   * @param minecraft the minecraft
   * @param ci the callback info
   */
  @Inject(method = "<init>", at = @At("RETURN"))
  public void addAutomaticDebugOption(final Minecraft minecraft, final CallbackInfo ci) {
    if (!GeneralOptions.disableMod && GeneralOptions.autoF3) {
      this.minecraft.debugEntries.setOverlayVisible(true);
      if (GeneralOptions.alwaysEnableProfiler) this.renderProfilerChart = true;
      if (GeneralOptions.alwaysEnableTPS) this.renderFpsCharts = true;
      if (GeneralOptions.alwaysEnablePing) this.toggleNetworkCharts();
    }
  }

  @Inject(method = "reset", at = @At("RETURN"))
  private void automaticF3(final CallbackInfo ci) {
    if (!GeneralOptions.disableMod && GeneralOptions.autoF3) {
      this.minecraft.debugEntries.setOverlayVisible(true);
      if (GeneralOptions.alwaysEnableProfiler) this.renderProfilerChart = true;
      if (GeneralOptions.alwaysEnableTPS) this.renderFpsCharts = true;
      if (GeneralOptions.alwaysEnablePing) this.toggleNetworkCharts();
    }
  }

  @Inject(method = "showDebugScreen", at = @At("HEAD"), cancellable = true)
  private void shouldRenderDebug(final CallbackInfoReturnable<Boolean> cir) {
    if (!GeneralOptions.disableMod && GeneralOptions.autoF3 && this.minecraft.debugEntries.isOverlayVisible() && !this.minecraft.options.hideGui) {
      cir.setReturnValue(this.minecraft.level != null);
    }
  }

}
