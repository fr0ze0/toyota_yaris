package xyz.fr0ze.toyotayaris.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import xyz.fr0ze.toyotayaris.ToyotaYarisMod;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ToyotaYarisMod.MOD_ID);

    public static final Supplier<CreativeModeTab> TOYOTA_YARIS_TAB = CREATIVE_MODE_TAB.register("toyota_yaris_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.WHY_THIS_DEALER_MUSIC_DISC.get()))
                    .title(Component.translatable("creativetab.toyotayaris.toyota_yaris_items"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.WHY_THIS_DEALER_MUSIC_DISC);
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
