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

import net.minecrell.box.game.BoxGameType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public enum BoxCommand {
    STATUS("Show the current box status.") {

        @Override
        public void execute(Box box, CommandSender sender, String[] args) {
            box.status(sender);
        }
    },
    PREPARE("<game> <player> [spectators...]", "Prepare a specific game in the box for a player.") {

        @Override
        public void execute(Box box, CommandSender sender, String[] args) {
            if (args.length > 2) {
                BoxGameType gameType = BoxGameType.of(args[1]);
                if (gameType == null) {
                    sender.sendMessage(ChatColor.RED + "Unknown game type: " + args[1]);
                    sender.sendMessage(ChatColor.GOLD + "Available game types: " +
                            Arrays.stream(BoxGameType.values()).map(type -> type.name().toLowerCase(Locale.ENGLISH))
                                    .collect(Collectors.joining(", ")));
                    return;
                }

                box.prepare(sender, gameType, args[2],
                        args.length > 3 ? Arrays.copyOfRange(args, 3, args.length) : new String[0]);
            } else {
                HELP.execute(box, sender, args);
            }
        }
    },
    ADD("<spectators...>", "Add spectators to the prepared box.") {

        @Override
        public void execute(Box box, CommandSender sender, String[] args) {
            if (args.length > 1) {
                box.addSpectators(sender, Arrays.copyOfRange(args, 1, args.length));
            } else {
                HELP.execute(box, sender, args);
            }
        }
    },
    READY("Set the box to ready.") {

        @Override
        public void execute(Box box, CommandSender sender, String[] args) {
            box.ready(sender);
        }
    },
    RESTART("Restart the game.") {

        @Override
        public void execute(Box box, CommandSender sender, String[] args) {
            box.restart(sender);
        }
    },
    PAUSE("Pause the box until it is ready again.") {

        @Override
        public void execute(Box box, CommandSender sender, String[] args) {
            box.pause(sender);
        }
    },
    RESET("Reset the current game.") {

        @Override
        public void execute(Box box, CommandSender sender, String[] args) {
            box.reset();
            sender.sendMessage(ChatColor.GREEN + "Box cleared.");
        }
    },
    HELP("Show this help page.") {

        @Override
        public void execute(Box box, CommandSender sender, String[] args) {
            for (BoxCommand command : values()) {
                StringBuilder help = new StringBuilder();
                help.append(ChatColor.GRAY).append("/box ").append(command.name().toLowerCase(Locale.ENGLISH));
                if (command.usage != null) {
                    help.append(' ').append(command.usage);
                }
                help.append(ChatColor.WHITE).append(" - ").append(ChatColor.GOLD).append(command.description);

                sender.sendMessage(help.toString());
            }
        }
    };

    public static final BoxCommand DEFAULT = STATUS;

    private final String usage;
    private final String description;

    BoxCommand(String description) {
        this(null, description);
    }

    BoxCommand(String usage, String description) {
        this.usage = usage;
        this.description = description;
    }

    public abstract void execute(Box box, CommandSender sender, String[] args);

    public static BoxCommand of(String[] args) {
        if (args.length > 0) {
            try {
                return valueOf(args[0].toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException ignored) {
                return HELP;
            }
        } else {
            return DEFAULT;
        }
    }

    public static void process(Box box, CommandSender sender, String[] args) {
        of(args).execute(box, sender, args);
    }

}
