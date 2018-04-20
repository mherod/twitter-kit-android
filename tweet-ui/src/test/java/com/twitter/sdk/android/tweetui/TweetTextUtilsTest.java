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

import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.TweetBuilder;
import com.twitter.sdk.android.core.models.UrlEntity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class TweetTextUtilsTest {
    private static final String UNESCAPED_TWEET_TEXT = ">Hello there <\"What is a?\" &;";
    private static final String ESCAPED_TWEET_TEXT
            = "&gt;Hello there &lt;&quot;What is a?&quot; &;";
    private static final String ESCAPED_TWEET_TEXT_WITH_EMOJI =
            "\ud83d\udc69\ud83c\udffd\u200d\ud83d\udcbb, community \ud83d\udc93 &amp; https://t.co/oCkwy2C80m";

    // test ported from:
    // twitter-android/app/src/androidTest/java/com/twitter/library/util/EntitiesTests.java
    // tests fixing up entity indices after unescaping html characters in tweet text
    @Test
    public void testFormat_singleEscaping() {
        final FormattedTweetText formattedTweetText = setupAdjustedTweet();
        final Tweet tweet = setupTweetToBeFormatted();
        TweetTextUtils.format(formattedTweetText, tweet);

        assertEquals(UNESCAPED_TWEET_TEXT, formattedTweetText.getText());
        assertEquals("Hello", 1, formattedTweetText.getUrlEntities().get(0).getStart());
        assertEquals("Hello", 5, formattedTweetText.getUrlEntities().get(0).getEnd());
        assertEquals("There", 7, formattedTweetText.getUrlEntities().get(1).getStart());
        assertEquals("There", 11, formattedTweetText.getUrlEntities().get(1).getEnd());

        assertEquals("What", 15, formattedTweetText.getUrlEntities().get(2).getStart());
        assertEquals("What", 18, formattedTweetText.getUrlEntities().get(2).getEnd());

        assertEquals("is", 20, formattedTweetText.getUrlEntities().get(3).getStart());
        assertEquals("is", 21, formattedTweetText.getUrlEntities().get(3).getEnd());

        assertEquals("a", 23, formattedTweetText.getUrlEntities().get(4).getStart());
        assertEquals("a", 23, formattedTweetText.getUrlEntities().get(4).getEnd());
    }

    @Test
    public void testFormat_htmlEntityEdgeCases() {
        final FormattedTweetText formattedTweetText = new FormattedTweetText();

        Tweet tweet = new TweetBuilder().setText("&amp;").build();
        TweetTextUtils.format(formattedTweetText, tweet);
        assertEquals("&", formattedTweetText.getText());

        tweet = new TweetBuilder().setText("&#;").build();
        TweetTextUtils.format(formattedTweetText, tweet);
        assertEquals("&#;", formattedTweetText.getText());

        tweet = new TweetBuilder().setText("&#34;").build();
        TweetTextUtils.format(formattedTweetText, tweet);
        assertEquals("\"", formattedTweetText.getText());

        tweet = new TweetBuilder().setText("&#x22;").build();
        TweetTextUtils.format(formattedTweetText, tweet);
        assertEquals("\"", formattedTweetText.getText());

        tweet = new TweetBuilder().setText("&lt; & Larry &gt; &").build();
        TweetTextUtils.format(formattedTweetText, tweet);
        assertEquals("< & Larry > &", formattedTweetText.getText());

        tweet = new TweetBuilder().setText("&&amp;").build();
        TweetTextUtils.format(formattedTweetText, tweet);
        assertEquals("&&", formattedTweetText.getText());

        tweet = new TweetBuilder().setText("&&&&&&&&amp;").build();
        TweetTextUtils.format(formattedTweetText, tweet);
        assertEquals("&&&&&&&&", formattedTweetText.getText());

        tweet = new TweetBuilder().setText("&&&&gt&&lt&&amplt;").build();
        TweetTextUtils.format(formattedTweetText, tweet);
        assertEquals("&&&&gt&&lt&&amplt;", formattedTweetText.getText());
    }

    @Test
    public void testFormat_withEmojiAndEscapedHtml() {
        final FormattedTweetText formattedTweetText = new FormattedTweetText();
        final UrlEntity url = TestFixtures.newUrlEntity(24, 47);
        formattedTweetText.getUrlEntities().add(FormattedUrlEntity.Companion.createFormattedUrlEntity(url));

        final Tweet tweet = new TweetBuilder()
                .setText(ESCAPED_TWEET_TEXT_WITH_EMOJI)
                .build();
        TweetTextUtils.format(formattedTweetText, tweet);

        assertEquals(24, formattedTweetText.getUrlEntities().get(0).getStart());
        assertEquals(47, formattedTweetText.getUrlEntities().get(0).getEnd());
    }

    private Tweet setupTweetToBeFormatted() {
        return new TweetBuilder().setText(ESCAPED_TWEET_TEXT).build();
    }

    private FormattedTweetText setupAdjustedTweet() {
        final FormattedTweetText formattedTweetText = new FormattedTweetText();

        UrlEntity url = TestFixtures.newUrlEntity(4, 8);
        // Hello
        formattedTweetText.getUrlEntities().add(FormattedUrlEntity.Companion.createFormattedUrlEntity(url));

        // There
        url = TestFixtures.newUrlEntity(10, 14);
        formattedTweetText.getUrlEntities().add(FormattedUrlEntity.Companion.createFormattedUrlEntity(url));

        // What
        url = TestFixtures.newUrlEntity(26, 29);
        formattedTweetText.getUrlEntities().add(FormattedUrlEntity.Companion.createFormattedUrlEntity(url));

        // is
        url = TestFixtures.newUrlEntity(31, 32);
        formattedTweetText.getUrlEntities().add(FormattedUrlEntity.Companion.createFormattedUrlEntity(url));

        // a
        url = TestFixtures.newUrlEntity(34, 34);
        formattedTweetText.getUrlEntities().add(FormattedUrlEntity.Companion.createFormattedUrlEntity(url));

        return formattedTweetText;
    }
}
