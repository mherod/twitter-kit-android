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

package com.twitter.sdk.android.mopub;

import android.graphics.Color;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
public class ColorUtilsTest {

    @Test
    public void testIsLightColor_black() {
        Assert.assertThat(ColorUtils.isLightColor(Color.BLACK), is(false));
    }

    @Test
    public void testIsLightColor_white() {
        Assert.assertThat(ColorUtils.isLightColor(Color.WHITE), is(true));
    }

    @Test
    public void testDefaultCtaButtonIsDarkColor() {
        Assert.assertThat(ColorUtils.isLightColor(R.color.tw__ad_cta_default), is(false));
    }

    @Test
    public void testCtaTextColorIsLightForDarkBgColor() {
        Assert.assertThat(ColorUtils.calculateCtaTextColor(R.color.tw__ad_cta_default), is(Color.WHITE));
        Assert.assertThat(ColorUtils.calculateCtaTextColor(Color.BLACK), is(Color.WHITE));
        Assert.assertThat(ColorUtils.calculateCtaTextColor(Color.DKGRAY), is(Color.WHITE));
    }

    @Test
    public void testCtaTextColorIsDarkForLightBgColor() {
        Assert.assertThat(Color.WHITE, not(is(ColorUtils.calculateCtaTextColor(Color.WHITE))));
        Assert.assertThat(Color.WHITE, not(is(ColorUtils.calculateCtaTextColor(Color.LTGRAY))));
    }

    @Test
    public void testCTAOnTapColorIsLighterForDarkBgColor() {
        final int darkColor = Color.BLACK;
        final int originalRed = Color.red(darkColor);
        final int originalGreen = Color.green(darkColor);
        final int originalBlue = Color.blue(darkColor);

        final int lighterColor = ColorUtils.calculateCtaOnTapColor(darkColor);
        final int lighterRed = Color.red(lighterColor);
        final int lighterGreen = Color.green(lighterColor);
        final int lighterBlue = Color.blue(lighterColor);

        Assert.assertThat(lighterRed > originalRed
                && lighterGreen > originalGreen
                && lighterBlue > originalBlue, is(true));
    }

    @Test
    public void testCTAOnTapColorIsDarkerForLightBgColor() {
        final int lightColor = Color.WHITE;
        final int originalRed = Color.red(lightColor);
        final int originalGreen = Color.green(lightColor);
        final int originalBlue = Color.blue(lightColor);

        final int darkerColor = ColorUtils.calculateCtaOnTapColor(lightColor);
        final int darkerRed = Color.red(darkerColor);
        final int darkerGreen = Color.green(darkerColor);
        final int darkerBlue = Color.blue(darkerColor);

        Assert.assertThat(originalRed > darkerRed
                && originalGreen > darkerGreen
                && originalBlue > darkerBlue, is(true));
    }

    @Test
    public void testContrastColorForDarkColor() {
        final int darkColor = Color.BLACK;
        final int contrastingLightColor = ColorUtils.calculateContrastingColor(darkColor);
        Assert.assertThat(ColorUtils.isLightColor(contrastingLightColor), is(true));
    }

    @Test
    public void testContrastColorForLightColor() {
        final int lightColor = Color.WHITE;
        final int contrastingDarkColor = ColorUtils.calculateContrastingColor(lightColor);
        Assert.assertThat(ColorUtils.isLightColor(contrastingDarkColor), is(false));
    }
}
