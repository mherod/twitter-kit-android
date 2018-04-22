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
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class TweetEntitiesTest {

    private static final int EXPECTED_URLS_SIZE = 1;
    private static final int EXPECTED_USER_MENTIONS_SIZE = 1;
    private static final int EXPECTED_MEDIA_SIZE = 1;
    private static final int EXPECTED_HASHTAGS_SIZE = 1;
    private static final int EXPECTED_SYMBOLS_SIZE = 1;

    @Rule
    public final TestResources testResources = new TestResources();

    private Gson gson;

    @Before
    public void setUp() throws Exception {
        gson = new Gson();
    }

    @Test
    public void testConstructor_nullParameters() {
        try {
            final TweetEntities entities = new TweetEntities(null, null, null, null, null);
            Assert.assertThat(entities.getUrls(), is(Collections.EMPTY_LIST));
            Assert.assertThat(entities.getUserMentions(), is(Collections.EMPTY_LIST));
            Assert.assertThat(entities.getMedia(), is(Collections.EMPTY_LIST));
            Assert.assertThat(entities.getHashtags(), is(Collections.EMPTY_LIST));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testDeserialization() {
        JsonReader reader = null;
        try {
            reader = new JsonReader(new InputStreamReader(testResources
                    .getAsStream("model_tweetentities.json")));
            final TweetEntities tweetEntities = gson.fromJson(reader, TweetEntities.class);
            // We simply assert that we parsed it successfully and rely on our other unit tests to
            // verify parsing of the individual objects.
            Assert.assertThat(tweetEntities.getUrls().size(), is(EXPECTED_URLS_SIZE));
            Assert.assertThat(tweetEntities.getUserMentions().size(), is(EXPECTED_USER_MENTIONS_SIZE));
            Assert.assertThat(tweetEntities.getMedia().size(), is(EXPECTED_MEDIA_SIZE));
            Assert.assertThat(tweetEntities.getHashtags().size(), is(EXPECTED_HASHTAGS_SIZE));
            Assert.assertThat(tweetEntities.getSymbols().size(), is(EXPECTED_SYMBOLS_SIZE));
        } finally {
            CommonUtils.closeQuietly(reader);
        }
    }

    @Test
    public void testDeserialization_noEntities() {
        final TweetEntities tweetEntities = gson.fromJson("{\"urls\":[]}", TweetEntities.class);
        // We simply assert that we parsed it successfully and rely on our other unit tests to
        // verify parsing of the individual objects.
        Assert.assertThat(tweetEntities.getUrls(), notNullValue());
        Assert.assertThat(tweetEntities.getUrls().size(), is(0));
        Assert.assertThat(tweetEntities.getUserMentions(), notNullValue());
        Assert.assertThat(tweetEntities.getUserMentions().size(), is(0));
        Assert.assertThat(tweetEntities.getMedia(), notNullValue());
        Assert.assertThat(tweetEntities.getMedia().size(), is(0));
        Assert.assertThat(tweetEntities.getHashtags(), notNullValue());
        Assert.assertThat(tweetEntities.getHashtags().size(), is(0));
        Assert.assertThat(tweetEntities.getSymbols(), notNullValue());
        Assert.assertThat(tweetEntities.getSymbols().size(), is(0));
    }
}
