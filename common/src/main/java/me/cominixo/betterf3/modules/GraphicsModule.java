package me.cominixo.betterf3.modules;

import me.cominixo.betterf3.utils.DebugLine;
import net.minecraft.ChatFormatting;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.StringUtils;

/**
 * The Graphics module.
 */
public class GraphicsModule extends BaseModule {

  /**
   * Instantiates a new Graphics module.
   */
  public GraphicsModule() {
    this.defaultNameColor = TextColor.fromLegacyFormat(ChatFormatting.GOLD);
    this.defaultValueColor = TextColor.fromLegacyFormat(ChatFormatting.AQUA);

    this.nameColor = defaultNameColor;
    this.valueColor = defaultValueColor;

    lines.add(new DebugLine("render_distance"));
    lines.add(new DebugLine("graphics"));
    lines.add(new DebugLine("clouds"));
    lines.add(new DebugLine("biome_blend_radius"));
    lines.add(new DebugLine("shader"));
  }

  /**
   * Updates the Graphics module.
   *
   * @param client the Minecraft client
   */
  public void update(final Minecraft client) {

    final String cloudString = client.options.cloudStatus().get() == CloudStatus.OFF ? I18n.get("text" +
    ".betterf3.line.off")
    : (client.options.cloudStatus().get() == CloudStatus.FAST ? I18n.get("text.betterf3.line.fast") :
    I18n.get("text" +
    ".betterf3.line.fancy"));

    // Render Distance
    lines.get(0).value(client.levelRenderer.lastViewDistance);
    // Graphics
    lines.get(1).value(StringUtils.capitalize(client.options.graphicsPreset().get().toString()));
    // Clouds
    lines.get(2).value(cloudString);
    // Biome Blend Radius
    lines.get(3).value(client.options.biomeBlendRadius().get());

    // Shader
    final Identifier shaderEffect = client.gameRenderer.currentPostEffect();
    if (shaderEffect != null) {
      lines.get(4).value(String.valueOf(shaderEffect));
    } else {
      lines.get(4).active = false;
    }

    lines.get(0).inReducedDebug = true;
    lines.get(3).inReducedDebug = true;
  }
}
