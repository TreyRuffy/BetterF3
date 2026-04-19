package me.cominixo.betterf3.modules;

import com.electronwill.nightconfig.core.Config;
import me.cominixo.betterf3.utils.DebugLine;
import net.minecraft.client.Minecraft;

/**
 * An Empty module.
 */
public class EmptyModule extends BaseModule {

    /**
     * The number of empty lines to display.
     */
    public int emptyLines;

    /**
     * The number of lines displayed to the client.
     */
    public int displayedLines;

    /**
     * The default number of empty lines to display.
     */
    public final int defaultEmptyLines = 1;

    /**
     * Instantiates a new Empty module.
     *
     * @param invisible sets invisibility
     */
    public EmptyModule(final boolean invisible) {
        super(invisible);

        this.emptyLines = this.defaultEmptyLines;
        this.displayedLines = this.emptyLines;

        // Allow for multiple empty lines
        for (int i = 0; i < 20; i++) {
            lines.add(new DebugLine("nothing", "", false));

            // Set all empty lines to show in reduced debug
            lines.get(i).inReducedDebug = true;
        }
    }

    @Override
    public void update(final Minecraft client) {
        final int clampedEmptyLines = Math.min(this.emptyLines, 20);
        final int loopLines = clampedEmptyLines != this.displayedLines ? 20 : this.displayedLines;
        for (int i = 0; i < loopLines; i++) {
            lines.get(i).value("");
            lines.get(i).active = i < clampedEmptyLines;
        }
        if (loopLines == 20) {
            this.emptyLines = clampedEmptyLines;
            this.displayedLines = clampedEmptyLines;
        }
    }

    @Override
    public boolean updatesEveryFrame() {
        return false;
    }

    @Override
    protected void loadModuleConfig(final Config moduleConfig) {
        this.emptyLines = moduleConfig.getOrElse("empty_lines", this.defaultEmptyLines);
    }

    @Override
    protected void saveModuleConfig(final Config moduleConfig) {
        moduleConfig.set("empty_lines", this.emptyLines);
    }
}
