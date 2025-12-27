package me.cominixo.betterf3.config.gui;

import me.cominixo.betterf3.config.gui.modules.ModulesScreen;
import me.cominixo.betterf3.utils.PositionEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The Mod Config screen.
 */
public class ModConfigScreen extends Screen {
  private final Screen parent;

  /**
   * Instantiates a new Mod Config screen.
   *
   * @param parent the parent screen
   */
  public ModConfigScreen(final Screen parent) {
    super(Component.translatable("config.betterf3.title.config"));
    this.parent = parent;
  }

  @Override
  public void init() {
    final Minecraft client = Minecraft.getInstance();

    final Button leftButton = Button.builder(Component.translatable("config.betterf3.order_left_button"),
        button -> client.setScreen(new ModulesScreen(client.screen, PositionEnum.LEFT)))
      .bounds(this.width / 2 - 130, this.height / 4, 120, 20).build();
    this.addRenderableWidget(leftButton);

    final Button rightButton = Button.builder(Component.translatable("config.betterf3.order_right_button"),
        button -> client.setScreen(new ModulesScreen(client.screen, PositionEnum.RIGHT)))
      .bounds(this.width / 2 + 10, this.height / 4, 120, 20).build();
    this.addRenderableWidget(rightButton);

    final Button configButton = Button.builder(Component.translatable("config.betterf3.general_settings"),
        button -> client.setScreen(GeneralOptionsScreen.configBuilder(client).build()))
      .bounds(this.width / 2 - 130, this.height / 4 - 24, 260, 20).build();
    this.addRenderableWidget(configButton);

    final Button doneButton = Button.builder(Component.translatable("config.betterf3.modules.done_button"),
        button -> client.setScreen(this.parent))
      .bounds(this.width / 2 - 130, this.height - 50, 260, 20).build();
    this.addRenderableWidget(doneButton);

    if (minecraft != null && minecraft.level != null && !minecraft.getDebugOverlay().showDebugScreen()) {
      minecraft.debugEntries.toggleDebugOverlay();
    }
  }

  @Override
  public void render(final GuiGraphics context, final int mouseX, final int mouseY, final float delta) {
    super.render(context, mouseX, mouseY, delta);
    context.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
  }
}
