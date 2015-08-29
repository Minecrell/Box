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

import com.google.common.collect.ImmutableSet;
import net.minecrell.box.Box;
import net.minecrell.box.point.BlockDirection;
import net.minecrell.box.point.BoxVector;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public enum PathType {

    RANDOM {

        @Override
        public Set<BoxVector> generate(Box box) {
            BlockDirection dir, opposite = box.getStartDirection().opposite();
            BlockDirection[] dirs = BlockDirection.values();

            BoxVector pos = box.getStart();
            Set<BoxVector> path = new LinkedHashSet<>();
            path.add(pos);
            path.add(pos = box.getStartDirection().relative(pos));

            do {
                BoxVector next;
                do {
                    dir = dirs[ThreadLocalRandom.current().nextInt(dirs.length)];
                    next = dir.relative(pos);
                } while (dir == opposite || path.contains(next) || !box.contains(next) || nextTo(pos, next, path));

                path.add(pos = next);
            } while (!box.getEnd().contains(pos));

            return ImmutableSet.copyOf(path);
        }

        private boolean nextTo(BoxVector pos, BoxVector next, Set<BoxVector> path) {
            for (BoxVector vector : path) {
                if (vector == pos) {
                    continue;
                }
                for (BlockDirection dir : BlockDirection.values()) {
                    if (next.equals(dir.relative(vector))) {
                        return true;
                    }
                }
            }

            return false;
        }

    }, SIMPLE {

        @Override
        public Set<BoxVector> generate(Box box) {
            ImmutableSet.Builder<BoxVector> builder = ImmutableSet.builder();

            BoxVector pos = box.getStart();
            builder.add(pos);
            do {
                builder.add(pos = box.getStartDirection().relative(pos));
            } while (!box.getEnd().contains(pos));

            return builder.build();
        }

    };

    public static final PathType DEFAULT = RANDOM;

    public abstract Set<BoxVector> generate(Box box);

}
