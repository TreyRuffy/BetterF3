package me.cominixo.betterf3.mixin.time;

import io.netty.channel.ChannelHandlerContext;
import me.cominixo.betterf3.modules.FpsModule;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to count time updates sent by the server.
 */
@Mixin(Connection.class)
public class TimeUpdateMixin {

  /**
   * Used to catch the first two time updates received after joining the server as they're usually out of sync with the others.
   */
  private int skipTimeUpdates = 2;

  /**
   * Counts time updates sent by the server.
   *
   * @param context Channel Handler Context
   * @param packet Time Update Packet
   * @param info Callback info
   */
  @Inject(at = @At("HEAD"), method = "channelRead0")
  private void onPacket(final ChannelHandlerContext context, final Packet<?> packet, final CallbackInfo info) {
    if (packet instanceof ClientboundLoginPacket) {
      this.skipTimeUpdates = 2;
      FpsModule.firstTimeUpdate = Long.MAX_VALUE;
      FpsModule.lastTimeUpdates.clear();
    } else if (packet instanceof ClientboundSetTimePacket) {
      if (this.skipTimeUpdates > 0) {
        this.skipTimeUpdates--;
      } else {
        FpsModule.firstTimeUpdate = Math.min(FpsModule.firstTimeUpdate, System.currentTimeMillis());
        FpsModule.lastTimeUpdates.add(System.currentTimeMillis());
      }
    }
  }
}
