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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;

import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.TweetBuilder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ShareTweetActionTest {

    private static final String REQUIRED_SEND_ACTION = Intent.ACTION_SEND;
    private static final String REQUIRED_MIME_TYPE = "text/plain";
    private static final String A_SHARE_SUBJECT =
            "Tweet from " + TestFixtures.TEST_NAME + " (@" + TestFixtures.TEST_SCREEN_NAME + ")";
    private static final String A_SHARE_TEXT
            = "Check out @" + TestFixtures.TEST_SCREEN_NAME + "'s Tweet: https://twitter.com/" +
            TestFixtures.TEST_SCREEN_NAME + "/status/" + TestFixtures.TEST_TWEET.getId();

    private ShareTweetAction listener;
    private Resources resources;
    private TweetUi mockTweetUi;
    private TweetScribeClient mockScribeClient;

    @Before
    public void setUp() throws Exception {
        mockTweetUi = mock(TweetUi.class);
        mockScribeClient = mock(TweetScribeClient.class);
        listener = new ShareTweetAction(TestFixtures.TEST_TWEET, mockTweetUi, mockScribeClient);
        resources = RuntimeEnvironment.application.getResources();
    }

    @Test
    public void testOnClick_nullTweet() {
        final ShareTweetAction listener = new ShareTweetAction(null, mockTweetUi);
        final Context context = mock(Context.class);
        listener.onClick(context, resources);
        verify(context, times(0)).startActivity(any(Intent.class));
        verifyNoMoreInteractions(mockTweetUi);
    }

    @Test
    public void testOnClick_nullTweetUser() {
        final ShareTweetAction listener =
                new ShareTweetAction(new TweetBuilder().build(), mockTweetUi);
        final Context context = mock(Context.class);
        listener.onClick(context, resources);
        verify(context, times(0)).startActivity(any(Intent.class));
        verifyNoMoreInteractions(mockTweetUi);
    }

    @Test
    public void testOnClick_tweetWithData() {
        final Context context = createContextWithPackageManager();
        listener.onClick(context, resources);
        verify(context, times(1)).startActivity(any(Intent.class));

        assertScribe();
    }

    private void assertScribe() {
        final ArgumentCaptor<Tweet> tweetCaptor
                = ArgumentCaptor.forClass(Tweet.class);

        verify(mockScribeClient).share(tweetCaptor.capture());
        Assert.assertThat(tweetCaptor.getValue(), is(TestFixtures.TEST_TWEET));
    }

    @Test
    public void testGetShareContent() {
        final String shareContent = listener.getShareContent(resources);
        Assert.assertThat(shareContent, is(A_SHARE_TEXT));
    }

    @Test
    public void testGetShareSubject() {
        final String shareSubject = listener.getShareSubject(resources);
        Assert.assertThat(shareSubject, is(A_SHARE_SUBJECT));
    }

    @Test
    public void testLaunchShareIntent_startsActivity() {
        final Intent intent = mock(Intent.class);
        final Context context = createContextWithPackageManager();
        listener.launchShareIntent(intent, context);
        verify(context, times(1)).startActivity(intent);
    }

    @Test
    public void testGetShareIntent() {
        final Intent intent = listener.getShareIntent(A_SHARE_SUBJECT, A_SHARE_TEXT);
        Assert.assertThat(intent.getAction(), is(REQUIRED_SEND_ACTION));
        Assert.assertThat(intent.getType(), is(REQUIRED_MIME_TYPE));
        Assert.assertThat(intent.getStringExtra(Intent.EXTRA_SUBJECT), is(A_SHARE_SUBJECT));
        Assert.assertThat(intent.getStringExtra(Intent.EXTRA_TEXT), is(A_SHARE_TEXT));
    }

    private Context createContextWithPackageManager() {
        final Context context = mock(Context.class);
        final PackageManager pm = mock(PackageManager.class);
        final List<ResolveInfo> activities = new ArrayList<>();
        activities.add(mock(ResolveInfo.class));

        when(pm.queryIntentActivities(any(Intent.class), anyInt())).thenReturn(activities);
        when(context.getPackageManager()).thenReturn(pm);

        return context;
    }
}
