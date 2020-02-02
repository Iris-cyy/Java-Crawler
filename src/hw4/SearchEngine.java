package hw4;

import java.io.*;
import java.util.Vector;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class SearchEngine {
	public static void createIndex(String filePath, Essay essay){
		File f=new File(filePath);
		IndexWriter iwr=null;
		try {
			Directory dir=FSDirectory.open(f);
			Analyzer analyzer = new IKAnalyzer();

			IndexWriterConfig conf=new IndexWriterConfig(Version.LUCENE_4_10_0,analyzer);
			iwr=new IndexWriter(dir,conf);//建立IndexWriter。固定套路

			Document doc = getDocument(essay.getInfoType(), essay.getInfoValue());
			iwr.addDocument(doc);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			iwr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static Document getDocument(Vector<String> infoType, Vector<String> infoValue){
		//doc中内容由field构成，在检索过程中，Lucene会按照指定的Field依次搜索每个document的该项field是否符合要求。
		Document doc=new Document();
		
		for(int i=0; i<infoType.size(); i++) {
			Field f = new TextField(infoType.get(i), infoValue.get(i), Field.Store.YES);
			doc.add(f);
		}
		return doc;
		
	}
	
	public static void search(String filePath, String queryStr, String queryField){
		File f=new File(filePath);
		try {
			IndexSearcher searcher=new IndexSearcher(DirectoryReader.open(FSDirectory.open(f)));
			Analyzer analyzer = new IKAnalyzer();
			//指定field为“name”，Lucene会按照关键词搜索每个doc中的name。
			QueryParser parser = new QueryParser(Version.LUCENE_4_10_0, queryField, analyzer);
			
			Query query=parser.parse(queryStr);
			TopDocs hits=searcher.search(query,10);//前面几行代码也是固定套路，使用时直接改field和关键词即可
			for(ScoreDoc doc:hits.scoreDocs){
				Document d=searcher.doc(doc.doc);
				System.out.println("Title: " + d.get("title"));
				System.out.println("Author: " + d.get("author"));
				System.out.println("URL: " + d.get("url"));
			}
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}
}
