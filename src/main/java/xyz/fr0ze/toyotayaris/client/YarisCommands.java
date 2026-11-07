package xyz.fr0ze.toyotayaris.client;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import xyz.fr0ze.toyotayaris.ToyotaYarisMod;
import xyz.fr0ze.toyotayaris.client.renderer.EntityScaleHandler;

@EventBusSubscriber(
        modid = ToyotaYarisMod.MOD_ID,
        value = Dist.CLIENT
)

public class YarisCommands {

    @SubscribeEvent
    public static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("yaris")
                        .then(Commands.literal("squash")
                                .then(Commands.argument(
                                                "value",
                                                DoubleArgumentType.doubleArg(0.0, 1000.0)
                                        )
                                        .executes(ctx -> {
                                            double squash = DoubleArgumentType.getDouble(
                                                    ctx, "value"
                                            );

                                            EntityScaleHandler.SQUASH_STRENGTH = squash;

                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal(
                                                            "Squash strength set to " + squash
                                                    ),
                                                    false
                                            );

                                            return 1;
                                        }))
                        )
        );
    }
}
