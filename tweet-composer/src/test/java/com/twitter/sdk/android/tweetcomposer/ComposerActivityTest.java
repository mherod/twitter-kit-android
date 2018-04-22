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

package com.twitter.sdk.android.tweetcomposer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterSession;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ComposerActivityTest {
    private static final String ANY_HASHTAG = "#hashtag";
    private static final String ANY_TEXT = "text";
    private Context mockContext;
    private TwitterSession mockSession;
    private TwitterAuthToken mockAuthToken;
    private Uri mockUri;

    @Before
    public void setUp() {
        mockContext = mock(Context.class);
        mockSession = mock(TwitterSession.class);
        mockAuthToken = mock(TwitterAuthToken.class);
        mockUri = Uri.EMPTY;
        when(mockSession.getAuthToken()).thenReturn(mockAuthToken);
    }

    @Test
    public void testBuilder() {
        final ComposerActivity.Builder builder = new ComposerActivity.Builder(mockContext);
        assertThat(builder, notNullValue());
    }

    @Test
    public void testBuilder_nullContext() {
        try {
            new ComposerActivity.Builder(null);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Context must not be null"));
        }
    }

    @Test
    public void testBuilderSession() {
        final Intent intent = new ComposerActivity.Builder(mockContext)
                .session(mockSession)
                .createIntent();
        verify(mockSession).getAuthToken();
        assertThat(intent.getParcelableExtra(ComposerActivity.EXTRA_USER_TOKEN), is(mockAuthToken));
    }

    @Test
    public void testBuilderSession_nullSession() {
        try {
            new ComposerActivity.Builder(mockContext).session(null);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("TwitterSession must not be null"));
        }
    }

    @Test
    public void testBuilderSession_nullAuthToken() {
        when(mockSession.getAuthToken()).thenReturn(null);
        try {
            new ComposerActivity.Builder(mockContext).session(mockSession);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("TwitterSession token must not be null"));
        }
    }

    @Test
    public void testBuilderSession_sessionNotSet() {
        try {
            new ComposerActivity.Builder(mockContext).createIntent();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Must set a TwitterSession"));
        }
    }

    @Test
    public void testBuilderDarkTheme() {
        final Intent intent = new ComposerActivity.Builder(mockContext)
                .session(mockSession)
                .darkTheme()
                .createIntent();
        assertThat(intent.getIntExtra(ComposerActivity.EXTRA_THEME, -1), is(R.style.ComposerDark));
    }

    @Test
    public void testBuilder_defaultLightTheme() {
        final Intent intent = new ComposerActivity.Builder(mockContext)
                .session(mockSession)
                .createIntent();
        assertThat(intent.getIntExtra(ComposerActivity.EXTRA_THEME, -1), is(R.style.ComposerLight));
    }

    @Test
    public void testBuilderText() {
        final Intent intent = new ComposerActivity.Builder(mockContext)
                .session(mockSession)
                .text(ANY_TEXT)
                .createIntent();

        assertThat(intent.getStringExtra(ComposerActivity.EXTRA_TEXT), is(ANY_TEXT));
    }

    @Test
    public void testBuilder_emptyArray() {
        final Intent intent = new ComposerActivity.Builder(mockContext)
                .session(mockSession)
                .hashtags()
                .createIntent();

        assertThat(intent.getStringExtra(ComposerActivity.EXTRA_HASHTAGS), nullValue());
    }

    @Test
    public void testBuilder_validHashtags() {
        final Intent intent = new ComposerActivity.Builder(mockContext)
                .session(mockSession)
                .hashtags(ANY_HASHTAG)
                .createIntent();

        assertThat(intent.getStringExtra(ComposerActivity.EXTRA_HASHTAGS), is(ANY_HASHTAG));
    }

    @Test
    public void testBuilder_invalidHashtags() {
        final Intent intent = new ComposerActivity.Builder(mockContext)
                .session(mockSession)
                .hashtags("NotHashtag")
                .createIntent();

        assertThat(intent.getStringExtra(ComposerActivity.EXTRA_HASHTAGS), nullValue());
    }

    @Test
    public void testBuilderImage() {
        final Intent intent = new ComposerActivity.Builder(mockContext)
                .session(mockSession)
                .image(mockUri)
                .createIntent();
        assertThat(intent.getParcelableExtra(ComposerActivity.EXTRA_IMAGE_URI), is(mockUri));
    }
}
