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

import android.view.View;
import android.view.ViewGroup;

import com.twitter.sdk.android.core.models.MediaEntity;
import com.twitter.sdk.android.tweetui.internal.GalleryImageView;
import com.twitter.sdk.android.tweetui.internal.SwipeToDismissTouchListener;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class GalleryAdapterTest {
    @Mock
    SwipeToDismissTouchListener.Callback callback;
    @Mock
    MediaEntity entity;
    @Mock
    View view;
    @Mock
    ViewGroup container;
    GalleryAdapter subject;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        subject = new GalleryAdapter(RuntimeEnvironment.application, callback);
    }

    @Test
    public void testGetCount_withZeroItems() {
        Assert.assertThat(subject.getCount(), is(0));
    }

    @Test
    public void testGetCount_withOneItems() {
        subject.addAll(Collections.singletonList(entity));
        Assert.assertThat(subject.getCount(), is(1));
    }

    @Test
    public void testIsViewFromObject_withSameObject() {
        Assert.assertThat(subject.isViewFromObject(view, view), is(true));
    }

    @Test
    public void testIsViewFromObject_withDifferentObject() {
        Assert.assertThat(subject.isViewFromObject(view, entity), is(false));
    }

    @Test
    public void testInstantiateItem() {
        subject.addAll(Collections.singletonList(entity));
        final GalleryImageView result = (GalleryImageView) subject.instantiateItem(container, 0);

        Assert.assertThat(result, notNullValue());
        verify(container).addView(result);
    }

    @Test
    public void testDestroyItem() {
        subject.destroyItem(container, 0, view);

        verify(container).removeView(view);
    }
}
