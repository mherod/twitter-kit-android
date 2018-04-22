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

import android.database.DataSetObservable;
import android.database.DataSetObserver;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;

import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
public class TimelineDelegateTest {
    private static final TestItem TEST_ITEM_1 = new TestItem(1111L);
    private static final TestItem TEST_ITEM_2 = new TestItem(2222L);
    private static final TestItem TEST_ITEM_3 = new TestItem(3333L);
    private static final TestItem TEST_ITEM_4 = new TestItem(4444L);
    private static final int TOTAL_ITEMS = 4;
    private static final int NUM_ITEMS = 100;
    private static final int ZERO_ITEMS = 0;
    private static final Long ANY_POSITION = 1234L;
    private static final Long TEST_MIN_POSITION = 3333L;
    private static final Long TEST_MAX_POSITION = 4444L;
    private static final TimelineCursor TEST_TIMELINE_CURSOR = new TimelineCursor(TEST_MIN_POSITION,
            TEST_MAX_POSITION);
    private static final String REQUIRED_MAX_CAPACITY_ERROR = "Max capacity reached";
    private static final String REQUIRED_REQUEST_IN_FLIGHT_ERROR = "Request already in flight";
    private static final TwitterException TEST_TWITTER_EXCEPTION
            = new TwitterException("Some exception");

    private TimelineDelegate<TestItem> delegate;
    private Timeline<TestItem> mockTimeline;
    private DataSetObservable mockObservable;
    private List<TestItem> testItems = new ArrayList<>();
    // test items for testing prepending and appending to another list
    private List<TestItem> testExtraItems = new ArrayList<>();
    private static Result<TimelineResult<TestItem>> testResult;

    @Before
    public void setUp() throws Exception {
        //noinspection unchecked,unchecked
        mockTimeline = mock(Timeline.class);
        mockObservable = mock(DataSetObservable.class);
        // lists of items ordered from larger id to smaller
        testItems.add(TEST_ITEM_2);
        testItems.add(TEST_ITEM_1);
        // extra result items ordered from larger id to smaller
        testExtraItems.add(TEST_ITEM_4);
        testExtraItems.add(TEST_ITEM_3);
        testResult = new Result<>(new TimelineResult<>(TEST_TIMELINE_CURSOR, testItems), null);
    }

    @Test
    public void testConstructor() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, testItems);
        Assert.assertThat(delegate.timeline, is(mockTimeline));
        Assert.assertThat(delegate.listAdapterObservable, is(mockObservable));
        Assert.assertThat(delegate.itemList, is(testItems));
        Assert.assertThat(delegate.timelineStateHolder, notNullValue());
        // initial positions must be null
        Assert.assertThat(delegate.timelineStateHolder.positionForNext(), nullValue());
        Assert.assertThat(delegate.timelineStateHolder.positionForPrevious(), nullValue());
    }

    @Test
    public void testConstructor_defaults() {
        delegate = new TimelineDelegate<>(mockTimeline);
        Assert.assertThat(delegate.timeline, is(mockTimeline));
        Assert.assertThat(delegate.listAdapterObservable, notNullValue());
        Assert.assertThat(delegate.itemList, notNullValue());
        Assert.assertThat(delegate.timelineStateHolder, notNullValue());
        // initial positions must be null
        Assert.assertThat(delegate.timelineStateHolder.positionForNext(), nullValue());
        Assert.assertThat(delegate.timelineStateHolder.positionForPrevious(), nullValue());
    }

    @Test
    public void testConstructor_nullTimeline() {
        try {
            delegate = new TimelineDelegate<>(null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            Assert.assertThat(e.getMessage(), is("Timeline must not be null"));
        }
    }

    @Test
    public void testGetCount() {
        delegate = new TimelineDelegate<>(mockTimeline);
        Assert.assertThat(delegate.getCount(), is(0));
        delegate = new TimelineDelegate<>(mockTimeline, null, testItems);
        Assert.assertThat(delegate.getCount(), is(testItems.size()));
    }

    @Test
    public void testGetItem() {
        delegate = new TimelineDelegate<>(mockTimeline, null, testItems);
        Assert.assertThat(delegate.getItem(0), is(TEST_ITEM_2));
        Assert.assertThat(delegate.getItem(1), is(TEST_ITEM_1));
    }

    @Test
    public void testGetLastItem_loadsPrevious() {
        final Timeline<TestItem> fakeTimeline = new FakeItemTimeline(NUM_ITEMS, ANY_POSITION,
                ANY_POSITION);
        delegate = new TimelineDelegate<>(fakeTimeline, mockObservable, null);
        delegate.refresh(null);
        // refresh loads latest items (notifyChange)
        verify(mockObservable).notifyChanged();
        delegate.getItem(NUM_ITEMS - 1);
        // assert items are added and notifyChanged is called again
        Assert.assertThat(delegate.getCount(), is(2 * NUM_ITEMS));
        verify(mockObservable, times(2)).notifyChanged();
    }

    @Test
    public void testGetNonLastItem_doesNotLoadPrevious() {
        final Timeline<TestItem> fakeTimeline = new FakeItemTimeline(NUM_ITEMS, ANY_POSITION,
                ANY_POSITION);
        delegate = new TimelineDelegate<>(fakeTimeline, mockObservable, null);
        delegate.refresh(null);
        // refresh loads latest items (notifyChange)
        verify(mockObservable).notifyChanged();
        Assert.assertThat(delegate.getCount(), is(NUM_ITEMS));
        delegate.getItem(1);
        // assert no items added and notifyChanged is not called again
        Assert.assertThat(delegate.getCount(), is(NUM_ITEMS));
        verify(mockObservable, times(1)).notifyChanged();
    }

    @Test
    public void testGetItemId() {
        delegate = new TimelineDelegate<>(mockTimeline, null, testItems);
        Assert.assertThat(delegate.getItemId(0), is(TEST_ITEM_2.getId()));
        Assert.assertThat(delegate.getItemId(1), is(TEST_ITEM_1.getId()));
    }

    @Test
    public void testSetItemById() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, testItems);
        Assert.assertThat(delegate.getItem(0), is(TEST_ITEM_2));
        Assert.assertThat(delegate.getItem(1), is(TEST_ITEM_1));
        final TestItem differentItemSameId = new TestItem(TEST_ITEM_2.getId());
        delegate.setItemById(differentItemSameId);
        MatcherAssert.assertThat(TEST_ITEM_2, not(delegate.getItem(0)));
        Assert.assertThat(delegate.getItem(0), is(differentItemSameId));
        Assert.assertThat(delegate.getItem(1), is(TEST_ITEM_1));
        verify(mockObservable).notifyChanged();
    }

    @Test
    public void testWithinMaxCapacity() {
        delegate = new TimelineDelegate<>(mockTimeline);
        Assert.assertThat(delegate.withinMaxCapacity(), is(true));
        TestItem.populateList(testItems, TimelineDelegate.CAPACITY);
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, testItems);
        Assert.assertThat(delegate.withinMaxCapacity(), is(false));
    }

    @Test
    public void testIsLastPosition() {
        testItems = new ArrayList<>();
        TestItem.populateList(testItems, NUM_ITEMS);
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, testItems);
        Assert.assertThat(delegate.isLastPosition(0), is(false));
        Assert.assertThat(delegate.isLastPosition(NUM_ITEMS - 2), is(false));
        Assert.assertThat(delegate.isLastPosition(NUM_ITEMS - 1), is(true));
    }

    // reset, next, previous

    @Test
    public void testRefresh_resetsTimelineCursors() {
        delegate = new TimelineDelegate<>(mockTimeline);
        delegate.timelineStateHolder.setNextCursor(new TimelineCursor(ANY_POSITION, ANY_POSITION));
        delegate.timelineStateHolder.setPreviousCursor(new TimelineCursor(ANY_POSITION,
                ANY_POSITION));
        delegate.refresh(null);
        Assert.assertThat(delegate.timelineStateHolder.positionForNext(), nullValue());
        Assert.assertThat(delegate.timelineStateHolder.positionForPrevious(), nullValue());
    }

    @Test
    public void testRefresh_callsNextForLatest() {
        delegate = new TimelineDelegate<>(mockTimeline);
        delegate.refresh(null);
        verify(mockTimeline).next(isNull(Long.class), any(TimelineDelegate.NextCallback.class));
    }

    @Test
    public void testRefresh_replacesItems() {
        // refresh replaces initial items
        final Timeline<TestItem> fakeTimeline = new FakeItemTimeline(NUM_ITEMS, ANY_POSITION,
                ANY_POSITION);
        delegate = new TimelineDelegate<>(fakeTimeline, mockObservable, testItems);
        Assert.assertThat(delegate.itemList, is(testItems));
        delegate.refresh(null);
        // assert that items were replaced and notifyChanged called
        Assert.assertThat(delegate.itemList.size(), is(NUM_ITEMS));
        verify(mockObservable).notifyChanged();
    }

    @Test
    public void testNext_addsItems() {
        final Timeline<TestItem> fakeTimeline = new FakeItemTimeline(NUM_ITEMS, ANY_POSITION,
                ANY_POSITION);
        delegate = new TimelineDelegate<>(fakeTimeline, mockObservable, null);
        delegate.refresh(null);
        // refresh loads latest items (notifyChange)
        delegate.next(null);
        // assert items are added and notifyChanges is called again
        Assert.assertThat(delegate.getCount(), is(2 * NUM_ITEMS));
        verify(mockObservable, times(2)).notifyChanged();
    }

    @Test
    public void testNext_doesNotAddItemsAtBeginningOfTimeline() {
        // when a Timeline successfully returns an empty set of items, there are no next items (yet)
        List<TestItem> initialItems = new ArrayList<>();
        final int INITIAL_COUNT = 5;
        initialItems = TestItem.populateList(initialItems, INITIAL_COUNT);
        final Timeline<TestItem> fakeEndTimeline = new FakeItemTimeline(ZERO_ITEMS, ANY_POSITION,
                ANY_POSITION);
        delegate = new TimelineDelegate<>(fakeEndTimeline, mockObservable, initialItems);
        delegate.next(null);
        // assert no items are added and notifyChanged is not called
        Assert.assertThat(delegate.getCount(), is(INITIAL_COUNT));
        verifyZeroInteractions(mockObservable);
    }

    @Test
    public void testNext_updatesPositionForNext() {
        final Timeline<TestItem> fakeTimeline = new FakeItemTimeline(NUM_ITEMS, ANY_POSITION,
                TEST_MAX_POSITION);
        delegate = new TimelineDelegate<>(fakeTimeline);
        Assert.assertThat(delegate.timelineStateHolder.positionForPrevious(), nullValue());
        delegate.next(null);
        Assert.assertThat(delegate.timelineStateHolder.positionForNext(), is(TEST_MAX_POSITION));
    }

    @Test
    public void testNext_doesNotUpdatePositionAtBeginningOfTimeline() {
        final Timeline<TestItem> fakeEndTimeline = new FakeItemTimeline(ZERO_ITEMS, ANY_POSITION,
                ANY_POSITION);
        delegate = new TimelineDelegate<>(fakeEndTimeline);
        delegate.timelineStateHolder.setPreviousCursor(new TimelineCursor(null, null));
        delegate.next(null);
        Assert.assertThat(delegate.timelineStateHolder.positionForNext(), nullValue());
    }

    @Test
    public void testPrevious_addsItems() {
        final Timeline<TestItem> fakeTimeline = new FakeItemTimeline(NUM_ITEMS, ANY_POSITION,
                ANY_POSITION);
        delegate = new TimelineDelegate<>(fakeTimeline, mockObservable, null);
        delegate.refresh(null);
        // refresh loads latest items (notifyChange)
        delegate.previous();
        // assert items are added and notifyChanges is called again
        Assert.assertThat(delegate.getCount(), is(2 * NUM_ITEMS));
        verify(mockObservable, times(2)).notifyChanged();
    }

    @Test
    public void testPrevious_doesNotAddItemsAtEndOfTimeline() {
        // when a Timeline successfully returns an empty set of items, its end has been reached
        List<TestItem> initialItems = new ArrayList<>();
        final int INITIAL_COUNT = 5;
        initialItems = TestItem.populateList(initialItems, INITIAL_COUNT);
        final Timeline<TestItem> fakeEndTimeline = new FakeItemTimeline(ZERO_ITEMS, ANY_POSITION,
                ANY_POSITION);
        delegate = new TimelineDelegate<>(fakeEndTimeline, mockObservable, initialItems);
        delegate.previous();
        // assert no items are added and notifyChanged is not called
        Assert.assertThat(delegate.getCount(), is(INITIAL_COUNT));
        verifyZeroInteractions(mockObservable);
    }

    @Test
    public void testPrevious_updatesPositionForPrevious() {
        final Timeline<TestItem> fakeTimeline = new FakeItemTimeline(NUM_ITEMS, TEST_MIN_POSITION,
                ANY_POSITION);
        delegate = new TimelineDelegate<>(fakeTimeline);
        Assert.assertThat(delegate.timelineStateHolder.positionForPrevious(), nullValue());
        delegate.previous();
        Assert.assertThat(delegate.timelineStateHolder.positionForPrevious(), is(TEST_MIN_POSITION));
    }

    @Test
    public void testPrevious_doesNotUpdatePositionAtEndOfTimeline() {
        final Timeline<TestItem> fakeEndTimeline = new FakeItemTimeline(ZERO_ITEMS, ANY_POSITION,
                ANY_POSITION);
        delegate = new TimelineDelegate<>(fakeEndTimeline);
        delegate.timelineStateHolder.setPreviousCursor(new TimelineCursor(null, null));
        delegate.previous();
        Assert.assertThat(delegate.timelineStateHolder.positionForPrevious(), nullValue());
    }

    // loadNext, loadPrevious

    @Test
    public void testLoadNext() {
        delegate = new TimelineDelegate<>(mockTimeline);
        final Callback<TimelineResult<TestItem>> testCb = delegate.new NextCallback(null,
                delegate.timelineStateHolder);
        delegate.loadNext(TEST_MIN_POSITION, testCb);
        verify(mockTimeline).next(TEST_MIN_POSITION, testCb);
    }

    @Test
    public void testLoadNext_respectsMaxCapacity() {
        delegate = new TimelineDelegate<>(mockTimeline);
        TestItem.populateList(delegate.itemList, TimelineDelegate.CAPACITY);
        final Callback<TimelineResult<TestItem>> mockCallback = mock(Callback.class);
        delegate.loadNext(ANY_POSITION, mockCallback);
        final ArgumentCaptor<TwitterException> exceptionCaptor
                = ArgumentCaptor.forClass(TwitterException.class);
        verifyZeroInteractions(mockTimeline);
        verify(mockCallback).failure(exceptionCaptor.capture());
        Assert.assertThat(exceptionCaptor.getValue().getMessage(), is(REQUIRED_MAX_CAPACITY_ERROR));
    }

    @Test
    public void testLoadNext_respectsRequestInFlight() {
        delegate = new TimelineDelegate<>(mockTimeline);
        delegate.timelineStateHolder.startTimelineRequest();
        final Callback<TimelineResult<TestItem>> mockCallback = mock(Callback.class);
        delegate.loadNext(ANY_POSITION, mockCallback);
        final ArgumentCaptor<TwitterException> exceptionCaptor
                = ArgumentCaptor.forClass(TwitterException.class);
        verifyZeroInteractions(mockTimeline);
        verify(mockCallback).failure(exceptionCaptor.capture());
        Assert.assertThat(exceptionCaptor.getValue().getMessage(), is(REQUIRED_REQUEST_IN_FLIGHT_ERROR));
    }

    @Test
    public void testLoadPrevious() {
        delegate = new TimelineDelegate<>(mockTimeline);
        final Callback<TimelineResult<TestItem>> testCb = delegate.new PreviousCallback(
                delegate.timelineStateHolder);
        delegate.loadPrevious(TEST_MAX_POSITION, testCb);
        verify(mockTimeline).previous(TEST_MAX_POSITION, testCb);
    }

    @Test
    public void testLoadPrevious_respectsMaxCapacity() {
        delegate = new TimelineDelegate<>(mockTimeline);
        TestItem.populateList(delegate.itemList, TimelineDelegate.CAPACITY);
        final Callback<TimelineResult<TestItem>> mockCallback = mock(Callback.class);
        delegate.loadPrevious(ANY_POSITION, mockCallback);
        final ArgumentCaptor<TwitterException> exceptionCaptor
                = ArgumentCaptor.forClass(TwitterException.class);
        verifyZeroInteractions(mockTimeline);
        verify(mockCallback).failure(exceptionCaptor.capture());
        Assert.assertThat(exceptionCaptor.getValue().getMessage(), is(REQUIRED_MAX_CAPACITY_ERROR));
    }

    @Test
    public void testLoadPrevious_respectsRequestInFlight() {
        delegate = new TimelineDelegate<>(mockTimeline);
        delegate.timelineStateHolder.startTimelineRequest();
        final Callback<TimelineResult<TestItem>> mockCallback = mock(Callback.class);
        delegate.loadPrevious(ANY_POSITION, mockCallback);
        final ArgumentCaptor<TwitterException> exceptionCaptor
                = ArgumentCaptor.forClass(TwitterException.class);
        verifyZeroInteractions(mockTimeline);
        verify(mockCallback).failure(exceptionCaptor.capture());
        Assert.assertThat(exceptionCaptor.getValue().getMessage(), is(REQUIRED_REQUEST_IN_FLIGHT_ERROR));
    }

    /* nested Callbacks */

    // should unconditionally set requestInFlight to false
    @Test
    public void testDefaultCallback_successCallsFinishTimelineRequest() {
        delegate = new TimelineDelegate<>(mockTimeline);
        final TimelineStateHolder mockHolder = mock(TimelineStateHolder.class);
        final TimelineDelegate.DefaultCallback cb = delegate.new DefaultCallback(null, mockHolder);
        cb.success(null);
        verify(mockHolder).finishTimelineRequest();
    }

    @Test
    public void testDefaultCallback_successCallsDeveloperCallback() {
        final Callback<TimelineResult<TestItem>> developerCb = mock(Callback.class);
        delegate = new TimelineDelegate<>(mockTimeline);
        final TimelineDelegate.DefaultCallback cb = delegate.new DefaultCallback(developerCb,
                delegate.timelineStateHolder);
        cb.success(testResult);
        verify(developerCb).success(testResult);
    }

    @Test
    public void testDefaultCallback_successHandlesNullDeveloperCallback() {
        delegate = new TimelineDelegate<>(mockTimeline);
        final TimelineDelegate.DefaultCallback cb = delegate.new DefaultCallback(null,
                delegate.timelineStateHolder);
        try {
            cb.success(testResult);
        } catch (NullPointerException e) {
            fail("Should have handled null callback");
        }
    }

    // should unconditionally set requestInFlight to false
    @Test
    public void testDefaultCallback_failureCallsFinishTimelineRequest() {
        delegate = new TimelineDelegate<>(mockTimeline);
        final TimelineStateHolder mockHolder = mock(TimelineStateHolder.class);
        final TimelineDelegate.DefaultCallback cb = delegate.new DefaultCallback(null, mockHolder);
        cb.failure(null);
        verify(mockHolder).finishTimelineRequest();
    }

    @Test
    public void testDefaultCallback_failureCallsDeveloperCallback() {
        final Callback<TimelineResult<TestItem>> developerCb = mock(Callback.class);
        delegate = new TimelineDelegate<>(mockTimeline);
        final TimelineDelegate.DefaultCallback cb = delegate.new DefaultCallback(developerCb,
                delegate.timelineStateHolder);
        cb.failure(TEST_TWITTER_EXCEPTION);
        verify(developerCb).failure(TEST_TWITTER_EXCEPTION);
    }

    @Test
    public void testDefaultCallback_failureHandlesNullDeveloperCallback() {
        delegate = new TimelineDelegate<>(mockTimeline);
        final TimelineDelegate.DefaultCallback cb = delegate.new DefaultCallback(null,
                delegate.timelineStateHolder);
        try {
            cb.failure(TEST_TWITTER_EXCEPTION);
        } catch (NullPointerException e) {
            fail("Should have handled null callback");
        }
    }

    // should prepend result items, set next cursor, and call notifyChanged
    @Test
    public void testNextCallback_successReceivedItems() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, testItems);
        final TimelineStateHolder timelineStateHolder = new TimelineStateHolder(
                new TimelineCursor(ANY_POSITION, ANY_POSITION),
                new TimelineCursor(ANY_POSITION, ANY_POSITION));
        final TimelineDelegate.NextCallback cb = delegate.new NextCallback(null,
                timelineStateHolder);
        cb.success(new Result<>(new TimelineResult<>(TEST_TIMELINE_CURSOR, testExtraItems), null));
        // assert the next TimelineCursor is set on the ScrollStateHolder, previous unchanged
        Assert.assertThat(timelineStateHolder.positionForNext(), is(TEST_MAX_POSITION));
        Assert.assertThat(timelineStateHolder.positionForPrevious(), is(ANY_POSITION));
        // assert that extra items were prepended in reverse order
        Assert.assertThat(delegate.itemList.size(), is(TOTAL_ITEMS));
        Assert.assertThat(delegate.getItem(0), is(TEST_ITEM_4));
        Assert.assertThat(delegate.getItem(1), is(TEST_ITEM_3));
        Assert.assertThat(delegate.getItem(2), is(TEST_ITEM_2));
        Assert.assertThat(delegate.getItem(3), is(TEST_ITEM_1));
        // assert observer's notifyChanged is called
        verify(mockObservable).notifyChanged();
    }

    // should set both nextCursor and previousCursor to be non-null
    @Test
    public void testNextCallback_successFirstReceivedItems() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, testItems);
        final TimelineStateHolder timelineStateHolder = new TimelineStateHolder();
        final TimelineDelegate.NextCallback cb = delegate.new NextCallback(null,
                timelineStateHolder);
        cb.success(new Result<>(new TimelineResult<>(TEST_TIMELINE_CURSOR, testExtraItems), null));
        // assert the next TimelineCursor is set on the ScrollStateHolder, previous unchanged
        Assert.assertThat(timelineStateHolder.positionForNext(), is(TEST_MAX_POSITION));
        Assert.assertThat(timelineStateHolder.positionForPrevious(), is(TEST_MIN_POSITION));
    }

    // should do nothing
    @Test
    public void testNextCallback_successReceivedZeroItems() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, testItems);
        final TimelineStateHolder timelineStateHolder = new TimelineStateHolder();
        final TimelineDelegate.NextCallback cb = delegate.new NextCallback(null,
                timelineStateHolder);
        cb.success(new Result<>(new TimelineResult<>(TEST_TIMELINE_CURSOR, Collections.emptyList()),
                null));
        // assert that the cursors and itemList are left unmodified
        Assert.assertThat(timelineStateHolder.positionForNext(), nullValue());
        Assert.assertThat(timelineStateHolder.positionForPrevious(), nullValue());
        Assert.assertThat(delegate.itemList.size(), is(testItems.size()));
        verifyZeroInteractions(mockObservable);
    }

    // should clear items with result items, set next cursor, and call notifyChanged
    @Test
    public void testRefreshCallback_successReceivedItems() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, testItems);
        final TimelineStateHolder timelineStateHolder = new TimelineStateHolder(
                new TimelineCursor(ANY_POSITION, ANY_POSITION),
                new TimelineCursor(ANY_POSITION, ANY_POSITION));
        final TimelineDelegate.RefreshCallback cb = delegate.new RefreshCallback(null,
                timelineStateHolder);
        cb.success(new Result<>(new TimelineResult<>(TEST_TIMELINE_CURSOR, testExtraItems), null));
        // assert the next TimelineCursor is set on the ScrollStateHolder, previous unchanged
        Assert.assertThat(timelineStateHolder.positionForNext(), is(TEST_MAX_POSITION));
        Assert.assertThat(timelineStateHolder.positionForPrevious(), is(ANY_POSITION));
        // assert that extra items replaced the old items
        Assert.assertThat(delegate.itemList.size(), is(testExtraItems.size()));
        Assert.assertThat(delegate.getItem(0), is(TEST_ITEM_4));
        Assert.assertThat(delegate.getItem(1), is(TEST_ITEM_3));
        // assert observer's notifyChanged is called
        verify(mockObservable).notifyChanged();
    }

    // should do nothing
    @Test
    public void testRefreshCallback_successReceivedZeroItems() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, testItems);
        final TimelineStateHolder timelineStateHolder = new TimelineStateHolder();
        final TimelineDelegate.RefreshCallback cb = delegate.new RefreshCallback(null,
                timelineStateHolder);
        cb.success(new Result<>(new TimelineResult<>(TEST_TIMELINE_CURSOR, Collections.emptyList()),
                null));
        // assert that the cursors and itemList are left unmodified
        Assert.assertThat(timelineStateHolder.positionForNext(), nullValue());
        Assert.assertThat(timelineStateHolder.positionForPrevious(), nullValue());
        Assert.assertThat(delegate.itemList.size(), is(testItems.size()));
        verifyZeroInteractions(mockObservable);
    }

    // should append result items, set previous cursor, and call notifyChanged
    @Test
    public void testPreviousCallback_successReceivedItems() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, testItems);
        final TimelineStateHolder timelineStateHolder = new TimelineStateHolder(
                new TimelineCursor(ANY_POSITION, ANY_POSITION),
                new TimelineCursor(ANY_POSITION, ANY_POSITION));
        final TimelineDelegate.PreviousCallback cb
                = delegate.new PreviousCallback(timelineStateHolder);
        cb.success(new Result<>(new TimelineResult<>(TEST_TIMELINE_CURSOR, testExtraItems), null));
        // assert the previous TimelineCursor is set on the ScrollStateHolder
        Assert.assertThat(timelineStateHolder.positionForPrevious(), is(TEST_MIN_POSITION));
        Assert.assertThat(timelineStateHolder.positionForNext(), is(ANY_POSITION));
        // assert that extra items were appended in order received
        Assert.assertThat(delegate.itemList.size(), is(TOTAL_ITEMS));
        Assert.assertThat(delegate.getItem(0), is(TEST_ITEM_2));
        Assert.assertThat(delegate.getItem(1), is(TEST_ITEM_1));
        Assert.assertThat(delegate.getItem(2), is(TEST_ITEM_4));
        Assert.assertThat(delegate.getItem(3), is(TEST_ITEM_3));
        // assert observer's notifyChanged is called
        verify(mockObservable).notifyChanged();
    }

    // should set both nextCursor and previousCursor to be non-null
    @Test
    public void testPreviousCallback_successFirstReceivedItems() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, testItems);
        final TimelineStateHolder timelineStateHolder = new TimelineStateHolder();
        final TimelineDelegate.PreviousCallback cb
                = delegate.new PreviousCallback(timelineStateHolder);
        cb.success(new Result<>(new TimelineResult<>(TEST_TIMELINE_CURSOR, testExtraItems), null));
        // assert the previous TimelineCursor is set on the ScrollStateHolder
        Assert.assertThat(timelineStateHolder.positionForNext(), is(TEST_MAX_POSITION));
        Assert.assertThat(timelineStateHolder.positionForPrevious(), is(TEST_MIN_POSITION));
    }


    // should do nothing
    @Test
    public void testPreviousCallback_successReceivedZeroItems() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, testItems);
        final TimelineStateHolder timelineStateHolder = new TimelineStateHolder();
        final TimelineDelegate.PreviousCallback cb
                = delegate.new PreviousCallback(timelineStateHolder);
        cb.success(new Result<>(new TimelineResult<>(TEST_TIMELINE_CURSOR, Collections.emptyList()),
                null));
        // assert that the cursors and itemList are left unmodified
        Assert.assertThat(timelineStateHolder.positionForNext(), nullValue());
        Assert.assertThat(timelineStateHolder.positionForPrevious(), nullValue());
        Assert.assertThat(delegate.itemList.size(), is(testItems.size()));
        verifyZeroInteractions(mockObservable);
    }

    /* test DataSetObservable */

    @Test
    public void testRegisterDataSetObserver() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, null);
        delegate.registerDataSetObserver(mock(DataSetObserver.class));
        verify(mockObservable, times(1)).registerObserver(any(DataSetObserver.class));
    }

    @Test
    public void testUnregisterDataSetObserver() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, null);
        delegate.unregisterDataSetObserver(mock(DataSetObserver.class));
        verify(mockObservable, times(1)).unregisterObserver(any(DataSetObserver.class));
    }

    @Test
    public void testNotifyDataSetChanged() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, null);
        delegate.notifyDataSetChanged();
        verify(mockObservable, times(1)).notifyChanged();
    }

    @Test
    public void testNotifyDataSetInvalidated() {
        delegate = new TimelineDelegate<>(mockTimeline, mockObservable, null);
        delegate.notifyDataSetInvalidated();
        verify(mockObservable, times(1)).notifyInvalidated();
    }

    /**
     * Timeline which loads numItems TestItems on each next/previous call. Use zero for numItems
     * to simulate reaching the end of a finite timeline.
     */
    public static class FakeItemTimeline implements Timeline<TestItem> {
        private long numItems;
        private Long minPosition;
        private Long maxPosition;

        /**
         * Constructs a FakeItemTimeline
         *
         * @param numItems    the number of TestItems to return per call to next/previous
         * @param minPosition the TimelineCursor minPosition returned by calls to next/previous
         * @param maxPosition the TimelineCursor maxPosition returned by calls to next/previous
         */
        public FakeItemTimeline(long numItems, Long minPosition, Long maxPosition) {
            this.numItems = numItems;
            this.minPosition = minPosition;
            this.maxPosition = maxPosition;
        }

        @Override
        public void next(Long sinceId, Callback<TimelineResult<TestItem>> cb) {
            final List<TestItem> testItems = new ArrayList<>();
            TestItem.populateList(testItems, numItems);
            final TimelineResult<TestItem> timelineResult
                    = new TimelineResult<>(new TimelineCursor(minPosition, maxPosition), testItems);
            cb.success(new Result<>(timelineResult, null));
        }

        @Override
        public void previous(Long maxId, Callback<TimelineResult<TestItem>> cb) {
            final List<TestItem> testItems = new ArrayList<>();
            TestItem.populateList(testItems, numItems);
            final TimelineResult<TestItem> timelineResult
                    = new TimelineResult<>(new TimelineCursor(minPosition, maxPosition), testItems);
            cb.success(new Result<>(timelineResult, null));
        }
    }
}
