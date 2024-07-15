
package RFID;
import java.util.*;


public class ETIC4_8 extends ICNPBasic{
	//the following values are for EPC Global C1G2 tags
	String fname = "ICNPATIC";  //调试用的文件
	String collectname = "ICNPATIC-data";
	//boolean firstcolflag = true; //标记第一个标签收集的时间。
	int[] seeds = new int[8];
	int Lr = 4;
	public ETIC4_8(){
		ptname = new String("ICNPATIC4_8");
	}
	

	double execute(){
		
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
	
		//boolean flagcoll = false;
		while(targetset.size()>0){
			int[][]tworet =  eliminatePhase(); 
			MultiSeedCollection(tworet);

		}
		//Utils.logAppend("collecttotalnumber-ICNPATIC.txt", "ICNPATIC "+collecttotalnumber+"\n");
		return tcollection;
	}
	
	/**
	 * 排除阶段：收取1bit回复来排除标签
	 * @return
	 */
	int[][] eliminatePhase(){
		
		ttime_filtering +=TimingScheme.t_lambda; //统计过滤阶段的时间
		
		tcollection+=TimingScheme.t_lambda; //发送query的时间
		
		
		int [][] tworet =new int[3][];
		double[] ret = new double[]{0,0,0,0};
		//int f = localtagsset.size();
		int f = (int) getOptimalFrame_eta(targetset.size(),nontargetset.size(),localtagsset.size())[0];   //帧长需要用matlab来调试。
		if(f<20){
			f=20;
		}
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
		
		ret[1] = filter(targetset, replyL); //排除不在阅读器下的target。收到本地回复为0.
		ret[2] = filternontarget(nontargetset, replyW, replyL);
		ret[3] = filter(localtagsset, replyW);   //replyW为0表示是排除时隙，如果本地标签映射排除时隙，可以排除

		System.out.println("targetset:"+targetset.size()+" nontargetset: "+nontargetset.size()+"  localtagsset:"+localtagsset.size());
		
		tcollection += ret[0]; //收集时间先写到这里
		
		ttime_filtering += ret[0]; //统计过滤阶段的时间
		
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
	
	
	/**
	 * 方法重载
	 */
	HashSet<Tag> canReconciledMultiSeed(HashSet<Tag> thset,HashSet<Tag> nhset, int[] seeds, int Lr){
		
		HashSet<Tag> tp = new HashSet<Tag>();
		tp.clear();
		
		HashSet<Tag> MaxT = new HashSet<Tag>();
		MaxT.clear();
		
		HashSet<Tag> tset = new HashSet<Tag>(); //为了不修改原来的时隙. 收集不完全也可能是这个地方的问题。
		HashSet<Tag> nset = new HashSet<Tag>();
		tset.addAll(thset);
		nset.addAll(nhset);
		
		int num = Lr;  //总共设Lr个时隙
		for (int i = 0; i < seeds.length; i++) {
			
			int[] reply1 = getReply(num,seeds[i],tset); //target标签时隙
			int[] reply2 = getReply(num,seeds[i],nset); //target标签时隙
			for(Tag t: tset){
				if(reply2[t.slotidx]==0&&reply1[t.slotidx]==1){
					tp.add(t);
				}
			}
			if(tp.size()>MaxT.size()){
				MaxT.clear();
				MaxT.addAll(tp);
			}
			tp.clear();
		}
		return MaxT;

	}
		

	HashSet<Tag> canReconciledMultiSeed(HashSet<Tag> hset, int[] seeds, int Lr){
		
		HashSet<Tag> tp = new HashSet<Tag>();
		tp.clear();
		
		HashSet<Tag> MaxT = new HashSet<Tag>();
		MaxT.clear();
		int maxN = 0;
		
		int num = Lr; 
		HashSet<Tag> tset = new HashSet<Tag>();
		tset.addAll(hset);
		
		
		for (int i = 0; i < seeds.length; i++) {
			
			int[] reply = getReply(num,seeds[i],tset); //此时不用回复的。
			int flag =0;
			for(Tag t:tset){
				if(reply[t.slotidx]==1){
					flag +=1;
					tp.add(t);
				}
			}
			if(flag>maxN){
				MaxT.clear();
				maxN = flag;
				MaxT.addAll(tp);
			}
			tp.clear();
		
		}
		return MaxT;
	}
	
void MultiSeedCollection(int[][] tworet ){
		
	 for(int i = 0; i < seeds.length; i++){
			seeds[i] = rand.nextInt();
		}
	 
		int[] replyW = tworet[0];
		int[] replyN = tworet[1];
		int[] replyL = tworet[2];
		int countslot = 0; //记录所需时隙数目
		int ReconciledNum = 0;  //记录所需seed的数目。

		HashSet<Tag> temp = new HashSet<Tag>();
		temp.clear();
		HashSet<Tag> tempT = new HashSet<Tag>();
		HashSet<Tag> tempNT = new HashSet<Tag>();
		
		HashSet<Tag> tempTC = new HashSet<Tag>();
		
		for (int i = 0; i < replyL.length; i++) {
			if((replyL[i] >0)&&(replyW[i] >0)){//预测小于3的同构时隙，且收到回复
				 if( replyN[i]!=0 ){
					tempT = findSlotTagSet(targetset,i);
					tempNT = findSlotTagSet(nontargetset,i);
					tempTC.addAll( canReconciledMultiSeed(tempT,tempNT,seeds,Lr) );
					
					if(!tempTC.isEmpty()){
				
						ReconciledNum += 1;
						temp.addAll(tempTC);

					}
					
					tempT.clear();
					tempNT.clear();
					tempTC.clear();
					
				}else if(replyN[i]==0){
					tempT = findSlotTagSet(targetset,i);
					
					tempTC.addAll( canReconciledMultiSeed(tempT,seeds,Lr) );
					if(!tempTC.isEmpty()){
	
						ReconciledNum +=1;

						temp.addAll(tempTC);
					
					}

					tempT.clear();
					tempTC.clear();
				}
			}
			
		}
		
		int Lastf = replyW.length;
		int colNum = temp.size();  //可以收集的target标签的数目
		int Vc = (int) (ReconciledNum*(Math.log(seeds.length)/Math.log(2)));//seed 向量的总bit数目
		
		
		
		int framesize = replyW.length;
		
		double eta_c = 1.0*colNum/(Lr*framesize/96.0)*TimingScheme.t_id + (Vc/96.0)*TimingScheme.t_id;
		
		//由于已经执行上一轮的排除阶段，剩下的 W_i_1, N_i_1, 和 L_i_1 可以 直接获得，不需要像论文一样去计算了。
		int W_i_1 = targetset.size();
		int N_i_1 =  nontargetset.size();
		int L_i_1 = localtagsset.size(); //假设你能够准确估计。
		double eta_i_1 =  getOptimalFrame_eta(W_i_1, N_i_1, L_i_1)[1];
		
		
		/*String data1 = targetset.size()+"\t "+nontargetset.size()+"\t "+localtagsset.size()+"\t ";
		data1 += framesize+"\t "+colNum+"\t "+Vc +"\t "+flag+"\r";
		 Utils.logAppend(collectname, data1);*/
	
		 
		 if(colNum<1){
				return;
			}
		 
		if( eta_c >  eta_i_1 || nontargetset.size()==0){
		
			int f = replyL.length;
			tcollection += (Lr*f/96.0)*TimingScheme.t_id; //发送allocated vector的时间
			tcollection += (Vc/96.0)*TimingScheme.t_id;
						
			//收集标签时间  temp, temp2
			for(Tag t: temp){
				if(localtagsset.contains(t)){
					targetcollected.add(t);  //加入到收集信息的集合中
					tcollection += (TimingScheme.interval+TimingScheme.onebit*infLength);
					collecttotalnumber +=1;
					sumtcollection += tcollection + eachcolortime; //收集的时间
				}else{
					tcollection += TimingScheme.t_e;
				}
			}
			
			//System.out.println("temp:"+temp.size());
			localtagsset.removeAll(temp); 
			targetset.removeAll(temp);
			
		//	System.out.println("targetset:"+targetset.size()+" nontargetset: "+nontargetset.size()+"  localtagsset:"+localtagsset.size());
			
		}
		temp.clear();
		
	}
}
