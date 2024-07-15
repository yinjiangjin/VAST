package RFID;
import java.util.*;
public class TagGenerator {
	private static Hashtable<Integer,Tag> tagPool = new Hashtable<Integer,Tag>();
	private static Random rand = new Random();
	private static int curidx = -1;
	/**
	 * Generate n tags that are scattered in a xrange X yrange area
	 * @param ntag
	 * @param xrange
	 * @param yrange
	 * @return
	 */
	public static HashSet<Tag> getnewTags(int n, int xrange, int yrange){
		int tagcount = 0;
		HashSet<Tag> vret = new HashSet<Tag>();
		while(tagcount < n){
			Integer key = new Integer(rand.nextInt(Integer.MAX_VALUE));
			if(tagPool.containsKey(key))
				continue;
			else{
				double x = rand.nextDouble()*xrange;
				double y = rand.nextDouble()*yrange;
				Tag t = new Tag(key,x,y);
				vret.add(t);
				tagPool.put(key, t);
				tagcount++;
			}
		}
		return vret;
	}
	/**
	 * 函数重载
	 */
	
	public static HashSet<Tag> getnewTags(int n, int xrange, int yrange, int fingerLength){
		int tagcount = 0;
		HashSet<Tag> vret = new HashSet<Tag>();
		while(tagcount < n){
			Integer key = new Integer(rand.nextInt(Integer.MAX_VALUE));
			if(tagPool.containsKey(key))
				continue;
			else{
				double x = rand.nextDouble()*xrange;
				double y = rand.nextDouble()*yrange;
				//Tag t = new Tag(key,x,y);
				Tag t = new Tag(key,x,y,fingerLength); //调用带指纹长度的构造函数
				vret.add(t);
				tagPool.put(key, t);
				tagcount++;
			}
		}
		return vret;
	}
	
	/**
	 * 
	 */
	public static HashSet<Tag> getnewTags(int n, int xrange, int yrange, int fingerLength, int lambda){
		int tagcount = 0;
		HashSet<Tag> vret = new HashSet<Tag>();
		while(tagcount < n){
			Integer key = new Integer(rand.nextInt(Integer.MAX_VALUE));
			if(tagPool.containsKey(key))
				continue;
			else{
				double x = rand.nextDouble()*xrange;
				double y = rand.nextDouble()*yrange;
				//Tag t = new Tag(key,x,y);
				Tag t = new Tag(key,x,y,fingerLength); //调用带指纹长度的构造函数
				
				vret.add(t);
				tagPool.put(key, t);
				tagcount++;
			}
		}
		return vret;
	}
	
	public static void gettotalTags(int n, int xrange, int yrange){
		int tagcount = 0;
		int add = n - tagPool.size();
//		System.out.println("add is " + add);
		while(tagcount < add){
			Integer key = new Integer(rand.nextInt(Integer.MAX_VALUE));
			if(tagPool.containsKey(key))
				continue;
			else{
				double x = rand.nextDouble()*xrange;
				double y = rand.nextDouble()*yrange;
				Tag t = new Tag(key,x,y);
				tagPool.put(key, t);
				tagcount++;
			}
		}
	}
	/**
	 * 生成有指纹的标签
	 * @param n
	 * @param xrange
	 * @param yrange
	 * @param fingerLength
	 */
	public static void gettotalTags(int n, int xrange, int yrange, int fingerLength){
		int tagcount = 0;
		int add = n - tagPool.size();
//		System.out.println("add is " + add);
		while(tagcount < add){
			Integer key = new Integer(rand.nextInt(Integer.MAX_VALUE));
			if(tagPool.containsKey(key))
				continue;
			else{
				double x = rand.nextDouble()*xrange;
				double y = rand.nextDouble()*yrange;
				//Tag t = new Tag(key,x,y);
	
				Tag t = new Tag(key,x,y,fingerLength); //调用带指纹长度的构造函数
				tagPool.put(key, t);
				tagcount++;
			}
		}
	}
	
	public static void gettotalTags(int n, int xrange, int yrange, int fingerLength, int lambda, int[] groupNumArr, int categoryBitLength, boolean CIDrandom){
		
		//生成CID数组
		int[] CIDArr;
		if(CIDrandom){//如果随机生成category ID
			CIDArr = categoryIDGenerator(categoryBitLength, groupNumArr.length);
		}else{
			System.out.println("*********** 序列生成category ID******************");
			//序列生成
			CIDArr = 	new int[groupNumArr.length]; 
			for (int i = 0; i < CIDArr.length; i++) {
				CIDArr[i] = i; //初始化全为-1
			}
		}
		
		
		System.out.println("n:"+n);
		for (int i = 0; i < groupNumArr.length; i++) {
			System.out.println(""+i+"-----"+ groupNumArr[i]);
		}
		int tagcount = 0;
		int i=0;
		int add = n - tagPool.size();
//		System.out.println("add is " + add);
		while(tagcount < add){
			Integer key = new Integer(rand.nextInt(Integer.MAX_VALUE));
			if(tagPool.containsKey(key))
				continue;
			else{
				//如何确定组标签数目。
				if(groupNumArr[i]<=0){//
					i++;
				}
				double x = rand.nextDouble()*xrange;
				double y = rand.nextDouble()*yrange;
				//Tag t = new Tag(key,x,y);
	
				Tag t = new Tag(key,x,y,fingerLength, lambda, CIDArr[i], categoryBitLength); //调用带指纹长度的构造函数
				tagPool.put(key, t);
				tagcount++;
				
				groupNumArr[i]--; //这个组的标签数目减少1
				
				if(groupNumArr[lambda-1]==0)
					break;
			}
		}
	}
	
	/**
	 * 随机生成category ID.
	 * @param categoryBitLength
	 * @param categoryNum
	 * @return
	 */
	static int[] categoryIDGenerator(int categoryBitLength, int categoryNum){
		
		int[] categoryArr = new int[categoryNum]; 
		for (int i = 0; i < categoryArr.length; i++) {
			categoryArr[i] = -1; //初始化全为-1
		}
	
		int index = categoryBitLength;
		if(index>30){
			index = 30; //怕超出整形能够表示的范围。
		}
		int upperbound = (int) Math.pow(2, index);
		
		int i = 0;
		double seed1 = 0;
		int CID_i =-1;
		boolean flag = true;
		while(i < categoryNum){
			seed1 = rand.nextDouble();
		    CID_i = (int) Math.floor(seed1*upperbound);
			for (int j = 0; j < i; j++) {
				if( categoryArr[j] == categoryArr[i] ){
					flag = false;
					break;
				}
			}
			
			if(flag){ //生成的CID成功后再生成下一个
				categoryArr[i] = CID_i;
				i++;
			}
		}
		return categoryArr;
		
	}
	
	
	public static void gettotalTags(int n, int xrange, int yrange, int fingerLength, int lambda, int[] groupNumArr){
		
		System.out.println("n:"+n);
		for (int i = 0; i < groupNumArr.length; i++) {
			System.out.println(""+i+"-----"+ groupNumArr[i]);
		}
		int tagcount = 0;
		int i=0;
		int add = n - tagPool.size();
//		System.out.println("add is " + add);
		while(tagcount < add){
			Integer key = new Integer(rand.nextInt(Integer.MAX_VALUE));
			if(tagPool.containsKey(key))
				continue;
			else{
				//如何确定组标签数目。
				if(groupNumArr[i]<=0){//应该可以
					i++;
				}
				double x = rand.nextDouble()*xrange;
				double y = rand.nextDouble()*yrange;
				//Tag t = new Tag(key,x,y);
	
				Tag t = new Tag(key,x,y,fingerLength, lambda, i); //调用带指纹长度的构造函数
				tagPool.put(key, t);
				tagcount++;
				
				groupNumArr[i]--; //这个组的标签数目减少1
				
				if(groupNumArr[lambda-1]==0)
					break;
			}
		}
	}
	
	public static void gettotalTags(int n, int xrange, int yrange, int fingerLength, int lambda){
		int tagcount = 0;
		int add = n - tagPool.size();
//		System.out.println("add is " + add);
		while(tagcount < add){
			Integer key = new Integer(rand.nextInt(Integer.MAX_VALUE));
			if(tagPool.containsKey(key))
				continue;
			else{
				double x = rand.nextDouble()*xrange;
				double y = rand.nextDouble()*yrange;
				//Tag t = new Tag(key,x,y);
	
				Tag t = new Tag(key,x,y,fingerLength, lambda); //调用带指纹长度的构造函数
				tagPool.put(key, t);
				tagcount++;
			}
		}
	}
	
	/*
	 * 动态生成range
	 */
	static void generateDynamicRange(HashSet<Tag> hset, int lambda){
		
		for(Tag t:hset){
			t.getRangeIndicator(lambda);  // 
		}
	}
	
	
	
	public static void flush(){
		curidx = -1;
//		tagPool.clear();
	}
	public static void realflush(){
		curidx = -1;
		tagPool.clear();
	}
	public static void resetidx(){
		curidx = -1;
	}
	/**
	 * 从tagPool中拿n个tag
	 */
	public static HashSet<Tag> getTags(int n){
		int idx = 0;
		int added = 0;
		Enumeration<Tag> ite = tagPool.elements();
		HashSet<Tag> hsret = new HashSet<Tag>();
		while(ite.hasMoreElements()){
			if(idx >= curidx){
				hsret.add(ite.nextElement());
				added++;
				idx++;
				if(added == n ){
					break;
				}
			}else{
				ite.nextElement();
				idx++;
			}
		}
//		System.out.println("added is " + added + ", n is " + n);
	//	curidx = idx; //我把这句话注释了。因为每个算法需要拿标签，所以需要注释掉这句话。(STEP不能注释这句话)
		return hsret;
	}
	
	/**
	 * 从tagPool中拿n个tag， 且属于CID类别
	 */
	public static HashSet<Tag> getTags(int n, int CID){
		int idx = 0;
		int added = 0;
		Enumeration<Tag> ite = tagPool.elements();
		HashSet<Tag> hsret = new HashSet<Tag>();
		System.out.println("CID:"+CID);
		while(ite.hasMoreElements()){
			if( (idx >= curidx) ){
				Tag t = ite.nextElement();
				if(t.categoryID==CID){
					hsret.add(t);
					added++;
					idx++;
					if(added == n ){
						break;
					}
				}
				
			}else{
				ite.nextElement();
				idx++;
			}
		}
//		System.out.println("added is " + added + ", n is " + n);
	//	curidx = idx; //我把这句话注释了。因为每个算法需要拿标签，所以需要注释掉这句话。(STEP不能注释这句话)
		return hsret;
	}
	
	
}
