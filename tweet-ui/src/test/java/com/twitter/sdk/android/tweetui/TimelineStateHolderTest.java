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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class TimelineStateHolderTest {
    private static final Long ANY_POSITION = 1234L;
    private static final Long TEST_MIN_POSITION = 1111L;
    private static final Long TEST_MAX_POSITION = 3333L;
    private static final TimelineCursor TEST_TIMELINE_CURSOR = new TimelineCursor(ANY_POSITION,
            ANY_POSITION);

    private TimelineCursor mockTimelineCursor;

    @Before
    public void setUp() throws Exception {

        mockTimelineCursor = mock(TimelineCursor.class);
    }

    @Test
    public void testConstructor() {
        final TimelineStateHolder holder = new TimelineStateHolder();
        Assert.assertThat(holder.nextCursor, nullValue());
        Assert.assertThat(holder.previousCursor, nullValue());
        Assert.assertThat(holder.requestInFlight.get(), is(false));
    }

    @Test
    public void testInternalConstructor() {
        final TimelineStateHolder holder = new TimelineStateHolder(TEST_TIMELINE_CURSOR,
                TEST_TIMELINE_CURSOR);
        Assert.assertThat(holder.previousCursor, is(TEST_TIMELINE_CURSOR));
        Assert.assertThat(holder.nextCursor, is(TEST_TIMELINE_CURSOR));
        Assert.assertThat(holder.requestInFlight.get(), is(false));
    }

    @Test
    public void testResetCursors() {
        final TimelineStateHolder holder = new TimelineStateHolder(TEST_TIMELINE_CURSOR,
                TEST_TIMELINE_CURSOR);
        holder.resetCursors();
        Assert.assertThat(holder.nextCursor, nullValue());
        Assert.assertThat(holder.previousCursor, nullValue());
    }

    @Test
    public void testPositionForNext() {
        final TimelineStateHolder holder = new TimelineStateHolder(new TimelineCursor(ANY_POSITION,
                TEST_MAX_POSITION), mockTimelineCursor);
        Assert.assertThat(holder.positionForNext(), is(TEST_MAX_POSITION));
    }

    @Test
    public void testPositionForNext_nullCursor() {
        final TimelineStateHolder holder = new TimelineStateHolder(null, mockTimelineCursor);
        Assert.assertThat(holder.positionForNext(), nullValue());
    }

    @Test
    public void testSetNextCursor() {
        final TimelineCursor previousCursor = new TimelineCursor(ANY_POSITION, ANY_POSITION);
        final TimelineStateHolder holder = new TimelineStateHolder(
                new TimelineCursor(ANY_POSITION, ANY_POSITION),
                previousCursor);
        holder.setNextCursor(TEST_TIMELINE_CURSOR);
        Assert.assertThat(holder.nextCursor, is(TEST_TIMELINE_CURSOR));
        Assert.assertThat(holder.previousCursor, is(previousCursor));
    }

    // first next load will set both nextCursor and previousCursor
    @Test
    public void testSetNextCursor_firstLoad() {
        final TimelineStateHolder holder = new TimelineStateHolder();
        holder.setNextCursor(TEST_TIMELINE_CURSOR);
        Assert.assertThat(holder.nextCursor, is(TEST_TIMELINE_CURSOR));
        Assert.assertThat(holder.previousCursor, is(TEST_TIMELINE_CURSOR));
    }

    @Test
    public void testPositionForPrevious() {
        final TimelineStateHolder holder = new TimelineStateHolder(mockTimelineCursor,
                new TimelineCursor(TEST_MIN_POSITION, ANY_POSITION));
        Assert.assertThat(holder.positionForPrevious(), is(TEST_MIN_POSITION));
    }

    @Test
    public void testPositionForPrevious_nullCursor() {
        final TimelineStateHolder holder = new TimelineStateHolder(mockTimelineCursor, null);
        Assert.assertThat(holder.positionForPrevious(), nullValue());
    }

    @Test
    public void testSetPreviousCursor() {
        final TimelineCursor nextCursor = new TimelineCursor(ANY_POSITION, ANY_POSITION);
        final TimelineStateHolder holder = new TimelineStateHolder(nextCursor,
                new TimelineCursor(ANY_POSITION, ANY_POSITION));
        holder.setPreviousCursor(TEST_TIMELINE_CURSOR);
        Assert.assertThat(holder.previousCursor, is(TEST_TIMELINE_CURSOR));
        Assert.assertThat(holder.nextCursor, is(nextCursor));
    }

    // first previous load will set both nextCursor and previousCursor
    @Test
    public void testSetPreviousCursor_firstLoad() {
        final TimelineStateHolder holder = new TimelineStateHolder();
        holder.setPreviousCursor(TEST_TIMELINE_CURSOR);
        Assert.assertThat(holder.nextCursor, is(TEST_TIMELINE_CURSOR));
        Assert.assertThat(holder.previousCursor, is(TEST_TIMELINE_CURSOR));
    }

    @Test
    public void testStartTimelineRequest() {
        final TimelineStateHolder holder = new TimelineStateHolder();
        Assert.assertThat(holder.requestInFlight.get(), is(false));
        Assert.assertThat(holder.startTimelineRequest(), is(true));
        Assert.assertThat(holder.requestInFlight.get(), is(true));
        Assert.assertThat(holder.startTimelineRequest(), is(false));
    }

    @Test
    public void testFinishTimelineRequest() {
        final TimelineStateHolder holder = new TimelineStateHolder();
        holder.requestInFlight.set(true);
        holder.finishTimelineRequest();
        Assert.assertThat(holder.requestInFlight.get(), is(false));
    }
}
