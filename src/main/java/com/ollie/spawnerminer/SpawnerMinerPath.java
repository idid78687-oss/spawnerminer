package com.ollie.spawnerminer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import java.util.*;
public class SpawnerMinerPath {
    private static final Random RNG = new Random();
    public static List<BlockPos> findPath(MinecraftClient client, BlockPos target) {
        BlockPos start = client.player.getBlockPos();
        ClientWorld world = client.world;
        List<BlockPos> adjacents = new ArrayList<>();
        for (Direction d : Direction.values()) {
            if (d == Direction.UP) continue;
            BlockPos adj = target.offset(d);
            if (isWalkable(world, adj)) adjacents.add(adj);
        }
        adjacents.add(target.down());
        Map<BlockPos, BlockPos> cameFrom = new HashMap<>();
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(start); visited.add(start); cameFrom.put(start, null);
        BlockPos reached = null; int nodes = 0;
        outer: while (!queue.isEmpty() && nodes++ < 2048) {
            BlockPos cur = queue.poll();
            for (BlockPos adj : adjacents) { if (cur.equals(adj)) { reached = cur; break outer; } }
            for (Direction d : new Direction[]{ Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN }) {
                BlockPos next = cur.offset(d);
                if (visited.contains(next)) continue;
                visited.add(next);
                BlockState bs = world.getBlockState(next);
                if (bs.isAir() || bs.getHardness(world, next) >= 0) {
                    cameFrom.put(next, cur);
                    if (adjacents.contains(next)) { reached = next; break outer; }
                    queue.add(next);
                }
            }
        }
        if (reached == null) return Collections.emptyList();
        List<BlockPos> path = new ArrayList<>();
        BlockPos cur = reached;
        while (cur != null) { path.add(0, cur); cur = cameFrom.get(cur); }
        return path;
    }
    public static void walkAndClear(MinecraftClient client, List<BlockPos> path) throws InterruptedException {
        for (int i = 1; i < path.size(); i++) {
            BlockPos pos = path.get(i);
            BlockState state = client.world.getBlockState(pos);
            if (!state.isAir()) breakBlock(client, pos, state);
            Vec3d dest = Vec3d.ofCenter(pos).subtract(0, 0.5, 0);
            client.execute(() -> {
                client.player.setPosition(dest.x, dest.y, dest.z);
                client.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(dest.x, dest.y, dest.z, true));
            });
            Thread.sleep(180 + RNG.nextInt(80));
        }
    }
    public static void breakBlock(MinecraftClient client, BlockPos pos, BlockState state) throws InterruptedException {
        float hardness = state.getHardness(client.world, pos);
        if (hardness < 0) return;
        int breakMs = Math.max(200, (int)(hardness * 210)) + RNG.nextInt(60);
        float[] rot = getRotation(client.player.getEyePos(), pos);
        client.execute(() -> {
            sendRotation(client, rot[0], rot[1]);
            client.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP));
            client.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        });
        Thread.sleep(breakMs);
        client.execute(() -> client.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP)));
        Thread.sleep(100 + RNG.nextInt(80));
    }
    static boolean isWalkable(ClientWorld world, BlockPos pos) {
        return world.getBlockState(pos).isAir() && world.getBlockState(pos.up()).isAir();
    }
    static float[] getRotation(Vec3d eyes, BlockPos target) {
        Vec3d center = Vec3d.ofCenter(target);
        double dx = center.x - eyes.x, dy = center.y - eyes.y, dz = center.z - eyes.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        return new float[]{ (float)(Math.toDegrees(Math.atan2(dz, dx))) - 90f, (float)(-Math.toDegrees(Math.atan2(dy, dist))) };
    }
    static void sendRotation(MinecraftClient client, float yaw, float pitch) {
        client.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, client.player.isOnGround()));
        client.player.setYaw(yaw); client.player.setPitch(pitch);
    }
}
