package me.cominixo.betterf3.utils;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Nullable;

/**
 * A Debug line.
 */
public class DebugLine {

  private Object value;
  private String format;
  private final String id;
  private @Nullable String name = null;

  /**
   * If active.
   */
  public boolean active = true;

  /**
   * If enabled.
   */
  public boolean enabled = true;

  /**
   * If custom.
   */
  public boolean isCustom = false;

  /**
   * If enabled in reduced debug.
   */
  public boolean inReducedDebug = false;

  /**
   * Instantiates a new Debug line.
   *
   * @param id the id
   */
  public DebugLine(final String id) {
    this.id = id;
    this.format = "format.betterf3.default_format";
    this.value = "";
  }

  /**
   * Instantiates a new Debug line.
   *
   * @param id           the id
   * @param formatString the format string
   * @param isCustom     custom
   */
  public DebugLine(final String id, final String formatString, final boolean isCustom) {
    this.id = id;
    this.value = "";
    this.format = formatString;
    this.isCustom = isCustom;
  }

  /**
   * Sets the key and value color.
   *
   * @param nameColor the color of the key
   * @param valueColor the color of the value
   * @return the styled component
   */
  public Component toText(final TextColor nameColor, final TextColor valueColor) {
    final String name = this.name();

    final Component nameStyled = Utils.styledText(name, nameColor);
    final Component valueStyled;

    if (this.value instanceof Component component) {
      valueStyled = component;
    } else {
      valueStyled = Utils.styledText(this.value, valueColor);
    }
    if (this.value.toString().isEmpty()) {
      this.active = false;
    }
    return Component.translatable(this.format, nameStyled, valueStyled);
  }

  /**
   * Sets key color.
   *
   * @param nameColor the key color
   * @return the stylized component
   */
  public Component toTextCustom(final TextColor nameColor) {
    final String name = this.name();

    if (this.value instanceof final List<?> listValue) {
      // format properly if value is a List (bad)
      final List<Object> values = new ArrayList<>();

      if (!name.isEmpty()) {
        values.add(Utils.styledText(name, nameColor));
      }
      values.addAll(listValue);
      return Component.translatable(this.format, values.toArray()).withStyle(style -> style.withColor(nameColor));
    } else {
      return Component.translatable(this.format, name, this.value);
    }
  }

  /**
   * Sets value.
   *
   * @param value the value
   */
  public void value(final Object value) {
    this.active = true;
    this.value = value;
  }

  /**
   * Sets format.
   *
   * @param format the format
   */
  public void format(final String format) {
    this.format = format;
  }

  /**
   * Gets name.
   *
   * @return the name
   */
  public String name() {
    if (this.name != null) {
      return this.name;
    }
    if (this.id.isEmpty()) {
      this.format = "%s%s";
      return "";
    }
    final Language language = Language.getInstance();
    return language.getOrDefault("text.betterf3.line." + this.id);
  }

  /**
   * Sets name.
   *
   * @param name the name
   */
  public void name(final String name) {
    this.name = name;
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  public String id() {
    return this.id;
  }

}
