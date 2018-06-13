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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.audio.StaticSound;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.gookeeper.component.*;
import org.terasology.gookeeper.event.OnStunnedEvent;
import org.terasology.gookeeper.input.DecreaseFrequencyButton;
import org.terasology.gookeeper.input.IncreaseFrequencyButton;
import org.terasology.logic.characters.CharacterHeldItemComponent;
import org.terasology.logic.characters.GazeMountPointComponent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.health.DoDamageEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.ClientComponent;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.Physics;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.protobuf.EntityData;
import org.terasology.registry.In;
import org.terasology.utilities.Assets;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.items.OnBlockItemPlaced;

@RegisterSystem(RegisterMode.AUTHORITY)
public class VisitorSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    @In
    private WorldProvider worldProvider;

    @In
    private Physics physicsRenderer;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private EntityManager entityManager;

    @In
    private DelayManager delayManager;

    @In
    private InventoryManager inventoryManager;

    @In
    private Time time;

    @In
    private LocalPlayer localPlayer;

    private static final Logger logger = LoggerFactory.getLogger(VisitorSystem.class);
    private Random random = new FastRandom();

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void update(float delta) {
        for (EntityRef visitor : entityManager.getEntitiesWith(VisitorComponent.class)) {
            VisitorComponent visitorComponent = visitor.getComponent(VisitorComponent.class);

            if (visitorComponent.pensToVisit.isEmpty()) {
                int cutoffRNG = random.nextInt(0, 10);

                for (EntityRef visitBlock : entityManager.getEntitiesWith(VisitBlockComponent.class)) {
                    VisitBlockComponent visitBlockComponent = visitBlock.getComponent(VisitBlockComponent.class);
                    if (visitBlockComponent.cutoffFactor <= cutoffRNG) {
                        visitorComponent.pensToVisit.add(visitBlock);
                        visitor.saveComponent(visitorComponent);
                    }
                }
            }
        }
    }

    @ReceiveEvent(components = {VisitBlockComponent.class, BlockComponent.class})
    public void onBlockPlaced(OnBlockItemPlaced event, EntityRef entity) {
        BlockComponent blockComponent = event.getPlacedBlock().getComponent(BlockComponent.class);
        if (blockComponent == null) {
            return;
        }

        Vector3i targetBlock = blockComponent.getPosition();
        EntityRef pen = getClosestPen(new Vector3f(targetBlock.x, targetBlock.y, targetBlock.z));

        VisitBlockComponent visitBlockComponent = event.getPlacedBlock().getComponent(VisitBlockComponent.class);
        visitBlockComponent.type = pen.getComponent(PenBlockComponent.class).type;
        visitBlockComponent.cutoffFactor = pen.getComponent(PenBlockComponent.class).cutoffFactor;

        event.getPlacedBlock().saveComponent(visitBlockComponent);
    }

    private EntityRef getClosestPen (Vector3f location) {
        EntityRef closestPen = EntityRef.NULL;
        float minDistance = 10f;

        for (EntityRef pen : entityManager.getEntitiesWith(PenBlockComponent.class)) {
            BlockComponent blockComponent = pen.getComponent(BlockComponent.class);
            Vector3f blockPos = new Vector3f(blockComponent.getPosition().x, blockComponent.getPosition().y, blockComponent.getPosition().z);
            if (Vector3f.distance(blockPos, location) < minDistance) {
                minDistance = Vector3f.distance(blockPos, location);
                closestPen = pen;
            }
        }

        return closestPen;
    }
}
