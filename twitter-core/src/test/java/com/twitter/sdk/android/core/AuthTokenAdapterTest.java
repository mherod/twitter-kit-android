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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.twitter.sdk.android.core.internal.oauth.GuestAuthToken;
import com.twitter.sdk.android.core.internal.oauth.OAuth2Token;
import com.twitter.sdk.android.core.internal.oauth.OAuthUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
public class AuthTokenAdapterTest  {
    private static final long CREATED_AT = 1414450780L;
    private static final String TOKEN_TYPE = "testTokenType";
    private static final String ACCESS_TOKEN = "testAccessToken";
    private static final String GUEST_TOKEN = "testGuestToken";
    private static final String JSON_OAUTH1A_TOKEN
            = "{\"authToken\":{\"auth_type\":\"oauth1a\","
            + "\"auth_token\":{"
            + "\"token\":\"" + TestFixtures.TOKEN + "\","
            + "\"secret\":\"" + TestFixtures.SECRET + "\","
            + "\"created_at\":" + CREATED_AT + "}}}";
    private static final String JSON_OAUTH2_TOKEN
            = "{\"authToken\":{\"auth_type\":\"oauth2\","
            + "\"auth_token\":{"
            + "\"token_type\":\"" + TOKEN_TYPE + "\","
            + "\"access_token\":\"" + ACCESS_TOKEN + "\","
            + "\"created_at\":" + CREATED_AT + "}}}";
    private static final String JSON_GUEST_AUTH_TOKEN
            = "{\"authToken\":{\"auth_type\":\"guest\","
            + "\"auth_token\":{\"guest_token\":\"" + GUEST_TOKEN + "\","
            + "\"token_type\":\"" + TOKEN_TYPE + "\","
            + "\"access_token\":\"" + ACCESS_TOKEN + "\","
            + "\"created_at\":" + CREATED_AT + "}}}";
    private static final String JSON_APP_AUTH_TOKEN
            = "{\"authToken\":{\"auth_type\":\"app\","
            + "\"auth_token\":{"
            + "\"token_type\":\"" + TOKEN_TYPE + "\","
            + "\"access_token\":\"" + ACCESS_TOKEN + "\","
            + "\"created_at\":" + CREATED_AT + "}}}";
    private static final String JSON_OAUTH1A_TOKEN_MISSING_CREATED_AT
            = "{\"authToken\":{\"auth_type\":\"oauth1a\","
            + "\"auth_token\":{\"secret\":\"" + TestFixtures.SECRET + "\","
            + "\"token\":\"" + TestFixtures.TOKEN + "\"}}}";
    private static final String JSON_OAUTH2_TOKEN_MISSING_CREATED_AT
            = "{\"authToken\":{\"auth_type\":\"oauth2\","
            + "\"auth_token\":{\"access_token\":\"" + ACCESS_TOKEN + "\","
            + "\"token_type\":\"" + TOKEN_TYPE + "\"}}}";
    private static final String JSON_GUEST_AUTH_TOKEN_MISSING_CREATED_AT
            = "{\"authToken\":{\"auth_type\":\"guest\","
            + "\"auth_token\":{\"guest_token\":\"" + GUEST_TOKEN + "\","
            + "\"access_token\":\"" + ACCESS_TOKEN + "\","
            + "\"token_type\":\"" + TOKEN_TYPE + "\"}}}";

    private Gson gson;

    @Before
    public void setUp() throws Exception {

        gson = new GsonBuilder()
                .registerTypeAdapter(AuthToken.class, new AuthTokenAdapter())
                .create();
    }

    @Test
    public void testGetAuthTypeString() {
        Assert.assertThat(AuthTokenAdapter.getAuthTypeString(TwitterAuthToken.class), is("oauth1a"));
    }

    @Test
    public void testGetAuthTypeString_unregisteredAuthType() {
        Assert.assertThat(AuthTokenAdapter.getAuthTypeString(TestAuthToken.class), is(""));
    }

    @Test
    public void testSerialize_oauth1aToken() {
        final AuthTokenWrapper authTokenWrapper = new AuthTokenWrapper(
                new TwitterAuthToken(TestFixtures.TOKEN, TestFixtures.SECRET, CREATED_AT));
        final String json = gson.toJson(authTokenWrapper);
        Assert.assertThat(json, json, is(JSON_OAUTH1A_TOKEN));
    }

    @Test
    public void testSerialize_oauth2Token() {
        final AuthTokenWrapper test = new AuthTokenWrapper(
                OAuthUtils.createOAuth2Token(TOKEN_TYPE, ACCESS_TOKEN, CREATED_AT));
        final String json = gson.toJson(test);
        Assert.assertThat(json, json, is(JSON_OAUTH2_TOKEN));
    }

    @Test
    public void testSerialize_guestAuthToken() {
        final AuthTokenWrapper test = new AuthTokenWrapper(
                OAuthUtils.createGuestAuthToken(TOKEN_TYPE, ACCESS_TOKEN, GUEST_TOKEN,
                        CREATED_AT));
        final String json = gson.toJson(test);
        Assert.assertThat(json, json, is(JSON_GUEST_AUTH_TOKEN));
    }

    @Test
    public void testDeserialize_oauth1aToken() {
        final AuthTokenWrapper authTokenWrapper = gson.fromJson(JSON_OAUTH1A_TOKEN,
                AuthTokenWrapper.class);
        Assert.assertThat(authTokenWrapper.authToken instanceof TwitterAuthToken, is(true));
        final TwitterAuthToken authToken = (TwitterAuthToken) authTokenWrapper.authToken;
        Assert.assertThat(authToken.getToken(), is(TestFixtures.TOKEN));
        Assert.assertThat(authToken.getSecret(), is(TestFixtures.SECRET));
    }

    @Test
    public void testDeserialize_oauth2Token() {
        final AuthTokenWrapper authTokenWrapper = gson.fromJson(JSON_OAUTH2_TOKEN,
                AuthTokenWrapper.class);
        Assert.assertThat(authTokenWrapper.authToken instanceof OAuth2Token, is(true));
        final OAuth2Token authToken = (OAuth2Token) authTokenWrapper.authToken;
        Assert.assertThat(authToken.getTokenType(), is(TOKEN_TYPE));
        Assert.assertThat(authToken.getAccessToken(), is(ACCESS_TOKEN));
    }

    @Test
    public void testDeserialize_guestAuthToken() {
        final AuthTokenWrapper authTokenWrapper = gson.fromJson(JSON_GUEST_AUTH_TOKEN,
                AuthTokenWrapper.class);
        Assert.assertThat(authTokenWrapper.authToken instanceof GuestAuthToken, is(true));
        final GuestAuthToken authToken = (GuestAuthToken) authTokenWrapper.authToken;
        Assert.assertThat(authToken.getGuestToken(), is(GUEST_TOKEN));
        Assert.assertThat(authToken.getTokenType(), is(TOKEN_TYPE));
        Assert.assertThat(authToken.getAccessToken(), is(ACCESS_TOKEN));
    }

    @Test
    public void testDeserialize_oauth1aTokenMissingCreatedAt() {
        final AuthTokenWrapper authTokenWrapper = gson.fromJson(
                JSON_OAUTH1A_TOKEN_MISSING_CREATED_AT, AuthTokenWrapper.class);
        Assert.assertThat(authTokenWrapper.authToken instanceof TwitterAuthToken, is(true));
        final TwitterAuthToken authToken = (TwitterAuthToken) authTokenWrapper.authToken;
        Assert.assertThat(authToken.getToken(), is(TestFixtures.TOKEN));
        Assert.assertThat(authToken.getSecret(), is(TestFixtures.SECRET));
        Assert.assertThat(authToken.getCreatedAt(), is(0));
    }

    @Test
    public void testDeserialize_oauth2TokenMissingCreatedAt() {
        final AuthTokenWrapper authTokenWrapper = gson.fromJson(
                JSON_OAUTH2_TOKEN_MISSING_CREATED_AT, AuthTokenWrapper.class);
        Assert.assertThat(authTokenWrapper.authToken instanceof OAuth2Token, is(true));
        final OAuth2Token authToken = (OAuth2Token) authTokenWrapper.authToken;
        Assert.assertThat(authToken.getTokenType(), is(TOKEN_TYPE));
        Assert.assertThat(authToken.getAccessToken(), is(ACCESS_TOKEN));
        Assert.assertThat(authToken.getCreatedAt(), is(0));
    }

    @Test
    public void testDeserialize_guestAuthTokenMissingCreatedAt() {
        final AuthTokenWrapper authTokenWrapper = gson.fromJson(
                JSON_GUEST_AUTH_TOKEN_MISSING_CREATED_AT, AuthTokenWrapper.class);
        Assert.assertThat(authTokenWrapper.authToken instanceof GuestAuthToken, is(true));
        final GuestAuthToken authToken = (GuestAuthToken) authTokenWrapper.authToken;
        Assert.assertThat(authToken.getGuestToken(), is(GUEST_TOKEN));
        Assert.assertThat(authToken.getTokenType(), is(TOKEN_TYPE));
        Assert.assertThat(authToken.getAccessToken(), is(ACCESS_TOKEN));
        Assert.assertThat(authToken.getCreatedAt(), is(0));
    }

    private static class AuthTokenWrapper {
        final AuthToken authToken;

        AuthTokenWrapper(AuthToken authToken) {
            this.authToken = authToken;
        }
    }

    private static class TestAuthToken extends AuthToken {

        @Override
        public boolean isExpired() {
            return false;
        }
    }
}
