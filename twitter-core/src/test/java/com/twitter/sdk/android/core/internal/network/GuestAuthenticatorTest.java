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

package com.twitter.sdk.android.core.internal.network;

import com.twitter.sdk.android.core.GuestSession;
import com.twitter.sdk.android.core.GuestSessionProvider;
import com.twitter.sdk.android.core.internal.oauth.GuestAuthToken;
import com.twitter.sdk.android.core.internal.oauth.OAuth2Token;
import com.twitter.sdk.android.core.internal.oauth.OAuthConstants;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class GuestAuthenticatorTest {
    static final String TEST_GUEST_TOKEN = "139854932048";
    static final String TEST_ACCESS_TOKEN = "AjhdlsjreurWfjdiskdjieidfkdjshrow";
    static final String TEST_GUEST_TOKEN_2 = "13985434545048";
    static final String TEST_ACCESS_TOKEN_2 = "ldsjfljsdhfjhuYSGYYSuagshjhags";
    static final String TEST_HEADER_AUTHORIZATION = OAuth2Token.TOKEN_TYPE_BEARER
            + " " + TEST_ACCESS_TOKEN;
    static final String TEST_HEADER_AUTHORIZATION_2 = OAuth2Token.TOKEN_TYPE_BEARER
            + " " + TEST_ACCESS_TOKEN_2;
    static final String TEST_URL = "https://api.twitter.com";

    @Mock
    GuestSessionProvider mockGuestSessionProvider;
    @Mock
    GuestAuthToken mockAuthToken;
    Request request;
    Response response;
    GuestAuthenticator authenticator;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(mockAuthToken.getGuestToken()).thenReturn(TEST_GUEST_TOKEN_2);
        when(mockAuthToken.getAccessToken()).thenReturn(TEST_ACCESS_TOKEN_2);
        when(mockAuthToken.getTokenType()).thenReturn(OAuth2Token.TOKEN_TYPE_BEARER);

        request = new Request.Builder()
                .url(TEST_URL)
                .header(OAuthConstants.HEADER_AUTHORIZATION, TEST_HEADER_AUTHORIZATION)
                .header(OAuthConstants.HEADER_GUEST_TOKEN, TEST_GUEST_TOKEN)
                .build();

        response = new Response.Builder()
                .code(401)
                .message("Unauthorized")
                .protocol(Protocol.HTTP_1_1)
                .request(request)
                .build();

        authenticator = new GuestAuthenticator(mockGuestSessionProvider);
    }

    @Test
    public void testGetExpiredToken() {
        final GuestSession session = authenticator.getExpiredSession(response);

        final GuestAuthToken token = session.getAuthToken();
        Assert.assertThat(token.getGuestToken(), is(TEST_GUEST_TOKEN));
        Assert.assertThat(token.getAccessToken(), is(TEST_ACCESS_TOKEN));
    }

    @Test
    public void testGetExpiredToken_emptyHeaders() {
        request = new Request.Builder()
                .url(TEST_URL)
                .build();

        response = new Response.Builder()
                .code(401)
                .message("Unauthorized")
                .protocol(Protocol.HTTP_1_1)
                .request(request)
                .build();

        final GuestSession session = authenticator.getExpiredSession(response);

        Assert.assertThat(session, nullValue());
    }

    @Test
    public void testReauth_emptyHeaders() {
        request = new Request.Builder()
                .url(TEST_URL)
                .build();

        response = new Response.Builder()
                .code(401)
                .message("Unauthorized")
                .protocol(Protocol.HTTP_1_1)
                .request(request)
                .build();

        final Request request = authenticator.reauth(response);
        Assert.assertThat(request, nullValue());
    }

    @Test
    public void testResign() {
        final Request newRequest = authenticator.resign(request, mockAuthToken);

        Assert.assertThat(newRequest.header(OAuthConstants.HEADER_AUTHORIZATION), is(TEST_HEADER_AUTHORIZATION_2));
        Assert.assertThat(newRequest.header(OAuthConstants.HEADER_GUEST_TOKEN), is(TEST_GUEST_TOKEN_2));
    }

    @Test
    public void testCanRetry_firstRetry() {
        Assert.assertThat(authenticator.canRetry(response), is(true));
    }

    @Test
    public void testCanRetry_secondRetry() {
        final Response failedResponse = new Response.Builder()
                .code(401)
                .message("Unauthorized")
                .protocol(Protocol.HTTP_1_1)
                .request(request)
                .priorResponse(response)
                .build();

        Assert.assertThat(authenticator.canRetry(failedResponse), is(false));
    }
}
