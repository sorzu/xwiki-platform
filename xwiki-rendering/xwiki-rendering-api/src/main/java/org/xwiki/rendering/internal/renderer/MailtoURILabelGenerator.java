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
package org.xwiki.rendering.internal.renderer;

import org.apache.commons.lang.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.listener.Link;
import org.xwiki.rendering.renderer.link.URILabelGenerator;

/**
 * Generate link labels for MAILTO URIs.
 * 
 * @version $Id$
 * @since 2.2RC1
 */
@Component("mailto")
public class MailtoURILabelGenerator implements URILabelGenerator
{
    /**
     * The MAILTO URI prefix (the scheme followed by ":").
     */
    private static final String MAILTO = "mailto:";
    
    /**
     * {@inheritDoc}
     * @see org.xwiki.rendering.renderer.link.URILabelGenerator#generateLabel(org.xwiki.rendering.listener.Link)
     */
    public String generateLabel(Link link)
    {
        // Remove the scheme prefix and the query string (if any).
        return StringUtils.substringBefore(StringUtils.removeStart(link.getReference(), MAILTO), "?");
    }
}