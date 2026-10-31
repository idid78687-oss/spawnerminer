package com.ollie.spawnerminer;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
public class SpawnerMinerDetector {
    private static final ScheduledExecutorService EXEC = Executors.newSingleThreadScheduledExecutor(r -> { Thread t = new Thread(r,"spawner-worker"); t.setDaemon(true); return t; });
    private static ScheduledFuture<?> task = null;
    private static final AtomicBoolean running    = new AtomicBoolean(false);
    private static final AtomicBoolean mineActive = new AtomicBoolean(false);
    private static final Random RNG = new Random();
    public static void tick(MinecraftClient c) {
        if (!SpawnerMinerConfig.enabled) { cancelAll(); return; }
        if (c.player == null || c.world == null) return;
        boolean near = !SpawnerMinerConfig.requirePlayer || detectPlayer(c);
        if (near && !running.get()) startScan(c);
        else if (!near && running.get()) cancelAll();
    }
    private static boolean detectPlayer(MinecraftClient c) {
        Vec3d self = c.player.getPos(); double rsq = Math.pow(SpawnerMinerConfig.playerRadius, 2);
        for (AbstractClientPlayerEntity p : c.world.getPlayers()) { if (p == c.player) continue; if (p.getPos().squaredDistanceTo(self) <= rsq) return true; } return false;
    }
    private static List<BlockPos> findSpawners(MinecraftClient c) {
        List<BlockPos> out = new ArrayList<>(); BlockPos center = c.player.getBlockPos(); int r = SpawnerMinerConfig.spawnerRadius;
        for (int x=-r;x<=r;x++) for (int y=-r;y<=r;y++) for (int z=-r;z<=r;z++) { BlockPos p=center.add(x,y,z); if(center.getSquaredDistance(p)>(double)(r*r))continue; if(c.world.getBlockState(p).isOf(Blocks.SPAWNER))out.add(p.toImmutable()); } return out;
    }
    private static void startScan(MinecraftClient c) {
        running.set(true);
        task = EXEC.scheduleWithFixedDelay(() -> {
            if (c.player==null||c.world==null){cancelAll();return;}
            if (SpawnerMinerConfig.requirePlayer&&!detectPlayer(c)){cancelAll();return;}
            if (mineActive.get()) return;
            List<BlockPos> spawners = findSpawners(c);
            if (spawners.isEmpty()) return;
            mineActive.set(true);
            try { mineAll(c, spawners); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } finally { mineActive.set(false); }
        }, 0, 800, TimeUnit.MILLISECONDS);
    }
    private static void mineAll(MinecraftClient c, List<BlockPos> spawners) throws InterruptedException {
        int mined = 0;
        for (BlockPos sp : spawners) {
            if (!running.get()) break;
            List<BlockPos> path = SpawnerMinerPath.findPath(c, sp);
            if (!path.isEmpty()) SpawnerMinerPath.walkAndClear(c, path);
            if (c.world.getBlockState(sp).isOf(Blocks.SPAWNER)) {
                SpawnerMinerPath.breakBlock(c, sp, c.world.getBlockState(sp));
                mined++;
            }
            Thread.sleep(300 + RNG.nextInt(300));
        }
        if (mined > 0) { PostMineSequence.run(c, mined); cancelAll(); }
    }
    public static void cancelAll() { if(task!=null){task.cancel(true);task=null;} running.set(false); mineActive.set(false); }
}
