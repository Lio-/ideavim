package com.maddyhome.idea.vim.ui;

/*
 * IdeaVim - A Vim emulator plugin for IntelliJ Idea
 * Copyright (C) 2003-2006 Rick Maddy
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.maddyhome.idea.vim.KeyHandler;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.Date;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.Document;
import javax.swing.text.Keymap;
import javax.swing.text.TextAction;

/**
 * Provides a custom keymap for the text field. The keymap is the VIM Ex command keymapping
 */
public class ExTextField extends JTextField
{
    /**
     */
    public ExTextField()
    {
        Font font = new Font("Monospaced", Font.PLAIN, 12);
        setFont(font);

        setInputMap(WHEN_FOCUSED, new InputMap());
        Keymap map = addKeymap("ex", null);
        loadKeymap(map, ExKeyBindings.getBindings(), getActions());
        map.setDefaultAction(new ExEditorKit.DefaultKeyTypedAction());
        setKeymap(map);
    }

    void setEditor(Editor editor, DataContext context)
    {
        this.editor = editor;
        this.context = context;
    }

    public Editor getEditor()
    {
        return editor;
    }

    public DataContext getContext()
    {
        return context;
    }

    public Action[] getActions()
    {
        return TextAction.augmentList(super.getActions(), ExEditorKit.getInstance().getActions());
    }

    public void handleKey(KeyStroke stroke)
    {
        logger.debug("stroke="+stroke);
        KeyEvent event = new KeyEvent(this, stroke.getKeyChar() != KeyEvent.CHAR_UNDEFINED ? KeyEvent.KEY_TYPED :
            (stroke.isOnKeyRelease() ? KeyEvent.KEY_RELEASED : KeyEvent.KEY_PRESSED),
            (new Date()).getTime(), stroke.getModifiers(), stroke.getKeyCode(), stroke.getKeyChar());

        super.processKeyEvent(event);
    }

    protected void processKeyEvent(KeyEvent e)
    {
        logger.debug("key="+e);
        boolean keep = false;
        switch (e.getID())
        {
            case KeyEvent.KEY_TYPED:
                keep = true;
                break;
            case KeyEvent.KEY_PRESSED:
                logger.debug("pressed");
                if (e.getKeyChar() == KeyEvent.CHAR_UNDEFINED)
                {
                    logger.debug("keeping");
                    keep = true;
                }
                break;
            case KeyEvent.KEY_RELEASED:
                logger.debug("released");
                if (e.getKeyChar() != KeyEvent.CHAR_UNDEFINED)
                {
                    if (e.getModifiers() != 0)
                    {
                        logger.debug("keeping");
                        keep = true;
                    }
                    /*
                    else
                    {
                        switch(e.getKeyCode())
                        {
                            case KeyEvent.VK_ESCAPE:
                                KeyHandler.getInstance().handleKey(editor, KeyStroke.getKeyStroke((char)27), context);
                                e.consume();
                        }
                    }
                    */
                }
                break;
        }
        if (keep)
        {
            KeyHandler.getInstance().handleKey(editor, KeyStroke.getKeyStrokeForEvent(e), context);
            e.consume();
        }
        else
        {
            super.processKeyEvent(e);
        }

    }

    /**
     * Creates the default implementation of the model
     * to be used at construction if one isn't explicitly
     * given.  An instance of <code>PlainDocument</code> is returned.
     *
     * @return the default model implementation
     */
    protected Document createDefaultModel()
    {
        return new ExDocument();
    }

    // TODO - support block cursor for overwrite mode
    private Editor editor;
    private DataContext context;

    private static final Logger logger = Logger.getInstance(ExTextField.class.getName());
}
