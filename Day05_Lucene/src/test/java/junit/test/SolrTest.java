package junit.test;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

public class SolrTest {
	
	private SolrServer server = new HttpSolrServer("http://192.168.70.132:8080/solr/collection1");
	
	@Test
	public void testHighLightQuery() throws SolrServerException {
		
		SolrQuery query = new SolrQuery();
		
		query.setQuery("apple");
		query.set("df", "key_words");
		
		//开启高亮显示功能
		query.setHighlight(true);
		
		//添加要设置高亮效果的字段
		query.addHighlightField("song_name");
		query.addHighlightField("song_singer");
		query.addHighlightField("song_lyric");
		
		//设置高亮显示效果的前后HTML标签
		//<span style="color:red">深圳</span>
		query.setHighlightSimplePre("<span style=\"color:red\">");
		query.setHighlightSimplePost("</span>");
		
		//执行查询
		QueryResponse response = server.query(query);
		
		//声明一个集合用来存储返回给页面的最终数据
		//Map<id,Map<fieldName,fieldValue>>
		Map<String,Map<String,Object>> finalDataMap = new HashMap<>();
		
		//从查询结果中获取常规的不带高亮效果的数据
		SolrDocumentList results = response.getResults();
		for (SolrDocument solrDocument : results) {
			
			//获取当前文档的id
			String id = solrDocument.getFieldValue("id") + "";
			
			//根据id从finalDataMap中尝试获取具体记录数据的Map：recordMap
			Map<String, Object> recordMap = finalDataMap.get(id);
			if(recordMap == null) {
				//如果recordMap为空则创建一个新的对象
				recordMap = new HashMap<>();
				//把新创建的recordMap存入finalDataMap
				finalDataMap.put(id, recordMap);
			}
			
			Collection<String> fieldNames = solrDocument.getFieldNames();
			for (String fieldName : fieldNames) {
				
				//排除_version_字段
				if("_version_".equals(fieldName)) {
					continue ;
				}
				
				Object fieldValue = solrDocument.getFieldValue(fieldName);
				
				//把当前字段名和字段值存入
				recordMap.put(fieldName, fieldValue);
			}
		}
		
		//从查询结果中获取常规的带高亮效果的数据
		//Map<文档id,Map<字段名称,[加了高亮效果的字段值]>>
		Map<String, Map<String, List<String>>> highlightingMap = response.getHighlighting();
		
		//执行高亮数据对常规数据的叠加操作
		for (SolrDocument solrDocument : results) {
			
			Collection<String> fieldNames = solrDocument.getFieldNames();
			for (String fieldName : fieldNames) {
				
				//获取当前文档的id值
				String id = solrDocument.getFieldValue("id") + "";
				
				//根据id值从高亮Map中获取具体数据
				//Map<字段名称,[加了高亮效果的字段值]>
				Map<String, List<String>> docHighLightMap = highlightingMap.get(id);
				
				//根据字段名称获取“[加了高亮效果的字段值]”
				List<String> highLightList = docHighLightMap.get(fieldName);
				
				//※注意：highLightList不一定是有效的List
				if(highLightList != null && highLightList.size() > 0) {
					
					//执行叠加替换操作
					//i.使用id从finalDataMap中获取对应的recordMap
					Map<String, Object> recordMap = finalDataMap.get(id);
					
					//ii.获取highLightList中的字段值（加了高亮效果的值）
					String highLightValue = highLightList.get(0);
					
					//iii.执行替换
					recordMap.put(fieldName, highLightValue);
					
				}
				
			}
			
		}
		
		System.out.println();
	}
	
	@Test
	public void testSimpleQuery() throws SolrServerException {
		//1.创建SolrQuery对象
		SolrQuery solrQuery = new SolrQuery();
		
		//2.设置查询关键词
		solrQuery.setQuery("apple");
		
		//3.设置默认查询的字段
		//df：default field默认字段
		solrQuery.set("df", "key_words");
		
		//4.执行查询
		QueryResponse response = server.query(solrQuery);
		
		//5.解析查询结果
		SolrDocumentList results = response.getResults();
		
		//6.查询结果数量
		long numFound = results.getNumFound();
		System.out.println("查询结果数量="+numFound);
		
		//7.遍历SolrDocumentList
		for (SolrDocument solrDocument : results) {
			
			Collection<String> fieldNames = solrDocument.getFieldNames();
			for (String fieldName : fieldNames) {
				Object fieldValue = solrDocument.getFieldValue(fieldName);
				System.out.println(fieldName + "=" + fieldValue);
			}
			
		}
		
	}
	
	@Test
	public void testDeleteById() throws SolrServerException, IOException {
		server.deleteById("11");
		server.commit();
	}
	
	@Test
	public void testDeleteByQueryCondition() throws SolrServerException, IOException {
		server.deleteByQuery("song_name:banana");
		server.commit();
	}
	
	@Test
	public void testAddDocument() throws SolrServerException, IOException {
		
		//1.创建Solr的文档对象
		SolrInputDocument document = new SolrInputDocument();
		
		//2.在文档中添加字段的值
		//注意：Solr的文档是必须有一个id的
		document.addField("id", 11);
		document.addField("song_name", "会被删除的歌记录banana");
		document.addField("song_singer", "红红的apple");
		document.addField("song_lyric", "送给你的小小的歌词");
		
		//3.添加文档
		server.add(document);
		
		//4.提交：针对索引库的添加、删除操作必须提交，否则不生效
		server.commit();
	}

}
