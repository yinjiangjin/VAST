/***
 * This file 
 */
package RFID;

public class Params {
	
	boolean flag_key_missing = true; //是否为关键标签丢失识别。
	
	int []EachCategoryNumArr_ser;//每个类别的标签数,服务器
	int []EachCategoryNumArr_sys;//每个类别的标签数,系统下
	int categoryNum = 2;  //KEY Missing
	
	int []Each_KEY_noKEY_NumArr_ser;//KEY和noKEY标签数,服务器.
	int []Each_KEY_noKEY_NumArr_sys;//KEY和noKEY标签数,系统下
	
	
	int num_KEYtags_ser; //KEYMissing
	int num_nonKEYtags_ser;
	
	int ntarget;
	int nnontarget;
	int fingerLength=0;
	//int infLengthPara = 64; // 信息的默认长度
	
	//PMI
	int nTaginserver; //服务器标签
	int nMissingNum;
	int nTaginsys;//total number of tags in the system
	
	//PMI
	//int groupNum = 50; 
	//int[] groupNumArr;
	int categoryBitLength = 32; //类别所占比特长度，默认32比特。
	boolean CIDRandomGenerate = false;
	
	//public boolean CIDRandomGenerate;
	
	double alpha;//false positive rate, |RA-RT|/|RT| where RT is the ground truth result, RA is the result returned by the algorithm
	
	double gama;//the ratio of search result to the total number of tags in the system
				//defined as |K intersec M|/|M|, default value set at 0.1
	double eta;//Parameter eta: the ratio of search result to the total number of searchign tags
				//defined as |K intersec M|/|K|, default value set at 0.5
	double missingRate;
	
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
	public  Params(){
		setDefault();	//调用setDefault()
	}
	void setDefault(){//对于PMI, 函数没啥用
		
		alpha = 0.05; //假阳性
		missingRate = 0.5;  //丢失比率 默认为0.5
		width = 100; 
		length = 100;
		rcom = 10;
		rinfer = 1.5*rcom;
		//nsearch = 2000;  //待搜索标签
		//nSearchinsys = (int)(nsearch*eta);  //大概能够找到的数目
		//nSearchoutsys = nsearch - nSearchinsys;  
	}
	
	void set_flag_key_missing(boolean flag){
		flag_key_missing = flag;
	}
	
	 void setServerTagNum(int nserver){//设置系统标签数目
			
			nTaginserver = nserver; //系统标签数目
	}
		
	  void setSystenTagNum(int nsys){//设置系统标签数目
			
			nTaginsys = nsys; //系统标签数目
		}
	
    void setntargetAndfingerLength(int nsys,int targetnumber){
		
		nTaginsys = nsys; //系统标签数目
		ntarget = targetnumber;
		nnontarget = nTaginsys - ntarget;
		fingerLength = (int)( 6 - (Math.log((double)(ntarget)/nnontarget)/Math.log(2))); //指纹长度
		//System.out.println("fingerLength:"+fingerLength);
	}
	
	void setEachCategoryNum_server_Uniform(int nserver,int categoryNum){//根据服务其总标签数，均匀设置每个类别的标签数。
		
		EachCategoryNumArr_ser = new int[categoryNum];
		int avgNum = nserver/categoryNum;
		//int remainsNum = nserver - avgNum*categoryNum; //不能平均分配标签，所剩下的标签数。
		System.out.print("EachCategoryNumArr: ");
		for (int i = 0; i < EachCategoryNumArr_ser.length; i++) {
			EachCategoryNumArr_ser[i] = avgNum;
			System.out.print(EachCategoryNumArr_ser[i]+" ");
		}
		
	}
	
	void setEachCategoryNum_system_Uniform(int nsystem,int categoryNum){//根据服务其总标签数，均匀设置每个类别的标签数。
		
		EachCategoryNumArr_sys = new int[categoryNum];
		int avgNum = nsystem/categoryNum;
		//int remainsNum = nserver - avgNum*categoryNum; //不能平均分配标签，所剩下的标签数。
		System.out.print("EachCategoryNumArr: ");
		for (int i = 0; i < EachCategoryNumArr_sys.length; i++) {
			EachCategoryNumArr_sys[i] = avgNum;
			System.out.print(EachCategoryNumArr_sys[i]+" ");
		}
		//groupNumArr[ groupNum-1]=groupNumArr[ groupNum-1]-100;
		
	}
	
	//
	void setEachCategoryNum_KEY_noKEY_system(int[] categoryNum_KEY_noKEY_Arr, double missingratio){
		
		Each_KEY_noKEY_NumArr_sys = new int[categoryNum];
		for (int i = 0; i < Each_KEY_noKEY_NumArr_sys.length; i++) {
			Each_KEY_noKEY_NumArr_sys[i] = (int) ( categoryNum_KEY_noKEY_Arr[i]*(1-missingratio) ); //服务器标签数目 乘以丢失标签比例
			System.out.print("system_KEY_noKEY: "+Each_KEY_noKEY_NumArr_sys[i]+" ");
		}
		nTaginsys  = Each_KEY_noKEY_NumArr_sys[0] + Each_KEY_noKEY_NumArr_sys[1];
	}
	
	//KEY missing
	void setEachCategoryNum_server_KEY_noKEY(int[] categoryNum_KEY_noKEY_Arr){
		
		num_KEYtags_ser = categoryNum_KEY_noKEY_Arr[0];
		num_nonKEYtags_ser = categoryNum_KEY_noKEY_Arr[1];
		
		Each_KEY_noKEY_NumArr_ser = new int[categoryNum];
		for (int i = 0; i < Each_KEY_noKEY_NumArr_ser.length; i++) {
			Each_KEY_noKEY_NumArr_ser[i] = categoryNum_KEY_noKEY_Arr[i];
			System.out.print("server_KEY_noKEY: "+Each_KEY_noKEY_NumArr_ser[i]+" ");
		}
		ntarget = Each_KEY_noKEY_NumArr_ser[0];
		nnontarget = Each_KEY_noKEY_NumArr_ser[1];
		
	}
	
	
	/*void setEachCategoryNum_server_Gauss(int[] categoryNum_Arr){//根据服务其总标签数，高斯gauss分布设置每个类别的标签数。
		
		EachCategoryNumArr_ser = new int[categoryNum];
		for (int i = 0; i < EachCategoryNumArr_ser.length; i++) {
			EachCategoryNumArr_ser[i] = categoryNum_Arr[i];
			System.out.print(EachCategoryNumArr_ser[i]+" ");
		}
	}*/
	
	/**
	 * 系统下每个类别的标签数，以及 求和得出系统标签数目
	 * @param categoryNum_Arr
	 * @param missingRatio
	 */
	/*void setEachCategoryNum_system_Gauss(int[] categoryNum_Arr, double missingRatio){//根据服务其总标签数，均匀设置每个类别的标签数。
		
		EachCategoryNumArr_sys = new int[categoryNum];

		for (int i = 0; i < EachCategoryNumArr_sys.length; i++) {
			EachCategoryNumArr_sys[i] = (int) (categoryNum_Arr[i]*missingRatio);//系统下每个类别的标签数
			System.out.print(EachCategoryNumArr_sys[i]+" ");
		}
		nTaginsys =0;
		for (int i = 0; i < EachCategoryNumArr_sys.length; i++) {
			nTaginsys +=EachCategoryNumArr_sys[i];//计算出系统的总标签数目
		}
		//groupNumArr[ groupNum-1]=groupNumArr[ groupNum-1]-100;
		
	}*/
  
//	void setGroupNums(int[] groupNumAs){
//		
//		groupNumArr = new int[groupNum];
//		for (int i = 0; i < groupNumArr.length; i++) {
//			groupNumArr[i] =groupNumAs[i];
//			System.out.println("groupNumArr[i]"+groupNumArr[i]);
//		}
//		
//	}
	
	void setntargetAndfingerLength(int nsys,double ratio){
		
		nTaginsys = nsys; //系统标签数目
		ntarget = (int) (nTaginsys*ratio);
		nnontarget = nTaginsys - ntarget;
		fingerLength = (int)( 6 - (Math.log((double)(ntarget)/nnontarget)/Math.log(2))); //指纹长度
		//System.out.println("fingerLength:"+fingerLength);
	}
	
	void setntargetAndfingerLength(int nt){
		
	
		ntarget = nt;
		nnontarget = nTaginsys - ntarget;
		fingerLength = (int)( 6 - (Math.log((double)(ntarget)/nnontarget)/Math.log(2))); //指纹长度
		//System.out.println("fingerLength:"+fingerLength);
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
	String getPrefix2(){
		return nTaginserver +"\t"+ntarget+"\t"+ nnontarget +"\t sysNum: " +"\t" +   nTaginsys +  "\t MissingNum: "+"\t" + nMissingNum +  "\t";
	}
	
	String getPrefix(){
		return nreader + "\t" + delta + "\t" + kstop + "\t" + itspreq + "\t"+ nTaginsys + "\t" + nsearch + "\t" + nSearchinsys + "\t" + nSearchoutsys + "\t" + eta + "\t" + alpha + "\t" + avglocalsize;
	}
}
