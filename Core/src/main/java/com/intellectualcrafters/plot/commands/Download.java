package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.general.commands.CommandDeclaration;

import java.net.URL;

@CommandDeclaration(command = "download", aliases = { "dl" }, category = CommandCategory.SCHEMATIC, requiredType = RequiredType.NONE, description = "Download your plot", permission = "plots.download")
public class Download extends SubCommand {
    
    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        final String world = plr.getLocation().getWorld();
        if (!PS.get().hasPlotArea(world)) {
            return !sendMessage(plr, C.NOT_IN_PLOT_WORLD);
        }
        final Plot plot = plr.getCurrentPlot();
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            MainUtil.sendMessage(plr, C.PLOT_UNOWNED);
            return false;
        }
        if ((!plot.isOwner(plr.getUUID()) || (Settings.DOWNLOAD_REQUIRES_DONE && (FlagManager.getPlotFlagRaw(plot, "done") != null))) && !Permissions.hasPermission(plr, "plots.admin.command.download")) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        if (plot.getRunning() > 0) {
            MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
            return false;
        }
        plot.addRunning();
        MainUtil.sendMessage(plr, C.GENERATING_LINK);
        SchematicHandler.manager.getCompoundTag(plot, new RunnableVal<CompoundTag>() {
            @Override
            public void run(final CompoundTag value) {
                TaskManager.runTaskAsync(new Runnable() {
                    @Override
                    public void run() {
                        final URL url = SchematicHandler.manager.upload(value, null, null);
                        if (url == null) {
                            MainUtil.sendMessage(plr, C.GENERATING_LINK_FAILED);
                            plot.removeRunning();
                            return;
                        }
                        MainUtil.sendMessage(plr, url.toString());
                        plot.removeRunning();
                    }
                });
            }
        });
        return true;
    }
}
