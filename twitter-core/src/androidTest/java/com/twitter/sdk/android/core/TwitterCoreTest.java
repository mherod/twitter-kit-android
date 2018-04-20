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

import android.test.AndroidTestCase;

import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TwitterCoreTest extends AndroidTestCase {

    private static final String TWITTER_NOT_INIT_ERROR_MSG = "Must initialize Twitter before using getInstance()";
    private TwitterCore twitterCore;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Twitter.initialize(new TwitterConfig.Builder(getContext())
                .executorService(mock(ExecutorService.class))
                .build());
        twitterCore = new TwitterCore(new TwitterAuthConfig("", ""));
        TwitterCore.instance = twitterCore;
    }

    @Override
    protected void tearDown() throws Exception {
        TwitterTestUtils.resetTwitter();
        TwitterCoreTestUtils.resetTwitterCore();
        super.tearDown();
    }

    public void testGuestSessionManager_noSdkStart() {
        try {
            TwitterTestUtils.resetTwitter();
            TwitterCoreTestUtils.resetTwitterCore();
            TwitterCore.getInstance().getGuestSessionProvider();
            Assert.fail("Should fail if Twitter is not initialized.");
        } catch (IllegalStateException ie) {
            Assert.assertEquals(TWITTER_NOT_INIT_ERROR_MSG, ie.getMessage());
        }
    }

    public void testGuestSessionManager_sdkStartNoTwitterKit() {
        try {
            TwitterTestUtils.resetTwitter();
            TwitterCoreTestUtils.resetTwitterCore();
            TwitterCore.getInstance().getGuestSessionProvider();
            Assert.fail("Should fail if Twitter is not initialized.");
        } catch (IllegalStateException ie) {
            Assert.assertEquals(TWITTER_NOT_INIT_ERROR_MSG, ie.getMessage());
        }
    }

    public void testGetIdentifier() {
        final String identifier = BuildConfig.GROUP + ":" + BuildConfig.ARTIFACT_ID;
        Assert.assertEquals(identifier, twitterCore.getIdentifier());
    }

    public void testGetSessionManager() {
        Assert.assertNotNull(twitterCore.getSessionManager());
    }

    public void testGetSessionManager_twitterNotInitialized() {
        try {
            TwitterTestUtils.resetTwitter();
            TwitterCoreTestUtils.resetTwitterCore();
            TwitterCore.getInstance().getSessionManager();
            Assert.fail("Should fail if Twitter is not initialized.");
        } catch (IllegalStateException ex) {
            Assert.assertEquals(TWITTER_NOT_INIT_ERROR_MSG, ex.getMessage());
        }
    }

    public void testGetAppSessionManager() {
        Assert.assertNotNull(twitterCore.getGuestSessionProvider());
    }

    public void testGetAppSessionManager_twitterNotInitialized() {
        try {
            TwitterTestUtils.resetTwitter();
            TwitterCoreTestUtils.resetTwitterCore();
            TwitterCore.getInstance().getGuestSessionProvider();
            Assert.fail("Should fail if Twitter is not initialized.");
        } catch (IllegalStateException ex) {
            Assert.assertEquals(TWITTER_NOT_INIT_ERROR_MSG, ex.getMessage());
        }
    }

    public void testGetApiClient_activeSessionExists() {
        twitterCore.twitterSessionManager = setUpSessionManager(mock(TwitterSession.class));
        Assert.assertNotNull(twitterCore.getApiClient());
    }

    public void testGetApiClient_twitterNotInitialized() {
        try {
            TwitterTestUtils.resetTwitter();
            TwitterCoreTestUtils.resetTwitterCore();
            twitterCore.getApiClient();
            Assert.fail("Should fail if Twitter is not initialized.");
        } catch (IllegalStateException ex) {
            Assert.assertEquals(TWITTER_NOT_INIT_ERROR_MSG, ex.getMessage());
        }
    }

    public void testGetApiClient_withSession() {
        Assert.assertNotNull(twitterCore.getApiClient(mock(TwitterSession.class)));
    }

    public void testGetApiClient_withSessionTwitterNotInitialized() {
        try {
            TwitterTestUtils.resetTwitter();
            TwitterCoreTestUtils.resetTwitterCore();
            TwitterCore.getInstance().getApiClient(mock(TwitterSession.class));
            Assert.fail("Should fail if Twitter is not initialized.");
        } catch (IllegalStateException ex) {
            Assert.assertEquals(TWITTER_NOT_INIT_ERROR_MSG, ex.getMessage());
        }
    }

    public void testGetGuestApiClient_twitterNotInitialized() {
        try {
            TwitterTestUtils.resetTwitter();
            TwitterCoreTestUtils.resetTwitterCore();
            TwitterCore.getInstance().getGuestApiClient();
            Assert.fail("Should fail if Twitter is not initialized.");
        } catch (IllegalStateException ex) {
            Assert.assertEquals(TWITTER_NOT_INIT_ERROR_MSG, ex.getMessage());
        }
    }

    private <T extends Session> SessionManager<T> setUpSessionManager(T session) {
        final SessionManager<T> sessionManager = mock(SessionManager.class);
        when(sessionManager.getActiveSession()).thenReturn(session);
        return sessionManager;
    }
}
