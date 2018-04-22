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

package com.twitter.sdk.android.core.models;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.twitter.sdk.android.core.TestResources;
import com.twitter.sdk.android.core.internal.CommonUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.InputStreamReader;

import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
public class TwitterCollectionTest {
    private static final int EXPECTED_NUM_USERS = 2;
    private static final int EXPECTED_NUM_TWEETS = 3;
    private static final Long EXPECTED_TWEET_ID_FIRST = 504032379045179393L;
    private static final Long EXPECTED_TWEET_ID_SECOND = 532654992071852032L;
    private static final Long EXPECTED_USER_ID_FIRST = 2244994945L;
    private static final String EXPECTED_USER_SCREEN_NAME_FIRST = "TwitterDev";

    private static final String EXPECTED_TIMELINE_ID = "custom-539487832448843776";
    private static final Long EXPECTED_MAX_POSITION = 371578415352947200L;
    private static final Long EXPECTED_MIN_POSITION = 371578380871797248L;

    @Rule
    public final TestResources testResources = new TestResources();

    private Gson gson;

    @Before
    public void setUp() throws Exception {
        gson = new Gson();
    }

    @Test
    public void testDeserialization() {
        JsonReader reader = null;
        try {
            reader = new JsonReader(new InputStreamReader(testResources
                    .getAsStream("model_twitter_collection.json")));
            final TwitterCollection twitterCollection
                    = gson.fromJson(reader, TwitterCollection.class);

            // check collection decomposed object maps in objects field
            Assert.assertThat(twitterCollection.contents.tweetMap.size(), is(EXPECTED_NUM_TWEETS));
            Assert.assertThat(twitterCollection.contents.userMap.size(), is(EXPECTED_NUM_USERS));
            Assert.assertThat(twitterCollection.contents.tweetMap.containsKey(EXPECTED_TWEET_ID_FIRST), is(true));
            Assert.assertThat(twitterCollection.contents.tweetMap.get(EXPECTED_TWEET_ID_FIRST).getId(), is(EXPECTED_TWEET_ID_FIRST));
            Assert.assertThat(twitterCollection.contents.tweetMap.containsKey(EXPECTED_TWEET_ID_SECOND), is(true));
            Assert.assertThat(twitterCollection.contents.userMap.containsKey(EXPECTED_USER_ID_FIRST), is(true));
            Assert.assertThat(twitterCollection.contents.userMap.get(EXPECTED_USER_ID_FIRST).getScreenName(), is(EXPECTED_USER_SCREEN_NAME_FIRST));

            // check object references and contextual info in response field
            Assert.assertThat(twitterCollection.metadata.timelineId, is(EXPECTED_TIMELINE_ID));
            Assert.assertThat(twitterCollection.metadata.position.maxPosition, is(EXPECTED_MAX_POSITION));
            Assert.assertThat(twitterCollection.metadata.position.minPosition, is(EXPECTED_MIN_POSITION));
            Assert.assertThat(twitterCollection.metadata.timelineItems.size(), is(EXPECTED_NUM_TWEETS));
            Assert.assertThat(twitterCollection.metadata.timelineItems.get(0).tweetItem.id, is(EXPECTED_TWEET_ID_FIRST));
            Assert.assertThat(twitterCollection.metadata.timelineItems.get(1).tweetItem.id, is(EXPECTED_TWEET_ID_SECOND));
        } finally {
            CommonUtils.closeQuietly(reader);
        }
    }
}
