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
import net.minecrell.box.config.BoxBlock;
import net.minecrell.box.config.BoxSound;
import net.minecrell.box.config.BoxTicks;
import net.minecrell.box.config.FontShape;
import net.minecrell.box.point.BoxVector;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MemorizePathGame extends BoxGame {

    private PathType pathType = PathType.DEFAULT;

    private Set<BoxVector> path, walkedPath;
    private Iterator<BoxVector> pathIterator;

    private BukkitTask task;
    private BlockTask blockTask;

    public MemorizePathGame(Box box) {
        super(box);
    }

    public PathType getPathType() {
        return pathType;
    }

    public void setPathType(PathType pathType) {
        this.pathType = pathType;
    }

    @Override
    public void start() {
        start(this.pathType);
    }

    public void start(PathType type) {
        start(type.generate(box));
    }

    public void start(Set<BoxVector> path) {
        this.path = requireNonNull(path, "path");

        Player player = getPlayer();
        player.setGameMode(GameMode.ADVENTURE);
        player.setExp(0);
        player.setLevel(0);

        this.task = getScheduler().runTaskTimer(getPlugin(), this.blockTask = new PreviewBlockTask(
                path.iterator(), BoxBlock.WAY, BoxSound.WAY), BoxTicks.PREVIEW_DELAY, BoxTicks.PREVIEW);
    }

    private void pathComplete() {
        this.resetTask();
        this.task = getScheduler().runTaskTimer(getPlugin(), this.blockTask = new RemoveBlockTask(
                path.iterator(), BoxBlock.BASE, BoxSound.REMOVE), BoxTicks.REMOVE_DELAY, BoxTicks.REMOVE);
    }

    private void checkPath() {
        this.resetTask();
        this.pathIterator = path.iterator();
        this.walkedPath = new HashSet<>();
    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event, BoxVector from, BoxVector to) {
        BoxVector playerPos = box.getPlayerPosition();

        if (pathIterator == null) {
            if (task != null && !playerPos.equals(to)) {
                Location playerStart = box.getPlayerStart();
                event.getTo().setX(playerStart.getX());
                event.getTo().setY(playerStart.getY());
                event.getTo().setZ(playerStart.getZ());
                event.getPlayer().teleport(event.getTo());
                if (blockTask != null) {
                    blockTask.run();
                }
            }
        } else if (task == null) {
            if (!playerPos.equals(to)) {
                to = to.subtract(0, 1, 0);
                if (box.contains(to)) {
                    checkPath(to);
                } else {
                    Location tp = event.getFrom().clone();
                    tp.setYaw(event.getTo().getYaw());
                    tp.setPitch(event.getTo().getPitch());
                    event.setTo(tp);
                    event.getPlayer().teleport(tp);
                }
            }
        }
    }

    private void checkPath(BoxVector to) {
        if (walkedPath.contains(to)) {
            return;
        }

        walkedPath.add(to);
        BoxVector current = pathIterator.next();
        if (current.equals(to)) {
            box.displayBlock(to, BoxBlock.SUCCESS, BoxSound.STEP_SUCCESS);

            Player player = getPlayer();
            player.setLevel(path.size() - walkedPath.size());
            getPlayer().setExp((float) ((double) walkedPath.size() / (double) path.size()));

            if (!pathIterator.hasNext()) {
                box.drawFontShape(FontShape.SUCCESS);
                this.task = getScheduler().runTaskLater(getPlugin(), () -> {
                    pathIterator = null;
                    resetTask();
                    box.clearBlocks();
                    box.teleport();
                    this.task = getScheduler().runTaskLater(getPlugin(), box::start, BoxTicks.RESPAWN);
                }, BoxTicks.END);
            }
        } else {
            box.displayBlock(to, BoxBlock.FAIL, BoxSound.FAIL);
            box.displayBlock(current, BoxBlock.WAY, null);
            while (pathIterator.hasNext()) {
                box.displayBlock(getPlayer(), pathIterator.next(), BoxBlock.WAY, null);
            }
            box.drawFontShape(FontShape.FAIL);
            this.task = getScheduler().runTaskLater(getPlugin(), this::failPath, BoxTicks.END);
        }
    }

    public void failPath() {
        this.pause();
        getPlayer().setHealth(0);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!event.getPlayer().getName().equals(box.getPlayerName())) {
            return;
        }

        event.setRespawnLocation(box.getPlayerStart());
        getScheduler().runTaskLater(getPlugin(), () -> {
            box.clearBlocks();
            box.drawFontShape(FontShape.FAIL);
        }, 2);
        getScheduler().runTaskLater(getPlugin(), this::start, BoxTicks.RESPAWN);
    }

    @Override
    public void pause() {
        Player player = getPlayer();
        if (player != null) {
            player.setExp(0);
            player.setLevel(0);
        }

        this.path = null;
        this.pathIterator = null;
        this.walkedPath = null;
    }

    private void resetTask() {
        if (task != null) {
            task.cancel();
            this.task = null;
            this.blockTask = null;
        }
    }

    private abstract class BlockTask implements Runnable {

        private final Iterator<BoxVector> path;
        private final MaterialData block;
        private final Sound sound;

        private BlockTask(Iterator<BoxVector> path, MaterialData block, Sound sound) {
            this.path = path;
            this.block = block;
            this.sound = sound;
        }

        @Override
        public void run() {
            if (path.hasNext()) {
                process(path.next(), block, sound);
            } else {
                done();
            }
        }

        protected abstract void process(BoxVector v, MaterialData block, Sound sound);

        protected abstract void done();
    }

    private class PreviewBlockTask extends BlockTask {

        private int counter = 0;

        private PreviewBlockTask(Iterator<BoxVector> path, MaterialData block, Sound sound) {
            super(path, block, sound);
        }

        @Override
        protected void process(BoxVector v, MaterialData block, Sound sound) {
            box.displayBlock(v, block, sound);
            getPlayer().setLevel(++counter);
        }

        @Override
        protected void done() {
            pathComplete();
        }
    }

    private class RemoveBlockTask extends BlockTask {

        private RemoveBlockTask(Iterator<BoxVector> path, MaterialData block, Sound sound) {
            super(path, block, sound);
        }

        @Override
        protected void process(BoxVector v, MaterialData block, Sound sound) {
            box.displayBlock(getPlayer(), v, block, sound);
        }

        @Override
        protected void done() {
            checkPath();
        }
    }

}
