package com.googlecode.mp4parser.authoring.tracks.ttml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/* loaded from: classes.dex */
public class TtmlSegmenter {
    public static List<Document> split(Document document, int i) throws XPathExpressionException {
        boolean z;
        int i2;
        XPath xPath;
        XPathExpression xPathExpression;
        int i3 = i * 1000;
        XPath newXPath = XPathFactory.newInstance().newXPath();
        XPathExpression compile = newXPath.compile("//*[name()='p']");
        ArrayList arrayList = new ArrayList();
        do {
            long size = arrayList.size() * i3;
            long size2 = (arrayList.size() + 1) * i3;
            Document document2 = (Document) document.cloneNode(true);
            NodeList nodeList = (NodeList) compile.evaluate(document2, XPathConstants.NODESET);
            int i4 = 0;
            z = false;
            while (i4 < nodeList.getLength()) {
                Node item = nodeList.item(i4);
                long startTime = TtmlHelpers.getStartTime(item);
                long endTime = TtmlHelpers.getEndTime(item);
                if (startTime >= size || endTime <= size) {
                    i2 = i3;
                    xPath = newXPath;
                } else {
                    i2 = i3;
                    xPath = newXPath;
                    changeTime(item, "begin", size - startTime);
                    startTime = size;
                }
                if (startTime < size || startTime >= size2 || endTime <= size2) {
                    xPathExpression = compile;
                } else {
                    xPathExpression = compile;
                    changeTime(item, "end", size2 - endTime);
                    startTime = size;
                    endTime = size2;
                }
                if (startTime > size2) {
                    z = true;
                }
                if (startTime < size || endTime > size2) {
                    item.getParentNode().removeChild(item);
                } else {
                    long j = -size;
                    changeTime(item, "begin", j);
                    changeTime(item, "end", j);
                }
                i4++;
                compile = xPathExpression;
                i3 = i2;
                newXPath = xPath;
            }
            trimWhitespace(document2);
            Element element = (Element) newXPath.compile("/*[name()='tt']/*[name()='body'][1]").evaluate(document2, XPathConstants.NODE);
            String attribute = element.getAttribute("begin");
            String attribute2 = element.getAttribute("end");
            if (attribute == null || "".equals(attribute)) {
                element.setAttribute("begin", TtmlHelpers.toTimeExpression(size));
            } else {
                changeTime(element, "begin", size);
            }
            if (attribute2 == null || "".equals(attribute2)) {
                element.setAttribute("end", TtmlHelpers.toTimeExpression(size2));
            } else {
                changeTime(element, "end", size2);
            }
            arrayList.add(document2);
        } while (z);
        return arrayList;
    }

    public static void changeTime(Node node, String str, long j) {
        if (node.getAttributes() == null || node.getAttributes().getNamedItem(str) == null) {
            return;
        }
        String nodeValue = node.getAttributes().getNamedItem(str).getNodeValue();
        long time = TtmlHelpers.toTime(nodeValue) + j;
        node.getAttributes().getNamedItem(str).setNodeValue(TtmlHelpers.toTimeExpression(time, nodeValue.contains(".") ? -1 : ((int) (time - ((time / 1000) * 1000))) / 44));
    }

    public static Document normalizeTimes(Document document) throws XPathExpressionException {
        XPath newXPath = XPathFactory.newInstance().newXPath();
        newXPath.setNamespaceContext(TtmlHelpers.NAMESPACE_CONTEXT);
        NodeList nodeList = (NodeList) newXPath.compile("//*[name()='p']").evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            pushDown(nodeList.item(i));
        }
        for (int i2 = 0; i2 < nodeList.getLength(); i2++) {
            Node item = nodeList.item(i2);
            removeAfterPushDown(item, "begin");
            removeAfterPushDown(item, "end");
        }
        return document;
    }

    private static void pushDown(Node node) {
        long j = 0;
        Node node2 = node;
        while (true) {
            node2 = node2.getParentNode();
            if (node2 == null) {
                break;
            } else if (node2.getAttributes() != null && node2.getAttributes().getNamedItem("begin") != null) {
                j += TtmlHelpers.toTime(node2.getAttributes().getNamedItem("begin").getNodeValue());
            }
        }
        if (node.getAttributes() != null && node.getAttributes().getNamedItem("begin") != null) {
            node.getAttributes().getNamedItem("begin").setNodeValue(TtmlHelpers.toTimeExpression(TtmlHelpers.toTime(node.getAttributes().getNamedItem("begin").getNodeValue()) + j));
        }
        if (node.getAttributes() == null || node.getAttributes().getNamedItem("end") == null) {
            return;
        }
        node.getAttributes().getNamedItem("end").setNodeValue(TtmlHelpers.toTimeExpression(j + TtmlHelpers.toTime(node.getAttributes().getNamedItem("end").getNodeValue())));
    }

    private static void removeAfterPushDown(Node node, String str) {
        while (true) {
            node = node.getParentNode();
            if (node == null) {
                return;
            }
            if (node.getAttributes() != null && node.getAttributes().getNamedItem(str) != null) {
                node.getAttributes().removeNamedItem(str);
            }
        }
    }

    public static void trimWhitespace(Node node) {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item.getNodeType() == 3) {
                item.setTextContent(item.getTextContent().trim());
            }
            trimWhitespace(item);
        }
    }
}
