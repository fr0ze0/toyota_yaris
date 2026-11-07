package xyz.fr0ze.toyotayaris.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import xyz.fr0ze.toyotayaris.ToyotaYarisMod;

import java.util.*;

@EventBusSubscriber(
        modid = ToyotaYarisMod.MOD_ID,
        value = Dist.CLIENT
)

public class EntityScaleHandler {
    public static final double RADIUS = 24.0;
    public static double SQUASH_STRENGTH = 0.4F;

    private static final ResourceLocation SONG_ID =
            ResourceLocation.fromNamespaceAndPath(ToyotaYarisMod.MOD_ID, "why_this_dealer");

    public static List<PlayingSong> getSongs() {
        return List.copyOf(songs);
    }

    public record PlayingSong(
            SoundInstance sound,
            BlockPos jukeboxPos,
            long startTick
    ) {
        double getSeconds(float partialTick) {
            Minecraft mc = Minecraft.getInstance();

            if (mc.level == null) return 0.0;

            return (
                    mc.level.getGameTime()
                    - startTick
                    + partialTick
                    ) / 20.0;
        }
    }

    private static final List<PlayingSong> songs = new ArrayList<>();
    private static final Set<Integer> scaledEntities = new HashSet<>();

    @SubscribeEvent
    public static void onSoundStart(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();

        if (sound == null) return;

        if (sound.getSource() != SoundSource.RECORDS) return;
        if (!sound.getLocation().equals(SONG_ID)) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        BlockPos pos = BlockPos.containing(
                sound.getX(), sound.getY(), sound.getZ()
        );

        if (!mc.level.getBlockState(pos).is(Blocks.JUKEBOX)) {
            return;
        }

        songs.removeIf(song -> song.jukeboxPos().equals(pos));

        songs.add(new PlayingSong(
                sound, pos, mc.level.getGameTime()
        ));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null) {
            songs.clear();
            scaledEntities.clear();
            return;
        }

        long now = System.nanoTime();

        songs.removeIf(song -> {
            if (now - song.startTick() < 500_000_000L) {
                return false;
            }

            return !mc.getSoundManager().isActive(song.sound())
                    || !mc.level.getBlockState(song.jukeboxPos())
                    .is(Blocks.JUKEBOX);
        });
    }

    public static PlayingSong getNearbySong(double x, double y, double z) {
        PlayingSong closest = null;
        double closestDistance = RADIUS * RADIUS;

        for (PlayingSong song : songs) {
            double dx = x - (song.jukeboxPos.getX() + 0.5);
            double dy = y - (song.jukeboxPos.getY() + 0.5);
            double dz = z - (song.jukeboxPos.getZ() + 0.5);

            double distance = dx * dx + dy * dy + dz * dz;

            if (distance <= closestDistance) {
                closestDistance = distance;
                closest = song;
            }
        }

        return closest;
    }

    public static double yarisWave(double time) {
        double frequency = 135.0 / 60.0;
        double sharpness = 1.8;


        double sine = Math.sin(
                2.0 * Math.PI * frequency * time
        );

        return Math.tanh(sharpness * sine)
                / Math.tanh(sharpness);
    }

    @SubscribeEvent
    public static void onRenderPre(RenderLivingEvent.Pre<?, ?> event) {
        PlayingSong song = getNearbySong(
                event.getEntity().getX(),
                event.getEntity().getY(),
                event.getEntity().getZ()
        );

        if (song == null) return;

        double time = song.getSeconds(event.getPartialTick());

        float wave = (float) yarisWave(time);

        float scaleY = 1.0F + wave * 0.5F;
        float scaleXZ = 1.0F - wave * (float)SQUASH_STRENGTH;

        float bounce = 0.15F * (1.0F + wave);

        event.getPoseStack().pushPose();

        event.getPoseStack().translate(0.0, bounce, 0.0);
        event.getPoseStack().scale(scaleXZ, scaleY, scaleXZ);

        scaledEntities.add(event.getEntity().getId());
    }

    @SubscribeEvent
    public static void onRenderPost(RenderLivingEvent.Post<?, ?> event) {
        if (scaledEntities.remove(event.getEntity().getId())) {
            event.getPoseStack().popPose();
        }
    }
}
