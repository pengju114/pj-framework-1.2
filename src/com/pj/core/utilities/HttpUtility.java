/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pj.core.utilities;

import java.io.ByteArrayInputStream;
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
    private static final String VALUE_NODE_NAME="init-param";
    private static final String VALUE_NODE_ATTRIBUTE_NAME="name";
    
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
            wrapper=parseValueNode(doc.getDocumentElement().getChildNodes());
            doc=null;
        } catch (Exception e) {
        	LogManager.trace(e);
        }
        
        return  wrapper;
    }
    
    /**
     * XML数据处理
     * PENGJU
     * 2012-10-18 13:45:56
     * @param in
     * @return
     */
    public static DataWrapper parseXML(String xml){
    	DataWrapper wrapper=null;
        try {
            Document doc=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
            wrapper=parseValueNode(doc.getDocumentElement().getChildNodes());
            doc=null;
        } catch (Exception e) {
        	LogManager.trace(e);
        }
        
        return  wrapper;
    }
    
    private static DataWrapper parseValueNode(NodeList list){
    	DataWrapper wrapper=new DataWrapper();
        for (int i = 0; i < list.getLength(); i++) {
        	
    		Node node = list.item(i);
            if (node.getNodeType()==Node.TEXT_NODE) {
                continue;
            }
            String nodeName=node.getNodeName();
            //元数据节点
            if (VALUE_NODE_NAME.equals(nodeName)) {
            	//key属性值
                String key=node.getAttributes().getNamedItem(VALUE_NODE_ATTRIBUTE_NAME).getNodeValue();
                Node fc=node.getFirstChild();
                String value=null;
                if (fc!=null) {
                	value=fc.getNodeValue().trim();
				}
                wrapper.setObject(key, value);
            	
            } else {
            	List<DataWrapper> l=wrapper.getList(nodeName);
                if (l==null) {
                    l=new ArrayList<DataWrapper>();
                    wrapper.setObject(nodeName, l);
                }
                l.add(parseValueNode(node.getChildNodes()));
            }
        }
        
        return wrapper;
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
		ArrayList<DataWrapper> list=new ArrayList<DataWrapper>();
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
