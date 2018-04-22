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

import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import com.twitter.sdk.android.core.models.MediaEntity
import com.twitter.sdk.android.core.models.UrlEntity
import org.hamcrest.Matchers.*
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class TweetTextLinkifierTest {

    @Test
    fun testLinkifyUrls_nullFormattedTweetText() {
        try {
            TweetTextLinkifier.linkifyUrls(null, null, 0, 0, true, true)
        } catch (e: Exception) {
            fail("threw unexpected exception")
        }

    }

    @Test
    fun testLinkifyUrls_newFormattedTweetText() {
        try {
            TweetTextLinkifier.linkifyUrls(FormattedTweetText(), null, 0, 0, true, true)
        } catch (e: Exception) {
            fail("threw unexpected exception")
        }

    }

    @Test
    fun testLinkifyUrls_oneUrlEntity() {
        val url = "http://t.co/foo"
        val displayUrl = "dev.twitter.com"
        val fullText = "$BASE_TEXT http://t.co/foo"
        val urlEntity = EntityFactory.newUrlEntity(fullText, url, displayUrl)

        val formattedText = FormattedTweetText()
        formattedText.text = fullText
        formattedText.urlEntities.add(FormattedUrlEntity.createFormattedUrlEntity(urlEntity))

        val linkifiedText = TweetTextLinkifier.linkifyUrls(formattedText, null, 0, 0, true, true)
        val displayUrlFromEntity = linkifiedText!!.subSequence(urlEntity.start, urlEntity.end).toString()
        assertThat(displayUrlFromEntity, `is`(urlEntity.displayUrl))
    }

    @Test
    fun testLinkifyUrls_oneInvalidUrlEntity() {
        val fullText = ""
        val urlEntity = UrlEntity("x z", "y", "z", -1, 30)
        val formattedText = FormattedTweetText()
        formattedText.text = fullText
        formattedText.urlEntities.add(FormattedUrlEntity.createFormattedUrlEntity(urlEntity))

        val linkifiedText = TweetTextLinkifier.linkifyUrls(formattedText, null, 0, 0, true, true)
        assertThat(linkifiedText!!.toString(), `is`(""))
    }

    @Test
    fun testLinkifyUrls_linkClickListener() {
        val url = "http://t.co/foo"
        val displayUrl = "dev.twitter.com"
        val fullText = "$BASE_TEXT http://t.co/foo"

        val mockClickListener = mock(LinkClickListener::class.java)

        val urlEntity = EntityFactory.newUrlEntity(fullText, url, displayUrl)
        val formattedText = FormattedTweetText()
        formattedText.text = fullText
        formattedText.urlEntities.add(FormattedUrlEntity.createFormattedUrlEntity(urlEntity))

        val linkifiedText = TweetTextLinkifier.linkifyUrls(formattedText, mockClickListener, 0, 0, true,
                true) as SpannableStringBuilder?
        val clickables = linkifiedText!!.getSpans(urlEntity.start, urlEntity.end,
                ClickableSpan::class.java)
        assertThat(clickables.size, `is`(1))
    }

    @Test
    fun testLinkifyHashtags_oneHashtagEntity() {
        val hashtag = "TwitterForGood"
        val fullHashtag = "#$hashtag"
        val fullText = "$BASE_TEXT $fullHashtag"
        val hashtagEntity = EntityFactory.newHashtagEntity(fullText, hashtag)

        val formattedText = FormattedTweetText()
        formattedText.text = fullText
        formattedText.hashtagEntities.add(FormattedUrlEntity.createFormattedUrlEntity(
                hashtagEntity))

        val linkifiedText = TweetTextLinkifier.linkifyUrls(formattedText, null, 0, 0, true, true)
        val displayUrlFromEntity = linkifiedText!!.subSequence(hashtagEntity.start,
                hashtagEntity.end).toString()
        assertThat(displayUrlFromEntity, `is`(fullHashtag))
    }

    @Test
    fun testLinkifyHashtags_linkClickListener() {
        val hashtag = "TwitterForGood"
        val fullText = "$BASE_TEXT #$hashtag"

        val mockClickListener = mock(LinkClickListener::class.java)

        val hashtagEntity = EntityFactory.newHashtagEntity(fullText, hashtag)
        val formattedText = FormattedTweetText()
        formattedText.text = fullText
        formattedText.hashtagEntities.add(FormattedUrlEntity.createFormattedUrlEntity(
                hashtagEntity))

        val linkifiedText = TweetTextLinkifier.linkifyUrls(formattedText, mockClickListener, 0, 0, true,
                true) as SpannableStringBuilder?
        val clickables = linkifiedText!!.getSpans(hashtagEntity.start, hashtagEntity.end,
                ClickableSpan::class.java)
        assertThat(clickables.size, `is`(1))
    }

    @Test
    fun testLinkifyMentions_oneMentionEntity() {
        val mention = "TwitterDev"
        val fullMention = "@$mention"
        val fullText = "$BASE_TEXT $fullMention"
        val mentionEntity = EntityFactory.newMentionEntity(fullText, mention)

        val formattedText = FormattedTweetText()
        formattedText.text = fullText
        formattedText.mentionEntities.add(FormattedUrlEntity.createFormattedUrlEntity(
                mentionEntity))

        val linkifiedText = TweetTextLinkifier.linkifyUrls(formattedText, null, 0, 0, true, true)
        val displayUrlFromEntity = linkifiedText!!.subSequence(mentionEntity.start,
                mentionEntity.end).toString()
        assertThat(displayUrlFromEntity, `is`(fullMention))
    }

    @Test
    fun testLinkifyMentions_linkClickListener() {
        val mention = "TwitterDev"
        val fullText = "$BASE_TEXT @$mention"

        val mockClickListener = mock(LinkClickListener::class.java)

        val mentionEntity = EntityFactory.newMentionEntity(fullText, mention)
        val formattedText = FormattedTweetText()
        formattedText.text = fullText
        formattedText.mentionEntities.add(FormattedUrlEntity.createFormattedUrlEntity(
                mentionEntity))

        val linkifiedText = TweetTextLinkifier.linkifyUrls(formattedText, mockClickListener, 0, 0, true,
                true) as SpannableStringBuilder?
        val clickables = linkifiedText!!.getSpans(mentionEntity.start, mentionEntity.end,
                ClickableSpan::class.java)
        assertThat(clickables.size, `is`(1))
    }

    @Test
    fun testLinkifySymbols_oneSymbolEntity() {
        val symbol = "TWTR"
        val fullSymbol = "$$symbol"
        val fullText = "$BASE_TEXT $fullSymbol"
        val symbolEntity = EntityFactory.newSymbolEntity(fullText, symbol)

        val formattedText = FormattedTweetText()
        formattedText.text = fullText
        formattedText.symbolEntities.add(FormattedUrlEntity.createFormattedUrlEntity(
                symbolEntity))

        val linkifiedText = TweetTextLinkifier.linkifyUrls(formattedText, null, 0, 0, true, true)
        val displayUrlFromEntity = linkifiedText!!.subSequence(symbolEntity.start,
                symbolEntity.end).toString()
        assertThat(displayUrlFromEntity, `is`(fullSymbol))
    }

    @Test
    fun testLinkifySymbols_linkClickListener() {
        val symbol = "TWTR"
        val fullText = "$BASE_TEXT $$symbol"

        val mockClickListener = mock(LinkClickListener::class.java)

        val symbolEntity = EntityFactory.newSymbolEntity(fullText, symbol)
        val formattedText = FormattedTweetText()
        formattedText.text = fullText
        formattedText.symbolEntities.add(FormattedUrlEntity.createFormattedUrlEntity(
                symbolEntity))

        val linkifiedText = TweetTextLinkifier.linkifyUrls(formattedText, mockClickListener, 0, 0, true,
                true) as SpannableStringBuilder?
        val clickables = linkifiedText!!.getSpans(symbolEntity.start, symbolEntity.end,
                ClickableSpan::class.java)
        assertThat(clickables.size, `is`(1))
    }

    @Test
    fun testLinkifyUrls_verifyPhotoOnlyStrippedFromEnd() {
        val formattedText = setupPicTwitterEntities()
        val lastPhotoUrl = formattedText.mediaEntities[0]
        val linkifiedText = TweetTextLinkifier.linkifyUrls(formattedText, null, 0, 0, true, true)

        // make sure we are stripping out a photo entity since it is the only media entity
        // that we can render inline
        assertThat(lastPhotoUrl.type, `is`("photo"))
        // assert that we do not strip it here and display it in the middle
        assertThat(linkifiedText!!.toString(), containsString(lastPhotoUrl.displayUrl))
    }

    @Test
    fun testGetEntityToStrip_withLtrMarker() {
        val result = TweetTextLinkifier.stripLtrMarker(TEST_RLT_STRING)

        assertThat(result, not(`is`(TEST_RLT_STRING)))
        assertThat(result.endsWith(Character.toString('\u200E')), `is`(false))
    }

    @Test
    fun testGetEntityToStrip_withoutLtrMarker() {
        val result = TweetTextLinkifier.stripLtrMarker(BASE_TEXT)

        assertThat(result, `is`(BASE_TEXT))
        assertThat(result.endsWith(Character.toString('\u200E')), `is`(false))
    }

    @Test
    fun testIsPhotoEntity_withPhotoUrl() {
        val mediaEntity = MediaEntity("http://t.co/PFHCdlr4i0", null,
                "pic.twitter.com/abc", 27, 49, 0L, null, null, null, null, 0L, null, "photo", null,
                "")
        val formattedUrlEntity = FormattedMediaEntity(mediaEntity)

        assertThat(TweetTextLinkifier.isPhotoEntity(formattedUrlEntity), `is`(true))
    }

    @Test
    fun testIsQuotedStatus_withQuotedStatusUrl() {
        val urlEntity = UrlEntity("https://t.co/kMXdOEnVMg",
                "https://twitter.com/nasajpl/status/634475698174865408",
                "twitter.com/nasajpl/status\u2026", 50, 72)
        val formattedUrlEntity = FormattedUrlEntity(urlEntity.start,
                urlEntity.end, urlEntity.displayUrl, urlEntity.url, urlEntity.expandedUrl)
        assertThat(TweetTextLinkifier.isQuotedStatus(formattedUrlEntity), `is`(true))
    }

    @Test
    fun testIsVineCard_withVineUrl() {
        val urlEntity = UrlEntity("https://t.co/NdpqweoNbi",
                "https://vine.co/v/eVmZVXbeDK1", "vine.co/v/eVmZVXbeDK1", 1, 23)
        val formattedUrlEntity = FormattedUrlEntity(urlEntity.start,
                urlEntity.end, urlEntity.displayUrl, urlEntity.url, urlEntity.expandedUrl)

        assertThat(TweetTextLinkifier.isVineCard(formattedUrlEntity), `is`(true))
    }

    private fun setupPicTwitterEntities(): FormattedTweetText {
        val text = "first link is a pictwitter http://t.co/PFHCdlr4i0 " + "http://t.co/V3hLRdFdeN final text"

        val mediaEntity = MediaEntity(
                "http://t.co/PFHCdlr4i0",
                null,
                "pic.twitter.com/abc",
                27,
                49,
                0L,
                null,
                null,
                null,
                null,
                0L,
                null,
                "photo",
                null,
                ""
        )

        val urlEntity = UrlEntity(
                "http://t.co/V3hLRdFdeN",
                null,
                "example.com",
                50,
                72
        )

        val formattedText = FormattedTweetText()
        formattedText.text = text
        formattedText.urlEntities += FormattedUrlEntity.createFormattedUrlEntity(urlEntity)
        formattedText.mediaEntities += FormattedMediaEntity(mediaEntity)

        return formattedText
    }

    @Test
    fun testTrimEnd_withoutTrailingSpace() {
        assertThat(TweetTextLinkifier.trimEnd(BASE_TEXT), sameInstance<CharSequence>(BASE_TEXT))
    }

    @Test
    fun testTrimEnd_withTrailingSpace() {
        val result = TweetTextLinkifier.trimEnd("$BASE_TEXT\n\r\t ")
        assertThat(result, `is`<CharSequence>(BASE_TEXT))
        assertThat(result, not(sameInstance<CharSequence>(BASE_TEXT)))
    }

    /*
     * mergeAndSortEntities method
     */
    @Test
    fun testMergeAndSortEntities_emptyEntities() {
        val urls = ArrayList<FormattedUrlEntity>()
        val media = ArrayList<FormattedMediaEntity>()
        val hashtags = ArrayList<FormattedUrlEntity>()
        val mentions = ArrayList<FormattedUrlEntity>()
        val symbols = ArrayList<FormattedUrlEntity>()
        assertThat(TweetTextLinkifier.mergeAndSortEntities(urls, media, hashtags,
                mentions, symbols), `is`<List<FormattedUrlEntity>>(urls))
    }

    @Test
    fun testMergeAndSortEntities_sortUrlsAndMediaAndHashtags() {
        val urls = ArrayList<FormattedUrlEntity>()
        val urlEntity = TestFixtures.newUrlEntity(2, 5)
        val adjustedUrl = FormattedUrlEntity.createFormattedUrlEntity(
                urlEntity)
        urls.add(adjustedUrl)

        val media = ArrayList<FormattedMediaEntity>()
        val photo = TestFixtures.newMediaEntity(1, 5, "photo")
        val adjustedPhoto = FormattedMediaEntity(photo)
        media.add(adjustedPhoto)

        val hashtags = ArrayList<FormattedUrlEntity>()
        val hashtag = TestFixtures.newHashtagEntity("TwitterForGood", 0, 13)
        val adjustedHashtag = FormattedUrlEntity.createFormattedUrlEntity(hashtag)
        hashtags.add(adjustedHashtag)

        val mentions = ArrayList<FormattedUrlEntity>()
        val mention = TestFixtures.newMentionEntity("twitterdev", 0, 9)
        val adjustedMention = FormattedUrlEntity.createFormattedUrlEntity(mention)
        mentions.add(adjustedMention)

        val symbols = ArrayList<FormattedUrlEntity>()
        val symbol = TestFixtures.newSymbolEntity("TWTR", 0, 3)
        val adjustedSymbol = FormattedUrlEntity.createFormattedUrlEntity(symbol)
        symbols.add(adjustedSymbol)

        val combined = TweetTextLinkifier.mergeAndSortEntities(urls, media, hashtags, mentions, symbols)
        assertThat(combined[3], `is`<FormattedUrlEntity>(adjustedPhoto))
        assertThat(combined[4], `is`(adjustedUrl))
        assertThat(combined[0], `is`(adjustedHashtag))
        assertThat(combined[1], `is`(adjustedMention))
        assertThat(combined[2], `is`(adjustedSymbol))
    }

    companion object {
        private const val BASE_TEXT = "just setting up my twttr"
        private const val TEST_RLT_STRING = "ייִדיש משלי https://t.co/sfb4Id7esk\u200E"
    }
}
