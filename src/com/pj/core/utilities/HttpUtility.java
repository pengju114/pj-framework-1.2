/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pj.core.utilities;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



import com.pj.core.datamodel.DataWrapper;
import com.pj.core.managers.LogManager;

/**
 * Http返回数据处理助手
 * @author 陆振文[PENGJU]
 * 2012-7-27 10:40:10
 */
public class HttpUtility {
	private static final String NODE_NAME_LIST_ITEM="item";
    
    /**
     * XML数据处理
     * PENGJU
     * 2012-10-18 13:45:56
     * @param in
     * @return
     */
    public static DataWrapper parseXML(InputStream in){
    	DataWrapper wrapper=null;
        try {
            Document doc=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
            wrapper = new DataWrapper();
            DataWrapper parent  = new DataWrapper();
            parseNodeTree(parent,wrapper,doc.getDocumentElement());
            doc=null;
        } catch (Exception e) {
        	LogManager.trace(e);
        }
        
        return  wrapper;
    }
    
    
    private static void parseNodeTree(DataWrapper parent,DataWrapper element,Node node) {
		// TODO Auto-generated method stub
    	
		
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			
			NodeList list = node.getChildNodes();
			
			if (list!=null && list.getLength()>1) {
				for (int i = 0; i < list.getLength(); i++) {
					Node n = list.item(i);
					if (n.getNodeType()==Node.TEXT_NODE) {
						continue;
					}
					
					if (!isMetaItemAndFetch(n, element)) {
						DataWrapper childWrapper = new DataWrapper();
						parseNodeTree(element , childWrapper, n);
						
						if (NODE_NAME_LIST_ITEM.equalsIgnoreCase(n.getNodeName())) {
							addChildWrapper(parent, childWrapper, node.getNodeName());
						}else {
							addChildWrapper(element, childWrapper, n.getNodeName());
						}
					}
				}
			}
		}
	}
	
	private static boolean isMetaItemAndFetch(Node node,DataWrapper wrapper){
		NodeList children = node.getChildNodes();
		if (node.getNodeType()==Node.ELEMENT_NODE && children.getLength()<2) {
			Node child = children.item(0);
			boolean isMetaItem = child==null || (child.getNodeType()==Node.TEXT_NODE||child.getNodeType()==Node.CDATA_SECTION_NODE);
			if (isMetaItem && wrapper!=null) {
				String val = child==null?"":child.getNodeValue();
				if (val==null) {
					val = "";
				}
				wrapper.setObject(node.getNodeName(), val.trim());
			}
			return isMetaItem;
		}
		return false;
	}
    
    private static void addChildWrapper(DataWrapper parent,DataWrapper child,String key){
    	if (child.isEmpty()) {
			return;
		}
    	List<DataWrapper> array = parent.getList(key);
		if (array == null) {
			array = new ArrayList<DataWrapper>();
			parent.setObject(key, array);
		}
		array.add(child);
    }
    
    /**
     * JSON数据处理,数据跟对象如果不是JSONObject则把数据放在key为array的JSONObject里面
     * PENGJU
     * 2012-10-18 13:46:17
     * @param jsonString
     * @return
     */
    public static DataWrapper parseJSON(String jsonString){
    	DataWrapper wrapper=null;
    	try {
			JSONTokener jsonTokener=new JSONTokener(jsonString);
			Object object=jsonTokener.nextValue();
			
			if (object instanceof JSONArray) {
				JSONObject rect=new JSONObject();
				rect.put("array", object);
				object=rect;
			}
			
			if (object instanceof JSONObject) {
				wrapper=parseJSONObject((JSONObject)object);
			} else {
				throw new JSONException("parsing "+jsonString+" error!");
			}
		} catch (Exception e) {
			// TODO: handle exception
			LogManager.trace(e);
		}
    	return wrapper;
    }

	@SuppressWarnings("unchecked")
	private static DataWrapper parseJSONObject(JSONObject object) {
		// TODO Auto-generated method stub
		DataWrapper wrapper=new DataWrapper();
		
		Iterator<String> keys=object.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			try{
				Object o=object.get(key);
				
				if (o instanceof JSONObject) {
					wrapper.setObject(key, parseJSONObject((JSONObject)o));
				}else if (o instanceof JSONArray) {
					wrapper.setObject(key, parseJSONArray((JSONArray) o));
				}else {
					wrapper.setObject(key, o);
				}
			}catch (Exception e) {
				LogManager.trace(e);
			}
		}
		
		return wrapper;
	}
	
	private static List<DataWrapper> parseJSONArray(JSONArray object) {
		// TODO Auto-generated method stub
		ArrayList<DataWrapper> list=new ArrayList<DataWrapper>(object.length());
		for (int i = 0; i < object.length(); i++) {
			try {
				Object o=object.get(i);
				if (o instanceof JSONObject) {
					list.add(parseJSONObject((JSONObject)o));
				}else if (o instanceof JSONArray) {
					DataWrapper newWrapper=new DataWrapper();
					newWrapper.setObject("array", parseJSONArray((JSONArray)o));
					list.add(newWrapper);
				}else {
					throw new JSONException("skip "+o+" at index:"+i);
				}
			} catch (Exception e) {
				// TODO: handle exception
				LogManager.trace(e);
			}
		}
		return list;
	}
}
