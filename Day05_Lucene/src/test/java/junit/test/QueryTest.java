package junit.test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class QueryTest {
	
	@Test
	public void testQuery() throws IOException, ParseException {
		
		//1.声明索引库位置
		String path = "E:\\my_index";
		
		//2.创建File对象
		File file = new File(path);
		
		//3.读取索引库
		FSDirectory directory = FSDirectory.open(file);
		DirectoryReader reader = DirectoryReader.open(directory);
		
		//4.创建IndexSearcher
		IndexSearcher searcher = new IndexSearcher(reader);
		
		//5.声明查询的字段
		//String field = "song_singer";
		String[] fields = new String[]{"song_name","song_singer","song_album","song_lyric"};
		
		//6.创建分词器对象。注意：查询时候使用的分词器一定要和创建索引库时使用的分词器一致
		StandardAnalyzer analyzer = new StandardAnalyzer();
		
		//7.创建查询解析器对象
		//QueryParser queryParser = new QueryParser(field, analyzer);
		QueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
		
		//8.声明关键词
		String keywords = "lyric";
		
		//9.解析关键词
		Query query = queryParser.parse(keywords);
		
		//10.执行查询
		TopDocs docs = searcher.search(query, 10);
		
		//11.解析查询结果
		ScoreDoc[] scoreDocs = docs.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			//12.从scoreDoc对象中获取文档得分
			float score = scoreDoc.score;
			System.out.println("当前文档得分="+score);
			
			//13.从scoreDoc对象中获取文档id
			int docId = scoreDoc.doc;
			
			//14.根据文档的id查询对应的Document对象
			Document doc = searcher.doc(docId);
			
			//15.获取当前文档的各个字段的值
			Iterator<IndexableField> iterator = doc.iterator();
			while (iterator.hasNext()) {
				IndexableField indexableField = (IndexableField) iterator.next();
				String name = indexableField.name();
				String stringValue = indexableField.stringValue();
				
				System.out.println(name+"="+stringValue);
			}
			
			System.out.println();
			
		}
		
	}
	
	@Test
	public void testAnalyzer() throws IOException {
		
		String source = "Hello tom,do you have lunch.for album.你好汤姆，吃午饭了吗？我们去开黑吧！你来打野，我负责中单。";
		
		//1.创建具体分词器对象
		Analyzer analyzer = new IKAnalyzer();
		
		//2.调用tokenStream方法
		TokenStream tokenStream = analyzer.tokenStream("aaa",source);
		
		//3.设置指针回到开始位置
		tokenStream.reset();
		
		//4.设置偏移量对象
		OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
		
		//5.设置获取分词数据对象
		CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
		
		System.out.println(source);
		
		//6.遍历显示分词结果
		while(tokenStream.incrementToken()) {
			
			int startOffset = offsetAttribute.startOffset();
			int endOffset = offsetAttribute.endOffset();
			
			System.out.println(term+"["+startOffset+","+endOffset+"]");
			
		}
		
		analyzer.close();
		
	}
	
	@Test
	public void testCreateIndex() throws IOException {
		
		//1.声明一个String类型对象用来保存索引库路径
		String path = "E:\\my_index";
		
		//2.创建File对象
		File file = new File(path);
		
		//3.创建索引库目录对象
		FSDirectory directory = FSDirectory.open(file);
		
		//4.创建分词器对象
		Analyzer analyzer = new StandardAnalyzer();
		
		//5.创建“索引库写入器配置”对象
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);
		
		//6.创建索引库写入器对象
		IndexWriter writer = new IndexWriter(directory, config);
		
		//7.创建三个文档
		//①第一个文档
		Document document01 = new Document();
		
		//id：song_id
		IntField songId = new IntField("song_id", 5, Store.YES);
		
		//歌名：song_name
		//StringField[索引，不分词]/TextField[索引，分词]
		TextField songName = new TextField("song_name", "songName Hello my song", Store.YES);
		
		//歌手：song_singer
		//StringField[索引，不分词]/TextField[索引，分词]
		//TFBoy，S.H.E.，Jerry，Kate，Bob，Jerry&Kate
		TextField songSinger = new TextField("song_singer", "Jerry", Store.YES);
		
		//专辑：song_album
		TextField songAlbum = new TextField("song_album", "this is apple", Store.YES);
		
		//歌词：song_lyric
		//点击量：song_count
		TextField songLyric = new TextField("song_lyric", "this is a good lyric", Store.NO);
		
		document01.add(songId);
		document01.add(songName);
		document01.add(songSinger);
		document01.add(songAlbum);
		document01.add(songLyric);
		
		//②第二个文档
		Document document02 = new Document();
		
		//id：song_id
		songId = new IntField("song_id", 6, Store.YES);
		
		//歌名：song_name
		//StringField[索引，不分词]/TextField[索引，分词]
		songName = new TextField("song_name", "songName apple", Store.YES);
		
		//歌手：song_singer
		//StringField[索引，不分词]/TextField[索引，分词]
		//TFBoy，S.H.E.，Jerry，Kate，Bob，Jerry&Kate
		songSinger = new TextField("song_singer", "Bob", Store.YES);
		
		//专辑：song_album
		songAlbum = new TextField("song_album", "album banana", Store.YES);
		
		//歌词：song_lyric
		//点击量：song_count
		songLyric = new TextField("song_lyric", "lyric apple", Store.NO);
		
		document02.add(songId);
		document02.add(songName);
		document02.add(songSinger);
		document02.add(songAlbum);
		document02.add(songLyric);
		
		//③第个文档
		Document document03 = new Document();
		
		//id：song_id
		songId = new IntField("song_id", 7, Store.YES);
		
		//歌名：song_name
		//StringField[索引，不分词]/TextField[索引，分词]
		songName = new TextField("song_name", "songName pen", Store.YES);
		
		//歌手：song_singer
		//StringField[索引，不分词]/TextField[索引，分词]
		//TFBoy，S.H.E.，Jerry，Kate，Bob，Jerry&Kate
		songSinger = new TextField("song_singer", "apple", Store.YES);
		
		//专辑：song_album
		songAlbum = new TextField("song_album", "album pen", Store.YES);
		
		//歌词：song_lyric
		//点击量：song_count
		songLyric = new TextField("song_lyric", "happy pen", Store.NO);
		
		//Alt+Shift+A
		document03.add(songId);     
		document03.add(songName);   
		document03.add(songSinger); 
		document03.add(songAlbum);  
		document03.add(songLyric);  
		
		//8.将文档写入索引库
		writer.addDocument(document01);
		writer.addDocument(document02);
		writer.addDocument(document03);
		
		//9.提交
		writer.commit();
		
		//10.关闭
		writer.close();
	}

}
