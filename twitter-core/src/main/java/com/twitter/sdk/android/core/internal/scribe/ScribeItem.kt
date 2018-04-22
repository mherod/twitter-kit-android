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

package com.twitter.sdk.android.core.internal.scribe

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.twitter.sdk.android.core.internal.VineCardUtils
import com.twitter.sdk.android.core.models.Card
import com.twitter.sdk.android.core.models.MediaEntity
import com.twitter.sdk.android.core.models.Tweet
import com.twitter.sdk.android.core.models.User
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class ScribeItem(
        /**
         * The type of item (tweet, message, etc).
         * Optional field.
         */
        @field:SerializedName("item_type") val itemType: Int?,
        /**
         * A numerical id associated with the item.
         * Optional field.
         */
        @field:SerializedName("id") val id: Long?,
        /**
         * A description of the item.
         * Optional field.
         */
        @field:SerializedName("description") val description: String?,
        /**
         * Card event.
         * Optional field.
         */
        @field:SerializedName("card_event") val cardEvent: CardEvent?,
        /**
         * Media details.
         * Optional field.
         */
        @field:SerializedName("media_details") val mediaDetails: MediaDetails?) : Serializable, Parcelable {

    /**
     * Card event.
     */
    @Parcelize
    class CardEvent(@field:SerializedName("promotion_card_type") val promotionCardType: Int) : Serializable, Parcelable

    /**
     * Media details.
     */
    @Parcelize
    data class MediaDetails(
            @field:SerializedName("content_id") val contentId: Long,
            @field:SerializedName("media_type") val mediaType: Int,
            @field:SerializedName("publisher_id") val publisherId: Long?
    ) : Serializable, Parcelable {

        companion object {
            const val TYPE_CONSUMER = 1
            const val TYPE_AMPLIFY = 2
            const val TYPE_ANIMATED_GIF = 3
            const val TYPE_VINE = 4

            const val GIF_TYPE = "animated_gif"
        }
    }

    class Builder {
        private var itemType: Int? = null
        private var id: Long? = null
        private var description: String? = null
        private var cardEvent: CardEvent? = null
        private var mediaDetails: MediaDetails? = null

        fun setItemType(itemType: Int): Builder {
            this.itemType = itemType
            return this
        }

        fun setId(id: Long): Builder {
            this.id = id
            return this
        }

        fun setDescription(description: String): Builder {
            this.description = description
            return this
        }

        fun setCardEvent(cardEvent: CardEvent): Builder {
            this.cardEvent = cardEvent
            return this
        }

        fun setMediaDetails(mediaDetails: MediaDetails): Builder {
            this.mediaDetails = mediaDetails
            return this
        }

        fun build(): ScribeItem = ScribeItem(itemType, id, description, cardEvent, mediaDetails)
    }

    companion object {
        /**
         * Scribe item types. See ItemType in
         * See: source/tree/science/src/thrift/com/twitter/clientapp/gen/client_app.thrift
         */
        const val TYPE_TWEET = 0
        const val TYPE_USER = 3
        const val TYPE_MESSAGE = 6

        fun fromTweet(tweet: Tweet): ScribeItem = ScribeItem.Builder()
                .setItemType(TYPE_TWEET)
                .setId(tweet.id)
                .build()

        fun fromUser(user: User): ScribeItem = ScribeItem.Builder()
                .setItemType(TYPE_USER)
                .setId(user.id)
                .build()

        fun fromMessage(message: String): ScribeItem = ScribeItem.Builder()
                .setItemType(TYPE_MESSAGE)
                .setDescription(message)
                .build()

        fun fromTweetCard(tweetId: Long, card: Card): ScribeItem = ScribeItem.Builder()
                .setItemType(ScribeItem.TYPE_TWEET)
                .setId(tweetId)
                .setMediaDetails(createCardDetails(tweetId, card))
                .build()

        fun fromMediaEntity(tweetId: Long, mediaEntity: MediaEntity): ScribeItem =
                ScribeItem.Builder()
                        .setItemType(ScribeItem.TYPE_TWEET)
                        .setId(tweetId)
                        .setMediaDetails(createMediaDetails(tweetId, mediaEntity))
                        .build()

        private fun createMediaDetails(tweetId: Long, mediaEntity: MediaEntity): ScribeItem.MediaDetails =
                ScribeItem.MediaDetails(
                        tweetId,
                        getMediaType(mediaEntity),
                        mediaEntity.id
                )

        private fun createCardDetails(tweetId: Long, card: Card): ScribeItem.MediaDetails =
                ScribeItem.MediaDetails(
                        tweetId,
                        MediaDetails.TYPE_VINE,
                        VineCardUtils.getPublisherId(card).toLongOrNull()
                )

        private fun getMediaType(mediaEntity: MediaEntity): Int =
                if (MediaDetails.GIF_TYPE == mediaEntity.type) {
                    ScribeItem.MediaDetails.TYPE_ANIMATED_GIF
                } else {
                    ScribeItem.MediaDetails.TYPE_CONSUMER
                }
    }
}
