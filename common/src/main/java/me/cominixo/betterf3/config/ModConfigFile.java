package me.cominixo.betterf3.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import me.cominixo.betterf3.modules.BaseModule;
import me.cominixo.betterf3.modules.EmptyModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Mod config file.
 */
public final class ModConfigFile {

    private ModConfigFile() {
        // Do nothing
    }

    private static final Logger LOGGER = LogManager.getLogger(ModConfigFile.class);

    private static FileType storedFileType = FileType.JSON;

    /**
     * Saves the config.
     */
    public static final Runnable saveRunnable = () -> {
        final Path path = Paths.get(storedFileType == FileType.JSON ? "config/betterf3.json" : "config/betterf3.toml");

        final File file = path.toFile();
        final File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (final FileConfig config =
                FileConfig.builder(path).concurrent().autosave().build()) {
            final Config general = Config.inMemory();
            general.set("disable_mod", GeneralOptions.disableMod);
            general.set("auto_start", GeneralOptions.autoF3);
            general.set("space_modules", GeneralOptions.spaceEveryModule);
            general.set("shadow_text", GeneralOptions.shadowText);
            general.set("animations", GeneralOptions.enableAnimations);
            general.set("animationSpeed", GeneralOptions.animationSpeed);
            general.set("fontScale", GeneralOptions.fontScale);
            general.set("background_color", GeneralOptions.backgroundColor);
            general.set("hide_debug_crosshair", GeneralOptions.hideDebugCrosshair);
            general.set("hide_sidebar", GeneralOptions.hideSidebar);
            general.set("hide_bossbar", GeneralOptions.hideBossbar);
            general.set("always_show_profiler", GeneralOptions.alwaysEnableProfiler);
            general.set("always_show_tps", GeneralOptions.alwaysEnableTPS);
            general.set("always_show_ping", GeneralOptions.alwaysEnablePing);
            general.set("performance_optimizations", GeneralOptions.enablePerformanceOptimizations);

            final List<Config> configsLeft = new ArrayList<>();
            for (final BaseModule module : BaseModule.modules) {
                configsLeft.add(saveModule(module));
            }

            final List<Config> configsRight = new ArrayList<>();
            for (final BaseModule module : BaseModule.modulesRight) {
                configsRight.add(saveModule(module));
            }

            config.set("modules_left", configsLeft);
            config.set("modules_right", configsRight);
            config.set("general", general);
        } catch (final RuntimeException ex) {
            LOGGER.warn("Failed to save BetterF3 config to {}: {}", path, ex.getMessage(), ex);
        }
        BaseModule.markAllDirty();
    };

    /**
     * Loads the config.
     *
     * @param filetype the filetype (JSON or TOML)
     */
    public static void load(final FileType filetype) {

        storedFileType = filetype;

        final Path path = Paths.get(storedFileType == FileType.JSON ? "config/betterf3.json" : "config/betterf3.toml");
        final File file = path.toFile();

        if (!file.exists()) {
            return;
        }

        try (final FileConfig config =
                FileConfig.builder(file).concurrent().autosave().build()) {
            config.load();

            final Config allModulesConfig = config.getOrElse("modules", () -> null);

            // Support for old configs
            if (allModulesConfig != null) {
                for (final BaseModule module : BaseModule.allModules) {
                    final Config moduleConfig = allModulesConfig.getOrElse(module.id, () -> null);
                    if (moduleConfig != null) {
                        module.loadConfig(moduleConfig);
                    }
                }
            } else {
                final List<BaseModule> modulesLeft = new ArrayList<>();
                final List<BaseModule> modulesRight = new ArrayList<>();

                final List<Config> modulesLeftConfig = config.getOrElse("modules_left", () -> null);
                if (modulesLeftConfig != null) {
                    for (final Config moduleConfig : modulesLeftConfig) {
                        final String moduleName = moduleConfig.getOrElse("name", null);
                        if (moduleName != null) {
                            modulesLeft.add(ModConfigFile.loadModule(moduleConfig));
                        }
                    }
                }

                final List<Config> modulesRightConfig = config.getOrElse("modules_right", () -> null);
                if (modulesRightConfig != null) {
                    for (final Config moduleConfig : modulesRightConfig) {
                        final String moduleName = moduleConfig.getOrElse("name", () -> null);
                        if (moduleName != null) {
                            modulesRight.add(ModConfigFile.loadModule(moduleConfig));
                        }
                    }
                }

                if (!modulesLeft.isEmpty() || !modulesRight.isEmpty()) {
                    BaseModule.modules = modulesLeft;
                    BaseModule.modulesRight = modulesRight;
                }
            }

            final Config general = config.getOrElse("general", () -> null);
            if (general != null) {
                if (allModulesConfig != null) {
                    final List<BaseModule> modulesLeft = new ArrayList<>();
                    final List<BaseModule> modulesRight = new ArrayList<>();

                    for (final Object s : general.getOrElse("modules_left_order", new ArrayList<>())) {
                        final BaseModule baseModule = BaseModule.moduleById(s.toString());
                        if (baseModule != null) {
                            modulesLeft.add(baseModule);
                        }
                    }
                    if (!modulesLeft.isEmpty()) {
                        BaseModule.modules = modulesLeft;
                    }

                    for (final Object s : general.getOrElse("modules_right_order", new ArrayList<>())) {
                        final BaseModule baseModule = BaseModule.moduleById(s.toString());
                        if (baseModule != null) {
                            modulesRight.add(baseModule);
                        }
                    }
                    if (!modulesRight.isEmpty()) {
                        BaseModule.modulesRight = modulesRight;
                    }
                }

                GeneralOptions.disableMod = general.getOrElse("disable_mod", false);
                GeneralOptions.autoF3 = general.getOrElse("auto_start", false);
                GeneralOptions.spaceEveryModule = general.getOrElse("space_modules", false);
                GeneralOptions.shadowText = general.getOrElse("shadow_text", true);
                GeneralOptions.enableAnimations = general.getOrElse("animations", true);
                GeneralOptions.animationSpeed = general.getOrElse("animationSpeed", 1.0);
                GeneralOptions.fontScale = general.getOrElse("fontScale", 1.0);
                GeneralOptions.backgroundColor = general.getOrElse("background_color", 0x6F505050);
                GeneralOptions.hideDebugCrosshair = general.getOrElse("hide_debug_crosshair", false);
                GeneralOptions.hideSidebar = general.getOrElse("hide_sidebar", true);
                GeneralOptions.hideBossbar = general.getOrElse("hide_bossbar", true);
                GeneralOptions.alwaysEnableProfiler = general.getOrElse("always_show_profiler", false);
                GeneralOptions.alwaysEnableTPS = general.getOrElse("always_show_tps", false);
                GeneralOptions.alwaysEnablePing = general.getOrElse("always_show_ping", false);
                GeneralOptions.enablePerformanceOptimizations = general.getOrElse("performance_optimizations", true);
            }
        } catch (final RuntimeException ex) {
            LOGGER.warn("Failed to load BetterF3 config from {}: {}", path, ex.getMessage(), ex);
        }
        BaseModule.markAllDirty();
    }

    private static BaseModule loadModule(final Config moduleConfig) {
        final String moduleName = moduleConfig.getOrElse("name", null);

        BaseModule baseModule;
        final BaseModule moduleTemplate = BaseModule.moduleById(moduleName);
        try {
            if (moduleTemplate != null) {
                baseModule = moduleTemplate.getClass().getDeclaredConstructor().newInstance();
            } else {
                baseModule = new EmptyModule(false);
            }
        } catch (InstantiationException
                | IllegalAccessException
                | NoSuchMethodException
                | InvocationTargetException _) {
            baseModule = new EmptyModule(false);
        }
        baseModule.loadConfig(moduleConfig);
        return baseModule;
    }

    private static Config saveModule(final BaseModule module) {
        final Config moduleConfig = Config.inMemory();
        module.saveConfig(moduleConfig);
        return moduleConfig;
    }

    /**
     * The enum File type.
     */
    public enum FileType {
        /**
         * Json file type.
         */
        JSON,
        /**
         * Toml file type.
         */
        TOML
    }
}
