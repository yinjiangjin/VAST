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
	
//	int numCategory = categoryNum_Basic; //��������Ŀ
//	int categoryBitLength = categoryBitNum; //�õ��������Ŀ��
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
		TOTAL_bits= 0; //��¼�ܵı�����
		
		
		while(serverset.size()>0){
			
			int ser_tagnum = serverset.size();
			int f = CR_MTI_optimalf(ser_tagnum);
		//	System.out.println("CR_MTI frame size: " +f);
			HashSet<Tag> tempIdentifiedSet = new HashSet<Tag>();
			
			tempIdentifiedSet.addAll(CR_MTI_pre_identify(f, serverset,localtagsset));
			double x = CR_MTI_tag_filtering(serverset,localtagsset,tempIdentifiedSet);

		}
		
		totalMissingNumber_identified = missingset.size(); //�Ѷ�ʧ��ǩ������¼��basic����
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
//		double []Arr_Tavg = new double[10+N_server]; //ֻ����10��ֵ
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
//				MinAverage = Arr_Tavg[i]; //��¼��Сֵ
//				optf = i;  //��¼��Сֵ��Ӧ����(֡��)	
//			}
//		}
		
		return optf; //��������֡��
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
			if(replyArrServer[i]>1){  //��ͻʱ϶(�����Ƿ��ܹ�Э��)����1���ر�ʾ
				totalbit_Vm += 1;
			}else{
				totalbit_Vm += 2; //�պ͵�ʱ϶�������ر�ʾ
			}
		}
		tcollection += TimingScheme.t_id*totalbit_Vm/96.0;//�㲥Vm��ʱ��
		TOTAL_bits += totalbit_Vm;
		
		HashSet<Tag> identifiedTags = new HashSet<Tag>(); //record the tags that can be identified 
		
		//1. CR_MTI: singleton 
		for(Tag t: ser_tagset){
			if(replyArrServer[t.slotidx]==1){
				identifiedTags.add(t);//identify the tags in the pre-mapped singleton slots
				tcollection += 0.1; //
				tcollection += TimingScheme.t_s;
				TOTAL_bits += 1; //����1���ء�
//				if(systemtagset.contains(t)){ //��������ǩ������ϵͳ�£�Ҳ����˵�Ķ����յ��ظ�
//					tcollection += TimingScheme.t_s;
//				}else {// ����Ķ���û���յ��ظ�
//					tcollection += TimingScheme.t_e;
//				}
			}
		}
		
		//2. CR_MTI: Reconcile collisions�� 
		int seed2 = rand.nextInt();
		
		HashSet<Tag> tempSet_ser = new HashSet<Tag>(); //server
		
		HashSet<Tag> tempSet_sys = new HashSet<Tag>(); //system
		
		for (int ii = 0; ii < replyArrServer.length; ii++) {
			if ( replyArrServer[ii] >1 ){ //collision slots
				tempSet_ser.clear();
				tempSet_ser.addAll(findTagSet(ii, ser_tagset));
				
				tempSet_sys.clear();
				tempSet_sys.addAll(findTagSet(ii, systemtagset));
				
				
				//frame size = CR_MTI_w, ��ͻ��Э��by seed2
				int[] replyArrReconcile_ser = getReply2(CR_MTI_w,seed2,tempSet_ser);//���»ظ���slotidx2�ϣ�ע�ⲻ��slotidx1��
				
				//whether or not reconciled by seed2?
				boolean flag = true;
				for (int jj = 0; jj < replyArrReconcile_ser.length; jj++) {
					if( replyArrReconcile_ser[jj]>1 ){//��������г�ͻ
						flag = false;
						break;
					}
				}
				
				if(flag){ //������Ա���Э��
					identifiedTags.addAll(tempSet_ser); //���б�ǩ�����Ա�ʶ��
					tempSet_ser.clear();
					//if(tempSet_sys.size()>0){//ϵͳ�б�ǩ�ظ�
					tcollection += 0.1; //	
					tcollection += TimingScheme.t_e + CR_MTI_w*TimingScheme.onebit; //t_wʱ��
					TOTAL_bits += CR_MTI_w; //���Ͽ���Э����CR_MTI_w�Ĵ��ڴ�С��
//					}else{
//						tcollection += TimingScheme.t_e; //��ʱ϶
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
	 * ���ݵڶ��ι�ϣ�Ļظ�ʱ϶���ҵ���Ҫ��tag set
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
		
		/*  �õ�missing ����   */
		missingset.addAll(stagsetReconciled);   
		missingset.removeAll(ltagset); //��������missing ��ǩ
    
		stagset.removeAll(stagsetReconciled); //remove the verified tags in server
		ltagset.removeAll(stagsetReconciled); //remove the verified tags in local
		
		return 0.0;  //
		
	}
	
	



}
