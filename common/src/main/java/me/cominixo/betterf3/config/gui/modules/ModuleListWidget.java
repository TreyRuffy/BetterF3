package me.cominixo.betterf3.config.gui.modules;

import java.util.ArrayList;
import java.util.List;
import me.cominixo.betterf3.modules.BaseModule;
import me.cominixo.betterf3.modules.CoordsModule;
import me.cominixo.betterf3.modules.FpsModule;
import me.cominixo.betterf3.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * The Module list widget.
 */
public class ModuleListWidget extends ObjectSelectionList<ModuleListWidget.ModuleEntry> {

  /**
   * The modules screen.
   */
  final ModulesScreen modulesScreen;
  /**
   * The Module entries.
   */
  final List<ModuleEntry> moduleEntries = new ArrayList<>();

  /**
   * Instantiates a new Module list widget.
   *
   * @param modulesScreen the module screen
   * @param client        the Minecraft client
   * @param width         the width
   * @param height        the height
   * @param y             the y
   * @param itemHeight    the itemHeight
   */
  public ModuleListWidget(final ModulesScreen modulesScreen, final Minecraft client, final int width,
                          final int height, final int y, final int itemHeight) {
    super(client, width, height, y, itemHeight);
    this.modulesScreen = modulesScreen;
  }

  /**
   * Gets scrollbar position x.
   *
   * @return the scrollbar position x
   */
  protected int scrollbarPositionX() {
    return super.scrollBarX() + 30;
  }

  /**
   * Gets the row width.
   *
   * @return the row width
   */
  public int rowWidth() {
    return super.getRowWidth() + 85;
  }

  /**
   * The entry.
   *
   * @param index the index
   *
   * @return ModuleEntry
   */
  public ModuleEntry entry(final int index) {
    return this.moduleEntries.get(index);
  }

  /**
   * Sets modules.
   *
   * @param modules the modules
   */
  public void modules(final List<BaseModule> modules) {
    this.moduleEntries.clear();
    this.clearEntries();

    for (final BaseModule module : modules) {
      this.addModule(module);
    }
  }

  /**
   * Updates the modules.
   */
  public void updateModules() {
    this.clearEntries();
    this.moduleEntries.forEach(this::addEntry);

  }

  /**
   * Add a module.
   *
   * @param module the module
   */
  public void addModule(final BaseModule module) {
    final ModuleEntry entry = new ModuleEntry(this.modulesScreen, module);
    this.moduleEntries.add(entry);
    this.addEntry(entry);
  }

  /**
   * Remove a module.
   *
   * @param index the index of the module
   */
  public void removeModule(final int index) {
    final ModuleEntry entry = this.moduleEntries.get(index);
    this.moduleEntries.remove(entry);
    this.removeEntry(entry);
    this.modulesScreen.updateButtons();
    if (this.scrollAmount() > this.maxScrollAmount()) {
      this.setScrollAmount(this.maxScrollAmount());
    }
    //BaseModule.modules.remove(index);
  }

  /**
   * A module entry.
   */
  public class ModuleEntry extends Entry<ModuleEntry> {
    private final ModulesScreen modulesScreen;
    private final Minecraft client;
    /**
     * The Module.
     */
    public final BaseModule module;

    /**
     * Instantiates a new Module entry.
     *
     * @param modulesScreen the module screen
     * @param module the module
     */
    protected ModuleEntry(final ModulesScreen modulesScreen, final BaseModule module) {
      this.modulesScreen = modulesScreen;
      this.module = module;
      this.client = Minecraft.getInstance();
    }

    // Fixes 1.17 crash
    @Override
    public @NotNull Component getNarration() {
      return Component.nullToEmpty(this.module.toString());
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent mouseButtonEvent, final boolean a) {
      final double mouseX = mouseButtonEvent.x();
      final double mouseY = mouseButtonEvent.y();
      final double d = mouseX - this.modulesScreen.modulesListWidget.getRowLeft();
      final double e =
      mouseY - ModuleListWidget.this.getRowTop(ModuleListWidget.this.children().indexOf(this));
      if (d <= 32.0D) {
        final int i = this.modulesScreen.modulesListWidget.children().indexOf(this);
        if (d < 16.0D && e < 16.0D && i > 0) {
          this.swapEntries(i, i - 1);
          return true;
        }

        if (d < 16.0D && e > 16.0D && i < ModuleListWidget.this.moduleEntries.size() - 1) {
          this.swapEntries(i, i + 1);
          return true;
        }
      }

      this.modulesScreen.select(this);
      return false;
    }

    private void swapEntries(final int i, final int j) {

      final ModuleEntry temp = ModuleListWidget.this.moduleEntries.get(i);

      ModuleListWidget.this.moduleEntries.set(i, ModuleListWidget.this.moduleEntries.get(j));
      ModuleListWidget.this.moduleEntries.set(j, temp);

      this.modulesScreen.modulesListWidget.setSelected(temp);
      this.modulesScreen.updateButtons();
      this.modulesScreen.modulesListWidget.updateModules();

    }

    @Override
    public void renderContent(final @NotNull GuiGraphics context, final int mouseX, final int mouseY, final boolean hovered, final float tickDelta) {
      final int x = this.getContentX();
      final int y = this.getContentY();
      context.drawString(this.client.font, this.module.toString(), x + 35, y + 1, 0xffffffff, true);

      final Component exampleText;

      if (this.module instanceof CoordsModule coordsModule) {
        exampleText = Utils.styledText("X", coordsModule.colorX).append(Utils.styledText("Y", coordsModule.colorY)).append(Utils.styledText("Z", coordsModule.colorZ)).append(Utils.styledText(": ", coordsModule.nameColor))
        .append(Utils.styledText("100 ", coordsModule.colorX).append(Utils.styledText("200 ", coordsModule.colorY)).append(Utils.styledText("300", coordsModule.colorZ)));

      } else if (this.module instanceof FpsModule fpsModule) {
        exampleText = Utils.styledText("60 fps  ", fpsModule.colorHigh).append(Utils.styledText("40 fps  ", fpsModule.colorMed)).append(Utils.styledText("10 fps", fpsModule.colorLow));
      } else if (this.module.nameColor != null && this.module.valueColor != null) {
        exampleText = Utils.styledText("Name: ", this.module.nameColor).append(Utils.styledText("Value", this.module.valueColor));
      } else {
        exampleText = Component.nullToEmpty("");
      }

      context.drawString(this.client.font, exampleText, x + 43, y + 13, 0xffffffff, true);

      if (this.client.options.touchscreen().get() || hovered) {
        context.fill(x, y, x + 32, y + 32, -1601138544);
        final int v = mouseX - x;
        final int w = mouseY - y;
        final int index = ModuleListWidget.this.children().indexOf(this);

        if (index > 0) {
          if (v < 16 && w < 16) {
            context.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.parse("server_list/move_up_highlighted"), x, y, 32, 32);
          } else {
            context.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.parse("server_list/move_up"), x, y, 32, 32);
          }
        }

        if (index < ModuleListWidget.this.moduleEntries.size() - 1) {
          if (v < 16 && w > 16) {
            context.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.parse("server_list/move_down_highlighted"), x, y, 32, 32);
          } else {
            context.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.parse("server_list/move_down"), x, y, 32, 32);
          }
        }
      }
    }
  }
}
