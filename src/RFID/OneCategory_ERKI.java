package RFID;

import java.util.*;

/**
 * Efficient and robust missing key tags identification for large-scale RFID systems -Chu Chu  2022 Digital communications and networks (DCN)
 * the following coding is the iERKI protocol.
 * @author Jiangjin
 *
 */
public class OneCategory_ERKI extends ICNPBasic {
	
	HashSet<Tag> tempTargetset = new HashSet<Tag>(); //
	HashSet<Tag> tempNotarget = new HashSet<Tag>();
	HashSet<Tag> tempLocal = new HashSet<Tag>();
	
	HashSet<Tag> missingset = new HashSet<Tag>();

	Arrayset[] targetArray = null; 
	int targetArr_index = 0;
	public OneCategory_ERKI(){
		ptname = new String("oneCategory_ERKI");
	}
	
	double execute(){
		
		if(!flag_key_missing){ //如果不是key标签丢失识别，即为全部标签识别，则可以设置ordinary为空
			localtagsset.removeAll(nontargetset);
			nontargetset.clear();
		}
		
		int n_target = targetset.size();
		
		targetArray = new Arrayset[targetset.size()]; //设置为目标标签大小。
		for (int i = 0; i < targetArray.length; i++) { 
			targetArray[i] =new Arrayset();
		}
		
		System.out.println("********start oneCategory_ERKI******** ");
		
		tcollection = 0;  
		TOTAL_bits = 0; //记录总的比特数
		
		HashSet<Tag> roundIdentifiedset = new HashSet<Tag>();
		while(targetset.size()>0 ){//当目标标签未被完全label时
			
			if(nontargetset.size()>0){ //只有非目标标签数目大于0才会执行bloom filter
			//1. deactivation phase
			int K = targetset.size();
			
			double fp = ERKI_calculatedFalsePositive();//计算出bloom filter 最优假阳性率
			
			int f1 = (int) (-K*Math.log(fp)/(Math.log(2)*Math.log(2)));
			//System.out.println("f1:"+f1);
			
			int h = (int) (f1*Math.log(2)/K); //
			
			//System.out.println("h:"+h);
			
			//System.out.println("nontargetset.size():"+nontargetset.size());
			//System.out.println("targetset.size():"+targetset.size());
			
			ERKI_DeactivationPhase(f1, h);
			}
			
			//System.out.println("********nontargetset.size():"+nontargetset.size());
			//System.out.println("********targetset.size():"+targetset.size());
			
			roundIdentifiedset.clear();
			//2. labeling phase 
			roundIdentifiedset.addAll( ERKI_LabelingPhase(targetset, nontargetset) );
		}
		
		//3. verification phase
		ERKI_VerificationPhase(n_target, localtagsset);
			
		totalMissingNumber_identified = missingset.size(); //把丢失标签总数记录到basic类中
		//System.out.println("missing tag: "+ missingset.size());
		
		System.out.println("****End oneCategory_ERKI***** tcollection******:"+ tcollection+"total bit:"+ TOTAL_bits);
		return tcollection;
	}
	

	public void ERKI_DeactivationPhase(int f1, int h){
		
		tcollection+=TimingScheme.t_lambda; 
		
		ttime_filtering +=TimingScheme.t_lambda; //统计过滤阶段的时间
	
		tcollection += (TimingScheme.t_id*f1)/96.0;
		TOTAL_bits += f1; 
		
		
		
		ttime_filtering += (TimingScheme.t_id*f1)/96.0;//统计过滤阶段的时间
		
		int[] filter = new int[f1]; //向量长度
		int[] seeds = new int[h]; //seeds数目
		//init the filter
		for(int i = 0 ; i < filter.length; i++){
			filter[i] = 0;
		}
		//get the k seeds
		for(int i = 0; i < seeds.length; i++){
			seeds[i] = rand.nextInt();
		}
		//for all tags in local, let them select k replying slots with the k seeds, and construct the virtual filter
		for(Tag t:targetset){
			t.getSlots(f1, h, seeds);
			for(int idx : t.slots){
				filter[idx]++;
			}
		}

		tempremove.clear();
		for(Tag t:nontargetset){
			t.getSlots(f1, h, seeds); //同样的随机种子，应该会用同样的hash
			for(int idx : t.slots){
				if(filter[idx]==0){
					tempremove.add(t);
				}
			}
		}
		localtagsset.removeAll(tempremove); 
		nontargetset.removeAll(tempremove);
	}
	
	/**
	 * (2) ERKI 第二阶段
	 */
	public HashSet<Tag> ERKI_LabelingPhase(HashSet<Tag> targettagset, HashSet<Tag> nontargetset){
		
		tcollection+=TimingScheme.t_lambda; 
		ttime_filtering +=TimingScheme.t_lambda; //统计过滤阶段的时间
		
		int seed1 = rand.nextInt();
		int f2 = targettagset.size() + nontargetset.size(); //剩余的
		
		System.out.println("f2:"+f2);
		
		tcollection += (TimingScheme.t_id*f2)/96.0; //time. broadcast f2 bits 
		TOTAL_bits += f2;
		
		ttime_filtering += (TimingScheme.t_id*f2)/96.0;//第二阶段也是
		
		HashSet<Tag> temp_T_nT = new HashSet<Tag>();
		temp_T_nT.clear();
		temp_T_nT.addAll(targettagset);
		temp_T_nT.addAll(nontargetset);
		
		int[] replyArrServer = getReply(f2,seed1,temp_T_nT);//expected reply vector when server tags is pre-mapped; 
	
		HashSet<Tag> LabelTagset = new HashSet<Tag>(); //record the tags that can be identified
		
		//Time 2: singleton slot tag reply
		for(Tag t: targettagset){//add the tags in the singleton slots
			if(replyArrServer[t.slotidx]==1){
				targetArray[targetArr_index].tagset.add(t);
				targetArr_index++; //自增1。
				LabelTagset.add(t);
				
			}
		}
		targettagset.removeAll(LabelTagset); //移除已经识别的标签
		
		return LabelTagset; //return the tag set 
	}
	
	
	public void ERKI_VerificationPhase(int n_targettagset, HashSet<Tag> localtagset){
		
		//先计算missing比例
		double missingRatio = 1 - 1.0*localtagset.size()/n_targettagset; //missing比例
		int W =0; //确定w的值。
		if(missingRatio<0.729){
			W = 1;	
		}else if(missingRatio<0.749){
			W = 3;
		}else if(missingRatio<0.801){
			W = 4; 
		}else if(missingRatio<0.833){
			W = 5; 
		}else if(missingRatio<0.855){
			W = 6; 
		}else if(missingRatio<0.875){
			W = 7; 
		}else if(missingRatio<0.887){
			W = 8; 
		}else if(missingRatio<0.899){
			W = 9;
		}else{
			W = 10;
		}
		//1. 第一轮 根据W的值有序识别标签
		boolean flag =true;
		
		tempTargetset.clear(); //先清空
		HashSet<Tag> tempTagset = new HashSet<Tag>(); //
		
		for (int kk= 0; kk < targetArray.length; kk=kk+W) {//步长为W
			
			tempTagset.clear();
			
			for(int jj = 0; jj < W; jj++){ //
				for(Tag t: targetArray[kk+jj].tagset ){  // 这个地方的kk+jj需要测试一下。
					tempTagset.add(t);
					if(localtagsset.contains(t)){
						flag = false;
					}
				}
			}
			
			
			if(flag){
				tcollection += TimingScheme.t_e; //空回复
				TOTAL_bits += 1; //非空回复传输1比特。
				
			}else{
				tempTargetset.addAll(tempTagset);
				tcollection += TimingScheme.t_s; //非空回复
				TOTAL_bits += 1; //非空回复传输1比特。
			}
			
		}
		
		//2. 随后独立识别missing。
		int presentNum = localtagsset.size();
		int absentNum = tempTargetset.size() - localtagsset.size();
		tcollection += TimingScheme.t_s * presentNum;
		tcollection += TimingScheme.t_e * absentNum ;
		
		TOTAL_bits += ( presentNum + absentNum ); 
		
		
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
	 *  ERKI remove identified tags
	 *  stagset is the  server tag set can be verfied
	 *  ltagset is the  local tag set 
	 *  return time overhead
	 */
	public double ERKI_remove(HashSet<Tag> stagset, HashSet<Tag> ltagset, HashSet<Tag> stagsetReconciled){
	
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
	 * @return 最优的假阳性率设置。
	 */
	public double ERKI_calculatedFalsePositive(){
		
		double E = Math.E;
		
		int Ki = targetset.size();
		int Di = nontargetset.size();
		
		double []pArr = new double[99];
		for (int i = 0; i < pArr.length; i++) {
			pArr[i]=0.01 + i*0.01; //从0.01起，步长为0.01.
		}
		
		//double []Tavg_deact_derivation = new double[99]; //记录导数值。注意这里不是均值，是导数值；所以找导数值为0的点。
		
		double []value_arr = new double[99]; 
		
		for (int ii = 0; ii < pArr.length; ii++) {
			
			
			/**依据公式12*/
		//	double alpha = Math.pow( 1 - Math.pow(Math.E,-Math.log(2)) , -Math.log(pArr[ii])/Math.log(2) );
		//	double beta = 96* Math.log(2)*Math.log(2)/TimingScheme.t_id;
			
		//	double term1_fenzi = Math.pow(Math.E, Math.log(pArr[ii]))*Ki*( 1.48 - Math.log( pArr[ii] ) );
			
		//	double term2_fenzi = 0.48*Di*Math.pow(Math.E, Math.log(pArr[ii]))-Ki;
			
		//	double fenmu = beta* Di* (1-alpha)* Di*(1-alpha);
			
		//	Tavg_deact_derivation [ii] = (term1_fenzi+ term2_fenzi)/fenmu;
			
			/**
			 int opt_index = -10;
			for (int index = 0; index < Tavg_deact_derivation.length; index++) {
			if( Tavg_deact_derivation[index]>0){ //如果导数值大于0
				opt_index = index; //找到第一个大于0的 导数值索引。
				System.out.println("opt_index:"+opt_index);
				break;
			}
		} 
			 
			 */
			
			
			/**依据公式14*/
			value_arr[ii] = Math.pow(Math.E, Math.log(pArr[ii]))*(1.48*Ki - Ki* Math.log(pArr[ii]) + 0.48*Di) - Ki;
			value_arr[ii] = Math.abs(value_arr[ii]);
		}
		
		int opt_index = 0;
		for (int index = 0; index < value_arr.length; index++) {
			if( value_arr[index] < value_arr[opt_index]){ //
				opt_index= index; //
			}
		}
		
		
		//System.out.println("opt_fp: "+ pArr[opt_index]);
		
		return pArr[opt_index];
	}
}
