/*
 * Copyright (c) 2021 TeamMoeg
 *
 * This file is part of Frosted Heart.
 *
 * Frosted Heart is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Frosted Heart is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frosted Heart. If not, see <https://www.gnu.org/licenses/>.
 */

package com.teammoeg.frostedheart.climate.chunkdata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.teammoeg.frostedheart.network.PacketHandler;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.fml.network.PacketDistributor;

/**
 * Cache of chunk data
 * Used for various purposes:
 * {@link ChunkDataCache#CLIENT} and {@link ChunkDataCache#SERVER} are logical sided caches, used for when chunk data is needed without a world context. Care must be taken to choose the cache for the correct logical side
 * {@link ChunkDataCache#WORLD_GEN} is used for chunk data during world generation, as it's being generated. It is cleared once the chunk is completely generated
 */
public final class ChunkDataCache {
    /**
     * This is a cache of client side chunk data, used for when there is no world context available.
     * It is synced on chunk watch / unwatch
     */
    public static final ChunkDataCache CLIENT = new ChunkDataCache("client");

    /**
     * This is a cache of server side chunk data.
     * It is not synced, it is updated on chunk load / unload
     */
    public static final ChunkDataCache SERVER = new ChunkDataCache("server");

    /**
     * This is a cache of incomplete chunk data used by world generation
     * It is generated in stages:
     * - {@link ChunkData.Status#CLIMATE} during biome generation to generate climate variants
     * - {@link ChunkData.Status#ROCKS} during surface generation, later used for feature generation
     * When the chunk is finished generating on server, this cache is cleared and the data is saved to the chunk capability for long term storage
     */
    public static final ChunkDataCache WORLD_GEN = new ChunkDataCache("worldgen");

    /**
     * This is a set of chunk positions which have been queued for chunk watch, but were not loaded or generated at the time.
     * As a result, no data was able to be sent to the client cache. In these situations, we wait for chunk load on server, and if the chunk is present here, it is re-synchronized.
     */
    public static final WatchQueue WATCH_QUEUE = new WatchQueue();

    /**
     * Gets the normal (not world gen) cache of chunk data for the current logical side
     */
    public static ChunkDataCache get(IWorldReader world) {
        return world.isRemote() ? CLIENT : SERVER;
    }

    /**
     * Gets the normal (not world en) cache of chunk data based on a heuristic - pick the one that is not empty
     * On dedicated servers / clients only one will be non-empty
     * On logical server / clients, this will default to using the server cache
     * DO NOT call this unless absolutely necessary, e.g. returning from a vanilla method which is called from both logical sides
     */
    public static ChunkDataCache getUnsided() {
        return SERVER.cache.isEmpty() ? CLIENT : SERVER;
    }

    public static void clearAll() {
        CLIENT.cache.clear();
        SERVER.cache.clear();
        WORLD_GEN.cache.clear();
        WATCH_QUEUE.queue.clear();
    }

    private final String name;
    private Map<ChunkPos, ChunkData> cache;

    /**
     * Creates an infinite size cache that must be managed to not create memory leaks
     */
    private ChunkDataCache(String name) {
        this.name = name;
        this.cache = new HashMap<>();
    }

    public void putChunkDataToCache(ChunkPos chunkPos, ChunkData chunkData) {
        if (this.cache.containsKey(chunkPos)) {
            this.cache.replace(chunkPos, chunkData);
        } else {
            this.cache.put(chunkPos, chunkData);
        }
    }

    public void setCache(Map<ChunkPos, ChunkData> cache) {
        this.cache = cache;
    }

    public Map<ChunkPos, ChunkData> getCache() {
        return cache;
    }

    public ChunkData getOrEmpty(BlockPos pos) {
        return getOrEmpty(new ChunkPos(pos));
    }

    public ChunkData getOrEmpty(ChunkPos pos) {
        return cache.getOrDefault(pos, ChunkData.EMPTY);
    }

    @Nullable
    public ChunkData get(BlockPos pos) {
        return get(new ChunkPos(pos));
    }

    @Nullable
    public ChunkData get(ChunkPos pos) {
        return cache.get(pos);
    }

    @Nullable
    public ChunkData remove(ChunkPos pos) {
        return cache.remove(pos);
    }

    public void update(ChunkPos pos, ChunkData data) {
        cache.put(pos, data);
    }

    public ChunkData getOrCreate(ChunkPos pos) {
        return cache.computeIfAbsent(pos, ChunkData::new);
    }

    @Override
    public String toString() {
        return "ChunkDataCache[" + name + ']';
    }

    public static class WatchQueue {
        private final Map<ChunkPos, Set<ServerPlayerEntity>> queue;

        private WatchQueue() {
            queue = new HashMap<>(256);
        }

        public void enqueueUnloadedChunk(ChunkPos pos, ServerPlayerEntity player) {
            queue.computeIfAbsent(pos, key -> new HashSet<>()).add(player);
        }

        public void dequeueChunk(ChunkPos pos, ServerPlayerEntity player) {
            Set<ServerPlayerEntity> players = queue.get(pos);
            if (players != null) {
                players.remove(player);
                if (players.isEmpty()) {
                    queue.remove(pos);
                }
            }
        }

        public void dequeueLoadedChunk(ChunkPos pos, ChunkData data) {
            if (queue.containsKey(pos)) {
                Set<ServerPlayerEntity> players = queue.remove(pos);
                for (ServerPlayerEntity player : players) {
                    PacketHandler.send(PacketDistributor.PLAYER.with(() -> player), data.getUpdatePacket());
                }
            }
        }
    }
}