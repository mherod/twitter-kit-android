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

import android.test.AndroidTestCase;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;

import org.junit.Assert;

import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class VideoControlViewTest extends AndroidTestCase {
    private static final int SECOND_IN_MS = 1000;
    private static final int MINUTE_IN_MS = 60000;
    private static final int TEST_BUFFER_PROGRESS = 32;
    private VideoControlView videoControlView;

    public void setUp() throws Exception {
        super.setUp();
        videoControlView = new VideoControlView(getContext());
        videoControlView.onFinishInflate();
    }

    public void testInitialState() {
        Assert.assertThat(videoControlView.getVisibility(), is(View.VISIBLE));
        Assert.assertThat(videoControlView.seekBar, notNullValue());
        Assert.assertThat(videoControlView.duration, notNullValue());
        Assert.assertThat(videoControlView.currentTime, notNullValue());
        Assert.assertThat(videoControlView.stateControl, notNullValue());
        Assert.assertThat(videoControlView.player, nullValue());

        Assert.assertThat(videoControlView.seekBar.getMax(), is(1000));
        Assert.assertThat(videoControlView.seekBar.getProgress(), is(0));
        Assert.assertThat(videoControlView.seekBar.getSecondaryProgress(), is(0));

        Assert.assertThat(videoControlView.duration.getText(), is("0:00"));
        Assert.assertThat(videoControlView.currentTime.getText(), is("0:00"));
    }

    public void testCreateStateControlClickListener() {
        final VideoControlView.MediaPlayerControl player =
                mock(VideoControlView.MediaPlayerControl.class);
        videoControlView.setMediaPlayer(player);

        final View.OnClickListener listener = videoControlView.createStateControlClickListener();

        when(player.isPlaying()).thenReturn(false);
        listener.onClick(null);
        verify(player).start();

        when(player.isPlaying()).thenReturn(true);
        listener.onClick(null);
        verify(player).pause();
    }

    public void testCreateProgressChangeListener() {
        final VideoControlView.MediaPlayerControl player =
                mock(VideoControlView.MediaPlayerControl.class);
        videoControlView.setMediaPlayer(player);

        final SeekBar.OnSeekBarChangeListener listener =
                videoControlView.createProgressChangeListener();

        when(player.getDuration()).thenReturn(MINUTE_IN_MS);
        listener.onProgressChanged(null, 500, true);
        verify(player).seekTo(30000);
        Assert.assertThat(videoControlView.currentTime.getText(), is("0:30"));
    }

    public void testCreateProgressChangeListener_fromUserFalse() {
        final VideoControlView.MediaPlayerControl player =
                mock(VideoControlView.MediaPlayerControl.class);
        videoControlView.setMediaPlayer(player);

        final SeekBar.OnSeekBarChangeListener listener =
                videoControlView.createProgressChangeListener();

        when(player.getDuration()).thenReturn(MINUTE_IN_MS);
        listener.onProgressChanged(null, 500, false);
        verifyNoMoreInteractions(player);
    }

    public void testIsShowing() {
        Assert.assertThat(videoControlView.isShowing(), is(true));
    }

    public void testUpdateProgress() {
        final VideoControlView.MediaPlayerControl player =
                mock(VideoControlView.MediaPlayerControl.class);
        when(player.getCurrentPosition()).thenReturn(SECOND_IN_MS);
        when(player.getDuration()).thenReturn(MINUTE_IN_MS);
        when(player.getBufferPercentage()).thenReturn(50);
        videoControlView.setMediaPlayer(player);

        videoControlView.updateProgress();

        Assert.assertThat(videoControlView.seekBar.getProgress(), is(16));
        Assert.assertThat(videoControlView.seekBar.getSecondaryProgress(), is(500));

        Assert.assertThat(videoControlView.duration.getText(), is("1:00"));
        Assert.assertThat(videoControlView.currentTime.getText(), is("0:01"));
    }

    public void testSetDuration() {
        videoControlView.setDuration(SECOND_IN_MS);
        Assert.assertThat(videoControlView.duration.getText(), is("0:01"));
    }

    public void testSetCurrentTime() {
        videoControlView.setCurrentTime(SECOND_IN_MS);
        Assert.assertThat(videoControlView.currentTime.getText(), is("0:01"));
    }

    public void testSetSeekBarProgress() {
        videoControlView.setProgress(SECOND_IN_MS, MINUTE_IN_MS, TEST_BUFFER_PROGRESS);
        Assert.assertThat(videoControlView.seekBar.getProgress(), is(16));
        Assert.assertThat(videoControlView.seekBar.getSecondaryProgress(), is(320));
    }

    public void testSetSeekBarProgress_zeroDuration() {
        videoControlView.setProgress(SECOND_IN_MS, 0, TEST_BUFFER_PROGRESS);
        Assert.assertThat(videoControlView.seekBar.getProgress(), is(0));
        Assert.assertThat(videoControlView.seekBar.getSecondaryProgress(), is(320));
    }

    public void testSetPlayDrawable() {
        videoControlView.stateControl = mock(ImageButton.class);
        videoControlView.setPlayDrawable();

        verify(videoControlView.stateControl).setImageResource(R.drawable.tw__video_play_btn);
        verify(videoControlView.stateControl)
                .setContentDescription(getContext().getString(R.string.tw__play));
    }

    public void testSetPauseDrawable() {
        videoControlView.stateControl = mock(ImageButton.class);
        videoControlView.setPauseDrawable();

        verify(videoControlView.stateControl).setImageResource(R.drawable.tw__video_pause_btn);
        verify(videoControlView.stateControl)
                .setContentDescription(getContext().getString(R.string.tw__pause));
    }

    public void testSetReplayDrawable() {
        videoControlView.stateControl = mock(ImageButton.class);
        videoControlView.setReplayDrawable();

        verify(videoControlView.stateControl).setImageResource(R.drawable.tw__video_replay_btn);
        verify(videoControlView.stateControl)
                .setContentDescription(getContext().getString(R.string.tw__replay));
    }

    public void testSetMediaPlayer() {
        videoControlView.setMediaPlayer(mock(VideoControlView.MediaPlayerControl.class));
        Assert.assertThat(videoControlView.player, notNullValue());
    }
}
