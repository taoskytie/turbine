/**
 * Copyright 2017 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.turbine.data.meta;

import com.netflix.turbine.data.TurbineData;

/**
 * Interface to represent the logic to translate {@link MetaInformation} <K> to data K which can be then sent to listeners downstream.
 * 
 * @author poberai
 * @param <K>
 */
public interface MetaInfoAdaptor<K extends TurbineData> {
    
    /**
     * Get streamable data K from {@link MetaInformation}
     * @param metaInfo
     * @return K
     */
    public K getData(MetaInformation<K> metaInfo);
}
