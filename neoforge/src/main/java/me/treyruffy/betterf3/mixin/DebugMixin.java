package me.treyruffy.betterf3.mixin;

import com.google.common.base.Strings;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import me.cominixo.betterf3.config.GeneralOptions;
import me.cominixo.betterf3.modules.BaseModule;
import me.cominixo.betterf3.modules.MiscLeftModule;
import me.cominixo.betterf3.modules.MiscRightModule;
import me.cominixo.betterf3.utils.PositionEnum;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.cominixo.betterf3.utils.Utils.START_X_POS;
import static me.cominixo.betterf3.utils.Utils.closingAnimation;
import static me.cominixo.betterf3.utils.Utils.lastAnimationUpdate;
import static me.cominixo.betterf3.utils.Utils.xPos;

/**
 * The Debug Screen Overlay.
 */
@Mixin(DebugHud.class)
public abstract class DebugMixin {

  @Shadow @Final private MinecraftClient client;
  @Shadow @Final private TextRenderer textRenderer;

  /**
   * Gets the information on the left side of the screen.
   *
   * @return the game information
   */
  @SuppressWarnings("checkstyle:MethodName")
  @Shadow protected abstract List<String> getLeftText();

  /**
   * Gets the information on the right side of the screen.
   *
   * @return the system information
   */
  @SuppressWarnings("checkstyle:MethodName")
  @Shadow protected abstract List<String> getRightText();

  /**
   * Toggles the debug HUD.
   */
  @Shadow private boolean showDebugHud;

  /**
   * Sets up modules on the left side of the screen.
   *
   * @return the left side modules
   */
  @Unique
  public List<Text> betterF3$newLeftText() {

    final List<Text> list = new ArrayList<>();

    for (final BaseModule module : BaseModule.modules) {
      if (!module.enabled) {
        continue;
      }
      if (module instanceof MiscLeftModule) {
        ((MiscLeftModule) module).update(this.getLeftText());
      } else if (module instanceof MiscRightModule) {
        ((MiscRightModule) module).update(this.getRightText());
      } else {
        module.update(this.client);
      }

      list.addAll(module.linesFormatted(this.client.hasReducedDebugInfo()));
      if (GeneralOptions.spaceEveryModule) {
        list.add(Text.of(""));
      }
    }

    return list;

  }

  /**
   * Sets up modules on the right side of the screen.
   *
   * @return the right side modules
   */
  @Unique
  public List<Text> betterF3$newRightText() {

    final List<Text> list = new ArrayList<>();

    for (final BaseModule module : BaseModule.modulesRight) {
      if (!module.enabled) {
        continue;
      }
      if (module instanceof MiscRightModule) {
        ((MiscRightModule) module).update(this.getRightText());
      } else if (module instanceof MiscLeftModule) {
        ((MiscLeftModule) module).update(this.getLeftText());
      } else {
        module.update(this.client);
      }

      list.addAll(module.linesFormatted(this.client.hasReducedDebugInfo()));
      if (GeneralOptions.spaceEveryModule) {
        list.add(Text.of(""));
      }
    }

    return list;
  }

  /**
   * Renders the text on the right side of the screen.
   *
   * @param context Draw Context
   * @param text The text
   * @param left If the text is on the left
   * @param ci Callback info
   */
  @Inject(method = "drawText", at = @At("HEAD"), cancellable = true)
  public void drawRightText(final DrawContext context, final List<String> text, final boolean left, final CallbackInfo ci) {

    if (GeneralOptions.disableMod) {
      return;
    }
    if (left) {
      return;
    }

    final List<Text> list = this.betterF3$newRightText();

    final VertexConsumerProvider.Immediate immediate = this.betterF3$immediate(PositionEnum.RIGHT, list, context.getMatrices());

    for (int i = 0; i < list.size(); i++) {

      if (!Strings.isNullOrEmpty(list.get(i).getString())) {
        final int height = 9;
        final int width = this.textRenderer.getWidth(list.get(i).getString());
        int windowWidth =
        (int) (this.client.getWindow().getScaledWidth() / GeneralOptions.fontScale) - 2 - width;
        if (GeneralOptions.enableAnimations) {
          windowWidth += xPos;
        }
        final int y = 2 + height * i;

        this.textRenderer.draw(list.get(i), windowWidth, y, 0xE0E0E0, GeneralOptions.shadowText, context.getMatrices().peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
      }
    }
    immediate.draw();

    ci.cancel();
  }

  /**
   * Lets us draw in batches.
   *
   * @param pos The position
   * @param list The list of Text
   * @param matrixStack The MatrixStack
   * @return VertexConsumerProvider
   */
  @Unique
  public VertexConsumerProvider.Immediate betterF3$immediate(final PositionEnum pos, final List<Text> list,
                                                             final MatrixStack matrixStack) {

    final float f = (float) (GeneralOptions.backgroundColor >> 24 & 255) / 255.0F;
    final float g = (float) (GeneralOptions.backgroundColor >> 16 & 255) / 255.0F;
    final float h = (float) (GeneralOptions.backgroundColor >> 8 & 255) / 255.0F;
    final float k = (float) (GeneralOptions.backgroundColor & 255) / 255.0F;
    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
    final BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();
    bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

    for (int i = 0; i < list.size(); i++) {
      final int height = 9;
      final int width = this.textRenderer.getWidth(list.get(i).getString());
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
        windowWidth = (int) (this.client.getWindow().getScaledWidth() / GeneralOptions.fontScale) - 2 - width;
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

      final Matrix4f matrix = matrixStack.peek().getPositionMatrix();

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

      bufferBuilder.vertex(matrix, (float) x1, (float) y2, 0.0F).color(g, h, k, f).next();
      bufferBuilder.vertex(matrix, (float) x2, (float) y2, 0.0F).color(g, h, k, f).next();
      bufferBuilder.vertex(matrix, (float) x2, (float) y1, 0.0F).color(g, h, k, f).next();
      bufferBuilder.vertex(matrix, (float) x1, (float) y1, 0.0F).color(g, h, k, f).next();

    }
    BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    RenderSystem.disableBlend();

    return VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());

  }

  /**
   * Renders the text on the left side of the screen.
   *
   * @param context Draw Context
   * @param text The text
   * @param left If the text is on the left
   * @param ci Callback info
   */
  @Inject(method = "drawText", at = @At("HEAD"), cancellable = true)
  public void drawLeftText(final DrawContext context, final List<String> text, final boolean left, final CallbackInfo ci) {

    if (GeneralOptions.disableMod) {
      return;
    }
    if (!left) {
      return;
    }

    final List<Text> list = this.betterF3$newLeftText();
    final VertexConsumerProvider.Immediate immediate = this.betterF3$immediate(PositionEnum.LEFT, list, context.getMatrices());

    for (int i = 0; i < list.size(); i++) {

      if (!Strings.isNullOrEmpty(list.get(i).getString())) {

        final int height = 9;
        final int y = 2 + height * i;
        int xPosLeft = 2;

        if (GeneralOptions.enableAnimations) {
          xPosLeft -= xPos;
        }

        this.textRenderer.draw(list.get(i), xPosLeft, y, 0xE0E0E0, GeneralOptions.shadowText, context.getMatrices().peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
      }
    }
    immediate.draw();

    ci.cancel();
  }

  /**
   * Disables the unneeded math for allocation rate.
   *
   * @param instance the allocation rate calculator
   * @param allocatedBytes the allocated bytes
   * @return nothing
   */
  @Redirect(method = "getRightText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud$AllocationRateCalculator;get(J)J"))
  public long allocationRateCalculatorGet(final DebugHud.AllocationRateCalculator instance, final long allocatedBytes) {
    return 0;
  }

  /**
   * Ensures that the TPS graph works.
   *
   * @param context Draw Context
   * @param ci Callback info
   */
  @Inject(method = "render", at = @At(value = "HEAD"))
  public void renderBefore(final DrawContext context, final CallbackInfo ci) {
    if (GeneralOptions.disableMod) {
      return;
    }
    context.getMatrices().push();
  }

  /**
   * Modifies the font scale.
   *
   * @param context Draw Context
   * @param ci Callback info
   */
  @Inject(method = "method_51746", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;drawGameInformation(Lnet/minecraft/client/gui/DrawContext;Ljava/util/List;)V"))
  public void renderFontScaleBefore(final DrawContext context, final CallbackInfo ci) {
    if (!GeneralOptions.disableMod) {
      context.getMatrices().scale((float) GeneralOptions.fontScale, (float) GeneralOptions.fontScale, 1F);
    }
  }

  /**
   * Fixes the font scale.
   *
   * @param context Draw Context
   * @param ci Callback info
   */
  @Inject(method = "method_51746", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;drawSystemInformation(Lnet/minecraft/client/gui/DrawContext;Ljava/util/List;)V", shift = At.Shift.AFTER))
  public void renderFontScaleAfter(final DrawContext context, final CallbackInfo ci) {
    if (!GeneralOptions.disableMod) {
      context.getMatrices().pop();
    }
  }

  /**
   * Renders the animation.
   *
   * @param context Draw Context
   * @param ci Callback info
   */
  @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V"))
  public void renderAnimation(final DrawContext context, final CallbackInfo ci) {

    if (GeneralOptions.disableMod) {
      return;
    }
    if (!GeneralOptions.enableAnimations) {
      return;
    } // Only displays the animation if set to true

    final long time = Util.getMeasuringTimeMs();
    if (time - lastAnimationUpdate >= 10 && (xPos != 0 || closingAnimation)) {

      int i = ((START_X_POS / 2 + xPos) / 10) - 9;

      if (xPos != 0 && !closingAnimation) {
        xPos = (int) (xPos / GeneralOptions.animationSpeed);
        xPos -= i;
      }

      if (i == 0) {
        i = 1;
      }

      if (closingAnimation) {

        xPos += i;
        xPos = (int) (xPos * GeneralOptions.animationSpeed);

        if (xPos >= 300) {
          this.showDebugHud = false;
          closingAnimation = false;
        }

      }

      lastAnimationUpdate = time;
    }
  }

}
