package com.ollie.spawnerminer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.text.Text;
import java.time.Instant;
public class PostMineSequence {
    public static void run(MinecraftClient client, int mined) {
        new Thread(() -> {
            try {
                client.execute(() -> client.player.networkHandler.sendPacket(
                    new net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket(
                        "rtp", Instant.now(), 0L, null,
                        new net.minecraft.network.message.LastSeenMessageList.Acknowledgment(0)
                    )
                ));
                Thread.sleep(1000);
                int inv = countSpawners(client);
                String msg = "PLAYER IN RADIUS SPAWNERS MINED AND IN YOUR INVENTORY [" + inv + " SPAWNERS]";
                client.execute(() -> client.getNetworkHandler().onDisconnected(
                    new DisconnectionInfo(Text.literal(msg), null, null)
                ));
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
