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

package com.twitter.sdk.android.tweetui

import com.twitter.sdk.android.core.models.HashtagEntity
import com.twitter.sdk.android.core.models.MentionEntity
import com.twitter.sdk.android.core.models.SymbolEntity
import com.twitter.sdk.android.core.models.UrlEntity

internal open class FormattedUrlEntity(
        var start: Int,
        var end: Int,
        val displayUrl: String,
        val url: String,
        val expandedUrl: String
) {
    companion object {

        fun createFormattedUrlEntity(entity: UrlEntity): FormattedUrlEntity {
            return FormattedUrlEntity(entity.start, entity.end, entity.displayUrl,
                    entity.url, entity.expandedUrl)
        }

        fun createFormattedUrlEntity(hashtagEntity: HashtagEntity): FormattedUrlEntity {
            val url = TweetUtils.getHashtagPermalink(hashtagEntity.text)
            return FormattedUrlEntity(hashtagEntity.start, hashtagEntity.end,
                    "#" + hashtagEntity.text, url, url)
        }

        fun createFormattedUrlEntity(mentionEntity: MentionEntity): FormattedUrlEntity {
            val url = TweetUtils.getProfilePermalink(mentionEntity.screenName)
            return FormattedUrlEntity(mentionEntity.start, mentionEntity.end,
                    "@" + mentionEntity.screenName, url, url)
        }

        fun createFormattedUrlEntity(symbolEntity: SymbolEntity): FormattedUrlEntity {
            val url = TweetUtils.getSymbolPermalink(symbolEntity.text)
            return FormattedUrlEntity(symbolEntity.start, symbolEntity.end,
                    "$" + symbolEntity.text, url, url)
        }
    }
}
