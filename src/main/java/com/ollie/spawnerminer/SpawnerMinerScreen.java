package com.ollie.spawnerminer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
public class SpawnerMinerScreen extends Screen {
    private int localPlayerRadius  = SpawnerMinerConfig.playerRadius;
    private int localSpawnerRadius = SpawnerMinerConfig.spawnerRadius;
    private int localBreakDelay    = SpawnerMinerConfig.breakDelayMs;
    public SpawnerMinerScreen() { super(Text.literal("Spawner Miner")); }
    @Override
    protected void init() {
        int cx = this.width / 2, cy = this.height / 2;
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Enabled: " + (SpawnerMinerConfig.enabled ? "§aON" : "§cOFF")),
            btn -> {
                SpawnerMinerConfig.enabled = !SpawnerMinerConfig.enabled;
                btn.setMessage(Text.literal("Enabled: " + (SpawnerMinerConfig.enabled ? "§aON" : "§cOFF")));
                if (!SpawnerMinerConfig.enabled) SpawnerMinerDetector.cancelAll();
            }).dimensions(cx - 100, cy - 110, 200, 20).build());
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Require Player: " + (SpawnerMinerConfig.requirePlayer ? "§aON" : "§cOFF")),
            btn -> {
                SpawnerMinerConfig.requirePlayer = !SpawnerMinerConfig.requirePlayer;
                btn.setMessage(Text.literal("Require Player: " + (SpawnerMinerConfig.requirePlayer ? "§aON" : "§cOFF")));
            }).dimensions(cx - 100, cy - 82, 200, 20).build());
        this.addDrawableChild(new SliderWidget(cx - 100, cy - 54, 200, 20,
            Text.literal("Player Radius: " + localPlayerRadius),
            (localPlayerRadius - 8.0) / (128.0 - 8.0)) {
            @Override protected void updateMessage() { localPlayerRadius = (int)(8 + value * 120); setMessage(Text.literal("Player Radius: " + localPlayerRadius)); }
            @Override protected void applyValue()    { localPlayerRadius = (int)(8 + value * 120); }
        });
        this.addDrawableChild(new SliderWidget(cx - 100, cy - 26, 200, 20,
            Text.literal("Spawner Radius: " + localSpawnerRadius),
            (localSpawnerRadius - 4.0) / (64.0 - 4.0)) {
            @Override protected void updateMessage() { localSpawnerRadius = (int)(4 + value * 60); setMessage(Text.literal("Spawner Radius: " + localSpawnerRadius)); }
            @Override protected void applyValue()    { localSpawnerRadius = (int)(4 + value * 60); }
        });
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Apply & Close"), btn -> this.close())
            .dimensions(cx - 100, cy + 2, 200, 20).build());
    }
    @Override public void close() {
        SpawnerMinerConfig.playerRadius  = localPlayerRadius;
        SpawnerMinerConfig.spawnerRadius = localSpawnerRadius;
        SpawnerMinerConfig.breakDelayMs  = localBreakDelay;
        super.close();
    }
    @Override public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§6Spawner Miner"), this.width / 2, this.height / 2 - 130, 0xFFFFFF);
        super.render(ctx, mouseX, mouseY, delta);
    }
    @Override public boolean shouldPause() { return false; }
}
