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
package org.terasology.gookeeper.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.behaviors.components.NPCMovementComponent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.gookeeper.component.GooeyComponent;
import org.terasology.gookeeper.component.SlimePodComponent;
import org.terasology.gookeeper.event.OnCapturedEvent;
import org.terasology.logic.behavior.BehaviorComponent;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.events.OnEnterBlockEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3f;
import org.terasology.minion.move.MinionMoveComponent;
import org.terasology.registry.In;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

@RegisterSystem(RegisterMode.AUTHORITY)
public class SlimePodAction extends BaseComponentSystem implements UpdateSubscriberSystem {

    @In
    private WorldProvider worldProvider;

    @In
    private LocalPlayer localPlayer;

    private static final Logger logger = LoggerFactory.getLogger(SlimePodAction.class);

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void update(float delta) {
    }

    @ReceiveEvent
    public void onActivate(ActivateEvent event, EntityRef entity) {
        SlimePodComponent slimePodComponent = entity.getComponent(SlimePodComponent.class);
        slimePodComponent.isActivated = !slimePodComponent.isActivated;

        logger.info("Khali hai kya: " + (slimePodComponent.capturedEntity == EntityRef.NULL));
        // TODO: Add implementation to release captured gooeys by reattaching removed comps.
        if (slimePodComponent.capturedEntity != EntityRef.NULL) {
            EntityRef releasedGooey = slimePodComponent.capturedEntity;
            for (int i = 0; i < slimePodComponent.disabledComponents.size(); i++) {
                releasedGooey.addOrSaveComponent(slimePodComponent.disabledComponents.get(i));
            }
            LocationComponent locationComponent = releasedGooey.getComponent(LocationComponent.class);
            LocationComponent blockPos = entity.getComponent(LocationComponent.class);
            locationComponent.setWorldPosition(new Vector3f(blockPos.getWorldPosition().x, blockPos.getWorldPosition().y + 1, blockPos.getWorldPosition().z));
        }
    }

    @ReceiveEvent
    public void onEnterBlock(OnEnterBlockEvent event, EntityRef entity) {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        Vector3f pos = loc.getWorldPosition();
        pos.setY(pos.getY() -1);
        Block block = worldProvider.getBlock(pos);

        if (block.getEntity().hasComponent(SlimePodComponent.class) && entity.hasComponent(GooeyComponent.class)) {
            SlimePodComponent slimePodComponent = block.getEntity().getComponent(SlimePodComponent.class);
            if (slimePodComponent.isActivated && slimePodComponent.capturedEntity == EntityRef.NULL) {
                slimePodComponent.capturedEntity = entity;
                entity.send(new OnCapturedEvent(localPlayer.getCharacterEntity(), slimePodComponent));
                block.getEntity().saveComponent(slimePodComponent);
            }
        }
    }
}
