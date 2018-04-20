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
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.InputStreamReader;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(RobolectricTestRunner.class)
public class ConfigurationTest {
    @Rule
    public final TestResources testResources = new TestResources();

    private static final int TEST_DM_CHAR_LIMIT = 10000;
    private static final int TEST_SHORT_URL_LENGTH = 23;
    private static final int TEST_NUN_NON_USER_NAME = 85;
    private static final long TEST_PHOTO_SIZE_LIMIT = 3145728;
    private static final MediaEntity.Size TEST_SIZE_THUMB = new MediaEntity.Size(150, 150, "crop");
    private static final MediaEntity.Size TEST_SIZE_SMALL = new MediaEntity.Size(340, 480, "fit");
    private static final MediaEntity.Size TEST_SIZE_MEDIUM = new MediaEntity.Size(600, 1200, "fit");
    private static final MediaEntity.Size TEST_SIZE_LARGE = new MediaEntity.Size(1024, 2048, "fit");

    @Test
    public void testDeserialization() {
        JsonReader reader = null;
        try {
            reader = new JsonReader(new InputStreamReader(testResources
                    .getAsStream("model_configuration.json")));
            final Configuration configuration = new Gson().fromJson(reader, Configuration.class);
            Assert.assertThat(configuration.getDmTextCharacterLimit(), is(TEST_DM_CHAR_LIMIT));
            Assert.assertThat(configuration.getNonUsernamePaths(), notNullValue());
            Assert.assertThat(configuration.getNonUsernamePaths().size(), is(TEST_NUN_NON_USER_NAME));
            Assert.assertThat(configuration.getPhotoSizeLimit(), is(TEST_PHOTO_SIZE_LIMIT));
            Assert.assertThat(configuration.getPhotoSizes(), notNullValue());
            MediaEntityTest.assertSizeEquals(TEST_SIZE_THUMB, configuration.getPhotoSizes().thumb);
            MediaEntityTest.assertSizeEquals(TEST_SIZE_SMALL, configuration.getPhotoSizes().small);
            MediaEntityTest.assertSizeEquals(TEST_SIZE_MEDIUM, configuration.getPhotoSizes().medium);
            MediaEntityTest.assertSizeEquals(TEST_SIZE_LARGE, configuration.getPhotoSizes().large);
            Assert.assertThat(configuration.getShortUrlLengthHttps(), is(TEST_SHORT_URL_LENGTH));
        } finally {
            CommonUtils.closeQuietly(reader);
        }
    }
}
