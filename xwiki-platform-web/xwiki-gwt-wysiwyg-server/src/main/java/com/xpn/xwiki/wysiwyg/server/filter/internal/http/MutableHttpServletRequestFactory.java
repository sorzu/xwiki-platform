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
package com.xpn.xwiki.wysiwyg.server.filter.internal.http;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.xwiki.component.annotation.Component;

import com.xpn.xwiki.wysiwyg.server.filter.MutableServletRequest;
import com.xpn.xwiki.wysiwyg.server.filter.MutableServletRequestFactory;

/**
 * {@link MutableServletRequestFactory} implementation for the HTTP protocol.
 * 
 * @version $Id$
 */
@Component(hints = {"HTTP/1.1", "HTTP/1.0" })
public class MutableHttpServletRequestFactory implements MutableServletRequestFactory
{
    /**
     * {@inheritDoc}
     * 
     * @see MutableServletRequestFactory#newInstance(ServletRequest)
     */
    public synchronized MutableServletRequest newInstance(ServletRequest request)
    {
        if (request instanceof HttpServletRequest) {
            return new MutableHttpServletRequest((HttpServletRequest) request);
        } else {
            throw new IllegalArgumentException("Expecting HttpServletRequest!");
        }
    }
}