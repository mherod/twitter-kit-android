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

import android.graphics.Color;
import android.text.TextPaint;
import android.view.View;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricTestRunner.class)
public class ClickableLinkSpanTest {

    private static final int SELECTED_COLOR = 1;
    private static final int LINK_COLOR = 2;

    @Mock
    private TextPaint textPaint;

    private ClickableLinkSpan clickableLinkSpan;

    @Before
    public void before() {
        initMocks(this);
    }

    @Test
    public void testUpdateDrawState_shouldSetTextPaintDefaultColor() {
        clickableLinkSpan = new TestClickableLinkSpan(SELECTED_COLOR);
        clickableLinkSpan.updateDrawState(textPaint);
        verify(textPaint).setColor(textPaint.linkColor);
        Assert.assertThat(textPaint.bgColor, is(Color.TRANSPARENT));
    }

    @Test
    public void testUpdateDrawState_shouldSetTextPaintProvidedColor() {
        clickableLinkSpan = new TestClickableLinkSpan(SELECTED_COLOR, LINK_COLOR, false);
        clickableLinkSpan.updateDrawState(textPaint);
        verify(textPaint).setColor(LINK_COLOR);
        Assert.assertThat(textPaint.bgColor, is(Color.TRANSPARENT));
    }

    @Test
    public void testUpdateDrawState_shouldSetTextPaintUnderlined() {
        clickableLinkSpan = new TestClickableLinkSpan(SELECTED_COLOR, LINK_COLOR, true);
        clickableLinkSpan.updateDrawState(textPaint);
        verify(textPaint).setUnderlineText(true);
        Assert.assertThat(textPaint.bgColor, is(Color.TRANSPARENT));
    }

    @Test
    public void testUpdateDrawState_shouldSetTextPaintBackground() {
        clickableLinkSpan = new TestClickableLinkSpan(SELECTED_COLOR, LINK_COLOR, true);
        clickableLinkSpan.select(true);
        clickableLinkSpan.updateDrawState(textPaint);
        verify(textPaint).setUnderlineText(true);
        Assert.assertThat(textPaint.bgColor, is(SELECTED_COLOR));
    }

    @Test
    public void testSelect_shouldSetIsSelectedTrue() {
        clickableLinkSpan = new TestClickableLinkSpan(SELECTED_COLOR, LINK_COLOR, true);
        clickableLinkSpan.select(true);
        Assert.assertThat(clickableLinkSpan.isSelected(), is(true));
    }

    @Test
    public void testSelect_shouldSetIsSelectedFalse() {
        clickableLinkSpan = new TestClickableLinkSpan(SELECTED_COLOR, LINK_COLOR, true);
        clickableLinkSpan.select(false);
        Assert.assertThat(clickableLinkSpan.isSelected(), is(false));
    }

    private static class TestClickableLinkSpan extends ClickableLinkSpan {
        TestClickableLinkSpan(int selectedColor) {
            super(selectedColor, 0, false, false);
        }

        TestClickableLinkSpan(int selectedColor, int linkColor, boolean underlined) {
            super(selectedColor, linkColor, true, underlined);
        }

        @Override
        public void onClick(View widget) {
        }
    }
}
