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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.Matchers.*;

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

        Assert.assertThat(formattedTweetText.getText(), is(UNESCAPED_TWEET_TEXT));
        Assert.assertThat("Hello", formattedTweetText.getUrlEntities().get(0).getStart(), is(1));
        Assert.assertThat("Hello", formattedTweetText.getUrlEntities().get(0).getEnd(), is(5));
        Assert.assertThat("There", formattedTweetText.getUrlEntities().get(1).getStart(), is(7));
        Assert.assertThat("There", formattedTweetText.getUrlEntities().get(1).getEnd(), is(11));

        Assert.assertThat("What", formattedTweetText.getUrlEntities().get(2).getStart(), is(15));
        Assert.assertThat("What", formattedTweetText.getUrlEntities().get(2).getEnd(), is(18));

        Assert.assertThat("is", formattedTweetText.getUrlEntities().get(3).getStart(), is(20));
        Assert.assertThat("is", formattedTweetText.getUrlEntities().get(3).getEnd(), is(21));

        Assert.assertThat("a", formattedTweetText.getUrlEntities().get(4).getStart(), is(23));
        Assert.assertThat("a", formattedTweetText.getUrlEntities().get(4).getEnd(), is(23));
    }

    @Test
    public void testFormat_htmlEntityEdgeCases() {
        final FormattedTweetText formattedTweetText = new FormattedTweetText();

        Tweet tweet = new TweetBuilder().setText("&amp;").build();
        TweetTextUtils.format(formattedTweetText, tweet);
        Assert.assertThat(formattedTweetText.getText(), is("&"));

        tweet = new TweetBuilder().setText("&#;").build();
        TweetTextUtils.format(formattedTweetText, tweet);
        Assert.assertThat(formattedTweetText.getText(), is("&#;"));

        tweet = new TweetBuilder().setText("&#34;").build();
        TweetTextUtils.format(formattedTweetText, tweet);
        Assert.assertThat(formattedTweetText.getText(), is("\""));

        tweet = new TweetBuilder().setText("&#x22;").build();
        TweetTextUtils.format(formattedTweetText, tweet);
        Assert.assertThat(formattedTweetText.getText(), is("\""));

        tweet = new TweetBuilder().setText("&lt; & Larry &gt; &").build();
        TweetTextUtils.format(formattedTweetText, tweet);
        Assert.assertThat(formattedTweetText.getText(), is("< & Larry > &"));

        tweet = new TweetBuilder().setText("&&amp;").build();
        TweetTextUtils.format(formattedTweetText, tweet);
        Assert.assertThat(formattedTweetText.getText(), is("&&"));

        tweet = new TweetBuilder().setText("&&&&&&&&amp;").build();
        TweetTextUtils.format(formattedTweetText, tweet);
        Assert.assertThat(formattedTweetText.getText(), is("&&&&&&&&"));

        tweet = new TweetBuilder().setText("&&&&gt&&lt&&amplt;").build();
        TweetTextUtils.format(formattedTweetText, tweet);
        Assert.assertThat(formattedTweetText.getText(), is("&&&&gt&&lt&&amplt;"));
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

        Assert.assertThat(formattedTweetText.getUrlEntities().get(0).getStart(), is(24));
        Assert.assertThat(formattedTweetText.getUrlEntities().get(0).getEnd(), is(47));
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
