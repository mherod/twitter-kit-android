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
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.TwitterTestUtils;
import com.twitter.sdk.android.core.models.Card;
import com.twitter.sdk.android.core.models.ImageValue;
import com.twitter.sdk.android.core.models.MediaEntity;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.internal.AspectRatioFrameLayout;
import com.twitter.sdk.android.tweetui.internal.TweetMediaView;

import org.junit.Assert;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the state of BaseTweetViews created via constructors.
 */
public abstract class BaseTweetViewTest extends TweetUiTestCase {
    private static final String REQUIRED_RETWEETED_BY_TEXT = "Retweeted by Mr Retweets";
    protected static final double DELTA = 0.001f;
    protected static final String ALT_TEXT = "ALT_TEXT";

    protected Context context;
    private Resources resources;
    private Locale defaultLocale;
    protected BaseTweetView.DependencyProvider mockDependencyProvider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = getContext();
        resources = context.getResources();
        defaultLocale = TestUtils.setLocale(getContext(), Locale.ENGLISH);
        setUpMockDependencyProvider();
    }

    @SuppressWarnings("SuperTearDownInFinally")
    @Override
    protected void tearDown() throws Exception {
        TestUtils.setLocale(getContext(), defaultLocale);
        scrubClass(BaseTweetViewTest.class);
        super.tearDown();
    }

    public Resources getResources() {
        return resources;
    }

    // constructor factories

    abstract BaseTweetView createView(Context context, Tweet tweet);

    abstract BaseTweetView createView(Context context, Tweet tweet, int styleResId);

    abstract BaseTweetView createViewInEditMode(Context context, Tweet tweet);

    abstract BaseTweetView createViewWithMocks(Context context, Tweet tweet);

    abstract BaseTweetView createViewWithMocks(Context context, Tweet tweet, int styleResId,
            BaseTweetView.DependencyProvider dependencyProvider);

    private void setUpMockDependencyProvider() {
        mockDependencyProvider = mock(TestDependencyProvider.class);
        when(mockDependencyProvider.getImageLoader())
                .thenReturn(TweetUi.getInstance().getImageLoader());
        when(mockDependencyProvider.getTweetUi()).thenReturn(TweetUi.getInstance());
        when(mockDependencyProvider.getTweetScribeClient()).thenReturn(scribeClient);
    }

    // initialization

    public void testInit() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET);
        final long tweetId = TestFixtures.TEST_TWEET.getId();
        Assert.assertThat(view.getTweetId(), is(tweetId));
        Assert.assertThat(view.fullNameView.getText().toString(), is(TestFixtures.TEST_NAME));
        Assert.assertThat(view.screenNameView.getText(), is(TestFixtures.TEST_FORMATTED_SCREEN_NAME));
        Assert.assertThat(view.contentView.getText().toString(), is(TestFixtures.TEST_STATUS));
        Assert.assertThat(view.timestampView.getText().toString(), is(TestFixtures.TIMESTAMP_RENDERED));
    }

    public void testInit_withEmptyTweet() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET);
        // recycle so we're not relying on first time defaults, fields should clear
        view.setTweet(TestFixtures.EMPTY_TWEET);
        Assert.assertThat(view.getTweetId(), is(TestFixtures.EMPTY_TWEET.getId()));
        Assert.assertThat(view.fullNameView.getText().toString(), is(TestFixtures.EMPTY_STRING));
        Assert.assertThat(view.screenNameView.getText().toString(), is(TestFixtures.EMPTY_STRING));
        Assert.assertThat(view.contentView.getText().toString(), is(TestFixtures.EMPTY_STRING));
        Assert.assertThat(view.timestampView.getText().toString(), is(TestFixtures.EMPTY_STRING));
    }

    public void testInit_withNullTweet() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET);
        // recycle so we're not relying on first time defaults, fields should clear
        view.setTweet(null);
        Assert.assertThat(view.getTweetId(), is(TestFixtures.EMPTY_TWEET.getId()));
        Assert.assertThat(view.fullNameView.getText().toString(), is(TestFixtures.EMPTY_STRING));
        Assert.assertThat(view.screenNameView.getText().toString(), is(TestFixtures.EMPTY_STRING));
        Assert.assertThat(view.contentView.getText().toString(), is(TestFixtures.EMPTY_STRING));
        Assert.assertThat(view.timestampView.getText().toString(), is(TestFixtures.EMPTY_STRING));
    }

    // setTweet with a Tweet with an invalid timestamp makes timestamp view show an empty string
    public void testInit_withInvalidTimestamp() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET);
        // recycle so we're not relying on first time defaults, timestamp should clear
        view.setTweet(TestFixtures.INVALID_TIMESTAMP_TWEET);
        Assert.assertThat(view.getTweetId(), is(TestFixtures.INVALID_TIMESTAMP_TWEET.getId()));
        Assert.assertThat(view.timestampView.getText().toString(), is(TestFixtures.EMPTY_STRING));
    }

    public void testInit_inEditMode() {
        TwitterTestUtils.resetTwitter();
        try {
            final BaseTweetView view = createViewInEditMode(context, TestFixtures.TEST_TWEET);
            Assert.assertThat(view.isInEditMode(), is(true));
            Assert.assertThat(view.isEnabled(), is(true));
        } catch (Exception e) {
            Assert.fail("Must start TweetUi... IllegalStateException should be caught");
        } finally {
            TwitterTestUtils.resetTwitter();
        }
    }

    public void testIsTweetUiEnabled_withEditMode() {
        final BaseTweetView view = createView(getContext(), TestFixtures.TEST_TWEET);
        Assert.assertThat(view.isTweetUiEnabled(), is(true));
    }

    public void testIsTweetUiEnabled_inEditMode() {
        final BaseTweetView view = createViewInEditMode(getContext(), TestFixtures.TEST_TWEET);
        Assert.assertThat(view.isTweetUiEnabled(), is(false));
    }

    public void testIsTweetUiEnabled_tweetUiStarted() {
        final BaseTweetView view = new TweetView(getContext(), TestFixtures.TEST_TWEET);
        Assert.assertThat(view.isTweetUiEnabled(), is(true));
        Assert.assertThat(view.isEnabled(), is(true));
    }

    // Tests Date formatting reliant string, manually sets english and restores original locale
    public void testGetContentDescription_emptyTweet() {
        final Locale originalLocale = TestUtils.setLocale(getContext(), Locale.ENGLISH);
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET);
        view.setTweet(TestFixtures.EMPTY_TWEET);
        Assert.assertThat(view.getContentDescription(), is(getResources().getString(R.string.tw__loading_tweet)));
        TestUtils.setLocale(getContext(), originalLocale);
    }

    // Tests Date formatting reliant string, manually sets english and restores original locale
    public void testGetContentDescription_fullTweet() {
        final Locale originalLocale = TestUtils.setLocale(getContext(), Locale.ENGLISH);

        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET);
        Assert.assertThat(TweetUtils.isTweetResolvable(view.tweet), is(true));
        Assert.assertThat(view.getContentDescription(), is(TestFixtures.TEST_CONTENT_DESCRIPTION));

        TestUtils.setLocale(getContext(), originalLocale);
    }

    public void testSetTweetMediaClickListener() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET_LINK);
        view.setTweetMediaClickListener((tweet, entity) -> {

        });

        Assert.assertThat(view.tweetMediaClickListener, notNullValue());
    }

    public void testSetTweetLinkClickListener() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET_LINK);
        final TweetLinkClickListener linkClickListener = mock(TweetLinkClickListener.class);
        view.setTweetLinkClickListener(linkClickListener);

        Assert.assertThat(view.tweetLinkClickListener, notNullValue());

        view.getLinkClickListener().onUrlClicked(TestFixtures.TEST_URL);
        verify(linkClickListener).onLinkClick(TestFixtures.TEST_TWEET_LINK, TestFixtures.TEST_URL);
    }

    public void testSetTweet_defaultClickListener() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET_LINK);

        Assert.assertThat(view.tweetLinkClickListener, nullValue());
    }

    // Permalink click
    public void testSetTweet_permalink() {
        final BaseTweetView view = createView(context, null);
        view.setTweet(TestFixtures.TEST_TWEET);
        Assert.assertThat(view.getPermalinkUri().toString(), is(TestFixtures.TEST_PERMALINK_ONE));
    }

    // permalinkUri should be null so the permalink launcher will be a NoOp
    public void testSetTweet_nullTweetPermalink() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET);
        view.setTweet(null);
        Assert.assertThat(view.getPermalinkUri(), nullValue());
    }

    public void testSetTweet_updatePermalink() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET);
        Assert.assertThat(view.getPermalinkUri().toString(), is(TestFixtures.TEST_PERMALINK_ONE));
        view.setTweet(TestFixtures.TEST_PHOTO_TWEET);
        Assert.assertThat(view.getPermalinkUri().toString(), is(TestFixtures.TEST_PERMALINK_TWO));
    }

    // Styling
    // light style (default)

    public void testStaticColorsDefault() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET);
        TweetAsserts.assertDefaultColors(view, getResources());
    }

    public void testSecondaryColorsDefault() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET);
        final int primaryTextColor = getResources().getColor(
                R.color.tw__tweet_light_primary_text_color);
        final int color = ColorUtils.calculateOpacityTransform(
                BaseTweetView.SECONDARY_TEXT_COLOR_LIGHT_OPACITY, Color.WHITE, primaryTextColor);
        Assert.assertThat(view.secondaryTextColor, is(color));
        Assert.assertThat(view.timestampView.getCurrentTextColor(), is(color));
        Assert.assertThat(view.screenNameView.getCurrentTextColor(), is(color));
        Assert.assertThat(view.retweetedByView.getCurrentTextColor(), is(color));
    }

    public void testAvatarDefault() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET);
        final int containerColor = getResources().getColor(
                R.color.tw__tweet_light_container_bg_color);
        final int color = ColorUtils.calculateOpacityTransform(
                BaseTweetView.MEDIA_BG_LIGHT_OPACITY, Color.BLACK, containerColor);
        Assert.assertThat(TestUtils.getDrawableColor(view.avatarView), is(color));
    }

    public void testPhotoDefault() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_PHOTO_TWEET);
        final int containerColor = getResources().getColor(
                R.color.tw__tweet_light_container_bg_color);
        final int color = ColorUtils.calculateOpacityTransform(
                BaseTweetView.MEDIA_BG_LIGHT_OPACITY, Color.BLACK, containerColor);

        final MediaEntity entity = TestFixtures.createMediaEntityWithPhoto(100, 100);
        final List<MediaEntity> mediaEntities = new ArrayList<>();
        mediaEntities.add(entity);

        final TweetMediaView tweetMediaView = view.tweetMediaView;
        tweetMediaView.setTweetMediaEntities(TestFixtures.TEST_TWEET, mediaEntities);

        final ImageView imageView = (ImageView) tweetMediaView.getChildAt(0);
        Assert.assertThat(TestUtils.getBackgroundColor(imageView), is(color));
    }

    public void testTweetPhotoErrorDefault() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_PHOTO_TWEET);
        Assert.assertThat(view.photoErrorResId, is(R.drawable.tw__ic_tweet_photo_error_light));
    }

    public void testRetweetIconDefault() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_RETWEET);
        Assert.assertThat(view.retweetIconResId, is(R.drawable.tw__ic_retweet_light));
    }

    // dark style

    public void testStaticColorsDark() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET,
                R.style.tw__TweetDarkStyle);
        TweetAsserts.assertDarkColors(view, getResources());
    }

    public void testSecondaryColorsDark() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET,
                R.style.tw__TweetDarkStyle);
        final int primaryTextColor = getResources().getColor(
                R.color.tw__tweet_dark_primary_text_color);
        final int color = ColorUtils.calculateOpacityTransform(
                BaseTweetView.SECONDARY_TEXT_COLOR_DARK_OPACITY, Color.BLACK, primaryTextColor);
        Assert.assertThat(view.secondaryTextColor, is(color));
        Assert.assertThat(view.timestampView.getCurrentTextColor(), is(color));
        Assert.assertThat(view.screenNameView.getCurrentTextColor(), is(color));
        Assert.assertThat(view.retweetedByView.getCurrentTextColor(), is(color));
    }

    public void testAvatarDark() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET,
                R.style.tw__TweetDarkStyle);
        final int containerColor = getResources().getColor(
                R.color.tw__tweet_dark_container_bg_color);
        final int color = ColorUtils.calculateOpacityTransform(
                BaseTweetView.MEDIA_BG_DARK_OPACITY, Color.WHITE, containerColor);
        Assert.assertThat(TestUtils.getDrawableColor(view.avatarView), is(color));
    }

    public void testPhotoDark() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_PHOTO_TWEET,
                R.style.tw__TweetDarkStyle);
        final int containerColor = getResources().getColor(
                R.color.tw__tweet_dark_container_bg_color);
        final int color = ColorUtils.calculateOpacityTransform(
                BaseTweetView.MEDIA_BG_DARK_OPACITY, Color.WHITE, containerColor);

        final MediaEntity entity = TestFixtures.createMediaEntityWithPhoto(100, 100);
        final List<MediaEntity> mediaEntities = new ArrayList<>();
        mediaEntities.add(entity);
        final TweetMediaView tweetMediaView = view.tweetMediaView;
        tweetMediaView.setTweetMediaEntities(TestFixtures.TEST_TWEET, mediaEntities);

        final ImageView imageView = (ImageView) tweetMediaView.getChildAt(0);
        Assert.assertThat(TestUtils.getBackgroundColor(imageView), is(color));
    }

    public void testTweetPhotoErrorDark() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET,
                R.style.tw__TweetDarkStyle);
        Assert.assertThat(view.photoErrorResId, is(R.drawable.tw__ic_tweet_photo_error_dark));
    }

    public void testRetweetIconDark() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_RETWEET,
               R.style.tw__TweetDarkStyle);
        Assert.assertThat(view.retweetIconResId, is(R.drawable.tw__ic_retweet_dark));
    }

    public void testTweetActionsEnabled() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET,
                R.style.tw__TweetActionsEnabled);
        Assert.assertThat(view.tweetActionsEnabled, is(true));
    }

    public void testSetOnActionCallback_passesCorrectTweetToActionBarView() {
        final BaseTweetView tweetView = createView(context, TestFixtures.TEST_RETWEET,
                R.style.tw__TweetActionsEnabled);
        final TweetActionBarView mockActionBarView = mock(TestTweetActionBarView.class);
        tweetView.tweetActionBarView = mockActionBarView;
        doNothing().when(mockActionBarView).setLike(any(Tweet.class));
        final Callback<Tweet> mockCallback = mock(Callback.class);
        tweetView.setOnActionCallback(mockCallback);
        // verify that the TweetActionBarView is set with the Tweet, not the inner retweeted Tweet
        final ArgumentCaptor<Tweet> tweetCaptor = ArgumentCaptor.forClass(Tweet.class);
        verify(mockActionBarView).setTweet(tweetCaptor.capture());
        Assert.assertThat(tweetCaptor.getValue().getId(), is(TestFixtures.TEST_RETWEET.getId()));
    }

    public void testRender_passesCorrectTweetToActionBarView() {
        final BaseTweetView tweetView = createView(context, TestFixtures.TEST_RETWEET,
                R.style.tw__TweetActionsEnabled);
        final TweetActionBarView mockActionBarView = mock(TestTweetActionBarView.class);
        tweetView.tweetActionBarView = mockActionBarView;
        doNothing().when(mockActionBarView).setLike(any(Tweet.class));
        tweetView.render();
        // verify that the TweetActionBarView is set with the Tweet, not the inner retweeted Tweet
        final ArgumentCaptor<Tweet> tweetCaptor = ArgumentCaptor.forClass(Tweet.class);
        verify(mockActionBarView).setTweet(tweetCaptor.capture());
        Assert.assertThat(tweetCaptor.getValue().getId(), is(TestFixtures.TEST_RETWEET.getId()));
    }

    public void testTweetActionsDisabled() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET,
                R.style.tw__TweetActionsDisabled);
        Assert.assertThat(view.tweetActionsEnabled, is(false));
    }

    public void testGetAspectRatio_withNullMediaEntity() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET);
        final MediaEntity mediaEntity = null;
        Assert.assertEquals(BaseTweetView.DEFAULT_ASPECT_RATIO, view.getAspectRatio(mediaEntity), 0.0);
    }

    public void testGetAspectRatio_withNullImageValue() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET);
        final ImageValue imageValue = null;
        Assert.assertEquals(BaseTweetView.DEFAULT_ASPECT_RATIO, view.getAspectRatio(imageValue), 0.0);
    }

    public void testGetAspectRatio_mediaEntityWithNullSizes() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET);
        final MediaEntity mediaEntity = TestFixtures.createMediaEntityWithPhoto(null);

        Assert.assertEquals(BaseTweetView.DEFAULT_ASPECT_RATIO, view.getAspectRatio(mediaEntity), 0.0);
    }

    public void testGetAspectRatio_mediaEntityWithEmptySizes() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET);
        final MediaEntity.Sizes sizes = new MediaEntity.Sizes(null, null, null, null);
        final MediaEntity mediaEntity = TestFixtures.createMediaEntityWithPhoto(sizes);

        Assert.assertEquals(BaseTweetView.DEFAULT_ASPECT_RATIO, view.getAspectRatio(mediaEntity), 0.0);
    }

    public void testGetAspectRatio_mediaEntityWithZeroDimension() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET);

        Assert.assertEquals(BaseTweetView.DEFAULT_ASPECT_RATIO,
                view.getAspectRatio(TestFixtures.createMediaEntityWithPhoto(0, 0)), 0.0);
        Assert.assertEquals(BaseTweetView.DEFAULT_ASPECT_RATIO,
                view.getAspectRatio(TestFixtures.createMediaEntityWithPhoto(100, 0)), 0.0);
        Assert.assertEquals(BaseTweetView.DEFAULT_ASPECT_RATIO,
                view.getAspectRatio(TestFixtures.createMediaEntityWithPhoto(0, 100)), 0.0);
    }

    // Scribing
    private BaseTweetView setUpScribeTest() {
        return createViewWithMocks(context, null, R.style.tw__TweetDarkStyle,
                mockDependencyProvider);
    }

    public void testScribeImpression() {
        final BaseTweetView view = setUpScribeTest();
        view.tweet = TestFixtures.TEST_TWEET;

        view.scribeImpression();

        verify(scribeClient).impression(TestFixtures.TEST_TWEET, view.getViewTypeName(), false);
    }

    public void testScribePermalinkClick() {
        final BaseTweetView view = setUpScribeTest();
        view.tweet = TestFixtures.TEST_TWEET;

        view.scribePermalinkClick();

        verify(scribeClient).click(TestFixtures.TEST_TWEET, view.getViewTypeName());
    }

    public void testSetProfilePhotoView_handlesNullPicasso() {
        when(mockDependencyProvider.getImageLoader()).thenReturn(null);

        final BaseTweetView tweetView = createViewWithMocks(context, TestFixtures.TEST_TWEET,
                R.style.tw__TweetDarkStyle, mockDependencyProvider);

        try {
            tweetView.setProfilePhotoView(TestFixtures.TEST_TWEET);
        } catch (NullPointerException e) {
            org.junit.Assert.fail("Should have handled null error image");
        }
    }

    public void testSetTweetMedia_handlesNullPicasso() {
        when(mockDependencyProvider.getImageLoader()).thenReturn(null);

        final BaseTweetView tweetView = createViewWithMocks(context, TestFixtures.TEST_TWEET,
                R.style.tw__TweetDarkStyle, mockDependencyProvider);

        try {
            tweetView.setTweetMedia(mock(Tweet.class));
        } catch (NullPointerException e) {
            org.junit.Assert.fail("Should have handled null error image");
        }
    }

    public void testRender_forSinglePhotoEntity() {
        final BaseTweetView tweetView = createViewWithMocks(context, null);
        tweetView.setTweet(TestFixtures.TEST_PHOTO_TWEET);

        Assert.assertThat(tweetView.mediaContainer.getVisibility(), is(View.VISIBLE));
        Assert.assertThat(tweetView.tweetMediaView.getVisibility(), is(View.VISIBLE));
        Assert.assertThat(tweetView.mediaBadgeView.getVisibility(), is(View.GONE));
    }

    public void testRender_forMultiplePhotoEntities() {
        final BaseTweetView tweetView = createViewWithMocks(context, null);
        tweetView.setTweet(TestFixtures.TEST_MULTIPLE_PHOTO_TWEET);

        Assert.assertThat(tweetView.mediaContainer.getVisibility(), is(View.VISIBLE));
        Assert.assertThat(tweetView.tweetMediaView.getVisibility(), is(View.VISIBLE));
        Assert.assertThat(tweetView.mediaBadgeView.getVisibility(), is(View.GONE));
    }

    public void testRender_rendersRetweetedStatus() {
        final BaseTweetView tweetView = createViewWithMocks(context, null);
        tweetView.setTweet(TestFixtures.TEST_RETWEET);
        Assert.assertThat(tweetView.retweetedByView.getText(), is(REQUIRED_RETWEETED_BY_TEXT));
        Assert.assertThat(tweetView.fullNameView.getText(), is(TestFixtures.TEST_NAME));
        Assert.assertThat(tweetView.screenNameView.getText(), is(TestFixtures.TEST_FORMATTED_SCREEN_NAME));
        Assert.assertThat(tweetView.contentView.getText().toString(), is(TestFixtures.TEST_STATUS));
    }

    public void testSetRetweetedBy_nullTweet() {
        final BaseTweetView tweetView = createViewWithMocks(context, null);
        tweetView.setTweet(null);
        Assert.assertThat(tweetView.retweetedByView.getVisibility(), is(View.GONE));
    }

    public void testSetRetweetedBy_nonRetweet() {
        final BaseTweetView tweetView = createViewWithMocks(context, null);
        tweetView.setTweet(TestFixtures.TEST_TWEET);
        Assert.assertThat(tweetView.retweetedByView.getVisibility(), is(View.GONE));
    }

    public void testSetRetweetedBy_retweet() {
        final BaseTweetView tweetView = createViewWithMocks(context, null);
        tweetView.setTweet(TestFixtures.TEST_RETWEET);
        Assert.assertThat(tweetView.retweetedByView.getVisibility(), is(View.VISIBLE));
        Assert.assertThat(tweetView.retweetedByView.getText(), is(REQUIRED_RETWEETED_BY_TEXT));
    }

    public void testRender_rendersVineCard() {
        final BaseTweetView view = createViewWithMocks(context, null);
        final Card sampleVineCard = TestFixtures.sampleValidVineCard();
        final Tweet tweetWithVineCard = TestFixtures.createTweetWithVineCard(
                TestFixtures.TEST_TWEET_ID, TestFixtures.TEST_USER,
                TestFixtures.TEST_STATUS, sampleVineCard);

        view.setTweet(tweetWithVineCard);

        Assert.assertThat(view.fullNameView.getText().toString(), is(TestFixtures.TEST_NAME));
        Assert.assertThat(view.screenNameView.getText(), is(TestFixtures.TEST_FORMATTED_SCREEN_NAME));
        Assert.assertThat(view.contentView.getText().toString(), is(TestFixtures.TEST_STATUS));
        Assert.assertThat(view.mediaContainer.getVisibility(), is(View.VISIBLE));
        Assert.assertThat(view.mediaBadgeView.getVisibility(), is(View.VISIBLE));
        Assert.assertThat(view.tweetMediaView.getVisibility(), is(View.VISIBLE));
    }

    public void testClearMedia() {
        final BaseTweetView view = createViewWithMocks(context, null);
        view.mediaContainer = mock(AspectRatioFrameLayout.class);

        view.clearTweetMedia();

        verify(view.mediaContainer).setVisibility(View.GONE);
    }
}
