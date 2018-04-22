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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class TwitterSessionTest  {

    @Test
    public void testConstructor_noAuthToken() {
        try {
            final TwitterSession session = new TwitterSession(null, TwitterSession.UNKNOWN_USER_ID,
                    TwitterSession.UNKNOWN_USER_NAME);
            fail();
        } catch (IllegalArgumentException ie) {
            Assert.assertThat(ie.getMessage(), is("AuthToken must not be null."));
        }
    }

    @Test
    public void testEquals_sameObjects() {
        final TwitterSession session = new TwitterSession(
                new TwitterAuthToken(TestFixtures.TOKEN, TestFixtures.SECRET), TestFixtures.USER_ID,
                TestFixtures.SCREEN_NAME);
        final TwitterSession newSession = new TwitterSession(
                new TwitterAuthToken(TestFixtures.TOKEN, TestFixtures.SECRET), TestFixtures.USER_ID,
                TestFixtures.SCREEN_NAME);
        Assert.assertThat(newSession.hashCode(), is(session.hashCode()));
        Assert.assertThat(newSession, is(session));
    }

    @Test
    public void testEquals_sameObjectsWithNullUserName() {
        final TwitterSession session = new TwitterSession(
                new TwitterAuthToken(TestFixtures.TOKEN, TestFixtures.SECRET), TestFixtures.USER_ID,
                null);
        final TwitterSession newSession = new TwitterSession(
                new TwitterAuthToken(TestFixtures.TOKEN, TestFixtures.SECRET), TestFixtures.USER_ID,
                null);
        Assert.assertThat(newSession.hashCode(), is(session.hashCode()));
        Assert.assertThat(newSession, is(session));
    }

    @Test
    public void testEquals_diffObjects() {
        final TwitterSession session = new TwitterSession(
                new TwitterAuthToken(TestFixtures.TOKEN, TestFixtures.SECRET), TestFixtures.USER_ID,
                TestFixtures.SCREEN_NAME);
        final long differentUserId = TestFixtures.USER_ID + 1;
        final TwitterSession newSession = new TwitterSession(
                new TwitterAuthToken(TestFixtures.TOKEN, TestFixtures.SECRET), differentUserId,
                TestFixtures.SCREEN_NAME);
        Assert.assertThat(newSession, not(sameInstance(session)));
    }

    @Test
    public void testEquals_diffObjectsWithNullUserName() {
        final TwitterSession session = new TwitterSession(
                new TwitterAuthToken(TestFixtures.TOKEN, TestFixtures.SECRET), TestFixtures.USER_ID,
                null);
        final long differentUserId = TestFixtures.USER_ID + 1;
        final TwitterSession newSession = new TwitterSession(
                new TwitterAuthToken(TestFixtures.TOKEN, TestFixtures.SECRET), differentUserId,
                null);
        Assert.assertThat(newSession, not(sameInstance(session)));
    }

}

