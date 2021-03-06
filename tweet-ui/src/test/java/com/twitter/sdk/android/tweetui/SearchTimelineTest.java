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
import com.twitter.sdk.android.core.services.SearchService;
import com.twitter.sdk.android.core.services.params.Geocode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SearchTimelineTest {
    private static final String TEST_QUERY = "twitterflock";
    private static final Geocode TEST_GEOCODE =
            new Geocode(37.7767902, -122.4164055, 1, Geocode.Distance.MILES);
    private static final String TEST_FILTER_QUERY = "from:twitter";
    private static final String TEST_RESULT_TYPE = "popular";
    private static final String TEST_LANG = "en";
    private static final String TEST_UNTIL_DATE = "2012-08-20";
    private static final Date TEST_UNTIL =
            new GregorianCalendar(2012, Calendar.AUGUST, 20).getTime();
    private static final Integer REQUIRED_DEFAULT_ITEMS_PER_REQUEST = 30;
    private static final Integer TEST_ITEMS_PER_REQUEST = 100;
    private static final Long TEST_SINCE_ID = 1000L;
    private static final Long TEST_MAX_ID = 1111L;
    private static final String REQUIRED_IMPRESSION_SECTION = "search";

    TwitterCore twitterCore;
    TwitterApiClient apiClient;
    SearchService searchService;

    @Before
    public void setUp() {
        twitterCore = mock(TwitterCore.class);
        apiClient = mock(TwitterApiClient.class);
        searchService = mock(SearchService.class, new MockCallAnswer());

        when(apiClient.getSearchService()).thenReturn(searchService);
        when(twitterCore.getApiClient()).thenReturn(apiClient);
    }

    @Test
    public void testConstructor() {
        final SearchTimeline timeline = new SearchTimeline(twitterCore, TEST_QUERY, TEST_GEOCODE,
                TEST_RESULT_TYPE, TEST_LANG, TEST_ITEMS_PER_REQUEST, TEST_UNTIL_DATE);
        Assert.assertThat(timeline.query, is(TEST_QUERY + SearchTimeline.FILTER_RETWEETS));
        Assert.assertThat(timeline.languageCode, is(TEST_LANG));
        Assert.assertThat(timeline.maxItemsPerRequest, is(TEST_ITEMS_PER_REQUEST));
        Assert.assertThat(timeline.untilDate, is(TEST_UNTIL_DATE));
    }

    @Test
    // most api arguments should default to Null to allow the backend to determine default behavior
    public void testConstructor_defaults() {
        final SearchTimeline timeline = new SearchTimeline(twitterCore, null, null, null, null,
                null, null);
        Assert.assertThat(timeline.query, nullValue());
        Assert.assertThat(timeline.languageCode, nullValue());
        Assert.assertThat(timeline.maxItemsPerRequest, nullValue());
        Assert.assertThat(timeline.untilDate, nullValue());
    }

    @Test
    // FILTER_RETWEETS modifier should be added to the end of the non-null search queries
    public void testFilterRetweets() {
        final SearchTimeline timeline = new SearchTimeline(twitterCore, TEST_QUERY, null, null,
                null, null, null);
        Assert.assertThat(timeline.query.endsWith(SearchTimeline.FILTER_RETWEETS), is(true));
    }

    @Test
    public void testAddFilterRetweets() {
        final SearchTimeline timeline = new SearchTimeline(twitterCore, TEST_FILTER_QUERY, null,
                null, null, null, null);
        Assert.assertThat(timeline.query, is("from:twitter -filter:retweets"));
    }

    @Test
    public void testFilterRetweets_nullQuery() {
        // handle null queries, do not append FILTER_RETWEETS
        final SearchTimeline timeline = new SearchTimeline(twitterCore, null, null, null, null,
                null, null);
        Assert.assertThat(timeline.query, nullValue());
    }

    @Test
    public void testNext_createsCorrectRequest() {
        final SearchTimeline timeline = spy(new SearchTimeline(twitterCore, TEST_QUERY,
                TEST_GEOCODE, TEST_RESULT_TYPE, TEST_LANG, TEST_ITEMS_PER_REQUEST,
                TEST_UNTIL_DATE));
        timeline.next(TEST_SINCE_ID, mock(Callback.class));
        verify(timeline).createSearchRequest(eq(TEST_SINCE_ID),
                isNull(Long.class));
    }

    @Test
    public void testPrevious_createsCorrectRequest() {
        final SearchTimeline timeline = spy(new SearchTimeline(twitterCore, TEST_QUERY,
                TEST_GEOCODE, TEST_RESULT_TYPE, TEST_LANG, TEST_ITEMS_PER_REQUEST,
                TEST_UNTIL_DATE));
        timeline.previous(TEST_MAX_ID, mock(Callback.class));
        // intentionally decrementing the maxId which is passed through to the request
        verify(timeline).createSearchRequest(isNull(Long.class),
                eq(TEST_MAX_ID - 1));
    }

    @Test
    public void testCreateSearchRequest() {
        // build a timeline with test params
        final SearchTimeline timeline = spy(new SearchTimeline(twitterCore, TEST_QUERY, null,
                TEST_RESULT_TYPE, TEST_LANG, TEST_ITEMS_PER_REQUEST, TEST_UNTIL_DATE));
        // create a request directly
        timeline.createSearchRequest(TEST_SINCE_ID, TEST_MAX_ID);

        // assert searchTimeline call is made with the correct arguments
        verify(twitterCore.getApiClient().getSearchService())
                .tweets(eq(TEST_QUERY + SearchTimeline.FILTER_RETWEETS),
                isNull(Geocode.class), eq(TEST_LANG), isNull(String.class),
                eq(TEST_RESULT_TYPE), eq(TEST_ITEMS_PER_REQUEST), eq(TEST_UNTIL_DATE),
                eq(TEST_SINCE_ID), eq(TEST_MAX_ID), eq(true));
    }

    @Test
    public void testGetScribeSection() {
        final SearchTimeline timeline = new SearchTimeline.Builder(twitterCore)
                .query(TEST_QUERY)
                .build();
        Assert.assertThat(timeline.getTimelineType(), is(REQUIRED_IMPRESSION_SECTION));
    }

    /* Builder */
    @Test
    public void testBuilder() {
        final SearchTimeline timeline = new SearchTimeline.Builder(twitterCore)
                .query(TEST_QUERY)
                .geocode(TEST_GEOCODE)
                .languageCode(TEST_LANG)
                .maxItemsPerRequest(TEST_ITEMS_PER_REQUEST)
                .resultType(SearchTimeline.ResultType.POPULAR)
                .untilDate(TEST_UNTIL)
                .build();
        Assert.assertThat(timeline.query, is(TEST_QUERY + SearchTimeline.FILTER_RETWEETS));
        Assert.assertThat(timeline.resultType, is(TEST_RESULT_TYPE));
        Assert.assertThat(timeline.languageCode, is(TEST_LANG));
        Assert.assertThat(timeline.maxItemsPerRequest, is(TEST_ITEMS_PER_REQUEST));
        Assert.assertThat(timeline.untilDate, is(TEST_UNTIL_DATE));
        Assert.assertThat(timeline.geocode, sameInstance(TEST_GEOCODE));
    }

    @Test
    // api arguments should default to Null to allow the backend to determine default behavior
    public void testBuilder_defaults() {
        final SearchTimeline timeline = new SearchTimeline.Builder(twitterCore)
                .query(TEST_QUERY)
                .build();
        Assert.assertThat(timeline.languageCode, nullValue());
        Assert.assertThat(timeline.maxItemsPerRequest, is(REQUIRED_DEFAULT_ITEMS_PER_REQUEST));
        Assert.assertThat(timeline.untilDate, nullValue());
    }

    @Test
    public void testBuilder_query() {
        final SearchTimeline timeline = new SearchTimeline.Builder(twitterCore)
                .query(TEST_QUERY)
                .build();
        Assert.assertThat(timeline.query, is(TEST_QUERY + SearchTimeline.FILTER_RETWEETS));
    }

    @Test
    public void testBuilder_nullQuery() {
        try {
            new SearchTimeline.Builder(twitterCore).build();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            Assert.assertThat(e.getMessage(), is("query must not be null"));
        }
    }

    @Test
    public void testBuilder_lang() {
        final SearchTimeline timeline = new SearchTimeline.Builder(twitterCore)
                .query(TEST_QUERY)
                .languageCode(TEST_LANG)
                .build();
        Assert.assertThat(timeline.languageCode, is(TEST_LANG));
    }

    @Test
    public void testBuilder_geocode() {
        final SearchTimeline timeline = new SearchTimeline.Builder(twitterCore)
                .query(TEST_QUERY)
                .geocode(TEST_GEOCODE)
                .build();
        Assert.assertThat(timeline.geocode, sameInstance(TEST_GEOCODE));
    }

    @Test
    public void testBuilder_maxItemsPerRequest() {
        final SearchTimeline timeline = new SearchTimeline.Builder(twitterCore)
                .query(TEST_QUERY)
                .maxItemsPerRequest(TEST_ITEMS_PER_REQUEST)
                .build();
        Assert.assertThat(timeline.maxItemsPerRequest, is(TEST_ITEMS_PER_REQUEST));
    }
}
