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
public class TweetTest {

    private static final String EXPECTED_CREATED_AT = "Wed Jun 06 20:07:10 +0000 2012";
    private static final long EXPECTED_ID = 210462857140252672L;
    private static final String EXPECTED_TEXT = "Along with our new #Twitterbird, we've also updated our Display Guidelines: https://t.co/Ed4omjYs  ^JC";
    private static final Integer[] EXPECTED_DISPLAY_TEXT_RANGE = {0, 102};
    private static final String EXPECTED_WITHHELD_IN_COUNTRIES = "XY";
    private static final long EXPECTED_QUOTED_STATUS_ID = 745634624466911232L;

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
                    .getAsStream("model_tweet.json")));
            final Tweet tweet = gson.fromJson(reader, Tweet.class);
            // We simply assert that we parsed it successfully and rely on our other unit tests to
            // verify parsing of the individual objects.
            Assert.assertThat(tweet.getCreatedAt(), is(EXPECTED_CREATED_AT));
            Assert.assertThat(tweet.getEntities(), notNullValue());
            Assert.assertThat(tweet.getEntities().getHashtags(), notNullValue());
            Assert.assertThat(tweet.getEntities().getMedia(), notNullValue());
            Assert.assertThat(tweet.getEntities().getSymbols(), notNullValue());
            Assert.assertThat(tweet.getEntities().getUrls(), notNullValue());
            Assert.assertThat(tweet.getEntities().getUserMentions(), notNullValue());
            Assert.assertThat(tweet.getUser(), notNullValue());
            Assert.assertThat(tweet.getRetweeted(), is(true));
            Assert.assertThat(tweet.getId(), is(EXPECTED_ID));
            Assert.assertThat(tweet.getId(), is(EXPECTED_ID));
            Assert.assertThat(tweet.getText(), notNullValue());
            Assert.assertThat(tweet.getText(), is(EXPECTED_TEXT));
            Assert.assertThat(tweet.getDisplayTextRange(), notNullValue());
            Assert.assertThat(tweet.getTruncated(), is(false));
            Assert.assertThat(tweet.getDisplayTextRange().toArray(), is(EXPECTED_DISPLAY_TEXT_RANGE));
            Assert.assertThat(tweet.getWithheldInCountries(), notNullValue());
            Assert.assertThat(tweet.getWithheldInCountries().size(), is(1));
            Assert.assertThat(tweet.getWithheldInCountries().get(0), is(EXPECTED_WITHHELD_IN_COUNTRIES));
        } finally {
            CommonUtils.closeQuietly(reader);
        }
    }

    @Test
    public void testQuotedTweetDeserialization() {
        JsonReader reader = null;
        try {
            reader = new JsonReader(new InputStreamReader(testResources
                .getAsStream("model_quoted_tweet.json")));
            final Tweet tweet = gson.fromJson(reader, Tweet.class);
            Assert.assertThat(tweet.getQuotedStatusId(), is(EXPECTED_QUOTED_STATUS_ID));
            Assert.assertThat(tweet.getQuotedStatusIdStr(), is(String.valueOf(EXPECTED_QUOTED_STATUS_ID)));
            Assert.assertThat(tweet.getQuotedStatus(), notNullValue());
        } finally {
            CommonUtils.closeQuietly(reader);
        }
    }
}
