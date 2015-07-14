package net.minecrell.box;

import net.minecrell.box.config.BoxBlock;
import net.minecrell.box.config.BoxSound;
import net.minecrell.box.config.BoxTicks;
import net.minecrell.box.config.FontShape;
import net.minecrell.box.points.BlockDirection;
import net.minecrell.box.points.BoxLocation;
import net.minecrell.box.points.BoxVector;
import net.minecrell.box.regions.BoxRegion;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class Box extends BoxRegion implements Listener {
    private static final Random RANDOM = new Random();
    private static final Joiner NAME_JOINER = Joiner.on(ChatColor.RESET + ", ");
    private static final Set<FontShape> START_COUNTDOWN = ImmutableSet.of(FontShape.THREE, FontShape.TWO,
            FontShape.ONE);

    private final Plugin plugin;

    private final BoxVector start, playerPos;
    private final Location playerStart;
    private final BlockDirection startDirection;
    private final BoxRegion end;

    private final BoxFont font;

    private boolean ready;
    private BukkitTask task;
    private BlockTask blockTask;
    private boolean running;
    private Set<BoxVector> path, walkedPath;
    private Iterator<BoxVector> pathIterator;

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
            if (this.contains(start = dir.relative(playerPos)))
                break;
        }

        this.start = Preconditions.checkNotNull(start);
        this.startDirection = Preconditions.checkNotNull(startDirection);
        this.playerPos = playerPos.add(0, 1, 0);
        this.playerStart = this.playerPos.toLocation(world).toLocation();
        playerStart.add(0.5, 0, 0.5);
        playerStart.setYaw(yaw);
        playerStart.setPitch(pitch);

        if (startDirection.isLength()) {
            int z = start.getZ() + this.size(startDirection) - 1;
            this.end = new BoxRegion(world, y, x1, z, x2, z);
        } else {
            int x = start.getX() + this.size(startDirection) - 1;
            this.end = new BoxRegion(world, y, x, z1, x, z2);
        }

        this.font = BoxFont.create(world, font1, font2);
    }

    public Plugin getPlugin() {
        return plugin;
    }

    @SuppressWarnings("deprecation")
    public Player player() {
        if (player == null) return null;
        return Bukkit.getPlayerExact(player);
    }

    @SuppressWarnings("deprecation")
    public Iterable<Player> spectators() {
        if (spectators == null) return null;
        return Iterables.filter(Iterables.transform(spectators, Bukkit::getPlayerExact), player -> player != null);
    }

    public boolean isPrepared() {
        return player != null;
    }

    private boolean checkPrepared(CommandSender sender) {
        if (!this.isPrepared()) {
            sender.sendMessage(ChatColor.RED + "Box is not prepared"); return false;
        } else return true;
    }

    public boolean isReady() {
        if (ready && player() == null) this.ready = false;
        return ready;
    }

    public boolean checkReady(CommandSender sender) {
        if (!this.checkPrepared(sender)) return false;
        if (!this.isReady()) {
            sender.sendMessage(ChatColor.RED + "Box is not ready"); return false;
        } else return true;
    }

    public void status(CommandSender sender) {
        if (!this.checkPrepared(sender)) return;
        Player player = this.player();
        sender.sendMessage(ChatColor.GOLD + "Player: " + ChatColor.RESET +
                (player != null ? player.getDisplayName() : this.player));
        if (spectators != null)
            sender.sendMessage(ChatColor.GOLD + "Spectators: " + ChatColor.RESET + NAME_JOINER.join(
                Iterables.transform(spectators(), Player::getDisplayName)));
        if (this.isReady())
            sender.sendMessage(ChatColor.GREEN + "Box is ready");
        else sender.sendMessage(ChatColor.RED + "Box is not ready");
    }

    public void prepare(CommandSender sender, String playerName, String... spectators) {
        if (this.isPrepared()) {
            Player player = this.player();
            sender.sendMessage(ChatColor.RED + "Already prepared for " + ChatColor.RESET + (player != null ?
                    player.getDisplayName() : this.player)); return;
        }

        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Unknown player: " + playerName); return;
        }

        this.player = player.getName();

        sender.sendMessage(ChatColor.GOLD + "Box prepared for " + ChatColor.RESET + player.getDisplayName());
        this.addSpectators(sender, spectators);
    }

    public void addSpectators(CommandSender sender, String... playerNames) {
        if (playerNames.length <= 0) return;
        if (!this.checkPrepared(sender)) return;
        if (spectators == null) this.spectators = new HashSet<>();

        Set<String> spectators = new HashSet<>();
        Set<String> skipped = new HashSet<>(0);
        for (String playerName : playerNames) {
            Player player = Bukkit.getPlayer(playerName);
            if (player != null && !player.getName().equals(this.player)) {
                this.spectators.add(player.getName());
                spectators.add(player.getDisplayName());
            } else {
                skipped.add(playerName);
            }
        }

        if (spectators.size() > 0) sender.sendMessage(ChatColor.GOLD + "Added spectators: " + ChatColor.RESET +
                NAME_JOINER.join(spectators));
        if (skipped.size() > 0) sender.sendMessage(ChatColor.GOLD + "Skipped spectators: " + ChatColor.RESET +
                NAME_JOINER.join(skipped));
    }

    public void ready(CommandSender sender) {
        if (!this.checkPrepared(sender)) return;
        if (this.isReady()) {
            sender.sendMessage(ChatColor.RED + "Box is already ready!"); return;
        }

        if (this.player() != null) {
            this.setReady(true);
            this.status(sender);
        } else
            sender.sendMessage(ChatColor.RESET + this.player + ChatColor.RED + " is not online!");
    }

    public void setReady(boolean ready) {
        if (ready != this.ready) {
            this.ready = ready;
            if (ready)
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
            else HandlerList.unregisterAll(this);
        }
    }

    public void pause() {
        this.running = false;
        this.resetTask();

        this.path = null;
        this.pathIterator = null;
        this.walkedPath = null;

        Player player = player();
        if (player != null) {
            player.setExp(0);
            player.setLevel(0);
        }
    }

    public void pause(CommandSender sender) {
        if (!this.checkReady(sender)) return;
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
            sender.sendMessage(ChatColor.RED + "Box is not running"); return false;
        } else return true;
    }

    public void teleport() {
        player().teleport(playerStart);
    }

    public void start() {
        this.resetTask();

        this.teleport();
        this.startNow();
    }

    public void startNow() {
        this.startNow(randomPath());
    }

    public void startNow(Set<BoxVector> path) {
        this.clear();

        Player player = player();
        player.setGameMode(GameMode.ADVENTURE);
        player.setExp(0);
        player.setLevel(0);

        this.running = true;
        this.path = path;
        this.task = plugin.getServer().getScheduler().runTaskTimer(plugin, this.blockTask = new PreviewBlockTask(
                        path.iterator(), BoxBlock.WAY, BoxSound.WAY), BoxTicks.PREVIEW_DELAY, BoxTicks.PREVIEW);
    }

    public void pathComplete() {
        this.resetTask();
        this.task = plugin.getServer().getScheduler().runTaskTimer(plugin, this.blockTask = new RemoveBlockTask(
                path.iterator(), BoxBlock.BASE, BoxSound.REMOVE), BoxTicks.REMOVE_DELAY, BoxTicks.REMOVE);
    }

    public void checkPath() {
        this.resetTask();
        this.pathIterator = path.iterator();
        this.walkedPath = new HashSet<>();
    }

    public void newPath(CommandSender sender) {
        if (!checkRunning(sender)) return;
        this.pause();
        this.start();
    }

    public void easyPath(CommandSender sender) {
        if (!checkRunning(sender)) return;
        this.pause();
        this.teleport();
        this.startNow(createEasyPath());
    }

    public void checkPath(BoxVector to) {
        if (walkedPath.contains(to)) return;

        walkedPath.add(to);
        BoxVector current = pathIterator.next();
        if (current.equals(to)) {
            displayBlock(to, BoxBlock.SUCCESS, BoxSound.STEP_SUCCESS);

            Player player = player();
            player.setLevel(path.size() - walkedPath.size());
            player().setExp((float) ((double) walkedPath.size() / (double) path.size()));

            if (!pathIterator.hasNext()) {
                this.drawFontShape(FontShape.SUCCESS);
                this.task = this.getPlugin().getServer().getScheduler().runTaskLater(plugin, () -> {
                    pathIterator = null;
                    resetTask();
                    clearBlocks();
                    teleport();
                    this.task = getPlugin().getServer().getScheduler().runTaskLater(plugin, Box.this::start,
                            BoxTicks.RESPAWN);
                }, BoxTicks.END);
            }
        } else {
            displayBlock(to, BoxBlock.FAIL, BoxSound.FAIL);
            displayBlock(current, BoxBlock.WAY, null);
            while (pathIterator.hasNext())
                displayBlock(player(), pathIterator.next(), BoxBlock.WAY, null);
            this.drawFontShape(FontShape.FAIL);
            this.task = this.getPlugin().getServer().getScheduler().runTaskLater(plugin, this::failPath,
                    BoxTicks.END);
        }
    }

    public void failPath() {
        this.pause();
        player().setHealth(0);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.getPlayer().getName().equals(player)) return;
        BoxVector from = BoxVector.from(event.getFrom()), to = BoxVector.from(event.getTo());
        if (from.equals(to)) return;
        if (running) {
            if (pathIterator == null) {
                if (task != null && !playerPos.equals(to)) {
                    event.getTo().setX(playerStart.getX());
                    event.getTo().setY(playerStart.getY());
                    event.getTo().setZ(playerStart.getZ());
                    event.getPlayer().teleport(event.getTo());
                    if (blockTask != null)
                        blockTask.run();
                }
            } else if (task == null) {
                if (!playerPos.equals(to)) {
                    to = to.subtract(0, 1, 0);
                    if (this.contains(to)) {
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
        } else {
            if (task == null) {
                if (playerPos.equals(to))
                    this.task = getPlugin().getServer().getScheduler().runTaskTimer(plugin, new StartCountdown(),
                        BoxTicks.START, BoxTicks.START);
            } else if (!playerPos.equals(to)) {
                this.resetTask();
                this.clearFont();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!event.getPlayer().getName().equals(player)) return;
        event.setRespawnLocation(playerStart);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            clearBlocks();
            drawFontShape(FontShape.FAIL);
        }, 2);
        plugin.getServer().getScheduler().runTaskLater(plugin, this::start, BoxTicks.RESPAWN);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer().getName().equals(player))
            this.reset();
    }

    private void resetTask() {
        if (task != null) {
            task.cancel();
            this.task = null;
            this.blockTask = null;
        }
    }

    public void clear() {
        this.clearBlocks();
        this.clearFont();
    }

    public void clearBlocks() {
        for (BoxLocation loc : this)
            displayBlock(loc, BoxBlock.BASE, null);
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

    private Set<BoxVector> createEasyPath() {
        ImmutableSet.Builder<BoxVector> builder = ImmutableSet.builder();

        BoxVector pos = start;
        builder.add(start);
        do {
            builder.add(pos = startDirection.relative(pos));
        } while (!end.contains(pos));

        return builder.build();
    }

    private Set<BoxVector> randomPath() {
        BlockDirection dir, opposite = startDirection.opposite();
        BlockDirection[] dirs = BlockDirection.values();

        BoxVector pos = start;
        Set<BoxVector> path = new LinkedHashSet<>();
        path.add(start);
        path.add(pos = startDirection.relative(pos));

        do {
            BoxVector next;
            do {
                dir = dirs[RANDOM.nextInt(dirs.length)];
                next = dir.relative(pos);
            } while (dir == opposite || path.contains(next) || !this.contains(next) || nextTo(pos, next, path));

            path.add(pos = next);
        } while (!end.contains(pos));

        return ImmutableSet.copyOf(path);
    }

    private static boolean nextTo(BoxVector pos, BoxVector next, Set<BoxVector> path) {
        for (BoxVector vector : path) {
            if (vector == pos) continue;
            for (BlockDirection dir : BlockDirection.values())
                if (next.equals(dir.relative(vector))) return true;
        }

        return false;
    }

    private void displayBlock(BoxVector loc, MaterialData block, Sound sound) {
        this.displayBlock(player(), loc, block, sound);
        if (spectators != null)
            for (Player player : this.spectators()) this.displayBlock(player, loc, block, sound);
    }

    @SuppressWarnings("deprecation")
    private void displayBlock(Player player, BoxVector loc, MaterialData block, Sound sound) {
        if (player == null) return;
        Location l = loc.toLocation(this.getWorld()).toLocation();
        player.sendBlockChange(l, block.getItemType(), block.getData());
        if (sound != null) player.playSound(l, sound, 1, 1);
    }

    private void drawFontShape(FontShape shape) {
        this.drawFontShape(shape, shape.getMaterial());
    }

    private void drawFontShape(FontShape shape, MaterialData material) {
        for (BoxLocation loc : font.draw(shape))
            displayBlock(loc, material, null);
    }

    private class StartCountdown implements Runnable {
        private final Iterator<FontShape> countdown = START_COUNTDOWN.iterator();
        private FontShape current;

        @Override
        public void run() {
            if (countdown.hasNext()) {
                if (current != null) drawFontShape(current, FontShape.BASE.getMaterial());
                drawFontShape(current = countdown.next());
                Player player = player();
                player.playSound(player.getLocation(), BoxSound.COUNTDOWN, 1, 0);
            } else {
                Player player = player();
                player.playSound(player.getLocation(), BoxSound.COUNTDOWN, 1, 2);
                start();
            }
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
            } else done();
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
            displayBlock(v, block, sound);
            player().setLevel(++counter);
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
            displayBlock(player(), v, block, sound);
        }

        @Override
        protected void done() {
            checkPath();
        }
    }
}
