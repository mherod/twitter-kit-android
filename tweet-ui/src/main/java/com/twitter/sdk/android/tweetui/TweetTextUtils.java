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

package com.twitter.sdk.android.tweetui;

import android.text.TextUtils;

import com.twitter.sdk.android.core.models.HashtagEntity;
import com.twitter.sdk.android.core.models.MediaEntity;
import com.twitter.sdk.android.core.models.MentionEntity;
import com.twitter.sdk.android.core.models.SymbolEntity;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.UrlEntity;
import com.twitter.sdk.android.tweetui.internal.util.HtmlEntities;

import java.util.ArrayList;
import java.util.List;

final class TweetTextUtils {

    private TweetTextUtils() {}

    /**
     * Should not be called directly outside of TweetRepository, the return value should be cached
     * or memoized.
     *
     * @param tweet The tweet to format
     * @return      The formatted Tweet text
     */
    static FormattedTweetText formatTweetText(Tweet tweet) {
        if (tweet == null) return null;

        final FormattedTweetText adjustedTweet = new FormattedTweetText();

        convertEntities(adjustedTweet, tweet);
        format(adjustedTweet, tweet);

        return adjustedTweet;
    }

    /**
     * Populates the list of formatted entities within the formattedTweetText.
     *
     * @param formattedTweetText The formatted tweet text that is to be populated
     * @param tweet The source Tweet
     */
    static void convertEntities(FormattedTweetText formattedTweetText, Tweet tweet) {
        if (tweet.getEntities() == null) return;

        final List<UrlEntity> coreUrls = tweet.getEntities().getUrls();
        if (coreUrls != null) {
            for (UrlEntity entity : coreUrls) {
                final FormattedUrlEntity formattedUrlEntity =
                        FormattedUrlEntity.createFormattedUrlEntity(entity);
                formattedTweetText.getUrlEntities().add(formattedUrlEntity);
            }
        }

        final List<MediaEntity> coreMedia = tweet.getEntities().getMedia();
        if (coreMedia != null) {
            for (MediaEntity entity : coreMedia) {
                final FormattedMediaEntity formattedMediaEntity = new FormattedMediaEntity(entity);
                formattedTweetText.getMediaEntities().add(formattedMediaEntity);
            }
        }

        final List<HashtagEntity> coreHashtags = tweet.getEntities().getHashtags();
        if (coreHashtags != null) {
            for (HashtagEntity entity : coreHashtags) {
                final FormattedUrlEntity formattedHashtagEntity =
                        FormattedUrlEntity.Companion.createFormattedUrlEntity(entity);
                formattedTweetText.getHashtagEntities().add(formattedHashtagEntity);
            }
        }

        final List<MentionEntity> coreMentions = tweet.getEntities().getUserMentions();
        if (coreMentions != null) {
            for (MentionEntity entity : coreMentions) {
                final FormattedUrlEntity formattedMentionEntity =
                        FormattedUrlEntity.Companion.createFormattedUrlEntity(entity);
                formattedTweetText.getMentionEntities().add(formattedMentionEntity);
            }
        }

        final List<SymbolEntity> coreSymbols = tweet.getEntities().getSymbols();
        if (coreSymbols != null) {
            for (SymbolEntity entity : coreSymbols) {
                final FormattedUrlEntity formattedSymbolEntity =
                        FormattedUrlEntity.Companion.createFormattedUrlEntity(entity);
                formattedTweetText.getSymbolEntities().add(formattedSymbolEntity);
            }
        }
    }

    /**
     * Calls the html unescaper and then the method to fix the entity indices errors caused by
     * emoji/supplementary characters.
     *
     * @param formattedTweetText The formatted tweet text that is to be populated
     * @param tweet The source Tweet
     */
    static void format(FormattedTweetText formattedTweetText, Tweet tweet) {
        if (TextUtils.isEmpty(tweet.getText())) return;

        final HtmlEntities.Unescaped u = HtmlEntities.HTML40.unescape(tweet.getText());
        final StringBuilder result = new StringBuilder(u.unescaped);

        adjustIndicesForEscapedChars(formattedTweetText.getUrlEntities(), u.indices);
        adjustIndicesForEscapedChars(formattedTweetText.getMediaEntities(), u.indices);
        adjustIndicesForEscapedChars(formattedTweetText.getHashtagEntities(), u.indices);
        adjustIndicesForEscapedChars(formattedTweetText.getMentionEntities(), u.indices);
        adjustIndicesForEscapedChars(formattedTweetText.getSymbolEntities(), u.indices);
        adjustIndicesForSupplementaryChars(result, formattedTweetText);
        formattedTweetText.setText(result.toString());
    }

    /**
     * Since the unescaping of html causes for example &amp; to turn into & we need to adjust
     * the entity indices after that by 4 characters. This loops through the entities and adjusts
     * them as necessary.
     *
     * @param entities The entities that need to be adjusted
     * @param indices The indices of where there were escaped html chars that we unescaped
     */
    static void adjustIndicesForEscapedChars(
            List<? extends FormattedUrlEntity> entities,
            List<int[]> indices) {
        if (entities == null || indices == null || indices.isEmpty()) {
            return;
        }
        final int size = indices.size();
        int m = 0; // marker
        int diff = 0; // accumulated difference
        int inDiff; // end difference for escapes in range
        int len; // escaped length
        int start; // escaped start
        int end; // escaped end
        int i; // reusable index
        int[] index;
        // For each of the entities, update the start and end indices
        // Note: tweet entities are sorted.

        for (FormattedUrlEntity entity : entities) {
            inDiff = 0;
            // Go through the escaped entities' indices
            for (i = m; i < size; i++) {
                index = indices.get(i);
                start = index[0];
                end = index[1];
                // len is actually (end - start + 1) - 1
                len = end - start;
                if (end < entity.getStart()) {
                    // bump position of the next marker
                    diff += len;
                    m++;
                } else if (end < entity.getEnd()) {
                    inDiff += len;
                }
            }
            // Once we've accumulated diffs, calc the offset
            entity.setStart(entity.getStart() - (diff + inDiff));
            entity.setEnd(entity.getEnd() - (diff + inDiff));
        }
    }

    /**
     * Adjusts entity indices for supplementary characters (Emoji being the most common example) in
     * UTF-8 (ones outside of U+0000 to U+FFFF range) are represented as a pair of char values, the
     * first from the high-surrogates range, and the second from the low-surrogates range.
     *
     * @param content The content of the tweet
     * @param formattedTweetText The formatted tweet text with entities that we need to adjust
     */
    static void adjustIndicesForSupplementaryChars(StringBuilder content,
            FormattedTweetText formattedTweetText) {
        final List<Integer> highSurrogateIndices = new ArrayList<>();
        final int len = content.length() - 1;
        for (int i = 0; i < len; ++i) {
            if (Character.isHighSurrogate(content.charAt(i))
                    && Character.isLowSurrogate(content.charAt(i + 1))) {
                highSurrogateIndices.add(i);
            }
        }

        adjustEntitiesWithOffsets(formattedTweetText.getUrlEntities(), highSurrogateIndices);
        adjustEntitiesWithOffsets(formattedTweetText.getMediaEntities(), highSurrogateIndices);
        adjustEntitiesWithOffsets(formattedTweetText.getHashtagEntities(), highSurrogateIndices);
        adjustEntitiesWithOffsets(formattedTweetText.getMentionEntities(), highSurrogateIndices);
        adjustEntitiesWithOffsets(formattedTweetText.getSymbolEntities(), highSurrogateIndices);
    }

    /**
     * Shifts indices by 1 since the Twitter REST Api does not count them correctly for our language
     * runtime.
     *
     * @param entities The entities that need to be adjusted
     * @param indices The indices in the string where there are supplementary chars
     */
    static void adjustEntitiesWithOffsets(List<? extends FormattedUrlEntity> entities,
            List<Integer> indices) {
        if (entities == null || indices == null) return;
        for (FormattedUrlEntity entity : entities) {
            // find all indices <= start and update offsets by that much
            final int start = entity.getStart();
            int offset = 0;
            for (Integer index : indices) {
                if (index - offset <= start) {
                    offset += 1;
                } else {
                    break;
                }
            }
            entity.setStart(entity.getStart() + offset);
            entity.setEnd(entity.getEnd() + offset);
        }
    }
}
