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

import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.CharacterStyle
import android.view.View

import com.twitter.sdk.android.core.models.ModelUtils
import com.twitter.sdk.android.tweetui.internal.ClickableLinkSpan
import com.twitter.sdk.android.tweetui.internal.TweetMediaUtils

import java.util.ArrayList
import java.util.Collections
import java.util.regex.Pattern

object TweetTextLinkifier {
    val QUOTED_STATUS_URL = Pattern.compile("^https?://twitter\\.com(/#!)?/\\w+/status/\\d+$")
    val VINE_URL = Pattern.compile("^https?://vine\\.co(/#!)?/v/\\w+$")

    /**
     * Returns a charSequence with the display urls substituted in place of the t.co links. It will
     * strip off the last photo entity, quote Tweet, and Vine card urls in the text. The return
     * value can be set directly onto a text view.
     *
     * @param tweetText             The formatted and adjusted tweet wrapper
     * @param linkListener          A listener to handle link clicks
     * @param linkColor             The link color
     * @param linkHighlightColor    The link background color when pressed
     * @param stripQuoteTweet       If true we should strip the quote Tweet URL
     * @param stripVineCard         If true we should strip the Vine card URL
     * @return                      The Tweet text with displayUrls substituted in
     */
    @JvmStatic
    fun linkifyUrls(
            tweetText: FormattedTweetText?,
            linkListener: LinkClickListener?,
            linkColor: Int,
            linkHighlightColor: Int,
            stripQuoteTweet: Boolean,
            stripVineCard: Boolean
    ): CharSequence? {
        if (tweetText == null) return null

        if (TextUtils.isEmpty(tweetText.text)) {
            return tweetText.text
        }

        val spannable = SpannableStringBuilder(tweetText.text)
        val urls = ModelUtils.getSafeList(tweetText.urlEntities)
        val media = ModelUtils.getSafeList(tweetText.mediaEntities)
        val hashtags = ModelUtils.getSafeList(tweetText.hashtagEntities)
        val mentions = ModelUtils.getSafeList(tweetText.mentionEntities)
        val symbols = ModelUtils.getSafeList(tweetText.symbolEntities)
        /*
         * We combine and sort the entities here so that we can correctly calculate the offsets
         * into the text.
         */
        val combined = mergeAndSortEntities(urls, media, hashtags,
                mentions, symbols)
        val strippedEntity = getEntityToStrip(tweetText.text, combined,
                stripQuoteTweet, stripVineCard)

        addUrlEntities(spannable, combined, strippedEntity, linkListener, linkColor,
                linkHighlightColor)

        return trimEnd(spannable)
    }

    /**
     * Trim trailing whitespaces. Similar to String#trim(), but only for trailing characters.
     */
    @JvmStatic
    fun trimEnd(charSequence: CharSequence): CharSequence {
        var length = charSequence.length

        while (length > 0 && charSequence[length - 1] <= ' ') {
            length--
        }

        // Avoid creating new object if length hasn't changed
        return if (length < charSequence.length) charSequence.subSequence(0, length) else charSequence
    }

    /**
     * Combines and sorts the two lists of entities, it only considers the start index as the
     * parameter to sort on because the api guarantees that we are to have non-overlapping entities.
     *
     * @param urls  Expected to be non-null
     * @param media Can be null
     * @return      Combined and sorted list of urls and media
     */
    @JvmStatic
    fun mergeAndSortEntities(urls: List<FormattedUrlEntity>,
                             media: List<FormattedMediaEntity>, hashtags: List<FormattedUrlEntity>,
                             mentions: List<FormattedUrlEntity>, symbols: List<FormattedUrlEntity>): List<FormattedUrlEntity> {
        val combined = ArrayList(urls)
        combined.addAll(media)
        combined.addAll(hashtags)
        combined.addAll(mentions)
        combined.addAll(symbols)
        combined.sortWith(Comparator { lhs, rhs ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Integer.compare(lhs?.start ?: 0, rhs?.start ?: 0)
            } else {
                TODO("VERSION.SDK_INT < KITKAT")
            }

        })
        return combined
    }

    /**
     * Swaps display urls in for t.co urls and adjusts the remaining entity indices.
     *
     * @param spannable          The final formatted text that we are building
     * @param entities           The combined list of media and url entities
     * @param strippedEntity     The trailing entity that we should strip from the text
     * @param linkListener       The link click listener to attach to the span
     * @param linkColor          The link color
     * @param linkHighlightColor The link background color when pressed
     */
    @JvmStatic
    private fun addUrlEntities(spannable: SpannableStringBuilder,
                               entities: List<FormattedUrlEntity>?,
                               strippedEntity: FormattedUrlEntity?,
                               linkListener: LinkClickListener?,
                               linkColor: Int, linkHighlightColor: Int) {
        if (entities == null || entities.isEmpty()) return

        var offset = 0
        var len: Int
        var start: Int
        var end: Int
        for (url in entities) {
            start = url.start - offset
            end = url.end - offset
            if (start >= 0 && end <= spannable.length) {
                // replace the last photo url with empty string, we can use the start indices as
                // as simple check, since none of this will work anyways if we have overlapping
                // entities
                if (strippedEntity != null && strippedEntity.start == url.start) {
                    spannable.replace(start, end, "")
                    len = end - start
                    offset += len
                } else if (!TextUtils.isEmpty(url.displayUrl)) {
                    spannable.replace(start, end, url.displayUrl)
                    len = end - (start + url.displayUrl.length)
                    end -= len
                    offset += len

                    val span = object : ClickableLinkSpan(linkHighlightColor,
                            linkColor, false) {
                        override fun onClick(widget: View) {
                            if (linkListener == null) return
                            linkListener.onUrlClicked(url.url)
                        }
                    }
                    spannable.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
    }

    @JvmStatic
    fun getEntityToStrip(tweetText: String?, combined: List<FormattedUrlEntity>,
                         stripQuoteTweet: Boolean, stripVineCard: Boolean): FormattedUrlEntity? {
        if (combined.isEmpty()) return null

        val urlEntity = combined[combined.size - 1]
        return if (stripLtrMarker(tweetText!!).endsWith(urlEntity.url) && (isPhotoEntity(urlEntity) ||
                        stripQuoteTweet && isQuotedStatus(urlEntity) ||
                        stripVineCard && isVineCard(urlEntity))) {
            urlEntity
        } else null

    }

    @JvmStatic
    fun stripLtrMarker(tweetText: String): String {
        return if (tweetText.endsWith(Character.toString('\u200E'))) {
            tweetText.substring(0, tweetText.length - 1)
        } else tweetText

    }

    @JvmStatic
    fun isPhotoEntity(urlEntity: FormattedUrlEntity): Boolean {
        return urlEntity is FormattedMediaEntity && TweetMediaUtils.PHOTO_TYPE == urlEntity.type
    }

    @JvmStatic
    fun isQuotedStatus(urlEntity: FormattedUrlEntity): Boolean {
        return QUOTED_STATUS_URL.matcher(urlEntity.expandedUrl).find()
    }

    @JvmStatic
    fun isVineCard(urlEntity: FormattedUrlEntity): Boolean {
        return VINE_URL.matcher(urlEntity.expandedUrl).find()
    }
}
