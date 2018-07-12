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
package org.terasology.gookeeper.component;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;

import java.util.ArrayList;
import java.util.List;

public class VisitorComponent implements Component {
    /**
     * The list of visit block entities to be visited
     */
    public List<EntityRef> pensToVisit = new ArrayList<>();

    /**
     * The associated visitor entrance block from where the NPC got spawned
     */
    public EntityRef visitorEntranceBlock = EntityRef.NULL;
}