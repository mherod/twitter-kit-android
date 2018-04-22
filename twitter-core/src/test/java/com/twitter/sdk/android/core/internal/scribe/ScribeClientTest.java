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

package com.twitter.sdk.android.core.internal.scribe;

import android.content.Context;

import com.twitter.sdk.android.core.GuestSession;
import com.twitter.sdk.android.core.GuestSessionProvider;
import com.twitter.sdk.android.core.SessionManager;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.internal.IdManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class ScribeClientTest {

    private static final long TEST_USER_ID = 12L;
    private static final String ANY_PATH_VERSION = "version";
    private static final String ANY_PATH_TYPE = "type";

    private ScribeClient scribeClient;

    @Before
    public void setUp() throws Exception {
        scribeClient = new ScribeClient(RuntimeEnvironment.application,
                mock(ScheduledExecutorService.class), mock(ScribeConfig.class),
                mock(ScribeEvent.Transform.class), mock(TwitterAuthConfig.class),
                mock(SessionManager.class), mock(GuestSessionProvider.class),
                mock(IdManager.class));
    }

    @Test
    public void testScribe() {
        final ScribeHandler mockHandler = mock(ScribeHandler.class);
        final ScribeEvent mockScribeEvent = mock(ScribeEvent.class);

        scribeClient.scribeHandlers.put(GuestSession.LOGGED_OUT_USER_ID, mockHandler);
        scribeClient.scribe(mockScribeEvent, GuestSession.LOGGED_OUT_USER_ID);

        verify(mockHandler).scribe(mockScribeEvent);
    }

    @Test
    public void testScribeAndFlush() {
        final ScribeHandler mockHandler = mock(ScribeHandler.class);
        final ScribeEvent mockScribeEvent = mock(ScribeEvent.class);

        scribeClient.scribeHandlers.put(GuestSession.LOGGED_OUT_USER_ID, mockHandler);
        scribeClient.scribeAndFlush(mockScribeEvent, GuestSession.LOGGED_OUT_USER_ID);

        verify(mockHandler).scribeAndFlush(mockScribeEvent);
    }

    @Test
    public void testGetScribeHandler() throws IOException {
        final ScribeHandler loggedOutScribeHandler
                = scribeClient.getScribeHandler(GuestSession.LOGGED_OUT_USER_ID);
        Assert.assertThat(loggedOutScribeHandler, notNullValue());
        // Verify that asking for a scribe handler for the same owner id results in the same one
        // being returned.
        Assert.assertThat(scribeClient.getScribeHandler(GuestSession.LOGGED_OUT_USER_ID), is(loggedOutScribeHandler));

        // Verify that different scribe handlers are returned for the different user ids.
        final ScribeHandler testUserScribeHandler
                = scribeClient.getScribeHandler(TEST_USER_ID);
        Assert.assertThat(testUserScribeHandler, notNullValue());
        Assert.assertThat(testUserScribeHandler, not(sameInstance(loggedOutScribeHandler)));
    }

    @Test
    public void testGetScribeStrategy_scribeEnabled() {
        final ScribeConfig config = new ScribeConfig(true, ScribeConfig.BASE_URL, ANY_PATH_VERSION,
                ANY_PATH_TYPE, null, null, ScribeConfig.DEFAULT_MAX_FILES_TO_KEEP,
                ScribeConfig.DEFAULT_SEND_INTERVAL_SECONDS);
        scribeClient = new ScribeClient(mock(Context.class), mock(ScheduledExecutorService.class),
                config, mock(ScribeEvent.Transform.class), mock(TwitterAuthConfig.class),
                mock(SessionManager.class), mock(GuestSessionProvider.class),
                mock(IdManager.class));

        final EventsStrategy<ScribeEvent> scribeStrategy
                = scribeClient.getScribeStrategy(GuestSession.LOGGED_OUT_USER_ID, null);
        Assert.assertThat(scribeStrategy instanceof EnabledScribeStrategy, is(true));
    }

    @Test
    public void testGetScribeStrategy_scribeDisabled() {
        final ScribeConfig config = new ScribeConfig(false, ScribeConfig.BASE_URL, ANY_PATH_VERSION,
                ANY_PATH_TYPE, null, null, ScribeConfig.DEFAULT_MAX_FILES_TO_KEEP,
                ScribeConfig.DEFAULT_SEND_INTERVAL_SECONDS);
        scribeClient = new ScribeClient(mock(Context.class), mock(ScheduledExecutorService.class),
                config, mock(ScribeEvent.Transform.class), mock(TwitterAuthConfig.class),
                mock(SessionManager.class), mock(GuestSessionProvider.class),
                mock(IdManager.class));

        final EventsStrategy<ScribeEvent> scribeStrategy
                = scribeClient.getScribeStrategy(GuestSession.LOGGED_OUT_USER_ID, null);
        Assert.assertThat(scribeStrategy instanceof DisabledEventsStrategy, is(true));
    }

    @Test
    public void testGetWorkingFileNameForOwner() {
        Assert.assertThat(scribeClient.getWorkingFileNameForOwner(GuestSession.LOGGED_OUT_USER_ID)
                .startsWith(Long.toString(GuestSession.LOGGED_OUT_USER_ID)), is(true));

        Assert.assertThat(scribeClient.getWorkingFileNameForOwner(TEST_USER_ID)
                .startsWith(Long.toString(TEST_USER_ID)), is(true));
    }

    @Test
    public void testGetStorageDirForOwner() {
        Assert.assertThat(scribeClient.getStorageDirForOwner(GuestSession.LOGGED_OUT_USER_ID)
                .startsWith(Long.toString(GuestSession.LOGGED_OUT_USER_ID)), is(true));

        Assert.assertThat(scribeClient.getStorageDirForOwner(TEST_USER_ID)
                .startsWith(Long.toString(TEST_USER_ID)), is(true));
    }
}
