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

package com.twitter.sdk.android.tweetcomposer;

import android.test.AndroidTestCase;

import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCoreTestUtils;
import com.twitter.sdk.android.core.TwitterTestUtils;

import org.junit.Assert;

import java.util.concurrent.ThreadPoolExecutor;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;

public class TweetComposerTest extends AndroidTestCase {
    private static final String TWITTER_NOT_INIT_ERROR_MSG =
            "Must initialize Twitter before using getInstance()";
    private TweetComposer tweetComposer;

    public void setUp() throws Exception {
        super.setUp();
        Twitter.initialize(new TwitterConfig.Builder(getContext())
                .executorService(mock(ThreadPoolExecutor.class))
                .build());
        tweetComposer = new TweetComposer();
        TweetComposer.instance = tweetComposer;
    }

    public void tearDown() throws Exception {
        TwitterTestUtils.resetTwitter();
        TwitterCoreTestUtils.resetTwitterCore();
        TweetComposer.instance = null;

        super.tearDown();
    }

    public void testGetVersion() {
        final String version = BuildConfig.VERSION_NAME + "." + BuildConfig.BUILD_NUMBER;
        Assert.assertThat(tweetComposer.getVersion(), is(version));
    }

    public void testGetIdentifier() {
        final String identifier = BuildConfig.GROUP + ":" + BuildConfig.ARTIFACT_ID;
        Assert.assertThat(tweetComposer.getIdentifier(), is(identifier));
    }

    public void testGetInstance_twitterNotInitialized() {
        try {
            TwitterTestUtils.resetTwitter();
            TwitterCoreTestUtils.resetTwitterCore();
            TweetComposer.instance = null;

            TweetComposer.getInstance();
            Assert.fail("Should fail if Twitter is not initialized");
        } catch (IllegalStateException e) {
            Assert.assertThat(e.getMessage(), is(TWITTER_NOT_INIT_ERROR_MSG));
        }
    }
}
