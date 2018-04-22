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
 * Users can be anyone or anything. They tweet, follow, create lists, have a home_timeline, can be
 * mentioned, and can be looked up in bulk.
 */
class User(
        /**
         * Indicates that the user has an account with "contributor mode" enabled, allowing for Tweets
         * issued by the user to be co-authored by another account. Rarely true.
         */
        @field:SerializedName("contributors_enabled")
        val contributorsEnabled: Boolean,
        /**
         * The UTC datetime that the user account was created on Twitter.
         */
        @field:SerializedName("created_at")
        val createdAt: String,
        /**
         * When true, indicates that the user has not altered the theme or background of their user
         * profile.
         */
        @field:SerializedName("default_profile")
        val defaultProfile: Boolean,
        /**
         * When true, indicates that the user has not uploaded their own avatar and a default egg avatar
         * is used instead.
         */
        @field:SerializedName("default_profile_image")
        val defaultProfileImage: Boolean,
        /**
         * Nullable. The user-defined UTF-8 string describing their account.
         */
        @field:SerializedName("description")
        val description: String,
        /**
         * Nullable. The logged in user email address if available. Must have permission to access email
         * address.
         */
        @field:SerializedName("email")
        val email: String,
        /**
         * Entities which have been parsed out of the url or description fields defined by the user.
         * Read more about User Entities.
         */
        @field:SerializedName("entities")
        val entities: UserEntities,
        /**
         * The number of tweets this user has favorited in the account's lifetime. British spelling used
         * in the field name for historical reasons.
         */
        @field:SerializedName("favourites_count")
        val favouritesCount: Int,
        /**
         * Nullable. Perspectival. When true, indicates that the authenticating user has issued a follow
         * request to this protected user account.
         */
        @field:SerializedName("follow_request_sent")
        val followRequestSent: Boolean,
        /**
         * The number of followers this account currently has. Under certain conditions of duress, this
         * field will temporarily indicate "0."
         */
        @field:SerializedName("followers_count")
        val followersCount: Int,
        /**
         * The number of users this account is following (AKA their "followings"). Under certain
         * conditions of duress, this field will temporarily indicate "0."
         */
        @field:SerializedName("friends_count")
        val friendsCount: Int,
        /**
         * When true, indicates that the user has enabled the possibility of geotagging their Tweets.
         * This field must be true for the current user to attach geographic data when using
         * POST statuses / update.
         */
        @field:SerializedName("geo_enabled")
        val geoEnabled: Boolean,
        /**
         * The integer representation of the unique identifier for this User. This number is greater
         * than 53 bits and some programming languages may have difficulty/silent defects in
         * interpreting it. Using a signed 64 bit integer for storing this identifier is safe. Use
         * id_str for fetching the identifier to stay on the safe side. See Twitter IDs, JSON and
         * Snowflake.
         */
        @field:SerializedName("id")
        override val id: Long,
        /**
         * The string representation of the unique identifier for this User. Implementations should use
         * this rather than the large, possibly un-consumable integer in id
         */
        @field:SerializedName("id_str")
        val idStr: String,
        /**
         * When true, indicates that the user is a participant in Twitter's translator community.
         */
        @field:SerializedName("is_translator")
        val isTranslator: Boolean,
        /**
         * The BCP 47 code for the user's self-declared user interface language. May or may not have
         * anything to do with the content of their Tweets.
         */
        @field:SerializedName("lang")
        val lang: String,
        /**
         * The number of public lists that this user is a member of.
         */
        @field:SerializedName("listed_count")
        val listedCount: Int,
        /**
         * Nullable. The user-defined location for this account's profile. Not necessarily a location
         * nor parseable. This field will occasionally be fuzzily interpreted by the Search service.
         */
        @field:SerializedName("location")
        val location: String,
        /**
         * The name of the user, as they've defined it. Not necessarily a person's name. Typically
         * capped at 20 characters, but subject to change.
         */
        @field:SerializedName("name")
        val name: String,
        /**
         * The hexadecimal color chosen by the user for their background.
         */
        @field:SerializedName("profile_background_color")
        val profileBackgroundColor: String,
        /**
         * A HTTP-based URL pointing to the background image the user has uploaded for their profile.
         */
        @field:SerializedName("profile_background_image_url")
        val profileBackgroundImageUrl: String,
        /**
         * A HTTPS-based URL pointing to the background image the user has uploaded for their profile.
         */
        @field:SerializedName("profile_background_image_url_https")
        val profileBackgroundImageUrlHttps: String,
        /**
         * When true, indicates that the user's profile_background_image_url should be tiled when
         * displayed.
         */
        @field:SerializedName("profile_background_tile")
        val profileBackgroundTile: Boolean,
        /**
         * The HTTPS-based URL pointing to the standard web representation of the user's uploaded
         * profile banner. By adding a final path element of the URL, you can obtain different image
         * sizes optimized for specific displays. In the future, an API method will be provided to serve
         * these URLs so that you need not modify the original URL. For size variations, please see
         * User Profile Images and Banners.
         */
        @field:SerializedName("profile_banner_url")
        val profileBannerUrl: String,
        /**
         * A HTTP-based URL pointing to the user's avatar image. See User Profile Images and Banners.
         */
        @field:SerializedName("profile_image_url")
        val profileImageUrl: String,
        /**
         * A HTTPS-based URL pointing to the user's avatar image.
         */
        @field:SerializedName("profile_image_url_https")
        val profileImageUrlHttps: String,
        /**
         * The hexadecimal color the user has chosen to display links with in their Twitter UI.
         */
        @field:SerializedName("profile_link_color")
        val profileLinkColor: String,
        /**
         * The hexadecimal color the user has chosen to display sidebar borders with in their Twitter
         * UI.
         */
        @field:SerializedName("profile_sidebar_border_color")
        val profileSidebarBorderColor: String,
        /**
         * The hexadecimal color the user has chosen to display sidebar backgrounds with in their
         * Twitter UI.
         */
        @field:SerializedName("profile_sidebar_fill_color")
        val profileSidebarFillColor: String,
        /**
         * The hexadecimal color the user has chosen to display text with in their Twitter UI.
         */
        @field:SerializedName("profile_text_color")
        val profileTextColor: String,
        /**
         * When true, indicates the user wants their uploaded background image to be used.
         */
        @field:SerializedName("profile_use_background_image")
        val profileUseBackgroundImage: Boolean,
        /**
         * When true, indicates that this user has chosen to protect their Tweets. See About Public and
         * Protected Tweets.
         */
        @field:SerializedName("protected")
        val protectedUser: Boolean,
        /**
         * The screen name, handle, or alias that this user identifies themselves with. screen_names are
         * unique but subject to change. Use id_str as a user identifier whenever possible. Typically a
         * maximum of 15 characters long, but some historical accounts may exist with longer names.
         */
        @field:SerializedName("screen_name")
        val screenName: String,
        /**
         * Indicates that the user would like to see media inline. Somewhat disused.
         */
        @field:SerializedName("show_all_inline_media")
        val showAllInlineMedia: Boolean,
        /**
         * Nullable. If possible, the user's most recent tweet or retweet. In some circumstances, this
         * data cannot be provided and this field will be omitted, null, or empty. Perspectival
         * attributes within tweets embedded within users cannot always be relied upon. See Why are
         * embedded objects stale or inaccurate?.
         */
        @field:SerializedName("status")
        val status: Tweet,
        /**
         * The number of tweets (including retweets) issued by the user.
         */
        @field:SerializedName("statuses_count")
        val statusesCount: Int,
        /**
         * Nullable. A string describing the Time Zone this user declares themselves within.
         */
        @field:SerializedName("time_zone")
        val timeZone: String,
        /**
         * Nullable. A URL provided by the user in association with their profile.
         */
        @field:SerializedName("url")
        val url: String,
        /**
         * Nullable. The offset from GMT/UTC in seconds.
         */
        @field:SerializedName("utc_offset")
        val utcOffset: Int,
        /**
         * When true, indicates that the user has a verified account. See Verified Accounts.
         */
        @field:SerializedName("verified")
        val verified: Boolean,
        /**
         * When present, indicates a textual representation of the two-letter country codes this user is
         * withheld from.
         */
        @field:SerializedName("withheld_in_countries")
        val withheldInCountries: List<String>,
        /**
         * When present, indicates whether the content being withheld is the "status" or a "user."
         */
        @field:SerializedName("withheld_scope")
        val withheldScope: String) : Serializable, Identifiable {
    companion object {
        private const val serialVersionUID = 4663450696842173958L
        val INVALID_ID = -1L
    }
}
