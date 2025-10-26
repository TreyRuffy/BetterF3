package me.cominixo.betterf3.utils;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import me.cominixo.betterf3.config.GeneralOptions;
import me.cominixo.betterf3.config.gui.modules.ModulesScreen;
import me.cominixo.betterf3.modules.BaseModule;
import me.cominixo.betterf3.modules.MiscLeftModule;
import me.cominixo.betterf3.modules.MiscRightModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import static me.cominixo.betterf3.utils.Utils.xPos;

/**
 * Universal render methods to render the debug screen.
 */
public final class DebugRenderer {

  private DebugRenderer() {
    // Do nothing
  }

  /**
   * Lets us draw in batches.
   *
   * @param minecraft   The Minecraft instance
   * @param font        The font renderer
   * @param pos         The position
   * @param list        The list of Text
   * @param guiGraphics The Draw Context
   */
  public static void drawBackground(final Minecraft minecraft, final Font font, final PositionEnum pos,
                                    final List<Component> list, final GuiGraphics guiGraphics) {

    for (int i = 0; i < list.size(); i++) {
      final int height = 9;
      final int width = font.width(list.get(i).getString());
      if (width == 0) {
        continue;
      }
      final int y = 2 + height * i;

      int x1;
      int x2;
      int y1;
      int y2;
      int j;

      int windowWidth;
      if (pos == PositionEnum.RIGHT) {
        windowWidth = (int) (minecraft.getWindow().getGuiScaledWidth() / GeneralOptions.fontScale) - 2 - width;
        if (GeneralOptions.enableAnimations) {
          windowWidth += xPos;
        }

        x1 = windowWidth - 1;
        x2 = windowWidth + width + 1;
      } else {
        windowWidth = 2;

        if (GeneralOptions.enableAnimations) {
          windowWidth -= xPos;
        }
        x1 = windowWidth - 1;
        x2 = width + 1 + windowWidth;
      }
      y1 = y - 1;
      y2 = y + height - 1;

      if (x1 < x2) {
        j = x1;
        x1 = x2;
        x2 = j;
      }

      if (y1 < y2) {
        j = y1;
        y1 = y2;
        y2 = j;
      }

      guiGraphics.fill(x1, y1, x2, y2, GeneralOptions.backgroundColor);
    }
  }

  /**
   * Renders the right side text.
   *
   * @param list        the list of {@link Component}s to draw
   * @param guiGraphics Draw Context
   * @param minecraft   Minecraft Client
   * @param font        the Font Renderer
   * @param additional  Additional text to draw
   */
  public static void drawRightText(final List<Component> list,
                                   final GuiGraphics guiGraphics, final Minecraft minecraft,
                                   final Font font, @Nullable final List<String> additional) {

    if (additional != null) {
      additional.forEach(text -> list.add(Component.nullToEmpty(text)));
    }

    drawBackground(minecraft, font, PositionEnum.RIGHT, list, guiGraphics);

    for (int i = 0; i < list.size(); i++) {

      if (!Strings.isNullOrEmpty(list.get(i).getString())) {
        final int height = 9;
        final int width = font.width(list.get(i).getString());
        int windowWidth = (int) (minecraft.getWindow().getGuiScaledWidth() / GeneralOptions.fontScale) - 2 - width;
        if (GeneralOptions.enableAnimations) {
          windowWidth += xPos;
        }
        final int y = 2 + height * i;

        guiGraphics.drawString(font, list.get(i), windowWidth, y, 0xFFE0E0E0, GeneralOptions.shadowText);
      }
    }

    guiGraphics.pose().popMatrix();
  }

  /**
   * Renders the left side text.
   *
   * @param list        the list of {@link Component}s to draw
   * @param guiGraphics Draw Context
   * @param minecraft   Minecraft Client
   * @param font        the Font Renderer
   * @param additional  Additional text to draw
   */
  public static void drawLeftText(final List<Component> list,
                                  final GuiGraphics guiGraphics, final Minecraft minecraft,
                                  final Font font, @Nullable final List<String> additional) {
    if (additional != null) {
      additional.forEach(text -> list.add(Component.nullToEmpty(text)));
    }

    drawBackground(minecraft, font, PositionEnum.LEFT, list, guiGraphics);

    for (int i = 0; i < list.size(); i++) {

      if (!Strings.isNullOrEmpty(list.get(i).getString())) {

        final int height = 9;
        final int y = 2 + height * i;
        int xPosLeft = 2;

        if (GeneralOptions.enableAnimations) {
          xPosLeft -= xPos;
        }

        guiGraphics.drawString(font, list.get(i), xPosLeft, y, 0xFFE0E0E0, GeneralOptions.shadowText);
      }
    }
  }

  /**
   * Gets a list of {@link Component}s from modules for either the left or right side of the screen.
   *
   * @param minecraft         The Minecraft instance
   * @param left              Whether the modules are on the left or right
   * @param gameInformation   The game information string list
   * @param systemInformation The system information string list
   * @return the right side modules
   */
  public static List<Component> newText(final Minecraft minecraft, final boolean left, final List<String> gameInformation,
                                        final List<String> systemInformation) {

    final List<Component> list = new ArrayList<>();

    if (minecraft.level == null || (minecraft.screen != null && !(minecraft.screen instanceof ModulesScreen || minecraft.screen instanceof ChatScreen))) {
      return list;
    }

    for (final BaseModule module : left ? BaseModule.modules : BaseModule.modulesRight) {
      if (!module.enabled) {
        continue;
      }
      switch (module) {
        case MiscRightModule miscRightModule -> miscRightModule.update(systemInformation);
        case MiscLeftModule miscLeftModule -> miscLeftModule.update(gameInformation);
        default -> module.update(minecraft);
      }

      list.addAll(module.linesFormatted(minecraft.showOnlyReducedInfo()));
      if (GeneralOptions.spaceEveryModule) {
        list.add(Component.nullToEmpty(""));
      }
    }

    return list;
  }
}
