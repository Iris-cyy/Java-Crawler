package hw4;

import java.io.*;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class Main {
	private static String bibPath = "anthology.bib"; //bib�ļ�λ��
	private static String pdfPath = "./file/"; //pdf�ļ��洢λ��
	private static String indexPath = "./index"; //�����洢λ��
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
		
		Essay essay; //�洢���׵������Ϣ
		Vector<String> infoType = null; //�洢�ֶ���
		Vector<String> infoValue = null;  //�洢�ֶ�����
		
		String input = "";
		String url = ""; //����Ԫ��������
		String pdf_url = ""; //����pdf�ĵ�ַ
		String id = ""; //anthology id
		String fileName = ""; //���ص��ļ���

		int startNum = (45 - 1) * 500 + 1; //��ʼ���ص�λ��
		int endNum = startNum + 500; //�������ص�λ��
		
		String queryField = ""; //��ѯ���ֶ�
		String queryStr = ""; //��ѯ�Ĺؼ���
		String infot = ""; //������
		String infov = ""; //��������
		
		int errnum = 0; //����ʧ�ܵĴ���
		boolean isauthor = false; //��ǰ���Ƿ�Ϊ����author����
		String author = ""; //����
		
		Document document = null;
		Elements content;

		//�ӳ����ʳ�ʱ��ʱ����ֵ
		System.setProperty("sun.net.client.defaultConnectTimeout", String.valueOf(1000000));
		System.setProperty("sun.net.client.defaultReadTimeout", String.valueOf(1000000)); 
		
		while(scanner.hasNext()) {
			input = scanner.nextLine();
			//�м䲿������
			if(input.charAt(0)!='@' && input.charAt(0)!='}') {
				//��ȡtitle��Ϣ
				if(input.length()>5 && input.trim().substring(0, 5).equals("title")) {
					infoType.add("title");
					infoValue.add(input.trim().substring(9, input.trim().length()-2));
				}
				//��ȡauthor��Ϣ
				if(input.length()>6 && input.trim().substring(0, 6).equals("author")) {
					isauthor = true;
				}
				//����ж��author�����ָ��ڲ�ͬ���У�����ϲ���ͬһ���ֶ���
				if(isauthor) {
					author += " " + input.trim();
					if(input.trim().charAt(input.trim().length()-1) == ',') {
						infoType.add("author");
						infoValue.add(author.trim().substring(10, author.trim().length()-2));
						isauthor = false;
						author = "";
					}
				}
				//��ȡ�������ӡ�pdf�������ӡ��ļ�����anthology id
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
			//@��ʾ������������������һ���µ�����
			if(input.charAt(0) == '@') {
				infoType = new Vector<String>();
				infoValue = new Vector<String>();
			}
			
			//��ʾ��ǰ�ļ���Ϣ����
			if(input.charAt(0) == '}') {
				cnt++;
				if(cnt >= startCollect) {
					System.out.println("Collecting........" + cnt +"/53745");
	
					if(url.startsWith("http://doi.org") || url.startsWith("https://doi.org") || url.endsWith(".pdf")) {
						pdf_url = url;
						if(cnt >= startNum && cnt < endNum) {
							try {
								//����pdf�ĵ�������������pdf�ļ�
								Download.downLoadByUrl(pdf_url, fileName, pdfPath);
							} catch (IOException e) {
								//�������ʧ�ܣ��׳��쳣
								System.out.println("Current cnt = " + cnt);
								e.printStackTrace();
							}
							System.out.println("Downloaded: " + (cnt-startNum+1) + "/500");
						}
					}else {
						while(true) {
							try {
								//��ȡ��������ҳ�������
								document = Jsoup.connect(url).get();
								errnum = 0;
								break;
							} catch (IOException e) {
								//������ӳ����������20�Σ�ͣ��1s֮���ٴγ�������
								//������20�Σ��׳��쳣��Ϣ����ֹ����
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
						
						//ѡ����ҳ����Ҫ����Ϣ���ڵ�λ��
						content = document.select(".col-lg-10.order-2");
						//��ȡ��ϸ��Ϣ
						for(int i=1; !content.select("dt:nth-child(" + i + ")").text().trim().equals(""); i+=2){
							infot = content.select("dt:nth-child(" + i + ")").text();
							infov = content.select("dd:nth-child(" + (i+1) + ")").text();
							infoType.add(infot.substring(0, infot.length()-1).toLowerCase());
							infoValue.add(infov);
						}
						//��ȡժҪ
						if(content.select("div > div").hasText()) {
							infoType.add("abstract");
							infov = content.select("div > div").text().substring(1).trim();
							infoValue.add(infov);
						}
						essay = new Essay(infoType, infoValue);
						
						//��������
						SearchEngine.createIndex(indexPath, essay);
						
						//������������Ҫ���صĲ�����
						if(cnt >= startNum && cnt <= endNum) {
							try {
								//����pdf�ĵ�������������pdf�ļ�
								Download.downLoadByUrl(pdf_url, fileName, pdfPath);
							} catch (IOException e) {
								//�������ʧ�ܣ��׳��쳣
								System.out.println("Current cnt = " + cnt);
								e.printStackTrace();
							}
							System.out.println("Downloaded: " + (cnt-startNum+1) + "/500");
						}
					}
				}
			}
		}
		
		//����������ֶ����͹ؼ��ʽ��в���
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
