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

import org.hamcrest.number.OrderingComparison;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.InputStreamReader;

import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
public class UserTest {

    private static final long EXPECTED_ID = 795649L;
    private static final String EXPECTED_NAME = "Ryan Sarver";
    private static final String EXPECTED_SCREEN_NAME = "rsarver";
    private static final String EXPECTED_PROFILE_IMAGE_URL_HTTPS
            = "https://si0.twimg.com/profile_images/1777569006/image1327396628_normal.png";
    private static final boolean EXPECTED_VERIFIED = false;
    private static final String EXPECTED_WITHHELD_IN_COUNTRIES = "XY";

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
                    .getAsStream("model_user.json")));
            final User user = gson.fromJson(reader, User.class);
            // We simply assert that we parsed it successfully and rely on our other unit tests to
            // verify parsing of the individual objects.
            Assert.assertThat(user.getId(), is(EXPECTED_ID));
            Assert.assertThat(user.getId(), is(EXPECTED_ID));
            Assert.assertThat(user.getName(), is(EXPECTED_NAME));
            Assert.assertThat(user.getEntities().url.urls.size(), OrderingComparison.greaterThan(0));
            Assert.assertThat(user.getEntities().description.urls.isEmpty(), is(true));
            Assert.assertThat(user.getScreenName(), is(EXPECTED_SCREEN_NAME));
            Assert.assertThat(user.getProfileImageUrlHttps(), is(EXPECTED_PROFILE_IMAGE_URL_HTTPS));
            Assert.assertThat(user.getVerified(), is(EXPECTED_VERIFIED));
            Assert.assertThat(user.getStatus(), notNullValue());
            Assert.assertThat(user.getWithheldInCountries(), notNullValue());
            Assert.assertThat(user.getWithheldInCountries().size(), is(1));
            Assert.assertThat(user.getWithheldInCountries().get(0), is(EXPECTED_WITHHELD_IN_COUNTRIES));
        } finally {
            CommonUtils.closeQuietly(reader);
        }
    }
}
