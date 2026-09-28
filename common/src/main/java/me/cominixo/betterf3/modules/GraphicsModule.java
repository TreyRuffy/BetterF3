package me.cominixo.betterf3.modules;

import java.util.List;
import java.util.stream.Collectors;
import me.cominixo.betterf3.utils.DebugLine;
import net.minecraft.ChatFormatting;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
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
        this.defaultNameColor = legacyColor(ChatFormatting.GOLD);
        this.defaultValueColor = legacyColor(ChatFormatting.AQUA);

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

        final String cloudString = client.options.cloudStatus().get() == CloudStatus.OFF
                ? I18n.get("text" + ".betterf3.line.off")
                : (client.options.cloudStatus().get() == CloudStatus.FAST
                        ? I18n.get("text.betterf3.line.fast")
                        : I18n.get("text" + ".betterf3.line.fancy"));

        // Render Distance
        lines.get(0).value(client.levelExtractor.lastViewDistance);
        // Graphics
        lines.get(1)
                .value(StringUtils.capitalize(
                        client.options.graphicsPreset().get().toString()));
        // Clouds
        lines.get(2).value(cloudString);
        // Biome Blend Radius
        lines.get(3).value(client.options.biomeBlendRadius().get());

        // Shader
        final List<Identifier> postEffects = client.gameRenderer.getAppliedPostEffects();
        if (!postEffects.isEmpty()) {
            lines.get(4).value(postEffects.stream().map(Identifier::toString).collect(Collectors.joining(", ")));
        } else {
            lines.get(4).active = false;
        }

        lines.get(0).inReducedDebug = true;
        lines.get(3).inReducedDebug = true;
    }

    @Override
    public boolean updatesEveryFrame() {
        return false;
    }
}
