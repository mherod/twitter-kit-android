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

import com.twitter.sdk.android.core.TestFixtures;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.internal.oauth.OAuthConstants;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.Request;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class OAuth1aInterceptorTest {
    static final String POST_KEY = "test";
    static final String POST_KEY_2 = "test 2!";
    static final String POST_KEY_2_ENCODED = "test%202%21";
    static final String POST_VALUE = "value";
    static final String POST_VALUE_2 = "value 2!";
    static final String TEST_URL = "https://api.twitter.com";
    static final String TEST_HEADER = "TEST_HEADER";

    @Mock
    TwitterSession mockTwitterSession;
    @Mock
    TwitterAuthToken mockAuthToken;
    @Mock
    TwitterAuthConfig mockAuthConfig;
    @Mock
    Interceptor.Chain mockChain;
    @Captor
    ArgumentCaptor<Request> requestCaptor;
    OAuth1aInterceptor interceptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(mockAuthConfig.getConsumerKey()).thenReturn(TestFixtures.KEY);
        when(mockAuthConfig.getConsumerSecret()).thenReturn(TestFixtures.SECRET);

        when(mockTwitterSession.getAuthToken()).thenReturn(mockAuthToken);

        interceptor = new OAuth1aInterceptor(mockTwitterSession, mockAuthConfig);
    }

    @Test
    public void testIntercept() throws Exception {
        final Request request = new Request.Builder()
                .url(TEST_URL)
                .header(TEST_HEADER, TEST_HEADER)
                .build();
        when(mockChain.request()).thenReturn(request);

        interceptor.intercept(mockChain);

        verify(mockChain).proceed(requestCaptor.capture());

        final Request signedRequest = requestCaptor.getValue();
        Assert.assertThat(signedRequest.header(OAuthConstants.HEADER_AUTHORIZATION), notNullValue());
        Assert.assertThat(signedRequest.header(TEST_HEADER), is(TEST_HEADER));
    }

    @Test
    public void testGetAuthHeaders() {
        final Request request = new Request.Builder().url(TEST_URL).build();

        final String header = interceptor.getAuthorizationHeader(request);

        Assert.assertThat(header, notNullValue());
    }

    @Test
    public void testGetPostParameters_bodyWithMultipleParams() {
        final FormBody formBody = new FormBody.Builder()
                .add(POST_KEY, POST_VALUE)
                .add(POST_KEY_2, POST_VALUE_2)
                .build();

        final Request request = new Request.Builder()
                .url(TEST_URL)
                .post(formBody)
                .build();

        final Map<String, String> params = interceptor.getPostParams(request);

        Assert.assertThat(params.size(), is(2));
        Assert.assertThat(params.get(POST_KEY), is(POST_VALUE));
        Assert.assertThat(params.get(POST_KEY_2_ENCODED), is(POST_VALUE_2));
    }

    @Test
    public void testGetPostParameters_withZeroParams() {
        final FormBody formBody = new FormBody.Builder().build();

        final Request request = new Request.Builder()
                .url(TEST_URL)
                .post(formBody)
                .build();

        final Map<String, String> params = interceptor.getPostParams(request);

        Assert.assertThat(params.size(), is(0));
    }

    @Test
    public void testGetPostParameters_withGetRequest() {
        final Request request = new Request.Builder().url(TEST_URL).build();

        final Map<String, String> params = interceptor.getPostParams(request);

        Assert.assertThat(params.size(), is(0));
    }
}
