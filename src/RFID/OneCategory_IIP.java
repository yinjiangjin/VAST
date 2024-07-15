package RFID;

import java.util.*;

/**
 * Identifying the Missing Tags in a Large RFID System - MobiHoc  2010 
 * @author Jiangjin
 *
 */
public class OneCategory_IIP extends ICNPBasic {
	
	HashSet<Tag> tempTarget = new HashSet<Tag>(); //
	HashSet<Tag> tempNotarget = new HashSet<Tag>();
	HashSet<Tag> tempLocal = new HashSet<Tag>();
	
	HashSet<Tag> missingset = new HashSet<Tag>();

	//HashSet<int[]> tempLocal2 = new HashSet<int[]>();
	
//	int numCategory = categoryNum_Basic; //获得类别数目
//	int categoryBitLength = categoryBitNum; //得到父类的数目。
//	
//	int[] CategoryIDArr = new int[numCategory];
	

	public OneCategory_IIP(){
		ptname = new String("oneCategory_IIP");
	}
	
	double execute(){
		
		System.out.println("********start oneCategory_IIP******** ");
		
		tcollection = 0;  
		TOTAL_bits = 0; //记录总的比特数
		
		while(serverset.size()>0){
						
			int tagnum = serverset.size();
			int f = optimalFrameS(tagnum);
			
			HashSet<Tag> tempIdentifiedSet = new HashSet<Tag>();
			//加上前frame 和 后 frame的时间
			tcollection+=preframeVector(f);
			tcollection+=postframeVector(f);
			
			TOTAL_bits += 2*f;
			
			
			tempIdentifiedSet.addAll(IIP_identify(f, serverset,localtagsset));
			//phase 3
			double x = IIP_remove(serverset,localtagsset,tempIdentifiedSet);

		}
		
		totalMissingNumber_identified = missingset.size(); //把丢失标签总数记录到basic类中
		System.out.println("missing tag: "+ missingset.size());
		
		System.out.println("****End oneCategory_IIP***** tcollection******:"+ tcollection+"total bit:"+ TOTAL_bits);
		return tcollection;
	}
	
	
	


	/**
	 * IIP optimal frame size setting (done)
	 * @param N  number of tag 

	 * @return f --- frame size
	 */
	public int optimalFrameS(int N_n ){
		
		double rho = 1.516;
		double f = N_n/1.516;
		if(f<5){
			f=5;
		}
		
		return (int)f;
		
	}
	
	/**
	 * pre-frame vector
	 * @param framesize
	 * @return time for broadcasting the pre-frame vector
	 */
	double preframeVector(int framesize){
		
		double preframeTime = (1.0*framesize/96)*TimingScheme.t_id;
		return preframeTime;
	}
	
	/**
	 * post-frame vector
	 * @param framesize
	 * @return time for broadcasting the post-frame vector
	 */
	double postframeVector(int framesize){
		
		double postframeTime = (1.0*framesize/96)*TimingScheme.t_id;
		return postframeTime;
	}
	
	/**
	 *  identify missing and calculate time overhead slot by slot
	 *  
	 */
	public  HashSet<Tag> IIP_identify(int f, HashSet<Tag> stagset, HashSet<Tag> systemtagset){
		
		tcollection += TimingScheme.t_lambda; 
		int seed1 = rand.nextInt();
		System.out.println("IIP frame size: "+f);
		int[] replyArrServer = getReply(f,seed1,stagset);//expected reply vector when server tags is pre-mapped; 
		int[] Pre_replyArrSystem = getReply(f,seed1,systemtagset);
	
		//1. count the total time of empty slots
		for (int i = 0; i < replyArrServer.length; i++) {
			if(replyArrServer[i]==0){ //empty slot 
				tcollection += 0.1; //
				tcollection += TimingScheme.t_e;
				TOTAL_bits += 1;
			}
		}
		
		HashSet<Tag> identifiedTags = new HashSet<Tag>(); //record the tags that can be identified 
		
		//2. singleton 
		for(Tag t: stagset){//add the tags in the pre-mapped singleton slots
			if(replyArrServer[t.slotidx]==1){
				identifiedTags.add(t);
				if(systemtagset.contains(t)){ //如果这个标签存在于系统下，也就是说阅读器收到回复
					tcollection += 0.1; //
					tcollection += TimingScheme.t_s;
					TOTAL_bits += 1;
				}else {// 如果阅读器没有收到回复
					tcollection += 0.1; //
					tcollection += TimingScheme.t_e;
					TOTAL_bits += 1;
				}
			}
		}
		
		//3. Reconcile collisions
		int seed2 = rand.nextInt();
		
		HashSet<Tag> tempSet_ser = new HashSet<Tag>(); //server
		
		HashSet<Tag> tempSet_sys = new HashSet<Tag>(); //system
		
		for (int i = 0; i < replyArrServer.length; i++) {
			if ( replyArrServer[i] >1 ){ //collision slots
				tempSet_ser.clear();
				tempSet_ser.addAll(findTagSet(i, stagset));
				
				tempSet_sys.clear();
				tempSet_sys.addAll(findTagSet(i, systemtagset));//找到系统中预先回复在该时隙的标签集合
				
				//frame size = 2, where 1=回复，0=沉默
				int[] replyArrReconcile_ser = getReply2(2,seed2,tempSet_ser);//obtain the reconcile slot
				
				int[] replyArrReconcile_sys = getReply2(2,seed2,tempSet_sys);//
				
				
				//whether or not reconciled by seed2?
				if(replyArrReconcile_ser[1]==1){ //reconciled singleton slot. 确定可以验证丢失与否
					identifiedTags.addAll(findTagSet2(1, tempSet_ser)); //加入到识别的标签集合
					tcollection += 0.1; //
					tcollection += TimingScheme.t_e;
					TOTAL_bits += 1;
				}else if(replyArrReconcile_ser[1]>1){//still collision
					if( replyArrReconcile_sys[1] == 0 ){ //虽然服务器标签依旧冲突，但是没有收到回复。
						/* 这个地方实际可以移除标签的，暂时先不加了，时间：20220608*/
						tcollection += 0.1; //
						tcollection += TimingScheme.t_e;
						TOTAL_bits += 1;
					}else{
						tcollection += 0.1; //
						tcollection += TimingScheme.t_s;
						TOTAL_bits += 1;
					}
					
				}else{//empty slots
					tcollection += 0.1; //
					tcollection += TimingScheme.t_e;//服务器标签为空时隙
					TOTAL_bits += 1;
				}
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
	 * 根据第二次哈希的回复时隙，找到想要的tag set
	 * @param slotIdx
	 * @param stagset
	 * @return
	 */
	private HashSet<Tag> findTagSet2(int slotIdx2, HashSet<Tag> stagset) {
		// TODO Auto-generated method stub
		HashSet<Tag> temp = new HashSet<Tag>();
		temp.clear();
		for(Tag t: stagset){
			if(t.slotidx2==slotIdx2){
				temp.add(t);
			}
		}
		
		return temp;
	}
	
	
	/**
	 *  Tag verifying
	 *  stagset is the  server tag set can be verfied
	 *  ltagset is the  local tag set 
	 *  return 
	 */
	public double IIP_remove(HashSet<Tag> stagset, HashSet<Tag> ltagset,  HashSet<Tag> stagsetReconciled){
	
		
		int n= stagsetReconciled.size(); 
		
		/*  得到missing 集合   */
		missingset.addAll(stagsetReconciled);   
		missingset.removeAll(ltagset); //求差集，就是missing 标签
		    
		stagset.removeAll(stagsetReconciled); //remove the verified tags in server
		ltagset.removeAll(stagsetReconciled); //remove the verified tags in local
		
		return 0.0;  //
		
	}
	
	
	// 标签集 能否被seed 2 协调
	boolean isReconcileCollision(HashSet<Tag> tagset, int seed2){
		int f = tagset.size();
		int[] replyTemp = getReply(f,seed2,tagset);
		boolean flagRC = true;
		for (int i = 0; i < replyTemp.length; i++) {
			if(replyTemp[i]!=1){
				flagRC = false;
				break;
			}
		}
		
		return flagRC; //successful is true
		
	}
	


}
