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

import org.terasology.engine.Time;
import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.minion.move.MinionMoveComponent;
import org.terasology.registry.In;


@BehaviorAction(name = "BeginBreeding")
public class BeginBreedingAction extends BaseAction {

    @Override
    public BehaviorState modify(Actor actor, BehaviorState state) {
        CharacterMovementComponent characterMovementComponent = actor.getComponent(CharacterMovementComponent.class);
        characterMovementComponent.speedMultiplier = 0f;

        actor.save(characterMovementComponent);
        return BehaviorState.SUCCESS;
    }

}
