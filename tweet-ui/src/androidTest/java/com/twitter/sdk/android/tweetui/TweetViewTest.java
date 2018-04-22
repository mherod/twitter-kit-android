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
import android.widget.ImageView;

import com.twitter.sdk.android.core.models.Tweet;

import org.junit.Assert;

import static org.hamcrest.CoreMatchers.is;

public class TweetViewTest extends BaseTweetViewTest {
    @Override
    TweetView createView(Context context, Tweet tweet) {
        return new TweetView(context, tweet);
    }

    @Override
    TweetView createView(Context context, Tweet tweet, int styleResId) {
        return new TweetView(context, tweet, styleResId);
    }

    @Override
    TweetView createViewInEditMode(Context context, Tweet tweet) {
        return new TweetView(context, tweet) {
            @Override
            public boolean isInEditMode() {
                return true;
            }
        };
    }

    @Override
    TweetView createViewWithMocks(Context context, Tweet tweet) {
        return new TweetView(context, tweet);
    }

    @Override
    TweetView createViewWithMocks(Context context, Tweet tweet, int styleResId,
            BaseTweetView.DependencyProvider dependencyProvider) {
        return new TweetView(context, tweet, styleResId, dependencyProvider);
    }

    // Initialization

    @Override
    public void testInit() {
        super.testInit();
        final TweetView view = createView(context, TestFixtures.TEST_TWEET);
        Assert.assertThat(view.mediaContainer.getVisibility(), is(ImageView.GONE));
    }

    @Override
    public void testInit_withEmptyTweet() {
        super.testInit();
        final TweetView view = createView(context, TestFixtures.TEST_TWEET);
        Assert.assertThat(view.mediaContainer.getVisibility(), is(ImageView.GONE));
    }

    public void testInit_withPhotoTweet() {
        final TweetView view = createView(context, TestFixtures.TEST_PHOTO_TWEET);
        Assert.assertThat(view.mediaContainer.getVisibility(), is(ImageView.VISIBLE));
    }

    // Layout
    public void testLayout() {
        final TweetView tweetView = new TweetView(context, TestFixtures.TEST_TWEET);
        Assert.assertThat(tweetView.getLayout(), is(R.layout.tw__tweet));
    }

    public void testGetAspectRatio() {
        final BaseTweetView view = createView(context, TestFixtures.TEST_TWEET);

        Assert.assertEquals(1, view.getAspectRatio(
                TestFixtures.createMediaEntityWithPhoto(100, 100)), DELTA);
        Assert.assertEquals(.5, view.getAspectRatio(
                TestFixtures.createMediaEntityWithPhoto(100, 200)), DELTA);
        Assert.assertEquals(2, view.getAspectRatio(
                TestFixtures.createMediaEntityWithPhoto(200, 100)), DELTA);
    }

    public void testGetAspectRatioForPhotoEntity() {
        final TweetView tweetView = createView(context, TestFixtures.TEST_PHOTO_TWEET);

        Assert.assertEquals(1.5, tweetView.getAspectRatioForPhotoEntity(1), 0.0);
        Assert.assertEquals(1.5, tweetView.getAspectRatioForPhotoEntity(2), 0.0);
        Assert.assertEquals(1.5, tweetView.getAspectRatioForPhotoEntity(3), 0.0);
        Assert.assertEquals(1.0, tweetView.getAspectRatioForPhotoEntity(4), 0.0);
    }
}
