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

package com.twitter.sdk.android.core.internal.oauth;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.internal.TwitterApi;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Header;
import retrofit2.http.Query;
import retrofit2.mock.Calls;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@SuppressWarnings("checkstyle:linelength")
public class OAuth1aServiceTest {

    private TwitterAuthConfig authConfig;
    private TwitterCore twitterCore;
    private OAuth1aService service;
    private TwitterApi twitterApi;

    @Before
    public void setUp() throws Exception {
        authConfig = new TwitterAuthConfig("key", "secret");
        twitterCore = mock(TwitterCore.class);
        when(twitterCore.getAuthConfig()).thenReturn(authConfig);
        twitterApi = new TwitterApi();
        service = new OAuth1aService(twitterCore, twitterApi);
    }

    @Test
    public void testGetTempTokenUrl() {
        Assert.assertThat(service.getTempTokenUrl(), is("https://api.twitter.com/oauth/request_token"));
    }

    @Test
    public void testGetAccessTokenUrl() {
        Assert.assertThat(service.getAccessTokenUrl(), is("https://api.twitter.com/oauth/access_token"));
    }

    @Test
    public void testRequestTempToken() {
        service.api = new MockOAuth1aService() {
            @Override
            public Call<ResponseBody> getTempToken(@Header(OAuthConstants.HEADER_AUTHORIZATION) String auth) {
                Assert.assertThat(auth, containsString(OAuthConstants.PARAM_CALLBACK));
                return super.getTempToken(auth);
            }
        };
        service.requestTempToken(mock(Callback.class));
    }

    @Test
    public void testRequestAccessToken() {
        final TwitterAuthToken token = new TwitterAuthToken("token", "secret");
        final String verifier = "verifier";
        service.api = new MockOAuth1aService() {
            @Override
            public Call<ResponseBody> getAccessToken(@Header(OAuthConstants.HEADER_AUTHORIZATION) String auth,
                                                     @Query(OAuthConstants.PARAM_VERIFIER) String innerVerifier) {

                Assert.assertThat(innerVerifier, is(verifier));
                Assert.assertThat(auth, notNullValue());
                Assert.assertThat(auth, containsString(token.getToken()));

                return super.getAccessToken(auth, innerVerifier);
            }
        };
        service.requestAccessToken(mock(Callback.class), token, verifier);
    }

    @Test
    public void testApiHost() {
        Assert.assertThat(service.getApi(), is(twitterApi));
    }

    @Test
    public void testGetUserAgent() {
        final String userAgent = TwitterApi.buildUserAgent("TwitterAndroidSDK",
                twitterCore.getVersion());
        Assert.assertThat(service.getUserAgent(), is(userAgent));
    }

    @Test
    public void testBuildCallbackUrl() {
        final String callbackUrl = service.buildCallbackUrl(authConfig);

        Assert.assertThat(callbackUrl, is(String.format("twittersdk://callback?version=%s&app=%s",
                twitterCore.getVersion(), authConfig.getConsumerKey())));
    }

    @Test
    public void testGetAuthorizeUrl() {
        final TwitterAuthToken authToken = new TwitterAuthToken("token", "secret");
        final String authorizeUrl = service.getAuthorizeUrl(authToken);
        Assert.assertThat(authorizeUrl, is("https://api.twitter.com/oauth/authorize?oauth_token=token"));
    }

    @Test
    public void testParseAuthResponse() {
        final String response = "oauth_token=7588892-kagSNqWge8gB1WwE3plnFsJHAZVfxWD7Vb57p0b4&"
                + "oauth_token_secret=PbKfYqSryyeKDWz4ebtY3o5ogNLG11WJuZBc9fQrQo&"
                + "screen_name=test&user_id=1";
        final OAuthResponse authResponse = OAuth1aService.parseAuthResponse(response);
        Assert.assertThat(authResponse.authToken.getToken(), is("7588892-kagSNqWge8gB1WwE3plnFsJHAZVfxWD7Vb57p0b4"));
        Assert.assertThat(authResponse.authToken.getSecret(), is("PbKfYqSryyeKDWz4ebtY3o5ogNLG11WJuZBc9fQrQo"));
        Assert.assertThat(authResponse.userName, is("test"));
        Assert.assertThat(authResponse.userId, is(1L));
    }

    @Test
    public void testParseAuthResponse_noQueryParameters() {
        final String response = "noQueryParameters";
        final OAuthResponse authResponse = OAuth1aService.parseAuthResponse(response);
        Assert.assertThat(authResponse, nullValue());
    }

    @Test
    public void testParseAuthResponse_noToken() {
        final String response = "oauth_token_secret=PbKfYqSryyeKDWz4ebtY3o5ogNLG11WJuZBc9fQrQo&"
                + "screen_name=test&user_id=1";
        final OAuthResponse authResponse = OAuth1aService.parseAuthResponse(response);
        Assert.assertThat(authResponse, nullValue());
    }

    @Test
    public void testParseAuthResponse_noSecret() {
        final String response = "oauth_token=7588892-kagSNqWge8gB1WwE3plnFsJHAZVfxWD7Vb57p0b4&"
                + "screen_name=test&user_id=1";
        final OAuthResponse authResponse = OAuth1aService.parseAuthResponse(response);
        Assert.assertThat(authResponse, nullValue());
    }

    @Test
    public void testParseAuthResponse_noScreenName() {
        final String response = "oauth_token=7588892-kagSNqWge8gB1WwE3plnFsJHAZVfxWD7Vb57p0b4&"
                + "oauth_token_secret=PbKfYqSryyeKDWz4ebtY3o5ogNLG11WJuZBc9fQrQo&"
                + "user_id=1";
        final OAuthResponse authResponse = OAuth1aService.parseAuthResponse(response);
        Assert.assertThat(authResponse.authToken.getToken(), is("7588892-kagSNqWge8gB1WwE3plnFsJHAZVfxWD7Vb57p0b4"));
        Assert.assertThat(authResponse.authToken.getSecret(), is("PbKfYqSryyeKDWz4ebtY3o5ogNLG11WJuZBc9fQrQo"));
        Assert.assertThat(authResponse.userName, nullValue());
        Assert.assertThat(authResponse.userId, is(1L));
    }

    @Test
    public void testParseAuthResponse_noUserId() {
        final String response = "oauth_token=7588892-kagSNqWge8gB1WwE3plnFsJHAZVfxWD7Vb57p0b4&"
                + "oauth_token_secret=PbKfYqSryyeKDWz4ebtY3o5ogNLG11WJuZBc9fQrQo&"
                + "screen_name=test";
        final OAuthResponse authResponse = OAuth1aService.parseAuthResponse(response);
        Assert.assertThat(authResponse.authToken.getToken(), is("7588892-kagSNqWge8gB1WwE3plnFsJHAZVfxWD7Vb57p0b4"));
        Assert.assertThat(authResponse.authToken.getSecret(), is("PbKfYqSryyeKDWz4ebtY3o5ogNLG11WJuZBc9fQrQo"));
        Assert.assertThat(authResponse.userName, is("test"));
        Assert.assertThat(authResponse.userId, is(0L));
    }

    @Test
    public void testCallbackWrapperSuccess() {
        final String response = "oauth_token=7588892-kagSNqWge8gB1WwE3plnFsJHAZVfxWD7Vb57p0b4&"
                + "oauth_token_secret=PbKfYqSryyeKDWz4ebtY3o5ogNLG11WJuZBc9fQrQo&"
                + "screen_name=test&user_id=1";
        final Callback<OAuthResponse> callback = new Callback<OAuthResponse>() {
            @Override
            public void success(Result<OAuthResponse> result) {
                final OAuthResponse authResponse = result.getData();
                Assert.assertThat(authResponse.authToken.getToken(), is("7588892-kagSNqWge8gB1WwE3plnFsJHAZVfxWD7Vb57p0b4"));
                Assert.assertThat(authResponse.authToken.getSecret(), is("PbKfYqSryyeKDWz4ebtY3o5ogNLG11WJuZBc9fQrQo"));
                Assert.assertThat(authResponse.userName, is("test"));
                Assert.assertThat(authResponse.userId, is(1L));
            }

            @Override
            public void failure(TwitterException exception) {
                fail();
            }
        };
        setupCallbackWrapperTest(response, callback);
    }

    private void setupCallbackWrapperTest(String responseStr,
                                          Callback<OAuthResponse> authResponseCallback) {
        final Callback<ResponseBody> callbackWrapper = service.getCallbackWrapper(authResponseCallback);
        final ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), responseStr);
        final Response<ResponseBody> response = Response.success(responseBody);

        callbackWrapper.success(new Result<>(responseBody, response));
    }

    @Test
    public void testCallbackWrapperSuccess_noToken() {
        final String response = "oauth_token_secret=PbKfYqSryyeKDWz4ebtY3o5ogNLG11WJuZBc9fQrQo&"
                + "screen_name=test&user_id=1";
        final Callback<OAuthResponse> callback = new Callback<OAuthResponse>() {
            @Override
            public void success(Result<OAuthResponse> result) {
                fail();
            }

            @Override
            public void failure(TwitterException exception) {
                Assert.assertThat(exception, notNullValue());
            }
        };
        setupCallbackWrapperTest(response, callback);
    }

    @Test
    public void testCallbackWrapperSuccess_iOException() {
        final Callback<OAuthResponse> callback = new Callback<OAuthResponse>() {
            @Override
            public void success(Result<OAuthResponse> result) {
                fail();
            }

            @Override
            public void failure(TwitterException exception) {
                Assert.assertThat(exception, notNullValue());
            }
        };
        final Callback<ResponseBody> callbackWrapper = service.getCallbackWrapper(callback);
        final ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "");
        callbackWrapper.success(new Result<>(responseBody, Response.success(responseBody)));
    }

    @Test
    public void testCallbackWrapperFailure() {
        final Callback<OAuthResponse> authResponseCallback = mock(Callback.class);
        final Callback<ResponseBody> callbackWrapper = service.getCallbackWrapper(authResponseCallback);
        final TwitterException mockException = mock(TwitterException.class);
        callbackWrapper.failure(mockException);
        verify(authResponseCallback).failure(eq(mockException));
    }

    private static class MockOAuth1aService implements OAuth1aService.OAuthApi {

        @Override
        public Call<ResponseBody> getTempToken(@Header(OAuthConstants.HEADER_AUTHORIZATION) String auth) {
            final ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "");
            return Calls.response(Response.success(responseBody));
        }

        @Override
        public Call<ResponseBody> getAccessToken(@Header(OAuthConstants.HEADER_AUTHORIZATION) String auth,
                @Query(OAuthConstants.PARAM_VERIFIER) String verifier) {
            final ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "");
            return Calls.response(Response.success(responseBody));
        }
    }
}
