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

package com.twitter.sdk.android.tweetcomposer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.twitter.Validator;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.services.AccountService;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;

import retrofit2.Call;

import static com.twitter.sdk.android.tweetcomposer.TweetUploadService.TWEET_COMPOSE_CANCEL;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ComposerControllerTest {
    private static final String TWEET_TEXT = "some text";
    private static final int REMAINING_CHAR_COUNT = 131;
    private static final int OVERFLOW_REMAINING_CHAR_COUNT = -3;
    private static final String ANY_TEXT = "text";
    private static final String ANY_HASHTAG = "#hashtag";
    private ComposerController controller;
    private ComposerView mockComposerView;
    private Context mockContext;
    private TwitterAuthToken mockAuthToken;
    private TwitterSession mockTwitterSession;
    private AccountService mockAccountService;
    private ComposerActivity.Finisher mockFinisher;
    private ComposerScribeClient mockComposerScribeClient;
    private ComposerController.DependencyProvider mockDependencyProvider;

    @Before
    public void setUp() {
        mockComposerView = mock(ComposerView.class);
        mockContext = mock(Context.class);
        when(mockComposerView.getContext()).thenReturn(mockContext);

        mockFinisher = mock(ComposerActivity.Finisher.class);
        mockAuthToken = mock(TwitterAuthToken.class);
        mockTwitterSession = mock(TwitterSession.class);
        when(mockTwitterSession.getAuthToken()).thenReturn(mockAuthToken);

        final TwitterApiClient mockTwitterApiClient = mock(TwitterApiClient.class);
        mockAccountService = mock(AccountService.class);
        when(mockAccountService
                .verifyCredentials(any(Boolean.class), any(Boolean.class), any(Boolean.class)))
                .thenReturn(mock(Call.class));
        when(mockTwitterApiClient.getAccountService()).thenReturn(mockAccountService);

        mockComposerScribeClient = mock(ComposerScribeClient.class);

        mockDependencyProvider = mock(ComposerController.DependencyProvider.class);
        when(mockDependencyProvider.getApiClient(any(TwitterSession.class)))
                .thenReturn(mockTwitterApiClient);
        when(mockDependencyProvider.getTweetValidator()).thenReturn(new Validator());
        when(mockDependencyProvider.getScribeClient()).thenReturn(mockComposerScribeClient);
    }

    @Test
    public void testComposerController() {
        controller = new ComposerController(mockComposerView, mockTwitterSession, Uri.EMPTY,
                ANY_TEXT, ANY_HASHTAG, mockFinisher, mockDependencyProvider);
        Assert.assertThat(controller.session, is(mockTwitterSession));
        // assert that
        // - sets callbacks on the view
        // - sets initial Tweet text and cursor position
        // - gets a TwitterApiClient AccountService to set the profile photo
        // - sets card view in composer
        // - scribes a Tweet Composer impression
        verify(mockComposerView).setCallbacks(any(ComposerController.ComposerCallbacks.class));
        verify(mockComposerView).setTweetText(ANY_TEXT + " " + ANY_HASHTAG);
        verify(mockComposerView).setImageView(Uri.EMPTY);
        verify(mockDependencyProvider).getApiClient(mockTwitterSession);
        verify(mockAccountService).verifyCredentials(eq(false), eq(true), eq(false));
        verify(mockComposerScribeClient).impression();
    }

    @Test
    public void testTweetTextLength() {
        controller = new ComposerController(mockComposerView, mockTwitterSession, Uri.EMPTY,
                ANY_TEXT, ANY_HASHTAG, mockFinisher, mockDependencyProvider);

        Assert.assertThat(controller.tweetTextLength(null), is(0));
        Assert.assertThat(controller.tweetTextLength(""), is(0));
        Assert.assertThat(controller.tweetTextLength("☃"), is(1));
        Assert.assertThat(controller.tweetTextLength("tweet"), is(5));
        Assert.assertThat(controller.tweetTextLength("tweet with link https://example.com"), is(39));
        Assert.assertThat(controller.tweetTextLength("https://example.com/foo/bar/foo"), is(23));
    }

    @Test
    public void testRemainingCharCount() {
        Assert.assertThat(ComposerController.remainingCharCount(0), is(140));
        Assert.assertThat(ComposerController.remainingCharCount(1), is(139));
        Assert.assertThat(ComposerController.remainingCharCount(140), is(0));
        Assert.assertThat(ComposerController.remainingCharCount(141), is(-1));
    }

    @Test
    public void testIsPostEnabled() {
        Assert.assertThat(ComposerController.isPostEnabled(0), is(false));
        Assert.assertThat(ComposerController.isPostEnabled(1), is(true));
        Assert.assertThat(ComposerController.isPostEnabled(140), is(true));
        Assert.assertThat(ComposerController.isPostEnabled(141), is(false));
    }

    @Test
    public void testIsTweetTextOverflow() {
        Assert.assertThat(ComposerController.isTweetTextOverflow(0), is(false));
        Assert.assertThat(ComposerController.isTweetTextOverflow(1), is(false));
        Assert.assertThat(ComposerController.isTweetTextOverflow(140), is(false));
        Assert.assertThat(ComposerController.isTweetTextOverflow(141), is(true));
    }

    @Test
    public void testComposerCallbacksImpl_onTextChangedOk() {
        mockTwitterSession = mock(TwitterSession.class);
        controller = new ComposerController(mockComposerView, mockTwitterSession, Uri.EMPTY,
                ANY_TEXT, ANY_HASHTAG, mockFinisher, mockDependencyProvider);
        final ComposerController.ComposerCallbacks callbacks
                = controller.new ComposerCallbacksImpl();
        callbacks.onTextChanged(TWEET_TEXT);

        verify(mockComposerView).setCharCount(REMAINING_CHAR_COUNT);
        verify(mockComposerView).setCharCountTextStyle(R.style.tw__ComposerCharCount);
        verify(mockComposerView).postTweetEnabled(true);
    }

    @Test
    public void testComposerCallbacksImpl_onTextChangedOverflow() {
        final String OVERFLOW_TEXT = "This tweet is longer than 140 characters. This tweet is " +
                "longer than 140 characters. This tweet is longer than 140 characters. Overflow." +
                "Overflow";
        controller = new ComposerController(mockComposerView, mockTwitterSession, Uri.EMPTY,
                ANY_TEXT, ANY_HASHTAG, mockFinisher, mockDependencyProvider);
        final ComposerController.ComposerCallbacks callbacks
                = controller.new ComposerCallbacksImpl();
        callbacks.onTextChanged(OVERFLOW_TEXT);

        verify(mockComposerView).setCharCount(OVERFLOW_REMAINING_CHAR_COUNT);
        verify(mockComposerView).setCharCountTextStyle(R.style.tw__ComposerCharCountOverflow);
        verify(mockComposerView).postTweetEnabled(false);
    }

    @Test
    public void testComposerCallbacksImpl_onTweetPost() {
        final Context mockContext = mock(Context.class);
        when(mockComposerView.getContext()).thenReturn(mockContext);

        controller = new ComposerController(mockComposerView, mockTwitterSession, Uri.EMPTY,
                ANY_TEXT, ANY_HASHTAG, mockFinisher, mockDependencyProvider);
        final ComposerController.ComposerCallbacks callbacks
                = controller.new ComposerCallbacksImpl();
        callbacks.onTweetPost(TWEET_TEXT);
        // assert that
        // - context is used to start the TweetUploadService
        // - intent extras contain the session token and tweet text and card
        // - scribes a Tweet Composer Tweet Click
        final ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        verify(mockContext).startService(intentCaptor.capture());
        final Intent intent = intentCaptor.getValue();
        Assert.assertThat(intent.getComponent().getClassName(), is(TweetUploadService.class.getCanonicalName()));
        Assert.assertThat(intent.getParcelableExtra(TweetUploadService.EXTRA_USER_TOKEN), is(mockAuthToken));
        Assert.assertThat(intent.getParcelableExtra(TweetUploadService.EXTRA_IMAGE_URI), is(Uri.EMPTY));
        verify(mockComposerScribeClient).click(eq(ScribeConstants.SCRIBE_TWEET_ELEMENT));
    }

    @Test
    public void testComposerCallbacksImpl_onClose() {
        controller = new ComposerController(mockComposerView, mockTwitterSession, Uri.EMPTY,
                ANY_TEXT, ANY_HASHTAG, mockFinisher, mockDependencyProvider);
        final ComposerController.ComposerCallbacks callbacks
                = controller.new ComposerCallbacksImpl();
        callbacks.onCloseClick();
        // assert that
        // - finishes the activity
        // - scribes a Tweet Composer Cancel click
        verify(mockFinisher).finish();
        verify(mockComposerScribeClient).click(eq(ScribeConstants.SCRIBE_CANCEL_ELEMENT));
    }

    @Test
    public void testSendCancelBroadcast() {
        controller = new ComposerController(mockComposerView, mockTwitterSession, Uri.EMPTY,
                ANY_TEXT, ANY_HASHTAG, mockFinisher, mockDependencyProvider);
        final ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        controller.sendCancelBroadcast();
        verify(mockContext).sendBroadcast(intentCaptor.capture());

        final Intent capturedIntent = intentCaptor.getValue();
        Assert.assertThat(capturedIntent.getAction(), is(TWEET_COMPOSE_CANCEL));
    }
}
