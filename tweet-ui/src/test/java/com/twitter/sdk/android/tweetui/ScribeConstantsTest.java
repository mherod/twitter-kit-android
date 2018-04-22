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

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

public class ScribeConstantsTest {
    static final String REQUIRED_TFW_SCRIBE_CLIENT = "tfw";
    static final String REQUIRED_TFW_SCRIBE_PAGE = "android";

    static final String REQUIRED_SDK_SCRIBE_CLIENT = "android";
    static final String REQUIRED_SDK_SCRIBE_ELEMENT = "";
    static final String REQUIRED_SCRIBE_IMPRESSION_ACTION = "impression";
    static final String REQUIRED_SCRIBE_INITIAL_ELEMENT = "initial";
    static final String REQUIRED_SCRIBE_TIMELINE_SECTION = "timeline";
    static final String REQUIRED_SCRIBE_TIMELINE_PAGE = "timeline";
    static final String REQUIRED_SCRIBE_INITIAL_COMPONENT = "initial";

    static final String TEST_VIEW_NAME = "compact";

    @Test
    public void testGetSyndicatedSdkTimelineNamespace() {
        final EventNamespace ns = ScribeConstants.getSyndicatedSdkTimelineNamespace(TEST_VIEW_NAME);

        Assert.assertThat(ns.client, is(REQUIRED_SDK_SCRIBE_CLIENT));
        Assert.assertThat(ns.page, is(REQUIRED_SCRIBE_TIMELINE_PAGE));
        Assert.assertThat(ns.section, is(TEST_VIEW_NAME));
        Assert.assertThat(ns.component, is(REQUIRED_SCRIBE_INITIAL_COMPONENT));
        Assert.assertThat(ns.element, is(REQUIRED_SDK_SCRIBE_ELEMENT));
        Assert.assertThat(ns.action, is(REQUIRED_SCRIBE_IMPRESSION_ACTION));
    }

    @Test
    public void testGetTfwClientTimelineNamespace() {
        final EventNamespace ns = ScribeConstants.getTfwClientTimelineNamespace(TEST_VIEW_NAME);

        Assert.assertThat(ns.client, is(REQUIRED_TFW_SCRIBE_CLIENT));
        Assert.assertThat(ns.page, is(REQUIRED_TFW_SCRIBE_PAGE));
        Assert.assertThat(ns.section, is(REQUIRED_SCRIBE_TIMELINE_SECTION));
        Assert.assertThat(ns.component, is(TEST_VIEW_NAME));
        Assert.assertThat(ns.element, is(REQUIRED_SCRIBE_INITIAL_ELEMENT));
        Assert.assertThat(ns.action, is(REQUIRED_SCRIBE_IMPRESSION_ACTION));
    }
}
