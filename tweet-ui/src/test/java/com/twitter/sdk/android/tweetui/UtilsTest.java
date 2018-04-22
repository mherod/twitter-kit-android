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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
public class UtilsTest {

    @Test
    public void testNumberOrDefault_validNumber() {
        Assert.assertThat(Utils.numberOrDefault("123", -1L), is(Long.valueOf(123)));
    }

    @Test
    public void testNumberOrDefault_invalidNumber() {
        Assert.assertThat(Utils.numberOrDefault("abc", -1L), is(Long.valueOf(-1L)));
    }

    @Test
    public void testStringOrEmpty_validString() {
        Assert.assertThat(Utils.stringOrEmpty("string"), is("string"));
    }

    @Test
    public void testStringOrEmpty_nullString() {
        Assert.assertThat(Utils.stringOrEmpty(null), is(""));
    }

    @Test
    public void testStringOrDefault_validString() {
        Assert.assertThat(Utils.stringOrDefault("string", "default"), is("string"));
    }

    @Test
    public void testStringOrDefault_nullString() {
        Assert.assertThat(Utils.stringOrDefault(null, "default"), is("default"));
    }

    @Test
    public void testCharSeqOrEmpty_validCharSeq() {
        Assert.assertThat(Utils.charSeqOrEmpty("string"), is("string"));
    }

    @Test
    public void testCharSeqOrEmpty_nullCharSeq() {
        Assert.assertThat(Utils.charSeqOrEmpty(null), is(""));
    }

    @Test
    public void testCharSeqOrDefault_validCharSeq() {
        Assert.assertThat(Utils.charSeqOrDefault("string", "default"), is("string"));
    }

    @Test
    public void testCharSeqOrDefault_nullCharSeq() {
        Assert.assertThat(Utils.charSeqOrDefault(null, "default"), is("default"));
    }

    @Test
    public void testSortTweets() {
        final List<Long> requestedIds = TestFixtures.TWEET_IDS;
        final List<Tweet> tweets = new ArrayList<>();
        tweets.addAll(TestFixtures.UNORDERED_TWEETS);
        final List<Tweet> ordered = Utils.orderTweets(requestedIds, tweets);
        Assert.assertThat(ordered, is(TestFixtures.ORDERED_TWEETS));
    }

    // Tweet results will match the requested Tweet ids, duplicate requested ids duplicate Tweets.
    @Test
    public void testSortTweets_duplicateRequestedIds() {
        final List<Long> requestedIds = TestFixtures.DUPLICATE_TWEET_IDS;
        final List<Tweet> tweets = new ArrayList<>();
        tweets.addAll(TestFixtures.UNORDERED_TWEETS);
        final List<Tweet> ordered = Utils.orderTweets(requestedIds, tweets);
        Assert.assertThat(ordered, is(TestFixtures.ORDERED_DUPLICATE_TWEETS));
    }

    // Tweet results will match the requested Tweet ids, duplicate results ignored.
    @Test
    public void testSortTweets_duplicateTweets() {
        final List<Long> requestedIds = TestFixtures.TWEET_IDS;
        final List<Tweet> tweets = new ArrayList<>();
        tweets.addAll(TestFixtures.UNORDERED_DUPLICATE_TWEETS);
        final List<Tweet> ordered = Utils.orderTweets(requestedIds, tweets);
        Assert.assertThat(ordered, is(TestFixtures.ORDERED_TWEETS));
    }

    @Test
    public void testSortTweets_missingTweets() {
        final List<Long> requestedIds = TestFixtures.TWEET_IDS;
        final List<Tweet> tweets = new ArrayList<>();
        tweets.addAll(TestFixtures.UNORDERED_MISSING_TWEETS);
        final List<Tweet> ordered = Utils.orderTweets(requestedIds, tweets);
        Assert.assertThat(ordered, is(TestFixtures.ORDERED_MISSING_TWEETS));
    }

    // Tweet result with an extra, unrequested Tweet, not included in the result.
    @Test
    public void testSortTweets_extraTweetsFirst() {
        final List<Long> requestedIds = TestFixtures.TWEET_IDS;
        final List<Tweet> tweets = new ArrayList<>();
        tweets.addAll(TestFixtures.UNORDERED_TWEETS);
        tweets.add(TestFixtures.TEST_TWEET);

        final List<Tweet> ordered = Utils.orderTweets(requestedIds, tweets);
        Assert.assertThat(ordered, is(TestFixtures.ORDERED_TWEETS));
    }
}

