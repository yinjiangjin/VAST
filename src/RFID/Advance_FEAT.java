package RFID;

import java.awt.print.Printable;
import java.util.*;

/**
 * 
 * 均执行pre-selection phase的协议
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
//	int numCategory = categoryNum_Basic; //获得类别数目
//	int categoryBitLength = categoryBitNum; //得到父类的数目
//	
//	int[] CategoryIDArr = new int[numCategory];
//	

	public Advance_FEAT(){
		ptname = new String("adv_FEAT");
	}
	
	double execute(){
		
		if(!flag_key_missing){ //如果不是key标签丢失识别，即为全部标签识别，则可以设置ordinary为空
			localtagsset.removeAll(nontargetset);
			nontargetset.clear();
		}
		
		System.out.println("********start adv_FEAT new protocol********** ");
		tcollection = 0;  
		TOTAL_bits = 0; //记录总的比特数

		ttime_pre_selection= 0;  //E-FEAT 第一阶段
		ttime_tag_verify= 0;  //FEAT 或者 E-FEAT 第二阶段
		ttime_hopping_selection= 0;  //FEAT 或者 E-FEAT 第三阶段
		
		tbits_pre_selection= 0; //E-FEAT 第一阶段  比特数
		tbits_tag_verify= 0;  //FEAT 或者 E-FEAT 第二阶段    比特数
		tbits_hopping_selection = 0;  //FEAT 或者 E-FEAT 第三阶段 比特数
		
		total_rounds = 0;
		
		System.out.println("nontargetset.size():"+nontargetset.size());
		System.out.println("targetset:"+targetset.size());
		System.out.println("localtagsset: "+localtagsset.size());
		
		HashSet<Tag> roundIdentifiedset = new HashSet<Tag>();
		while(targetset.size()>0){ //当服务器目标标签被全部识别，协议就可以终止
			
			total_rounds++;
			
			int L =96;
	
			double FEAT_opt_frame_Tavg[] =  optimal_frame_FEAT(L); //获到最优帧长
		
			double adv_FEAT_opt_frame_Tavg[] =  optimal_frame_FEAT_adv(L); //获到最优帧长
			
			for (int i = 0; i < 2; i++) {
				System.out.println("FEAT_opt_frame_Tavg  "+i+": " + FEAT_opt_frame_Tavg[i]);
				System.out.println("adv_FEAT_opt_frame_Tavg  "+i+": " + adv_FEAT_opt_frame_Tavg[i]);
			}
			
			
			if(FEAT_opt_frame_Tavg[1] <  adv_FEAT_opt_frame_Tavg[1] ){
				
				//Utils.logAppend("adv_FEAT_new.txt", "optf: "+opt_frame+"  ");		
				
				int opt_frame = (int) FEAT_opt_frame_Tavg[0];
				FEAT_testingAndhopping(opt_frame); //根据最优帧长执行协议
			}else{
				
				int opt_frame = (int) adv_FEAT_opt_frame_Tavg[0];
				Adv_FEAT_testingAndhopping(opt_frame); //根据最优帧长执行协议
				
//				System.out.println("nontargetset.size():"+nontargetset.size());
//				System.out.println("targetset:"+targetset.size());
//				System.out.println("localtagsset: "+localtagsset.size());
				
			}
			
			
		}
	
			
		totalMissingNumber_identified = missingset.size(); //把丢失标签总数记录到basic类中
		System.out.println("missing tag: "+ missingset.size());
		

		System.out.println("****End adv_FEAT new protocol***** tcollection******:"+ tcollection+"total bit:"+ TOTAL_bits);
		return tcollection;
	}
	
	
	
	public void FEAT_testingAndhopping(int optf){
		
tcollection+=TimingScheme.t_lambda; 
		
		ttime_tag_verify += TimingScheme.t_lambda;//算这一阶段的时间
		
		int seed1 = rand.nextInt();
		
		int frameS = optf; //最优帧长
		if(frameS<30){
			frameS =30;
		}
		tcollection += TimingScheme.t_id*frameS/96.0;
		TOTAL_bits += frameS; //帧长的比特数，verifying phase
		
		ttime_tag_verify += TimingScheme.t_id*frameS/96.0;
		tbits_tag_verify += frameS; //帧长的比特数，verifying phase
		
		
		
		System.out.println("frameS:"+frameS);
		
		int[] replyArr_Local = getReply(frameS,seed1,localtagsset);//收的是本地标签的回复。 用本地标签做hash。
		
		int[] replyArr_Target = getReply(frameS,seed1,targetset);//
		int[] replyArr_Nontarget = getReply(frameS,seed1,nontargetset);//
		
		//1. 过滤server端的 nontarget
		tempNotarget.clear(); //用之前先清空
		for(Tag tnt: nontargetset){
			if( replyArr_Local[tnt.slotidx]==0 || replyArr_Target[tnt.slotidx]==0){ //1.本地没有回复。2.不被target选择。均可排除
				tempNotarget.add(tnt);
			}
		}
		nontargetset.removeAll(tempNotarget);
		
		//2. 过滤server端的 target 
		tempTarget.clear();
		for(Tag tt: targetset){//排除标签
			//two cases for validating target tags: 1) 本地回复为0. 2) 仅一个target选择，不被nontarget选择。
			if( replyArr_Local[tt.slotidx]==0 ||( replyArr_Target[tt.slotidx]==1 && replyArr_Nontarget[tt.slotidx]==0) ){
				tempTarget.add(tt);
			}
		}
		targetset.removeAll(tempTarget);
		
		/*2.1 hopping phase */
		tempLocal.clear();
		
		for (Tag tl: localtagsset ){
			//1.不被target选择的标签肯定能够排除。  2.仅被一个target选择的，不被nontarget选择的
			if(replyArr_Target[tl.slotidx]==0 || ( replyArr_Target[tl.slotidx]==1 && replyArr_Nontarget[tl.slotidx]==0) ){
				tempLocal.add(tl);
			}
		}
		localtagsset.removeAll( tempLocal );
		
		int[] slot_hopping_Arr = new int[frameS];
		for (int i = 0; i < slot_hopping_Arr.length; i++) {
			slot_hopping_Arr[i] = -1; // 赋初始值为-1
		}
		
		for (int i = 0; i < slot_hopping_Arr.length; i++) {
			
			if(replyArr_Local[i]==0){ //没有收到回复赋值为0
				slot_hopping_Arr[i]=0; 
			}else if(replyArr_Target[i]==0){ //不被KEYtags选择也赋值为0
				slot_hopping_Arr[i]=0; 
			}else if(replyArr_Target[i]==1 && replyArr_Nontarget[i]==0){ //仅被一个KEYtags选择的时隙也赋值为0
				slot_hopping_Arr[i]=0;
			}else{
				slot_hopping_Arr[i] = 1; 
			}
			
		}
		
		int hop_bit = 0;
		
		int last_index = -1; //起始值为-1
		for (int i = 0; i < slot_hopping_Arr.length; i++) {
			if(slot_hopping_Arr[i]==1){
				int need_bits = (int) Math.ceil( Math.log( i - last_index )/Math.log(2) );
				last_index = i; //把起始的 last_index换一下。
				hop_bit += need_bits ;
			}
		}
		
		tcollection += (TimingScheme.t_id* hop_bit )/96.0;
		TOTAL_bits += hop_bit; //第二阶段比特数。
		
		ttime_hopping_selection += (TimingScheme.t_id* hop_bit )/96.0;
		tbits_hopping_selection += hop_bit; //第二阶段比特数。
		
		
		tempTarget.clear(); //用完后清空
		tempNotarget.clear(); //用完后清空
		tempLocal.clear();
	}
	
	/**
	 * 时间，之类的重新算。
	 * @param optf
	 */
	public void Adv_FEAT_testingAndhopping(int optf){
		

		tcollection+=TimingScheme.t_lambda; 
		
		ttime_pre_selection += TimingScheme.t_lambda;//
		
		int seed1 = rand.nextInt();
		
		int frameS = optf; //最优帧长
		if(frameS<30){
			frameS =30;
		}
		
		System.out.println("frameS:"+frameS);
		
		int[] replyArr_Local = getReply(frameS,seed1,localtagsset);//收的是本地标签的回复。 用本地标签做hash。
		int[] replyArr_Target = getReply(frameS,seed1,targetset);//
		int[] replyArr_Nontarget = getReply(frameS,seed1,nontargetset);//
		
		/** 1. pre-selection time  */
		int N_k = 0; //记录key slots的总数目
		
		int[] pre_selection_Arr = new int[frameS];
		for (int i = 0; i < pre_selection_Arr.length; i++) {
			pre_selection_Arr[i] = -1; // 赋初始值为-1
		}
		
		for (int i = 0; i < pre_selection_Arr.length; i++) {
			
			if(replyArr_Target[i]!=0){ //只要被KEY tags选择，置为1
				pre_selection_Arr[i]=1; 
				N_k += 1;
			}else{
				pre_selection_Arr[i] = 0; 
			}
		}
		
		int pre_selection_bits = 0;
		int need_bits_1 = 0;
		int last_index_pre = -1; //起始值为-1
		for (int i = 0; i < pre_selection_Arr.length; i++) {
			if(pre_selection_Arr[i]==1){
				if ((i - last_index_pre ) ==1){//如果差值为1，也就是相邻
					need_bits_1 =1;
				}else{
					need_bits_1 = (int) Math.ceil( Math.log( i - last_index_pre )/Math.log(2) );
				}
				last_index_pre = i; //把起始的 last_index换一下。
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
		int[] hopping_Arr = new int[N_k]; // 注意： 此处是N_k
		for (int i = 0; i < hopping_Arr.length; i++) {
			hopping_Arr[i] = 0; // 赋初始值为0
		}
		
		int temp_index = 0;
		for (int i = 0; i < replyArr_Target.length; i++) { //因为要遍历所有回复位置，还是帧长的大小 
			
			if(replyArr_Target[i] > 0 ){ // 必须是key slot
				if(replyArr_Local[i]==0){
					temp_index += 1;
					continue; 
				}else if(replyArr_Target[i]==1 && replyArr_Nontarget[i]==0){//仅被一个KEYtags选择的时隙也赋值为0
					temp_index += 1;
					continue; 
				}else{
					hopping_Arr[temp_index] = 1; // 注意：此处是temp_index。
					temp_index+=1;
				}
			}
			
		}
		
		int hop3_bits = 0;
		int need_bits_2 =0;
		int hop3_index = -1; //起始值为-1
		for (int i = 0; i < hopping_Arr.length; i++) {
			if(hopping_Arr[i]==1){
				if ((i - hop3_index ==1)){
					need_bits_2 =1;
				}else{
					need_bits_2 = (int) Math.ceil( Math.log( i - hop3_index )/Math.log(2) );
				}
				hop3_index = i; //把起始的 last_index换一下。
				hop3_bits += need_bits_2 ;
			}
		}
		
		tcollection += (TimingScheme.t_id* hop3_bits )/96.0;
		TOTAL_bits += hop3_bits;
		
		ttime_hopping_selection += (TimingScheme.t_id* hop3_bits )/96.0;
		tbits_hopping_selection += hop3_bits;
		
		
		
		/** 1. 过滤server端的 nontarget */
		tempNotarget.clear(); //用之前先清空
		for(Tag tnt: nontargetset){
			if( replyArr_Local[tnt.slotidx]==0 || replyArr_Target[tnt.slotidx]==0){ //1.本地没有回复。2.不被target选择。均可排除
				tempNotarget.add(tnt);
			}
		}
		nontargetset.removeAll(tempNotarget);
		
		/**2. 过滤server端的 target  */
		tempTarget.clear();
		for(Tag tt: targetset){//排除标签
			//two cases for validating target tags: 1) 本地回复为0. 2) 仅一个target选择，不被nontarget选择。
			if( replyArr_Local[tt.slotidx]==0 ||( replyArr_Target[tt.slotidx]==1 && replyArr_Nontarget[tt.slotidx]==0) ){
				tempTarget.add(tt);
			}
		}
		targetset.removeAll(tempTarget);
		
		/*2.1 hopping phase */
		tempLocal.clear();
		
		for (Tag tl: localtagsset ){
			//1.不被target选择的标签肯定能够排除。  2.仅被一个target选择的，不被nontarget选择的
			if(replyArr_Target[tl.slotidx]==0 || ( replyArr_Target[tl.slotidx]==1 && replyArr_Nontarget[tl.slotidx]==0) ){
				tempLocal.add(tl);
			}
		}
		localtagsset.removeAll( tempLocal );
		
		tempTarget.clear(); //用完后清空
		tempNotarget.clear(); //用完后清空
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
		
		/*  得到missing 集合   */
		missingset.addAll(stagsetReconciled);   
		missingset.removeAll(ltagset); //求差集，就是missing 标签
		
		stagset.removeAll(stagsetReconciled); //remove the verified tags in server
		ltagset.removeAll(stagsetReconciled); //remove the verified tags in local
	
		return 0.0;  //
	}
	
	
	
	/**
	 * 
	 * @param L 一般等于96，输出最优帧长
	 * @return
	 */
	public double[] optimal_frame_FEAT(int L){
		double X = targetset.size();
		double Y = nontargetset.size();
		double Z = localtagsset.size();
		int large_f = (int) (X+Y)*10;
		
		double Tavg_obj_arr[] = new double[large_f];//两倍帧长应该差不多了
		
		for (int i = 5; i < Tavg_obj_arr.length; i++) {
			
			double f = i; //逐步增加帧长，逐个计算energe_obj_arr[i]
			
			double N_key = X*Math.pow(Math.E, -(X+Y)/f)* ( 1.0- Math.pow(Math.E , -Z/f) ) + X*Math.pow(Math.E , -Z/f );
		
			double N_u = f* (1.0 - Math.pow(Math.E, -X/f) - Math.pow(Math.E, -(X+Y)/f)*X/f)*(1.0 - Math.pow(Math.E, -Z/f));
			
			double N_bits = N_u*( 1 + Math.log(f/N_u)/Math.log(2));
			
			double t_p1 = TimingScheme.t_lambda + (TimingScheme.t_e + L*TimingScheme.onebit)*f/L;
			
			double t_p2 = TimingScheme.t_lambda + N_bits*TimingScheme.t_id/96;
					
		
			
			double T_round = t_p1 + t_p2;
			
			Tavg_obj_arr[i] = T_round/N_key; // 这个就是目标函数。
		}
		
//		for (int i = 0; i < Tavg_obj_arr.length; i++) {
//			System.out.print(Tavg_obj_arr[i]+ " ");
//		}
		//System.exit(0);
		
		double min_T_avg = Tavg_obj_arr[5]; //最小化验证一个key标签的时间开销
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
		
		
		
		return optf_Tavg_min; //输出最优帧长，and min_Tavg
		
	}
	
	/**
	 * 
	 * @param L 一般等于96，输出最优帧长
	 * @return
	 */
	public double[] optimal_frame_FEAT_adv(int L){
		double X = targetset.size();
		double Y = nontargetset.size();
		double Z = localtagsset.size();
		int large_f = (int) (X+Y)*10;
		
		double Tavg_obj_arr[] = new double[large_f];//两倍帧长应该差不多了
		
		for (int i = 5; i < Tavg_obj_arr.length; i++) {
			
			double f2 = i; //逐步增加帧长，逐个计算energe_obj_arr[i]
			
			double N_k = f2 * (1.0 - Math.pow(Math.E, -X/f2));   //key slot 的数目。
			double N_b2 = N_k*( 1 + Math.log(f2/N_k)/Math.log(2)); //pre-selection 阶段比特数
			double T_pre = TimingScheme.t_lambda + N_b2*TimingScheme.t_id/96;//1. pre-selection time overhead
			
			double T_ver = TimingScheme.t_lambda + N_k*TimingScheme.t_id/96; //2. verifying phase overhead
			
			//phase 3. 未验证的时隙数目
			double N_u2 = f2* (1.0 - Math.pow(Math.E, -X/f2) - Math.pow(Math.E, -(X+Y)/f2)*X/f2)*(1.0 - Math.pow(Math.E, -Z/f2));
			
			double N_bits_hop = N_u2*( 1 + Math.log(N_k/N_u2)/Math.log(2)); //pre-selection 阶段比特数
			
			//time phase 3:
			double T_hop = TimingScheme.t_lambda + N_bits_hop*TimingScheme.t_id/96;
			
			double T_round_2 = T_pre + T_ver + T_hop;
			// 所有能够验证的key tag数目
			double N_key_2 = X*Math.pow(Math.E, -(X+Y)/f2)* ( 1.0- Math.pow(Math.E , -Z/f2) ) + X*Math.pow(Math.E , -Z/f2 );
		
	
			Tavg_obj_arr[i] = T_round_2/N_key_2; // 这个就是目标函数。
		}
		
		double min_T_avg = Tavg_obj_arr[5]; //最小化验证一个key标签的时间开销
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
		
		
		return optf_Tavg_min; //输出最优帧长，and min_Tavg
		
	}
}
