package eg.edu.alexu.csd.filestructure.btree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.RuntimeErrorException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class searchEngine implements ISearchEngine{
	private IBTree<String,List<ISearchResult>>tree;
	private int t;
	searchEngine(int t){
		tree=new BTree<>(t);
		this.t=t;
	}
 
	public void indexWebPage(String filePath) {
		if(filePath==null||filePath=="") {
			throw new RuntimeErrorException(null);
		}
		File file = new File(filePath);
		if(!file.exists()) {
			return;
		}
		
		try {
			DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
			DocumentBuilder builder=factory.newDocumentBuilder();
			Document doc=builder.parse(filePath);
			doc.getDocumentElement().normalize();
			NodeList docList=doc.getElementsByTagName("doc");
			for(int i=0;i<docList.getLength();i++) {   //loop for strings
				Node p=docList.item(i);
				if(p.getNodeType()==Node.ELEMENT_NODE) {
					Element st=(Element) p;
					Map<String,Integer>map=new HashMap<String, Integer>();
					String s=st.getTextContent();
					//s=s.replaceAll("\\s+"," ");
					String[]words=s.split("\\s+");
					List<String>list=new ArrayList<>();
					list=Arrays.asList(words);
					for(int j=0;j<words.length;j++) {   //loop for all words of the string
						int c=Collections.frequency(list,words[j]);
						if(map.containsKey(words[j].toLowerCase())) {
							
						}
						else {
							map.put(words[j].toLowerCase(),c);
							ISearchResult result=new SearchResult(st.getAttribute("id"),c);
							if(tree.search(words[j].toLowerCase())==null) {   //word doesn't exist in tree
								List<ISearchResult> newlist=new ArrayList<>();
								newlist.add(result);
								tree.insert(words[j].toLowerCase(),newlist);
							}
							else {
								tree.search(words[j].toLowerCase()).add(result);
							}
						}
					}
					
				}
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

	@Override
	public void indexDirectory(String directoryPath) {
		if(directoryPath==null||directoryPath=="") {
			throw new RuntimeErrorException(null);
		}
		File file = new File(directoryPath);
		if(file.exists()) {
		for (File childFile : file.listFiles()) {
			String s = childFile.toString();
				indexWebPage(s);
			
		}
		}
	}
	

	@Override
	public void deleteWebPage(String filePath) {
		if(filePath==null||filePath=="") {
			throw new RuntimeErrorException(null);
		}
		File file = new File(filePath);
		if(file.exists()) {
			try {
				DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
				DocumentBuilder builder=factory.newDocumentBuilder();
				Document doc=builder.parse(filePath);
				doc.getDocumentElement().normalize();
				NodeList docList=doc.getElementsByTagName("doc");
				for(int i=0;i<docList.getLength();i++) {   //loop for strings
					Node p=docList.item(i);
					if(p.getNodeType()==Node.ELEMENT_NODE) {
						Element st=(Element) p;
						String s=st.getTextContent();
						String[]words=s.split("\\s+");
						List<String>list=new ArrayList<>();
						list=Arrays.asList(words);
						Set<String> hashSet = new HashSet<String>();
						hashSet.addAll(list);
						
						for(String word: list) {
							List<ISearchResult> result = tree.search(word);
							if(result==null) {
							}
							else {
								for(int w = 0; w < result.size();w++) {
									if(result.get(w).getId().equals(st.getAttribute("id"))) {
										tree.search(word).remove(w);
									}
								}
								if(tree.search(word).size() == 0) {
									tree.delete(word);
								}
								
								
							}
						}
						
					}
				}
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	

	@Override
	public List<ISearchResult> searchByWordWithRanking(String word) {
		if(word==null) {
			throw new RuntimeErrorException(null);
		}
		if(word=="") {
			return new ArrayList<>();
		}
		List<ISearchResult>list= tree.search(word.toLowerCase());
		if(list==null) {
			return new ArrayList<>();
		}
		return list;
	}
	

	@Override
	public List<ISearchResult> searchByMultipleWordWithRanking(String sentence) {
		if(sentence==null) {
			throw new RuntimeErrorException(null);
		}
		if(sentence=="") {
			return new ArrayList<>();
		}
		if(sentence.charAt(0) == ' ') {
			 sentence = sentence.substring(1, sentence.length());
		 }
		boolean flag=true;
		String[] words = (sentence).split("\\s+");
		 List<ISearchResult> result = tree.search(words[0].toLowerCase());
		 if(result==null) {
			 return new ArrayList<>();
		 }
		 for(int i=1;i<words.length;i++) {
			 List<ISearchResult> list = tree.search(words[i].toLowerCase());
			 if(list==null) {
				 return new ArrayList<>();
			 }
			
		 }
		 return result;
	}

}
