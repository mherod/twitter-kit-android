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

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.test.AndroidTestCase;

import org.junit.Assert;

import static org.hamcrest.CoreMatchers.is;

public class MultiTouchImageViewTest extends AndroidTestCase {
    private static final RectF TEST_VIEW_RECT = new RectF(0, 0, 100, 100);
    private static final Matrix TEST_BASE_MATRIX = new MatrixBuilder().postScale(2f).build();
    private static final Matrix TEST_IDENTITY_MATRIX = new MatrixBuilder().build();
    private static final float TEST_BASE_SCALE = 1f;
    private static final Bitmap image = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
    private MultiTouchImageView view;

    public void setUp() throws Exception {
        super.setUp();
        view = new MultiTouchImageView(getContext());
        view.setImageBitmap(image);
        view.layout(0, 0, 100, 100);
    }

    public void testInitialViewState() {
        Assert.assertThat(view.baseMatrix, is(TEST_BASE_MATRIX));
        Assert.assertThat(view.updateMatrix, is(TEST_IDENTITY_MATRIX));
        Assert.assertThat(view.viewRect, is(TEST_VIEW_RECT));
        Assert.assertThat(view.getScale(), is(TEST_BASE_SCALE));
        Assert.assertThat(view.getDrawMatrix(), is(TEST_BASE_MATRIX));
    }

    public void testGetDrawRect() {
        final Matrix matrix = new MatrixBuilder()
                .postScale(2f)
                .postTranslate(10f, 10f)
                .build();
        final RectF result = view.getDrawRect(matrix);
        final RectF expected = new RectF(10f, 10f, 110f, 110f);
        Assert.assertThat(result, is(expected));
    }

    public void testSetScale() {
        view.setScale(1.5f, 50f, 50f);

        final Matrix expected = new MatrixBuilder()
                .postScale(1.5f)
                .postTranslate(-25f, -25f)
                .build();
        Assert.assertThat(view.updateMatrix, is(expected));
        Assert.assertEquals(1.5f, view.getScale(), 0.0);
        Assert.assertThat(view.baseMatrix, is(TEST_BASE_MATRIX));
    }

    public void testReset() {
        view.setScale(1.5f, 50f, 50f);
        view.reset();

        Assert.assertThat(view.updateMatrix, is(TEST_IDENTITY_MATRIX));
        Assert.assertEquals(TEST_BASE_SCALE, view.getScale(), 0.0);
        Assert.assertThat(view.baseMatrix, is(TEST_BASE_MATRIX));
    }

    public void testSetTranslate() {
        view.setTranslate(10f, 10f);

        final Matrix expected = new MatrixBuilder()
                .postTranslate(10f, 10f)
                .build();
        Assert.assertThat(view.updateMatrix, is(expected));
        Assert.assertEquals(TEST_BASE_SCALE, view.getScale(), 0.0);
        Assert.assertThat(view.baseMatrix, is(TEST_BASE_MATRIX));
    }

    public void testCanBeSwiped_withScaleEqualOne() {
        Assert.assertThat(view.canBeSwiped(), is(true));
    }

    public void testCanBeSwiped_withScaleGreaterThanOne() {
        view.setScale(2, 0, 0);
        Assert.assertThat(view.canBeSwiped(), is(false));
    }

    static class MatrixBuilder {
        private final Matrix matrix = new Matrix();

        MatrixBuilder postScale(float scale) {
            matrix.postScale(scale, scale);
            return this;
        }

        MatrixBuilder postTranslate(float x, float y) {
            matrix.postTranslate(x, y);
            return this;
        }

        Matrix build() {
            return matrix;
        }
    }
}
