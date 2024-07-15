package RFID;

import java.awt.print.Printable;
import java.util.*;

/**
 * 
 * ��ִ��pre-selection phase��Э��
 * @author Jiangjin
 *
 */
public class Advance_FEAT extends ICNPBasic {
	
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

	public Advance_FEAT(){
		ptname = new String("adv_FEAT");
	}
	
	double execute(){
		
		if(!flag_key_missing){ //�������key��ǩ��ʧʶ�𣬼�Ϊȫ����ǩʶ�����������ordinaryΪ��
			localtagsset.removeAll(nontargetset);
			nontargetset.clear();
		}
		
		System.out.println("********start adv_FEAT new protocol********** ");
		tcollection = 0;  
		TOTAL_bits = 0; //��¼�ܵı�����

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
	
			double FEAT_opt_frame_Tavg[] =  optimal_frame_FEAT(L); //������֡��
		
			double adv_FEAT_opt_frame_Tavg[] =  optimal_frame_FEAT_adv(L); //������֡��
			
			for (int i = 0; i < 2; i++) {
				System.out.println("FEAT_opt_frame_Tavg  "+i+": " + FEAT_opt_frame_Tavg[i]);
				System.out.println("adv_FEAT_opt_frame_Tavg  "+i+": " + adv_FEAT_opt_frame_Tavg[i]);
			}
			
			
			if(FEAT_opt_frame_Tavg[1] <  adv_FEAT_opt_frame_Tavg[1] ){
				
				//Utils.logAppend("adv_FEAT_new.txt", "optf: "+opt_frame+"  ");		
				
				int opt_frame = (int) FEAT_opt_frame_Tavg[0];
				FEAT_testingAndhopping(opt_frame); //��������֡��ִ��Э��
			}else{
				
				int opt_frame = (int) adv_FEAT_opt_frame_Tavg[0];
				Adv_FEAT_testingAndhopping(opt_frame); //��������֡��ִ��Э��
				
//				System.out.println("nontargetset.size():"+nontargetset.size());
//				System.out.println("targetset:"+targetset.size());
//				System.out.println("localtagsset: "+localtagsset.size());
				
			}
			
			
		}
	
			
		totalMissingNumber_identified = missingset.size(); //�Ѷ�ʧ��ǩ������¼��basic����
		System.out.println("missing tag: "+ missingset.size());
		

		System.out.println("****End adv_FEAT new protocol***** tcollection******:"+ tcollection+"total bit:"+ TOTAL_bits);
		return tcollection;
	}
	
	
	
	public void FEAT_testingAndhopping(int optf){
		
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
	
	/**
	 * ʱ�䣬֮��������㡣
	 * @param optf
	 */
	public void Adv_FEAT_testingAndhopping(int optf){
		

		tcollection+=TimingScheme.t_lambda; 
		
		ttime_pre_selection += TimingScheme.t_lambda;//
		
		int seed1 = rand.nextInt();
		
		int frameS = optf; //����֡��
		if(frameS<30){
			frameS =30;
		}
		
		System.out.println("frameS:"+frameS);
		
		int[] replyArr_Local = getReply(frameS,seed1,localtagsset);//�յ��Ǳ��ر�ǩ�Ļظ��� �ñ��ر�ǩ��hash��
		int[] replyArr_Target = getReply(frameS,seed1,targetset);//
		int[] replyArr_Nontarget = getReply(frameS,seed1,nontargetset);//
		
		/** 1. pre-selection time  */
		int N_k = 0; //��¼key slots������Ŀ
		
		int[] pre_selection_Arr = new int[frameS];
		for (int i = 0; i < pre_selection_Arr.length; i++) {
			pre_selection_Arr[i] = -1; // ����ʼֵΪ-1
		}
		
		for (int i = 0; i < pre_selection_Arr.length; i++) {
			
			if(replyArr_Target[i]!=0){ //ֻҪ��KEY tagsѡ����Ϊ1
				pre_selection_Arr[i]=1; 
				N_k += 1;
			}else{
				pre_selection_Arr[i] = 0; 
			}
		}
		
		int pre_selection_bits = 0;
		int need_bits_1 = 0;
		int last_index_pre = -1; //��ʼֵΪ-1
		for (int i = 0; i < pre_selection_Arr.length; i++) {
			if(pre_selection_Arr[i]==1){
				if ((i - last_index_pre ) ==1){//�����ֵΪ1��Ҳ��������
					need_bits_1 =1;
				}else{
					need_bits_1 = (int) Math.ceil( Math.log( i - last_index_pre )/Math.log(2) );
				}
				last_index_pre = i; //����ʼ�� last_index��һ�¡�
				pre_selection_bits += need_bits_1 ;
			}
		}
		tcollection += (TimingScheme.t_id* pre_selection_bits )/96.0; //pre-selection time
		TOTAL_bits += pre_selection_bits;
		
		tbits_pre_selection += pre_selection_bits;
		ttime_pre_selection +=(TimingScheme.t_id* pre_selection_bits )/96.0;
		
		System.out.println("N_k: "+N_k);
		System.out.println("pre_selection_bits: "+pre_selection_bits);
		
		/**  2. verifying phase  time    */
		tcollection += (TimingScheme.t_id* N_k )/96.0; //verifying time
		TOTAL_bits += N_k;
		
		tbits_tag_verify += N_k; //bits 
		ttime_tag_verify += (TimingScheme.t_id* N_k )/96.0; //time
		
		/**  3. hopping selection phase  time    */
		int[] hopping_Arr = new int[N_k]; // ע�⣺ �˴���N_k
		for (int i = 0; i < hopping_Arr.length; i++) {
			hopping_Arr[i] = 0; // ����ʼֵΪ0
		}
		
		int temp_index = 0;
		for (int i = 0; i < replyArr_Target.length; i++) { //��ΪҪ�������лظ�λ�ã�����֡���Ĵ�С 
			
			if(replyArr_Target[i] > 0 ){ // ������key slot
				if(replyArr_Local[i]==0){
					temp_index += 1;
					continue; 
				}else if(replyArr_Target[i]==1 && replyArr_Nontarget[i]==0){//����һ��KEYtagsѡ���ʱ϶Ҳ��ֵΪ0
					temp_index += 1;
					continue; 
				}else{
					hopping_Arr[temp_index] = 1; // ע�⣺�˴���temp_index��
					temp_index+=1;
				}
			}
			
		}
		
		int hop3_bits = 0;
		int need_bits_2 =0;
		int hop3_index = -1; //��ʼֵΪ-1
		for (int i = 0; i < hopping_Arr.length; i++) {
			if(hopping_Arr[i]==1){
				if ((i - hop3_index ==1)){
					need_bits_2 =1;
				}else{
					need_bits_2 = (int) Math.ceil( Math.log( i - hop3_index )/Math.log(2) );
				}
				hop3_index = i; //����ʼ�� last_index��һ�¡�
				hop3_bits += need_bits_2 ;
			}
		}
		
		tcollection += (TimingScheme.t_id* hop3_bits )/96.0;
		TOTAL_bits += hop3_bits;
		
		ttime_hopping_selection += (TimingScheme.t_id* hop3_bits )/96.0;
		tbits_hopping_selection += hop3_bits;
		
		
		
		/** 1. ����server�˵� nontarget */
		tempNotarget.clear(); //��֮ǰ�����
		for(Tag tnt: nontargetset){
			if( replyArr_Local[tnt.slotidx]==0 || replyArr_Target[tnt.slotidx]==0){ //1.����û�лظ���2.����targetѡ�񡣾����ų�
				tempNotarget.add(tnt);
			}
		}
		nontargetset.removeAll(tempNotarget);
		
		/**2. ����server�˵� target  */
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
	public double[] optimal_frame_FEAT(int L){
		double X = targetset.size();
		double Y = nontargetset.size();
		double Z = localtagsset.size();
		int large_f = (int) (X+Y)*10;
		
		double Tavg_obj_arr[] = new double[large_f];//����֡��Ӧ�ò����
		
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
				opt_f = i;
			}
		}
		
		double optf_Tavg_min[] = new double[2];
		optf_Tavg_min[0] = opt_f;
		optf_Tavg_min[1] = min_T_avg;
				
		System.out.println("opt_f: " + opt_f+" X: "+X+ " Y: "+Y+ " Z: "+Z);
		
		
		
		return optf_Tavg_min; //�������֡����and min_Tavg
		
	}
	
	/**
	 * 
	 * @param L һ�����96���������֡��
	 * @return
	 */
	public double[] optimal_frame_FEAT_adv(int L){
		double X = targetset.size();
		double Y = nontargetset.size();
		double Z = localtagsset.size();
		int large_f = (int) (X+Y)*10;
		
		double Tavg_obj_arr[] = new double[large_f];//����֡��Ӧ�ò����
		
		for (int i = 5; i < Tavg_obj_arr.length; i++) {
			
			double f2 = i; //������֡�����������energe_obj_arr[i]
			
			double N_k = f2 * (1.0 - Math.pow(Math.E, -X/f2));   //key slot ����Ŀ��
			double N_b2 = N_k*( 1 + Math.log(f2/N_k)/Math.log(2)); //pre-selection �׶α�����
			double T_pre = TimingScheme.t_lambda + N_b2*TimingScheme.t_id/96;//1. pre-selection time overhead
			
			double T_ver = TimingScheme.t_lambda + N_k*TimingScheme.t_id/96; //2. verifying phase overhead
			
			//phase 3. δ��֤��ʱ϶��Ŀ
			double N_u2 = f2* (1.0 - Math.pow(Math.E, -X/f2) - Math.pow(Math.E, -(X+Y)/f2)*X/f2)*(1.0 - Math.pow(Math.E, -Z/f2));
			
			double N_bits_hop = N_u2*( 1 + Math.log(N_k/N_u2)/Math.log(2)); //pre-selection �׶α�����
			
			//time phase 3:
			double T_hop = TimingScheme.t_lambda + N_bits_hop*TimingScheme.t_id/96;
			
			double T_round_2 = T_pre + T_ver + T_hop;
			// �����ܹ���֤��key tag��Ŀ
			double N_key_2 = X*Math.pow(Math.E, -(X+Y)/f2)* ( 1.0- Math.pow(Math.E , -Z/f2) ) + X*Math.pow(Math.E , -Z/f2 );
		
	
			Tavg_obj_arr[i] = T_round_2/N_key_2; // �������Ŀ�꺯����
		}
		
		double min_T_avg = Tavg_obj_arr[5]; //��С����֤һ��key��ǩ��ʱ�俪��
		int opt_f = 0;
		for (int i = 5; i < Tavg_obj_arr.length; i++) {
			if(Tavg_obj_arr[i] < min_T_avg ){
				min_T_avg = Tavg_obj_arr[i];
				opt_f =i;
			}
		}
		
		double optf_Tavg_min[] = new double[2];
		optf_Tavg_min[0] = opt_f;
		optf_Tavg_min[1] = min_T_avg;
				
		System.out.println("opt_f: " + opt_f+" X: "+X+ " Y: "+Y+ " Z: "+Z);
		
		
		return optf_Tavg_min; //�������֡����and min_Tavg
		
	}
}
