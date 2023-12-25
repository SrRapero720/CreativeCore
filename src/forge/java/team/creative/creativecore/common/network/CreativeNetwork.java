package team.creative.creativecore.common.network;

import java.util.HashMap;
import java.util.function.Supplier;

import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.simple.SimpleChannel;
import team.creative.creativecore.common.level.ISubLevel;

public class CreativeNetwork {
    
    @OnlyIn(value = Dist.CLIENT)
    private static Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }
    
    private final HashMap<Class<? extends CreativePacket>, CreativeNetworkPacket> packetTypes = new HashMap<>();
    private final Logger logger;
    private String version;
    
    private int id = 0;
    
    public final SimpleChannel instance;
    
    public CreativeNetwork(int version, Logger logger, ResourceLocation location) {
        this.logger = logger;
        this.version = "" + version;
        this.instance = NetworkRegistry.newSimpleChannel(location, () -> this.version, x -> true, this.version::equals);
        this.logger.debug("Created network " + location + "");
    }
    
    public <T extends CreativePacket> void registerType(Class<T> classType, Supplier<T> supplier) {
        CreativeNetworkPacket<T> handler = new CreativeNetworkPacket<>(classType, supplier);
        this.instance.messageBuilder(classType, id, null).encoder((message, buffer) -> handler.write(message, buffer)).decoder(buffer -> handler.read(buffer)).consumerMainThread(
            (message, ctx) -> {
                try {
                    message.execute(ctx.getSender() == null ? getClientPlayer() : ctx.getSender());
                } catch (Throwable e) {
                    e.printStackTrace();
                    throw e;
                }
            }).add();
        packetTypes.put(classType, handler);
        id++;
    }
    
    public CreativeNetworkPacket getPacketType(Class<? extends CreativePacket> clazz) {
        return packetTypes.get(clazz);
    }
    
    public void sendToServer(CreativePacket message) {
        this.instance.sendToServer(message);
    }
    
    public void sendToClient(CreativePacket message, ServerPlayer player) {
        this.instance.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
    
    public void sendToClient(CreativePacket message, Level level, BlockPos pos) {
        if (level instanceof ISubLevel)
            sendToClientTracking(message, ((ISubLevel) level).getHolder());
        else
            
            sendToClient(message, level.getChunkAt(pos));
    }
    
    public void sendToClient(CreativePacket message, LevelChunk chunk) {
        this.instance.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), message);
    }
    
    public void sendToClientTracking(CreativePacket message, Entity entity) {
        if (entity.level() instanceof ISubLevel sub)
            sendToClientTracking(message, sub.getHolder());
        else
            this.instance.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
    }
    
    public void sendToClientTrackingAndSelf(CreativePacket message, Entity entity) {
        if (entity.level() instanceof ISubLevel sub)
            sendToClientTrackingAndSelf(message, sub.getHolder());
        else
            this.instance.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), message);
    }
    
    public void sendToClientAll(MinecraftServer server, CreativePacket message) {
        this.instance.send(PacketDistributor.ALL.noArg(), message);
    }
}
