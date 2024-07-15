package RFID;

import java.util.*;

/**
 * Identifying the Missing Tags in a Large RFID System - MobiHoc  2010 
 * @author Jiangjin
 *
 */
public class OneCategory_CR_MTI extends ICNPBasic {
	
	HashSet<Tag> tempTarget = new HashSet<Tag>(); //
	HashSet<Tag> tempNotarget = new HashSet<Tag>();
	HashSet<Tag> tempLocal = new HashSet<Tag>();
	
	HashSet<Tag> missingset = new HashSet<Tag>();

	//HashSet<int[]> tempLocal2 = new HashSet<int[]>();
	
//	int numCategory = categoryNum_Basic; //获得类别数目
//	int categoryBitLength = categoryBitNum; //得到父类的数目。
//	
//	int[] CategoryIDArr = new int[numCategory];
	int CR_MTI_lambda = 15;
	int CR_MTI_w = 34;
	
	
	public OneCategory_CR_MTI(){
		ptname = new String("oneCategory_CR_MTI");
	}
	
	double execute(){
		
		System.out.println("********start oneCategory_CR_MTI******** ");
	
		tcollection = 0;  
		TOTAL_bits= 0; //记录总的比特数
		
		
		while(serverset.size()>0){
			
			int ser_tagnum = serverset.size();
			int f = CR_MTI_optimalf(ser_tagnum);
		//	System.out.println("CR_MTI frame size: " +f);
			HashSet<Tag> tempIdentifiedSet = new HashSet<Tag>();
			
			tempIdentifiedSet.addAll(CR_MTI_pre_identify(f, serverset,localtagsset));
			double x = CR_MTI_tag_filtering(serverset,localtagsset,tempIdentifiedSet);

		}
		
		totalMissingNumber_identified = missingset.size(); //把丢失标签总数记录到basic类中
		System.out.println("missing tag: "+ missingset.size());
		

		System.out.println("****End oneCategory_CR_MTI***** tcollection******:"+ tcollection+"total bit:"+ TOTAL_bits);
		return tcollection;
	}
	
	
	


	/**
	 * CR_MTI frame size: Fig.(6) and Fig.(11) where lambda = 15;
	 * @param N  number of tag 
	 * @return f --- frame size
	 */
	public int CR_MTI_optimalf(int N_server){
		
		double t_id  = TimingScheme.t_id;
		double t_s = TimingScheme.t_s;
		double E = Math.E;
		
		//int CR_MTI_w = 34;
		//int lambda = 15;
		int optf = N_server/CR_MTI_lambda;
		if (optf<5){
			optf = 5 ;
		}
//		double []Arr_Tavg = new double[10+N_server]; //只是有10个值
//		
//		
//		for (int framesize = 0; framesize < Arr_Tavg.length; framesize++) {
//			double lambda = 1.0*N_server/framesize;
//			
//			double fenzi = 1;
//			double fenmu = 1;
//			Arr_Tavg[framesize] = fenzi/fenmu;
//		}
		
		
//		int optf = 5;
//		double MinAverage = Arr_Tavg[10];
//
//		for (int i = 5; i < Arr_Tavg.length; i++) {
//			if(Arr_Tavg[i]<MinAverage){
//				MinAverage = Arr_Tavg[i]; //记录最小值
//				optf = i;  //记录最小值对应索引(帧长)	
//			}
//		}
		
		return optf; //返回最优帧长
	}
	

	
	/**
	 *  CR_MTI protocol
	 *  identify missing and calculate time overhead slot by slot
	 *  
	 */
	public  HashSet<Tag> CR_MTI_pre_identify(int f, HashSet<Tag> ser_tagset, HashSet<Tag> systemtagset){
		
		int seed1 = rand.nextInt();
		int[] replyArrServer = getReply(f,seed1,ser_tagset);//expected reply vector when server tags is pre-mapped; 
		int[] Pre_replyArrSystem = getReply(f,seed1,systemtagset);
	
		tcollection+=TimingScheme.t_lambda; 
		
		//Time 1: CR_MTI:  count the total bit of Vm
		int totalbit_Vm = 0;
		for (int i = 0; i < replyArrServer.length; i++) {
			if(replyArrServer[i]>1){  //冲突时隙(不管是否能够协调)均用1比特表示
				totalbit_Vm += 1;
			}else{
				totalbit_Vm += 2; //空和单时隙用两比特表示
			}
		}
		tcollection += TimingScheme.t_id*totalbit_Vm/96.0;//广播Vm的时间
		TOTAL_bits += totalbit_Vm;
		
		HashSet<Tag> identifiedTags = new HashSet<Tag>(); //record the tags that can be identified 
		
		//1. CR_MTI: singleton 
		for(Tag t: ser_tagset){
			if(replyArrServer[t.slotidx]==1){
				identifiedTags.add(t);//identify the tags in the pre-mapped singleton slots
				tcollection += 0.1; //
				tcollection += TimingScheme.t_s;
				TOTAL_bits += 1; //传输1比特。
//				if(systemtagset.contains(t)){ //如果这个标签存在于系统下，也就是说阅读器收到回复
//					tcollection += TimingScheme.t_s;
//				}else {// 如果阅读器没有收到回复
//					tcollection += TimingScheme.t_e;
//				}
			}
		}
		
		//2. CR_MTI: Reconcile collisions。 
		int seed2 = rand.nextInt();
		
		HashSet<Tag> tempSet_ser = new HashSet<Tag>(); //server
		
		HashSet<Tag> tempSet_sys = new HashSet<Tag>(); //system
		
		for (int ii = 0; ii < replyArrServer.length; ii++) {
			if ( replyArrServer[ii] >1 ){ //collision slots
				tempSet_ser.clear();
				tempSet_ser.addAll(findTagSet(ii, ser_tagset));
				
				tempSet_sys.clear();
				tempSet_sys.addAll(findTagSet(ii, systemtagset));
				
				
				//frame size = CR_MTI_w, 冲突重协调by seed2
				int[] replyArrReconcile_ser = getReply2(CR_MTI_w,seed2,tempSet_ser);//重新回复在slotidx2上（注意不是slotidx1）
				
				//whether or not reconciled by seed2?
				boolean flag = true;
				for (int jj = 0; jj < replyArrReconcile_ser.length; jj++) {
					if( replyArrReconcile_ser[jj]>1 ){//如果依旧有冲突
						flag = false;
						break;
					}
				}
				
				if(flag){ //如果可以被重协调
					identifiedTags.addAll(tempSet_ser); //其中标签均可以被识别
					tempSet_ser.clear();
					//if(tempSet_sys.size()>0){//系统有标签回复
					tcollection += 0.1; //	
					tcollection += TimingScheme.t_e + CR_MTI_w*TimingScheme.onebit; //t_w时间
					TOTAL_bits += CR_MTI_w; //加上可以协调的CR_MTI_w的窗口大小。
//					}else{
//						tcollection += TimingScheme.t_e; //空时隙
//					}
				}
			}
		}
		
		return identifiedTags; //return the tag set 
	}
	
	
	private HashSet<Tag> findTagSet(int slotIdx, HashSet<Tag> stagset) {
		// TODO Auto-generated method stub
		HashSet<Tag> temp = new HashSet<Tag>();
		temp.clear();
		for(Tag t: stagset){
			if(t.slotidx==slotIdx){
				temp.add(t);
			}
		}
		
		return temp;
	}
	
	/**
	 * 根据第二次哈希的回复时隙，找到想要的tag set
	 * @param slotIdx
	 * @param stagset
	 * @return
	 */
	private HashSet<Tag> findTagSet2(int slotIdx2, HashSet<Tag> stagset) {
		// TODO Auto-generated method stub
		HashSet<Tag> temp = new HashSet<Tag>();
		temp.clear();
		for(Tag t: stagset){
			if(t.slotidx2==slotIdx2){
				temp.add(t);
			}
		}
		
		return temp;
	}
	
	
	/**
	 *  CR_MTI: Tag verifying
	 *  stagset is the  server tag set can be verfied
	 *  ltagset is the  local tag set 
	 *  return 
	 */
	public double CR_MTI_tag_filtering(HashSet<Tag> stagset, HashSet<Tag> ltagset,  HashSet<Tag> stagsetReconciled){
	
		
		int n= stagsetReconciled.size(); 
		
		/*  得到missing 集合   */
		missingset.addAll(stagsetReconciled);   
		missingset.removeAll(ltagset); //求差集，就是missing 标签
    
		stagset.removeAll(stagsetReconciled); //remove the verified tags in server
		ltagset.removeAll(stagsetReconciled); //remove the verified tags in local
		
		return 0.0;  //
		
	}
	
	



}
