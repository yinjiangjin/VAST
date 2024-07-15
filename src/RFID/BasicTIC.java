
package RFID;
import java.util.*;


public class BasicTIC extends ICNPBasic{
	//the following values are for EPC Global C1G2 tags
	String fname = "HTIC";  //调试用的文件
	String collectname = "HTIC-data-1";
	int eliT = 0,eliNT=0,eliL =0; //该轮可以消除的标签
	//boolean firstcolflag = true; //标记第一个标签收集的时间。
	HashSet<Tag> zz = null;  //存放本地目标标签集合
	int round;  //成员变量自动初始化为0
	int roundReader;
	public BasicTIC(){
		ptname = new String("HTIC");
	}
	
	double execute(){
		roundReader++;
		TimeFirstPhase = 0; //初始化
		TimeSecondPhase = 0;
		
		tcollection = 0;
		//System.out.println("start TIC");
		//此处不能这样
		
		HashSet<Tag> local = new HashSet<Tag>();
		local.addAll(targetset);
		local.addAll(nontargetset);
		localtagsset.retainAll(local); //由于一个标签可以被多个阅读器覆盖，有些标签可能已经收集信息了。保留交集的部分
		if(localtagsset.size()==0){
			return 0;
		}
		//System.out.println("TIC "+"targetset.size():"+targetset.size()+" "+"localtagsset.size():"+localtagsset.size());
		//zz阅读器下有多少目标标签
		zz =new HashSet<Tag>();
		zz.addAll(localtagsset);
		zz.retainAll(targetset);
		System.out.println("阅读器下有多少目标标签-zz.size():"+zz.size());
				
		boolean flagcoll = false;
		while(targetset.size()>0){
			round++;
			int[][]tworet =  eliminatePhase(targetset.size(),nontargetset.size(),localtagsset.size());
			if(isCollect(tworet)){
				singleSeedCollection(targetset,localtagsset,tworet);
			}
		}
		
		//Utils.logAppend("collecttotalnumber-HTIC.txt", "HTIC "+collecttotalnumber+"\n");
		return tcollection;
	}
	
	/**
	 * 排除阶段：收取1bit回复来排除标签
	 * @return
	 */
	int[][] eliminatePhase(int W, int N, int L){
		
		ttime_filtering +=TimingScheme.t_lambda; //统计过滤阶段的时间
		
		tcollection+=TimingScheme.t_lambda; //发送query的时间
		
		//Utils.logAppend(collectname,  targetset.size()+"\t "+ nontargetset.size()+"\t ");
		double timeFirstPhase = 0;
		
		int [][] tworet =new int[3][];
		
		double[] ret = new double[]{0,0,0,0};

		//int f = localtagsset.size();
		int f = (int)getOptimalFrame_eta(W, N ,L)[0];   //帧长
		//System.out.println("最优帧长为："+f);
		
		if(f<10){
			f=10;
		}
		
		//String data1 = targetset.size()+"\t "+localtagsset.size()+"\t "+nontargetset.size()+"\t "+f+"\r";
		//Utils.logAppend(fname, data1);
			
		int seed = rand.nextInt();
		
		//回复向量
		int[] replyW = getReply(f,seed,targetset);//expected reply vector when W is mapped
		int[] replyN = getReply(f,seed,nontargetset);//expected reply vector when N is mapped
		int[] replyL = getReply(f,seed,localtagsset);//expected reply vector when L is mapped;local tags
		
		//time in collecting replies from local tags
		for(int i = 0; i < replyL.length; i++){
			if(replyL[i] == 0){
				ret[0] += TimingScheme.t_e;
			}else{
				ret[0] += TimingScheme.t_s;
			}
		}
		//返回该轮消除的数目
		ret[1] = filter(targetset, replyL); //排除不在阅读器下的target。收到本地回复为0.
		ret[2] = filternontarget(nontargetset, replyW, replyL);
		ret[3] = filter(localtagsset, replyW);   //replyW为0表示是排除时隙，如果本地标签映射排除时隙，可以排除
		
		eliT = (int) ret[1];
		eliNT = (int) ret[2];
		eliL = (int) ret[3];
		
		
		timeFirstPhase = ret[0];
		tcollection += timeFirstPhase;
		
		TimeFirstPhase += timeFirstPhase;//直接赋值
		
		ttime_filtering += timeFirstPhase; //统计过滤阶段的时间
		
		String data = " Firstphase" + " \t " + round + " \t " + timeFirstPhase+" \t "+eliT+" \t "+eliNT+" \t "+eliL+" \r\n ";
		if(roundReader==1){
			Utils.logAppend(collectname, data);
		}
		
		
		tworet[0] = replyW;
		tworet[1] = replyN;
		tworet[2] = replyL;
		

		return tworet;
		
	}

	
	public double[] getOptimalFrame_eta(int W, int N, int L) {
		// TODO Auto-generated method stub
		double e = Math.E;
	
		double[] eta_arr = new double[N+W];
		for (int f = 0; f < eta_arr.length; f++) {
			
			//分子
			double Xi = N*Math.pow(e, -1.0*W/f) +  N*Math.pow(e, -1.0*L/f)*(1-Math.pow(e, -1.0*W/f));
			double Yi =  W*Math.pow(e, -1.0*L/f);
			double fenzi_Neli = Xi+ Yi;
			
			//分母
			double P0 = Math.pow(e, -1.0*L/f);
			double fenmu_Ti = f*(P0*TimingScheme.t_e + (1-P0)*TimingScheme.t_s);
			
			eta_arr[f] = fenzi_Neli/fenmu_Ti; //计算出所有的eta值。
			
		}
		double max_eta = 0;
		int opt_f = 0;
		for (int i = 2; i < eta_arr.length; i++) {
			if(eta_arr[i]>max_eta){
				max_eta = eta_arr[i];
				opt_f =i;
			}
		}
		
		double []optf_maxeta = new double[2];
		optf_maxeta[0] = opt_f;
		optf_maxeta[1] = max_eta;
		return optf_maxeta;
	}

	boolean canReconciledsingSeed(HashSet<Tag> hset, int seed){
		
		int num = hset.size(); 
		int[] reply = getReply(num,seed,hset); //此时不用回复的。
		if((reply[0]==1)&&(reply[1]==1)){  //不管是二冲突还是三冲突，都可以用该条件
			return true;
		}
		return false;
	}


	
void singleSeedCollection(HashSet<Tag> tset,HashSet<Tag> lset,int[][] tworet ){
		
		double time2thPhase = tcollection; //用时间差来计算该阶段的时间
	
		HashSet<Tag> qualifiedtarget = new HashSet<Tag>();  //同构target标签
		qualifiedtarget.clear();
		
		int[] replypW = tworet[0];
		int[] replypN = tworet[1];
		int[] replypL = tworet[2];
		
		//所以说需要写到ICNPBasic中。
		for(Tag t:tset){
			int j = t.slotidx;
			if((replypL[j] >0)&&(replypN[j] == 0)&&(replypW[j] <= 3)){
				qualifiedtarget.add(t);
			}
		}
		String sendVector = ""; //发送的2bitvector
		int countslot = 0; //记录所需要的时隙数目
		int ReconciledNum = 0;
		
		HashSet<Tag> temp = new HashSet<Tag>();
		HashSet<Tag> tempT = new HashSet<Tag>();


		int seed2 = rand.nextInt();  //做参数传递进来应该好点
		//这样判断太复杂，而且时间复杂度高。但是好像是要按照时隙来. 
		for(int i=0;i<replypL.length;i++){
			if((replypL[i] >0)&&(replypN[i] == 0)&&(replypW[i] <= 3)){//预测小于3的同构时隙，且收到回复
				if(replypW[i]==1){ //期望回复为1
					for(Tag t1:qualifiedtarget){
						if(t1.slotidx==i){
							t1.slotidx = countslot; //设置回复的时隙
							temp.add(t1);
							countslot +=1;
						}
					}
					sendVector += "01";

					
				}else if(replypW[i]==2){ //预测为2的同构时隙且收到回复
					for(Tag t2:qualifiedtarget){
						if(t2.slotidx==i){
							tempT.add(t2);
						}
					}
					if(!canReconciledsingSeed(tempT,seed2)){//如果不能协调
						sendVector += "00";
					}else{
						for(Tag actualslot: tempT){
							actualslot.slotidx = actualslot.slotidx + countslot;
						}
						countslot += 2;
						ReconciledNum += 1; 
						sendVector += "10";
						temp.addAll(tempT);
						
					}
					
					tempT.clear();

				}else{
					for(Tag t3:qualifiedtarget){
						if(t3.slotidx==i){
							tempT.add(t3);
						}
					}	
					if(!canReconciledsingSeed(tempT,seed2)){
						sendVector += "00";
					}else{
						for(Tag actualslot: tempT){
							actualslot.slotidx = actualslot.slotidx + countslot;
						}
						countslot += 3;
						ReconciledNum += 1;
						sendVector += "11";
						temp.addAll(tempT);

					}
					tempT.clear();

				}
			}else{
				sendVector += "00"; //不满足条件发送00
			}
		}

		int f = replypL.length;
		tcollection += Math.ceil(2*f/96.0)*TimingScheme.t_id; //发送allocated vector的时间
		
		for(int i=0;i<temp.size();i++){  //需要这么多slots
			for(Tag t: temp){
				if(t.slotidx==i){
					
					if(lset.contains(t)){
						
						targetcollected.add(t); //加入到已经收集的标签集合中
						tcollection += (TimingScheme.interval+TimingScheme.onebit*infLength);
						collecttotalnumber +=1;
						sumtcollection += tcollection + eachcolortime; //收集的时间
						
					}else{
						tcollection += TimingScheme.t_e;
					}
					
					break; //找到后即可退出，不用遍历后续
				}
				
			}
		}
		
		time2thPhase = tcollection - time2thPhase ;
		String data = " Secondphase" + " \t " + round + " \t " + time2thPhase+" \t "+temp.size()+" \r\n ";
		if(roundReader==1){
			Utils.logAppend(collectname, data);
		}
		
		TimeSecondPhase += time2thPhase; //直接赋值
		
		tset.removeAll(temp); //target set中删除已经收集的标签
		lset.removeAll(temp);
		
	}

	
boolean isCollect(int[][] tworet){ 
	

	double flag = 100;
	int[] replyW = tworet[0];
	int[] replyN = tworet[1];
	int[] replyL = tworet[2];
	int Lastf = replyW.length;
	int ss1=0,ss2=0,ss3=0;
	for (int i = 0; i < replyL.length; i++) {
		if((replyL[i] >0)&&(replyW[i]==1)&&(replyN[i]==0)){
			ss1 +=1;
		}
		if((replyL[i] >0)&&(replyW[i]==2)&&(replyN[i]==0)){
			ss2 +=1;
		}
		if((replyL[i] >0)&&(replyW[i]==3)&&(replyN[i]==0)){
			ss3 +=1;
		}
	}
	
	int framesize = replyW.length;
	
	
	//1. 若进行信息采集
	double Y_ic = ss1 + ss2*2/2 + 3*ss3*(6.0/27);
	
	double T_ic = 2*framesize*TimingScheme.t_id/96;
	
	double eta_ic = Y_ic/T_ic;
	
	//2. 若不采集信息，直接进行下一阶段
	
	//由于已经执行上一轮的排除阶段，剩下的 W_i_1, N_i_1, 和 L_i_1 可以 直接获得，不需要像论文一样去计算了。
	int W_i_1 = targetset.size();
	int N_i_1 =  nontargetset.size();
	int L_i_1 = localtagsset.size(); //假设你能够准确估计。
	
	double eta_i_1 =  getOptimalFrame_eta(W_i_1, N_i_1, L_i_1)[1];
	

	if(eta_ic < eta_i_1){ 
		return false; //不收集信息
	}else{
		return true;
	}
	
}

}
