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

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

/**
 * Provides metadata and additional contextual information about content posted on Twitter
 */
open class Entity(start: Int, end: Int) : Serializable {

    /**
     * An array of integers indicating the offsets.
     */
    @SerializedName("indices")
    val indices: List<Int>

    val start: Int
        get() = indices[START_INDEX]

    val end: Int
        get() = indices[END_INDEX]

    init {
        val temp = ArrayList<Int>(2)
        temp.add(START_INDEX, start)
        temp.add(END_INDEX, end)

        indices = Collections.unmodifiableList(temp)
    }

    companion object {
        private const val START_INDEX = 0
        private const val END_INDEX = 1
    }
}
