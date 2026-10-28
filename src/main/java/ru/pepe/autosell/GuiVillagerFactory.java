package ru.pepe.autosell;

import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;

/** Creates a render-only villager, without spawning it or consuming a server entity ID. */
final class GuiVillagerFactory {
    // This entity never enters the world's entity index. A stable negative ID
    // lets the item/model renderer seed its state without aliasing a server ID.
    static final int RENDER_ID = -939001;

    static Villager create(Level level) {
        Villager villager = new Villager(EntityTypes.VILLAGER, level);
        return prepareForRendering(villager);
    }

    static Villager prepareForRendering(Villager villager) {
        // A client level does not assign IDs to standalone entities. The
        // renderer calls getId() even when the entity only exists in a GUI.
        villager.setId(RENDER_ID);
        return villager;
    }
}
