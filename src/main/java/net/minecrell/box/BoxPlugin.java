/*
 * Box
 * Copyright (c) 2014, Minecrell <https://github.com/Minecrell>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.minecrell.box;

import net.minecrell.box.points.BoxVector;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class BoxPlugin extends JavaPlugin {

    private Box box;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        ConfigurationSection box = this.getConfig().getConfigurationSection("box");
        this.box = new Box(this,
                this.getServer().getWorld(box.getString("world")),
                box.getInt("y"),
                box.getInt("pos1.x"), box.getInt("pos1.z"),
                box.getInt("pos2.x"), box.getInt("pos2.z"),
                box.getInt("start.x"), box.getInt("start.z"),
                (float) box.getDouble("start.yaw"), (float) box.getDouble("start.pitch"),
                parseVector(box.getConfigurationSection("font.pos1")),
                parseVector(box.getConfigurationSection("font.pos2"))
        );

        this.getLogger().info(this.getName() + " enabled.");
    }

    @Override
    public void onDisable() {
        box.reset();
        this.getLogger().info(this.getName() + " disabled.");
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub = args.length > 0 ? args[0] : "status";
        if ("status".equalsIgnoreCase(sub)) {
            box.status(sender);
        } else if ("prepare".equalsIgnoreCase(sub)) {
            if (args.length > 1) {
                box.prepare(sender, args[1],
                        args.length > 2 ? Arrays.copyOfRange(args, 2, args.length) : new String[0]);
            } else showHelp(sender);
        } else if ("add".equalsIgnoreCase(sub)) {
            if (args.length > 1) {
                box.addSpectators(sender, Arrays.copyOfRange(args, 1, args.length));
            } else showHelp(sender);
        } else if ("ready".equalsIgnoreCase(sub)) {
            box.ready(sender);
        } else if ("new".equalsIgnoreCase(sub)) {
            box.newPath(sender);
        } else if ("easy".equalsIgnoreCase(sub)) {
            box.easyPath(sender);
        } else if ("pause".equalsIgnoreCase(sub)) {
            box.pause(sender);
        } else if ("reset".equalsIgnoreCase(sub)) {
            box.reset();
            sender.sendMessage(ChatColor.GREEN + "Box cleared.");
        } else showHelp(sender);

        return true;
    }

    public void showHelp(CommandSender sender) {
        sendCommandHelp(sender,
                "help", "Show this help page.",
                "[status]", "Show the current box status.",
                "prepare <Player> [Spectators...]", "Prepare the box for a player.",
                "add <Spectators...>", "Add spectators to the prepared box.",
                "ready", "Set the box to ready.",
                "new", "Create a new path",
                "pause", "Pause the box until it is ready again.",
                "reset", "Reset the prepared game.");
    }

    private void sendCommandHelp(CommandSender sender, String... commands) {
        for (int i = 1; i < commands.length; i += 2) {
            sender.sendMessage(ChatColor.GRAY + "/box " + commands[i - 1]
                    + ChatColor.WHITE + " - " + ChatColor.GOLD + commands[i]);
        }
    }

    private static BoxVector parseVector(ConfigurationSection section) {
        return new BoxVector(section.getInt("x"), section.getInt("y"), section.getInt("z"));
    }
}
