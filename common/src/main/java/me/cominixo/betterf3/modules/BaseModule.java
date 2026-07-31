package me.cominixo.betterf3.modules;

import com.electronwill.nightconfig.core.Config;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import me.cominixo.betterf3.config.GeneralOptions;
import me.cominixo.betterf3.utils.DebugLine;
import me.cominixo.betterf3.utils.DebugLineList;
import me.cominixo.betterf3.utils.PositionEnum;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.Nullable;

/**
 * The Base module.
 */
public abstract class BaseModule implements Comparable<BaseModule> {

    /**
     * The color of the tag.
     */
    public TextColor nameColor = TextColor.fromRgb(0xFFFFFF);

    /**
     * The color of the value of the tag.
     */
    public TextColor valueColor = TextColor.fromRgb(0xFFFFFF);

    /**
     * The default color of the tag.
     */
    public TextColor defaultNameColor = TextColor.fromRgb(0xFFFFFF);
    /**
     * The default color of the value of the tag.
     */
    public TextColor defaultValueColor = TextColor.fromRgb(0xFFFFFF);

    /**
     * Is module enabled.
     */
    public boolean enabled = true;

    /**
     * Dirty flag for formatted line cache.
     */
    private volatile boolean dirty = true;

    /**
     * Cached formatted lines.
     */
    private List<Component> cachedLines = List.of();

    /**
     * Reduced debug mode used for the cached lines.
     */
    private boolean cachedReducedDebug = false;

    /**
     * Last observed state hash for dirty tracking.
     */
    private int cachedStateHash = 0;

    /**
     * Whether a state hash has been recorded.
     */
    private boolean hasCachedStateHash = false;

    /**
     * The module's lines.
     */
    protected final List<DebugLine> lines = new ArrayList<>();

    /**
     * The left modules.
     */
    public static List<BaseModule> modules = new ArrayList<>();

    /**
     * The right modules.
     */
    public static List<BaseModule> modulesRight = new ArrayList<>();

    /**
     * The modules both left and right.
     */
    public static final List<BaseModule> allModules = new ArrayList<>();

    /**
     * Module id.
     */
    public final String id =
            this.getClass().getSimpleName().replace("Module", "").toLowerCase();

    /**
     * Instantiates a new module.
     */
    public BaseModule() {
        // Do nothing
    }

    /**
     * Instantiates a new module.
     *
     * @param invisible sets invisibility
     */
    public BaseModule(final boolean invisible) {
        if (!invisible) {
            allModules.add(this);
        }
    }

    /**
     * Initializes the module.
     *
     * @param positionEnum the position
     */
    public void init(final PositionEnum positionEnum) {
        switch (positionEnum) {
            case RIGHT -> modulesRight.add(this);
            case LEFT -> modules.add(this);
            case BOTH -> {
                modulesRight.add(this);
                modules.add(this);
            }
        }
        allModules.add(this);
    }

    /**
     * Initializes the module on the left.
     */
    public void init() {
        modules.add(this);
        allModules.add(this);
    }

    /**
     * Gets lines.
     *
     * @return the lines
     */
    public List<DebugLine> lines() {
        return this.lines;
    }

    /**
     * Gets formatted lines.
     *
     * @param reducedDebug has reduced debug on
     * @return the lines formatted
     */
    public List<Component> linesFormatted(final boolean reducedDebug) {
        final List<Component> linesString = new ArrayList<>();

        for (final DebugLine line : this.lines) {
            if (reducedDebug && !line.inReducedDebug) {
                continue;
            }
            if (!line.active || !line.enabled) {
                continue;
            }
            if (line instanceof DebugLineList lineList) {
                linesString.addAll(lineList.toTexts(this.nameColor, this.valueColor));
                continue;
            }

            if (!line.isCustom) {
                linesString.add(line.toText(this.nameColor, this.valueColor));
            } else {
                linesString.add(line.toTextCustom(this.nameColor));
            }
        }
        return linesString;
    }

    /**
     * Gets line at an id.
     *
     * @param id the id
     * @return the line
     */
    public @Nullable DebugLine line(final String id) {
        final Optional<DebugLine> lineOptional =
                this.lines.stream().filter(line -> line.id().equals(id)).findFirst();
        return lineOptional.orElse(null);
    }

    /**
     * Gets module by string.
     *
     * @param string the string
     * @return the module
     */
    public static @Nullable BaseModule module(final String string) {
        return BaseModule.allModules.stream()
                .filter(baseModule -> baseModule.toString().equals(string))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets module by the id.
     *
     * @param id the id
     * @return the module
     */
    public static @Nullable BaseModule moduleById(final String id) {
        return BaseModule.allModules.stream()
                .filter(baseModule -> baseModule.id.equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets localized module name.
     *
     * @return localized module name
     */
    public String toString() {
        return I18n.get("text.betterf3.module." + this.id);
    }

    /**
     * Sets enabled.
     *
     * @param enabled enabled
     */
    public void enabled(final boolean enabled) {
        this.enabled = enabled;
        this.markDirty();
    }

    /**
     * Marks this module cache as dirty.
     */
    public final void markDirty() {
        this.dirty = true;
    }

    /**
     * Marks all registered module caches as dirty.
     */
    public static void markAllDirty() {
        final HashSet<BaseModule> loadedModules = new HashSet<>(BaseModule.allModules);
        loadedModules.addAll(BaseModule.modules);
        loadedModules.addAll(BaseModule.modulesRight);
        for (final BaseModule module : loadedModules) {
            module.markDirty();
        }
    }

    /**
     * Returns whether this module needs an update pass.
     *
     * @return true if update should run
     */
    public final boolean needsUpdate() {
        return !GeneralOptions.enablePerformanceOptimizations || this.dirty;
    }

    /**
     * Whether this module should update every rendered frame.
     *
     * @return true when the module should be updated each frame
     */
    public boolean updatesEveryFrame() {
        return true;
    }

    /**
     * Refreshes the dirty flag based on whether module state changed after update.
     */
    public final void refreshDirtyFromState() {
        if (!GeneralOptions.enablePerformanceOptimizations) {
            return;
        }
        final int nextStateHash = this.stateHash();
        if (!this.hasCachedStateHash || this.cachedStateHash != nextStateHash) {
            this.cachedStateHash = nextStateHash;
            this.hasCachedStateHash = true;
            this.dirty = true;
        }
    }

    private int stateHash() {
        int result = Boolean.hashCode(this.enabled);
        result = 31 * result + this.nameColor.getValue();
        result = 31 * result + this.valueColor.getValue();
        for (final DebugLine line : this.lines) {
            result = 31 * result + line.cacheStateHash();
        }
        return result;
    }

    /**
     * Returns formatted lines, using cached output when performance optimization is enabled.
     *
     * @param reducedDebug reduced debug mode state
     * @return formatted lines for rendering
     */
    public final List<Component> cachedLinesFormatted(final boolean reducedDebug) {
        if (!GeneralOptions.enablePerformanceOptimizations) {
            return this.linesFormatted(reducedDebug);
        }
        if (this.dirty || this.cachedReducedDebug != reducedDebug) {
            this.cachedLines = this.linesFormatted(reducedDebug);
            this.cachedReducedDebug = reducedDebug;
            this.dirty = false;
        }
        return this.cachedLines;
    }

    /**
     * Loads module configuration from the persisted config section.
     *
     * @param moduleConfig module config section
     */
    public void loadConfig(final Config moduleConfig) {
        final Config lineConfig = moduleConfig.getOrElse("lines", () -> null);
        if (lineConfig != null) {
            for (final Config.Entry entry : lineConfig.entrySet()) {
                final DebugLine line = this.line(entry.getKey());
                if (line != null) {
                    line.enabled = entry.getValue();
                }
            }
        }

        this.nameColor = readColor(moduleConfig, "name_color", this.defaultNameColor);
        this.valueColor = readColor(moduleConfig, "value_color", this.defaultValueColor);
        this.loadModuleConfig(moduleConfig);
        this.enabled = moduleConfig.getOrElse("enabled", true);
    }

    /**
     * Saves module configuration into the persisted config section.
     *
     * @param moduleConfig module config section
     */
    public void saveConfig(final Config moduleConfig) {
        final Config lineConfig = Config.inMemory();
        for (final DebugLine line : this.lines()) {
            lineConfig.set(line.id(), line.enabled);
        }

        moduleConfig.set("name", this.id);
        writeColor(moduleConfig, "name_color", this.nameColor);
        writeColor(moduleConfig, "value_color", this.valueColor);
        this.saveModuleConfig(moduleConfig);
        moduleConfig.set("enabled", this.enabled);
        moduleConfig.set("lines", lineConfig);
    }

    /**
     * Updates the module.
     *
     * @param client the Minecraft client
     */
    public abstract void update(Minecraft client);

    /**
     * Hook for modules with extra config values.
     *
     * @param moduleConfig module config section
     */
    protected void loadModuleConfig(final Config moduleConfig) {
        // default: no extra fields
    }

    /**
     * Hook for modules with extra config values.
     *
     * @param moduleConfig module config section
     */
    protected void saveModuleConfig(final Config moduleConfig) {
        // default: no extra fields
    }

    /**
     * Creates a non-null text color from a legacy formatting constant.
     *
     * @param formatting formatting constant
     * @return mapped text color
     */
    protected static TextColor legacyColor(final ChatFormatting formatting) {
        return Objects.requireNonNull(TextColor.fromLegacyFormat(formatting));
    }

    /**
     * Reads a color from config, falling back to a default.
     *
     * @param config config section
     * @param key key name
     * @param defaultColor fallback value
     * @return resolved color
     */
    protected static TextColor readColor(final Config config, final String key, final TextColor defaultColor) {
        return TextColor.fromRgb(config.getOrElse(key, defaultColor.getValue()));
    }

    /**
     * Writes a color value to config.
     *
     * @param config config section
     * @param key key name
     * @param color color value
     */
    protected static void writeColor(final Config config, final String key, final TextColor color) {
        config.set(key, color.getValue());
    }

    /**
     * Gets a set of the modules.
     *
     * @return distinct modules
     */
    public static SortedSet<BaseModule> distinctModules() {
        final HashSet<String> distinctModules = new HashSet<>(
                allModules.stream().map(BaseModule::toString).distinct().toList());
        final TreeSet<BaseModule> distinctModulesObjects = new TreeSet<>();
        for (final BaseModule module : allModules) {
            if (distinctModules.contains(module.toString())) {
                distinctModulesObjects.add(module);
                distinctModules.remove(module.toString());
            }
        }
        return distinctModulesObjects;
    }

    @Override
    public int compareTo(final BaseModule o) {
        return this.toString().compareTo(o.toString());
    }
}
