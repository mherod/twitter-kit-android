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

package com.twitter.sdk.android.tweetui

import android.graphics.Color

internal object ColorUtils {

    /**
     * This method calculates a combination of colors using an opacity of the foreground layered
     * over the background color. This allows us to optimize color calculations instead of setting
     * alpha values in the color attributes on the views directly.
     *
     * @param opacity      A value in the range of 0 to 1 that indicates the opacity desired for the
     * overlay color
     * @param overlayColor The foreground color that the opacity will be applied to
     * @param primaryColor The background color that the foreground color is applied to
     * @return             The combined color value
     */
    fun calculateOpacityTransform(opacity: Double, overlayColor: Int,
                                  primaryColor: Int): Int {
        val redPrimary = Color.red(primaryColor)
        val redOverlay = Color.red(overlayColor)
        val greenPrimary = Color.green(primaryColor)
        val greenOverlay = Color.green(overlayColor)
        val bluePrimary = Color.blue(primaryColor)
        val blueOverlay = Color.blue(overlayColor)

        val redCalculated = ((1 - opacity) * redPrimary + opacity * redOverlay).toInt()
        val greenCalculated = ((1 - opacity) * greenPrimary + opacity * greenOverlay).toInt()
        val blueCalculated = ((1 - opacity) * bluePrimary + opacity * blueOverlay).toInt()

        return Color.rgb(redCalculated, greenCalculated, blueCalculated)
    }

    /**
     * This method uses HSL to determine in a human eyesight terms if a color is light or not.
     * See: http://en.wikipedia.org/wiki/HSL_and_HSV. The threshold values are from ITU Rec. 709
     * http://en.wikipedia.org/wiki/Rec._709#Luma_coefficients
     *
     * @param  color A color value
     * @return Whether or not the color is considered light
     */
    fun isLightColor(color: Int): Boolean {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)

        val threshold = 0.21 * r + 0.72 * g + 0.07 * b
        return threshold > 128
    }
}
