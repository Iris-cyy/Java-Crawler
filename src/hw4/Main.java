package hw4;

import java.io.*;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class Main {
	private static String bibPath = "anthology.bib"; //bib文件位置
	private static String pdfPath = "./file/"; //pdf文件存储位置
	private static String indexPath = "./index"; //索引存储位置
	private static int cnt = 0;
	private static int startCollect = 22049;
	
	public static void main(String[] args) throws InterruptedException {
		FileReader bibFile = null;
		try {
			bibFile = new FileReader(bibPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		Scanner scanner = new Scanner(bibFile);
		Scanner wordScanner = new Scanner(System.in);
		
		Essay essay; //存储文献的相关信息
		Vector<String> infoType = null; //存储字段名
		Vector<String> infoValue = null;  //存储字段内容
		
		String input = "";
		String url = ""; //文献元数据链接
		String pdf_url = ""; //下载pdf的地址
		String id = ""; //anthology id
		String fileName = ""; //下载的文件名

		int startNum = (45 - 1) * 500 + 1; //开始下载的位置
		int endNum = startNum + 500; //结束下载的位置
		
		String queryField = ""; //查询的字段
		String queryStr = ""; //查询的关键词
		String infot = ""; //属性名
		String infov = ""; //属性内容
		
		int errnum = 0; //连接失败的次数
		boolean isauthor = false; //当前行是否为属于author属性
		String author = ""; //作者
		
		Document document = null;
		Elements content;

		//延长访问超时的时间阈值
		System.setProperty("sun.net.client.defaultConnectTimeout", String.valueOf(1000000));
		System.setProperty("sun.net.client.defaultReadTimeout", String.valueOf(1000000)); 
		
		while(scanner.hasNext()) {
			input = scanner.nextLine();
			//中间部分内容
			if(input.charAt(0)!='@' && input.charAt(0)!='}') {
				//获取title信息
				if(input.length()>5 && input.trim().substring(0, 5).equals("title")) {
					infoType.add("title");
					infoValue.add(input.trim().substring(9, input.trim().length()-2));
				}
				//获取author信息
				if(input.length()>6 && input.trim().substring(0, 6).equals("author")) {
					isauthor = true;
				}
				//如果有多个author，被分隔在不同行中，将其合并在同一个字段中
				if(isauthor) {
					author += " " + input.trim();
					if(input.trim().charAt(input.trim().length()-1) == ',') {
						infoType.add("author");
						infoValue.add(author.trim().substring(10, author.trim().length()-2));
						isauthor = false;
						author = "";
					}
				}
				//获取文献链接、pdf下载链接、文件名、anthology id
				if(input.length()>3 && input.trim().substring(0, 3).equals("url")) {
					url = input.trim().substring(7, input.trim().length()-2);
					if(url.startsWith("https://www.aclweb.org")) {
						id = url.substring(33, url.length());
						pdf_url = url + ".pdf";
					}else if(url.startsWith("http://www.lrec-conf.org")) {
						id = url.substring(50, url.length()-4);
						pdf_url = url;
					}else {
						id = cnt + "";
					}
					fileName = id + ".pdf";
				}
			}
			//@表示接下来部分内容属于一个新的文献
			if(input.charAt(0) == '@') {
				infoType = new Vector<String>();
				infoValue = new Vector<String>();
			}
			
			//表示当前文件信息结束
			if(input.charAt(0) == '}') {
				cnt++;
				if(cnt >= startCollect) {
					System.out.println("Collecting........" + cnt +"/53745");
	
					if(url.startsWith("http://doi.org") || url.startsWith("https://doi.org") || url.endsWith(".pdf")) {
						pdf_url = url;
						if(cnt >= startNum && cnt < endNum) {
							try {
								//根据pdf文档所在链接下载pdf文件
								Download.downLoadByUrl(pdf_url, fileName, pdfPath);
							} catch (IOException e) {
								//如果连接失败，抛出异常
								System.out.println("Current cnt = " + cnt);
								e.printStackTrace();
							}
							System.out.println("Downloaded: " + (cnt-startNum+1) + "/500");
						}
					}else {
						while(true) {
							try {
								//获取文献链接页面的内容
								document = Jsoup.connect(url).get();
								errnum = 0;
								break;
							} catch (IOException e) {
								//如果连接出错次数少于20次，停顿1s之后再次尝试连接
								//若多于20次，抛出异常信息并终止运行
								if(errnum < 20) {
									errnum++;
									Thread.sleep(1000);
								}else {
									System.out.println("Current cnt = " + cnt);
									e.printStackTrace();
									return;
								}
							}
						}
						
						//选择网页中需要的信息所在的位置
						content = document.select(".col-lg-10.order-2");
						//获取详细信息
						for(int i=1; !content.select("dt:nth-child(" + i + ")").text().trim().equals(""); i+=2){
							infot = content.select("dt:nth-child(" + i + ")").text();
							infov = content.select("dd:nth-child(" + (i+1) + ")").text();
							infoType.add(infot.substring(0, infot.length()-1).toLowerCase());
							infoValue.add(infov);
						}
						//获取摘要
						if(content.select("div > div").hasText()) {
							infoType.add("abstract");
							infov = content.select("div > div").text().substring(1).trim();
							infoValue.add(infov);
						}
						essay = new Essay(infoType, infoValue);
						
						//创建索引
						SearchEngine.createIndex(indexPath, essay);
						
						//如果编号在所需要下载的部分内
						if(cnt >= startNum && cnt <= endNum) {
							try {
								//根据pdf文档所在链接下载pdf文件
								Download.downLoadByUrl(pdf_url, fileName, pdfPath);
							} catch (IOException e) {
								//如果连接失败，抛出异常
								System.out.println("Current cnt = " + cnt);
								e.printStackTrace();
							}
							System.out.println("Downloaded: " + (cnt-startNum+1) + "/500");
						}
					}
				}
			}
		}
		
		//根据输入的字段名和关键词进行查找
		while(true) {
			System.out.print("Please input the field you want to search in (eg. title): ");
			if(wordScanner.hasNext()) {
				queryField = wordScanner.nextLine().toLowerCase().trim();
				if(queryField.equals("exit")) {
					break;
				}
			}
			System.out.print("Please input the key word you want to search: ");
			if(wordScanner.hasNext()) {
				queryStr = wordScanner.nextLine().toLowerCase().trim();
				if(queryStr.equals("exit")) {
					break;
				}
			}
			SearchEngine.search(indexPath, queryStr, queryField);
		}
	}
}
