/***
 * This file 
 */
package RFID;

public class ICNPParams {
	
	boolean flag_key_tag = true; //是否为关键标签丢失识别。
	
	double alpha;//false positive rate, |RA-RT|/|RT| where RT is the ground truth result, RA is the result returned by the algorithm
	int nTaginsys;//total number of tags in the system
	double gama;//the ratio of search result to the total number of tags in the system
				//defined as |K intersec M|/|M|, default value set at 0.1
	double eta;//Parameter eta: the ratio of search result to the total number of searchign tags
				//defined as |K intersec M|/|K|, default value set at 0.5
	int nSearchinsys;
	int nSearchoutsys;
	int nsearch;//number of tags in the search set
	int width=-1, length = -1;//the length and width of the deployment area, indicates system scale
	String readerPtn = "regular";//how the readers are deployed: "regular" means that readers are deployed in a grid
								//like topology, while "random" means that the readers are randomly scattered
	String searchTgptn = "random";//how the tags to be searched are distributed: "random" means that search tags
								  //are uniformly distributed in the system, while "crowd" means that search tags 
								  //are closely distributed in a special part of the system, e.g., the center 
	double rcom, rinfer; //interference range of reader
	double p;//the ratio of searching tag number to the number of tags in the local reader region
	int nreader;
	int kstop;
	int delta;
	double itspreq = 0.01;
	String paraname = null;
	static double avglocalsize;
	
	void set_flag_key_tag(boolean flag){
		flag_key_tag = flag;
	}
	public  ICNPParams(){
		setDefault();	//调用setDefault()
	}
	void setDefault(){
		alpha = 0.05;
		nTaginsys = 50000;
		eta = 0.2;
		width = 100;
		length = 100;
		rcom = 10;
		rinfer = 1.5*rcom;
		nsearch = 2000;
		nSearchinsys = (int)(nsearch*eta);
		nSearchoutsys = nsearch - nSearchinsys;
	}
	void setnearch(int ns){
		nsearch = ns;
		nSearchinsys = (int)(nsearch*eta);
		nSearchoutsys = nsearch - nSearchinsys;
	}
	void setrcom(double r){
		rcom = r;
		rinfer = rcom*1.5;
	}
	void seteta(double e){
		eta = e;
		nSearchinsys = (int)(nsearch*eta);
		nSearchoutsys = nsearch - nSearchinsys;
	}
	String getPrefix(){
		return nreader + "\t" + delta + "\t" + kstop + "\t" + itspreq + "\t"+ nTaginsys + "\t" + nsearch + "\t" + nSearchinsys + "\t" + nSearchoutsys + "\t" + eta + "\t" + alpha + "\t" + avglocalsize;
	}
}
