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

import static java.util.Objects.requireNonNull;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import net.minecrell.box.config.BoxBlock;
import net.minecrell.box.config.BoxSound;
import net.minecrell.box.config.BoxTicks;
import net.minecrell.box.config.FontShape;
import net.minecrell.box.game.BoxGame;
import net.minecrell.box.point.BlockDirection;
import net.minecrell.box.point.BoxLocation;
import net.minecrell.box.point.BoxVector;
import net.minecrell.box.region.Region2i;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Box extends Region2i implements Listener {

    private static final String NAME_DELIMITER = ChatColor.RESET + ", ";
    private static final Joiner NAME_JOINER = Joiner.on(NAME_DELIMITER);

    private static final Set<FontShape> START_COUNTDOWN = ImmutableSet.of(FontShape.THREE, FontShape.TWO,
            FontShape.ONE);

    private final Plugin plugin;

    private final BoxVector start, playerPos;
    private final Location playerStart;
    private final BlockDirection startDirection;
    private final Region2i end;

    private final BoxFont font;

    private boolean ready;
    private BukkitTask task;
    private boolean running;

    private BoxGame game;

    private String player;
    private Set<String> spectators;

    public Box(Plugin plugin, World world, int y, int x1, int z1, int x2, int z2,
            int startX, int startZ, float yaw, float pitch,
            BoxVector font1, BoxVector font2) {
        super(world, y, x1, z1, x2, z2);
        this.plugin = plugin;

        BoxVector playerPos = new BoxVector(startX, y, startZ);
        BoxVector start = null;
        BlockDirection startDirection = null;
        for (BlockDirection dir : BlockDirection.values()) {
            startDirection = dir;
            if (this.contains(start = dir.relative(playerPos))) {
                break;
            }
        }

        this.start = requireNonNull(start);
        this.startDirection = requireNonNull(startDirection);
        this.playerPos = playerPos.add(0, 1, 0);
        this.playerStart = this.playerPos.toLocation(world).toLocation();
        playerStart.add(0.5, 0, 0.5);
        playerStart.setYaw(yaw);
        playerStart.setPitch(pitch);

        if (startDirection.isLength()) {
            int z = start.getZ() + this.size(startDirection) - 1;
            this.end = new Region2i(world, y, x1, z, x2, z);
        } else {
            int x = start.getX() + this.size(startDirection) - 1;
            this.end = new Region2i(world, y, x, z1, x, z2);
        }

        this.font = BoxFont.create(world, font1, font2);
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public Server getServer() {
        return plugin.getServer();
    }

    public BoxVector getStart() {
        return start;
    }

    public BoxVector getPlayerPosition() {
        return playerPos;
    }

    public Location getPlayerStart() {
        return playerStart;
    }

    public BlockDirection getStartDirection() {
        return startDirection;
    }

    public Region2i getEnd() {
        return end;
    }

    @SuppressWarnings("deprecation")
    public Player getPlayer() {
        if (player == null) {
            return null;
        }

        return getServer().getPlayerExact(player);
    }

    public String getPlayerName() {
        return this.player;
    }

    @SuppressWarnings("deprecation")
    public Stream<Player> getSpectators() {
        if (spectators == null) {
            return null;
        }

        return spectators.stream().map(getServer()::getPlayerExact).filter(Objects::nonNull);
    }

    public boolean isPrepared() {
        return player != null;
    }

    private boolean checkPrepared(CommandSender sender) {
        if (!this.isPrepared()) {
            sender.sendMessage(ChatColor.RED + "Box is not prepared");
            return false;
        } else {
            return true;
        }
    }

    public boolean isReady() {
        if (ready && getPlayer() == null) {
            this.ready = false;
        }

        return ready;
    }

    public boolean checkReady(CommandSender sender) {
        if (!this.checkPrepared(sender)) {
            return false;
        }

        if (!this.isReady()) {
            sender.sendMessage(ChatColor.RED + "Box is not ready");
            return false;
        } else {
            return true;
        }
    }

    public void status(CommandSender sender) {
        if (!this.checkPrepared(sender)) {
            return;
        }

        Player player = this.getPlayer();
        sender.sendMessage(ChatColor.GOLD + "Player: " + ChatColor.RESET +
                (player != null ? player.getDisplayName() : this.player));
        if (spectators != null) {
            sender.sendMessage(ChatColor.GOLD + "Spectators: " + ChatColor.RESET +
                    getSpectators().map(Player::getDisplayName).collect(Collectors.joining(NAME_DELIMITER)));
        }
        if (this.isReady()) {
            sender.sendMessage(ChatColor.GREEN + "Box is ready");
        } else {
            sender.sendMessage(ChatColor.RED + "Box is not ready");
        }
    }

    public void prepare(CommandSender sender, String playerName, String... spectators) {
        if (this.isPrepared()) {
            Player player = this.getPlayer();
            sender.sendMessage(ChatColor.RED + "Already prepared for " + ChatColor.RESET + (player != null ?
                    player.getDisplayName() : this.player));
            return;
        }

        Player player = getPlugin().getServer().getPlayer(playerName);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Unknown player: " + playerName);
            return;
        }

        this.player = player.getName();

        sender.sendMessage(ChatColor.GOLD + "Box prepared for " + ChatColor.RESET + player.getDisplayName());
        this.addSpectators(sender, spectators);
    }

    public void addSpectators(CommandSender sender, String... playerNames) {
        if (playerNames.length <= 0 || !this.checkPrepared(sender)) {
            return;
        }

        if (spectators == null) {
            this.spectators = new HashSet<>();
        }

        Set<String> spectators = new HashSet<>();
        Set<String> skipped = new HashSet<>(0);
        for (String playerName : playerNames) {
            Player player = getServer().getPlayer(playerName);
            if (player != null && !player.getName().equals(this.player)) {
                this.spectators.add(player.getName());
                spectators.add(player.getDisplayName());
            } else {
                skipped.add(playerName);
            }
        }

        if (spectators.size() > 0) {
            sender.sendMessage(ChatColor.GOLD + "Added spectators: " + ChatColor.RESET + NAME_JOINER.join(spectators));
        }
        if (skipped.size() > 0) {
            sender.sendMessage(ChatColor.GOLD + "Skipped spectators: " + ChatColor.RESET + NAME_JOINER.join(skipped));
        }
    }

    public void ready(CommandSender sender) {
        if (!this.checkPrepared(sender)) {
            return;
        }

        if (this.isReady()) {
            sender.sendMessage(ChatColor.RED + "Box is already ready!");
            return;
        }

        if (this.getPlayer() != null) {
            this.setReady(true);
            this.status(sender);
        } else {
            sender.sendMessage(ChatColor.RESET + this.player + ChatColor.RED + " is not online!");
        }
    }

    public void setReady(boolean ready) {
        if (ready != this.ready) {
            this.ready = ready;
            if (ready) {
                getServer().getPluginManager().registerEvents(this, plugin);
            } else {
                HandlerList.unregisterAll(this);
            }
        }
    }

    public void pause() {
        this.running = false;
        HandlerList.unregisterAll(this.game);
        this.resetTask();
        if (this.game != null) {
            this.game.pause();
        }
    }

    public void pause(CommandSender sender) {
        if (!this.checkReady(sender)) {
            return;
        }

        this.setReady(false);
        this.pause();
        this.clear();
        sender.sendMessage(ChatColor.GOLD + "Box is no longer ready.");
    }

    public boolean isRunning() {
        return running;
    }

    public boolean checkRunning(CommandSender sender) {
        if (!isRunning()) {
            sender.sendMessage(ChatColor.RED + "Box is not running");
            return false;
        } else {
            return true;
        }
    }

    public void teleport() {
        getPlayer().teleport(playerStart);
    }

    public void start() {
        requireNonNull(this.game, "game");
        this.resetTask();

        this.teleport();
        this.clear();
        this.game.start();
        getServer().getPluginManager().registerEvents(this.game, plugin);

        this.running = true;
    }

    public void restart(CommandSender sender) {
        if (!checkRunning(sender)) {
            return;
        }

        this.pause();
        this.start();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.getPlayer().getName().equals(player)) {
            return;
        }

        BoxVector from = BoxVector.from(event.getFrom()), to = BoxVector.from(event.getTo());
        if (from.equals(to)) {
            return;
        }

        if (running) {
            this.game.onPlayerMove(event, from, to);
        } else {
            if (task == null) {
                if (playerPos.equals(to)) {
                    this.task = getServer().getScheduler().runTaskTimer(plugin, new StartCountdown(),
                            BoxTicks.START, BoxTicks.START);
                }
            } else if (!playerPos.equals(to)) {
                this.resetTask();
                this.clearFont();
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer().getName().equals(player)) {
            this.reset();
        }
    }

    private void resetTask() {
        if (task != null) {
            task.cancel();
            this.task = null;
        }
    }

    public void clear() {
        this.clearBlocks();
        this.clearFont();
    }

    public void clearBlocks() {
        for (BoxLocation loc : this) {
            displayBlock(loc, BoxBlock.BASE, null);
        }
    }

    public void clearFont() {
        this.drawFontShape(FontShape.BASE);
    }

    public void reset() {
        this.setReady(false);
        this.pause();
        this.clear();
        this.player = null;
        this.spectators = null;
    }

    public void displayBlock(BoxVector loc, MaterialData block, Sound sound) {
        this.displayBlock(getPlayer(), loc, block, sound);
        if (spectators != null) {
            this.getSpectators().forEach((player) -> displayBlock(player, loc, block, sound));
        }
    }

    @SuppressWarnings("deprecation")
    public void displayBlock(Player player, BoxVector loc, MaterialData block, Sound sound) {
        if (player == null) {
            return;
        }

        Location l = loc.toLocation(this.getWorld()).toLocation();
        player.sendBlockChange(l, block.getItemType(), block.getData());
        if (sound != null) {
            player.playSound(l, sound, 1, 1);
        }
    }

    public void drawFontShape(FontShape shape) {
        this.drawFontShape(shape, shape.getMaterial());
    }

    public void drawFontShape(FontShape shape, MaterialData material) {
        font.draw(shape).forEach((loc) -> displayBlock(loc, material, null));
    }

    private class StartCountdown implements Runnable {

        private final Iterator<FontShape> countdown = START_COUNTDOWN.iterator();
        private FontShape current;

        @Override
        public void run() {
            if (countdown.hasNext()) {
                if (current != null) {
                    drawFontShape(current, FontShape.BASE.getMaterial());
                }
                drawFontShape(current = countdown.next());
                Player player = getPlayer();
                player.playSound(player.getLocation(), BoxSound.COUNTDOWN, 1, 0);
            } else {
                Player player = getPlayer();
                player.playSound(player.getLocation(), BoxSound.COUNTDOWN, 1, 2);
                start();
            }
        }
    }


}
