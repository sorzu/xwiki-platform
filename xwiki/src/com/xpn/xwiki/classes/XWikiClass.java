/**
 * ===================================================================
 *
 * Copyright (c) 2003 Ludovic Dubost, All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details, published at
 * http://www.gnu.org/copyleft/lesser.html or in lesser.txt in the
 * root folder of this distribution.
 *
 * Created by
 * User: Ludovic Dubost
 * Date: 9 d�c. 2003
 * Time: 11:51:16
 */
package com.xpn.xwiki.classes;

import com.xpn.xwiki.XWikiException;

import java.util.HashMap;
import java.util.Map;


public class XWikiClass extends XWikiObject implements XWikiClassInterface {
    public XWikiObjectPropertyInterface get(String name) {
        return (XWikiObjectPropertyInterface)fields.get(name);
    }

    public void put(String name, XWikiObjectPropertyInterface property) {
        fields.put(name, property);
    }
}
