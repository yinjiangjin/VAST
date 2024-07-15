package RFID;

import java.util.*;
/**
 * four phase:
 * 1   allocate phase: allocate each target tag in a single slot
 * 2   fingerprint filter phase: send a fingerprint bitvector to filter nontarget tags (when the fingerprint is different.)
 * 3   polling phase: polling the remainder nontarget tags
 * 4   collect phase: Collect target tag information in order
 *
 * @author Jiangjin
 *
 */
public class New_PIC extends ICNPBasic {
	
	HashSet<Tag> tempTarget = new HashSet<Tag>(); //
	HashSet<Tag> tempNOTarget = new HashSet<Tag>();
	HashSet<Tag> tempLocal = new HashSet<Tag>();
	
	public New_PIC(){
		ptname = new String("New_PIC");
	}
	
	double execute(){
		
		//Utils.logAppend("collecttotalnumber-New_PIC.txt", collecttotalnumber+"\n");
		//清空

		tcollection = 0;  
		
		int W = targetset.size();
		int N = nontargetset.size();
		int fingerLength = (int)( 6 - (Math.log((double)(W)/N)/Math.log(2))); //指纹长度

		HashSet<Tag> local = new HashSet<Tag>(); 
		local.clear();
		local.addAll(targetset);
		local.addAll(nontargetset);
		localtagsset.retainAll(local); //由于一个标签可以被多个阅读器覆盖，有些标签可能已经收集信息了。保留交集的部分. 对单阅读器无影响不用修改
		if(localtagsset.size()==0){
			return 0;
		}
		while(!targetset.isEmpty()){
			oneround(fingerLength);
		}
		return tcollection;
	}
	
	
	private void oneround(int fingerLength) {
		
		
		
		int f = targetset.size(); //帧长设置为目标标签的数目，单时隙更多。 
		int seed = rand.nextInt();
		tcollection +=  Math.ceil(f/96.0)*TimingScheme.t_id; //分配阶段的时间。
		//根据帧长f和seed得到标签的回复
		int[] reply1 = getReply(f,seed,targetset);
		int[] reply2 = getReply(f,seed,nontargetset); // 回复时隙已经赋值
		int[] reply3 = getReply(f,seed,localtagsset);
		
		tempTarget.clear();
		tempNOTarget.clear();
		tempLocal.clear();
		
		for (Tag t1 : targetset) {
			if(reply1[t1.slotidx]==1){
				tempTarget.add(t1); // 如果回复为1。
			}
		}
		
		for (Tag t2 : nontargetset) {
			if(reply1[t2.slotidx]==1){
				tempNOTarget.add(t2); // 如果回复为1
			}
		}
		
		for (Tag t3 : localtagsset) {
			if(reply1[t3.slotidx]==1){
				tempLocal.add(t3); // 如果回复为1。
			}
		}
		int colNum = tempTarget.size();
		
		tcollection +=TimingScheme.t_lambda;
		tcollection +=  Math.ceil((fingerLength*colNum)/96.0)*TimingScheme.t_id; //finger阶段的时间。
		
		ttime_filtering +=TimingScheme.t_lambda; //统计ordinary过滤的时间
		ttime_filtering += Math.ceil((fingerLength*colNum)/96.0)*TimingScheme.t_id; //统计ordinary过滤的时间
		
		System.out.println("time finger:"+Math.ceil((fingerLength*colNum)/96.0)*TimingScheme.t_id);
		
		//polling phase
		int pollNum = 0;
		for(Tag t4: tempTarget){
			for(Tag t5:tempNOTarget){
				if(t5.tagfingerprint==t4.tagfingerprint&&t5.slotidx==t4.slotidx){ //时隙相同且指纹相同，需要轮询
					pollNum++;
				}
			}
		}
		tcollection +=  pollNum*TimingScheme.t_id; //polling time
		
		ttime_filtering += pollNum*TimingScheme.t_id; //统计ordinary过滤的时间
		
		System.out.println("time pollNum:"+pollNum);
		
		
		// information collection
		for (Tag t6 : tempTarget) {
			if(tempLocal.contains(t6)){
				
				if( targetcollected.contains(t6) ){
					tcollection += TimingScheme.t_e; //如果标签被收集过了，就不用收集了。
				
				}else{
					
					targetcollected.add( t6 ); //收集这个标签的信息。
					
					tcollection += (TimingScheme.interval+TimingScheme.onebit*infLength);
					collecttotalnumber +=1;
					sumtcollection += tcollection + eachcolortime ; //收集的时间
				}
			}else{
				tcollection += TimingScheme.t_e;
			}
		}
		
		targetset.removeAll(tempTarget);
		nontargetset.removeAll(tempNOTarget);
		localtagsset.removeAll(tempLocal);
		
	}

}
