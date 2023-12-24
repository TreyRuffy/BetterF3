package me.cominixo.betterf3.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * FPS count accessor.
 */
@Mixin(MinecraftClient.class)
public interface ClientAccessor {
  /**
   * Gets the current client FPS.
   *
   * @return the current client FPS
   */
  @Accessor("currentFps")
  static int betterF3$getCurrentFps() {
    throw new AssertionError();
  }
}
