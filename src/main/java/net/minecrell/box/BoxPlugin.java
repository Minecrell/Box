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

import net.minecrell.box.point.BoxVector;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        BoxCommand.process(this.box, sender, args);
        return true;
    }

    private static BoxVector parseVector(ConfigurationSection section) {
        return new BoxVector(section.getInt("x"), section.getInt("y"), section.getInt("z"));
    }

}
