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

package com.twitter.sdk.android.tweetui.internal;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

public class MediaTimeUtilsTest {
    static final int SECOND_IN_MS = 1000;
    static final int MINUTE_IN_MS = 60000;
    static final int HOUR_IN_MS = 3600000;

    @Test
    public void testFormatPlaybackTime() {
        Assert.assertThat(MediaTimeUtils.getPlaybackTime(SECOND_IN_MS), is("0:01"));
        Assert.assertThat(MediaTimeUtils.getPlaybackTime(MINUTE_IN_MS + SECOND_IN_MS), is("1:01"));
        Assert.assertThat(MediaTimeUtils
                .getPlaybackTime(HOUR_IN_MS + MINUTE_IN_MS + SECOND_IN_MS), is("1:01:01"));
    }
}
