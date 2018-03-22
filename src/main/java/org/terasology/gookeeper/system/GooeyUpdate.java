/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.gookeeper.system;

import com.google.common.collect.Lists;

import org.terasology.core.world.CoreBiome;
import org.terasology.gookeeper.component.GooeyComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.logic.health.OnDamagedEvent;
import org.terasology.registry.In;
import org.terasology.utilities.Assets;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.biomes.Biome;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.event.OnChunkGenerated;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RegisterSystem(RegisterMode.AUTHORITY)
public class GooeyUpdate extends BaseComponentSystem implements UpdateSubscriberSystem {
    @In
    private WorldProvider worldProvider;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private EntityManager entityManager;

    @In
    private BlockManager blockManager;

    @In
    private DelayManager delayManager;

    private Random random = new FastRandom();
    private List<Optional<Prefab>> gooeyPrefabs = new ArrayList();

    private Block airBlock;

    @Override
    public void initialise() {
        airBlock = blockManager.getBlock(BlockManager.AIR_ID);

        gooeyPrefabs.add(Assets.getPrefab("GooKeeper:redgooey"));
        gooeyPrefabs.add(Assets.getPrefab("GooKeeper:bluegooey"));
        gooeyPrefabs.add(Assets.getPrefab("GooKeeper:yellowgooey"));
    }

    @Override
    public void update (float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(GooeyComponent.class)) {
            GooeyComponent gooeyComponent = entity.getComponent(GooeyComponent.class);
            // All the updates regarding gooey entities goes here.
        }
    }
    /**
     * When a new chunk gets loaded, it checks which biome it falls under and tries to call a spawning method.
     *
     */
    @ReceiveEvent
    public void onChunkGenerated(OnChunkGenerated event, EntityRef worldEntity) {
        for (Optional<Prefab> gooey : gooeyPrefabs) {
            boolean trySpawn = gooey.get().getComponent(GooeyComponent.class).SPAWN_CHANCE > random.nextInt(100);
            if (!trySpawn) {
                return;
            }
            Vector3i chunkPos = event.getChunkPos();
            tryGooeySpawn(gooey, chunkPos);
        }
    }

    /**
     * Attempts to spawn gooey on the specified chunk. The number of gooeys spawned will depend on probability
     * configurations defined earlier.
     *
     * @param gooey,chunkPos   The prefab to be spawned, and the chunk which the game will try to spawn gooeys on
     */
    private void tryGooeySpawn(Optional<Prefab> gooey, Vector3i chunkPos) {
        GooeyComponent gooeyComponent = gooey.get().getComponent(GooeyComponent.class);
        List<Vector3i> foundPositions = findGooeySpawnPositions(gooeyComponent, chunkPos);

        if (foundPositions.size() < 1) {
            return;
        }

        int maxGooeyCount = foundPositions.size();
        if (maxGooeyCount > gooeyComponent.MAX_GROUP_SIZE) {
            maxGooeyCount = gooeyComponent.MAX_GROUP_SIZE;
        }
        int gooeyCount = random.nextInt(maxGooeyCount - 1) + 1;

        for (int i = 0; i < gooeyCount; i++ ) {
            int randomIndex = random.nextInt(foundPositions.size());
            Vector3i randomSpawnPosition = foundPositions.remove(randomIndex);
            spawnGooey(gooey, randomSpawnPosition);
        }
    }

    private List<Vector3i> findGooeySpawnPositions(GooeyComponent gooeyComponent, Vector3i chunkPos) {
        Vector3i worldPos = new Vector3i(chunkPos);
        worldPos.mul(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z);
        List<Vector3i> foundPositions = Lists.newArrayList();
        Vector3i blockPos = new Vector3i();
        for (int y = ChunkConstants.SIZE_Y - 1; y >= 0; y--) {
            for (int z = 0; z < ChunkConstants.SIZE_Z; z++) {
                for (int x = 0; x < ChunkConstants.SIZE_X; x++) {
                    blockPos.set(x + worldPos.x, y + worldPos.y, z + worldPos.z);
                    if (isValidSpawnPosition(gooeyComponent, blockPos)) {
                        foundPositions.add(new Vector3i(blockPos));
                    }
                }
            }
        }
        return foundPositions;
    }
    /**
     * Spawns the gooey at the location specified by the parameter.
     *
     * @param location   The location where the gooey is to be spawned
     */
    private void spawnGooey(Optional<Prefab> gooey, Vector3i location) {
        Vector3f floatVectorLocation = location.toVector3f();
        Vector3f yAxis = new Vector3f(0, 1, 0);
        float randomAngle = (float) (random.nextFloat()*Math.PI*2);
        Quat4f rotation = new Quat4f(yAxis, randomAngle);
        if (gooey.isPresent() && gooey.get().getComponent(LocationComponent.class) != null) {
            entityManager.create(gooey.get(), floatVectorLocation, rotation);
        }
    }

    /**
     * Check blocks at and around the target position and check if it's a valid spawning spot
     *
     * @param pos   The block to be checked if it's a valid spot for spawning
     * @return A boolean with the value of true if the block is a valid spot for spawing
     */
    private boolean isValidSpawnPosition(GooeyComponent gooeyComponent, Vector3i pos) {
        Vector3i below = new Vector3i(pos.x, pos.y - 1, pos.z);
        Block blockBelow = worldProvider.getBlock(below);
        if (!blockBelow.equals(getBlockFromString(gooeyComponent.blockBelow))) {
            return false;
        }
        Block blockAtPosition = worldProvider.getBlock(pos);
        if (!blockAtPosition.isPenetrable()) {
            return false;
        }

        Vector3i above = new Vector3i(pos.x, pos.y + 1, pos.z);
        Block blockAbove = worldProvider.getBlock(above);
        if (!blockAbove.equals(airBlock)) {
            return false;
        }
        if (worldProvider.getBiome(pos).equals(getBiomeFromString(gooeyComponent.biome)))
            return true;
        else
            return false;
    }

    private Biome getBiomeFromString (String biomeName) {
        if (biomeName.equals("DESERT")) {
            return CoreBiome.DESERT;
        } else if (biomeName.equals("FOREST")) {
            return CoreBiome.FOREST;
        } else {
            return CoreBiome.PLAINS;
        }
    }

    private Block getBlockFromString (String blockName) {
        if (blockName.equals("SAND")) {
            return blockManager.getBlock("core:Sand");
        } else {
            return blockManager.getBlock("core:Grass");
        }
    }
    @ReceiveEvent
    public void onDamage(OnDamagedEvent event, EntityRef entity) {
        return;
    }
}
