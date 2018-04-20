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
 * Current configuration used by Twitter
 */
class Configuration constructor(
        /**
         * Maximum number of characters per direct message
         */
        @SerializedName("dm_text_character_limit")
        val dmTextCharacterLimit: Int,

        /**
         * Maximum size in bytes for the media file.
         */
        @SerializedName("photo_size_limit")
        val photoSizeLimit: Long,
        /**
         * Maximum resolution for the media files.
         */
        @SerializedName("photo_sizes")
        val photoSizes: MediaEntity.Sizes?,
        /**
         * Current t.co URL length
         */
        @SerializedName("short_url_length_https")
        val shortUrlLengthHttps: Int,
        /**
         * Slugs which are not user names
         */
        @SerializedName("non_username_paths")
        val nonUsernamePaths: List<String>?)
