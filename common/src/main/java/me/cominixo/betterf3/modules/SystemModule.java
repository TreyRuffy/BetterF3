package me.cominixo.betterf3.modules;

import com.electronwill.nightconfig.core.Config;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.device.GpuDevice;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import me.cominixo.betterf3.utils.DebugLine;
import me.cominixo.betterf3.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugEntrySystemSpecs;

/**
 * The System module.
 */
public class SystemModule extends BaseModule {

    /**
     * Default enable memory usage color.
     */
    public final boolean defaultMemoryColorToggle = true;

    /**
     * Enable memory usage color.
     */
    public Boolean memoryColorToggle;

    /**
     * Default time format.
     */
    public final String defaultTimeFormat = "HH:mm:ss";

    /**
     * Time format.
     */
    public String timeFormat;

    /**
     * Instantiates a new System module.
     */
    public SystemModule() {
        this.defaultNameColor = legacyColor(ChatFormatting.GOLD);
        this.defaultValueColor = legacyColor(ChatFormatting.AQUA);

        this.nameColor = defaultNameColor;
        this.valueColor = defaultValueColor;
        this.memoryColorToggle = this.defaultMemoryColorToggle;
        this.timeFormat = this.defaultTimeFormat;

        lines.add(new DebugLine("time"));
        lines.add(new DebugLine("java_version"));
        lines.add(new DebugLine("memory_usage"));
        lines.add(new DebugLine("allocation_rate"));
        lines.add(new DebugLine("allocated_memory"));
        lines.add(new DebugLine("cpu"));
        lines.add(new DebugLine("display"));
        lines.add(new DebugLine("gpu"));
        lines.add(new DebugLine("gpu_utilization"));
        lines.add(new DebugLine("graphics_api"));
        lines.add(new DebugLine("gpu_driver"));

        for (final DebugLine line : lines) {
            line.inReducedDebug = true;
        }
    }

    /**
     * Updates the System module.
     *
     * @param client the Minecraft client
     */
    public void update(final Minecraft client) {
        final LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter timeFormatter;
        try {
            timeFormatter = DateTimeFormatter.ofPattern(this.timeFormat);
        } catch (final IllegalArgumentException e) {
            this.timeFormat = this.defaultTimeFormat;
            timeFormatter = DateTimeFormatter.ofPattern(this.timeFormat);
        }

        final String time = currentTime.format(timeFormatter);

        final long maxMemory = Runtime.getRuntime().maxMemory();
        final long totalMemory = Runtime.getRuntime().totalMemory();
        final long freeMemory = Runtime.getRuntime().freeMemory();
        final long usedMemory = totalMemory - freeMemory;

        final Window window = client.getWindow();
        final GpuDevice gpuDevice = RenderSystem.getDevice();

        final String javaVersion = String.format("%s", System.getProperty("java.version"));
        final String memoryUsage = String.format(
                "% 2d%% %03d/%03d MB", usedMemory * 100 / maxMemory, usedMemory / 1024 / 1024, maxMemory / 1024 / 1024);
        final String allocationRateStr = String.format("% 2d MB/s", this.allocationRate(usedMemory) / 1024 / 1024);
        final String allocatedMemory =
                String.format("% 2d%% %03dMB", totalMemory * 100 / maxMemory, totalMemory / 1024 / 1024);
        final String displayInfo = String.format(
                "%d x %d (%s)",
                window.getWidth(), window.getHeight(), gpuDevice.getDeviceInfo().vendorName());

        final String graphicsApi = gpuDevice.getDeviceInfo().backendName();
        final String gpuDriverVersion = gpuDevice.getDeviceInfo().driverInfo();
        final String gpuUtilization = gpuUtilization();

        lines.get(0).value(time);
        lines.get(1).value(javaVersion);
        lines.get(2)
                .value(
                        this.memoryColorToggle
                                ? Utils.percentColor((int) (usedMemory * 100 / maxMemory)) + memoryUsage
                                : memoryUsage);
        lines.get(3).value(allocationRateStr);
        lines.get(4).value(allocatedMemory);
        lines.get(5).value(DebugEntrySystemSpecs.getCpuInfo());
        lines.get(6).value(displayInfo);
        lines.get(7).value(gpuDevice.getDeviceInfo().name());
        lines.get(8).value(gpuUtilization);
        lines.get(9).value(graphicsApi);
        lines.get(10).value(gpuDriverVersion);
    }

    private static final List<GarbageCollectorMXBean> GARBAGE_COLLECTORS =
            ManagementFactory.getGarbageCollectorMXBeans();
    private long lastCalculated = 0L;
    private long allocatedBytes = -1L;
    private long collectionCount = -1L;
    private long allocationRate = 0L;

    long allocationRate(final long allocatedBytes) {
        final long lastCalculatedTime = System.currentTimeMillis();
        if (lastCalculatedTime - this.lastCalculated >= 500L) {
            final long collectionCountVar = collectionCount();
            if (this.lastCalculated != 0L && collectionCountVar == this.collectionCount) {
                final double d =
                        (double) TimeUnit.SECONDS.toMillis(1L) / (double) (lastCalculatedTime - this.lastCalculated);
                final long n = allocatedBytes - this.allocatedBytes;
                this.allocationRate = Math.round(n * d);
            }

            this.lastCalculated = lastCalculatedTime;
            this.allocatedBytes = allocatedBytes;
            this.collectionCount = collectionCountVar;
        }
        return this.allocationRate;
    }

    private static long collectionCount() {
        long l = 0L;

        GarbageCollectorMXBean garbageCollectorMXBean;
        for (Iterator<GarbageCollectorMXBean> var2 = GARBAGE_COLLECTORS.iterator();
                var2.hasNext();
                l += garbageCollectorMXBean.getCollectionCount()) {
            garbageCollectorMXBean = var2.next();
        }

        return l;
    }

    private static String gpuUtilization() {
        final double gpuUtilizationPercentage = Minecraft.getInstance().getGpuUtilization();
        if (gpuUtilizationPercentage > 0.0) {
            return gpuUtilizationPercentage > 100.0
                    ? ChatFormatting.RED + "100%"
                    : Math.round(gpuUtilizationPercentage) + "%";
        }
        return "N/A";
    }

    @Override
    protected void loadModuleConfig(final Config moduleConfig) {
        this.memoryColorToggle = moduleConfig.getOrElse("memory_color_toggle", this.defaultMemoryColorToggle);
        this.timeFormat = moduleConfig.getOrElse("time_format", this.defaultTimeFormat);
    }

    @Override
    protected void saveModuleConfig(final Config moduleConfig) {
        moduleConfig.set("memory_color_toggle", this.memoryColorToggle);
        moduleConfig.set("time_format", this.timeFormat);
    }
}
