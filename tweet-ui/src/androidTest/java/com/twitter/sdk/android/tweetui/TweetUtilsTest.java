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

import android.net.Uri;
import android.test.AndroidTestCase;

import com.twitter.sdk.android.core.TwitterCoreTestUtils;
import com.twitter.sdk.android.core.TwitterTestUtils;
import com.twitter.sdk.android.core.models.Card;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.TweetBuilder;
import com.twitter.sdk.android.core.models.TweetEntities;
import com.twitter.sdk.android.core.models.UserBuilder;

import org.junit.Assert;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

public class TweetUtilsTest extends AndroidTestCase {
    static final String NOT_STARTED_ERROR = "Must initialize Twitter before using getInstance()";
    private static final String A_FULL_PERMALINK =
            "https://twitter.com/jack/status/20?ref_src=twsrc%5Etwitterkit";
    private static final String A_PERMALINK_WITH_NO_SCREEN_NAME
            = "https://twitter.com/twitter_unknown/status/20?ref_src=twsrc%5Etwitterkit";
    private static final String A_VALID_SCREEN_NAME = "jack";
    private static final int A_VALID_TWEET_ID = 20;
    private static final int AN_INVALID_TWEET_ID = 0;

    @Override
    public void tearDown() throws Exception {
        TwitterTestUtils.resetTwitter();
        TwitterCoreTestUtils.resetTwitterCore();
        TweetUiTestUtils.resetTweetUi();

        super.tearDown();
    }

    public void testLoadTweet_beforeKitStart() {
        try {
            TweetUtils.loadTweet(TestFixtures.TEST_TWEET_ID, null);
            Assert.fail("IllegalStateException not thrown");
        } catch (IllegalStateException e) {
            Assert.assertThat(e.getMessage(), is(NOT_STARTED_ERROR));
        } catch (Exception ex) {
            Assert.fail();
        }
    }

    public void testLoadTweets_beforeKitStart() {
        try {
            TweetUtils.loadTweets(TestFixtures.TWEET_IDS, null);
            Assert.fail("IllegalStateException not thrown");
        } catch (IllegalStateException e) {
            Assert.assertThat(e.getMessage(), is(NOT_STARTED_ERROR));
        } catch (Exception ex) {
            Assert.fail();
        }
    }

    public void testIsTweetResolvable_nullTweet() {
        Assert.assertThat(TweetUtils.isTweetResolvable(null), is(false));
    }

    public void testIsTweetResolvable_hasInvalidIdAndNullUser() {
        final Tweet tweet = new TweetBuilder().build();
        Assert.assertThat(tweet.getUser(), nullValue());
        Assert.assertThat(TweetUtils.isTweetResolvable(tweet), is(false));
    }

    public void testIsTweetResolvable_hasValidIdAndNullUser() {
        final Tweet tweet = new TweetBuilder().setId(TestFixtures.TEST_TWEET_ID).build();
        Assert.assertThat(tweet.getUser(), nullValue());
        Assert.assertThat(TweetUtils.isTweetResolvable(tweet), is(false));
    }

    public void testIsTweetResolvable_hasInvalidIdAndUserWithNullScreenName() {
        final Tweet tweet = new TweetBuilder()
                .setUser(
                        new UserBuilder()
                                .setId(1)
                                .setName(null)
                                .setScreenName(null)
                                .setVerified(false)
                                .build())
                .build();
        Assert.assertThat(TweetUtils.isTweetResolvable(tweet), is(false));
    }

    public void testIsTweetResolvable_hasValidIdAndUserWithNullScreenName() {
        final Tweet tweet = new TweetBuilder()
                .setId(TestFixtures.TEST_TWEET_ID)
                .setUser(
                        new UserBuilder()
                                .setId(1)
                                .setName(null)
                                .setScreenName(null)
                                .setVerified(false)
                                .build()
                ).build();
        Assert.assertThat(TweetUtils.isTweetResolvable(tweet), is(false));
    }

    public void testIsTweetResolvable_hasInvalidIdAndUserWithEmptyScreenName() {
        final Tweet tweet = new TweetBuilder()
                .setUser(new UserBuilder()
                        .setId(1)
                        .setName(null)
                        .setScreenName("")
                        .setVerified(false)
                        .build())
                .build();
        Assert.assertThat(TweetUtils.isTweetResolvable(tweet), is(false));
    }

    public void testIsTweetResolvable_hasValidIdAndUserWithEmptyScreenName() {
        final Tweet tweet = new TweetBuilder()
                .setId(TestFixtures.TEST_TWEET_ID)
                .setUser(new UserBuilder()
                        .setId(1)
                        .setName(null)
                        .setScreenName("")
                        .setVerified(false)
                        .build())
                .build();
        Assert.assertThat(TweetUtils.isTweetResolvable(tweet), is(false));
    }

    public void testIsTweetResolvable_hasUserWithScreenNameAndValidId() {
        Assert.assertThat(TweetUtils.isTweetResolvable(TestFixtures.TEST_TWEET), is(true));
    }

    public void testGetPermalink_nullScreenNameValidId() {
        Assert.assertThat(TweetUtils.getPermalink(null, A_VALID_TWEET_ID).toString(), is(A_PERMALINK_WITH_NO_SCREEN_NAME));
    }

    public void testGetPermalink_validScreenNameZeroId() {
        Assert.assertThat(TweetUtils.getPermalink(A_VALID_SCREEN_NAME, AN_INVALID_TWEET_ID), nullValue());
    }

    public void testGetPermalink_validScreenNameAndId() {
        Assert.assertThat(TweetUtils.getPermalink(A_VALID_SCREEN_NAME, A_VALID_TWEET_ID).toString(), is(A_FULL_PERMALINK));
    }

    public void testGetPermalink_emptyScreenName() {
        final Uri permalink = TweetUtils.getPermalink("", 20);
        Assert.assertThat(permalink.toString(), is(A_PERMALINK_WITH_NO_SCREEN_NAME));
    }

    public void testGetDisplayTweet_nullTweet() {
        Assert.assertThat(TweetUtils.getDisplayTweet(null), nullValue());
    }

    public void testGetDisplayTweet_retweet() {
        Assert.assertThat(TweetUtils.getDisplayTweet(TestFixtures.TEST_RETWEET), is(TestFixtures.TEST_RETWEET.getRetweetedStatus()));
    }

    public void testGetDisplayTweet_nonRetweet() {
        Assert.assertThat(TweetUtils.getDisplayTweet(TestFixtures.TEST_TWEET), is(TestFixtures.TEST_TWEET));
    }

    public void testShowQuoteTweet() {
        final Tweet tweet = new TweetBuilder()
                .copy(TestFixtures.TEST_TWEET)
                .setQuotedStatus(TestFixtures.TEST_TWEET)
                .build();
        Assert.assertThat(TweetUtils.showQuoteTweet(tweet), is(true));
    }

    public void testShowQuoteTweet_withCardAndQuoteTweet() {
        final Tweet tweet = new TweetBuilder()
                .setQuotedStatus(TestFixtures.TEST_TWEET)
                .setCard(new Card(null, "Vine"))
                .setEntities(new TweetEntities(null, null, null, null, null))
                .build();
        Assert.assertThat(TweetUtils.showQuoteTweet(tweet), is(false));
    }

    public void testShowQuoteTweet_withMediaAndQuoteTweet() {
        final Tweet tweet = new TweetBuilder()
                .copy(TestFixtures.TEST_PHOTO_TWEET)
                .setQuotedStatus(TestFixtures.TEST_TWEET)
                .build();
        Assert.assertThat(TweetUtils.showQuoteTweet(tweet), is(false));
    }

    public void testShowQuoteTweet_nullEntity() {
        final Tweet tweet = new TweetBuilder()
                .copy(TestFixtures.TEST_PHOTO_TWEET)
                .setQuotedStatus(TestFixtures.TEST_TWEET)
                .setEntities(null)
                .build();
        Assert.assertThat(TweetUtils.showQuoteTweet(tweet), is(true));
    }
}
