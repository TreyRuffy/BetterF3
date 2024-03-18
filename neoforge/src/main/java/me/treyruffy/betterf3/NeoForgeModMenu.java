package me.treyruffy.betterf3;

import me.cominixo.betterf3.config.gui.ModConfigScreen;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.ConfigScreenHandler;

/**
 * Used to create mod configuration in the NeoForge mod menu.
 */
public final class NeoForgeModMenu {

  private NeoForgeModMenu() {
    // Do nothing
  }

  /**
   * Registers BetterF3 in the mod menu.
   */
  public static void registerModsPage() {
    ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
    () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> new ModConfigScreen(parent)));
  }
}
