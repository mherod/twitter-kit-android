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

import android.graphics.Color;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
public class ColorUtilsTest {

    @Test
    public void testIsLightColor_blue() {
        Assert.assertThat(ColorUtils.INSTANCE.isLightColor(Color.BLUE), is(false));
    }

    @Test
    public void testIsLightColor_black() {
        Assert.assertThat(ColorUtils.INSTANCE.isLightColor(Color.BLACK), is(false));
    }

    @Test
    public void testIsLightColor_white() {
        Assert.assertThat(ColorUtils.INSTANCE.isLightColor(Color.WHITE), is(true));
    }

    @Test
    public void testCalculateOpacityTransform_zeroOpacity() {
        Assert.assertThat(ColorUtils.calculateOpacityTransform(0, Color.BLUE, Color.WHITE), is(Color.WHITE));
    }

    @Test
    public void testCalculateOpacityTransform_fullOpacity() {
        Assert.assertThat(ColorUtils.calculateOpacityTransform(1, Color.BLUE, Color.WHITE), is(Color.BLUE));
    }

    @Test
    public void testCalculateOpacityTransform_returnsFullOpacity() {
        final int color = ColorUtils.calculateOpacityTransform(0, Color.BLUE, Color.WHITE);
        Assert.assertThat(color & 0xFF000000, is(0xFF000000));
    }
}
