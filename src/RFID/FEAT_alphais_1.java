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
//	int numCategory = categoryNum_Basic; //��������Ŀ
//	int categoryBitLength = categoryBitNum; //�õ��������Ŀ
//	
//	int[] CategoryIDArr = new int[numCategory];
//	

	public FEAT_alphais_1(){
		ptname = new String("FEAT_alphais_1");
	}
	
	double execute(){
		
		System.out.println("********start oneCategory_OurKEYMissing******** ");
		int totalbit = 0; //��¼�ܵı�����
		tcollection = 0;  
		
		System.out.println("nontargetset.size():"+nontargetset.size());
		System.out.println("targetset:"+targetset.size());
		System.out.println("localtagsset: "+localtagsset.size());
		
		HashSet<Tag> roundIdentifiedset = new HashSet<Tag>();
		while(targetset.size()>0){ //��������Ŀ���ǩ��ȫ��ʶ��Э��Ϳ�����ֹ
			
			int sample_num = 5; //��������Ŀ
			int interplotion_num = 100; //��ֵ����Ŀ
			
			double opt_alpha = 1;//ֱ������opt_alphaΪ1
			
			int opt_frame = (int) optimal_frame_obj(opt_alpha)[0]; //ȡ�����0��ֵ�õ�����֡��
			Utils.logAppend("opt_frame_alpha1.txt", "optf: "+opt_frame+"  ");
			OurKEYMissing_testingAndhopping(opt_frame); //��������֡��ִ��Э��
		}
		Utils.logAppend("opt_frame_alpha1.txt", "optf_alpha1: "+"\n");
	
			
		totalMissingNumber_identified = missingset.size(); //�Ѷ�ʧ��ǩ������¼��basic����
		System.out.println("missing tag: "+ missingset.size());
		
		System.out.println("total bit:"+ totalbit);
		System.out.println("****End oneCategory_OurKEYMissing***** tcollection******:"+ tcollection);
		return tcollection;
	}
	
	
	
	public void OurKEYMissing_testingAndhopping(int optf){
		
		tcollection+=TimingScheme.t_lambda; 
		
		int seed1 = rand.nextInt();
		
		int frameS = optf; //����֡��
		if(frameS<90){
			frameS =90;
		}
		tcollection += TimingScheme.t_id*frameS/96.0;
		
		System.out.println("frameS:"+frameS);
		
		int[] replyArr_Local = getReply(frameS,seed1,localtagsset);//�յ��Ǳ��ر�ǩ�Ļظ��� �ñ��ر�ǩ��hash��
		
		int[] replyArr_Target = getReply(frameS,seed1,targetset);//
		int[] replyArr_Nontarget = getReply(frameS,seed1,nontargetset);//
		
		//1. ����server�˵� nontarget
		tempNotarget.clear(); //��֮ǰ�����
		for(Tag tnt: nontargetset){
			if( replyArr_Local[tnt.slotidx]==0 || replyArr_Target[tnt.slotidx]==0){ //1.����û�лظ���2.����targetѡ�񡣾����ų�
				tempNotarget.add(tnt);
			}
		}
		nontargetset.removeAll(tempNotarget);
		
		//2. ����server�˵� target 
		tempTarget.clear();
		for(Tag tt: targetset){//�ų���ǩ
			//two cases for validating target tags: 1) ���ػظ�Ϊ0. 2) ��һ��targetѡ�񣬲���nontargetѡ��
			if( replyArr_Local[tt.slotidx]==0 ||( replyArr_Target[tt.slotidx]==1 && replyArr_Nontarget[tt.slotidx]==0) ){
				tempTarget.add(tt);
			}
		}
		targetset.removeAll(tempTarget);
		
		/*2.1 hopping phase */
		tempLocal.clear();
		
		for (Tag tl: localtagsset ){
			//1.����targetѡ��ı�ǩ�϶��ܹ��ų���  2.����һ��targetѡ��ģ�����nontargetѡ���
			if(replyArr_Target[tl.slotidx]==0 || ( replyArr_Target[tl.slotidx]==1 && replyArr_Nontarget[tl.slotidx]==0) ){
				tempLocal.add(tl);
			}
		}
		localtagsset.removeAll( tempLocal );
		
		int[] slot_hopping_Arr = new int[frameS];
		for (int i = 0; i < slot_hopping_Arr.length; i++) {
			slot_hopping_Arr[i] = -1; // ����ʼֵΪ-1
		}
		
		for (int i = 0; i < slot_hopping_Arr.length; i++) {
			
			if(replyArr_Local[i]==0){ //û���յ��ظ���ֵΪ0
				slot_hopping_Arr[i]=0; 
			}else if(replyArr_Target[i]==0){ //����KEYtagsѡ��Ҳ��ֵΪ0
				slot_hopping_Arr[i]=0; 
			}else if(replyArr_Target[i]==1 && replyArr_Nontarget[i]==1){ //����һ��KEYtagsѡ���ʱ϶Ҳ��ֵΪ0
				slot_hopping_Arr[i]=0;
			}else{
				slot_hopping_Arr[i] = 1; 
			}
			
		}
		
		int total_bit = 0;
		
		int last_index = -1; //��ʼֵΪ-1
		for (int i = 0; i < slot_hopping_Arr.length; i++) {
			if(slot_hopping_Arr[i]==1){
				int need_bits = (int) Math.ceil( Math.log( i - last_index )/Math.log(2) );
				total_bit += need_bits ;
			}
		}
		
		tcollection += (TimingScheme.t_id* total_bit )/96.0;
		
		
		tempTarget.clear(); //��������
		tempNotarget.clear(); //��������
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
		
		/*  �õ�missing ����   */
		missingset.addAll(stagsetReconciled);   
		missingset.removeAll(ltagset); //��������missing ��ǩ
		
		stagset.removeAll(stagsetReconciled); //remove the verified tags in server
		ltagset.removeAll(stagsetReconciled); //remove the verified tags in local
	
		return 0.0;  //
	}
	
	/**
	 * 
	 * @param alpha ������������alpha���������֡��
	 * @return
	 */
	public double[] optimal_frame_obj(double alpha){
		double X = targetset.size();
		double Y = nontargetset.size();
		double Z = localtagsset.size();
		int large_f = targetset.size()+nontargetset.size();
		
		double energe_obj_arr[] = new double[large_f*2];//����֡��Ӧ�ò����
		
		for (int i = 1; i < energe_obj_arr.length; i++) {
			
			double f = i; //������֡�����������energe_obj_arr[i]��
			
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
			
			double energe_obj = weight_nubers/T_round; //Ŀ�꺯��
			
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
		
		double []optf_max_energe_obj = new double[2]; //��һ��ֵΪ����֡�����ڶ���ֵΪ֡����Ӧ�����energe_objֵ
		optf_max_energe_obj[0] = opt_f;
		optf_max_energe_obj[1] = max_energe_obj;
		return optf_max_energe_obj;
		
	}
	
	/**
	 * ����������Ŀ���Ȳ���������ݵ�
	 * @param sample_count
	 * @return
	 */
	public double[][] sampling(int sample_count){
		
		double []alpha_Arr = new double[sample_count]; //��һ�����ݵ㣬���㷨����дһ���㼴��
		
		
		double [][]xdata_ydata = new double[2][sample_count];
		
		double step = 1.0/(sample_count-1); //����
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
		
		//our_lagrange ol = new our_lagrange(); //����ʱ��Ҫnew ��������������Ϊstatic��ֱ��ʹ�á���Ϊstatic���������࣬�����ڶ���
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
		double opt_alpha = 0; //���ŵ�alphaֵ
		for (int i = 2; i < yy_interplt.length; i++) {
			if(yy_interplt[i]>max_yy_interplt){
				max_yy_interplt = yy_interplt[i];
				opt_alpha =i;
			}
		}
		
		double []optalpha_maxEnergyObj = new double[2];
		optalpha_maxEnergyObj[0] = opt_alpha; //��ֵ��Ϻ����ŵ�alphaֵ
		optalpha_maxEnergyObj[1] = max_yy_interplt;//��ֵ��ϵõ�obj�����ֵ
		return optalpha_maxEnergyObj;
		
	}
	
	
	
}
