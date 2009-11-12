/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.xpn.xwiki.wysiwyg.client.editor;

import java.util.HashMap;
import java.util.Map;

import org.xwiki.gwt.user.client.Console;
import org.xwiki.gwt.user.client.ui.rta.Reloader;
import org.xwiki.gwt.user.client.ui.rta.cmd.Command;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TabPanel;
import com.xpn.xwiki.wysiwyg.client.converter.HTMLConverter;
import com.xpn.xwiki.wysiwyg.client.converter.HTMLConverterAsync;

/**
 * {@link WysiwygEditor} tab-switch handler.
 * 
 * @version $Id$
 */
public class WysiwygEditorListener implements SelectionHandler<Integer>, BeforeSelectionHandler<Integer>
{
    /**
     * Disables the rich text editor, enables the source editor and updates the source text.
     */
    private class SwitchToSourceCallback implements AsyncCallback<String>
    {
        /**
         * {@inheritDoc}
         * 
         * @see AsyncCallback#onSuccess(Object)
         */
        public void onSuccess(String result)
        {
            editor.setLoading(false);
            // Disable the rich text area to avoid submitting its content.
            editor.getRichTextEditor().getTextArea().getCommandManager().execute(ENABLE, false);
            // Enable the plain text area.
            editor.getPlainTextEditor().getTextArea().setEnabled(true);
            editor.getPlainTextEditor().getTextArea().setText(result);
            // Try giving focus to the plain text area (this might not work if the browser window is not focused).
            editor.getPlainTextEditor().getTextArea().setFocus(true);
            // Place the caret at the start.
            editor.getPlainTextEditor().getTextArea().setCursorPos(0);
            // Store the initial value of the plain text area in case it is submitted without gaining focus.
            editor.getPlainTextEditor().submit();
        }

        /**
         * {@inheritDoc}
         * 
         * @see AsyncCallback#onFailure(Throwable)
         */
        public void onFailure(Throwable caught)
        {
            Console.getInstance().error(caught);
            editor.setLoading(false);
        }
    }

    /**
     * The command used to store the value of the rich text area before submitting the including form.
     */
    private static final Command SUBMIT = new Command("submit");

    /**
     * The command used to enable or disable the rich text area.
     */
    private static final Command ENABLE = new Command("enable");

    /**
     * The command used to notify all the rich text area listeners when its content has been reset.
     */
    private static final Command RESET = new Command("reset");

    /**
     * The underlying WYSIWYG editor instance.
     */
    private final WysiwygEditor editor;

    /**
     * The component used to convert the HTML generated by the WYSIWYG editor to source syntax.
     */
    private final HTMLConverterAsync converter = GWT.create(HTMLConverter.class);

    /**
     * Creates a new tab-switch handler for the given WYSIWYG editor.
     * 
     * @param editor the {@link WysiwygEditor} instance
     */
    WysiwygEditorListener(WysiwygEditor editor)
    {
        this.editor = editor;
    }

    /**
     * {@inheritDoc}
     * 
     * @see BeforeSelectionHandler#onBeforeSelection(BeforeSelectionEvent)
     */
    public void onBeforeSelection(BeforeSelectionEvent<Integer> event)
    {
        TabPanel tabPanel = (TabPanel) event.getSource();
        if (tabPanel.getTabBar().getSelectedTab() == event.getItem()) {
            event.cancel();
        } else if (event.getItem() == WysiwygEditor.SOURCE_TAB_INDEX
            && editor.getRichTextEditor().getTextArea().isEnabled()) {
            // Notify the plug-ins that the content of the rich text area is about to be submitted.
            // We have to do this before the tabs are actually switched because plug-ins can't access the computed style
            // of the rich text area when it is hidden.
            editor.getRichTextEditor().getTextArea().getCommandManager().execute(SUBMIT);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see SelectionHandler#onSelection(SelectionEvent)
     */
    public void onSelection(SelectionEvent<Integer> event)
    {
        editor.setLoading(true);
        // We test if the rich text area is disabled to be sure that the editor is not already being switched.
        if (event.getSelectedItem() == WysiwygEditor.WYSIWYG_TAB_INDEX
            && !editor.getRichTextEditor().getTextArea().isEnabled()) {
            onSwitchToWysiwyg();
        } else {
            // We test if the rich text area is enabled to be sure that the editor is not already being switched.
            if (event.getSelectedItem() == WysiwygEditor.SOURCE_TAB_INDEX
                && editor.getRichTextEditor().getTextArea().isEnabled()) {
                // At this point we should have the HTML, adjusted by plug-ins, submitted.
                // See #onBeforeSelection(BeforeSelectionEvent)
                // Make the request to convert the HTML to source syntax.
                converter.fromHTML(editor.getRichTextEditor().getTextArea().getCommandManager().getStringValue(SUBMIT),
                    editor.getConfig().getParameter("syntax", WysiwygEditor.DEFAULT_SYNTAX),
                    new SwitchToSourceCallback());
            }
        }
    }

    /**
     * Disables the source editor, enables the rich text editor and updates the rich text.
     */
    private void onSwitchToWysiwyg()
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("source", editor.getPlainTextEditor().getTextArea().getText());

        Reloader reloader = new Reloader(editor.getRichTextEditor().getTextArea());
        reloader.reload(params, new LoadHandler()
        {
            public void onLoad(LoadEvent event)
            {
                // Disable the plain text area.
                editor.getPlainTextEditor().getTextArea().setEnabled(false);
                // Reset the content of the rich text area.
                editor.getRichTextEditor().getTextArea().getCommandManager().execute(RESET);
                // Store the initial value of the rich text area in case it is submitted without gaining focus.
                editor.getRichTextEditor().getTextArea().getCommandManager().execute(SUBMIT, true);
                // Enable the rich text area in order to be able to submit its content.
                editor.getRichTextEditor().getTextArea().getCommandManager().execute(ENABLE, true);
                // Focus the rich text area.
                editor.getRichTextEditor().getTextArea().setFocus(true);
            }
        });
    }
}
