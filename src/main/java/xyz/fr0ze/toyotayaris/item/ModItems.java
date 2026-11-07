package xyz.fr0ze.toyotayaris.item;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import xyz.fr0ze.toyotayaris.ToyotaYarisMod;
import xyz.fr0ze.toyotayaris.sound.ModSounds;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ToyotaYarisMod.MOD_ID);

    public static final DeferredItem<Item> WHY_THIS_DEALER_MUSIC_DISC = ITEMS.register("why_this_dealer_music_disc",
            () -> new Item(new Item.Properties().jukeboxPlayable(ModSounds.WHY_THIS_DEALER_KEY).stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
