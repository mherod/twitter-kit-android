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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
public class BaseTimelineTest {
    private static final Long TEST_ID = 200L;

    @Test
    public void testDecrementMaxId_positive() {
        final Long correctedId = BaseTimeline.decrementMaxId(TEST_ID);
        Assert.assertThat(correctedId, is(TEST_ID - 1L));
    }

    @Test
    public void testDecrementMaxId_nullId() {
        Assert.assertThat(BaseTimeline.decrementMaxId(null), nullValue());
    }
}
