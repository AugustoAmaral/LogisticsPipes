/*
 * Copyright (c) 2020  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2020  RS485
 *
 * This MIT license was reworded to only match this file. If you use the regular
 * MIT license in your project, replace this copyright notice (this line and any
 * lines below and NOT the copyright line above) with the lines from the original
 * MIT license located here: http://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this file and associated documentation files (the "Source Code"), to deal in
 * the Source Code without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Source Code, and to permit persons to whom the Source Code is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Source Code, which also can be
 * distributed under the MIT.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package network.rs485.logisticspipes.gui.guidebook

import logisticspipes.utils.MinecraftColor
import network.rs485.logisticspipes.gui.LPGuiDrawer
import network.rs485.logisticspipes.util.math.Rectangle

/**
 * This draws a line with a given thickness that will span the entire width of the page, minus padding.
 */

private const val horizontalPadding: Int = 3

class DrawableHorizontalLine(private val thickness: Int, private val padding: Int = 3, val color: Int = MinecraftColor.WHITE.colorCode) : DrawableParagraph() {
    override var relativeBody: Rectangle = Rectangle()
    override var parent: Drawable? = null

    override fun setPos(x: Int, y: Int): Int {
        relativeBody.setPos(x, y + padding)
        relativeBody.setSize(parent!!.width - 2 * horizontalPadding, padding + thickness)
        return super.setPos(x, y)
    }

    override fun getHovered(mouseX: Float, mouseY: Float): Drawable? = null

    override fun draw(mouseX: Float, mouseY: Float, delta: Float, visibleArea: Rectangle) {
        super.draw(mouseX, mouseY, delta, visibleArea)
        LPGuiDrawer.drawLine(absoluteBody.topLeft, absoluteBody.topRight, color, thickness.toFloat())
    }
}    