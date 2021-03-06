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
package org.xwiki.annotation.event;

import org.xwiki.bridge.event.AbstractDocumentEvent;
import org.xwiki.observation.event.filter.EventFilter;
import org.xwiki.observation.event.filter.FixedNameEventFilter;

/**
 * Base class for all annotation {@link org.xwiki.observation.event.Event}.
 * 
 * @version $Id$
 * @since 2.6RC2
 */
public abstract class AbstractAnnotationEvent extends AbstractDocumentEvent
{
    /**
     * The version identifier for this Serializable class. Increment only if the <i>serialized</i> form of the class
     * changes.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The annotation identifier used in events.
     */
    private String identifier;

    /**
     * Constructor initializing the event filter with an
     * {@link org.xwiki.observation.event.filter.AlwaysMatchingEventFilter}, meaning that this event will match any
     * other annotation event (add, update, delete).
     */
    public AbstractAnnotationEvent()
    {
        super();
    }

    /**
     * Constructor initializing the event filter with a {@link org.xwiki.observation.event.filter.FixedNameEventFilter},
     * meaning that this event will match only comment events affecting the document matching the passed document name.
     * 
     * @param documentName the name of the updated document to match
     * @param identifier the identifier of the annotation added/updated/deleted
     */
    public AbstractAnnotationEvent(String documentName, String identifier)
    {
        // TODO refactor annotation event to take a document reference as constructor and depreciate this constructor
        super(new FixedNameEventFilter(documentName));
        this.identifier = identifier;
    }

    /**
     * Constructor using a custom {@link EventFilter}.
     * 
     * @param eventFilter the filter to use for matching events
     */
    public AbstractAnnotationEvent(EventFilter eventFilter)
    {
        super(eventFilter);
    }

    /**
     * Retrieves the identifier of the annotation added/updated/deleted in the event.
     * 
     * @return identifier of the annotation
     */
    public String getIdentifier()
    {
        return identifier;
    }
}
