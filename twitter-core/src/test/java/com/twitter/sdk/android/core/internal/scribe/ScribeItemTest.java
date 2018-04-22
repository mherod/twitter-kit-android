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

package com.twitter.sdk.android.core.internal.scribe;

import com.twitter.sdk.android.core.TestFixtures;
import com.twitter.sdk.android.core.models.Card;
import com.twitter.sdk.android.core.models.MediaEntity;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.TweetBuilder;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.core.models.UserBuilder;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
public class ScribeItemTest {
    static final long TEST_ID = 123;
    static final long TEST_MEDIA_ID = 586671909L;
    static final String TEST_MESSAGE = "test message";
    static final ScribeItem.CardEvent TEST_CARD_EVENT = new ScribeItem.CardEvent(1);
    static final ScribeItem.MediaDetails TEST_MEDIA_DETAILS = new ScribeItem.MediaDetails(1, 2, 3);

    static final String TEST_TYPE_ANIMATED_GIF = "animated_gif";
    static final String TEST_TYPE_CONSUMER = "video";
    static final int TEST_TYPE_CONSUMER_ID = 1;
    static final int TEST_TYPE_ANIMATED_GIF_ID = 3;
    static final int TEST_TYPE_VINE_ID = 4;

    @Test
    public void testFromTweet() {
        final Tweet tweet = new TweetBuilder().setId(TEST_ID).build();
        final ScribeItem item = ScribeItem.Companion.fromTweet(tweet);

        Assert.assertThat(item.getId(), is(Long.valueOf(TEST_ID)));
        Assert.assertThat(item.getItemType(), is(Integer.valueOf(ScribeItem.TYPE_TWEET)));
        Assert.assertThat(item.getDescription(), nullValue());
    }

    @Test
    public void testFromUser() {
        final User user = new UserBuilder().setId(TEST_ID).build();
        final ScribeItem item = ScribeItem.Companion.fromUser(user);

        Assert.assertThat(item.getId(), is(Long.valueOf(TEST_ID)));
        Assert.assertThat(item.getItemType(), is(Integer.valueOf(ScribeItem.TYPE_USER)));
        Assert.assertThat(item.getDescription(), nullValue());
    }

    @Test
    public void testFromMediaEntity_withAnimatedGif() {
        final MediaEntity animatedGif = createTestEntity(TEST_TYPE_ANIMATED_GIF);
        final ScribeItem scribeItem = ScribeItem.Companion.fromMediaEntity(TEST_ID, animatedGif);

        Assert.assertThat(scribeItem.getId(), is(Long.valueOf(TEST_ID)));
        Assert.assertThat(scribeItem.getItemType(), is(Integer.valueOf(ScribeItem.TYPE_TWEET)));
        assertMediaDetails(scribeItem.getMediaDetails(), TEST_TYPE_ANIMATED_GIF_ID);
    }

    @Test
    public void testFromMediaEntity_withConsumerVideo() {
        final MediaEntity videoEntity = createTestEntity(TEST_TYPE_CONSUMER);
        final ScribeItem scribeItem = ScribeItem.Companion.fromMediaEntity(TEST_ID, videoEntity);

        Assert.assertThat(scribeItem.getId(), is(Long.valueOf(TEST_ID)));
        Assert.assertThat(scribeItem.getItemType(), is(Integer.valueOf(ScribeItem.TYPE_TWEET)));
        assertMediaDetails(scribeItem.getMediaDetails(), TEST_TYPE_CONSUMER_ID);
    }

    @Test
    public void testFromTweetCard() {
        final long tweetId = TEST_ID;
        final Card vineCard = TestFixtures.sampleValidVineCard();
        final ScribeItem scribeItem = ScribeItem.Companion.fromTweetCard(tweetId, vineCard);

        Assert.assertThat(scribeItem.getId(), is(Long.valueOf(TEST_ID)));
        Assert.assertThat(scribeItem.getItemType(), is(Integer.valueOf(ScribeItem.TYPE_TWEET)));
        assertMediaDetails(scribeItem.getMediaDetails(), TEST_TYPE_VINE_ID);
    }

    @Test
    public void testFromMessage() {
        final ScribeItem item = ScribeItem.Companion.fromMessage(TEST_MESSAGE);

        Assert.assertThat(item.getId(), nullValue());
        Assert.assertThat(item.getItemType(), is(Integer.valueOf(ScribeItem.TYPE_MESSAGE)));
        Assert.assertThat(item.getDescription(), is(TEST_MESSAGE));
    }

    @Test
    public void testBuilder() {
        final ScribeItem item = new ScribeItem.Builder()
                .setId(TEST_ID)
                .setItemType(ScribeItem.TYPE_MESSAGE)
                .setDescription(TEST_MESSAGE)
                .setCardEvent(TEST_CARD_EVENT)
                .setMediaDetails(TEST_MEDIA_DETAILS)
                .build();

        Assert.assertThat(item.getId(), is(Long.valueOf(TEST_ID)));
        Assert.assertThat(item.getItemType(), is(Integer.valueOf(ScribeItem.TYPE_MESSAGE)));
        Assert.assertThat(item.getDescription(), is(TEST_MESSAGE));
        Assert.assertThat(item.getCardEvent(), is(TEST_CARD_EVENT));
        Assert.assertThat(item.getMediaDetails(), is(TEST_MEDIA_DETAILS));
    }

    @Test
    public void testBuilder_empty() {
        final ScribeItem item = new ScribeItem.Builder().build();

        Assert.assertThat(item.getId(), nullValue());
        Assert.assertThat(item.getItemType(), nullValue());
        Assert.assertThat(item.getDescription(), nullValue());
        Assert.assertThat(item.getCardEvent(), nullValue());
        Assert.assertThat(item.getMediaDetails(), nullValue());
    }


    static void assertMediaDetails(ScribeItem.MediaDetails mediaDetails, int type) {
        Assert.assertThat(mediaDetails, notNullValue());
        Assert.assertThat(mediaDetails.getContentId(), is(TEST_ID));
        Assert.assertThat(mediaDetails.getMediaType(), is(type));
        Assert.assertThat(mediaDetails.getPublisherId(), is(TEST_MEDIA_ID));
    }

    private MediaEntity createTestEntity(String type) {
        return new MediaEntity(null, null, null, 0, 0, TEST_MEDIA_ID, null, null, null, null, 0,
                null, type, null, "");
    }
}
