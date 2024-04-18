package me.cominixo.betterf3.modules;

import java.util.Arrays;
import me.cominixo.betterf3.utils.DebugLine;
import me.cominixo.betterf3.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The Coordinates module.
 */
public class CoordsModule extends BaseModule {

  /**
   * The color for the x position.
   */
  public TextColor colorX;
  /**
   * The color for the y position.
   */
  public TextColor colorY;
  /**
   * The color for the z position.
   */
  public TextColor colorZ;

  /**
   * The default color for the x position.
   */
  public final TextColor defaultColorX = TextColor.fromLegacyFormat(ChatFormatting.RED);
  /**
   * The default color for the y position.
   */
  public final TextColor defaultColorY = TextColor.fromLegacyFormat(ChatFormatting.GREEN);
  /**
   * The default color for the z position.
   */
  public final TextColor defaultColorZ = TextColor.fromLegacyFormat(ChatFormatting.AQUA);

  /**
   * Instantiates a new Coordinates module.
   */
  public CoordsModule() {
    this.defaultNameColor = TextColor.fromLegacyFormat(ChatFormatting.RED);

    this.nameColor = defaultNameColor;
    this.colorX = this.defaultColorX;
    this.colorY = this.defaultColorY;
    this.colorZ = this.defaultColorZ;

    lines.add(new DebugLine("player_coords", "format.betterf3.coords", true));
    lines.add(new DebugLine("block_coords", "format.betterf3.coords", true));
    lines.add(new DebugLine("chunk_relative_coords", "format.betterf3.coords", true));
    lines.add(new DebugLine("chunk_coords", "format.betterf3.coords", true));
    lines.add(new DebugLine("velocity", "format.betterf3.coords", true));
    lines.add(new DebugLine("abs_velocity"));
    lines.add(new DebugLine("other_dimension_coords", "format.betterf3.coords", true));

    lines.get(2).inReducedDebug = true;
  }

  /**
   * Updates the Coordinates module.
   *
   * @param client the Minecraft client
   */
  public void update(final Minecraft client) {

    final Entity cameraEntity = client.getCameraEntity();

    final Component xyz =
    Utils.styledText("X", this.colorX).append(Utils.styledText("Y", this.colorY)).append(Utils.styledText("Z",
    this.colorZ));

    if (cameraEntity != null) {
      final String cameraX = String.format("%.3f", cameraEntity.getX());
      final String cameraY = String.format("%.5f", cameraEntity.getY());
      final String cameraZ = String.format("%.3f", cameraEntity.getZ());

      // Player coords
      lines.get(0).value(Arrays.asList(xyz, Utils.styledText(cameraX, this.colorX),
      Utils.styledText(cameraY, this.colorY), Utils.styledText(cameraZ, this.colorZ)));

      final BlockPos blockPos = cameraEntity.blockPosition();
      // Block coords
      lines.get(1).value(Arrays.asList(Utils.styledText(blockPos.getX(), this.colorX),
      Utils.styledText(blockPos.getY(), this.colorY), Utils.styledText(blockPos.getZ(), this.colorZ)));
      // Chunk Relative coords
      lines.get(2).value(Arrays.asList(Utils.styledText(blockPos.getX() & 15, this.colorX),
      Utils.styledText(blockPos.getY() & 15, this.colorY), Utils.styledText(blockPos.getZ() & 15, this.colorZ)));
      // Chunk coords
      lines.get(3).value(Arrays.asList(Utils.styledText(blockPos.getX() >> 4, this.colorX),
      Utils.styledText(blockPos.getY() >> 4, this.colorY), Utils.styledText(blockPos.getZ() >> 4, this.colorZ)));
      // Player velocity
      final Entity vehicle = cameraEntity.getRootVehicle();
      final int ticksPerSecond = 20;
      final Vec3 velocity = vehicle != null ? vehicle.getDeltaMovement() : cameraEntity.getDeltaMovement();
      final String vX = String.format("%.3f", velocity.x() * ticksPerSecond);
      final String vY = String.format("%.3f", velocity.y() * ticksPerSecond);
      final String vZ = String.format("%.3f", velocity.z() * ticksPerSecond);
      lines.get(4).value(Arrays.asList(Utils.styledText(vX, this.colorX),
      Utils.styledText(vY, this.colorY), Utils.styledText(vZ, this.colorZ)));
      lines.get(5).value(Utils.styledText(String.format("%.3f", velocity.length() * ticksPerSecond), this.defaultNameColor));

      // Other dimension coords (nether coords when in overworld and vise versa)
      final var dimension = client.level.dimension();
      if (dimension == Level.OVERWORLD || dimension == Level.NETHER) {
        final float dimensionalMultiplier = dimension == Level.OVERWORLD ? 1f / 8f : 8f;
        final String lable = I18n.get("text.betterf3.line." +
          (dimension == Level.OVERWORLD ? "nether" : "overworld"));

        final String cameraOtherDimX = String.format("%.3f", cameraEntity.getX() * dimensionalMultiplier);
        final String cameraOtherDimZ = String.format("%.3f", cameraEntity.getZ() * dimensionalMultiplier);

        lines.get(6).value(Arrays.asList(lable, Utils.styledText(cameraOtherDimX, this.colorX),
          Utils.styledText(cameraY, this.colorY), Utils.styledText(cameraOtherDimZ, this.colorZ)));
      } else {
        lines.get(6).active = false;
      }
    }
  }
}
