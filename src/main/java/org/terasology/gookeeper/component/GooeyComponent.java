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
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.world.biomes.Biome;
import org.terasology.world.block.ForceBlockActive;

import java.util.Optional;

@ForceBlockActive
public class GooeyComponent implements Component {
    /* The prefab corresponding to this gooey type. */
    public Optional<Prefab> prefab;
    /* The profit factor. (i.e how much money does the player make from the visitors viewing the gooey) */
    public float profitPayOff;
    /* The biome this type of gooey are to be found in. */
    public String biome;
    /* The percentage chance of spawning this type of gooey. (i.e rarity factor) */
    public float SPAWN_CHANCE;
    /* The maximum number of gooeys in a group. */
    public int MAX_GROUP_SIZE;
}
