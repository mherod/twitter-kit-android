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

package com.twitter.sdk.android.tweetui;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.services.StatusesService;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserTimelineTest {
    private static final Integer REQUIRED_DEFAULT_ITEMS_PER_REQUEST = 30;
    private static final Integer TEST_ITEMS_PER_REQUEST = 100;
    private static final Long TEST_SINCE_ID = 1000L;
    private static final Long TEST_MAX_ID = 1111L;
    private static final String REQUIRED_IMPRESSION_SECTION = "user";

    TwitterCore twitterCore;
    TwitterApiClient apiClient;
    StatusesService statusesService;

    @Before
    public void setUp() {
        twitterCore = mock(TwitterCore.class);
        apiClient = mock(TwitterApiClient.class);
        statusesService = mock(StatusesService.class, new MockCallAnswer());

        when(apiClient.getStatusesService()).thenReturn(statusesService);
        when(twitterCore.getApiClient()).thenReturn(apiClient);
    }

    @Test
    public void testConstructor() {
        final UserTimeline timeline = new UserTimeline(twitterCore, TestFixtures.TEST_USER.getId(),
                TestFixtures.TEST_USER.getScreenName(), TEST_ITEMS_PER_REQUEST, true, true);
        Assert.assertThat(timeline.userId, is(TestFixtures.TEST_USER.getId()));
        Assert.assertThat(timeline.screenName, is(TestFixtures.TEST_USER.getScreenName()));
        Assert.assertThat(timeline.maxItemsPerRequest, is(TEST_ITEMS_PER_REQUEST));
        Assert.assertThat(timeline.includeReplies, is(true));
        Assert.assertThat(timeline.includeRetweets, is(true));
    }

    @Test
    // most api arguments should default to Null to allow the backend to determine default behavior
    public void testConstructor_defaults() {
        final UserTimeline timeline = new UserTimeline(twitterCore, null, null, null, null, null);
        Assert.assertThat(timeline.userId, nullValue());
        Assert.assertThat(timeline.screenName, nullValue());
        Assert.assertThat(timeline.maxItemsPerRequest, nullValue());
        Assert.assertThat(timeline.includeRetweets, nullValue());
        // parameters which default to false
        Assert.assertThat(timeline.includeReplies, is(false));
    }

    @Test
    public void testNext_createsCorrectRequest() {
        final UserTimeline timeline = spy(new UserTimeline(twitterCore, TestFixtures.TEST_USER.getId(),
                TestFixtures.TEST_USER.getScreenName(), TEST_ITEMS_PER_REQUEST, null, null));
        timeline.next(TEST_SINCE_ID, mock(Callback.class));
        verify(timeline, times(1)).createUserTimelineRequest(eq(TEST_SINCE_ID),
                isNull(Long.class));
    }

    @Test
    public void testPrevious_createsCorrectRequest() {
        final UserTimeline timeline = spy(new UserTimeline(twitterCore, TestFixtures.TEST_USER.getId(),
                TestFixtures.TEST_USER.getScreenName(), TEST_ITEMS_PER_REQUEST, null, null));
        timeline.previous(TEST_MAX_ID, mock(Callback.class));
        // intentionally decrementing the maxId which is passed through to the request
        verify(timeline, times(1)).createUserTimelineRequest(isNull(Long.class),
                eq(TEST_MAX_ID - 1));
    }

    @Test
    public void testCreateUserTimelineRequest() {
        // build a timeline with test params
        final UserTimeline timeline = new UserTimeline(twitterCore, TestFixtures.TEST_USER.getId(),
                TestFixtures.TEST_USER.getScreenName(), TEST_ITEMS_PER_REQUEST, null, null);

        // create a request directly
        timeline.createUserTimelineRequest(TEST_SINCE_ID, TEST_MAX_ID);

        // assert userTimeline call is made with the correct arguments
        verify(twitterCore.getApiClient().getStatusesService())
                .userTimeline(eq(TestFixtures.TEST_USER.getId()),
                        eq(TestFixtures.TEST_USER.getScreenName()), eq(TEST_ITEMS_PER_REQUEST),
                        eq(TEST_SINCE_ID), eq(TEST_MAX_ID), eq(false), eq(true),
                        isNull(Boolean.class), isNull(Boolean.class));
    }

    @Test
    public void testGetScribeSection() {
        final UserTimeline timeline = new UserTimeline.Builder(twitterCore).build();
        Assert.assertThat(timeline.getTimelineType(), is(REQUIRED_IMPRESSION_SECTION));
    }

    /* Builder */
    @Test
    public void testBuilder() {
        final UserTimeline timeline = new UserTimeline.Builder(twitterCore)
                .userId(TestFixtures.TEST_USER.getId())
                .screenName(TestFixtures.TEST_USER.getScreenName())
                .maxItemsPerRequest(TEST_ITEMS_PER_REQUEST)
                .includeReplies(true)
                .includeRetweets(true)
                .build();
        Assert.assertThat(timeline.userId, is(TestFixtures.TEST_USER.getId()));
        Assert.assertThat(timeline.screenName, is(TestFixtures.TEST_USER.getScreenName()));
        Assert.assertThat(timeline.maxItemsPerRequest, is(TEST_ITEMS_PER_REQUEST));
        Assert.assertThat(timeline.includeReplies, is(true));
        Assert.assertThat(timeline.includeRetweets, is(true));
    }

    @Test
    // api arguments should default to Null to allow the backend to determine default behavior
    public void testBuilder_defaults() {
        final UserTimeline timeline = new UserTimeline.Builder(twitterCore).build();
        Assert.assertThat(timeline.userId, nullValue());
        Assert.assertThat(timeline.screenName, nullValue());
        Assert.assertThat(timeline.maxItemsPerRequest, is(REQUIRED_DEFAULT_ITEMS_PER_REQUEST));
        Assert.assertThat(timeline.includeRetweets, nullValue());
        // parameters which default to false
        Assert.assertThat(timeline.includeReplies, is(false));
    }

    @Test
    public void testBuilder_userId() {
        final Long USER_ID = TestFixtures.TEST_USER.getId();
        final UserTimeline timeline = new UserTimeline.Builder(twitterCore)
                .userId(USER_ID)
                .build();
        Assert.assertThat(timeline.userId, is(USER_ID));
    }

    @Test
    public void testBuilder_screenName() {
        final UserTimeline timeline = new UserTimeline.Builder(twitterCore)
                .screenName(TestFixtures.TEST_USER.getScreenName())
                .build();
        Assert.assertThat(timeline.screenName, is(TestFixtures.TEST_USER.getScreenName()));
    }

    @Test
    public void testBuilder_maxItemsPerRequest() {
        final UserTimeline timeline = new UserTimeline.Builder(twitterCore)
                .maxItemsPerRequest(TEST_ITEMS_PER_REQUEST)
                .build();
        Assert.assertThat(timeline.maxItemsPerRequest, is(TEST_ITEMS_PER_REQUEST));
    }

    @Test
    public void testBuilder_includeReplies() {
        // null includeReplies defaults to false
        UserTimeline timeline = new UserTimeline.Builder(twitterCore).build();
        Assert.assertThat(timeline.includeReplies, is(false));
        timeline = new UserTimeline.Builder(twitterCore).includeReplies(true).build();
        Assert.assertThat(timeline.includeReplies, is(true));
        timeline = new UserTimeline.Builder(twitterCore).includeReplies(false).build();
        Assert.assertThat(timeline.includeReplies, is(false));
    }

    @Test
    public void testBuilder_includeRetweets() {
        UserTimeline timeline = new UserTimeline.Builder(twitterCore).build();
        Assert.assertThat(timeline.includeRetweets, nullValue());
        timeline = new UserTimeline.Builder(twitterCore).includeRetweets(true).build();
        Assert.assertThat(timeline.includeRetweets, is(true));
        timeline = new UserTimeline.Builder(twitterCore).includeRetweets(false).build();
        Assert.assertThat(timeline.includeRetweets, is(false));
    }
}
