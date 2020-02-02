package hw4;

import java.util.*;

public class Essay {
//	private static String title;
//	private static 
//	int cnt = 0;
//	String input = "";
//	String title = "";
//	String author = "";
//	String booktitle = "";
//	String year = "";
//	String url = "";
	private Vector<String> infoType = new Vector<String>();
	private Vector<String> infoValue = new Vector<String>();

	Essay(Vector<String> type, Vector<String> value){
		setInfoType(type);
		setInfoValue(value);
	}

	public Vector<String> getInfoType() {
		return infoType;
	}

	public void setInfoType(Vector<String> infoType) {
		this.infoType = infoType;
	}

	public Vector<String> getInfoValue() {
		return infoValue;
	}

	public void setInfoValue(Vector<String> infoValue) {
		this.infoValue = infoValue;
	}
	
	public void print() {
		System.out.println("types:");
		for(int i=0; i<infoType.size(); i++) {
			System.out.println(infoType.get(i));
		}
		System.out.println("values:");
		for(int i=0; i<infoValue.size(); i++) {
			System.out.println(infoValue.get(i));
		}
	}
	
//	public static Vector<String> getType() {
//		return infoType;
//	}
//	 
//	public static Vector<String> getValue(){
//		return infoValue;
//	}
	
}
