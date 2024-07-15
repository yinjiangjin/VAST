package RFID;

import java.util.*;

/**
 * Completely pinpointing the missing rfid tags in a time-efficient way - Xiulong Liu  2015 TOC
 * @author Jiangjin
 *
 */
public class OneCategory_SFMTI extends ICNPBasic {
	
	HashSet<Tag> tempTarget = new HashSet<Tag>(); //
	HashSet<Tag> tempNotarget = new HashSet<Tag>();
	HashSet<Tag> tempLocal = new HashSet<Tag>();
	
	HashSet<Tag> missingset = new HashSet<Tag>();

	//HashSet<int[]> tempLocal2 = new HashSet<int[]>();
//	
//	int numCategory = categoryNum_Basic; //��������Ŀ
//	int categoryBitLength = categoryBitNum; //�õ��������Ŀ
//	
//	int[] CategoryIDArr = new int[numCategory];
//	

	public OneCategory_SFMTI(){
		ptname = new String("oneCategory_SFMTI");
	}
	
	double execute(){
		
		System.out.println("********start oneCategory_SFMTI******** ");
	
		tcollection = 0;  
		TOTAL_bits = 0; //��¼�ܵı�����
		
		HashSet<Tag> roundIdentifiedset = new HashSet<Tag>();
		while(serverset.size()>0){

			int tagnum = serverset.size();
			int f = optimalFrameS(tagnum);
			System.out.println("SFMTI frame size: " +f);
			
			roundIdentifiedset.clear();//����Ѿ���ʶ�����һ�ֻ����ǩ
			
			//ʶ���ǩ������ʱ��
			roundIdentifiedset.addAll(SFMTI_identify(f, serverset,localtagsset));
			//���Ƴ��Ѿ���ʶ��ı�ǩ
			double x = SFMTI_remove(serverset,localtagsset,roundIdentifiedset);
			
		}

		totalMissingNumber_identified = missingset.size(); //�Ѷ�ʧ��ǩ������¼��basic����
		System.out.println("missing tag: "+ missingset.size());
		
		System.out.println("****End oneCategory_SFMTI***** tcollection******:"+ tcollection+"total bit:"+ TOTAL_bits);
		return tcollection;
	}
	
	
	/**
	 * SFMTI optimal frame size setting (done)
	 * @param N  number of tag 

	 * @return f --- frame size
	 */
	public int optimalFrameS(int N ){
		
		double rho = 1.68;
		double f = N/1.68;
		if(f<5){
			f=5;
		}
		return (int)f;
		
	}
	
	
	/**
	 *  (1) collision slot reconciling phase
	 *  stagset is the  server tag set
	 */
	public  HashSet<Tag> SFMTI_identify(int f, HashSet<Tag> servertagset, HashSet<Tag> systemtagset){
		
		tcollection+=TimingScheme.t_lambda; 
		
		int seed1 = rand.nextInt();
		int[] replyArrServer = getReply(f,seed1,servertagset);//expected reply vector when server tags is pre-mapped; 
		//Time1: broadcasting vector
		tcollection += (TimingScheme.t_id*2*f)/96.0; //phase 1: time. broadcast 2*f bits 
		TOTAL_bits += 2*f;
		
		
		HashSet<Tag> identifiedtagset = new HashSet<Tag>(); //record the tags that can be identified
		
		//Time 2: singleton slot tag reply
		for(Tag t: servertagset){//add the tags in the singleton slots
			if(replyArrServer[t.slotidx]==1){
				identifiedtagset.add(t);
				tcollection += 0.1; //
				tcollection += TimingScheme.t_s;
				TOTAL_bits += 1;
				/* 
				if(systemtagset.contains(t)){//ϵͳ���ڸñ�ǩ�����յ��ظ�
					tcollection += TimingScheme.t_s;
				}else{
					tcollection += TimingScheme.t_e;
				}
				*/
			}
		}
		
		//2-collisions and 3-collisions
		int seed2 = rand.nextInt();
		
		HashSet<Tag> ser_tempSet = new HashSet<Tag>();//������
		HashSet<Tag> sys_tempSet = new HashSet<Tag>();//ϵͳ��ǩ
		
		for (int i = 0; i < replyArrServer.length; i++) {
			if ( replyArrServer[i] ==2 || replyArrServer[i] ==3 ){
				ser_tempSet.clear();
				ser_tempSet.addAll(findTagSet(i,servertagset));//ѡ���ڸ�ʱ϶�еķ�������ǩ
				
				sys_tempSet.clear();
				sys_tempSet.addAll(findTagSet(i,systemtagset));//ѡ���ڸ�ʱ϶�е�ϵͳ��ǩ
				
				
				//frame size = 2 or 3, where 1=�ظ���0=��Ĭ
				int[] replyArrReconcile_ser = getReply2(ser_tempSet.size(),seed2,ser_tempSet);//ע���ǣ�getreply2
				
//				System.out.println("replyArrReconcile_ser:"+replyArrReconcile_ser.length);
//				for (int j = 0; j < replyArrReconcile_ser.length; j++) {
//					System.out.print(replyArrReconcile_ser[j] );
//				}
//				System.out.println(); //replyArrReconcile_ser:2  \n 11
				
				//whether or not reconciled by seed2?
				boolean flag = true;
				for (int jj = 0; jj < replyArrReconcile_ser.length; jj++) {
					if( replyArrReconcile_ser[jj]>1 ){//��������г�ͻ
						flag = false;
						break;
					}
				}
				//Time 3: reconciled slot tag reply
				if(flag){//����ܹ�����ȫЭ��
					identifiedtagset.addAll(ser_tempSet);
					int missingNum =  ser_tempSet.size() - sys_tempSet.size();
					tcollection += ser_tempSet.size()*0.1; //
					tcollection += ser_tempSet.size()*TimingScheme.t_e;//�Ȳ�����ʱ϶ʱ�䡣
					
					TOTAL_bits += ser_tempSet.size(); //��ʱ϶Ҳ��1����
					
					//tcollection +=  missingNum*TimingScheme.t_e* + TimingScheme.t_s * sys_tempSet.size();
				}
				
			}
		}
		
		return identifiedtagset; //return the tag set 
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

	
	// ��ǩ�� �ܷ�seed 2 Э��
//	boolean isReconcileCollision(HashSet<Tag> tagset, int seed2){
//		int f = tagset.size();
//		int[] replyTemp = getReply(f,seed2,tagset);
//		boolean flagRC = true;
//		for (int i = 0; i < replyTemp.length; i++) {
//			if(replyTemp[i]!=1){
//				flagRC = false;
//				break;
//			}
//		}
//		
//		return flagRC; //successful is true
//		
//	}
	
	
	/**
	 *  SFMTI remove identified tags
	 *  stagset is the  server tag set can be verfied
	 *  ltagset is the  local tag set 
	 *  return time overhead
	 */
	public double SFMTI_remove(HashSet<Tag> stagset, HashSet<Tag> ltagset, HashSet<Tag> stagsetReconciled){
	
		int n= stagsetReconciled.size(); 
		
		/*  �õ�missing ����   */
		missingset.addAll(stagsetReconciled);   
		missingset.removeAll(ltagset); //��������missing ��ǩ
		
		stagset.removeAll(stagsetReconciled); //remove the verified tags in server
		ltagset.removeAll(stagsetReconciled); //remove the verified tags in local
	
		return 0.0;  //
	}
	


}
