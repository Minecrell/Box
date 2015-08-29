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
package net.minecrell.box.game;

import static java.util.Objects.requireNonNull;

import net.minecrell.box.Box;
import net.minecrell.box.point.BoxVector;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.stream.Stream;

public abstract class BoxGame implements Listener {

    protected final Box box;

    public BoxGame(Box box) {
        this.box = requireNonNull(box, "box");
    }

    public final Box getBox() {
        return box;
    }

    protected final Plugin getPlugin() {
        return box.getPlugin();
    }

    protected final Server getServer() {
        return box.getServer();
    }

    protected final BukkitScheduler getScheduler() {
        return getServer().getScheduler();
    }

    protected final Player getPlayer() {
        return box.getPlayer();
    }

    protected final Stream<Player> getSpectators() {
        return box.getSpectators();
    }

    public abstract void start();

    public abstract void pause();

    public void onPlayerMove(PlayerMoveEvent event, BoxVector from, BoxVector to) {
    }

}
