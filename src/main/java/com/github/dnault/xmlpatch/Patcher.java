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

package com.github.dnault.xmlpatch;

import com.github.dnault.xmlpatch.internal.XmlHelper;
import org.jaxen.jdom.XPathNamespace;
import org.jdom.*;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import static com.github.dnault.xmlpatch.internal.XmlHelper.getInScopeNamespaceDeclarations;

@SuppressWarnings("unchecked")
public class Patcher {

    static private boolean lenient = true;
    static private boolean trimMultilineText = true;

    public static void patch(InputStream target, InputStream diff,
                             OutputStream result) throws IOException {

        try {
            Document targetDoc = XmlHelper.parse(target);
            Document diffDoc = XmlHelper.parse(diff);

            Element diffRoot = diffDoc.getRootElement();
            if (!"diff".equals(diffRoot.getName())) {
                throw new PatchException(ErrorCondition.INVALID_DIFF_FORMAT,
                        "root element not named 'diff'");
            }

            for (Object o : diffRoot.getChildren()) {
                patch(targetDoc, (Element) o);
            }

            XMLOutputter outputter = new XMLOutputter();

            // Use the separator that is appropriate for the platform.
            Format format = Format.getRawFormat();
            format.setLineSeparator(System.getProperty("line.separator"));
            outputter.setFormat(format);

            outputter.output(targetDoc, result);
        } catch (JDOMException e) {
            throw new PatchException(ErrorCondition.INVALID_DIFF_FORMAT, e);
        }
    }

    private static void patch(Document target, Element patch) throws JDOMException {
        String operation = patch.getName();
        switch (operation) {
            case "add":
                add(target, patch);
                break;
            case "replace":
                replace(target, patch);
                break;
            case "remove":
                remove(target, patch);
                break;
            default:
                throw new RuntimeException("unknown operation: " + operation);
        }
    }

    private static void throwIfNotSingleNodeOfType(List<Content> content, Class expectedClass) {
        if (content.size() != 1 || !content.get(0).getClass().equals(expectedClass)) {
            throw new PatchException(ErrorCondition.INVALID_NODE_TYPES, "expected replacement to be a single content node of type "
                    + expectedClass.getSimpleName());
        }
    }

    private static void replace(Document target, Element patch) throws JDOMException {
        for (Object node : selectNodes(target, patch)) {
            doReplace(patch, node);
        }
    }

    public static void main(String[] args) throws Exception {
        Document doc = XmlHelper.parse(new ByteArrayInputStream("<replace/>".getBytes("UTF-8")));
        Element patch = doc.getRootElement();

        Document d = new Document();
        Element e = new Element("foo").setAttribute("message", "goodbye").setText("asdasda");
        d.addContent(e);

        doReplace(patch, e.getContent(0));
        System.out.println("[" + e.getText() +"]");
    }

    private static String getTextMaybeTrim(Element patch) {
        // Implement a non-standard "trim" attribute on patch nodes.
        String value = patch.getText();
        String override = patch.getAttributeValue("trim");
        if (override != null) {
            if (override.equals("true")) {
                return value.trim();
            }
            if (override.equals("false")) {
                return value;
            }
            throw new RuntimeException("expected 'trim' attribute to be 'true' or 'false' but found " + override);
        }

        return trimMultilineText && isMultiline(value) ? value.trim() : value;
    }

    private static void doReplace(Element patch, Object node) throws JDOMException {

        if (node instanceof Attribute) {
            for (Object o : patch.getContent()) {
                if (!(o instanceof Text)) {
                    throw new PatchException(ErrorCondition.INVALID_NODE_TYPES, "attribute value replacement must be text");
                }
            }

            ((Attribute) node).setValue(getTextMaybeTrim(patch));
            return;
        }

        if (node instanceof Element || node instanceof Comment || node instanceof ProcessingInstruction) {
            List<Content> replacement = patch.cloneContent();

            if (lenient) {
                // ignore whitespace siblings so the diff document can be pretty
                for (Iterator<Content> i = replacement.iterator(); i.hasNext();) {
                    if (isWhitespace(i.next())) {
                        i.remove();
                    }
                }
            }

            throwIfNotSingleNodeOfType(replacement, node.getClass());

            Content replaceMe = (Content) node;
            Parent p = replaceMe.getParent();
            int index = XmlHelper.indexOf(p, replaceMe);
            replaceMe.detach();

            if (p instanceof Element) {
                canonicalizeNamespaces((Element) p, replacement);
                ((Element) p).addContent(index, replacement);

            } else if (p instanceof Document) {
                if (!(node instanceof Element)) {
                    throw new PatchException(ErrorCondition.INVALID_XML_PROLOG_OPERATION,
                            "can't replace prolog nodes");
                }

                ((Document) p).setRootElement((Element) replacement.get(0));

            } else {
                // shouldn't happen.
                throw new RuntimeException("expected Parent to be either Document or Element but found " + p.getClass());
            }

            return;
        }

        if (node instanceof Text) {
            List<Content> replacement = patch.cloneContent();
            if (!replacement.isEmpty()) {
                throwIfNotSingleNodeOfType(replacement, Text.class);
            }

            Content replaceMe = (Content) node;
            Element p = replaceMe.getParentElement();
            int index = XmlHelper.indexOf(p, replaceMe);
            replaceMe.detach();

            String replacementText = getTextMaybeTrim(patch);
            if (replacementText.length() > 0) {
                p.addContent(index, new Text(replacementText));
            }

            return;
        }


        if (node instanceof XPathNamespace) {
            if (true) {
                throw new RuntimeException("removing namespace declarations is not yet implemented");
            }
            XPathNamespace jaxenNs = (XPathNamespace) node;
            Element parent = jaxenNs.getJDOMElement();
            Namespace ns = jaxenNs.getJDOMNamespace();

            String newUri = getTextMaybeTrim(patch);
            Namespace newNamespace = createNamespace(ns.getPrefix(), newUri);

            if (parent.getAdditionalNamespaces().contains(ns)) {
                parent.removeNamespaceDeclaration(ns);
                parent.addNamespaceDeclaration(newNamespace);
            } else if (parent.getNamespace().getPrefix().equals(ns.getPrefix())) {
                parent.setNamespace(newNamespace);
            } else {
                throw new PatchException(ErrorCondition.UNLOCATED_NODE);
            }
            return;
        }

        throw new PatchException(ErrorCondition.INVALID_PATCH_DIRECTIVE,
                "target node was not an Attribute, Element, Comment, Text, Processing Instruction, or Namespace");
    }

    private static boolean isMultiline(String replacement) {
        return replacement.contains("\n") || replacement.contains("\r");
    }

    private static boolean isWhitespace(Content node) {
        // todo stricter interpretation of whitespace?
        return node instanceof Text && node.getValue().trim().length() == 0;
    }

    private static void add(Document target, Element patch) throws JDOMException {
        for (Object node : selectNodes(target, patch)) {
            doAdd(patch, node);
        }
    }

    private static void doAdd(Element patch, Object nodeObject) throws JDOMException {
        String position = patch.getAttributeValue("pos");
        String type = patch.getAttributeValue("type");

        Content node = (Content) nodeObject;

        if (type != null) {
            if (type.startsWith("@")) {
                addAttribute(patch, type.substring(1), asElement(node));
                return;
            }
            if (type.startsWith("namespace::")) {
                addNamespaceDeclaration(patch, type.substring(11), asElement(node));
                return;
            }
            // todo validation
        }

        if ("before".equals(position) || "after".equals(position)) {
            if (node.getParentElement() == null) {
                for (Object o : patch.getContent()) {
                    if (!(o instanceof Comment) && !(o instanceof ProcessingInstruction)) {
                        throw new PatchException(ErrorCondition.INVALID_ROOT_ELEMENT_OPERATION);
                    }
                }
            }
        }

        List<Content> newContent = patch.cloneContent();
        try {
            if (position == null) {
                // default is "append"
                Element e = asElement(node);
                canonicalizeNamespaces(e, newContent);
                e.getContent().addAll(newContent);
            } else if ("prepend".equals(position)) {
                Element e = asElement(node);
                canonicalizeNamespaces(e, newContent);
                e.getContent().addAll(0, newContent);
            } else if ("before".equals(position)) {
                Parent p = node.getParent();

                if (p instanceof Element) {
                    canonicalizeNamespaces((Element) p, newContent);
                }

                int nodeIndex = XmlHelper.indexOf(p, node);
                p.getContent().addAll(nodeIndex, newContent);
            } else if ("after".equals(position)) {
                Parent p = node.getParent();

                if (p instanceof Element) {
                    canonicalizeNamespaces((Element) p, newContent);
                }

                int nodeIndex = XmlHelper.indexOf(p, node);
                p.getContent().addAll(nodeIndex + 1, newContent);
            } else {
                throw new PatchException(ErrorCondition.INVALID_DIFF_FORMAT,
                        "unrecognized position for add: " + position);
            }
        } catch (IllegalAddException e) {
            // todo nice message
            throw new PatchException(ErrorCondition.INVALID_PATCH_DIRECTIVE, e);
        }

    }

    private static void addNamespaceDeclaration(Element patch, String prefix, Element target) {

        Namespace ns = createNamespace(prefix, getTextMaybeTrim(patch));
        target.addNamespaceDeclaration(ns);
    }

    private static Namespace createNamespace(String prefix, String uri) {
        try {
            return Namespace.getNamespace(prefix, uri);
        } catch (IllegalNameException e) {
            throw new PatchException(ErrorCondition.INVALID_NAMESPACE_URI, e.getMessage());
        }
    }

    private static void addAttribute(Element patch, String name, Element target) {

        String prefix = null;
        if (name.contains(":")) {
            String[] prefixAndName = name.split(":");
            // todo validate length = 2?
            prefix = prefixAndName[0];
            name = prefixAndName[1];
        }

        String value = getTextMaybeTrim(patch);
        Attribute a = new Attribute(name, value);

        if (prefix != null) {
            Namespace ns = patch.getNamespace(prefix);
            if (ns == null) {
                throw new PatchException(
                        ErrorCondition.INVALID_NAMESPACE_PREFIX,
                        "could not resolve namespace prefix '" + prefix
                                + "' in the context of the diff document");
            }
            a.setNamespace(ns);
        }
        canonicalizeNamespace(a, getInScopeNamespaceDeclarations(target));
        target.setAttribute(a);
    }

    private static void canonicalizeNamespaces(Element scope,
                                               List<Content> content) {

        Map<String, String> prefixToUri = XmlHelper
                .getInScopeNamespaceDeclarations(scope);

        for (Content c : content) {
            if (c instanceof Element) {
                canonicalizeNamespace((Element) c, prefixToUri);
            }
        }

    }

    private static void canonicalizeNamespace(Attribute a,
                                              Map<String, String> prefixToUri) {
        for (Map.Entry<String, String> entry : prefixToUri.entrySet()) {
            if (a.getNamespaceURI().equals(entry.getValue())) {
                if (entry.getKey().equals("")) {
                    // default namespace doesn't apply to attributes
                    continue;
                }
                a.setNamespace(Namespace.getNamespace(entry.getKey(), entry
                        .getValue()));
                break;

            }
        }
    }

    private static void canonicalizeNamespace(Element e,
                                              Map<String, String> prefixToUri) {
        Namespace ns = e.getNamespace();

        for (Map.Entry<String, String> entry : prefixToUri.entrySet()) {
            if (ns.getURI().equals(entry.getValue())) {
                e.setNamespace(Namespace.getNamespace(entry.getKey(), entry
                        .getValue()));
                break;
            }
        }

        for (Object o : e.getAttributes()) {
            canonicalizeNamespace((Attribute) o, prefixToUri);
        }

        for (Object o : e.getChildren()) {
            canonicalizeNamespace((Element) o, prefixToUri);
        }
    }

    private static Element asElement(Content node) {
        try {
            return (Element) node;
        } catch (ClassCastException e) {
            throw new PatchException(ErrorCondition.INVALID_PATCH_DIRECTIVE,
                    "selected node is not an element");
        }
    }

    private static List<Object> selectNodes(Document target, Element patch) throws JDOMException {

        boolean isMultiSelect = false;

        String selector = patch.getAttributeValue("sel");
        if (selector == null) {
            selector = patch.getAttributeValue("msel");
            isMultiSelect = true;
        }

        XPath xpath = XPath.newInstance(selector);
        bindNamespacePrefixes(xpath, patch);
        List content = xpath.selectNodes(target);

        if (content.isEmpty()) {
            throw new PatchException(ErrorCondition.UNLOCATED_NODE,
                    "no matches for selector \"" + selector + "\"");
        }
        if (!isMultiSelect && content.size() > 1) {
            throw new PatchException(ErrorCondition.UNLOCATED_NODE,
                    "more that one match for selector \"" + selector + "\" -- if you want to select multiple nodes, use the 'msel' attribute instead of 'sel'.");
        }
        return content;
    }

    private static void bindNamespacePrefixes(XPath xpath, Element patch) {
        List<Element> chain = new ArrayList<>();
        for (Element e = patch; e != null; e = e.getParentElement()) {
            chain.add(e);
        }

        // namespace definitions on the child should override any parent defintions with the same prefix
        Collections.reverse(chain);

        for (Element e : chain) {
            for (Object o : e.getAdditionalNamespaces()) {
                xpath.addNamespace((Namespace) o);
            }
        }
    }

    private static void remove(Document target, Element patch) throws JDOMException {
        for (Object node : selectNodes(target, patch)) {
            doRemove(patch, node);
        }
    }

    private static void doRemove(Element patch, Object node) throws JDOMException {

        if (node instanceof Element || node instanceof Comment || node instanceof ProcessingInstruction) {

            String ws = patch.getAttributeValue("ws");
            boolean before = "both".equals(ws) || "before".equals(ws);
            boolean after = "both".equals(ws) || "after".equals(ws);

            Content c = (Content) node;
            Element e = c.getParentElement();
            if (e == null) {
                throw new PatchException(ErrorCondition.INVALID_ROOT_ELEMENT_OPERATION,
                        "can't remove root element");
            }

            int index = e.indexOf(c);
            List<Content> nodesToDetach = new ArrayList<>();
            nodesToDetach.add(c);

            if (before) {
                nodesToDetach.add(getWhitespace(e, index - 1));
            }
            if (after) {
                nodesToDetach.add(getWhitespace(e, index + 1));
            }

            for (Content detachMe : nodesToDetach) {
                detachMe.detach();
            }

            return;
        }

        if (patch.getAttribute("ws") != null) {
            throw new PatchException(ErrorCondition.INVALID_PATCH_DIRECTIVE,
                    "The 'ws' attribute is not allowed when removing " +
                            "Attribute, Text or Namespace nodes.");
        }

        if (node instanceof Attribute) {
            Attribute a = (Attribute) node;
            a.getParent().removeAttribute(a);
            return;
        }

        if (node instanceof Text) {
            ((Content) node).detach();
            return;
        }

        if (node instanceof XPathNamespace) {
            throw new RuntimeException("removing namespace declarations is not yet implemented");
            // return;
        }
    }

    private static Text getWhitespace(Element parent, int i) {

        try {
            Content c = parent.getContent(i);
            if (isWhitespace(c)) {
                return (Text) c;
            }
        } catch (IndexOutOfBoundsException noSuchSibling) {
            // invalid whitepace directive
        }

        throw new PatchException(ErrorCondition.INVALID_WHITESPACE_DIRECTIVE,
                "sibling is not a whitespace node");
    }

}
