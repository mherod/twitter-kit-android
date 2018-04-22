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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.Matchers.*;


@RunWith(RobolectricTestRunner.class)
public class TwitterSessionSerializerTest  {
    // static unix timestamp so that tests are repeatable and more easily debugged
    private static final long CREATED_AT = 1414450780L;
    public static final String SESSION_JSON = "{\"user_name\":\"\","
            + "\"auth_token\":{"
            + "\"token\":\"" + TestFixtures.TOKEN + "\","
            + "\"secret\":\"" + TestFixtures.SECRET + "\","
            + "\"created_at\":" + CREATED_AT + "},"
            + "\"id\":-1}";
    public static final String FULL_SESSION_JSON =
            "{\"user_name\":\"" + TestFixtures.SCREEN_NAME + "\","
            + "\"auth_token\":{"
            + "\"token\":\"" + TestFixtures.TOKEN + "\","
            + "\"secret\":\"" + TestFixtures.SECRET + "\","
            + "\"created_at\":" + CREATED_AT + "},"
            + "\"id\":" + TestFixtures.USER_ID + "}";
    public static final String SESSION_JSON_NULL_USERNAME = "{\"auth_token\":{"
            + "\"token\":\"token\","
            + "\"secret\":\"secret\","
            + "\"created_at\":" + CREATED_AT + "},"
            + "\"id\":" + TestFixtures.USER_ID + "}";

    private TwitterSession.Serializer serializer;

    @Before
    public void setUp() throws Exception {

        serializer = new TwitterSession.Serializer();
    }

    @Test
    public void testDeserialize_sessionWithAuthToken() {
        final TwitterSession session = serializer.deserialize(SESSION_JSON);
        final TwitterSession newSession = new TwitterSession(
                new TwitterAuthToken(TestFixtures.TOKEN, TestFixtures.SECRET, CREATED_AT),
                TwitterSession.UNKNOWN_USER_ID, TwitterSession.UNKNOWN_USER_NAME);
        Assert.assertThat(newSession, is(session));
    }

    @Test
    public void testDeserialize_session() {
        final TwitterSession session = serializer.deserialize(FULL_SESSION_JSON);
        Assert.assertThat(session, is(new TwitterSession(new TwitterAuthToken(TestFixtures.TOKEN,
                TestFixtures.SECRET, CREATED_AT), TestFixtures.USER_ID, TestFixtures.SCREEN_NAME)));
    }

    @Test
    public void testDeserialize_sessionWithNullUserName() {
        final TwitterSession session = serializer.deserialize(SESSION_JSON_NULL_USERNAME);
        Assert.assertThat(session, is(new TwitterSession(new TwitterAuthToken(TestFixtures.TOKEN,
                TestFixtures.SECRET, CREATED_AT), TestFixtures.USER_ID, null)));
    }

    @Test
    public void testDeserialize_nullSerializedSession() {
        final TwitterSession session = serializer.deserialize(null);
        Assert.assertThat(session, nullValue());
    }

    @Test
    public void testSerialize_sessionWithAuthToken() {
        final TwitterSession session = new TwitterSession(
                new TwitterAuthToken(TestFixtures.TOKEN, TestFixtures.SECRET, CREATED_AT),
                TwitterSession.UNKNOWN_USER_ID, TwitterSession.UNKNOWN_USER_NAME);
        Assert.assertThat(serializer.serialize(session), is(SESSION_JSON));
    }

    @Test
    public void testSerialize_session() {
        final TwitterSession session = new TwitterSession(
                new TwitterAuthToken(TestFixtures.TOKEN, TestFixtures.SECRET, CREATED_AT),
                TestFixtures.USER_ID, TestFixtures.SCREEN_NAME);
        Assert.assertThat(serializer.serialize(session), is(FULL_SESSION_JSON));
    }

    @Test
    public void testSerialize_sessionWithNullUserName() {
        final TwitterSession session = new TwitterSession(
                new TwitterAuthToken(TestFixtures.TOKEN, TestFixtures.SECRET, CREATED_AT),
                TestFixtures.USER_ID, null);
        Assert.assertThat(serializer.serialize(session), is(SESSION_JSON_NULL_USERNAME));
    }
}
