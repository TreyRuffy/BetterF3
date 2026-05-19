package me.cominixo.betterf3.config.gui.modules;

import java.util.Objects;
import me.cominixo.betterf3.config.ModConfigFile;
import me.cominixo.betterf3.modules.BaseModule;
import me.cominixo.betterf3.utils.PositionEnum;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * The Modules screen.
 */
@SuppressWarnings("NullAway.Init")
public final class ModulesScreen extends Screen {

    /**
     * The parent screen.
     */
    final Screen parent;
    /**
     * The Modules list widget.
     */
    @Nullable
    ModuleListWidget modulesListWidget;

    private boolean initialized = false;

    private @Nullable Button editButton;
    private @Nullable Button deleteButton;

    /**
     * The side of the screen (left or right).
     */
    public final PositionEnum side;

    /**
     * Instantiates a new Modules screen.
     *
     * @param parent the parent screen
     * @param side   the side of the screen
     */
    public ModulesScreen(final Screen parent, final PositionEnum side) {
        super(Component.translatable("config.betterf3.title.modules"));
        this.parent = parent;
        this.side = side;
    }

    @Override
    protected void init() {
        super.init();

        if (this.initialized) {
            final ModuleListWidget listWidget = Objects.requireNonNull(this.modulesListWidget);
            listWidget.setRectangle(this.width, this.height - 64 - 32, 0, 32);
            listWidget.updateModules();
        } else {
            this.initialized = true;
            this.modulesListWidget = new ModuleListWidget(
                    this, Objects.requireNonNull(this.minecraft), this.width, this.height - 64 - 32, 32, 36);
            if (this.side == PositionEnum.LEFT) {
                Objects.requireNonNull(this.modulesListWidget).modules(BaseModule.modules);
            } else if (this.side == PositionEnum.RIGHT) {
                Objects.requireNonNull(this.modulesListWidget).modules(BaseModule.modulesRight);
            }
        }

        Objects.requireNonNull(this.modulesListWidget);
        this.addRenderableWidget(this.modulesListWidget);

        final Button editButtonWidget = Button.builder(
                        Component.translatable("config.betterf3.modules.edit_button"), _ -> {
                            Objects.requireNonNull(this.modulesListWidget);
                            final Screen screen = EditModulesScreen.configBuilder(
                                            Objects.requireNonNull(this.modulesListWidget.getSelected()).module, this)
                                    .build();
                            minecraft.setScreen(screen);
                        })
                .bounds(this.width / 2 - 50, this.height - 50, 100, 20)
                .build();
        this.editButton = this.addRenderableWidget(editButtonWidget);

        final Button addButton = Button.builder(
                        Component.translatable("config.betterf3.modules.add_button"),
                        _ -> minecraft.setScreen(
                                AddModuleScreen.configBuilder(this).build()))
                .bounds(this.width / 2 + 4 + 50, this.height - 50, 100, 20)
                .build();
        this.addRenderableWidget(addButton);

        final Button deleteButtonWidget = Button.builder(
                        Component.translatable("config.betterf3.modules.delete_button"), _ -> {
                            Objects.requireNonNull(this.modulesListWidget);

                            this.modulesListWidget.removeModule(this.modulesListWidget.moduleEntries.indexOf(
                                    Objects.requireNonNull(this.modulesListWidget.getSelected())));
                        })
                .bounds(this.width / 2 - 154, this.height - 50, 100, 20)
                .build();
        this.deleteButton = this.addRenderableWidget(deleteButtonWidget);

        final Button doneButton = Button.builder(Component.translatable("config.betterf3.modules.done_button"), _ -> this.onClose())
                .bounds(this.width / 2 - 154, this.height - 30 + 4, 308, 20)
                .build();
        this.addRenderableWidget(doneButton);

        this.updateButtons();
    }

    @Override
    public void extractRenderState(
            final GuiGraphicsExtractor context, final int mouseX, final int mouseY, final float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.centeredText(this.font, this.title, this.width / 2, 20, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        final ModuleListWidget listWidget = Objects.requireNonNull(this.modulesListWidget);
        if (this.side == PositionEnum.LEFT) {
            BaseModule.modules.clear();
            for (final ModuleListWidget.ModuleEntry entry : listWidget.moduleEntries) {
                BaseModule.modules.add(entry.module);
            }
        } else if (this.side == PositionEnum.RIGHT) {
            BaseModule.modulesRight.clear();
            for (final ModuleListWidget.ModuleEntry entry : listWidget.moduleEntries) {
                BaseModule.modulesRight.add(entry.module);
            }
        }
        this.minecraft.setScreen(this.parent);
        ModConfigFile.saveRunnable.run();
    }

    /**
     * Selects a module.
     *
     * @param entry the entry
     */
    public void select(final ModuleListWidget.ModuleEntry entry) {
        Objects.requireNonNull(this.modulesListWidget).setSelected(entry);
        this.updateButtons();
    }

    /**
     * Updates the buttons.
     */
    public void updateButtons() {
        Objects.requireNonNull(this.modulesListWidget);
        Objects.requireNonNull(this.editButton);
        Objects.requireNonNull(this.deleteButton);
        if (this.modulesListWidget.getSelected() != null) {
            this.editButton.active = true;
            this.deleteButton.active = true;
        } else {
            this.editButton.active = false;
            this.deleteButton.active = false;
        }
    }
}
