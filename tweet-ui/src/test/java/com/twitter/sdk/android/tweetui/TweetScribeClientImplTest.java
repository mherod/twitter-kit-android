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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TweetScribeClientImplTest {
    static final String REQUIRED_TFW_SCRIBE_CLIENT = "tfw";
    static final String REQUIRED_TFW_SCRIBE_PAGE = "android";
    static final String REQUIRED_TFW_SCRIBE_SECTION = "tweet";
    static final String REQUIRED_TFW_SCRIBE_ELEMENT = "";

    static final String REQUIRED_SDK_SCRIBE_CLIENT = "android";
    static final String REQUIRED_SDK_SCRIBE_PAGE = "tweet";
    static final String REQUIRED_SDK_SCRIBE_COMPONENT = "";
    static final String REQUIRED_SDK_SCRIBE_ELEMENT = "";
    static final String REQUIRED_SCRIBE_CLICK_ACTION = "click";
    static final String REQUIRED_SCRIBE_IMPRESSION_ACTION = "impression";
    static final String REQUIRED_SCRIBE_FAVORITE_ACTION = "favorite";
    static final String REQUIRED_SCRIBE_UNFAVORITE_ACTION = "unfavorite";
    static final String REQUIRED_SCRIBE_SHARE_ACTION = "share";
    static final String REQUIRED_SCRIBE_ACTIONS_ELEMENT = "actions";

    static final String TEST_VIEW_NAME = "compact";

    private TweetScribeClientImpl scribeClient;
    @Mock
    private TweetUi tweetUi;
    @Captor
    private ArgumentCaptor<List<ScribeItem>> itemsArgumentCaptor;
    @Captor
    private ArgumentCaptor<EventNamespace> namespaceArgumentCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        scribeClient = new TweetScribeClientImpl(tweetUi);
    }

    @Test
    public void testImpression() {
        scribeClient.impression(TestFixtures.TEST_TWEET, TEST_VIEW_NAME, false);

        verify(tweetUi, times(2))
                .scribe(namespaceArgumentCaptor.capture(), itemsArgumentCaptor.capture());

        EventNamespace ns = namespaceArgumentCaptor.getAllValues().get(0);
        assertTfwNamespaceValuesForTweets(ns);
        Assert.assertThat(ns.component, is(TEST_VIEW_NAME));
        Assert.assertThat(ns.action, is(REQUIRED_SCRIBE_IMPRESSION_ACTION));
        ns = namespaceArgumentCaptor.getAllValues().get(1);
        assertSyndicatedNamespaceValuesForTweets(ns);
        Assert.assertThat(ns.section, is(TEST_VIEW_NAME));
        Assert.assertThat(ns.action, is(REQUIRED_SCRIBE_IMPRESSION_ACTION));

        List<ScribeItem> items = itemsArgumentCaptor.getAllValues().get(0);
        assertItems(items);
        items = itemsArgumentCaptor.getAllValues().get(1);
        assertItems(items);
    }

    @Test
    public void testShare() {
        scribeClient.share(TestFixtures.TEST_TWEET);

        verify(tweetUi).scribe(namespaceArgumentCaptor.capture(), itemsArgumentCaptor.capture());

        final EventNamespace ns = namespaceArgumentCaptor.getValue();
        assertTfwNamespaceForActions(ns);
        Assert.assertThat(ns.action, is(REQUIRED_SCRIBE_SHARE_ACTION));
        assertItems(itemsArgumentCaptor.getValue());
    }

    @Test
    public void testFavorite() {
        scribeClient.favorite(TestFixtures.TEST_TWEET);

        verify(tweetUi).scribe(namespaceArgumentCaptor.capture(), itemsArgumentCaptor.capture());
        final EventNamespace ns = namespaceArgumentCaptor.getValue();
        assertTfwNamespaceForActions(ns);
        Assert.assertThat(ns.action, is(REQUIRED_SCRIBE_FAVORITE_ACTION));
        assertItems(itemsArgumentCaptor.getValue());
    }

    @Test
    public void testUnfavorite() {
        scribeClient.unfavorite(TestFixtures.TEST_TWEET);

        verify(tweetUi).scribe(namespaceArgumentCaptor.capture(), itemsArgumentCaptor.capture());
        final EventNamespace ns = namespaceArgumentCaptor.getValue();
        assertTfwNamespaceForActions(ns);
        Assert.assertThat(ns.action, is(REQUIRED_SCRIBE_UNFAVORITE_ACTION));
        assertItems(itemsArgumentCaptor.getValue());
    }

    @Test
    public void testClick() {
        scribeClient.click(TestFixtures.TEST_TWEET, TEST_VIEW_NAME);

        verify(tweetUi).scribe(namespaceArgumentCaptor.capture(), itemsArgumentCaptor.capture());
        final EventNamespace ns = namespaceArgumentCaptor.getValue();
        assertTfwNamespaceValuesForTweets(ns);
        Assert.assertThat(ns.component, is(TEST_VIEW_NAME));
        Assert.assertThat(ns.action, is(REQUIRED_SCRIBE_CLICK_ACTION));
        assertItems(itemsArgumentCaptor.getValue());
    }

    @Test
    public void testGetTfwImpressionNamespace_actionsEnabled() {
        final EventNamespace ns =
                TweetScribeClientImpl.getTfwImpressionNamespace(TEST_VIEW_NAME, true);
        Assert.assertThat(ns.client, is(REQUIRED_TFW_SCRIBE_CLIENT));
        Assert.assertThat(ns.page, is(REQUIRED_TFW_SCRIBE_PAGE));
        Assert.assertThat(ns.section, is(REQUIRED_TFW_SCRIBE_SECTION));
        Assert.assertThat(ns.element, is(REQUIRED_SCRIBE_ACTIONS_ELEMENT));
        Assert.assertThat(ns.component, is(TEST_VIEW_NAME));
        Assert.assertThat(ns.action, is(REQUIRED_SCRIBE_IMPRESSION_ACTION));
    }

    @Test
    public void testGetTfwImpressionNamespace_actionsDisabled() {
        final EventNamespace ns =
                TweetScribeClientImpl.getTfwImpressionNamespace(TEST_VIEW_NAME, false);
        assertTfwNamespaceValuesForTweets(ns);
        Assert.assertThat(ns.component, is(TEST_VIEW_NAME));
        Assert.assertThat(ns.action, is(REQUIRED_SCRIBE_IMPRESSION_ACTION));
    }

    @Test
    public void testGetSyndicatedImpressionNamespace() {
        final EventNamespace ns =
                TweetScribeClientImpl.getSyndicatedImpressionNamespace(TEST_VIEW_NAME);
        assertSyndicatedNamespaceValuesForTweets(ns);
        Assert.assertThat(ns.section, is(TEST_VIEW_NAME));
        Assert.assertThat(ns.action, is(REQUIRED_SCRIBE_IMPRESSION_ACTION));
    }

    @Test
    public void testGetTfwClickNamespace() {
        final EventNamespace ns = TweetScribeClientImpl.getTfwClickNamespace(TEST_VIEW_NAME);

        assertTfwNamespaceValuesForTweets(ns);
        Assert.assertThat(ns.component, is(TEST_VIEW_NAME));
        Assert.assertThat(ns.action, is(REQUIRED_SCRIBE_CLICK_ACTION));
    }

    @Test
    public void testGetTfwFavoriteNamespace() {
        final EventNamespace ns = TweetScribeClientImpl.getTfwFavoriteNamespace();
        assertTfwNamespaceForActions(ns);
        Assert.assertThat(ns.action, is(REQUIRED_SCRIBE_FAVORITE_ACTION));
    }

    @Test
    public void testGetTfwUnfavoriteNamespace() {
        final EventNamespace ns = TweetScribeClientImpl.getTfwUnfavoriteNamespace();
        assertTfwNamespaceForActions(ns);
        Assert.assertThat(ns.action, is(REQUIRED_SCRIBE_UNFAVORITE_ACTION));
    }

    @Test
    public void testGetTfwShareNamespace() {
        final EventNamespace ns = TweetScribeClientImpl.getTfwShareNamespace();
        assertTfwNamespaceForActions(ns);
        Assert.assertThat(ns.action, is(REQUIRED_SCRIBE_SHARE_ACTION));
    }

    static void assertItems(List<ScribeItem> items) {
        Assert.assertThat(items, notNullValue());
        Assert.assertThat(items.size(), is(1));
        Assert.assertThat(items.get(0).getId().longValue(), is(TestFixtures.TEST_TWEET.getId()));
        Assert.assertThat(items.get(0).getItemType().intValue(), is(ScribeItem.TYPE_TWEET));
    }

    static void assertTfwNamespaceForActions(EventNamespace ns) {
        Assert.assertThat(ns.client, is(REQUIRED_TFW_SCRIBE_CLIENT));
        Assert.assertThat(ns.page, is(REQUIRED_TFW_SCRIBE_PAGE));
        Assert.assertThat(ns.section, is(REQUIRED_TFW_SCRIBE_SECTION));
        Assert.assertThat(ns.element, is(REQUIRED_SCRIBE_ACTIONS_ELEMENT));
    }

    static void assertTfwNamespaceValuesForTweets(EventNamespace ns) {
        Assert.assertThat(ns.client, is(REQUIRED_TFW_SCRIBE_CLIENT));
        Assert.assertThat(ns.page, is(REQUIRED_TFW_SCRIBE_PAGE));
        Assert.assertThat(ns.section, is(REQUIRED_TFW_SCRIBE_SECTION));
        Assert.assertThat(ns.element, is(REQUIRED_TFW_SCRIBE_ELEMENT));
    }

    static void assertSyndicatedNamespaceValuesForTweets(EventNamespace ns) {
        Assert.assertThat(ns.client, is(REQUIRED_SDK_SCRIBE_CLIENT));
        Assert.assertThat(ns.page, is(REQUIRED_SDK_SCRIBE_PAGE));
        Assert.assertThat(ns.component, is(REQUIRED_SDK_SCRIBE_COMPONENT));
        Assert.assertThat(ns.element, is(REQUIRED_SDK_SCRIBE_ELEMENT));
    }
}
