@file:JvmName("ModelUtils")

/*
 * Copyright (C) 2015 Twitter, Inc.
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
 *
 */

package com.twitter.sdk.android.core.models


import java.util.*

/**
 * Util class for twitter-core models
 */
object ModelUtils {

    @JvmStatic
    fun <T> getSafeList(entities: List<T>?): List<T> {
        return if (entities == null) {
            emptyList()
        } else {
            Collections.unmodifiableList(entities)
        }
    }

    @JvmStatic
    fun <K, V> getSafeMap(entities: Map<K, V>?): Map<K, V> {
        return if (entities == null) {
            emptyMap()
        } else {
            Collections.unmodifiableMap(entities)
        }
    }
}
