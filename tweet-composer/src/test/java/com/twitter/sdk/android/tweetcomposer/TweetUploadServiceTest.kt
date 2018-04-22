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

package com.twitter.sdk.android.tweetcomposer

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.twitter.sdk.android.core.TwitterApiClient
import com.twitter.sdk.android.core.TwitterAuthToken
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.models.Media
import com.twitter.sdk.android.core.models.TweetBuilder
import com.twitter.sdk.android.core.services.MediaService
import com.twitter.sdk.android.core.services.StatusesService
import okhttp3.RequestBody
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import retrofit2.Call
import retrofit2.mock.Calls
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class TweetUploadServiceTest {

    private var context: Context? = null
    private var mockStatusesService: StatusesService? = null
    private var mockMediaService: MediaService? = null
    private var service: TweetUploadService? = null

    @Before
    fun setUp() {
        context = RuntimeEnvironment.application
        mockMediaService = mock(MediaService::class.java)
        mockStatusesService = mock(StatusesService::class.java)

        val tweet = TweetBuilder().setId(123L).setText(EXPECTED_TWEET_TEXT).build()

        `when`<Call<Media>>(mockMediaService!!
                .upload(
                        ArgumentMatchers.any(RequestBody::class.java),
                        ArgumentMatchers.any(RequestBody::class.java),
                        ArgumentMatchers.any(RequestBody::class.java)
                ))
                .thenReturn(mock<Call<Media>>(Call::class.java as Class<Call<Media>>))

        `when`(mockStatusesService!!.update(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.isNull(Long::class.java),
                ArgumentMatchers.isNull(Boolean::class.java),
                ArgumentMatchers.isNull(Double::class.java),
                ArgumentMatchers.isNull(Double::class.java),
                ArgumentMatchers.isNull(String::class.java),
                ArgumentMatchers.isNull(Boolean::class.java),
                ArgumentMatchers.eq(true),
                ArgumentMatchers.isNull(String::class.java)
        )).thenReturn(Calls.response(tweet))

        val mockTwitterApiClient = mock(TwitterApiClient::class.java)
        `when`(mockTwitterApiClient.statusesService).thenReturn(mockStatusesService)
        `when`(mockTwitterApiClient.mediaService).thenReturn(mockMediaService)

        val mockDependencyProvider = mock(TweetUploadService.DependencyProvider::class.java)
        `when`(mockDependencyProvider.getTwitterApiClient(ArgumentMatchers.any(TwitterSession::class.java)))
                .thenReturn(mockTwitterApiClient)

        service = spy(Robolectric.buildService(TweetUploadService::class.java).create().get())
        service!!.dependencyProvider = mockDependencyProvider
    }

    @Test
    fun testOnHandleIntent() {
        val mockToken = mock(TwitterAuthToken::class.java)

        val intent = Intent(context, TweetUploadService::class.java)
        intent.putExtra(TweetUploadService.EXTRA_USER_TOKEN, mockToken)
        intent.putExtra(TweetUploadService.EXTRA_TWEET_TEXT, EXPECTED_TWEET_TEXT)
        intent.putExtra(TweetUploadService.EXTRA_IMAGE_URI, Uri.EMPTY)
        service!!.onHandleIntent(intent)

        verify<TweetUploadService>(service).uploadTweet(
                ArgumentMatchers.any(TwitterSession::class.java),
                ArgumentMatchers.eq(EXPECTED_TWEET_TEXT),
                ArgumentMatchers.eq(Uri.EMPTY)
        )
    }

    @Test
    fun testUploadTweet_withNoMediaSuccess() {
        service!!.uploadTweet(mock(TwitterSession::class.java), EXPECTED_TWEET_TEXT, null)

        verify<StatusesService>(mockStatusesService).update(
                ArgumentMatchers.eq(EXPECTED_TWEET_TEXT),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.eq(true),
                ArgumentMatchers.isNull()
        )
        verifyZeroInteractions(mockMediaService)
        verify<TweetUploadService>(service).sendSuccessBroadcast(ArgumentMatchers.eq(123L))
        verify<TweetUploadService>(service).stopSelf()
    }

    @Test
    fun testUploadTweet_withNoMediaFailure() {
        `when`(mockStatusesService!!.update(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.eq(true),
                ArgumentMatchers.isNull()
        )).thenReturn(Calls.failure(IOException("")))

        service!!.uploadTweet(mock(TwitterSession::class.java), EXPECTED_TWEET_TEXT, null)

        verify<StatusesService>(mockStatusesService).update(
                ArgumentMatchers.eq(EXPECTED_TWEET_TEXT),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.eq(true),
                ArgumentMatchers.isNull()
        )
        verifyZeroInteractions(mockMediaService)
        verify<TweetUploadService>(service).fail(ArgumentMatchers.any(TwitterException::class.java))
        verify<TweetUploadService>(service).stopSelf()
    }

    @Test
    fun testUploadTweet_withInvalidUri() {
        service!!.uploadTweet(mock(TwitterSession::class.java), EXPECTED_TWEET_TEXT, Uri.EMPTY)

        verifyZeroInteractions(mockStatusesService)
        verifyZeroInteractions(mockMediaService)
        verify<TweetUploadService>(service).fail(ArgumentMatchers.any(TwitterException::class.java))
        verify<TweetUploadService>(service).stopSelf()
    }

    @Test
    fun testSendSuccessBroadcast() {
        val intentCaptor = ArgumentCaptor.forClass(Intent::class.java)
        service!!.sendSuccessBroadcast(ArgumentMatchers.anyLong())
        verify<TweetUploadService>(service).sendBroadcast(intentCaptor.capture())

        val capturedIntent = intentCaptor.value
        assertThat(capturedIntent.action, `is`(TweetUploadService.UPLOAD_SUCCESS))
        assertThat(capturedIntent.`package`, `is`(RuntimeEnvironment.application.packageName))
    }

    @Test
    fun testSendFailureBroadcast() {
        val mockIntent = mock(Intent::class.java)
        val intentCaptor = ArgumentCaptor.forClass(Intent::class.java)
        service!!.sendFailureBroadcast(mockIntent)
        verify<TweetUploadService>(service).sendBroadcast(intentCaptor.capture())

        val capturedIntent = intentCaptor.value
        assertThat(capturedIntent.action, `is`(TweetUploadService.UPLOAD_FAILURE))
        assertThat(capturedIntent.getParcelableExtra(TweetUploadService.EXTRA_RETRY_INTENT), `is`(mockIntent))
        assertThat(capturedIntent.`package`, `is`(RuntimeEnvironment.application.packageName))
    }

    companion object {
        private const val EXPECTED_TWEET_TEXT = "tweet text"
    }
}
