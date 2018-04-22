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
 * A Tweet is the basic atomic building block of all things Twitter. Tweets, also known more
 * generically as "status updates." Tweets can be embedded, replied to, favorited, unfavorited and
 * deleted.
 */
class Tweet(
        /**
         * Nullable. Represents the geographic location of this Tweet as reported by the user or client
         * application. The inner coordinates array is formatted as geoJSON (longitude first,
         * then latitude).
         */
        @field:SerializedName("coordinates")
        val coordinates: Coordinates?,
        /**
         * UTC time when this Tweet was created.
         */
        @field:SerializedName("created_at")
        val createdAt: String?,
        /**
         * Perspectival. Only surfaces on methods supporting the include_my_retweet parameter, when set
         * to true. Details the Tweet ID of the user's own retweet (if existent) of this Tweet.
         */
        @field:SerializedName("current_user_retweet")
        val currentUserRetweet: Any?,
        entities: TweetEntities?,
        extendedEntities: TweetEntities?,
        /**
         * Nullable. Indicates approximately how many times this Tweet has been "favorited" by Twitter
         * users.
         */
        @field:SerializedName("favorite_count")
        val favoriteCount: Int?,
        /**
         * Nullable. Perspectival. Indicates whether this Tweet has been favorited by the authenticating
         * user.
         */
        @field:SerializedName("favorited")
        val favorited: Boolean,
        /**
         * Indicates the maximum value of the filter_level parameter which may be used and still stream
         * this Tweet. So a value of medium will be streamed on none, low, and medium streams.
         */
        @field:SerializedName("filter_level")
        val filterLevel: String?,
        /**
         * The integer representation of the unique identifier for this Tweet. This number is greater
         * than 53 bits and some programming languages may have difficulty/silent defects in
         * interpreting it. Using a signed 64 bit integer for storing this identifier is safe. Use
         * id_str for fetching the identifier to stay on the safe side. See Twitter IDs, JSON and
         * Snowflake.
         */
        @field:SerializedName("id")
        override val id: Long,
        /**
         * The string representation of the unique identifier for this Tweet. Implementations should use
         * this rather than the large integer in id
         */
        @field:SerializedName("id_str")
        val idStr: String,
        /**
         * Nullable. If the represented Tweet is a reply, this field will contain the screen name of
         * the original Tweet's author.
         */
        @field:SerializedName("in_reply_to_screen_name")
        val inReplyToScreenName: String?,
        /**
         * Nullable. If the represented Tweet is a reply, this field will contain the integer
         * representation of the original Tweet's ID.
         */
        @field:SerializedName("in_reply_to_status_id")
        val inReplyToStatusId: Long,
        /**
         * Nullable. If the represented Tweet is a reply, this field will contain the string
         * representation of the original Tweet's ID.
         */
        @field:SerializedName("in_reply_to_status_id_str")
        val inReplyToStatusIdStr: String,
        /**
         * Nullable. If the represented Tweet is a reply, this field will contain the integer
         * representation of the original Tweet's author ID. This will not necessarily always be the
         * user directly mentioned in the Tweet.
         */
        @field:SerializedName("in_reply_to_user_id")
        val inReplyToUserId: Long,
        /**
         * Nullable. If the represented Tweet is a reply, this field will contain the string
         * representation of the original Tweet's author ID. This will not necessarily always be the
         * user directly mentioned in the Tweet.
         */
        @field:SerializedName("in_reply_to_user_id_str")
        val inReplyToUserIdStr: String,
        /**
         * Nullable. When present, indicates a BCP 47 language identifier corresponding to the
         * machine-detected language of the Tweet text, or "und" if no language could be detected.
         */
        @field:SerializedName("lang")
        val lang: String?,
        /**
         * Nullable. When present, indicates that the tweet is associated (but not necessarily
         * originating from) a Place.
         */
        @field:SerializedName("place")
        val place: Place?,
        /**
         * Nullable. This field only surfaces when a tweet contains a link. The meaning of the field
         * doesn't pertain to the tweet content itself, but instead it is an indicator that the URL
         * contained in the tweet may contain content or media identified as sensitive content.
         */
        @field:SerializedName("possibly_sensitive")
        val possiblySensitive: Boolean,
        /**
         * A set of key-value pairs indicating the intended contextual delivery of the containing Tweet.
         * Currently used by Twitter's Promoted Products.
         */
        @field:SerializedName("scopes")
        val scopes: Any?,
        /**
         * This field only surfaces when the Tweet is a quote Tweet. This field contains the
         * integer value Tweet ID of the quoted Tweet.
         */
        @field:SerializedName("quoted_status_id")
        val quotedStatusId: Long,
        /**
         * This field only surfaces when the Tweet is a quote Tweet. This is the string representation
         * Tweet ID of the quoted Tweet.
         */
        @field:SerializedName("quoted_status_id_str")
        val quotedStatusIdStr: String,
        /**
         * This field only surfaces when the Tweet is a quote Tweet. This attribute contains the
         * Tweet object of the original Tweet that was quoted.
         */
        @field:SerializedName("quoted_status")
        val quotedStatus: Tweet?,
        /**
         * Number of times this Tweet has been retweeted. This field is no longer capped at 99 and will
         * not turn into a String for "100+"
         */
        @field:SerializedName("retweet_count")
        val retweetCount: Int,
        /**
         * Perspectival. Indicates whether this Tweet has been retweeted by the authenticating user.
         */
        @field:SerializedName("retweeted")
        val retweeted: Boolean,
        /**
         * Users can amplify the broadcast of tweets authored by other users by retweeting. Retweets can
         * be distinguished from typical Tweets by the existence of a retweeted_status attribute. This
         * attribute contains a representation of the original Tweet that was retweeted. Note that
         * retweets of retweets do not show representations of the intermediary retweet, but only the
         * original tweet. (Users can also unretweet a retweet they created by deleting their retweet.)
         */
        @field:SerializedName("retweeted_status")
        val retweetedStatus: Tweet?,
        /**
         * Utility used to post the Tweet, as an HTML-formatted string. Tweets from the Twitter website
         * have a source value of web.
         */
        @field:SerializedName("source")
        val source: String?,
        /**
         * The actual UTF-8 text of the status update. See twitter-text for details on what is currently
         * considered valid characters.
         */
        @field:SerializedName(value = "text", alternate = ["full_text"])
        val text: String?,
        displayTextRange: List<Int>?,
        /**
         * Indicates whether the value of the text parameter was truncated, for example, as a result of
         * a retweet exceeding the 140 character Tweet length. Truncated text will end in ellipsis, like
         * this ... Since Twitter now rejects long Tweets vs truncating them, the large majority of
         * Tweets will have this set to false.
         * Note that while native retweets may have their toplevel text property shortened, the original
         * text will be available under the retweeted_status object and the truncated parameter will be
         * set to the value of the original status (in most cases, false).
         */
        @field:SerializedName("truncated")
        val truncated: Boolean,
        /**
         * The user who posted this Tweet. Perspectival attributes embedded within this object are
         * unreliable. See Why are embedded objects stale or inaccurate?.
         */
        @field:SerializedName("user")
        val user: User?,
        /**
         * When present and set to "true", it indicates that this piece of content has been withheld due
         * to a DMCA complaint.
         */
        @field:SerializedName("withheld_copyright")
        val withheldCopyright: Boolean,
        withheldInCountries: List<String>?,
        /**
         * When present, indicates whether the content being withheld is the "status" or a "user."
         */
        @field:SerializedName("withheld_scope")
        val withheldScope: String?,
        /**
         * Nullable. Card data used to attach rich photos, videos and media experience to Tweets.
         */
        @field:SerializedName("card")
        val card: Card?
) : Identifiable {

    /**
     * Entities which have been parsed out of the text of the Tweet.
     */
    @SerializedName("entities")
    val entities: TweetEntities

    /**
     * Additional entities such as multi photos, animated gifs and video.
     */
    @SerializedName("extended_entities")
    val extendedEntities: TweetEntities


    /**
     * An array of two unicode code point indices, identifying the inclusive start and exclusive end
     * of the displayable content of the Tweet.
     */
    @SerializedName("display_text_range")
    val displayTextRange: List<Int>

    /**
     * When present, indicates a list of uppercase two-letter country codes this content is withheld
     * from. Twitter supports the following non-country values for this field:
     * "XX" - Content is withheld in all countries
     * "XY" - Content is withheld due to a DMCA request.
     */
    @SerializedName("withheld_in_countries")
    val withheldInCountries: List<String>

    private constructor() : this(null, null, null, TweetEntities.EMPTY, TweetEntities.EMPTY, 0, false, null, 0, "0", null, 0, "0", 0, "0", null, null, false, null, 0, "0", null, 0, false, null, null, null, null, false, null, false, null, null, null) {}

    init {
        this.entities = entities ?: TweetEntities.EMPTY
        this.extendedEntities = extendedEntities ?: TweetEntities.EMPTY
        this.displayTextRange = ModelUtils.getSafeList(displayTextRange)
        this.withheldInCountries = ModelUtils.getSafeList(withheldInCountries)
    }

    companion object {
        const val INVALID_ID = -1L
    }
}
