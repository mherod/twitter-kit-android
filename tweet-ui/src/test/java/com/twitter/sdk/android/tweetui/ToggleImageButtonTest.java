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

import android.util.AttributeSet;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
public class ToggleImageButtonTest {
    private static final String CONTENT_DESCRIPTION_ON = "ContentDescriptionOn";
    private static final String CONTENT_DESCRIPTION_OFF = "ContentDescriptionOff";

    ToggleImageButton createDefaultButton() {
        return new ToggleImageButton(RuntimeEnvironment.application);
    }

    ToggleImageButton createButtonWithAttributes() {
        final AttributeSet attrs = Robolectric.buildAttributeSet()
                .addAttribute(R.attr.contentDescriptionOff, CONTENT_DESCRIPTION_OFF)
                .addAttribute(R.attr.contentDescriptionOn, CONTENT_DESCRIPTION_ON)
                .addAttribute(R.attr.toggleOnClick, "false")
                .build();

        return new ToggleImageButton(RuntimeEnvironment.application, attrs);
    }

    @Test
    public void testInit() {
        final ToggleImageButton button = createDefaultButton();
        Assert.assertThat(button.contentDescriptionOn, nullValue());
        Assert.assertThat(button.contentDescriptionOff, nullValue());
        Assert.assertThat(button.isToggledOn(), is(false));
        Assert.assertThat(button.toggleOnClick, is(true));
    }

    @Test
    public void testPerformClick() {
        final ToggleImageButton button = createDefaultButton();
        Assert.assertThat(button.toggleOnClick, is(true));
        Assert.assertThat(button.isToggledOn(), is(false));
        button.performClick();
        Assert.assertThat(button.isToggledOn(), is(true));
    }

    @Test
    public void testSetToggledOn() {
        final ToggleImageButton button = createDefaultButton();
        Assert.assertThat(button.isToggledOn(), is(false));
        button.setToggledOn(true);
        Assert.assertThat(button.isToggledOn(), is(true));
    }

    @Test
    public void testToggle() {
        final ToggleImageButton button = createDefaultButton();
        Assert.assertThat(button.isToggledOn(), is(false));
        button.toggle();
        Assert.assertThat(button.isToggledOn(), is(true));
    }

    @Test
    public void testXmlInit() {
        final ToggleImageButton button = createButtonWithAttributes();
        Assert.assertThat(button.contentDescriptionOn, is(CONTENT_DESCRIPTION_ON));
        Assert.assertThat(button.contentDescriptionOff, is(CONTENT_DESCRIPTION_OFF));
        Assert.assertThat(button.isToggledOn(), is(false));
        Assert.assertThat(button.getContentDescription(), is(CONTENT_DESCRIPTION_OFF));
        Assert.assertThat(button.toggleOnClick, is(false));
    }

    @Test
    public void testPerformClick_toggleOnClickDisabled() {
        final ToggleImageButton button = createButtonWithAttributes();
        Assert.assertThat(button.toggleOnClick, is(false));
        Assert.assertThat(button.isToggledOn(), is(false));
        button.performClick();
        Assert.assertThat(button.isToggledOn(), is(false));
    }

    @Test
    public void testSetToggledOn_withContentDescription() {
        final ToggleImageButton button = createButtonWithAttributes();
        Assert.assertThat(button.isToggledOn(), is(false));
        Assert.assertThat(button.getContentDescription(), is(CONTENT_DESCRIPTION_OFF));
        button.setToggledOn(true);
        Assert.assertThat(button.isToggledOn(), is(true));
        Assert.assertThat(button.getContentDescription(), is(CONTENT_DESCRIPTION_ON));
    }
}
