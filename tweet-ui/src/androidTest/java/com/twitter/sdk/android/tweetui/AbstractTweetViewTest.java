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
import android.view.View;

import com.twitter.sdk.android.core.TwitterTestUtils;
import com.twitter.sdk.android.core.models.Card;
import com.twitter.sdk.android.core.models.ImageValue;
import com.twitter.sdk.android.core.models.MediaEntity;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.internal.AspectRatioFrameLayout;

import org.junit.Assert;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class AbstractTweetViewTest extends TweetUiTestCase {
    Context context;
    private Resources resources;
    private Locale defaultLocale;
    private AbstractTweetView.DependencyProvider mockDependencyProvider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = getContext();
        resources = context.getResources();
        defaultLocale = TestUtils.setLocale(getContext(), Locale.ENGLISH);
        setUpMockDependencyProvider();
    }

    @Override
    protected void tearDown() throws Exception {
        TestUtils.setLocale(getContext(), defaultLocale);
        scrubClass(AbstractTweetViewTest.class);
        super.tearDown();
    }

    private Resources getResources() {
        return resources;
    }

    // constructor factories
    abstract AbstractTweetView createView(Context context, Tweet tweet);

    abstract AbstractTweetView createViewInEditMode(Context context, Tweet tweet);

    abstract AbstractTweetView createViewWithMocks(Context context, Tweet tweet);

    abstract AbstractTweetView createViewWithMocks(Context context, Tweet tweet,
            AbstractTweetView.DependencyProvider dependencyProvider);

    private void setUpMockDependencyProvider() {
        mockDependencyProvider = mock(TestDependencyProvider.class);
        when(mockDependencyProvider.getImageLoader())
                .thenReturn(TweetUi.getInstance().getImageLoader());
        when(mockDependencyProvider.getTweetUi()).thenReturn(TweetUi.getInstance());
        when(mockDependencyProvider.getTweetScribeClient()).thenReturn(scribeClient);
    }

    // initialization

    public void testInit() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET);
        final long tweetId = TestFixtures.TEST_TWEET.getId();
        Assert.assertThat(view.getTweetId(), is(tweetId));
        Assert.assertThat(view.fullNameView.getText().toString(), is(TestFixtures.TEST_NAME));
        Assert.assertThat(view.screenNameView.getText(), is(TestFixtures.TEST_FORMATTED_SCREEN_NAME));
        Assert.assertThat(view.contentView.getText().toString(), is(TestFixtures.TEST_STATUS));
    }

    public void testInit_withEmptyTweet() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET);
        // recycle so we're not relying on first time defaults, fields should clear
        view.setTweet(TestFixtures.EMPTY_TWEET);
        Assert.assertThat(view.getTweetId(), is(TestFixtures.EMPTY_TWEET.getId()));
        Assert.assertThat(view.fullNameView.getText().toString(), is(TestFixtures.EMPTY_STRING));
        Assert.assertThat(view.screenNameView.getText().toString(), is(TestFixtures.EMPTY_STRING));
        Assert.assertThat(view.contentView.getText().toString(), is(TestFixtures.EMPTY_STRING));
    }

    public void testInit_withNullTweet() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET);
        // recycle so we're not relying on first time defaults, fields should clear
        view.setTweet(null);
        Assert.assertThat(view.getTweetId(), is(TestFixtures.EMPTY_TWEET.getId()));
        Assert.assertThat(view.fullNameView.getText().toString(), is(TestFixtures.EMPTY_STRING));
        Assert.assertThat(view.screenNameView.getText().toString(), is(TestFixtures.EMPTY_STRING));
        Assert.assertThat(view.contentView.getText().toString(), is(TestFixtures.EMPTY_STRING));
    }

    public void testInit_inEditMode() {
        TwitterTestUtils.resetTwitter();
        try {
            final AbstractTweetView view = createViewInEditMode(context, TestFixtures.TEST_TWEET);
            Assert.assertThat(view.isInEditMode(), is(true));
            Assert.assertThat(view.isEnabled(), is(true));
        } catch (Exception e) {
            Assert.fail("Must start TweetUi... IllegalStateException should be caught");
        } finally {
            TwitterTestUtils.resetTwitter();
        }
    }

    public void testIsTweetUiEnabled_withEditMode() {
        final AbstractTweetView view = createView(getContext(), TestFixtures.TEST_TWEET);
        Assert.assertThat(view.isTweetUiEnabled(), is(true));
    }

    public void testIsTweetUiEnabled_inEditMode() {
        final AbstractTweetView view = createViewInEditMode(getContext(), TestFixtures.TEST_TWEET);
        Assert.assertThat(view.isTweetUiEnabled(), is(false));
    }

    public void testIsTweetUiEnabled_tweetUiStarted() {
        final AbstractTweetView view = new TweetView(getContext(), TestFixtures.TEST_TWEET);
        Assert.assertThat(view.isTweetUiEnabled(), is(true));
        Assert.assertThat(view.isEnabled(), is(true));
    }

    // Tests Date formatting reliant string, manually sets english and restores original locale
    public void testGetContentDescription_emptyTweet() {
        final Locale originalLocale = TestUtils.setLocale(getContext(), Locale.ENGLISH);
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET);
        view.setTweet(TestFixtures.EMPTY_TWEET);
        Assert.assertThat(view.getContentDescription(), is(getResources().getString(R.string.tw__loading_tweet)));
        TestUtils.setLocale(getContext(), originalLocale);
    }

    // Tests Date formatting reliant string, manually sets english and restores original locale
    public void testGetContentDescription_fullTweet() {
        final Locale originalLocale = TestUtils.setLocale(getContext(), Locale.ENGLISH);

        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET);
        Assert.assertThat(TweetUtils.isTweetResolvable(view.tweet), is(true));
        Assert.assertThat(view.getContentDescription(), is(TestFixtures.TEST_CONTENT_DESCRIPTION));

        TestUtils.setLocale(getContext(), originalLocale);
    }

    public void testSetTweetMediaClickListener() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET_LINK);
        view.setTweetMediaClickListener((tweet, entity) -> {

        });

        Assert.assertThat(view.tweetMediaClickListener, notNullValue());
    }

    public void testSetTweetLinkClickListener() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET_LINK);
        final TweetLinkClickListener linkClickListener = mock(TweetLinkClickListener.class);
        view.setTweetLinkClickListener(linkClickListener);

        Assert.assertThat(view.tweetLinkClickListener, notNullValue());

        view.getLinkClickListener().onUrlClicked(TestFixtures.TEST_URL);
        verify(linkClickListener).onLinkClick(TestFixtures.TEST_TWEET_LINK, TestFixtures.TEST_URL);
    }

    public void testSetHashtagLinkClickListener() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET_HASHTAG);
        final TweetLinkClickListener linkClickListener = mock(TweetLinkClickListener.class);
        view.setTweetLinkClickListener(linkClickListener);

        Assert.assertThat(view.tweetLinkClickListener, notNullValue());

        view.getLinkClickListener().onUrlClicked(TestFixtures.TEST_HASHTAG);
        verify(linkClickListener).onLinkClick(TestFixtures.TEST_TWEET_HASHTAG,
                TestFixtures.TEST_HASHTAG);
    }

    public void testSetTweet_defaultClickListener() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET_LINK);

        Assert.assertThat(view.tweetLinkClickListener, nullValue());
    }

    // Permalink click
    public void testSetTweet_permalink() {
        final AbstractTweetView view = createView(context, null);
        view.setTweet(TestFixtures.TEST_TWEET);
        Assert.assertThat(view.getPermalinkUri().toString(), is(TestFixtures.TEST_PERMALINK_ONE));
    }

    // permalinkUri should be null so the permalink launcher will be a NoOp
    public void testSetTweet_nullTweetPermalink() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET);
        view.setTweet(null);
        Assert.assertThat(view.getPermalinkUri(), nullValue());
    }

    public void testSetTweet_updatePermalink() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET);
        Assert.assertThat(view.getPermalinkUri().toString(), is(TestFixtures.TEST_PERMALINK_ONE));
        view.setTweet(TestFixtures.TEST_PHOTO_TWEET);
        Assert.assertThat(view.getPermalinkUri().toString(), is(TestFixtures.TEST_PERMALINK_TWO));
    }

    public void testGetAspectRatio_withNullMediaEntity() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET);
        final MediaEntity mediaEntity = null;
        Assert.assertThat(view.getAspectRatio(mediaEntity), is(BaseTweetView.DEFAULT_ASPECT_RATIO));
    }

    public void testGetAspectRatio_withNullImageValue() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET);
        final ImageValue imageValue = null;
        Assert.assertEquals(BaseTweetView.DEFAULT_ASPECT_RATIO, view.getAspectRatio(imageValue), 0.0);
    }

    public void testGetAspectRatio_mediaEntityWithNullSizes() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET);
        final MediaEntity mediaEntity = TestFixtures.createMediaEntityWithPhoto(null);

        Assert.assertEquals(BaseTweetView.DEFAULT_ASPECT_RATIO, view.getAspectRatio(mediaEntity), 0.0);
    }

    public void testGetAspectRatio_mediaEntityWithEmptySizes() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET);
        final MediaEntity.Sizes sizes = new MediaEntity.Sizes(null, null, null, null);
        final MediaEntity mediaEntity = TestFixtures.createMediaEntityWithPhoto(sizes);

        Assert.assertEquals(BaseTweetView.DEFAULT_ASPECT_RATIO, view.getAspectRatio(mediaEntity), 0.0);
    }

    public void testGetAspectRatio_mediaEntityWithZeroDimension() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET);

        Assert.assertEquals(BaseTweetView.DEFAULT_ASPECT_RATIO,
                view.getAspectRatio(TestFixtures.createMediaEntityWithPhoto(0, 0)), 0.0);
        Assert.assertEquals(BaseTweetView.DEFAULT_ASPECT_RATIO,
                view.getAspectRatio(TestFixtures.createMediaEntityWithPhoto(100, 0)), 0.0);
        Assert.assertEquals(BaseTweetView.DEFAULT_ASPECT_RATIO,
                view.getAspectRatio(TestFixtures.createMediaEntityWithPhoto(0, 100)), 0.0);
    }

    // Scribing
    private AbstractTweetView setUpScribeTest() {
        return createViewWithMocks(context, null,
                mockDependencyProvider);
    }

    public void testScribeImpression() {
        final AbstractTweetView view = setUpScribeTest();
        view.tweet = TestFixtures.TEST_TWEET;

        view.scribeImpression();

        verify(scribeClient).impression(TestFixtures.TEST_TWEET, view.getViewTypeName(), false);
    }

    public void testScribePermalinkClick() {
        final AbstractTweetView view = setUpScribeTest();
        view.tweet = TestFixtures.TEST_TWEET;

        view.scribePermalinkClick();

        verify(scribeClient).click(TestFixtures.TEST_TWEET, view.getViewTypeName());
    }

    public void testSetTweetMedia_handlesNullPicasso() {
        when(mockDependencyProvider.getImageLoader()).thenReturn(null);

        final AbstractTweetView tweetView = createViewWithMocks(context, TestFixtures.TEST_TWEET,
                mockDependencyProvider);

        try {
            tweetView.setTweetMedia(mock(Tweet.class));
        } catch (NullPointerException e) {
            Assert.fail("Should have handled null error image");
        }
    }

    public void testRender_forSinglePhotoEntity() {
        final AbstractTweetView tweetView = createViewWithMocks(context, null);
        tweetView.setTweet(TestFixtures.TEST_PHOTO_TWEET);

        Assert.assertThat(tweetView.mediaContainer.getVisibility(), is(View.VISIBLE));
        Assert.assertThat(tweetView.tweetMediaView.getVisibility(), is(View.VISIBLE));
        Assert.assertThat(tweetView.mediaBadgeView.getVisibility(), is(View.GONE));
    }

    public void testRender_forMultiplePhotoEntities() {
        final AbstractTweetView tweetView = createViewWithMocks(context, null);
        tweetView.setTweet(TestFixtures.TEST_MULTIPLE_PHOTO_TWEET);

        Assert.assertThat(tweetView.mediaContainer.getVisibility(), is(View.VISIBLE));
        Assert.assertThat(tweetView.tweetMediaView.getVisibility(), is(View.VISIBLE));
        Assert.assertThat(tweetView.mediaBadgeView.getVisibility(), is(View.GONE));
    }

    public void testRender_rendersVineCard() {
        final AbstractTweetView view = createViewWithMocks(context, null);
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
        final AbstractTweetView view = createViewWithMocks(context, null);
        view.mediaContainer = mock(AspectRatioFrameLayout.class);

        view.clearTweetMedia();

        verify(view.mediaContainer).setVisibility(View.GONE);
    }
}

