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

import android.view.View;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.internal.scribe.EventNamespace;
import com.twitter.sdk.android.core.internal.scribe.ScribeItem;
import com.twitter.sdk.android.core.models.Identifiable;
import com.twitter.sdk.android.core.models.Tweet;

import org.junit.Assert;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TweetTimelineListAdapterTest extends TweetUiTestCase {
    private static final String NULL_CONTEXT_MESSAGE = "Context must not be null";
    private static final String NULL_TIMELINE_MESSAGE = "Timeline must not be null";
    private static final int ANY_STYLE = R.style.tw__TweetLightWithActionsStyle;
    private TweetTimelineListAdapter listAdapter;

    private static final String REQUIRED_SDK_IMPRESSION_CLIENT = "android";
    private static final String REQUIRED_SDK_IMPRESSION_PAGE = "timeline";
    private static final String REQUIRED_SDK_IMPRESSION_COMPONENT = "initial";
    private static final String REQUIRED_SDK_IMPRESSION_ELEMENT = "";
    private static final String REQUIRED_TFW_CLIENT = "tfw";
    private static final String REQUIRED_TFW_PAGE = "android";
    private static final String REQUIRED_TFW_SECTION = "timeline";
    private static final String REQUIRED_TFW_ELEMENT = "initial";
    private static final String REQUIRED_IMPRESSION_ACTION = "impression";
    private static final String TEST_SCRIBE_SECTION = "other";

    public void testConstructor() {
        final TimelineDelegate<Tweet> mockTimelineDelegate = mock(TestTimelineDelegate.class);
        final TweetUi tweetUi = mock(TweetUi.class);
        listAdapter = new TweetTimelineListAdapter(getContext(), mockTimelineDelegate, ANY_STYLE,
                null, tweetUi);
        if (listAdapter.getActionCallback() instanceof TweetTimelineListAdapter.ReplaceTweetCallback) {
            final TweetTimelineListAdapter.ReplaceTweetCallback replaceCallback
                    = (TweetTimelineListAdapter.ReplaceTweetCallback) listAdapter.getActionCallback();
            Assert.assertThat(replaceCallback.delegate, is(mockTimelineDelegate));
            Assert.assertThat(replaceCallback.cb, nullValue());
        } else {
            Assert.fail("Expected default actionCallback to be a ReplaceTweetCallback");
        }
    }

    public void testConstructor_withActionCallback() {
        final TimelineDelegate<Tweet> mockTimelineDelegate = mock(TestTimelineDelegate.class);
        final Callback<Tweet> mockCallback = mock(Callback.class);
        final TweetUi tweetUi = mock(TweetUi.class);
        listAdapter = new TweetTimelineListAdapter(getContext(), mockTimelineDelegate, ANY_STYLE,
                mockCallback, tweetUi);
        // assert that
        // - developer callback wrapped in a ReplaceTweetCallback
        if (listAdapter.getActionCallback() instanceof TweetTimelineListAdapter.ReplaceTweetCallback) {
            final TweetTimelineListAdapter.ReplaceTweetCallback replaceCallback
                    = (TweetTimelineListAdapter.ReplaceTweetCallback) listAdapter.getActionCallback();
            Assert.assertThat(replaceCallback.delegate, is(mockTimelineDelegate));
            Assert.assertThat(replaceCallback.cb, is(mockCallback));
        } else {
            Assert.fail("Expected actionCallback to be wrapped in ReplaceTweetCallback");
        }
    }

    public void testBuilder() {
        final Timeline<Tweet> mockTimeline = mock(Timeline.class);
        final Callback<Tweet> mockCallback = mock(Callback.class);
        listAdapter = new TweetTimelineListAdapter.Builder(getContext())
                .setTimeline(mockTimeline)
                .setOnActionCallback(mockCallback)
                .setViewStyle(R.style.tw__TweetDarkStyle)
                .build();
        Assert.assertThat(listAdapter.getStyleResId(), is(R.style.tw__TweetDarkStyle));
        if (listAdapter.getActionCallback() instanceof TweetTimelineListAdapter.ReplaceTweetCallback) {
            final TweetTimelineListAdapter.ReplaceTweetCallback replaceCallback
                    = (TweetTimelineListAdapter.ReplaceTweetCallback) listAdapter.getActionCallback();
            Assert.assertThat(replaceCallback.cb, is(mockCallback));
        } else {
            Assert.fail("Expected actionCallback to be wrapped in ReplaceTweetCallback");
        }
    }

    public void testBuilder_nullContext() {
        final Timeline<Tweet> mockTimeline = mock(Timeline.class);
        try {
            listAdapter = new TweetTimelineListAdapter.Builder(null).setTimeline(mockTimeline)
                    .build();
            Assert.fail("Null context should throw exception");
        } catch (IllegalArgumentException e) {
            Assert.assertThat(e.getMessage(), is(NULL_CONTEXT_MESSAGE));
        }
    }

    public void testBuilder_nullTimeline() {
        try {
            listAdapter = new TweetTimelineListAdapter.Builder(getContext()).setTimeline(null)
                    .build();
            org.junit.Assert.fail("Null timeline should throw exception");
        } catch (IllegalArgumentException e) {
            Assert.assertThat(e.getMessage(), is(NULL_TIMELINE_MESSAGE));
        }
    }

    public void testBuilder_withTimelineFilter() {
        final Timeline<Tweet> mockTimeline = mock(Timeline.class);
        final TimelineFilter mockTimelineFilter = mock(TimelineFilter.class);
        listAdapter = new TweetTimelineListAdapter.Builder(getContext())
                .setTimeline(mockTimeline)
                .setTimelineFilter(mockTimelineFilter)
                .build();

        Assert.assertThat(listAdapter.delegate instanceof FilterTimelineDelegate, is(true));
    }

    public void testBuilder_withNullTimelineFilter() {
        final Timeline<Tweet> mockTimeline = mock(Timeline.class);
        listAdapter = new TweetTimelineListAdapter.Builder(getContext())
                .setTimeline(mockTimeline)
                .setTimelineFilter(null)
                .build();

        Assert.assertThat(listAdapter.delegate instanceof TimelineDelegate, is(true));
    }

    /**
     * Requires TweetUi to be setup by the test class. Without TweetUi, TweetView construction
     * returns before calling setTweet to support IDE edit mode, so getTweetId would always be -1.
     */
    public void testGetView_getsCompactTweetView() {
        final Timeline<Tweet> fakeTimeline = new FakeTweetTimeline(10);
        final TimelineDelegate<Tweet> fakeDelegate = new TimelineDelegate<>(fakeTimeline);
        listAdapter = new TweetTimelineListAdapter(getContext(), fakeDelegate, ANY_STYLE,
                null, tweetUi);

        final View view = listAdapter.getView(0, null, null);
        // assert that
        // - default implementation of getView returns a CompactTweetView
        // - sanity check that CompactTweetView tweet id matches the Tweet's id
        Assert.assertThat(view.getClass(), sameInstance(CompactTweetView.class));
        final BaseTweetView tv = (BaseTweetView) view;
        Assert.assertThat(tv.getTweetId(), is(listAdapter.getItemId(0)));
    }

    public void testDefaultViewStyle_viaConstructor() {
        final Timeline<Tweet> fakeTimeline = new FakeTweetTimeline(10);
        listAdapter = new TweetTimelineListAdapter(getContext(), fakeTimeline);
        final View view = listAdapter.getView(0, null, null);
        final BaseTweetView tv = (BaseTweetView) view;
        Assert.assertThat(tv.styleResId, is(R.style.tw__TweetLightStyle));
    }

    public void testDefaultViewStyle_viaBuilder() {
        final Timeline<Tweet> fakeTimeline = new FakeTweetTimeline(10);
        listAdapter = new TweetTimelineListAdapter.Builder(getContext())
                .setTimeline(fakeTimeline)
                .build();
        final View view = listAdapter.getView(0, null, null);
        final BaseTweetView tv = (BaseTweetView) view;
        Assert.assertThat(tv.styleResId, is(R.style.tw__TweetLightStyle));
    }

    public void testConstructor_scribesImpression() {
        final TweetUi tweetUi = mock(TestTweetUi.class);
        final ArgumentCaptor<EventNamespace> sdkNamespaceCaptor
                = ArgumentCaptor.forClass(EventNamespace.class);
        final ArgumentCaptor<EventNamespace> tfwNamespaceCaptor
                = ArgumentCaptor.forClass(EventNamespace.class);
        final ArgumentCaptor<List<ScribeItem>> scribeItemsCaptor
                = ArgumentCaptor.forClass(List.class);

        final TimelineDelegate<Tweet> mockTimelineDelegate = mock(TestTimelineDelegate.class);
        new TweetTimelineListAdapter(getContext(), mockTimelineDelegate, ANY_STYLE,
                null, tweetUi);

        verify(tweetUi).scribe(sdkNamespaceCaptor.capture());
        verify(tweetUi).scribe(tfwNamespaceCaptor.capture(), scribeItemsCaptor.capture());

        final EventNamespace sdkNs = sdkNamespaceCaptor.getValue();
        Assert.assertThat(sdkNs.client, is(REQUIRED_SDK_IMPRESSION_CLIENT));
        Assert.assertThat(sdkNs.page, is(REQUIRED_SDK_IMPRESSION_PAGE));
        Assert.assertThat(sdkNs.section, is(TEST_SCRIBE_SECTION));
        Assert.assertThat(sdkNs.component, is(REQUIRED_SDK_IMPRESSION_COMPONENT));
        Assert.assertThat(sdkNs.element, is(REQUIRED_SDK_IMPRESSION_ELEMENT));
        Assert.assertThat(sdkNs.action, is(REQUIRED_IMPRESSION_ACTION));

        final EventNamespace tfwNs = tfwNamespaceCaptor.getValue();
        Assert.assertThat(tfwNs.client, is(REQUIRED_TFW_CLIENT));
        Assert.assertThat(tfwNs.page, is(REQUIRED_TFW_PAGE));
        Assert.assertThat(tfwNs.section, is(REQUIRED_TFW_SECTION));
        Assert.assertThat(tfwNs.component, is(TEST_SCRIBE_SECTION));
        Assert.assertThat(tfwNs.element, is(REQUIRED_TFW_ELEMENT));
        Assert.assertThat(tfwNs.action, is(REQUIRED_IMPRESSION_ACTION));

        final List<ScribeItem> scribeItems = scribeItemsCaptor.getValue();
        Assert.assertThat(scribeItems, notNullValue());
    }

    public void testSetViewStyle() {
        final Timeline<Tweet> fakeTimeline = new FakeTweetTimeline(10);
        listAdapter = new TweetTimelineListAdapter.Builder(getContext())
                .setTimeline(fakeTimeline)
                .setViewStyle(R.style.tw__TweetDarkWithActionsStyle)
                .build();
        final View view = listAdapter.getView(0, null, null);
        final BaseTweetView tv = (BaseTweetView) view;
        Assert.assertThat(tv.styleResId, is(R.style.tw__TweetDarkWithActionsStyle));
    }

    static class FakeTweetTimeline implements Timeline<Tweet> {
        private long numItems;

        /**
         * Constructs a FakeTweetTimeline
         *
         * @param numItems the number of Tweets to return per call to next/previous
         */
        FakeTweetTimeline(long numItems) {
            this.numItems = numItems;
        }

        @Override
        public void next(Long sinceId, Callback<TimelineResult<Tweet>> cb) {
            final List<Tweet> tweets = TestFixtures.getTweetList(numItems);
            final TimelineCursor timelineCursor = new TimelineCursor(tweets);
            final TimelineResult<Tweet> timelineResult
                    = new TimelineResult<>(timelineCursor, tweets);
            cb.success(new Result<>(timelineResult, null));
        }

        @Override
        public void previous(Long maxId, Callback<TimelineResult<Tweet>> cb) {
            final List<Tweet> tweets = TestFixtures.getTweetList(numItems);
            final TimelineCursor timelineCursor = new TimelineCursor(tweets);
            final TimelineResult<Tweet> timelineResult
                    = new TimelineResult<>(timelineCursor, tweets);
            cb.success(new Result<>(timelineResult, null));
        }
    }

    /**
     * Makes class public so it can be mocked on ART runtime.
     *
     * @param <T>
     */
    public class TestTimelineDelegate<T extends Identifiable> extends TimelineDelegate {
        public TestTimelineDelegate(Timeline<T> timeline) {
            super(timeline);
        }
    }
}
