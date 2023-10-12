package me.treyruffy.betterf3.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Forge InGame GUI Mixin.
 */
@Mixin(ForgeGui.class)
public abstract class ForgeGuiMixin {

  /**
   * Minecraft Client.
   *
   * @return the minecraft client
   */
  @SuppressWarnings("checkstyle:MethodName")
  @Shadow
  public abstract MinecraftClient getMinecraft();

  /**
   * Modifies the F3 Menu from Forge's to BetterF3.
   *
   * @param width width
   * @param height width
   * @param guiGraphics the draw context
   * @param ci Callback info
   */
  @Inject(remap = false, method = "renderHUDText", at = @At(value = "INVOKE", opcode = Opcodes.PUTFIELD, target =
  "Lnet/minecraftforge/client/gui/overlay/ForgeGui$OverlayAccess;update()V"), cancellable = true)
  public void customDebugMenu(final int width, final int height, final DrawContext guiGraphics, final @NotNull CallbackInfo ci) {
    // Sets up BetterF3's debug screen
    this.getMinecraft().getDebugHud().render(guiGraphics);

    this.getMinecraft().getProfiler().pop();

    // Cancels the rest of the code from running, which replaces Forge's debug screen.
    ci.cancel();
  }
}
