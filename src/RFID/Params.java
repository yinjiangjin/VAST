/***
 * This file 
 */
package RFID;

public class Params {
	
	boolean flag_key_missing = true; //�Ƿ�Ϊ�ؼ���ǩ��ʧʶ��
	
	int []EachCategoryNumArr_ser;//ÿ�����ı�ǩ��,������
	int []EachCategoryNumArr_sys;//ÿ�����ı�ǩ��,ϵͳ��
	int categoryNum = 2;  //KEY Missing
	
	int []Each_KEY_noKEY_NumArr_ser;//KEY��noKEY��ǩ��,������.
	int []Each_KEY_noKEY_NumArr_sys;//KEY��noKEY��ǩ��,ϵͳ��
	
	
	int num_KEYtags_ser; //KEYMissing
	int num_nonKEYtags_ser;
	
	int ntarget;
	int nnontarget;
	int fingerLength=0;
	//int infLengthPara = 64; // ��Ϣ��Ĭ�ϳ���
	
	//PMI
	int nTaginserver; //��������ǩ
	int nMissingNum;
	int nTaginsys;//total number of tags in the system
	
	//PMI
	//int groupNum = 50; 
	//int[] groupNumArr;
	int categoryBitLength = 32; //�����ռ���س��ȣ�Ĭ��32���ء�
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
		setDefault();	//����setDefault()
	}
	void setDefault(){//����PMI, ����ûɶ��
		
		alpha = 0.05; //������
		missingRate = 0.5;  //��ʧ���� Ĭ��Ϊ0.5
		width = 100; 
		length = 100;
		rcom = 10;
		rinfer = 1.5*rcom;
		//nsearch = 2000;  //��������ǩ
		//nSearchinsys = (int)(nsearch*eta);  //����ܹ��ҵ�����Ŀ
		//nSearchoutsys = nsearch - nSearchinsys;  
	}
	
	void set_flag_key_missing(boolean flag){
		flag_key_missing = flag;
	}
	
	 void setServerTagNum(int nserver){//����ϵͳ��ǩ��Ŀ
			
			nTaginserver = nserver; //ϵͳ��ǩ��Ŀ
	}
		
	  void setSystenTagNum(int nsys){//����ϵͳ��ǩ��Ŀ
			
			nTaginsys = nsys; //ϵͳ��ǩ��Ŀ
		}
	
    void setntargetAndfingerLength(int nsys,int targetnumber){
		
		nTaginsys = nsys; //ϵͳ��ǩ��Ŀ
		ntarget = targetnumber;
		nnontarget = nTaginsys - ntarget;
		fingerLength = (int)( 6 - (Math.log((double)(ntarget)/nnontarget)/Math.log(2))); //ָ�Ƴ���
		//System.out.println("fingerLength:"+fingerLength);
	}
	
	void setEachCategoryNum_server_Uniform(int nserver,int categoryNum){//���ݷ������ܱ�ǩ������������ÿ�����ı�ǩ����
		
		EachCategoryNumArr_ser = new int[categoryNum];
		int avgNum = nserver/categoryNum;
		//int remainsNum = nserver - avgNum*categoryNum; //����ƽ�������ǩ����ʣ�µı�ǩ����
		System.out.print("EachCategoryNumArr: ");
		for (int i = 0; i < EachCategoryNumArr_ser.length; i++) {
			EachCategoryNumArr_ser[i] = avgNum;
			System.out.print(EachCategoryNumArr_ser[i]+" ");
		}
		
	}
	
	void setEachCategoryNum_system_Uniform(int nsystem,int categoryNum){//���ݷ������ܱ�ǩ������������ÿ�����ı�ǩ����
		
		EachCategoryNumArr_sys = new int[categoryNum];
		int avgNum = nsystem/categoryNum;
		//int remainsNum = nserver - avgNum*categoryNum; //����ƽ�������ǩ����ʣ�µı�ǩ����
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
			Each_KEY_noKEY_NumArr_sys[i] = (int) ( categoryNum_KEY_noKEY_Arr[i]*(1-missingratio) ); //��������ǩ��Ŀ ���Զ�ʧ��ǩ����
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
	
	
	/*void setEachCategoryNum_server_Gauss(int[] categoryNum_Arr){//���ݷ������ܱ�ǩ������˹gauss�ֲ�����ÿ�����ı�ǩ����
		
		EachCategoryNumArr_ser = new int[categoryNum];
		for (int i = 0; i < EachCategoryNumArr_ser.length; i++) {
			EachCategoryNumArr_ser[i] = categoryNum_Arr[i];
			System.out.print(EachCategoryNumArr_ser[i]+" ");
		}
	}*/
	
	/**
	 * ϵͳ��ÿ�����ı�ǩ�����Լ� ��͵ó�ϵͳ��ǩ��Ŀ
	 * @param categoryNum_Arr
	 * @param missingRatio
	 */
	/*void setEachCategoryNum_system_Gauss(int[] categoryNum_Arr, double missingRatio){//���ݷ������ܱ�ǩ������������ÿ�����ı�ǩ����
		
		EachCategoryNumArr_sys = new int[categoryNum];

		for (int i = 0; i < EachCategoryNumArr_sys.length; i++) {
			EachCategoryNumArr_sys[i] = (int) (categoryNum_Arr[i]*missingRatio);//ϵͳ��ÿ�����ı�ǩ��
			System.out.print(EachCategoryNumArr_sys[i]+" ");
		}
		nTaginsys =0;
		for (int i = 0; i < EachCategoryNumArr_sys.length; i++) {
			nTaginsys +=EachCategoryNumArr_sys[i];//�����ϵͳ���ܱ�ǩ��Ŀ
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
		
		nTaginsys = nsys; //ϵͳ��ǩ��Ŀ
		ntarget = (int) (nTaginsys*ratio);
		nnontarget = nTaginsys - ntarget;
		fingerLength = (int)( 6 - (Math.log((double)(ntarget)/nnontarget)/Math.log(2))); //ָ�Ƴ���
		//System.out.println("fingerLength:"+fingerLength);
	}
	
	void setntargetAndfingerLength(int nt){
		
	
		ntarget = nt;
		nnontarget = nTaginsys - ntarget;
		fingerLength = (int)( 6 - (Math.log((double)(ntarget)/nnontarget)/Math.log(2))); //ָ�Ƴ���
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
