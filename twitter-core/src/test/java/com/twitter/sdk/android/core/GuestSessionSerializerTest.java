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

import com.twitter.sdk.android.core.internal.oauth.GuestAuthToken;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
public class GuestSessionSerializerTest {
    private static final long CREATED_AT = 1414450780L;

    private static final String JSON_SESSION_GUEST = "{\"auth_token\":{\"auth_type\":\"guest\","
            + "\"auth_token\":{\"guest_token\":\"guestToken\",\"token_type\":\"tokenType\","
            + "\"access_token\":\"accessToken\",\"created_at\":1414450780}},\"id\":0}";
    private static final String JSON_SESSION_INVALID_AUTH_TYPE =
            "{\"auth_token\":{\"auth_type\":\"INVALID\","
            + "\"auth_token\":{\"guest_token\":\"guestToken\",\"access_token\":\"accessToken\","
            + "\"token_type\":\"tokenType\",\"created_at\":1414450780}},\"id\":0}";

    private static final String TEST_TOKEN_TYPE = "tokenType";
    private static final String TEST_ACCESS_TOKEN = "accessToken";
    private static final String TEST_GUEST_TOKEN = "guestToken";

    private GuestSession.Serializer serializer;

    @Before
    public void setUp() throws Exception {

        serializer = new GuestSession.Serializer();
    }

    @Test
    public void testSerialize_sessionNull() {
        Assert.assertThat(serializer.serialize(null), is(""));
    }

    @Test
    public void testSerialize_sessionAuthTokenIsGuestAuthToken() {
        final GuestSession session = new GuestSession(new GuestAuthToken(TEST_TOKEN_TYPE,
                TEST_ACCESS_TOKEN, TEST_GUEST_TOKEN, CREATED_AT));
        Assert.assertThat(serializer.serialize(session), is(JSON_SESSION_GUEST));
    }

    @Test
    public void testDeserialize_serializedStringNull() {
        Assert.assertThat(serializer.deserialize(null), nullValue());
    }

    @Test
    public void testDeserialize_serializedStringEmpty() {
        Assert.assertThat(serializer.deserialize(""), nullValue());
    }

    @Test
    public void testDeserialize_serializedStringAuthTokenIsGuestAuthToken() {
        final GuestSession session = serializer.deserialize(JSON_SESSION_GUEST);
        Assert.assertThat(session.getAuthToken().getClass(), sameInstance(GuestAuthToken.class));
        Assert.assertThat(session.getAuthToken().getTokenType(), is(TEST_TOKEN_TYPE));
        Assert.assertThat(session.getAuthToken().getAccessToken(), is(TEST_ACCESS_TOKEN));
        Assert.assertThat(session.getAuthToken().getGuestToken(), is(TEST_GUEST_TOKEN));
    }

    @Test
    public void testDeserialize_serializedStringAuthTokenIsInvalid() {
        final GuestSession session = serializer.deserialize(JSON_SESSION_INVALID_AUTH_TYPE);
        Assert.assertThat(session, nullValue());
    }
}
