package RFID;

import java.util.*;

/**
 * 
 * the following coding is the proposed KEY missing protocol.
 * @author Jiangjin
 *
 */
public class FEAT_alphais_1 extends ICNPBasic {
	
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

	public FEAT_alphais_1(){
		ptname = new String("FEAT_alphais_1");
	}
	
	double execute(){
		
		System.out.println("********start oneCategory_OurKEYMissing******** ");
		int totalbit = 0; //记录总的比特数
		tcollection = 0;  
		
		System.out.println("nontargetset.size():"+nontargetset.size());
		System.out.println("targetset:"+targetset.size());
		System.out.println("localtagsset: "+localtagsset.size());
		
		HashSet<Tag> roundIdentifiedset = new HashSet<Tag>();
		while(targetset.size()>0){ //当服务器目标标签被全部识别，协议就可以终止
			
			int sample_num = 5; //采样点数目
			int interplotion_num = 100; //插值点数目
			
			double opt_alpha = 1;//直接设置opt_alpha为1
			
			int opt_frame = (int) optimal_frame_obj(opt_alpha)[0]; //取数组第0个值得到最优帧长
			Utils.logAppend("opt_frame_alpha1.txt", "optf: "+opt_frame+"  ");
			OurKEYMissing_testingAndhopping(opt_frame); //根据最优帧长执行协议
		}
		Utils.logAppend("opt_frame_alpha1.txt", "optf_alpha1: "+"\n");
	
			
		totalMissingNumber_identified = missingset.size(); //把丢失标签总数记录到basic类中
		System.out.println("missing tag: "+ missingset.size());
		
		System.out.println("total bit:"+ totalbit);
		System.out.println("****End oneCategory_OurKEYMissing***** tcollection******:"+ tcollection);
		return tcollection;
	}
	
	
	
	public void OurKEYMissing_testingAndhopping(int optf){
		
		tcollection+=TimingScheme.t_lambda; 
		
		int seed1 = rand.nextInt();
		
		int frameS = optf; //最优帧长
		if(frameS<90){
			frameS =90;
		}
		tcollection += TimingScheme.t_id*frameS/96.0;
		
		System.out.println("frameS:"+frameS);
		
		int[] replyArr_Local = getReply(frameS,seed1,localtagsset);//收的是本地标签的回复。 用本地标签做hash。
		
		int[] replyArr_Target = getReply(frameS,seed1,targetset);//
		int[] replyArr_Nontarget = getReply(frameS,seed1,nontargetset);//
		
		//1. 过滤server端的 nontarget
		tempNotarget.clear(); //用之前先清空
		for(Tag tnt: nontargetset){
			if( replyArr_Local[tnt.slotidx]==0 || replyArr_Target[tnt.slotidx]==0){ //1.本地没有回复。2.不被target选择。均可排除
				tempNotarget.add(tnt);
			}
		}
		nontargetset.removeAll(tempNotarget);
		
		//2. 过滤server端的 target 
		tempTarget.clear();
		for(Tag tt: targetset){//排除标签
			//two cases for validating target tags: 1) 本地回复为0. 2) 仅一个target选择，不被nontarget选择。
			if( replyArr_Local[tt.slotidx]==0 ||( replyArr_Target[tt.slotidx]==1 && replyArr_Nontarget[tt.slotidx]==0) ){
				tempTarget.add(tt);
			}
		}
		targetset.removeAll(tempTarget);
		
		/*2.1 hopping phase */
		tempLocal.clear();
		
		for (Tag tl: localtagsset ){
			//1.不被target选择的标签肯定能够排除。  2.仅被一个target选择的，不被nontarget选择的
			if(replyArr_Target[tl.slotidx]==0 || ( replyArr_Target[tl.slotidx]==1 && replyArr_Nontarget[tl.slotidx]==0) ){
				tempLocal.add(tl);
			}
		}
		localtagsset.removeAll( tempLocal );
		
		int[] slot_hopping_Arr = new int[frameS];
		for (int i = 0; i < slot_hopping_Arr.length; i++) {
			slot_hopping_Arr[i] = -1; // 赋初始值为-1
		}
		
		for (int i = 0; i < slot_hopping_Arr.length; i++) {
			
			if(replyArr_Local[i]==0){ //没有收到回复赋值为0
				slot_hopping_Arr[i]=0; 
			}else if(replyArr_Target[i]==0){ //不被KEYtags选择也赋值为0
				slot_hopping_Arr[i]=0; 
			}else if(replyArr_Target[i]==1 && replyArr_Nontarget[i]==1){ //仅被一个KEYtags选择的时隙也赋值为0
				slot_hopping_Arr[i]=0;
			}else{
				slot_hopping_Arr[i] = 1; 
			}
			
		}
		
		int total_bit = 0;
		
		int last_index = -1; //起始值为-1
		for (int i = 0; i < slot_hopping_Arr.length; i++) {
			if(slot_hopping_Arr[i]==1){
				int need_bits = (int) Math.ceil( Math.log( i - last_index )/Math.log(2) );
				total_bit += need_bits ;
			}
		}
		
		tcollection += (TimingScheme.t_id* total_bit )/96.0;
		
		
		tempTarget.clear(); //用完后清空
		tempNotarget.clear(); //用完后清空
		tempLocal.clear();
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
	 *  OurKEYMissing remove identified tags
	 */
	public double OurKEYMissing_remove(HashSet<Tag> stagset, HashSet<Tag> ltagset, HashSet<Tag> stagsetReconciled){
	
		int n= stagsetReconciled.size(); 
		
		/*  得到missing 集合   */
		missingset.addAll(stagsetReconciled);   
		missingset.removeAll(ltagset); //求差集，就是missing 标签
		
		stagset.removeAll(stagsetReconciled); //remove the verified tags in server
		ltagset.removeAll(stagsetReconciled); //remove the verified tags in local
	
		return 0.0;  //
	}
	
	/**
	 * 
	 * @param alpha 能量函数参数alpha，输出最优帧长
	 * @return
	 */
	public double[] optimal_frame_obj(double alpha){
		double X = targetset.size();
		double Y = nontargetset.size();
		double Z = localtagsset.size();
		int large_f = targetset.size()+nontargetset.size();
		
		double energe_obj_arr[] = new double[large_f*2];//两倍帧长应该差不多了
		
		for (int i = 1; i < energe_obj_arr.length; i++) {
			
			double f = i; //逐步增加帧长，逐个计算energe_obj_arr[i]，
			
			double N_key = X*Math.pow(Math.E, -(X+Y)/f)* ( 1.0- Math.pow(Math.E , -Z/f) ) + X*Math.pow(Math.E , -(X+Y)/f );
			
			double N_ordi = Y*Math.pow(Math.E, -Z/f) + Y*Math.pow(Math.E, -X/f)*( 1- Math.pow(Math.E , -Z/f) );
			
			double termX = TimingScheme.t_id*f/96;
			
			double term1 = 1.0 - Math.pow(Math.E, -X/f);
			
			double term2 = 1.0 - Math.pow(Math.E, -Z/f);
			
			double t_p1 = TimingScheme.t_lambda + termX;
			
			double t_p2_max = TimingScheme.t_lambda + termX*term1*term2*( 1- Math.log(term1*term2)/Math.log(2) );
			
			double t_p2_min = TimingScheme.t_lambda + termX*term1*term2;
			
			double t_p2 = t_p2_max;
			
			double T_round = t_p1 + t_p2;
			
			double weight_nubers = N_key + alpha * N_ordi;
			
			double energe_obj = weight_nubers/T_round; //目标函数
			
			energe_obj_arr[i] = energe_obj;
		}
		
		double max_energe_obj = 0;
		int opt_f = 0;
		for (int i = 2; i < energe_obj_arr.length; i++) {
			if(energe_obj_arr[i]>max_energe_obj){
				max_energe_obj = energe_obj_arr[i];
				opt_f =i;
			}
		}
		
		double []optf_max_energe_obj = new double[2]; //第一个值为最优帧长，第二个值为帧长对应的最大energe_obj值
		optf_max_energe_obj[0] = opt_f;
		optf_max_energe_obj[1] = max_energe_obj;
		return optf_max_energe_obj;
		
	}
	
	/**
	 * 根据样本数目均匀采样获得数据点
	 * @param sample_count
	 * @return
	 */
	public double[][] sampling(int sample_count){
		
		double []alpha_Arr = new double[sample_count]; //多一个数据点，在算法中少写一个点即可
		
		
		double [][]xdata_ydata = new double[2][sample_count];
		
		double step = 1.0/(sample_count-1); //步长
		for (int i = 0; i < alpha_Arr.length; i++) {
			alpha_Arr[i] = i*step;
		}
		
		for (int i = 0; i < alpha_Arr.length; i++) {
			
			double optf_maxobj[] = new double[2];
			optf_maxobj = optimal_frame_obj(alpha_Arr[i]);
			
			xdata_ydata[0][i] = optf_maxobj[0];
			xdata_ydata[1][i] = optf_maxobj[1];
		}
		
		return xdata_ydata;//
	}

	
	public double[] lagrange_Interpolation(double []xdata, double []ydata, int total_interpolation_nums){
		
		double []total_points = new double[total_interpolation_nums];
		for (int i = 0; i < total_points.length; i++) {
			total_points[i] = 1.0*i/total_interpolation_nums ;
		}
		
		//our_lagrange ol = new our_lagrange(); //调用时需要new 该类对象，如果设置为static就直接使用。因为static方法属于类，不属于对象。
		double yy_interplt[] = new double[total_interpolation_nums];
		yy_interplt = our_lagrange.lagrange_allValue(xdata, ydata, total_points);
		
		return yy_interplt;
	}
	
	
	public double[] sample_and_interpolation(int sample_count_1, int total_interpolation_nums_1){
		
		double [][]xdata_ydata_1 = new double[2][sample_count_1];
		xdata_ydata_1 = sampling(sample_count_1);
		
	
		double yy_interplt[] = new double[total_interpolation_nums_1];
		yy_interplt = lagrange_Interpolation(xdata_ydata_1[0], xdata_ydata_1[1], total_interpolation_nums_1);
		
		
		double max_yy_interplt = 0;
		double opt_alpha = 0; //最优的alpha值
		for (int i = 2; i < yy_interplt.length; i++) {
			if(yy_interplt[i]>max_yy_interplt){
				max_yy_interplt = yy_interplt[i];
				opt_alpha =i;
			}
		}
		
		double []optalpha_maxEnergyObj = new double[2];
		optalpha_maxEnergyObj[0] = opt_alpha; //插值拟合后最优的alpha值
		optalpha_maxEnergyObj[1] = max_yy_interplt;//插值拟合得到obj的最大值
		return optalpha_maxEnergyObj;
		
	}
	
	
	
}
