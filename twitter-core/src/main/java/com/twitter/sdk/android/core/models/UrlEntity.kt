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

/**
 * Represents URLs included in the text of a Tweet or within textual fields of a user object.
 */
open class UrlEntity(
        /**
         * Wrapped URL, corresponding to the value embedded directly into the raw Tweet text, and the
         * values for the indices parameter.
         */
        @field:SerializedName("url") val url: String,
        /**
         * Expanded version of display_url
         */
        @field:SerializedName("expanded_url") val expandedUrl: String?,
        /**
         * Version of the URL to display to clients.
         */
        @field:SerializedName("display_url") val displayUrl: String,
        start: Int,
        end: Int
) : Entity(start, end)
