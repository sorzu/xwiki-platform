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
package org.xwiki.gwt.wysiwyg.client.plugin.readonly;

import java.util.Arrays;
import java.util.List;

import org.xwiki.gwt.dom.client.DOMUtils;
import org.xwiki.gwt.dom.client.Document;
import org.xwiki.gwt.dom.client.Element;
import org.xwiki.gwt.dom.client.Event;
import org.xwiki.gwt.dom.client.Range;

import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;

/**
 * Handles the keyboard events concerning read-only regions inside the rich text area.
 * 
 * @version $Id$
 */
public class ReadOnlyKeyboardHandler implements KeyDownHandler, KeyPressHandler, KeyUpHandler
{
    /**
     * The list of key codes that are allowed on the read-only regions.
     */
    private static final List<Integer> NON_PRINTING_KEY_CODES =
        Arrays.asList(KeyCodes.KEY_ESCAPE, KeyCodes.KEY_PAGEUP, KeyCodes.KEY_PAGEDOWN, KeyCodes.KEY_END,
            KeyCodes.KEY_HOME, KeyCodes.KEY_LEFT, KeyCodes.KEY_UP, KeyCodes.KEY_RIGHT, KeyCodes.KEY_DOWN);

    /**
     * Utility object to manipulate the DOM.
     */
    private final DOMUtils domUtils = DOMUtils.getInstance();

    /**
     * Utility methods concerning read-only regions inside the rich text area.
     */
    private final ReadOnlyUtils readOnlyUtils = new ReadOnlyUtils();

    /**
     * Flag used to avoid handling both KeyDown and KeyPress events. This flag is needed because of the inconsistencies
     * between browsers regarding keyboard events. For instance IE doesn't generate the KeyPress event for backspace key
     * and generates multiple KeyDown events while a key is hold down. On the contrary, FF generates the KeyPress event
     * for the backspace key and generates just one KeyDown event while a key is hold down. FF generates multiple
     * KeyPress events when a key is hold down.
     */
    private boolean ignoreNextKeyPress;

    /**
     * Flag used to prevent the default browser behavior for the KeyPress event when the KeyDown event has been
     * canceled. This is needed only in functional tests where keyboard events (KeyDown, KeyPress, KeyUp) are triggered
     * independently and thus canceling KeyDown doesn't prevent the default KeyPress behavior. Without this flag, and
     * because we have to handle the KeyDown event besides the KeyPress in order to overcome cross-browser
     * inconsistencies, simulating keyboard typing in functional tests would trigger our custom behavior but also the
     * default browser behavior.
     */
    private boolean cancelNextKeyPress;

    /**
     * {@inheritDoc}
     * 
     * @see KeyDownHandler#onKeyDown(KeyDownEvent)
     */
    public void onKeyDown(KeyDownEvent event)
    {
        ignoreNextKeyPress = true;
        handleRepeatableKey((Event) event.getNativeEvent());
        cancelNextKeyPress = ((Event) event.getNativeEvent()).isCancelled();
    }

    /**
     * {@inheritDoc}
     * 
     * @see KeyPressHandler#onKeyPress(KeyPressEvent)
     */
    public void onKeyPress(KeyPressEvent event)
    {
        if (!ignoreNextKeyPress) {
            handleRepeatableKey((Event) event.getNativeEvent());
        } else if (cancelNextKeyPress) {
            ((Event) event.getNativeEvent()).xPreventDefault();
        }
        ignoreNextKeyPress = false;
        cancelNextKeyPress = false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see KeyUpHandler#onKeyUp(KeyUpEvent)
     */
    public void onKeyUp(KeyUpEvent event)
    {
        ignoreNextKeyPress = false;
        cancelNextKeyPress = false;
        handleKeyRelease((Event) event.getNativeEvent());
    }

    /**
     * Handles a repeatable key press.
     * 
     * @param event the native event that was fired
     */
    private void handleRepeatableKey(Event event)
    {
        // Don't handle the key if the event was canceled by a different party.
        if (!event.isCancelled()) {
            if (event.getKeyCode() == KeyCodes.KEY_BACKSPACE || event.getKeyCode() == KeyCodes.KEY_DELETE) {
                onDelete(event);
            } else if (!NON_PRINTING_KEY_CODES.contains(event.getKeyCode())) {
                onTyping(event);
            }
        }
    }

    /**
     * Handles a key release.
     * 
     * @param event the native event that was fired
     */
    private void handleKeyRelease(Event event)
    {
        // Ignore.
    }

    /**
     * Delete or backspace key has been pressed.
     * 
     * @param event the native event that was fired
     */
    private void onDelete(Event event)
    {
        Document document = Element.as(event.getEventTarget()).getOwnerDocument().cast();
        onDelete(event, document.getSelection().getRangeAt(0));
    }

    /**
     * Delete or backspace key has been pressed while the given range was selected.
     * 
     * @param event the native event that was fired
     * @param range the range targeted by the native event
     */
    private void onDelete(Event event, Range range)
    {
        if (range.isCollapsed()) {
            Element target = readOnlyUtils.getClosestReadOnlyAncestor(range.getCommonAncestorContainer());
            if (target == null || isDeleteAfter(event, target, range) || isBackspaceBefore(event, target, range)) {
                target = getNearbyReadOnlyContainer(range, event.getKeyCode() == KeyCodes.KEY_DELETE);
                if (target == null) {
                    return;
                }
            }
            onDelete(event, target);
        } else {
            Element startContainerReadOnlyAncestor =
                readOnlyUtils.getClosestReadOnlyAncestor(range.getStartContainer());
            Element endContainerReadOnlyAncestor =
                range.getStartContainer() != range.getEndContainer() ? readOnlyUtils.getClosestReadOnlyAncestor(range
                    .getEndContainer()) : startContainerReadOnlyAncestor;
            if (startContainerReadOnlyAncestor == endContainerReadOnlyAncestor) {
                if (startContainerReadOnlyAncestor != null) {
                    onDelete(event, startContainerReadOnlyAncestor);
                }
            } else {
                onDelete(event, startContainerReadOnlyAncestor, range, true);
                onDelete(event, endContainerReadOnlyAncestor, range, false);
            }
        }
    }

    /**
     * @param event the native event that was fired
     * @param container the read-only container
     * @param caret the caret
     * @return {@code true} if the {@link KeyCodes#KEY_BACKSPACE} was pressed and the caret is at the start of the given
     *         read-only container, {@code false} otherwise
     */
    private boolean isBackspaceBefore(Event event, Element container, Range caret)
    {
        return event.getKeyCode() == KeyCodes.KEY_BACKSPACE && isBefore(caret, container);
    }

    /**
     * @param caret the caret
     * @param container a DOM element
     * @return {@code true} if the caret is before the first visible leaf of the given container, {@code false}
     *         otherwise
     */
    private boolean isBefore(Range caret, Element container)
    {
        Node leaf = domUtils.getFirstLeaf(container);
        return domUtils.comparePoints(caret.getEndContainer(), caret.getEndOffset(), leaf, 0) <= 0;
    }

    /**
     * @param event the native event that was fired
     * @param container the read-only container
     * @param caret the caret
     * @return {@code true} if the {@link KeyCodes#KEY_DELETE} was pressed and the caret is at the end of the given
     *         read-only container, {@code false} otherwise
     */
    private boolean isDeleteAfter(Event event, Element container, Range caret)
    {
        return event.getKeyCode() == KeyCodes.KEY_DELETE && isAfter(caret, container);
    }

    /**
     * @param caret the caret
     * @param container a DOM element
     * @return {@code true} if the caret is after the last visible leaf of the given container, {@code false} otherwise
     */
    private boolean isAfter(Range caret, Element container)
    {
        Node leaf = domUtils.getLastLeaf(container);
        return domUtils
            .comparePoints(leaf, domUtils.getLength(leaf), caret.getStartContainer(), caret.getStartOffset()) <= 0;
    }

    /**
     * Deletes a read-only element when it is the only target of the given event.
     * 
     * @param event the native event that was fired
     * @param element the read-only element to be deleted
     */
    protected void onDelete(Event event, Element element)
    {
        element.getParentNode().removeChild(element);
        event.xPreventDefault();
    }

    /**
     * Deletes a read-only element when it is not the only target of the given event.
     * 
     * @param event the native event that was fired
     * @param element the read-only element to be deleted
     * @param range the range that touches the read-only element
     * @param start {@code true} if the start point of the given range is inside the read-only element, {@code false} if
     *            the end point is inside the read-only element
     */
    protected void onDelete(Event event, Element element, Range range, boolean start)
    {
        // Null safe.
        domUtils.detach(element);
    }

    /**
     * Looks for a read-only container to the left/right of the given caret. The caret has to be just after/before the
     * read-only container in order for this method to find it.
     * 
     * @param caret where to look for the read-only container
     * @param right {@code true} to look at the right of the caret, {@code false} to look at the left of the caret
     * @return the read-only container to the left/right of the caret
     */
    private Element getNearbyReadOnlyContainer(Range caret, boolean right)
    {
        if (caret.getStartContainer().getNodeType() == Node.TEXT_NODE) {
            boolean left = !right;
            boolean atStart = caret.getStartOffset() == 0;
            boolean atEnd = caret.getStartOffset() == caret.getStartContainer().getNodeValue().length();
            if ((left && !atStart) || (right && !atEnd)) {
                return null;
            }
        }
        Node leaf = right ? domUtils.getNextLeaf(caret) : domUtils.getPreviousLeaf(caret);
        return leaf != null ? readOnlyUtils.getClosestReadOnlyAncestor(leaf) : null;
    }

    /**
     * A printable character key was pressed.
     * 
     * @param event the native event that was fired
     */
    private void onTyping(Event event)
    {
        Document document = Element.as(event.getEventTarget()).getOwnerDocument().cast();
        if (readOnlyUtils.isSelectionBoundaryInsideReadOnlyElement(document)) {
            event.xPreventDefault();
        }
    }
}
