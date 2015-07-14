package net.minecrell.box.config;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;

import static org.bukkit.DyeColor.*;

public final class BoxBlock {
    private BoxBlock() {}

    public static final MaterialData
            BASE = stainedGlass(SILVER),
            WAY = stainedGlass(BLUE),
            SUCCESS = stainedGlass(LIME),
            FAIL = stainedGlass(RED);

    private static MaterialData stainedGlass(DyeColor color) {
        return coloredBlock(Material.STAINED_GLASS, color);
    }

    private static MaterialData coloredBlock(Material material, DyeColor color) {
        Wool wool = new Wool(material);
        wool.setColor(color);
        return wool;
    }
}
