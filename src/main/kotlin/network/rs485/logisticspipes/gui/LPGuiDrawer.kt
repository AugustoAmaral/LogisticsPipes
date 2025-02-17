/*
 * Copyright (c) 2021  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2021  RS485
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

package network.rs485.logisticspipes.gui

import logisticspipes.LPConstants
import logisticspipes.utils.MinecraftColor
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.inventory.Container
import net.minecraft.util.ResourceLocation
import network.rs485.logisticspipes.gui.font.LPFontRenderer
import network.rs485.logisticspipes.gui.guidebook.Screen
import network.rs485.logisticspipes.util.math.BorderedRectangle
import network.rs485.logisticspipes.util.math.Rectangle
import network.rs485.markdown.defaultDrawableState
import org.lwjgl.opengl.GL11
import java.lang.Float.min

/**
 * Drawing methods to help with Guis
 */
object LPGuiDrawer {

    const val TEXT_DARK: Int = 0xff404040.toInt()
    const val TEXT_WHITE: Int = 0xffffffff.toInt()
    const val TEXT_HOVERED: Int = 0xffffffa0.toInt()
    const val BACKGROUND_LIGHT: Int = 0xffc6c6c6.toInt()
    const val BACKGROUND_DARK: Int = 0xff8b8b8b.toInt()

    private val guiAtlas = Texture(ResourceLocation(LPConstants.LP_MOD_ID, "textures/gui/gui.png"), 256)
    private val guiNormalPatternTexture = Texture(ResourceLocation(LPConstants.LP_MOD_ID, "textures/gui/normal.png"), 64)
    private val guiLightPattern = Texture(ResourceLocation(LPConstants.LP_MOD_ID, "textures/gui/light.png"), 64)
    private val guiHoveredPatternTexture = Texture(ResourceLocation(LPConstants.LP_MOD_ID, "textures/gui/blue.png"), 64)
    private val guiDarkPatternTexture = Texture(ResourceLocation(LPConstants.LP_MOD_ID, "textures/gui/dark.png"), 64)

    private var currentTexture: Texture = guiAtlas

    private const val border: Int = 4

    // TODO update constructor params
    private val guiBackgroundTexture = Rectangle(0, 96, 16, 16)
    private val guiBlankTexture = Rectangle(2, 2, 2, 2)
    private val slotNormalTexture = Rectangle(0, 112, 18, 18)
    private val slotDiskTexture = Rectangle(18, 112, 36, 18)
    private val slotProgrammerTexture = Rectangle(36, 112, 18, 18)
    private val slotSmallTexture = Rectangle(54, 112, 8, 8)
    private val slotBigTexture = Rectangle(0, 130, 26, 26)

    private val guiGuidebookFrame = Rectangle(0, 0, 64, 64)
    private val guiGuidebookSlider = Rectangle(96, 64, 16, 16)

    private val buttonBorderTextureLight = Rectangle(0, 64, 8, 8)
    private val buttonBorderTextureNormal = Rectangle(8, 64, 8, 8)
    private val buttonBorderTextureDark = Rectangle(0, 72, 8, 8)
    private val buttonBorderTextureHovered = Rectangle(8, 72, 8, 8)

    private val tessellator: Tessellator get() = Tessellator.getInstance()
    private val buffer: BufferBuilder get() = tessellator.buffer
    private var isDrawing: Boolean = false
    private val textureManager = Minecraft.getMinecraft().renderEngine

    val lpFontRenderer: LPFontRenderer by lazy {
        LPFontRenderer.get("ter-u12n")
    }
    val mcFontRenderer: FontRenderer by lazy {
        Minecraft.getMinecraft().fontRenderer
    }

    // Container specific draw code

    fun drawGuiBackground(guiArea: Rectangle, container: Container) {
        setTexture(guiAtlas)
        start()
        putGuiBackgroundBase(guiArea)
        putContainerSlots(guiArea, container)
        finish()
    }

    // Container specific buffer code

    private fun putGuiBackgroundBase(guiArea: Rectangle) {
        val borderedGuiQuads = BorderedRectangle(guiArea, border).quads
        val borderedTexQuads = BorderedRectangle(guiBackgroundTexture, border).quads
        for ((i, quad) in borderedGuiQuads.withIndex()) {
            putTexturedQuad(quad, borderedTexQuads[i], -1)
        }
    }

    private fun putContainerSlots(guiArea: Rectangle, container: Container) {
        for (slot in container.inventorySlots) {
            putNormalSlot(guiArea.roundedLeft + slot.xPos, guiArea.roundedTop + slot.yPos)
        }
    }

    private fun putNormalSlot(x: Int, y: Int) {
        val normalSlotSize = 18
        putTexturedQuad(
            Rectangle(x, y, normalSlotSize, normalSlotSize).translate(-1),
            slotNormalTexture,
            MinecraftColor.WHITE.colorCode
        )
    }

    // Button specific draw code

    fun drawGuiTexturedRect(rect: Rectangle, text: Rectangle, blend: Boolean, color: Int) {
        if (blend) {
            GlStateManager.enableBlend()
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        } else {
            GlStateManager.disableBlend()
        }
        setTexture(guiAtlas)
        start()
        putTexturedQuad(rect, text, color)
        finish()
        if (blend) GlStateManager.disableBlend()
    }

    fun drawBorderedTile(rect: Rectangle, hovered: Boolean, enabled: Boolean, light: Boolean, thickerBottomBorder: Boolean) {
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)

        val border = 2

        val bottomBorder = if (thickerBottomBorder) border + 1 else border

        val (buttonBackgroundTexture, buttonBorderTexture) = when {
            !enabled -> {
                guiDarkPatternTexture to buttonBorderTextureDark
            }

            hovered -> {
                guiHoveredPatternTexture to buttonBorderTextureHovered
            }

            light -> {
                guiLightPattern to buttonBorderTextureLight
            }

            else -> {
                guiNormalPatternTexture to buttonBorderTextureNormal
            }
        }

        setTexture(buttonBackgroundTexture)
        start()
        putScaledTexturedQuad(rect, 0f to 0f, -1)
        finish()

        setTexture(guiAtlas)
        start()
        val borderedGuiQuads = BorderedRectangle(rect, border, border, bottomBorder, border).borderQuads
        val borderedTexQuads = BorderedRectangle(buttonBorderTexture, border, border, border, border).borderQuads
        for ((i, quad) in borderedGuiQuads.withIndex()) {
            putTexturedQuad(quad, borderedTexQuads[i], -1)
        }
        finish()

        GlStateManager.disableBlend()
    }

    fun drawGuideBookFrame(rect: Rectangle, slider: Rectangle) {
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)

        val borderedGui = BorderedRectangle(rect, 24)
        val borderedGuiTexQuads = BorderedRectangle(guiGuidebookFrame, 24).borderQuads
        val borderedSlider = BorderedRectangle(slider, 1, 0, 1, 0).quads.filter { it.width > 0.5 && it.height > 0.5 }
        val borderedSliderTexQuads = BorderedRectangle(guiGuidebookSlider, 1, 0, 1, 0).quads.filter { it.width > 0.5 && it.height > 0.5 }

        setTexture(guiAtlas)
        start()
        for ((i, quad) in (borderedGui.borderQuads + borderedSlider).withIndex()) {
            putTexturedQuad(quad, (borderedGuiTexQuads + borderedSliderTexQuads)[i], -1)
        }
        finish()

        GlStateManager.disableBlend()
    }

    /**
     * Draws a stylized tooltip at the specified position, and clamps to the edges (to be tested).
     * @param text  text to be displayed in the tooltip;
     * @param x     x position at the center of the tooltip, in case no clamping needs to be done;
     * @param y     y position of the top of the tooltip;
     * @param z     z position of the tooltip.
     */
    fun drawTextTooltip(
        text: List<String>,
        x: Int,
        y: Int,
        z: Float,
        horizontalAlign: HorizontalAlignment,
        verticalAlign: VerticalAlignment,
    ) {
        if (text.isEmpty() || text.all { it.isBlank() }) return

        val border = 4
        val horizontalPadding = 2
        val verticalPadding = 1

        // Calculate tooltip size
        val outerArea = Rectangle(
            (text.maxOfOrNull { lpFontRenderer.getStringWidth(it) } ?: 0) + 2 * (border + horizontalPadding),
            (text.size * lpFontRenderer.getFontHeight()) + 2 * (verticalPadding + border)
        )

        // Align tooltip accordingly
        outerArea.setPos(
            when (horizontalAlign) {
                HorizontalAlignment.CENTER -> x - outerArea.roundedWidth / 2
                HorizontalAlignment.LEFT -> x
                HorizontalAlignment.RIGHT -> x - outerArea.roundedWidth
            },
            when (verticalAlign) {
                VerticalAlignment.CENTER -> y - outerArea.roundedHeight / 2
                VerticalAlignment.TOP -> y
                VerticalAlignment.BOTTOM -> y - outerArea.roundedHeight
            }
        )

        // Constrain position to available screen space
        if (outerArea.x0 < 0) outerArea.translate(translateX = -outerArea.x0)
        if (outerArea.x1 > Screen.absoluteBody.roundedWidth) outerArea.translate(translateX = Screen.absoluteBody.roundedWidth - outerArea.x1)
        if (outerArea.y0 < 0) outerArea.translate(translateY = -outerArea.y0)
        if (outerArea.y1 > Screen.absoluteBody.roundedHeight) outerArea.translate(translateY = Screen.absoluteBody.roundedHeight - outerArea.y1)


        // Define required quads and texture quads.
        val borderedTooltipQuads = BorderedRectangle(
            outerArea,
            border
        ).quads
        val borderedTexQuads = BorderedRectangle(
            Rectangle(112, 32, 16, 16),
            border
        ).quads

        GlStateManager.translate(0.0f, 0.0f, z)
        GlStateManager.enableAlpha()

        // Draw background
        setTexture(guiAtlas)
        start()
        for ((i, quad) in borderedTooltipQuads.withIndex()) {
            putTexturedQuad(quad, borderedTexQuads[i], -1)
        }
        finish()

        val textX = outerArea.roundedX + border + horizontalPadding
        val textY = outerArea.roundedY + border + verticalPadding

        // Draw text
        text.fold(textY) { yOffset, line ->
            lpFontRenderer.drawString(
                line,
                textX.toFloat(),
                yOffset.toFloat(),
                defaultDrawableState.color,
                defaultDrawableState.format,
                1.0f
            )
        }
        GlStateManager.translate(0.0f, 0.0f, -z)
    }

    fun drawGuideBookBackground(rect: Rectangle) {
        val borderedGui = BorderedRectangle(rect, 24)
        setTexture(guiDarkPatternTexture)
        start()
        putScaledTexturedQuad(borderedGui.inner.translate(-8).grow(16), 0f to 0f, -1)
        finish()
    }

    fun drawSliderButton(body: Rectangle, texture: Rectangle) {
        setTexture(guiAtlas)

        val quads = BorderedRectangle(body.copy().apply {
            grow(4, 0)
            translate(-2, 0)
        }, 2)
        val textures = BorderedRectangle(texture.copy().apply {
            grow(4, 0)
            translate(-2, 0)
        }, 2)

        start()
        putTexturedQuad(quads.top, textures.top, -1)
        putRepeatingTexturedQuad(quads.inner, textures.inner, -1)
        putTexturedQuad(quads.bottom, textures.bottom, -1)
        finish()
    }

    // Text specific draw code

    fun drawCenteredString(text: String, x: Int, y: Int, color: Int, shadow: Boolean) {
        val xOffset = mcFontRenderer.getStringWidth(text) / 2
        mcFontRenderer.drawString(text, x.toFloat() - xOffset, y.toFloat(), color, shadow)
    }

    // Untextured draw code


    fun drawInteractionIndicator(mouseX: Float, mouseY: Float) {
        GlStateManager.disableTexture2D()
        GlStateManager.translate(0.0f, 0.0f, 100f)
        start(DefaultVertexFormats.POSITION_COLOR)
        putQuad(
            Rectangle(mouseX + 4f to mouseY - 5f, mouseX + 5f to mouseY - 2f),
            MinecraftColor.WHITE.colorCode
        )
        putQuad(
            Rectangle(mouseX + 3f to mouseY - 4f, mouseX + 6f to mouseY - 3f),
            MinecraftColor.WHITE.colorCode
        )
        finish()
        GlStateManager.enableTexture2D()
        GlStateManager.translate(0.0f, 0.0f, -100f)
    }

    fun drawRect(area: Rectangle, color: Int) {
        GlStateManager.disableTexture2D()
        GlStateManager.disableAlpha()
        start(DefaultVertexFormats.POSITION_COLOR)
        putQuad(area, color)
        finish()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
    }

    fun drawHorizontalGradientRect(area: Rectangle, colorLeft: Int, colorRight: Int) {
        drawGradientQuad(area, colorRight, colorLeft, colorLeft, colorRight)
    }

    fun drawVerticalGradientRect(area: Rectangle, colorTop: Int, colorBottom: Int) {
        drawGradientQuad(area, colorTop, colorTop, colorBottom, colorBottom)
    }

    fun drawLine(start: Pair<Float, Float>, finish: Pair<Float, Float>, color: Int, thickness: Float) {
        GlStateManager.disableTexture2D()
        GlStateManager.disableAlpha()
        start(DefaultVertexFormats.POSITION_COLOR)
        putLine(start, finish, color, thickness)
        finish()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
    }

    fun drawOutlineRect(rect: Rectangle, color: Int) {
        GlStateManager.disableTexture2D()
        GlStateManager.disableAlpha()
        start(DefaultVertexFormats.POSITION_COLOR)
        putOutlineQuad(rect, color)
        finish()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
    }

    fun drawGradientQuad(area: Rectangle, colorTopRight: Int, colorTopLeft: Int, colorBottomLeft: Int, colorBottomRight: Int) {
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        )
        GlStateManager.shadeModel(GL11.GL_SMOOTH)
        start(DefaultVertexFormats.POSITION_COLOR)
        buffer.pos(area.topRight).rgba(colorTopRight).endVertex()
        buffer.pos(area.topLeft).rgba(colorTopLeft).endVertex()
        buffer.pos(area.bottomLeft).rgba(colorBottomRight).endVertex()
        buffer.pos(area.bottomRight).rgba(colorBottomLeft).endVertex()
        finish()
        GlStateManager.shadeModel(GL11.GL_FLAT)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
    }

    // Buffer specific code

    private fun putRepeatingTexturedQuad(rect: Rectangle, texture: Rectangle, color: Int) {
        val tile = Rectangle().apply {
            setPos(rect.left, rect.top)
            setSize(
                min(rect.width, texture.width),
                min(rect.height, texture.height)
            )
        }
        for (x in 0 until rect.roundedWidth step tile.roundedWidth) {
            for (y in 0 until rect.roundedHeight step tile.roundedHeight) {
                putScaledTexturedQuad(
                    rect = if (rect.contains(tile)) {
                        tile
                    } else {
                        rect.overlap(tile)
                    },
                    texture = texture.topLeft,
                    color = color
                )
                tile.translate(0f, tile.height)
            }
            tile.setPos(newX = tile.left, newY = rect.top)
            tile.translate(tile.width, 0f)
        }
    }

    private fun putScaledTexturedQuad(rect: Rectangle, texture: Pair<Float, Float>, color: Int) {
        putTexturedQuad(rect, Rectangle(texture.first, texture.second, rect.width, rect.height), color)
    }

    private fun putTexturedQuad(rect: Rectangle, texture: Rectangle, color: Int) {
        if (buffer.vertexFormat == DefaultVertexFormats.POSITION_TEX_COLOR) {
            val scaledUV = texture.scaled(currentTexture.factor)
            buffer.pos(rect.topRight).tex(scaledUV.topRight).rgba(color).endVertex()
            buffer.pos(rect.topLeft).tex(scaledUV.topLeft).rgba(color).endVertex()
            buffer.pos(rect.bottomLeft).tex(scaledUV.bottomLeft).rgba(color).endVertex()
            buffer.pos(rect.bottomRight).tex(scaledUV.bottomRight).rgba(color).endVertex()
        }
    }

    private fun putLine(from: Pair<Float, Float>, to: Pair<Float, Float>, color: Int, thickness: Float = 1.0f, vertical: Boolean = false) {
        if (buffer.vertexFormat == DefaultVertexFormats.POSITION_COLOR) {
            if (!vertical) {
                putQuad(Rectangle(from, to.first to to.second + thickness), color)
            } else {
                putQuad(Rectangle(from, to.first + thickness to to.second), color)
            }
        }
    }

    private fun putOutlineQuad(rect: Rectangle, color: Int, thickness: Float = 1.0f) {
        rect.run {
            putLine(rect.topLeft, rect.topRight, color, thickness)
            putLine(rect.bottomLeft, rect.bottomRight, color, thickness)
            putLine(rect.topLeft, rect.bottomLeft, color, thickness, vertical = true)
            putLine(rect.topRight, rect.bottomRight, color, thickness, vertical = true)
        }
    }

    private fun putQuad(rect: Rectangle, color: Int) {
        if (buffer.vertexFormat == DefaultVertexFormats.POSITION_TEX_COLOR) {
            putTexturedQuad(rect, guiBlankTexture, color)
        } else if (buffer.vertexFormat == DefaultVertexFormats.POSITION_COLOR) {
            buffer.pos(rect.topRight).rgba(color).endVertex()
            buffer.pos(rect.topLeft).rgba(color).endVertex()
            buffer.pos(rect.bottomLeft).rgba(color).endVertex()
            buffer.pos(rect.bottomRight).rgba(color).endVertex()
        }
    }

    /**
     * Starts the BufferBuilder if it was not started already.
     */
    private fun start(vertexFormats: VertexFormat = DefaultVertexFormats.POSITION_TEX_COLOR) {
        if (!isDrawing) {
            buffer.begin(GL11.GL_QUADS, vertexFormats)
            isDrawing = true
        }
    }

    /**
     * Draws the buffered quads if the buffer is currently open.
     */
    private fun finish() {
        if (isDrawing) {
            tessellator.draw()
            isDrawing = false
        }
    }

    /**
     * Binds another texture keeping track of it's size
     * @param texture texture to be bound.
     */
    private fun setTexture(texture: Texture) {
        currentTexture = texture
        textureManager.bindTexture(currentTexture.resource)
    }

    /**
     * Cleanly splits the integer into the 4 rgba components and applies those to the BufferBuilder
     * Uses BufferBuilder.color(I,I,I,I) because BufferBuilder.color(F,F,F,F) will just multiply the floats back into integers.
     * @param color rgba color to be used
     * @return the buffer itself.
     */
    private fun BufferBuilder.rgba(color: Int): BufferBuilder {
        val a = (color shr 24 and 255)
        val r = (color shr 16 and 255)
        val g = (color shr 8 and 255)
        val b = (color and 255)
        return this.color(r, g, b, a)
    }

    /**
     * Takes in a Float pair to insert texture coordinates onto the draw buffer
     * Uses BufferBuilder.tex(D,D)
     * @param point coordinate on the texture (scaled from 0-1)
     * @return the buffer itself.
     */
    private fun BufferBuilder.tex(point: Pair<Float, Float>): BufferBuilder = tex(point.first.toDouble(), point.second.toDouble())

    /**
     * Takes in a Float pair to insert screen coordinates onto the draw buffer
     * Uses BufferBuilder.pos(D,D)
     * @param point coordinate to be inserted in the buffer.
     * @return the buffer itself.
     */
    private fun BufferBuilder.pos(point: Pair<Float, Float>): BufferBuilder = pos(point.first.toDouble(), point.second.toDouble(), 0.0)
}

private class Texture(val resource: ResourceLocation, size: Int) {
    val factor: Float = 1.0f / size
}
