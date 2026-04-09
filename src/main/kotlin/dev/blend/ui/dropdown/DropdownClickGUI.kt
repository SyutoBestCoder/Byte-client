package dev.blend.ui.dropdown

import com.syuto.bytes.module.ModuleManager
import com.syuto.bytes.module.api.Category
import com.syuto.bytes.module.impl.render.ClickGUIModule
import dev.blend.ui.dropdown.components.CategoryComponent
import dev.blend.util.animations.BackOutAnimation
import dev.blend.util.render.DrawUtil
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

object DropdownClickGUI: Screen(Component.literal("Dropdown Click GUI")) {

    private val openAnimation = BackOutAnimation()
    val components = mutableListOf<CategoryComponent>()
    var requestsClose = false

    init {
        var x = 20.0
        Category.entries.forEach {
            val component = CategoryComponent(it)
            component.x = x
            component.y = 20.0
            components.add(component)
            x += component.width + 10.0
        }
    }

    override fun init() {
        requestsClose = false
        openAnimation.set(0.5)
        openAnimation.reset()
        components.forEach{
            it.init()
        }
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {

        context?.pose()?.pushMatrix()
        DrawUtil.begin()
//        DrawUtil.save()
//        DrawUtil.translate(
//            (mc.window.scaledWidth / 2.0) * (1.0 - openAnimation.get()),
//            (mc.window.scaledHeight / 2.0) * (1.0 - openAnimation.get())
//        )
//        DrawUtil.scale(openAnimation.get(), openAnimation.get()) // max needed to prevent back out animation from going negative
        components.forEach {
            it.render(mouseX, mouseY)
        }

//        DrawUtil.resetTranslate()
//        DrawUtil.restore()
        DrawUtil.end()
        context?.pose()?.popMatrix()
        openAnimation.animate(
            if (requestsClose) {
                0.0
            } else {
                1.0
            }
        )
        // any / all
        if (requestsClose && components.any { it.openAnimation.finished }) {
            requestsClose = false
            ModuleManager.getModule(ClickGUIModule::class.java)?.setEnabled(false)
        }
    }

    override fun mouseClicked(click: MouseButtonEvent, doubled: Boolean): Boolean {
        components.forEach {
            if (it.isOver(click.x(), click.y())) {
                if (it.click(click.x(), click.y(), click.button())) {
                    return true
                }
            }
        }
        return super.mouseClicked(click, doubled)
    }

    override fun mouseReleased(click: MouseButtonEvent): Boolean {
        components.forEach {
            if (it.release(click.x, click.y, click.button())) {
                return true
            }
        }
        return super.mouseReleased(click)
    }

    override fun keyPressed(input: KeyEvent): Boolean {
        components.forEach {
            if (it.key(input.key(), input.scancode(), input.modifiers())) {
                return true
            }
        }
        return super.keyPressed(input)
    }


    override fun onClose() {
        components.forEach{
            it.close()
        }
//        ModuleManager.getModule(ClickGUIModule::class.java)?.setEnabled(false)
        requestsClose = true
    }

    override fun isPauseScreen(): Boolean {
        return false
    }

    override fun shouldCloseOnEsc(): Boolean {
        return true
    }

    override fun renderBackground(guiGraphics: GuiGraphics, mouseY: Int, j: Int, delta: Float) {
        // do nothing — suppresses the blur/dim overlay
    }

    override fun renderBlurredBackground(guiGraphics: GuiGraphics) {
        // suppress blur shader
    }

}