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

package com.twitter.sdk.android.core;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.util.concurrent.ExecutorService;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class TwitterTest {
    private static final String CONSUMER_KEY = "com.twitter.sdk.android.CONSUMER_KEY";
    private static final String CONSUMER_SECRET = "com.twitter.sdk.android.CONSUMER_SECRET";
    private static final String TEST_PACKAGE_NAME = "com.twitter.sdk.android.test";
    private static final String TEST_PATH_SUFFIX = ".TwitterKit" + File.separator + TEST_PACKAGE_NAME;

    @Mock
    Context mockContext;
    @Mock
    Application mockApplication;
    @Mock
    ExecutorService mockExecutorService;
    @Mock
    TwitterAuthConfig mockTwitterAuthConfig;
    @Mock
    Logger mockLogger;
    @Mock
    Resources mockResources;

    @Before
    @SuppressWarnings("ResourceType")
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(mockResources.getIdentifier(CONSUMER_KEY, "string", TEST_PACKAGE_NAME)).thenReturn(1);
        when(mockResources.getIdentifier(CONSUMER_SECRET, "string", TEST_PACKAGE_NAME))
                .thenReturn(2);
        when(mockResources.getString(1)).thenReturn(TestFixtures.KEY);
        when(mockResources.getString(2)).thenReturn(TestFixtures.SECRET);

        when(mockApplication.getResources()).thenReturn(mockResources);
        when(mockApplication.getApplicationInfo()).thenReturn(new ApplicationInfo());
        when(mockApplication.getApplicationContext()).thenReturn(mockApplication);
        when(mockApplication.getPackageName()).thenReturn(TEST_PACKAGE_NAME);

        when(mockContext.getApplicationContext()).thenReturn(mockApplication);
    }

    @After
    public void tearDown() {
        // Reset static instance for next test
        Twitter.instance = null;
    }

    @Test
    public void testInitialize_withConfig() {
        final TwitterConfig config = new TwitterConfig
                .Builder(mockContext)
                .executorService(mockExecutorService)
                .logger(mockLogger)
                .twitterAuthConfig(mockTwitterAuthConfig)
                .debug(true)
                .build();

        Twitter.initialize(config);

        Assert.assertThat(Twitter.getInstance().getExecutorService(), is(mockExecutorService));
        Assert.assertThat(Twitter.getLogger(), is(mockLogger));
        Assert.assertThat(Twitter.getInstance().getTwitterAuthConfig(), is(mockTwitterAuthConfig));
        Assert.assertThat(Twitter.getInstance().getIdManager(), notNullValue());
        Assert.assertThat(Twitter.getInstance().getActivityLifecycleManager(), notNullValue());
        Assert.assertThat(Twitter.getInstance().isDebug(), is(true));

        verifyContext(Twitter.getInstance().getContext(TEST_PACKAGE_NAME));
    }

    @Test
    public void testInitialize_withDefaults() {
        Twitter.initialize(mockContext);

        Assert.assertThat(Twitter.getInstance().getExecutorService(), notNullValue());
        Assert.assertThat(Twitter.getInstance().getIdManager(), notNullValue());
        Assert.assertThat(Twitter.getInstance().getActivityLifecycleManager(), notNullValue());
        Assert.assertThat(Twitter.getLogger(), is(Twitter.DEFAULT_LOGGER));
        Assert.assertThat(Twitter.getInstance().isDebug(), is(false));

        final TwitterAuthConfig authConfig = Twitter.getInstance().getTwitterAuthConfig();
        Assert.assertThat(authConfig, notNullValue());
        Assert.assertThat(authConfig.getConsumerKey(), is(TestFixtures.KEY));
        Assert.assertThat(authConfig.getConsumerSecret(), is(TestFixtures.SECRET));

        verifyContext(Twitter.getInstance().getContext(TEST_PACKAGE_NAME));
    }

    private void verifyContext(Context context) {
        Assert.assertThat(context, notNullValue());
        Assert.assertThat(context instanceof TwitterContext, is(true));
        Assert.assertThat(context.getFilesDir().getAbsolutePath().endsWith(TEST_PATH_SUFFIX), is(true));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetInstance_shouldThrowWhenNotInitialized() {
        Twitter.getInstance();
    }
}
