/**
 * Define the tag class
 */
package RFID;

import java.util.Random;

public class Tag{
	int id;
	int groupid;
	int[] groupIteResult;
	int groupIteCount; //iteration group 计数器，初始化为组数，后续为1.
	
	int tagfingerprint;//PIC fingerprint
	double x,y;//position of this tag
	int[] slots;//for the bloom filter based searching protocol
	int slotidx;//for the collection approach, Protocol 1,  and Protocol 2
	
	int slotidx2;//for the IIP protocol
	
	int categoryslotidx; //根据类别ID进行hash，保证同类别有相同时隙。
	
	int[]  categoryBitArr;
	
	long slotidxLong; //for LoF.
	int[] rangeIndicator; 
	int[] rangeResult;  //初始化全为1.
	
	int[] rangeIndicator_2;
	int[] rangeResult_2;
	
	int[] randomRangeIndicator;
	
	int[] arrCategory; //组类别ID,所以仅一个1。SEM+AP需要。
	int categoryID; //组编号
	
	int[] shiftArr = new int[4];
	
	//indicates whether this tag has been checked in the collection approach, Protocol 1, Protocol 2, and the filter based approch, respectively
	boolean donecollect = false, donep1 = false, donep2 = false, donefilter = false;
	public Tag(int pid, double px, double py){
		id = pid;
		x = px;
		y = py;
	}
	public Tag(Integer pid, double px, double py){
		id = pid;
		x = px;
		y = py;
	}
	
	public Tag(Integer pid, double px, double py, int fingerLength){
		id = pid;
		x = px;
		y = py;
		tagfingerprint = produceFingerprint( fingerLength);
	}
	
	public Tag(Integer pid, double px, double py, int fingerLength, int lambda){
		id = pid;
		x = px;
		y = py;
		tagfingerprint = produceFingerprint( fingerLength);
		getRangeIndicator(lambda);
		getRangeResult(lambda);
	}
	
	
	public Tag(Integer pid, double px, double py, int fingerLength, int lambda, int index, int categoryBitLength){ //lambda应该是组数
		id = pid;
		x = px;
		y = py;
		categoryID = index; //所属类别编号。 可以基于这个做hash。
		//setRangeIndicator(lambda, index); //设置类别对应的Single-one string (SEM+AP).
		
		tagfingerprint = produceFingerprint( fingerLength);
		
		String categoryIDStr = Integer.toBinaryString(categoryID);
		categoryBitArr = new int[categoryBitLength];
		
		int start =categoryBitArr.length - categoryIDStr.length();
		int start2 = 0;
		for (int j = start; j < categoryBitArr.length; j++) {
			categoryBitArr[j] = Integer.parseInt( categoryIDStr.substring(start2, start2+1) ); 
			start2 ++;
		}
		
	}
	
	
	public Tag(Integer pid, double px, double py, int fingerLength, int lambda, int index){ //lambda应该是组数
		id = pid;
		x = px;
		y = py;
	//	tagfingerprint = produceFingerprint( fingerLength); #指纹
		
	//	getRangeIndicator(lambda, i);
	//	getRangeResult(lambda);
	//	getRandomRangeIndicator(lambda); //生成一个随机位置的1，表示组。
		//计算需要左或右位移的长度
	//	getRangeIndicator_RandomRangeIndicator_shiftArr(lambda, i);
		
	//	groupid = i; //就是所属组id。
		
		

		categoryID = index; //所属类别编号。
		setRangeIndicator(lambda, index); //设置类别对应的Single-one string (SEM+AP).
		
	}
	
	public void setRangeIndicator(int lambda, int index){ //设置类别编号。
		arrCategory = new int[lambda];
		arrCategory[index] = 1;
		
	}
	

	public int getId() {
		return id;
	}

	/**
	 * Get k slot index ranging from 0...f-1
	 * @param f
	 * @param k
	 */
	public void getSlots(int f, int k, int[] seeds){
		slots = new int[k];
		String str = null;
		try{
			for(int i = 0; i < k; i++){
				str=id+"."+seeds[i];
				long hvalue = Utils.md5Hash(str);
				int sc = (int)(hvalue%f);
				slots[i] = sc;
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void getCategorySlot(int f, int seed){
		try{
			long hvalue = Utils.md5Hash(categoryID+"."+seed);
			categoryslotidx = (int)(hvalue%f);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * Log slot
	 * 基于标签后导0的位置。
	 */
	public void getLogSlot(int f, int seed){
		try{
			long hvalue = Utils.md5Hash_36(id+"."+seed);
			
			
			String zz = Long.toBinaryString(hvalue);
			
			int slot = 0;
			for (int i = zz.length(); i >=0 ; i--) {
				if(Integer.parseInt(zz.substring(i-1, i))==0){
					slot+=1;
				}else{
					break;
				}
			}
			slotidx = (int)(slot%f);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Traditonal aloha
	 */
	public void getSlot(int f, int seed){
		try{
			long hvalue = Utils.md5Hash(id+"."+seed);
			slotidx = (int)(hvalue%f);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * tag may be select slots more times (twice)
	 * @param f
	 * @param seed
	 */
	public void getSlot2(int f, int seed){
		try{
			long hvalue = Utils.md5Hash(id+"."+seed);
			slotidx2 = (int)(hvalue%f);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Traditonal aloha: Long type
	 */
	public void getSlotLong(long f, int seed){
		try{
			long hvalue = Utils.md5Hash(id+"."+seed);
			slotidxLong = (long)(hvalue%f);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public boolean equals(Object other){//重写equal和hashCode来定义标签相等。
		if(other == null) return false;
		Tag t = (Tag)other;
		if(t.id == id)
			return true;
		return false;
	}
	public int hashCode(){  //判断相等时，判断id相等就可以了，太nice了。
		return id;
	
	}
	
	/**
	 * range indicator: only one-bit is 1.
	 * @param infLength
	 */
	public void getRangeIndicator(int lambda){ //这个地方应该要反复做高斯的。
		Random randGauss = new Random();
		int foot = (int) ( 8*randGauss.nextGaussian() + (lambda/2) ); //mean: lambda/2 and standard: 5
		//int foot = randGauss.nextInt(lambda);
		if(foot>lambda-1)
			foot = lambda-1;
		if(foot<0)
			foot = 0;
		rangeIndicator = new int[lambda];
		rangeIndicator[foot] = 1;
		
		rangeIndicator_2 = new int[lambda];
		rangeIndicator_2[foot] = 1;
		
	}
	
	private void getRandomRangeIndicator(int lambda) {
		// TODO Auto-generated method stub
		Random rand  = new Random();
		int index = rand.nextInt(lambda);
		randomRangeIndicator = new int[lambda];
		randomRangeIndicator[index] = 1;
		
	}
	
	public void getRangeIndicator_RandomRangeIndicator_shiftArr(int lambda, int index){ //这个地方应该要反复做高斯的。
		//生成真实的组
		int foot = index;
		rangeIndicator = new int[lambda];
		rangeIndicator[foot] = 1;
		
		rangeIndicator_2 = new int[lambda];
		rangeIndicator_2[foot] = 1;
		
		//生成随机的组
		Random rand  = new Random();
		int ind = rand.nextInt(lambda);
		randomRangeIndicator = new int[lambda];
		randomRangeIndicator[ind] = 1;
		
		//计算随机的组移动到真实的组的位移（左移或者右移）
		if(ind<=index){
			
			shiftArr[0] = lambda - (index-ind); //左移动
			shiftArr[1] = index-ind; //右移动
			
			
			
		}else{
			shiftArr[0] = ind-index; //左移动
			shiftArr[1] = lambda - (ind-index); //右移动
		}
		shiftArr[2] = (int)(Math.ceil(Math.log10(shiftArr[0]+1)/Math.log10(2))); //左移动的bit数目
		shiftArr[3] = (int)(Math.ceil(Math.log10(shiftArr[1]+1)/Math.log10(2))); //右移动的比特数目
		if(shiftArr[2]==0)
			shiftArr[2]=1;
		if(shiftArr[3]==0) //至少移动1比特。
			shiftArr[3]=1;
	}
	
	public void getRangeIndicator(int lambda, int index){ //这个地方应该要反复做高斯的。
		int foot = index;
		rangeIndicator = new int[lambda];
		rangeIndicator[foot] = 1;
		
		rangeIndicator_2 = new int[lambda];
		rangeIndicator_2[foot] = 1;
		
	}
	
	
	public void getRangeResult(int lambda){//这个叫做初始化全1呀。
		
		rangeResult = new int[lambda];
		rangeResult_2 = new int[lambda];
		groupIteResult = new int[lambda];
		groupIteCount = lambda; //初始化为组数值。
		for (int i = 0; i < lambda; i++) {
			rangeResult[i] = 1;
			rangeResult_2[i] = 1;
			groupIteResult[i] = 1; // iteration group
		}
		

		
	}
	
	/**
	 * jiangjin
	 * @param slotidx
	 */
	public void setSlotidx(int slotidx) {
		this.slotidx = slotidx;
	}
	/**
	 * jiangjin
	 * @param length 长度为length的指纹--
	 * @return
	 */
	public int produceFingerprint( int length) {
	
		int maxvalue =(int) Math.pow(2, length);
		if(maxvalue<1){
			maxvalue=1;
		}
		return this.id%maxvalue;  //与ID相关的指纹

	}
}
