package RFID;
import java.util.*;
public class Subelement {
	Vector<Reader> rds = new Vector<Reader>();
	private static int count = 0;
	String sig = null;
	int id;
	public Subelement(){
		count++;
		id = count;
	}
	public Subelement(String psig){
		count++;
		id = count;
		sig = psig;
	}
	public void setSig(String psig){
		sig = psig;
	}
	public void addReader(Reader rd){
		rds.add(rd);
	}
	
}
