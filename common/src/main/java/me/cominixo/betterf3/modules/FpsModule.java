package me.cominixo.betterf3.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.cominixo.betterf3.utils.DebugLine;
import me.cominixo.betterf3.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextColor;

/**
 * The FPS module.
 */
public class FpsModule extends BaseModule {

  /**
   * The color for high fps.
   */
  public TextColor colorHigh;

  /**
   * The color for medium fps.
   */
  public TextColor colorMed;

  /**
   * The color for low fps.
   */
  public TextColor colorLow;

  /**
   * The default color for high fps.
   */
  public final TextColor defaultColorHigh = TextColor.fromLegacyFormat(ChatFormatting.GREEN);

  /**
   * The default color for medium fps.
   */
  public final TextColor defaultColorMed = TextColor.fromLegacyFormat(ChatFormatting.YELLOW);

  /**
   * The default color for low fps.
   */
  public final TextColor defaultColorLow = TextColor.fromLegacyFormat(ChatFormatting.RED);

  /**
   * The millisecond times of the first time update sent by the server.
   */
  public static long firstTimeUpdate = Long.MAX_VALUE;

  /**
   * The millisecond times of all time updates sent by the server during the last 30 seconds.
   */
  public static final List<Long> lastTimeUpdates = new ArrayList<>();

  /**
   * Instantiates a new FPS module.
   */
  public FpsModule() {
    lines.add(new DebugLine("fps", "format.betterf3.no_format", true));
    lines.add(new DebugLine("tps", "format.betterf3.no_format", true));
    lines.get(0).inReducedDebug = true;

    this.colorHigh = this.defaultColorHigh;
    this.colorMed = this.defaultColorMed;
    this.colorLow = this.defaultColorLow;
  }

  /**
   * Updates the FPS module.
   *
   * @param client the Minecraft client
   */
  public void update(final Minecraft client) {
    final int currentFps = client.getFps();

    final String fpsString = I18n
      .get("format.betterf3.fps", currentFps,
        (double) client.options.framerateLimit().get() == Options.UNLIMITED_FRAMERATE_CUTOFF ?
          I18n.get("text.betterf3.line.fps.unlimited") :
          client.options.framerateLimit().get(),
        client.options.enableVsync().get() ?
          I18n.get("text.betterf3.line.fps.vsync") : "")
      .trim();

    final TextColor fpsColor = switch (Utils.fpsColor(currentFps)) {
      case HIGH -> this.colorHigh;
      case MEDIUM -> this.colorMed;
      case LOW -> this.colorLow;
    };

    lines.get(0).value(Collections.singletonList(Utils.styledText(fpsString, fpsColor)));

    while (lastTimeUpdates.size() > 2 && System.currentTimeMillis() - lastTimeUpdates.getFirst() > 30000) {
      lastTimeUpdates.removeFirst();
    }
    final int secondsMeasured = (int) Math.max(Math.min(Math.ceil((System.currentTimeMillis() - firstTimeUpdate) / 1000F), 30), 0);
    final int currentTps = lastTimeUpdates.size() < 2 ? 0 : Math.round(20000F / Math.max(
      System.currentTimeMillis() - lastTimeUpdates.getLast(),
      lastTimeUpdates.getLast() - lastTimeUpdates.get(lastTimeUpdates.size() - 2)
    ));
    final int recentTps = secondsMeasured == 0 ? 0 : Math.round(20F * lastTimeUpdates.size() / secondsMeasured);

    final String tpsString = I18n.get("format.betterf3.tps", currentTps, recentTps, secondsMeasured).trim();

    final TextColor tpsColor = switch (Utils.tpsColor(currentTps)) {
      case HIGH -> this.colorHigh;
      case MEDIUM -> this.colorMed;
      case LOW -> this.colorLow;
    };

    lines.get(1).value(Collections.singletonList(Utils.styledText(tpsString, tpsColor)));
  }
}
