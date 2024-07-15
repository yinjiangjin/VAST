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
		//���

		tcollection = 0;  
		
		int W = targetset.size();
		int N = nontargetset.size();
		int fingerLength = (int)( 6 - (Math.log((double)(W)/N)/Math.log(2))); //ָ�Ƴ���

		HashSet<Tag> local = new HashSet<Tag>(); 
		local.clear();
		local.addAll(targetset);
		local.addAll(nontargetset);
		localtagsset.retainAll(local); //����һ����ǩ���Ա�����Ķ������ǣ���Щ��ǩ�����Ѿ��ռ���Ϣ�ˡ����������Ĳ���. �Ե��Ķ�����Ӱ�첻���޸�
		if(localtagsset.size()==0){
			return 0;
		}
		while(!targetset.isEmpty()){
			oneround(fingerLength);
		}
		return tcollection;
	}
	
	
	private void oneround(int fingerLength) {
		
		
		
		int f = targetset.size(); //֡������ΪĿ���ǩ����Ŀ����ʱ϶���ࡣ 
		int seed = rand.nextInt();
		tcollection +=  Math.ceil(f/96.0)*TimingScheme.t_id; //����׶ε�ʱ�䡣
		//����֡��f��seed�õ���ǩ�Ļظ�
		int[] reply1 = getReply(f,seed,targetset);
		int[] reply2 = getReply(f,seed,nontargetset); // �ظ�ʱ϶�Ѿ���ֵ
		int[] reply3 = getReply(f,seed,localtagsset);
		
		tempTarget.clear();
		tempNOTarget.clear();
		tempLocal.clear();
		
		for (Tag t1 : targetset) {
			if(reply1[t1.slotidx]==1){
				tempTarget.add(t1); // ����ظ�Ϊ1��
			}
		}
		
		for (Tag t2 : nontargetset) {
			if(reply1[t2.slotidx]==1){
				tempNOTarget.add(t2); // ����ظ�Ϊ1
			}
		}
		
		for (Tag t3 : localtagsset) {
			if(reply1[t3.slotidx]==1){
				tempLocal.add(t3); // ����ظ�Ϊ1��
			}
		}
		int colNum = tempTarget.size();
		
		tcollection +=TimingScheme.t_lambda;
		tcollection +=  Math.ceil((fingerLength*colNum)/96.0)*TimingScheme.t_id; //finger�׶ε�ʱ�䡣
		
		ttime_filtering +=TimingScheme.t_lambda; //ͳ��ordinary���˵�ʱ��
		ttime_filtering += Math.ceil((fingerLength*colNum)/96.0)*TimingScheme.t_id; //ͳ��ordinary���˵�ʱ��
		
		System.out.println("time finger:"+Math.ceil((fingerLength*colNum)/96.0)*TimingScheme.t_id);
		
		//polling phase
		int pollNum = 0;
		for(Tag t4: tempTarget){
			for(Tag t5:tempNOTarget){
				if(t5.tagfingerprint==t4.tagfingerprint&&t5.slotidx==t4.slotidx){ //ʱ϶��ͬ��ָ����ͬ����Ҫ��ѯ
					pollNum++;
				}
			}
		}
		tcollection +=  pollNum*TimingScheme.t_id; //polling time
		
		ttime_filtering += pollNum*TimingScheme.t_id; //ͳ��ordinary���˵�ʱ��
		
		System.out.println("time pollNum:"+pollNum);
		
		
		// information collection
		for (Tag t6 : tempTarget) {
			if(tempLocal.contains(t6)){
				
				if( targetcollected.contains(t6) ){
					tcollection += TimingScheme.t_e; //�����ǩ���ռ����ˣ��Ͳ����ռ��ˡ�
				
				}else{
					
					targetcollected.add( t6 ); //�ռ������ǩ����Ϣ��
					
					tcollection += (TimingScheme.interval+TimingScheme.onebit*infLength);
					collecttotalnumber +=1;
					sumtcollection += tcollection + eachcolortime ; //�ռ���ʱ��
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
