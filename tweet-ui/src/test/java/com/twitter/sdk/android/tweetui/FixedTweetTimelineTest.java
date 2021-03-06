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

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public class FixedTweetTimelineTest {
    private static final Long ANY_ID = 1234L;
    private List<Tweet> fixedTweets = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        fixedTweets.add(TestFixtures.TEST_TWEET);
        fixedTweets.add(TestFixtures.TEST_RETWEET);
        fixedTweets.add(TestFixtures.TEST_PHOTO_TWEET);
    }

    @Test
    public void testConstructor() {
        final FixedTweetTimeline timeline = new FixedTweetTimeline(fixedTweets);
        Assert.assertThat(timeline.tweets, notNullValue());
        Assert.assertThat(timeline.tweets, is(fixedTweets));
    }

    @Test
    public void testConstructor_nullTweets() {
        final FixedTweetTimeline timeline = new FixedTweetTimeline(null);
        Assert.assertThat(timeline.tweets.isEmpty(), is(true));
    }

    @Test
    public void testNext_succeedsWithFixedTweets() {
        final FixedTweetTimeline timeline = new FixedTweetTimeline(fixedTweets);
        timeline.next(ANY_ID, new Callback<TimelineResult<Tweet>>() {
            @Override
            public void success(Result<TimelineResult<Tweet>> result) {
                Assert.assertThat(result.getData().getItems(), is(fixedTweets));
                Assert.assertThat(result.getData().getTimelineCursor().minPosition, is(TestFixtures.TEST_PHOTO_TWEET.getId()));
                Assert.assertThat(result.getData().getTimelineCursor().maxPosition, is(TestFixtures.TEST_TWEET.getId()));
                Assert.assertThat(result.getResponse(), nullValue());
            }
            @Override
            public void failure(TwitterException exception) {
                fail("Expected FixedTweetTimeline next to always succeed.");
            }
        });
    }

    @Test
    public void testNext_succeedsWithEmptyTweets() {
        final FixedTweetTimeline timeline = new FixedTweetTimeline(fixedTweets);
        timeline.previous(ANY_ID, new Callback<TimelineResult<Tweet>>() {
            @Override
            public void success(Result<TimelineResult<Tweet>> result) {
                Assert.assertThat(result.getData().getItems().isEmpty(), is(true));
                Assert.assertThat(result.getData().getTimelineCursor().maxPosition, nullValue());
                Assert.assertThat(result.getData().getTimelineCursor().minPosition, nullValue());
                Assert.assertThat(result.getResponse(), nullValue());
            }

            @Override
            public void failure(TwitterException exception) {
                fail("Expected FixedTweetTimeline previous to always succeed.");
            }
        });
    }

    /* Builder */
    @Test
    public void testBuilder() {
        final FixedTweetTimeline timeline = new FixedTweetTimeline.Builder()
                .setTweets(fixedTweets).build();
        Assert.assertThat(timeline.tweets, is(fixedTweets));
    }

    @Test
    public void testBuilder_empty() {
        final FixedTweetTimeline timeline = new FixedTweetTimeline.Builder().build();
        Assert.assertThat(timeline.tweets.isEmpty(), is(true));
    }
}
