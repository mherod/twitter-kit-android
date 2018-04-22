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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.InputStreamReader;

import static org.hamcrest.Matchers.*;

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

        Assert.assertThat(card.getBindingValues(), notNullValue());
        Assert.assertThat(card.getBindingValues().containsKey("app_id"), is(true));
        Assert.assertThat(card.getBindingValues().get("app_id"), is("co.vine.android"));
        Assert.assertThat(card.getBindingValues().containsKey("app_is_free"), is(true));
        Assert.assertThat(card.getBindingValues().get("app_is_free"), is("true"));
        Assert.assertThat(card.getBindingValues().containsKey("app_name"), is(true));
        Assert.assertThat(card.getBindingValues().get("app_name"), is("Vine - video entertainment"));
        Assert.assertThat(card.getBindingValues().containsKey("app_num_ratings"), is(true));
        Assert.assertThat(card.getBindingValues().get("app_num_ratings"), is("1,080,460"));
        Assert.assertThat(card.getBindingValues().containsKey("app_price_amount"), is(true));
        Assert.assertThat(card.getBindingValues().get("app_price_amount"), is("0.0"));
        Assert.assertThat(card.getBindingValues().containsKey("app_price_currency"), is(true));
        Assert.assertThat(card.getBindingValues().get("app_price_currency"), is("USD"));
        Assert.assertThat(card.getBindingValues().containsKey("app_star_rating"), is(true));
        Assert.assertThat(card.getBindingValues().get("app_star_rating"), is("4.2"));
        Assert.assertThat(card.getBindingValues().containsKey("app_url"), is(true));
        Assert.assertThat(card.getBindingValues().containsKey("app_url_resolved"), is(true));
        Assert.assertThat(card.getBindingValues().containsKey("card_url"), is(true));
        Assert.assertThat(card.getBindingValues().containsKey("description"), is(true));
        Assert.assertThat(card.getBindingValues().get("description"), is("Vine by Krystaalized"));
        Assert.assertThat(card.getBindingValues().containsKey("domain"), is(true));
        Assert.assertThat(card.getBindingValues().get("domain"), is("vine.co"));
        Assert.assertThat(card.getBindingValues().containsKey("player_height"), is(true));
        Assert.assertThat(card.getBindingValues().get("player_height"), is("535"));
        Assert.assertThat(card.getBindingValues().containsKey("player_image"), is(true));
        final ImageValue imageValue = card.getBindingValues().get("player_image");
        Assert.assertThat(imageValue, notNullValue());
        Assert.assertThat(imageValue.height, is(480));
        Assert.assertThat(imageValue.width, is(480));
        Assert.assertThat(imageValue.url, is("https://o.twimg.com/qwhjddd"));
        Assert.assertThat(card.getBindingValues().containsKey("player_stream_content_type"), is(true));
        Assert.assertThat(card.getBindingValues().containsKey("player_stream_url"), is(true));
        Assert.assertThat(card.getBindingValues().containsKey("player_url"), is(true));
        Assert.assertThat(card.getBindingValues().containsKey("player_width"), is(true));
        Assert.assertThat(card.getBindingValues().get("player_width"), is("535"));
        Assert.assertThat(card.getBindingValues().containsKey("site"), is(true));
        Assert.assertThat(card.getBindingValues().get("site"), notNullValue());
        Assert.assertThat(((UserValue) card.getBindingValues().get("site")).getIdStr(), is("586671909"));
        Assert.assertThat(card.getBindingValues().containsKey("title"), is(true));
        Assert.assertThat(card.getBindingValues().containsKey("vanity_url"), is(true));
        Assert.assertThat(card.getBindingValues().get("vanity_url"), is("vine.co"));
        Assert.assertThat(card.getBindingValues().containsKey("foo"), is(false));
        Assert.assertThat(card.getBindingValues().containsKey(null), is(false));
    }

    @Test
    public void testDeserialize_withEmptyBindingValues() {
        final BindingValues bindingValues = gson.fromJson("{}", BindingValues.class);

        Assert.assertThat(bindingValues, notNullValue());
    }

    @Test
    public void testDeserialize_withNoType() {
        final String testString = "{\"app_id\": {}}";
        final BindingValues bindingValues = gson.fromJson(testString, BindingValues.class);

        Assert.assertThat(bindingValues, notNullValue());
        Assert.assertThat(bindingValues.containsKey("app_id"), is(true));
        Assert.assertThat(bindingValues.get("app_id"), nullValue());
    }

    @Test
    public void testDeserialize_withUnsupportedType() {
        final String testString = "{\"app_id\": {\"type\": \"FOOBAR\"}}";
        final BindingValues bindingValues = gson.fromJson(testString, BindingValues.class);

        Assert.assertThat(bindingValues, notNullValue());
        Assert.assertThat(bindingValues.containsKey("app_id"), is(true));
        Assert.assertThat(bindingValues.get("app_id"), nullValue());
    }

    @Test
    public void testDeserialize_withNonPrimitiveType() {
        final String testString = "{\"app_id\": {\"type\": {}}}";
        final BindingValues bindingValues = gson.fromJson(testString, BindingValues.class);

        Assert.assertThat(bindingValues, notNullValue());
        Assert.assertThat(bindingValues.containsKey("app_id"), is(true));
        Assert.assertThat(bindingValues.get("app_id"), nullValue());
    }

    @Test
    public void testDeserialize_withNoValue() {
        final String testString = "{\"app_id\": {\"type\": \"STRING\"}}";
        final BindingValues bindingValues = gson.fromJson(testString, BindingValues.class);

        Assert.assertThat(bindingValues, notNullValue());
        Assert.assertThat(bindingValues.containsKey("app_id"), is(true));
        Assert.assertThat(bindingValues.get("app_id"), nullValue());
    }
}
