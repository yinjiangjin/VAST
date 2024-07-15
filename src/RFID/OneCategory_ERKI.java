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
		
		if(!flag_key_missing){ //�������key��ǩ��ʧʶ�𣬼�Ϊȫ����ǩʶ�����������ordinaryΪ��
			localtagsset.removeAll(nontargetset);
			nontargetset.clear();
		}
		
		int n_target = targetset.size();
		
		targetArray = new Arrayset[targetset.size()]; //����ΪĿ���ǩ��С��
		for (int i = 0; i < targetArray.length; i++) { 
			targetArray[i] =new Arrayset();
		}
		
		System.out.println("********start oneCategory_ERKI******** ");
		
		tcollection = 0;  
		TOTAL_bits = 0; //��¼�ܵı�����
		
		HashSet<Tag> roundIdentifiedset = new HashSet<Tag>();
		while(targetset.size()>0 ){//��Ŀ���ǩδ����ȫlabelʱ
			
			if(nontargetset.size()>0){ //ֻ�з�Ŀ���ǩ��Ŀ����0�Ż�ִ��bloom filter
			//1. deactivation phase
			int K = targetset.size();
			
			double fp = ERKI_calculatedFalsePositive();//�����bloom filter ���ż�������
			
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
			
		totalMissingNumber_identified = missingset.size(); //�Ѷ�ʧ��ǩ������¼��basic����
		//System.out.println("missing tag: "+ missingset.size());
		
		System.out.println("****End oneCategory_ERKI***** tcollection******:"+ tcollection+"total bit:"+ TOTAL_bits);
		return tcollection;
	}
	

	public void ERKI_DeactivationPhase(int f1, int h){
		
		tcollection+=TimingScheme.t_lambda; 
		
		ttime_filtering +=TimingScheme.t_lambda; //ͳ�ƹ��˽׶ε�ʱ��
	
		tcollection += (TimingScheme.t_id*f1)/96.0;
		TOTAL_bits += f1; 
		
		
		
		ttime_filtering += (TimingScheme.t_id*f1)/96.0;//ͳ�ƹ��˽׶ε�ʱ��
		
		int[] filter = new int[f1]; //��������
		int[] seeds = new int[h]; //seeds��Ŀ
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
			t.getSlots(f1, h, seeds); //ͬ����������ӣ�Ӧ�û���ͬ����hash
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
	 * (2) ERKI �ڶ��׶�
	 */
	public HashSet<Tag> ERKI_LabelingPhase(HashSet<Tag> targettagset, HashSet<Tag> nontargetset){
		
		tcollection+=TimingScheme.t_lambda; 
		ttime_filtering +=TimingScheme.t_lambda; //ͳ�ƹ��˽׶ε�ʱ��
		
		int seed1 = rand.nextInt();
		int f2 = targettagset.size() + nontargetset.size(); //ʣ���
		
		System.out.println("f2:"+f2);
		
		tcollection += (TimingScheme.t_id*f2)/96.0; //time. broadcast f2 bits 
		TOTAL_bits += f2;
		
		ttime_filtering += (TimingScheme.t_id*f2)/96.0;//�ڶ��׶�Ҳ��
		
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
				targetArr_index++; //����1��
				LabelTagset.add(t);
				
			}
		}
		targettagset.removeAll(LabelTagset); //�Ƴ��Ѿ�ʶ��ı�ǩ
		
		return LabelTagset; //return the tag set 
	}
	
	
	public void ERKI_VerificationPhase(int n_targettagset, HashSet<Tag> localtagset){
		
		//�ȼ���missing����
		double missingRatio = 1 - 1.0*localtagset.size()/n_targettagset; //missing����
		int W =0; //ȷ��w��ֵ��
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
		//1. ��һ�� ����W��ֵ����ʶ���ǩ
		boolean flag =true;
		
		tempTargetset.clear(); //�����
		HashSet<Tag> tempTagset = new HashSet<Tag>(); //
		
		for (int kk= 0; kk < targetArray.length; kk=kk+W) {//����ΪW
			
			tempTagset.clear();
			
			for(int jj = 0; jj < W; jj++){ //
				for(Tag t: targetArray[kk+jj].tagset ){  // ����ط���kk+jj��Ҫ����һ�¡�
					tempTagset.add(t);
					if(localtagsset.contains(t)){
						flag = false;
					}
				}
			}
			
			
			if(flag){
				tcollection += TimingScheme.t_e; //�ջظ�
				TOTAL_bits += 1; //�ǿջظ�����1���ء�
				
			}else{
				tempTargetset.addAll(tempTagset);
				tcollection += TimingScheme.t_s; //�ǿջظ�
				TOTAL_bits += 1; //�ǿջظ�����1���ء�
			}
			
		}
		
		//2. ������ʶ��missing��
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
		
		/*  �õ�missing ����   */
		missingset.addAll(stagsetReconciled);   
		missingset.removeAll(ltagset); //��������missing ��ǩ
		
		stagset.removeAll(stagsetReconciled); //remove the verified tags in server
		ltagset.removeAll(stagsetReconciled); //remove the verified tags in local
	
		return 0.0;  //
	}
	

	/**
	 * 
	 * @return ���ŵļ����������á�
	 */
	public double ERKI_calculatedFalsePositive(){
		
		double E = Math.E;
		
		int Ki = targetset.size();
		int Di = nontargetset.size();
		
		double []pArr = new double[99];
		for (int i = 0; i < pArr.length; i++) {
			pArr[i]=0.01 + i*0.01; //��0.01�𣬲���Ϊ0.01.
		}
		
		//double []Tavg_deact_derivation = new double[99]; //��¼����ֵ��ע�����ﲻ�Ǿ�ֵ���ǵ���ֵ�������ҵ���ֵΪ0�ĵ㡣
		
		double []value_arr = new double[99]; 
		
		for (int ii = 0; ii < pArr.length; ii++) {
			
			
			/**���ݹ�ʽ12*/
		//	double alpha = Math.pow( 1 - Math.pow(Math.E,-Math.log(2)) , -Math.log(pArr[ii])/Math.log(2) );
		//	double beta = 96* Math.log(2)*Math.log(2)/TimingScheme.t_id;
			
		//	double term1_fenzi = Math.pow(Math.E, Math.log(pArr[ii]))*Ki*( 1.48 - Math.log( pArr[ii] ) );
			
		//	double term2_fenzi = 0.48*Di*Math.pow(Math.E, Math.log(pArr[ii]))-Ki;
			
		//	double fenmu = beta* Di* (1-alpha)* Di*(1-alpha);
			
		//	Tavg_deact_derivation [ii] = (term1_fenzi+ term2_fenzi)/fenmu;
			
			/**
			 int opt_index = -10;
			for (int index = 0; index < Tavg_deact_derivation.length; index++) {
			if( Tavg_deact_derivation[index]>0){ //�������ֵ����0
				opt_index = index; //�ҵ���һ������0�� ����ֵ������
				System.out.println("opt_index:"+opt_index);
				break;
			}
		} 
			 
			 */
			
			
			/**���ݹ�ʽ14*/
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
