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

import android.content.res.Resources;
import android.graphics.Color;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.junit.Assert;

import static org.hamcrest.CoreMatchers.*;

/**
 * Tests the state of BaseTweetViews created via XML.
 */
public abstract class BaseTweetViewXmlTest extends TweetUiTestCase {
    @Override
    protected void tearDown() throws Exception {
        scrubClass(BaseTweetViewXmlTest.class);
        super.tearDown();
    }

    // View without Tweet data attributes
    abstract BaseTweetView getView();

    // View with dark style attributes
    abstract BaseTweetView getViewDark();

    public Resources getResources() {
        return getContext().getResources();
    }

    protected View getInflatedLayout() {
        final ViewGroup group = new LinearLayout(getContext());
        return LayoutInflater.from(getContext())
                .inflate(R.layout.activity_tweet_view_test, group, true);
    }

    // Initialization

    // init without data attributes, subviews should have empty string values

    public void testInitName() {
        final BaseTweetView view = getView();
        Assert.assertThat(view, notNullValue());
        Assert.assertThat(view.fullNameView.getText().toString(), is(TestFixtures.EMPTY_STRING));
    }

    public void testInitScreenName() {
        final BaseTweetView view = getView();
        Assert.assertThat(view, notNullValue());
        Assert.assertThat(view.screenNameView.getText().toString(), is(TestFixtures.EMPTY_STRING));
    }

    public void testInitTimestamp() {
        final BaseTweetView view = getView();
        Assert.assertThat(view, notNullValue());
        Assert.assertThat(view.timestampView.getText().toString(), is(TestFixtures.EMPTY_STRING));
    }

    public void testInitText() {
        final BaseTweetView view = getView();
        Assert.assertThat(view, notNullValue());
        Assert.assertThat(view.contentView.getText().toString(), is(TestFixtures.EMPTY_STRING));
    }

    public void testInitWithTweetActionsDisabled() {
        final BaseTweetView view = getView();
        Assert.assertThat(view.tweetActionsEnabled, is(false));
    }

    public void testInitWithTweetActionsEnabled() {
        final ViewGroup group = new LinearLayout((getContext()));
        final View view = LayoutInflater.from(getContext()).inflate(
                R.layout.activity_tweet_actions_enabled, group, true);

        final BaseTweetView tweetView = view.findViewById(R.id.tweet_view);
        Assert.assertThat(tweetView.tweetActionsEnabled, is(true));
    }

    // asserts that a BaseTweetView with an invalid tweet id throws an exception
    public void testInitWithInvalidTweetId() {
        final ViewGroup group = new LinearLayout((getContext()));
        try {
            final View view = LayoutInflater.from(getContext()).inflate(
                    R.layout.activity_invalid_tweet_id, group, true);
            Assert.fail("InflateException not thrown");
        } catch (InflateException e) {
            // success
        }
    }

    // Permalink

    public void testSetTweet_permalink() {
        final BaseTweetView view = getView();
        Assert.assertThat(view.getPermalinkUri().toString(), is(TestFixtures.TEST_PERMALINK_UNKNOWN_USER));
    }

    // permalinkUri should be null so the permalink launcher will be a NoOp
    public void testSetTweet_nullTweetPermalink() {
        final BaseTweetView view = getView();
        view.setTweet(null);
        Assert.assertThat(view.getPermalinkUri(), nullValue());
    }

    public void testSetTweet_updatePermalink() {
        final BaseTweetView view = getView();
        view.setTweet(TestFixtures.TEST_PHOTO_TWEET);
        Assert.assertThat(view.getPermalinkUri().toString(), is(TestFixtures.TEST_PERMALINK_TWO));
    }

    // Styling
    // light style (default)

    public void testStaticColorsDefault() {
        final BaseTweetView view = getView();
        TweetAsserts.assertDefaultColors(view, getResources());
    }

    public void testSecondaryColorsDefault() {
        final BaseTweetView view = getView();
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
        final BaseTweetView view = getView();
        final int containerColor = getResources().getColor(
                R.color.tw__tweet_light_container_bg_color);
        final int color = ColorUtils.calculateOpacityTransform(
                BaseTweetView.MEDIA_BG_LIGHT_OPACITY, Color.BLACK, containerColor);
        Assert.assertThat(TestUtils.getDrawableColor(view.avatarView), is(color));
    }

    public void testPhotoErrorDefault() {
        final BaseTweetView view = getView();
        Assert.assertEquals(R.drawable.tw__ic_tweet_photo_error_light, view.photoErrorResId);
    }


    public void testRetweetIconDefault() {
        final BaseTweetView view = getView();
        Assert.assertEquals(R.drawable.tw__ic_retweet_light, view.retweetIconResId);
    }

    // dark style
    public void testStaticColorsDark() {
        final BaseTweetView view = getViewDark();
        TweetAsserts.assertDarkColors(view, getResources());
    }

    public void testSecondaryColorsDark() {
        final BaseTweetView view = getViewDark();
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
        final BaseTweetView view = getViewDark();
        final int containerColor = getResources().getColor(
                R.color.tw__tweet_dark_container_bg_color);
        final int color = ColorUtils.calculateOpacityTransform(
                BaseTweetView.MEDIA_BG_DARK_OPACITY, Color.WHITE, containerColor);
        Assert.assertThat(TestUtils.getDrawableColor(view.avatarView), is(color));
    }

    public void testPhotoErrorDark() {
        final BaseTweetView view = getViewDark();
        Assert.assertEquals(R.drawable.tw__ic_tweet_photo_error_dark, view.photoErrorResId);
    }

    public void testRetweetIconDark() {
        final BaseTweetView view = getViewDark();
        Assert.assertEquals(R.drawable.tw__ic_retweet_dark, view.retweetIconResId);
    }
}
