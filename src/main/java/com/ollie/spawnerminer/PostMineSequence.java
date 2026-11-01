package com.ollie.spawnerminer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;
public class PostMineSequence {
    public static void run(MinecraftClient client, int mined) {
        new Thread(() -> {
            try {
                client.execute(() -> {
                    ClientPlayNetworkHandler handler = client.player.networkHandler;
                    handler.sendChatCommand("rtp");
                });
                Thread.sleep(1000);
                int inv = countSpawners(client);
                String msg = "PLAYER IN RADIUS SPAWNERS MINED AND IN YOUR INVENTORY [" + inv + " SPAWNERS]";
                client.execute(() ->
                    client.player.networkHandler.getConnection().disconnect(Text.literal(msg))
                );
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "post-mine").start();
    }
    private static int countSpawners(MinecraftClient client) {
        if (client.player == null) return 0;
        int c = 0;
        for (int i = 0; i < client.player.getInventory().size(); i++) {
            var s = client.player.getInventory().getStack(i);
            if (s.getItem() == net.minecraft.item.Items.SPAWNER) c += s.getCount();
        }
        return c;
    }
}
