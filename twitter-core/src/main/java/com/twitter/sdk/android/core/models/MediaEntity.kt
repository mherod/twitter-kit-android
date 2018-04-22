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

/**
 * Represents media elements uploaded with the Tweet.
 */
class MediaEntity(
        url: String,
        expandedUrl: String?,
        displayUrl: String,
        start: Int,
        end: Int,
        /**
         * ID of the media expressed as a 64-bit integer.
         */
        @field:SerializedName("id") val id: Long,
        /**
         * ID of the media expressed as a string.
         */
        @field:SerializedName("id_str") val idStr: String?,
        /**
         * A http:// URL pointing directly to the uploaded media file.
         *
         * For media in direct messages, media_url is the same https URL as media_url_https and must be
         * accessed via an authenticated twitter.com session or by signing a request with the user's
         * access token using OAuth 1.0A. It is not possible to directly embed these images in a web
         * page.
         */
        @field:SerializedName("media_url") val mediaUrl: String?,
        /**
         * A https:// URL pointing directly to the uploaded media file, for embedding on https pages.
         *
         * For media in direct messages, media_url_https must be accessed via an authenticated
         * twitter.com session or by signing a request with the user's access token using OAuth 1.0A.
         * It is not possible to directly embed these images in a web page.
         */
        @field:SerializedName("media_url_https") val mediaUrlHttps: String?,
        /**
         * An object showing available sizes for the media file.
         */
        @field:SerializedName("sizes") val sizes: Sizes?,
        /**
         * For Tweets containing media that was originally associated with a different tweet, this ID
         * points to the original Tweet.
         */
        @field:SerializedName("source_status_id") val sourceStatusId: Long,
        /**
         * For Tweets containing media that was originally associated with a different tweet, this
         * string-based ID points to the original Tweet.
         */
        @field:SerializedName("source_status_id_str") val sourceStatusIdStr: String?,
        /**
         * Type of uploaded media.
         */
        @field:SerializedName("type") val type: String,
        /**
         * An object showing details for the video file. This field is present only when there is a
         * video in the payload.
         */
        @field:SerializedName("video_info") val videoInfo: VideoInfo?,
        @field:SerializedName("ext_alt_text") val altText: String
) : UrlEntity(url, expandedUrl, displayUrl, start, end) {

    class Sizes(
            /**
             * Information for a thumbnail-sized version of the media.
             */
            @field:SerializedName("thumb")
            val thumb: Size,
            /**
             * Information for a small-sized version of the media.
             */
            @field:SerializedName("small")
            val small: Size,
            /**
             * Information for a medium-sized version of the media.
             */
            @field:SerializedName("medium")
            val medium: Size,
            /**
             * Information for a large-sized version of the media.
             */
            @field:SerializedName("large")
            val large: Size) : Serializable

    class Size(
            /**
             * Width in pixels of this size.
             */
            @field:SerializedName("w")
            val w: Int,
            /**
             * Height in pixels of this size.
             */
            @field:SerializedName("h")
            val h: Int,
            /**
             * Resizing method used to obtain this size. A value of fit means that the media was resized
             * to fit one dimension, keeping its native aspect ratio. A value of crop means that the
             * media was cropped in order to fit a specific resolution.
             */
            @field:SerializedName("resize")
            val resize: String) : Serializable
}
