/*
 * Copyright 2015 David Nault and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dnault.xmlpatch.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.Parent;
import org.jdom.input.SAXBuilder;

public class XmlHelper {

    public static Document parse(File f) throws IOException, JDOMException {
        try (FileInputStream fis = new FileInputStream(f)) {
            return XmlHelper.parse(fis);
        }
    }

	public static Document parse(InputStream is) throws IOException, JDOMException {
		SAXBuilder builder = new SAXBuilder();

        // DTD validation is makes an HTTP request and is slow. Don't need this feature, so disable it.
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        Document doc = builder.build(is);
		return doc; 
	}
	
	public static List<Content> clone(List<Content> content) {
		List<Content> cloned = new ArrayList<Content>();
		for (Object o : content) {
			cloned.add((Content) ((Content)o).clone());
		}
		return cloned;
	}
	
	public static int indexOf(Parent p, Content child) {
		return p.getContent().indexOf(child);
	}
	
	public static Map<String,String> getInScopeNamespaceDeclarations(Element e) {
		Map<String, String> prefixToUri = new HashMap<String,String>();
		
		for (; e != null; e = e.getParentElement()) {
			putIfAbsent(prefixToUri, e.getNamespace());			
			
			for (Object o : e.getAdditionalNamespaces()) {
				putIfAbsent(prefixToUri, (Namespace) o);
			}
		}
		
		return prefixToUri;
	}

    @SuppressWarnings("unchecked")
    public static List<Element> getChildren(Element e) {
        return (List<Element>) e.getChildren();
    }

    @SuppressWarnings("unchecked")
    public static List<Element> getChildren(Element e, String name) {
        return (List<Element>) e.getChildren(name);
    }
	
	/* NOT ATOMIC */
	private static <K,V> boolean putIfAbsent(Map<K,V> map, K key, V value) {
		if (map.containsKey(key)) {
			return false;
		}
		map.put(key, value);
		return true;
	}
	
	private static boolean putIfAbsent(Map<String,String> prefixToUri, Namespace ns) {
		if (ns == null) {
			return false;			
		}
		return putIfAbsent(prefixToUri, ns.getPrefix(), ns.getURI());
	}
}

