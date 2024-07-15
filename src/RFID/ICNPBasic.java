/**
 * Define some useful data structures and functions common to Protocol 1 and Protocol 2
 */
package RFID;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;



//import icnptool.icnptool;
//import com.mathworks.toolbox.javabuilder.*;

public class ICNPBasic {
		
		static boolean flag_key_missing = true;
		
		int totalMissingNumber_identified; //被收集的总CIDs数目
	
		HashSet<Tag> serverset = new HashSet<Tag>(); //multi-category, 服务器的标签。
	
		HashSet<Tag> targetset = new HashSet<Tag>(); //KEY missing 
		HashSet<Tag> nontargetset = new HashSet<Tag>(); // KEY missing
		HashSet<Tag> localtagsset = new HashSet<Tag>(); // KEY missing
		
		int TOTAL_bits;   // KEY missing 总的传输比特。
		
		HashSet<Tag> targetsetbackup = new HashSet<Tag>();
		HashSet<Tag> nontargetsetbackup = new HashSet<Tag>();
		HashSet<Tag> tempremove = new HashSet<Tag>();
		HashSet<Tag> targetcollected = new HashSet<Tag>(); //缓存已经收集信息的target标签
		HashSet<Tag> result = new HashSet<Tag>();
		
		HashSet<Tag> RQ_PM_Result = new HashSet<Tag>();
		HashSet<Tag> nontargetcollected = new HashSet<Tag>(); //缓存已经排除的notarget标签
		
		//FEAT 或者 E-FEAT
		double ttime_pre_selection; //E-FEAT 第一阶段
		double ttime_tag_verify; //FEAT 或者 E-FEAT 第二阶段
		double ttime_hopping_selection; //FEAT 或者 E-FEAT 第三阶段
		
		double tbits_pre_selection; //E-FEAT 第一阶段  比特数
		double tbits_tag_verify; //FEAT 或者 E-FEAT 第二阶段    比特数
		double tbits_hopping_selection; //FEAT 或者 E-FEAT 第三阶段 比特数
		
		double total_rounds; //执行总
		
		
		double ttime_filtering; // the total time for filtering ordinary tags in missing key tag identification
		
		//double ttimeVectorN; // the total time for broadcasting the notification vector V_n in PMI
		
		double tcollection; // collection time in one round
		double sumtcollection; //收集所有target标签的时间和，除以总的target标签就是平均收集每个target标签的时间。
		double eachcolortime; //每轮调度所用时间
		double totalcollection;//the total time in the information process
		static int infLength=1; // 对于missing识别，默认信息长度为1
		int collecttotalnumber; //被收集的总CIDs数目
		
		
		
		double TimeMMTI;
		double totalTimeMMTI;
		
		double TimeFirstPhase;
		double TimeSecondPhase;
		
		double TotalTimeFirstPhase;
		double TotalTimeSecondPhase;
		//double TotalTimeFirstPhaseTIC;
		//double TotalTimeSecondPhaseTIC;
		
		//生成随机种子，需要的
		public static Random rand = new Random();//Random number generator
		public static Random randGauss = new Random();//Random number generator

		
		static int categoryBitNum =32; //PMI类别所占用bit,默认32。
		//static int groupNum_Basic;
		static int categoryNum_Basic;//PMI,类别数
		static int[] ActualCategoryNumArr;
		
		static double  alpha = 0.05;//the false positive rate
		HashSet<Tag> finalresult = new HashSet<Tag>();
		String ptname = "";  //协议的名字
		
		/**
		 * Just initialize the matlab program;
		 */
		public ICNPBasic(){
		
		}
		public void reset(){
			
			targetset.clear();
			nontargetset.clear();
			localtagsset.clear();
			targetsetbackup.clear();
			nontargetsetbackup.clear();
			tempremove.clear();
			
			result.clear();
			
			finalresult.clear();
			
		}
		/*********************** Commonly used functions **************************/
		
		static void setActualCategoryNumArr(int categoryNum, int[] categoryNumArr){
			ActualCategoryNumArr = new int[categoryNum];
			for (int i = 0; i < categoryNumArr.length; i++) {
				ActualCategoryNumArr[i] = categoryNumArr[i];
			}
			
		}
		
		
		/**
		 * Set the target tag set W 
		 * Store a copy of T in searchbackup
		 */
		void settargetset( boolean flag){
			
			targetset.clear();
			if(flag){
				
				targetsetbackup.removeAll(targetcollected); //移除该调度轮已经收集的target标签。
				targetset.addAll(targetsetbackup);
				System.out.println("输出这一个调度轮收集的标签数目:"+targetcollected.size()); //输出这一个调度轮收集的标签数目
				targetcollected.clear();
				
			}else{
				targetset.addAll(targetsetbackup);
				
			}
			
		}
		
		
		void setNontargetset( boolean flag){
			
			nontargetset.clear();
			if(flag){
				
				nontargetsetbackup.removeAll(nontargetcollected); //移除该调度轮已经收集的target标签。
				nontargetset.addAll(nontargetsetbackup);
				nontargetcollected.clear();
				
			}else{
				
				nontargetset.addAll(nontargetsetbackup);
				
			}
			
		}

		void setServerset(HashSet<Tag> sset){
			
			serverset.clear();
			serverset.addAll(sset);
		}
		
		//函数重载 - 每个调度轮完成后不删除已经收集的目标标签 ( 应用于PIC )
		void settargetset(HashSet<Tag> sset){
			targetset.clear();
			targetset.addAll(sset);
		}
		
		void setnontargetset(HashSet<Tag> sset){
			nontargetset.clear();
			nontargetset.addAll(sset);
		}
		
		/**
		 * Set local tag set N
		 * Store a copy of N in localbackup
		 */
		void setlocalset(HashSet<Tag> lset){
			localtagsset.clear();
			localtagsset.addAll(lset);
		}
		
		int[] getLogReply(int f, int seed, HashSet<Tag> tset){
			int[] ret = new int[f];
			for(int i = 0; i < f; i++){
				ret[i] = 0;
			}
			for(Tag t:tset){
				t.getLogSlot(f, seed);
				ret[t.slotidx]++;
			}
			return ret;
		}
		
		
		/**
		 * compute the reply vector when frame size is f and random number is seed, where tset is the tag set
		 * @param f
		 * @param seed
		 * @param tset
		 * @return
		 */
		int[] getReply(int f, int seed, HashSet<Tag> tset){
			int[] ret = new int[f];
			for(int i = 0; i < f; i++){
				ret[i] = 0;
			}
			for(Tag t:tset){
				t.getSlot(f, seed);
				ret[t.slotidx]++;
			}
			return ret;
		}
		
		/**
		 * second slot selection for IIP protocol
		 * @param f
		 * @param seed
		 * @param tset
		 * @return
		 */
		int[] getReply2(int f, int seed, HashSet<Tag> tset){
			int[] ret = new int[f];
			for(int i = 0; i < f; i++){
				ret[i] = 0;
			}
			for(Tag t:tset){
				t.getSlot2(f, seed);
				ret[t.slotidx2]++;
			}
			return ret;
		}
		
		
		/**
		 * 根据类别ID做hash，类别相同，时隙相同。
		 * @param f
		 * @param seed
		 * @param tset
		 * @return
		 */
		int[] getCategoryReply(int f, int seed, HashSet<Tag> tset){
			int[] ret = new int[f];
			for(int i = 0; i < f; i++){
				ret[i] = 0;
			}
			for(Tag t:tset){
				t.getCategorySlot(f, seed);
				ret[t.categoryslotidx]++;
			}
			return ret;
		}
		
		
		/**
		 * 过滤服务器上的nontarget标签
		 * @param ntset  	nontarget标签集合
		 * @param replyW 	target标签的预映射
		 * @param replyN 	nontarget标签的预映射
		 * @param replyL 	本地标签回复向量
		 * @return 返回服务器排除的nontarget总标签数目
		 */
		int filternontarget(HashSet<Tag> ntset,int[] replyW,int[] replyL){
			
			tempremove.clear();
			//消除nontraget标签两种方式： 1、标签映射的本地回复为0证明不在。 2、本地回复为1，但是是排除时隙。
			for(Tag t2:ntset){
				if(replyL[t2.slotidx]==0){ 
					tempremove.add(t2);
				}
			}
			for(Tag t2:ntset){
				if(replyW[t2.slotidx]==0){ //没有目标标签映射
					tempremove.add(t2);  //可以这样，set集合元素不能重复
				}
			}
			
			//System.out.println("before remove nontarget:"+ntset.size());
			ntset.removeAll(tempremove);
			//System.out.println("after remove nontarget:"+ntset.size());
			return tempremove.size();
		}
		
		
		/**
		 * Remove those tags whose mapped slots are single in replyptn, 
		 * because the tag who hashed in the single slots can be collection. i.e., remove t if replyptn[t.collectidx] == 1 
		 * @param tset
		 * @param replyptn
		 * @return
		 */
		int filterSingle(HashSet<Tag> tset, int[] replyptn){
			//toremove is a global variable, to save space
			tempremove.clear();
			for(Tag t:tset){
				if(replyptn[t.slotidx]==1){
					tempremove.add(t);
				}
			}
			tset.removeAll(tempremove);
			return tempremove.size();
		}
		
		/**
		 * Remove those tags whose mapped slots are empty in replyptn, i.e., remove t if replyptn[t.collectidx] == 0 
		 * @param tset
		 * @param replyptn
		 * @return
		 */
		int filter(HashSet<Tag> tset, int[] replyptn){
			//tempremove is a global variable, to save space
			tempremove.clear();
			for(Tag t:tset){
				if(replyptn[t.slotidx]==0){
					tempremove.add(t);
				}
			}
			//System.out.println("没有remove："+tset.size());
			
			tset.removeAll(tempremove);
			//System.out.println("remove 后："+tset.size());
			
			return tempremove.size();
		}
		
		public void logdatasingle(String dirname, String fname, String data){
			try{
				File dir = new File(dirname);
				dir.mkdirs();
				File datafile = new File(dir.getAbsolutePath(),fname + ".txt");
				datafile.createNewFile();	
				Utils.debug(fname + "\t" + data);
				BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(datafile,true));
				os.write(data.getBytes(),0,data.length());
				os.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
		public void logdata(ICNPBasic icnpbasic, Params para){
			String prefix = para.getPrefix2(); //前缀
			String filename = icnpbasic.ptname;
			String data = prefix+" "+icnpbasic.totalcollection+"\t "+icnpbasic.TOTAL_bits+"\t "+icnpbasic.categoryNum_Basic +"\t Protocol_Missing: "
					+icnpbasic.totalMissingNumber_identified+"\t " +icnpbasic.ttime_filtering+"\t " 
					+icnpbasic.ttime_pre_selection+"\t "+icnpbasic.ttime_tag_verify+"\t "  
					+icnpbasic.ttime_hopping_selection+"\t "+icnpbasic.tbits_pre_selection+"\t "  
					+icnpbasic.tbits_tag_verify+"\t "+icnpbasic.tbits_hopping_selection+"\t "  
					+icnpbasic.total_rounds+"\t "  +"\n";
			
			
			try{
				//File dir = new File(filename);
				File dir = new File(para.paraname);
				dir.mkdirs();
				File datafile = new File(dir.getAbsolutePath(),ptname + ".txt");
				datafile.createNewFile();				  
				//data = prefix + "\t" +  data +"\n";
				Utils.debug(ptname + "\t" + data);
				BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(datafile,true));
				os.write(data.getBytes(),0,data.length());
				os.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
//			System.out.println(ptname + "\t" + totaltsearch + "\t" + totalbitssend + "\t" + totalbitsrecv + "\t" + finalresult.size());
		}
		
		
		/*ublic void logdata(Params para){
			String prefix = para.getPrefix();
			String data = totaltsearch + "\t" + totalbitssend + "\t" + totalbitsrecv + "\t" + finalresult.size();
			try{
				File dir = new File(para.paraname);
				dir.mkdirs();
				File datafile = new File(dir.getAbsolutePath(),ptname + ".txt");
				datafile.createNewFile();				  
				data = prefix + "\t" +  data +"\n";
				Utils.debug(ptname + "\t" + data);
				BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(datafile,true));
				os.write(data.getBytes(),0,data.length());
				os.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
//			System.out.println(ptname + "\t" + totaltsearch + "\t" + totalbitssend + "\t" + totalbitsrecv + "\t" + finalresult.size());
		}
		*/
		
		
		
		HashSet<Tag> findSlotTagSet(HashSet<Tag> hs, int i){
			HashSet<Tag> st = new HashSet<Tag>();
			for(Tag t: hs){
				if(t.slotidx == i){
					st.add(t);
				}
			}
			return st;
		}
		
}
