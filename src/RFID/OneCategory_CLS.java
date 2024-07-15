package RFID;

import java.util.*;

/**
 * Collisions are Preferred: RFID-based Stocktaking with a High Missing Rate - Jiannong Cao  2020 TMC
 * @author Jiangjin
 *
 */
public class OneCategory_CLS extends ICNPBasic {
	
	HashSet<Tag> tempTarget = new HashSet<Tag>(); //
	HashSet<Tag> tempNotarget = new HashSet<Tag>();
	HashSet<Tag> tempLocal = new HashSet<Tag>();
	
	HashSet<Tag> missingset = new HashSet<Tag>();

	//HashSet<int[]> tempLocal2 = new HashSet<int[]>();
	
//	int numCategory = categoryNum_Basic; //获得类别数目
//	int categoryBitLength = categoryBitNum; //得到父类的数目。
	
//	int[] CategoryIDArr = new int[numCategory];
	

	public OneCategory_CLS(){
		ptname = new String("Onecategory_CLS");
	}
	
	double execute(){
		
		System.out.println("********start Onecategory_CLS******** ");
		
		tcollection = 0;  
		TOTAL_bits = 0; //记录总的比特数
		
		
		
		while(serverset.size()>0){
			
			tcollection+=TimingScheme.t_lambda; 
			
			int tagnum_ser = serverset.size();
			int tagnum_sys = localtagsset.size();
			int f = CLS_optimalf(tagnum_ser,tagnum_sys);//获得最优帧长
			
			HashSet<Tag> tempIdentifiedSet = new HashSet<Tag>();
			//1. slot allocation phase, 2 filter vector generation phase, and 3. tag verifying phase
			tempIdentifiedSet.addAll(phase123(f, serverset,localtagsset));
			
			double x = tag_remove(serverset,localtagsset,tempIdentifiedSet);

		}
		
		totalMissingNumber_identified = missingset.size(); //把丢失标签总数记录到basic类中
		System.out.println("missing tag: "+ missingset.size());
		
		System.out.println("****End Onecategory_CLS***** tcollection******:"+ tcollection+"total bit:"+ TOTAL_bits);
		return tcollection;
	}
	
	

	/**
	 * calculate the optimize frame size: according to Eq(13) in paper CLS.
	 * @param N_server
	 * @param N_system
	 * @return
	 */
	public int CLS_optimalf(int N_server, int N_system){
		
		double t_id  = TimingScheme.t_id;
		double t_s = TimingScheme.t_s;
		double E = Math.E;
		
		double []Arr_Tavg = new double[10+N_server]; //只是有10个值
		
		double p = 1.0*(N_server-N_system)/N_server; //missing rate
		
		for (int framesize = 0; framesize < Arr_Tavg.length; framesize++) {
			double rho = 1.0*N_server/framesize;
			
			double fenzi = t_id/96*(1 + Math.pow(E,-rho)+rho*Math.pow(E,-rho)) + (1- Math.pow(E,-rho))*t_s;
			double fenmu = rho*p*Math.pow(E,-rho*(1-p)) + (1-p)*rho*Math.pow(E,-rho);
			Arr_Tavg[framesize] = fenzi/fenmu;
		}
		
		
		int optf = 5;
		double MinAverage = Arr_Tavg[10];

		for (int i = 5; i < Arr_Tavg.length; i++) {
			if(Arr_Tavg[i]<MinAverage){
				MinAverage = Arr_Tavg[i]; //记录最小值
				optf = i;  //记录最小值对应索引(帧长)	
			}
		}
		
		if(optf<5){
			optf=5;
		}
		return optf; //返回最优帧长
		
	}
	
	/**
	 * the procedure of CLS (include phase 1, 2, and 3)
	 * @param f
	 * @param ser_tagset
	 * @param systemtagset
	 * @return identified tag set
	 */
	public  HashSet<Tag> phase123(int f, HashSet<Tag> ser_tagset, HashSet<Tag> systemtagset){
		
		int seed1 = rand.nextInt();
		int[] replyArrServer = getReply(f,seed1,ser_tagset);//expected reply vector when server tags is pre-mapped; 
		int[] Actual_replyArrSystem = getReply(f,seed1,systemtagset);
	
		//Time 1: count the total bit
		int totalbit_vector = 0;
		for (int i = 0; i < replyArrServer.length; i++) {
			if(replyArrServer[i]>1){  //冲突时隙用1比特表示
				totalbit_vector += 1;
			}else{
				totalbit_vector += 2;
			}
		}
		tcollection += TimingScheme.t_id*totalbit_vector/96.0;//广播vector的时间
		
		TOTAL_bits += totalbit_vector;
		
		//Time2: tag 收到的回复总时间
		for (int i = 0; i < Actual_replyArrSystem.length; i++) {
			if(replyArrServer[i] > 0){//空时隙不会实际执行
				tcollection += 0.1; //
				tcollection += TimingScheme.t_s;
				
				TOTAL_bits += 1;
				
//				if(Actual_replyArrSystem[i]==0){
//					tcollection += TimingScheme.t_e;
//				}else{
//					tcollection += TimingScheme.t_s;
//				}
			}
		}
		
		
		
		HashSet<Tag> identifiedTags = new HashSet<Tag>(); //record the tags that can be identified 
		
		//1. singleton 
		for(Tag t: ser_tagset){//add the tags in the pre-mapped singleton slots
			if(replyArrServer[t.slotidx]==1){
				identifiedTags.add(t);
			}
		}
		
		//2. collision slots
		HashSet<Tag> tempSet_ser = new HashSet<Tag>(); //server
		
		for (int i = 0; i < replyArrServer.length; i++) {
			//服务器标签预映射为冲突时隙，实际没有收到回复，可以验证该时隙的所有标签
			if ( replyArrServer[i] > 1 && Actual_replyArrSystem[i]==0  ){ //
				tempSet_ser.clear();
				tempSet_ser.addAll(findTagSet(i, ser_tagset));
				identifiedTags.addAll(tempSet_ser); //加入到已经识别的标签集合中
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
	 *  (3) presence verifying phase
	 *  stagset is the  server tag set can be verfied
	 *  ltagset is the  local tag set 
	 *  return time overhead
	 */
	public double tag_remove(HashSet<Tag> ser_tagset, HashSet<Tag> sys_tagset, HashSet<Tag> stagsetReconciled){
	
		
		int n= stagsetReconciled.size(); 
		
		/*  得到missing 集合   */
		missingset.addAll(stagsetReconciled);   
		missingset.removeAll(sys_tagset); //求差集，就是missing 标签。移除相同的元素
		
		ser_tagset.removeAll(stagsetReconciled); //remove the verified tags in server
		sys_tagset.removeAll(stagsetReconciled); //remove the verified tags in local
		
		return 0.0;  //
		
	}
	

}
