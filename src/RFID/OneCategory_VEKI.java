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
//	int numCategory = categoryNum_Basic; //获得类别数目
//	int categoryBitLength = categoryBitNum; //得到父类的数目
//	
//	int[] CategoryIDArr = new int[numCategory];
//	

	public OneCategory_VEKI(){
		ptname = new String("oneCategory_VEKI");
	}
	
	double execute(){
		
		if(!flag_key_missing){ //如果不是key标签丢失识别，即为全部标签识别，则可以设置ordinary为空
			localtagsset.removeAll(nontargetset);
			nontargetset.clear();
		}
		
		System.out.println("********start oneCategory_VEKI******** ");
		
		tcollection = 0;  
		TOTAL_bits = 0; //记录总的比特数
		
		
		System.out.println("nontargetset.size():"+nontargetset.size());
		System.out.println("targetset:"+targetset.size());
		System.out.println("localtagsset: "+localtagsset.size());
		
		HashSet<Tag> roundIdentifiedset = new HashSet<Tag>();
		int round =0 ;
		while(nontargetset.size()>0){ //当服务器目标标签被全部识别，过滤阶段终止
			double timeeachround = TimingScheme.t_lambda + (TimingScheme.t_id*targetset.size())/96.0; //每一轮过滤的时间相同
			//Utils.logAppend("filtering_time-VEKI-0.4.txt", "VEKI "+"\t"+ timeeachround+"\n");
			round++;
			VEKI_OrdinaryTagsDeactivation(targetset, nontargetset, localtagsset,round);
			//Utils.logAppend("collecttotalnumber-HTIC.txt", "HTIC "+collecttotalnumber+"\n");
		}
		
		System.out.println("start identification phase");
		System.out.println("targetset:"+targetset.size());
		System.out.println("localset:"+localtagsset.size());
	
		
		while(targetset.size()>0){ 
			
			roundIdentifiedset.clear();//清空已经被识别的上一轮缓存标签
			//识别标签并计算时间
			roundIdentifiedset.addAll(VEKI_KEYtagsIdentificationPhase(targetset,localtagsset));
		
		}
			
		totalMissingNumber_identified = missingset.size(); //把丢失标签总数记录到basic类中
		System.out.println("missing tag: "+ missingset.size());
		
		System.out.println("****End oneCategory_VEKI***** tcollection******:"+ tcollection+"total bit:"+ TOTAL_bits);
		return tcollection;
	}
	
	
	
	public void VEKI_OrdinaryTagsDeactivation(HashSet<Tag> targettagset, HashSet<Tag> nontargettagset, HashSet<Tag> localtagset,int round){
		
		
		ttime_filtering +=TimingScheme.t_lambda; //统计过滤阶段的时间
		
		tcollection+=TimingScheme.t_lambda; 
		int seed1 = rand.nextInt();
		
		int f1 = targettagset.size(); //最优帧长
		tcollection += (TimingScheme.t_id*f1)/96.0;
		TOTAL_bits += f1;
		
		ttime_filtering += (TimingScheme.t_id*f1)/96.0;//统计过滤阶段的时间
		
		int[] replyArrServerTarget = getReply(f1,seed1,targettagset);//就是用key标签做预映射的
		int[] replyArrServerNontarget = getReply(f1,seed1,nontargettagset);//
		int[] replyArrLocal = getReply(f1,seed1,localtagset);//
		
		tempNotarget.clear(); //用之前先清空
		for(Tag t: nontargettagset){//排除标签
			if( replyArrServerTarget[t.slotidx]==0 ){
				tempNotarget.add(t);
			}
		}
		
		tempLocal.clear();
		for(Tag t: localtagset){//排除标签
			if( replyArrServerTarget[t.slotidx]==0 ){
				tempLocal.add(t);
			}
		}
		
		
		
		nontargettagset.removeAll(tempNotarget);
		
		localtagsset.removeAll(tempLocal);
		
		
	//	Utils.logAppend("filtering_time-VEKI.txt-0.4", "VEKI "+"\t"+ round+"\t"+ tempNotarget.size()+"\t"+nontargettagset.size()+"\n");
		
		tempNotarget.clear(); //用完后清空
		tempLocal.clear();
	}
	
	/**
	 */
	public  HashSet<Tag> VEKI_KEYtagsIdentificationPhase(HashSet<Tag> targettagset, HashSet<Tag> localtagset){
		
		tcollection+=TimingScheme.t_lambda; 
		
		int seed1 = rand.nextInt();
		int f2 = targettagset.size(); //剩余的
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
				if(systemtagset.contains(t)){//系统存在该标签，即收到回复
					tcollection += TimingScheme.t_s;
				}else{
					tcollection += TimingScheme.t_e;
				}
				*/
			}
		}
		
		targettagset.removeAll(identifiedtagset); //移除已经识别的标签
		localtagset.removeAll(identifiedtagset); //移除已经识别的标签
		
		
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
		
		/*  得到missing 集合   */
		missingset.addAll(stagsetReconciled);   
		missingset.removeAll(ltagset); //求差集，就是missing 标签
		
		stagset.removeAll(stagsetReconciled); //remove the verified tags in server
		ltagset.removeAll(stagsetReconciled); //remove the verified tags in local
	
		return 0.0;  //
	}
	


}
