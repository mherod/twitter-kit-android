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

import com.twitter.sdk.android.core.internal.scribe.EventNamespace;
import com.twitter.sdk.android.core.internal.scribe.ScribeItem;
import com.twitter.sdk.android.core.internal.scribe.SyndicationClientEvent;
import com.twitter.sdk.android.core.models.MediaEntity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;

public class GalleryScribeClientImplTest {

    static final long TEST_MEDIA_ID = 123456789L;
    static final String TEST_TFW_CLIENT_EVENT_PAGE = "android";
    static final String TEST_TFW_CLIENT_EVENT_SECTION = "gallery";

    static final String TEST_SCRIBE_SHOW_ACTION = "show";
    static final String TEST_SCRIBE_IMPRESSION_ACTION = "impression";
    static final String TEST_SCRIBE_NAVIGATE_ACTION = "navigate";
    static final String TEST_SCRIBE_DISMISS_ACTION = "dismiss";

    static final int TEST_TYPE_CONSUMER_ID = 1;

    private GalleryScribeClient galleryScribeClient;
    @Mock
    private TweetUi tweetUi;
    @Captor
    private ArgumentCaptor<List<ScribeItem>> itemsArgumentCaptor;
    @Captor
    private ArgumentCaptor<EventNamespace> namespaceArgumentCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        galleryScribeClient = new GalleryScribeClientImpl(tweetUi);
    }

    @Test
    public void testShow() {
        galleryScribeClient.show();
        verify(tweetUi).scribe(namespaceArgumentCaptor.capture());

        final EventNamespace ns = namespaceArgumentCaptor.getValue();
        assertBaseNamespace(ns);
        Assert.assertThat(ns.action, is(TEST_SCRIBE_SHOW_ACTION));
    }

    @Test
    public void testImpression() {
        final ScribeItem scribeItem = ScribeItem.Companion.fromMediaEntity(TestFixtures.TEST_TWEET_ID,
                createTestEntity());
        galleryScribeClient.impression(scribeItem);
        verify(tweetUi).scribe(namespaceArgumentCaptor.capture(), itemsArgumentCaptor.capture());

        final EventNamespace ns = namespaceArgumentCaptor.getValue();
        assertBaseNamespace(ns);
        Assert.assertThat(ns.action, is(TEST_SCRIBE_IMPRESSION_ACTION));

        final List<ScribeItem> items = itemsArgumentCaptor.getValue();
        assertItems(items);
    }

    @Test
    public void testNavigate() {
        galleryScribeClient.navigate();
        verify(tweetUi).scribe(namespaceArgumentCaptor.capture());

        final EventNamespace ns = namespaceArgumentCaptor.getValue();
        assertBaseNamespace(ns);
        Assert.assertThat(ns.action, is(TEST_SCRIBE_NAVIGATE_ACTION));
    }

    @Test
    public void testDismiss() {
        galleryScribeClient.dismiss();
        verify(tweetUi).scribe(namespaceArgumentCaptor.capture());

        final EventNamespace ns = namespaceArgumentCaptor.getValue();
        assertBaseNamespace(ns);
        Assert.assertThat(ns.action, is(TEST_SCRIBE_DISMISS_ACTION));
    }


    static void assertItems(List<ScribeItem> items) {
        Assert.assertThat(items, notNullValue());
        Assert.assertThat(items.size(), is(1));
        Assert.assertThat(items.get(0).getId().longValue(), is(TestFixtures.TEST_TWEET_ID));
        Assert.assertThat(items.get(0).getItemType().intValue(), is(ScribeItem.TYPE_TWEET));

        assertMediaDetails(items.get(0).getMediaDetails(), TEST_TYPE_CONSUMER_ID);
    }

    static void assertMediaDetails(ScribeItem.MediaDetails mediaDetails, int type) {
        Assert.assertThat(mediaDetails, notNullValue());
        Assert.assertThat(mediaDetails.getContentId(), is(TestFixtures.TEST_TWEET_ID));
        Assert.assertThat(mediaDetails.getMediaType(), is(type));
        Assert.assertThat(mediaDetails.getPublisherId(), is(TEST_MEDIA_ID));
    }


    static void assertBaseNamespace(EventNamespace ns) {
        Assert.assertThat(ns.client, is(SyndicationClientEvent.CLIENT_NAME));
        Assert.assertThat(ns.page, is(TEST_TFW_CLIENT_EVENT_PAGE));
        Assert.assertThat(ns.section, is(TEST_TFW_CLIENT_EVENT_SECTION));
        Assert.assertThat(ns.element, nullValue());
        Assert.assertThat(ns.component, nullValue());
    }

    private MediaEntity createTestEntity() {
        return new MediaEntity(null, null, null, 0, 0, TEST_MEDIA_ID, null, null, null, null, 0,
                null, "photo", null, "");
    }
}
