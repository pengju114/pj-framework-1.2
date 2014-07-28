package com.pj.core.utilities;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XML节点类
 * @author luzhenwen
 */
public class XMLElement {
	public static enum XMLElementType{
		XMLElementNode,
		XMLElementText
	}
	
	private XMLElementType type;
	private String         text;
	private String         name;
	private XMLElement     parent;
	
	// 内部使用
	private HashMap<String, LinkedList<XMLElement>> childElements;
	private ArrayList<XMLElement>                   sortedChildElements;
	private HashMap<String, String>                 attributes;
	private LinkedList<XMLElement>                  stack;
	
	

	public XMLElement(String uri){
		this();
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			parser.parse(uri, new ParserHandler());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			domParseUri(uri);
		}
	}
	
	public XMLElement(File file){
		this();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			parser.parse(file, new ParserHandler());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			domParseFile(file);
		}
	}
	
	
	public XMLElement(InputStream stream){
		this();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			parser.parse(stream, new ParserHandler());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			domParseStream(stream);
		}
	}
	
	
	private void domParseUri(String uri) {
		// TODO Auto-generated method stub
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(uri);
			packageElement(parseNodeTree(document.getDocumentElement()));
			document = null;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	

	private void domParseFile(File file) {
		// TODO Auto-generated method stub
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
			packageElement(parseNodeTree(document.getDocumentElement()));
			document = null;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	private void domParseStream(InputStream stream) {
		// TODO Auto-generated method stub
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
			packageElement(parseNodeTree(document.getDocumentElement()));
			document = null;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	private XMLElement parseNodeTree(Node node) {
		// TODO Auto-generated method stub
		XMLElement element = new XMLElement();
		
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			
			element.name = node.getNodeName();
			NamedNodeMap attrMap = node.getAttributes();
			if (attrMap!=null && attrMap.getLength()>0) {
				for (int i = 0; i < attrMap.getLength(); i++) {
					Node attr = attrMap.item(i);
					element.setAttribute(attr.getNodeName(), attr.getNodeValue());
				}
			}
			
			
			NodeList list = node.getChildNodes();
			if (list.getLength()>1) {
				for (int i = 0; i < list.getLength(); i++) {
					Node n = list.item(i);
					if (n.getNodeType()==Node.TEXT_NODE) {
						continue;
					}
					element.addChildElement(parseNodeTree(n));
				}
			}else if (list.getLength()==1) {
				Node c = list.item(0);
				if ( c.getNodeType()==Node.ELEMENT_NODE) {
					element.addChildElement(parseNodeTree(c));
				}else if (c.getNodeType() == Node.TEXT_NODE || c.getNodeType() == Node.CDATA_SECTION_NODE) {
					element.text = c.getNodeValue().trim();
				}
			}
		}
		
		return element;
	}
	
	private void packageElement(XMLElement elem) {
		// TODO Auto-generated method stub
		copyFrom(elem);
		this.parent = null;
	}
	
	
	public XMLElement(){
		type    = XMLElementType.XMLElementNode;
		text    = "";
		name    = "";
		parent  = null;
		
		
		childElements        = new HashMap<String, LinkedList<XMLElement>>();
		sortedChildElements  = new ArrayList<XMLElement>();
		attributes           = new HashMap<String, String>();
		
	}
	
	public XMLElementType getType() {
		return type;
	}
	public void setType(XMLElementType type) {
		this.type = type;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public XMLElement getParent() {
		return parent;
	}
	

	public String getAttribute(String name){
		return attributes.get(name);
	}
	public Set<String> getAttributeNames(){
		return attributes.keySet();
	}
	public void setAttribute(String name, String value){
		attributes.put(name, value);
	}
	public void removeAttribute(String name){
		attributes.remove(name);
	}


	public List<XMLElement> getChildren(){
		return sortedChildElements;
	}
	public int getChildrenCount(){
		return sortedChildElements.size();
	}
	/*!
	 @abstract 根据节点名获取子节点列表
	 */
	public List<XMLElement> getChildrenByName(String elementName){
		return childElements.get(elementName);
	}
	/*!
	 @abstract 根据节点名获取一个子节点（即使有多个）
	 */
	public XMLElement getChildByName(String elementName){
		LinkedList<XMLElement> list = childElements.get(elementName);
		return (list!=null && list.size()>0)?list.getFirst():null;
	}
	public XMLElement getChildAt(int index){
		if (index>-1 && index < sortedChildElements.size()) {
			return sortedChildElements.get(index);
		}
		return null;
	}
	public void addChildElement(XMLElement child){
		if (child!=null) {
	        LinkedList<XMLElement> list = childElements.get(child.name);
	        if (list==null) {
	            list = new LinkedList<XMLElement>();
	            childElements.put(child.name, list);
	        }
	        
	        child.parent = this;
	        list.add(child);
	        sortedChildElements.add(child);
	    }
	}
	public List<XMLElement> removeChildrenByName(String elementName){
		LinkedList<XMLElement> elements = childElements.remove(elementName);
		if (elements!=null) {
			sortedChildElements.removeAll(elements);
			for (XMLElement xmlElement : elements) {
				xmlElement.parent = null;
			}
		}
		return elements;
	}
	public void removeChild(XMLElement element){
		do {
			sortedChildElements.remove(element);
		} while (sortedChildElements.remove(element));
		for (LinkedList<XMLElement> list : childElements.values()) {
			do {
				list.remove(element);
			} while (list.remove(element));
		}
		if (element!=null) {
			element.parent = null;
		}
	}

	public void each(XMLElementIterator xmlElementIterator){
		each(this, xmlElementIterator, 0);
	}
	
	private void each(XMLElement root ,XMLElementIterator iterator, int level){
	    for (int k = 0; k<root.sortedChildElements.size(); k++) {
	        XMLElement e = root.sortedChildElements.get(k);
	        // invoke handler
	        if(!iterator.receive(e,level)){
	            return;
	        }
	        
	        each(e ,iterator ,level+1);
	    }
	}
	
	public String toXMLString(){
		return XMLString(this, 0).toString();
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format("<%s %s>...has %d child(ren)...</%s>", name,attributeString(),getChildrenCount(),name);
	}

	public String attributeString(){
	    StringBuilder builder = new StringBuilder(attributes.size()*10);
	    
	    for (Map.Entry<String, String> e : attributes.entrySet()) {
			builder.append(e.getKey()).append('=').append('"').append(e.getValue()).append('"').append(' ');
		}
	    
	    if (attributes.size()>0) {
	       builder.deleteCharAt(builder.length()-1);
	    }
	    
	    return builder.toString();
	}

	private CharSequence XMLString(XMLElement elem , int level) {
	    StringBuilder builder = new StringBuilder();
	    builder.append('\n');
	    
	    addGap(builder, level);
	    builder.append('<').append(elem.name);
	    if (elem.attributes.size()>0) {
	    	builder.append(' ').append(elem.attributeString());
	    }
	    
	    if (elem.getChildrenCount()>0) {
	        builder.append('>');
	        
	        for (int k = 0; k<elem.getChildrenCount(); k++) {
	            XMLElement e = elem.getChildAt(k);
	            builder.append(XMLString(e,level+1));
	        }
	        addGap(builder, level);
	        builder.append('<').append('/').append(elem.name).append('>').append('\n');
	        
	    }else if(string(elem.text).length()>0){
	        builder.append('>').append('\n');
	        addGap(builder, level+1);
	        builder.append(elem.text);
	        builder.append('\n');
	        
	        addGap(builder, level);
	        builder.append('<').append('/').append(elem.name).append('>').append('\n');
	    }else{
	        builder.append(" />\n");
	    }
	    return builder;
	}

	private void addGap(StringBuilder buffer ,int count){
	    for (int i=0; i<count; i++) {
	        buffer.append('\t');
	    }
	}
	
	public static interface XMLElementIterator{
		public boolean receive(XMLElement element,int level);
	}
	
	private static String string(String str){
		return str==null?"":str;
	}
	
	private void copyFrom(XMLElement element){
		if (element!=null) {
			this.name = element.name;
			this.text = element.text;
			this.type = element.type;
			
			this.parent = element.parent;
			
			this.childElements = element.childElements;
			this.sortedChildElements = element.sortedChildElements;
			this.attributes = element.attributes;
		}
	}
	
	private class ParserHandler extends DefaultHandler{
		@Override
		public void startDocument() throws SAXException {
			// TODO Auto-generated method stub
			super.startDocument();
			
			text = "";
			name = "";
			
			childElements.clear();
			sortedChildElements.clear();
			attributes.clear();
			
			stack = new LinkedList<XMLElement>();
			
			stack.add(XMLElement.this);
		}
		
		@Override
		public void endDocument() throws SAXException {
			// TODO Auto-generated method stub
			super.endDocument();
			XMLElement root = stack.size()>0?stack.getLast():null;
			if (root!=null && root.getChildrenCount()>0) {
				copyFrom(root.getChildren().get(root.getChildrenCount()-1));
				XMLElement.this.parent = null;
			}
			stack.clear();
			stack = null;
		}
		
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			// TODO Auto-generated method stub
			XMLElement elem = new XMLElement();
		    
		    elem.name = qName;
		    
		    for (int i = 0; i < attributes.getLength(); i++) {
				elem.setAttribute(attributes.getQName(i), attributes.getValue(i));
			}
		    
		    XMLElement p = XMLElement.this.stack.getLast();
		    
		    p.addChildElement(elem);
		    
		    XMLElement.this.stack.add(elem);
		}
		
		
		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			// TODO Auto-generated method stub
			if (XMLElement.this.stack.size()>0) {
				XMLElement.this.stack.removeLast();
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			// TODO Auto-generated method stub
			try {
				XMLElement element = XMLElement.this.stack.getLast();
				StringBuilder sb = new StringBuilder();
				
				sb.append(element.text);
				sb.append(ch, start, length);
				
				element.text = sb.toString().trim();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
}
