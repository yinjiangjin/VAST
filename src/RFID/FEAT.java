package RFID;

import java.awt.print.Printable;
import java.util.*;

/**
 * 
 * the following coding is the proposed KEY missing protocol.
 * @author Jiangjin
 *
 */
public class FEAT extends ICNPBasic {
	
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

	public FEAT(){
		ptname = new String("FEAT");
	}
	
	double execute(){
		
		if(!flag_key_missing){ //�������key��ǩ��ʧʶ�𣬼�Ϊȫ����ǩʶ�����������ordinaryΪ��
			localtagsset.removeAll(nontargetset);
			nontargetset.clear();
		}
		
		System.out.println("********start FEAT new protocol********** ");
		//int totalbit = 0; //��¼�ܵı�����
		tcollection = 0;  
		TOTAL_bits = 0;
		
		ttime_pre_selection= 0;  //E-FEAT ��һ�׶�
		ttime_tag_verify= 0;  //FEAT ���� E-FEAT �ڶ��׶�
		ttime_hopping_selection= 0;  //FEAT ���� E-FEAT �����׶�
		
		tbits_pre_selection= 0; //E-FEAT ��һ�׶�  ������
		tbits_tag_verify= 0;  //FEAT ���� E-FEAT �ڶ��׶�    ������
		tbits_hopping_selection = 0;  //FEAT ���� E-FEAT �����׶� ������
		
		total_rounds = 0;
		
		System.out.println("nontargetset.size():"+nontargetset.size());
		System.out.println("targetset:"+targetset.size());
		System.out.println("localtagsset: "+localtagsset.size());
		
		HashSet<Tag> roundIdentifiedset = new HashSet<Tag>();
		while(targetset.size()>0){ //��������Ŀ���ǩ��ȫ��ʶ��Э��Ϳ�����ֹ
			
			total_rounds++;
			
			int L =96;
	
			int opt_frame =  optimal_frame_obj(L); //������֡��
		
			Utils.logAppend("feat_new.txt", "optf: "+opt_frame+"  ");		
			OurKEYMissing_testingAndhopping(opt_frame); //��������֡��ִ��Э��
			
		}
		Utils.logAppend("feat_new.txt", "optf: "+"\n");
	
			
		totalMissingNumber_identified = missingset.size(); //�Ѷ�ʧ��ǩ������¼��basic����
		System.out.println("missing tag: "+ missingset.size());
		
		System.out.println("****End FEAT ** tcollection**:"+ tcollection +"*** TOTAL_bits**"+ TOTAL_bits);
		return tcollection;
	}
	
	
	
	public void OurKEYMissing_testingAndhopping(int optf){
		
		tcollection+=TimingScheme.t_lambda; 
		
		ttime_tag_verify += TimingScheme.t_lambda;//����һ�׶ε�ʱ��
		
		int seed1 = rand.nextInt();
		
		int frameS = optf; //����֡��
		if(frameS<30){
			frameS =30;
		}
		tcollection += TimingScheme.t_id*frameS/96.0;
		TOTAL_bits += frameS; //֡���ı�������verifying phase
		
		ttime_tag_verify += TimingScheme.t_id*frameS/96.0;
		tbits_tag_verify += frameS; //֡���ı�������verifying phase
		
		
		
		System.out.println("frameS:"+frameS);
		
		int[] replyArr_Local = getReply(frameS,seed1,localtagsset);//�յ��Ǳ��ر�ǩ�Ļظ��� �ñ��ر�ǩ��hash��
		
		int[] replyArr_Target = getReply(frameS,seed1,targetset);//
		int[] replyArr_Nontarget = getReply(frameS,seed1,nontargetset);//
		
		//1. ����server�˵� nontarget
		tempNotarget.clear(); //��֮ǰ�����
		for(Tag tnt: nontargetset){
			if( replyArr_Local[tnt.slotidx]==0 || replyArr_Target[tnt.slotidx]==0){ //1.����û�лظ���2.����targetѡ�񡣾����ų�
				tempNotarget.add(tnt);
			}
		}
		nontargetset.removeAll(tempNotarget);
		
		//2. ����server�˵� target 
		tempTarget.clear();
		for(Tag tt: targetset){//�ų���ǩ
			//two cases for validating target tags: 1) ���ػظ�Ϊ0. 2) ��һ��targetѡ�񣬲���nontargetѡ��
			if( replyArr_Local[tt.slotidx]==0 ||( replyArr_Target[tt.slotidx]==1 && replyArr_Nontarget[tt.slotidx]==0) ){
				tempTarget.add(tt);
			}
		}
		targetset.removeAll(tempTarget);
		
		/*2.1 hopping phase */
		tempLocal.clear();
		
		for (Tag tl: localtagsset ){
			//1.����targetѡ��ı�ǩ�϶��ܹ��ų���  2.����һ��targetѡ��ģ�����nontargetѡ���
			if(replyArr_Target[tl.slotidx]==0 || ( replyArr_Target[tl.slotidx]==1 && replyArr_Nontarget[tl.slotidx]==0) ){
				tempLocal.add(tl);
			}
		}
		localtagsset.removeAll( tempLocal );
		
		int[] slot_hopping_Arr = new int[frameS];
		for (int i = 0; i < slot_hopping_Arr.length; i++) {
			slot_hopping_Arr[i] = -1; // ����ʼֵΪ-1
		}
		
		for (int i = 0; i < slot_hopping_Arr.length; i++) {
			
			if(replyArr_Local[i]==0){ //û���յ��ظ���ֵΪ0
				slot_hopping_Arr[i]=0; 
			}else if(replyArr_Target[i]==0){ //����KEYtagsѡ��Ҳ��ֵΪ0
				slot_hopping_Arr[i]=0; 
			}else if(replyArr_Target[i]==1 && replyArr_Nontarget[i]==0){ //����һ��KEYtagsѡ���ʱ϶Ҳ��ֵΪ0
				slot_hopping_Arr[i]=0;
			}else{
				slot_hopping_Arr[i] = 1; 
			}
			
		}
		
		int hop_bit = 0;
		
		int last_index = -1; //��ʼֵΪ-1
		for (int i = 0; i < slot_hopping_Arr.length; i++) {
			if(slot_hopping_Arr[i]==1){
				int need_bits = (int) Math.ceil( Math.log( i - last_index )/Math.log(2) );
				last_index = i; //����ʼ�� last_index��һ�¡�
				hop_bit += need_bits ;
			}
		}
		
		tcollection += (TimingScheme.t_id* hop_bit )/96.0;
		TOTAL_bits += hop_bit; //�ڶ��׶α�������
		
		ttime_hopping_selection += (TimingScheme.t_id* hop_bit )/96.0;
		tbits_hopping_selection += hop_bit; //�ڶ��׶α�������
		
		
		tempTarget.clear(); //��������
		tempNotarget.clear(); //��������
		tempLocal.clear();
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
	 *  OurKEYMissing remove identified tags
	 */
	public double OurKEYMissing_remove(HashSet<Tag> stagset, HashSet<Tag> ltagset, HashSet<Tag> stagsetReconciled){
	
		int n= stagsetReconciled.size(); 
		
		/*  �õ�missing ����   */
		missingset.addAll(stagsetReconciled);   
		missingset.removeAll(ltagset); //��������missing ��ǩ
		
		stagset.removeAll(stagsetReconciled); //remove the verified tags in server
		ltagset.removeAll(stagsetReconciled); //remove the verified tags in local
	
		return 0.0;  //
	}
	
	
	
	/**
	 * 
	 * @param L һ�����96���������֡��
	 * @return
	 */
	public int optimal_frame_obj(int L){
		double X = targetset.size();
		double Y = nontargetset.size();
		double Z = localtagsset.size();
		int large_f = (int) (X+Y);
		
		double Tavg_obj_arr[] = new double[large_f*10];//����֡��Ӧ�ò����
		
		for (int i = 5; i < Tavg_obj_arr.length; i++) {
			
			double f = i; //������֡�����������energe_obj_arr[i]
			
			double N_key = X*Math.pow(Math.E, -(X+Y)/f)* ( 1.0- Math.pow(Math.E , -Z/f) ) + X*Math.pow(Math.E , -Z/f );
		
			double N_u = f* (1.0 - Math.pow(Math.E, -X/f) - Math.pow(Math.E, -(X+Y)/f)*X/f)*(1.0 - Math.pow(Math.E, -Z/f));
			
			double N_bits = N_u*( 1 + Math.log(f/N_u)/Math.log(2));
			
			double t_p1 = TimingScheme.t_lambda + (TimingScheme.t_e + L*TimingScheme.onebit)*f/L;
			
			double t_p2 = TimingScheme.t_lambda + N_bits*TimingScheme.t_id/96;
					
		
			
			double T_round = t_p1 + t_p2;
			
			Tavg_obj_arr[i] = T_round/N_key; // �������Ŀ�꺯����
		}
		
//		for (int i = 0; i < Tavg_obj_arr.length; i++) {
//			System.out.print(Tavg_obj_arr[i]+ " ");
//		}
		//System.exit(0);
		
		double min_T_avg = Tavg_obj_arr[5]; //��С����֤һ��key��ǩ��ʱ�俪��
		int opt_f = 0;
		for (int i = 5; i < Tavg_obj_arr.length; i++) {
			if(Tavg_obj_arr[i] < min_T_avg ){
				min_T_avg = Tavg_obj_arr[i];
				opt_f =i;
			}
		}
		System.out.println("opt_f: " + opt_f+" X: "+X+ " Y: "+Y+ " Z: "+Z);
		
		return opt_f; //�������֡��
		
	}
	
	
}
