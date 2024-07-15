package RFID;

import java.util.*;

/**
 * Efficiently and completely identify missing key tags for anonymous RFID system - HongLong Chen  2018 IOT
 * the following coding is the iVEKI protocol.
 * @author Jiangjin
 *
 */
public class OneCategory_VEKI extends ICNPBasic {
	
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

	public OneCategory_VEKI(){
		ptname = new String("oneCategory_VEKI");
	}
	
	double execute(){
		
		if(!flag_key_missing){ //�������key��ǩ��ʧʶ�𣬼�Ϊȫ����ǩʶ�����������ordinaryΪ��
			localtagsset.removeAll(nontargetset);
			nontargetset.clear();
		}
		
		System.out.println("********start oneCategory_VEKI******** ");
		
		tcollection = 0;  
		TOTAL_bits = 0; //��¼�ܵı�����
		
		
		System.out.println("nontargetset.size():"+nontargetset.size());
		System.out.println("targetset:"+targetset.size());
		System.out.println("localtagsset: "+localtagsset.size());
		
		HashSet<Tag> roundIdentifiedset = new HashSet<Tag>();
		int round =0 ;
		while(nontargetset.size()>0){ //��������Ŀ���ǩ��ȫ��ʶ�𣬹��˽׶���ֹ
			double timeeachround = TimingScheme.t_lambda + (TimingScheme.t_id*targetset.size())/96.0; //ÿһ�ֹ��˵�ʱ����ͬ
			//Utils.logAppend("filtering_time-VEKI-0.4.txt", "VEKI "+"\t"+ timeeachround+"\n");
			round++;
			VEKI_OrdinaryTagsDeactivation(targetset, nontargetset, localtagsset,round);
			//Utils.logAppend("collecttotalnumber-HTIC.txt", "HTIC "+collecttotalnumber+"\n");
		}
		
		System.out.println("start identification phase");
		System.out.println("targetset:"+targetset.size());
		System.out.println("localset:"+localtagsset.size());
	
		
		while(targetset.size()>0){ 
			
			roundIdentifiedset.clear();//����Ѿ���ʶ�����һ�ֻ����ǩ
			//ʶ���ǩ������ʱ��
			roundIdentifiedset.addAll(VEKI_KEYtagsIdentificationPhase(targetset,localtagsset));
		
		}
			
		totalMissingNumber_identified = missingset.size(); //�Ѷ�ʧ��ǩ������¼��basic����
		System.out.println("missing tag: "+ missingset.size());
		
		System.out.println("****End oneCategory_VEKI***** tcollection******:"+ tcollection+"total bit:"+ TOTAL_bits);
		return tcollection;
	}
	
	
	
	public void VEKI_OrdinaryTagsDeactivation(HashSet<Tag> targettagset, HashSet<Tag> nontargettagset, HashSet<Tag> localtagset,int round){
		
		
		ttime_filtering +=TimingScheme.t_lambda; //ͳ�ƹ��˽׶ε�ʱ��
		
		tcollection+=TimingScheme.t_lambda; 
		int seed1 = rand.nextInt();
		
		int f1 = targettagset.size(); //����֡��
		tcollection += (TimingScheme.t_id*f1)/96.0;
		TOTAL_bits += f1;
		
		ttime_filtering += (TimingScheme.t_id*f1)/96.0;//ͳ�ƹ��˽׶ε�ʱ��
		
		int[] replyArrServerTarget = getReply(f1,seed1,targettagset);//������key��ǩ��Ԥӳ���
		int[] replyArrServerNontarget = getReply(f1,seed1,nontargettagset);//
		int[] replyArrLocal = getReply(f1,seed1,localtagset);//
		
		tempNotarget.clear(); //��֮ǰ�����
		for(Tag t: nontargettagset){//�ų���ǩ
			if( replyArrServerTarget[t.slotidx]==0 ){
				tempNotarget.add(t);
			}
		}
		
		tempLocal.clear();
		for(Tag t: localtagset){//�ų���ǩ
			if( replyArrServerTarget[t.slotidx]==0 ){
				tempLocal.add(t);
			}
		}
		
		
		
		nontargettagset.removeAll(tempNotarget);
		
		localtagsset.removeAll(tempLocal);
		
		
	//	Utils.logAppend("filtering_time-VEKI.txt-0.4", "VEKI "+"\t"+ round+"\t"+ tempNotarget.size()+"\t"+nontargettagset.size()+"\n");
		
		tempNotarget.clear(); //��������
		tempLocal.clear();
	}
	
	/**
	 */
	public  HashSet<Tag> VEKI_KEYtagsIdentificationPhase(HashSet<Tag> targettagset, HashSet<Tag> localtagset){
		
		tcollection+=TimingScheme.t_lambda; 
		
		int seed1 = rand.nextInt();
		int f2 = targettagset.size(); //ʣ���
		tcollection += (TimingScheme.t_id*f2)/96.0; //time. broadcast f2 bits 
	
		TOTAL_bits += f2;
		
		int[] replyArrTarget = getReply(f2,seed1,targettagset);//expected reply vector when server tags is pre-mapped; 
	
		
		HashSet<Tag> identifiedtagset = new HashSet<Tag>(); //record the tags that can be identified
		
		//Time 2: singleton slot tag reply
		for(Tag t: targettagset){//add the tags in the singleton slots
			if(replyArrTarget[t.slotidx]==1){
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
		
		targettagset.removeAll(identifiedtagset); //�Ƴ��Ѿ�ʶ��ı�ǩ
		localtagset.removeAll(identifiedtagset); //�Ƴ��Ѿ�ʶ��ı�ǩ
		
		
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


	/**
	 *  VEKI remove identified tags
	 */
	public double VEKI_remove(HashSet<Tag> stagset, HashSet<Tag> ltagset, HashSet<Tag> stagsetReconciled){
	
		int n= stagsetReconciled.size(); 
		
		/*  �õ�missing ����   */
		missingset.addAll(stagsetReconciled);   
		missingset.removeAll(ltagset); //��������missing ��ǩ
		
		stagset.removeAll(stagsetReconciled); //remove the verified tags in server
		ltagset.removeAll(stagsetReconciled); //remove the verified tags in local
	
		return 0.0;  //
	}
	


}
