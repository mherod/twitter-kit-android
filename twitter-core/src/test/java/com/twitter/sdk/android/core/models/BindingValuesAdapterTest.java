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

package com.twitter.sdk.android.core.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.twitter.sdk.android.core.TestResources;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BindingValuesAdapterTest {
    Gson gson;

    @Rule
    public final TestResources testResources = new TestResources();

    @Before
    public void setUp() {
        gson = new GsonBuilder()
                .registerTypeAdapter(BindingValues.class, new BindingValuesAdapter())
                .create();
    }

    @Test
    public void testDeserialize_withValidBindingValues() {
        final JsonReader reader = new JsonReader(new InputStreamReader(testResources
                .getAsStream("model_card.json")));
        final Card card = gson.fromJson(reader, Card.class);

        assertNotNull(card.getBindingValues());
        assertTrue(card.getBindingValues().containsKey("app_id"));
        assertEquals("co.vine.android", card.getBindingValues().get("app_id"));
        assertTrue(card.getBindingValues().containsKey("app_is_free"));
        assertEquals("true", card.getBindingValues().get("app_is_free"));
        assertTrue(card.getBindingValues().containsKey("app_name"));
        assertEquals("Vine - video entertainment", card.getBindingValues().get("app_name"));
        assertTrue(card.getBindingValues().containsKey("app_num_ratings"));
        assertEquals("1,080,460", card.getBindingValues().get("app_num_ratings"));
        assertTrue(card.getBindingValues().containsKey("app_price_amount"));
        assertEquals("0.0", card.getBindingValues().get("app_price_amount"));
        assertTrue(card.getBindingValues().containsKey("app_price_currency"));
        assertEquals("USD", card.getBindingValues().get("app_price_currency"));
        assertTrue(card.getBindingValues().containsKey("app_star_rating"));
        assertEquals("4.2", card.getBindingValues().get("app_star_rating"));
        assertTrue(card.getBindingValues().containsKey("app_url"));
        assertTrue(card.getBindingValues().containsKey("app_url_resolved"));
        assertTrue(card.getBindingValues().containsKey("card_url"));
        assertTrue(card.getBindingValues().containsKey("description"));
        assertEquals("Vine by Krystaalized", card.getBindingValues().get("description"));
        assertTrue(card.getBindingValues().containsKey("domain"));
        assertEquals("vine.co", card.getBindingValues().get("domain"));
        assertTrue(card.getBindingValues().containsKey("player_height"));
        assertEquals("535", card.getBindingValues().get("player_height"));
        assertTrue(card.getBindingValues().containsKey("player_image"));
        final ImageValue imageValue = card.getBindingValues().get("player_image");
        assertNotNull(imageValue);
        assertEquals(480, imageValue.height);
        assertEquals(480, imageValue.width);
        assertEquals("https://o.twimg.com/qwhjddd", imageValue.url);
        assertTrue(card.getBindingValues().containsKey("player_stream_content_type"));
        assertTrue(card.getBindingValues().containsKey("player_stream_url"));
        assertTrue(card.getBindingValues().containsKey("player_url"));
        assertTrue(card.getBindingValues().containsKey("player_width"));
        assertEquals("535", card.getBindingValues().get("player_width"));
        assertTrue(card.getBindingValues().containsKey("site"));
        assertNotNull(card.getBindingValues().get("site"));
        assertEquals("586671909", ((UserValue) card.getBindingValues().get("site")).getIdStr());
        assertTrue(card.getBindingValues().containsKey("title"));
        assertTrue(card.getBindingValues().containsKey("vanity_url"));
        assertEquals("vine.co", card.getBindingValues().get("vanity_url"));
        assertFalse(card.getBindingValues().containsKey("foo"));
        assertFalse(card.getBindingValues().containsKey(null));
    }

    @Test
    public void testDeserialize_withEmptyBindingValues() {
        final BindingValues bindingValues = gson.fromJson("{}", BindingValues.class);

        assertNotNull(bindingValues);
    }

    @Test
    public void testDeserialize_withNoType() {
        final String testString = "{\"app_id\": {}}";
        final BindingValues bindingValues = gson.fromJson(testString, BindingValues.class);

        assertNotNull(bindingValues);
        assertTrue(bindingValues.containsKey("app_id"));
        assertNull(bindingValues.get("app_id"));
    }

    @Test
    public void testDeserialize_withUnsupportedType() {
        final String testString = "{\"app_id\": {\"type\": \"FOOBAR\"}}";
        final BindingValues bindingValues = gson.fromJson(testString, BindingValues.class);

        assertNotNull(bindingValues);
        assertTrue(bindingValues.containsKey("app_id"));
        assertNull(bindingValues.get("app_id"));
    }

    @Test
    public void testDeserialize_withNonPrimitiveType() {
        final String testString = "{\"app_id\": {\"type\": {}}}";
        final BindingValues bindingValues = gson.fromJson(testString, BindingValues.class);

        assertNotNull(bindingValues);
        assertTrue(bindingValues.containsKey("app_id"));
        assertNull(bindingValues.get("app_id"));
    }

    @Test
    public void testDeserialize_withNoValue() {
        final String testString = "{\"app_id\": {\"type\": \"STRING\"}}";
        final BindingValues bindingValues = gson.fromJson(testString, BindingValues.class);

        assertNotNull(bindingValues);
        assertTrue(bindingValues.containsKey("app_id"));
        assertNull(bindingValues.get("app_id"));
    }
}
