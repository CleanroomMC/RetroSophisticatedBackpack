package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.drawable.IDrawable
import com.cleanroommc.modularui.api.widget.IFocusedWidget
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.widget.Widget
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiTextField
import org.lwjgl.input.Keyboard

abstract class VanillaTextFieldWidget<W : VanillaTextFieldWidget<W>>(
    textY: Int,
    textWidth: Int,
    textHeight: Int
) : Widget<W>(), Interactable, IFocusedWidget {
    companion object {
        private var focusedTextField: VanillaTextFieldWidget<*>? = null

        fun handleCommittedCharacter(character: Char): Boolean {
            val focused = focusedTextField ?: return false
            if (!focused.isFocused()) {
                focusedTextField = null
                return false
            }
            return focused.typeCharacter(character, 0)
        }
    }

    protected val textField = GuiTextField(0, Minecraft.getMinecraft().fontRenderer, 0, textY, textWidth, textHeight)

    init {
        background(IDrawable.EMPTY)
        textField.setEnableBackgroundDrawing(false)
        textField.setCanLoseFocus(false)
    }

    protected var text: String
        get() = textField.text
        set(value) {
            if (textField.text != value) {
                textField.setText(value)
            }
        }

    protected open fun textFieldX(): Int = 0

    protected open fun textFieldY(): Int = 0

    protected open fun textFieldWidth(): Int = area.width

    protected open fun textFieldHeight(): Int = area.height

    protected open fun mouseXForTextField(): Int = context.mouseX

    protected open fun mouseYForTextField(): Int = context.mouseY

    protected open fun onTextChanged(text: String) {}

    protected open fun onEditingFinished() {}

    protected open fun onFocusChanged(focused: Boolean) {}

    protected fun setTextColor(color: Int) {
        textField.setTextColor(color)
    }

    protected fun setDisabledTextColor(color: Int) {
        textField.setDisabledTextColour(color)
    }

    protected fun setMaxStringLength(length: Int) {
        textField.setMaxStringLength(length)
    }

    protected fun drawTextField() {
        updateTextFieldBounds()
        textField.drawTextBox()
    }

    protected fun updateTextFieldBounds() {
        textField.x = textFieldX()
        textField.y = textFieldY()
        textField.width = textFieldWidth().coerceAtLeast(1)
        textField.height = textFieldHeight().coerceAtLeast(1)
    }

    override fun onMousePressed(mouseButton: Int): Interactable.Result {
        if (mouseButton != 0) {
            return Interactable.Result.STOP
        }
        updateTextFieldBounds()
        textField.setFocused(true)
        textField.mouseClicked(mouseXForTextField(), mouseYForTextField(), mouseButton)
        return Interactable.Result.SUCCESS
    }

    override fun onKeyPressed(character: Char, keyCode: Int): Interactable.Result {
        if (!isFocused()) {
            return Interactable.Result.IGNORE
        }
        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            context.removeFocus()
            return Interactable.Result.SUCCESS
        }

        return if (typeCharacter(character, keyCode) || keyCode != Keyboard.KEY_TAB) {
            Interactable.Result.SUCCESS
        } else {
            Interactable.Result.IGNORE
        }
    }

    private fun typeCharacter(character: Char, keyCode: Int): Boolean {
        val oldText = textField.text
        val handled = textField.textboxKeyTyped(character, keyCode)
        if (textField.text != oldText) {
            onTextChanged(textField.text)
        }
        return handled
    }

    override fun onUpdate() {
        super.onUpdate()
        textField.updateCursorCounter()
    }

    override fun isFocused(): Boolean = context.isFocused(this)

    override fun onFocus(context: ModularGuiContext) {
        focusedTextField = this
        textField.setFocused(true)
        onFocusChanged(true)
        Keyboard.enableRepeatEvents(true)
    }

    override fun onRemoveFocus(context: ModularGuiContext) {
        if (focusedTextField === this) {
            focusedTextField = null
        }
        textField.setFocused(false)
        onFocusChanged(false)
        Keyboard.enableRepeatEvents(false)
        onEditingFinished()
    }
}
