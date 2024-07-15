package RFID;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.*;
public class Simulation {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//int[] ninflength = new int[]{32,64,96};
		//int[] ncollectionlow = new int[]{4000,5000,10000,15000,20000,25000};// target��Ŀ
		
		Params para = new Params(); //����setDefault()


	//	ReaderGenerator rg = getReaders(para);// �õ����沿����Ķ���������ɫ
//		para.setDefault();
//		para.alpha=0.05; //BIC��Bloom filter�ļ������ʡ� ǰ������ʵ����0.05. ����BIC�Լ���ʵ����0.0001.
		
		
		/**-- theoretical vs simulation category number:100,500, 1000,10000. system tags: 100000 --*/
		
//		para.paraname = "exptheoretical-fixM-100000/uniform";
//		
//		for(int round = 0; round < 500; round++){
//			
//			int[] ngroupNumArr = new int[]{100, 500, 1000,10000};
////			for (int i = 0; i < ngroupNumArr.length; i++) {
////				ngroupNumArr[i] = 100*Math.;
////			}
//			for(int ngroupNum:ngroupNumArr){
//				
//				para.groupNum = ngroupNum; //��������
//				System.out.println("para.groupNum:"+para.groupNum);
//				para.setSystenTagNum(100000);
//				para.setGroupNums(para.nTaginsys, para.groupNum);
//				
//				simSingleReader(para);
//				
//			}
//		}


		
/**-- FEAT ����ʵ�� (�ؼ���ǩ�����仯) , fix missing rate = 0.5, server tags=50000. key tag ratio varies: 0.1��0.01��1 .--*/
//		
//		para.paraname = "FEAT_EFEAT_add_exp/keyratio_0.1_0.01_1_missratio0.1";
//		for(int round = 0; round < 100; round++){
//			para.categoryNum = 2; //�����2: �ؼ���ǩ��� �� �ǹؼ���ǩ���ע��ʼ�ղ���
//			
//			para.setServerTagNum(50000); //���÷�������ǩ��
//			
//			double[] keyratio_Arr = new double[90];
//			for (int i = 0; i < keyratio_Arr.length; i++) {
//				
//				keyratio_Arr[i] = 0.1 + i*0.01;
//			}
//			double missingrate = 0.1;
//			
//			for(double keyratio_i: keyratio_Arr){
////	
//				int []KEY_noKEY_Arr = Generator_KEY_noKEY_Arr(para.nTaginserver, keyratio_i); //
//				
//				//����key��ǩ��no-key��ǩ����PICЭ���ָ�Ƴ���
//				int ntarget = KEY_noKEY_Arr[0];
//				int nnontarget = KEY_noKEY_Arr[1];
//				para.fingerLength = (int)( 6 - (Math.log((double)(ntarget)/nnontarget)/Math.log(2))); //ָ�Ƴ���
//				
//				for (int i = 0; i < KEY_noKEY_Arr.length; i++) {
//					System.out.println("KEY_noKEY_Arr "+KEY_noKEY_Arr[i]);
//				}
//				
//				para.setEachCategoryNum_server_KEY_noKEY(KEY_noKEY_Arr);
//				para.setEachCategoryNum_KEY_noKEY_system(KEY_noKEY_Arr, missingrate);
//				
//				simSingleReader(para);
//				
//			}
//			
//			
//		}
//		
		
/**-- FEAT ����ʵ�� (�ؼ���ǩ�����仯) , fix missing rate = 0.5, server tags=50000. key tag ratio varies: 0.1��0.01��1 .--*/
		
//		para.paraname = "alltags_exp/keyratio_0.1_0.01_1_missratio0.5_ok";
//		for(int round = 0; round < 50; round++){
//			para.categoryNum = 2; //�����2: �ؼ���ǩ��� �� �ǹؼ���ǩ���ע��ʼ�ղ���
//			
//			para.set_flag_key_missing(false); //����key��ǩ��ʧʶ��
//			
//			para.setServerTagNum(50010); //���÷�������ǩ��
//			
//			double missingrate = 0.5;
//			//double KEYRatio = 0.2;
//			
//			//����key��ǩ��no-key��ǩ����PICЭ���ָ�Ƴ���
//			
//			int [] KEY_noKEY_Arr = {50000, 10}; //��̬��ʼ��
//			int ntarget = 50000;
//			int nnontarget = 10;
//			para.fingerLength = (int)( 6 - (Math.log((double)(ntarget)/nnontarget)/Math.log(2))); //ָ�Ƴ���. ��FEAT�޹�
//			
//
//			para.setEachCategoryNum_server_KEY_noKEY(KEY_noKEY_Arr);
//			para.setEachCategoryNum_KEY_noKEY_system(KEY_noKEY_Arr, missingrate);
//			
//			simSingleReader(para);
//				
//
//		}
		
		

		/** FEAT ����ʵ�� (��ʧ�����仯  0.1 - 0.9 ) , all tag identification.
		 * system tags = 50000, key tag ratio = 1.0,  missing rate varies: 0.1 to 0.9
		 **/	
				para.paraname = "alltags_exp/missratio_0.1_0.1_0.9_okk";
				for(int round = 0; round < 50; round++){
					para.categoryNum = 2; //�����2: �ؼ���ǩ��� �� �ǹؼ���ǩ���ע��ʼ�ղ���
					
					para.set_flag_key_missing(false); //����key��ǩ��ʧʶ��
					
					para.setServerTagNum(50010); //���÷�������ǩ��
					
					// ��ʧ��ǩ����Խ�٣�ϵͳ��ǩ��ĿԽ�ࡣ
					double[] missingRatio_Arr = new double[9];
					for (int i = 0; i < missingRatio_Arr.length; i++) {
						missingRatio_Arr[i] = 0.1 + i*0.1;   
					}
				//	double missingrate = 0.5;
					double KEYRatio = 1.0;
					
					for(double missingRatio_i: missingRatio_Arr){
						
						
						int []KEY_noKEY_Arr = {50000,10}; // ��̬��ʼ��
						
						//����key��ǩ��no-key��ǩ����PICЭ���ָ�Ƴ���
						int ntarget = KEY_noKEY_Arr[0];
						int nnontarget = KEY_noKEY_Arr[1];
						para.fingerLength = (int)( 6 - (Math.log((double)(ntarget)/nnontarget)/Math.log(2))); //ָ�Ƴ���
						
						for (int i = 0; i < KEY_noKEY_Arr.length; i++) {
							System.out.println("KEY_noKEY_Arr "+KEY_noKEY_Arr[i]);
						}
						
						para.setEachCategoryNum_server_KEY_noKEY(KEY_noKEY_Arr);
						para.setEachCategoryNum_KEY_noKEY_system(KEY_noKEY_Arr, missingRatio_i);//���ö�ʧ����
						
						simSingleReader(para);
						
					}
					
				}
		
		
		
/**-- FEAT exp1.1(�ؼ���ǩ�����仯) , fix missing rate = 0.5, server tags=50000. key tag ratio varies: 0.1 to 0.9 .--*/
		
//		para.paraname = "FEAT_EFEAT/keyratio_0.1_0.9";
//		for(int round = 0; round < 100; round++){
//			para.categoryNum = 2; //�����2: �ؼ���ǩ��� �� �ǹؼ���ǩ���ע��ʼ�ղ���
//			
//			para.setServerTagNum(50000); //���÷�������ǩ��
//			
//			double[] keyratio_Arr = new double[9];
//			for (int i = 0; i < keyratio_Arr.length; i++) {
//				keyratio_Arr[i] = 0.1 + i*0.1;
//			}
//			double missingrate = 0.5;
//			//double KEYRatio = 0.2;
//			
//			for(double keyratio_i: keyratio_Arr){
////	
//				int []KEY_noKEY_Arr = Generator_KEY_noKEY_Arr(para.nTaginserver, keyratio_i); //
//				
//				//����key��ǩ��no-key��ǩ����PICЭ���ָ�Ƴ���
//				int ntarget = KEY_noKEY_Arr[0];
//				int nnontarget = KEY_noKEY_Arr[1];
//				para.fingerLength = (int)( 6 - (Math.log((double)(ntarget)/nnontarget)/Math.log(2))); //ָ�Ƴ���
//				
//				for (int i = 0; i < KEY_noKEY_Arr.length; i++) {
//					System.out.println("KEY_noKEY_Arr "+KEY_noKEY_Arr[i]);
//				}
//				
//				para.setEachCategoryNum_server_KEY_noKEY(KEY_noKEY_Arr);
//				para.setEachCategoryNum_KEY_noKEY_system(KEY_noKEY_Arr, missingrate);
//				
//				simSingleReader(para);
//				
//			}
//			
//		}
		

		
///**-- FEAT exp1.2(�ؼ���ǩ�����仯  0.01 - 0.1 ) , fix missing rate = 0.5, server tags=50000. key tag ratio varies: 0.01 to 0.1 .--*/
		
//		para.paraname = "FEAT_EFEAT/keyratio_0.01_0.1";
//		for(int round = 0; round < 100; round++){
//			para.categoryNum = 2; //�����2: �ؼ���ǩ��� �� �ǹؼ���ǩ���ע��ʼ�ղ���
//			
//			para.setServerTagNum(50000); //���÷�������ǩ��
//			
//			double[] keyratio_Arr = new double[9];
//			for (int i = 0; i < keyratio_Arr.length; i++) {
//				keyratio_Arr[i] = 0.01 + i*0.01;
//			}
//			double missingrate = 0.5;
//			//double KEYRatio = 0.2;
//			
//			for(double keyratio_i: keyratio_Arr){
////	
//				int []KEY_noKEY_Arr = Generator_KEY_noKEY_Arr(para.nTaginserver, keyratio_i); //
//				
//				//����key��ǩ��no-key��ǩ����PICЭ���ָ�Ƴ���
//				int ntarget = KEY_noKEY_Arr[0];
//				int nnontarget = KEY_noKEY_Arr[1];
//				para.fingerLength = (int)( 6 - (Math.log((double)(ntarget)/nnontarget)/Math.log(2))); //ָ�Ƴ���
//				
//				for (int i = 0; i < KEY_noKEY_Arr.length; i++) {
//					System.out.println("KEY_noKEY_Arr "+KEY_noKEY_Arr[i]);
//				}
//				
//				para.setEachCategoryNum_server_KEY_noKEY(KEY_noKEY_Arr);
//				para.setEachCategoryNum_KEY_noKEY_system(KEY_noKEY_Arr, missingrate);
//				
//				simSingleReader(para);
//				
//			}
//			
//		}
		


		
/** FEAT exp1.3 (ϵͳ��ǩ��Ŀ�仯  10000 - 100000 ) , 
 * missing rate = 0.5, key tag ratio = 0.1 . systemtags varies: 10,000 to 100,000--
 **/
		
//		para.paraname = "FEAT_EFEAT/systemtags_10000_100000";
//		for(int round = 0; round < 100; round++){
//			para.categoryNum = 2; //�����2: �ؼ���ǩ��� �� �ǹؼ���ǩ���ע��ʼ�ղ���
//			
//			//para.setServerTagNum(50000); //���÷�������ǩ��
//			
//			int[] ServerTagNum_Arr = new int[10];
//			for (int i = 0; i < ServerTagNum_Arr.length; i++) {
//				ServerTagNum_Arr[i] = 10000 + i*10000;
//			}
//			double missingrate = 0.5;
//			double KEYRatio = 0.1;
//			
//			for(int ServerTagNum_i: ServerTagNum_Arr){
//				
//				para.setServerTagNum(ServerTagNum_i); //����ϵͳ��ǩ��Ŀ��
//				
//				int []KEY_noKEY_Arr = Generator_KEY_noKEY_Arr(para.nTaginserver, KEYRatio); // KEYRatio �ؼ���ǩ����
//				
//				//����key��ǩ��no-key��ǩ����PICЭ���ָ�Ƴ���
//				int ntarget = KEY_noKEY_Arr[0];
//				int nnontarget = KEY_noKEY_Arr[1];
//				para.fingerLength = (int)( 6 - (Math.log((double)(ntarget)/nnontarget)/Math.log(2))); //ָ�Ƴ���
//				
//				for (int i = 0; i < KEY_noKEY_Arr.length; i++) {
//					System.out.println("KEY_noKEY_Arr "+KEY_noKEY_Arr[i]);
//				}
//				
//				para.setEachCategoryNum_server_KEY_noKEY(KEY_noKEY_Arr);
//				para.setEachCategoryNum_KEY_noKEY_system(KEY_noKEY_Arr, missingrate);
//				
//				simSingleReader(para);
//				
//			}
//			
//		}

		
/** FEAT exp1.4 (��ʧ�����仯  0.1 - 0.9 ) , 
 * system tags = 50000, key tag ratio = 0.1,  missing rate varies: 0.1 to 0.9
 **/	
//		para.paraname = "FEAT_EFEAT/missingRate_0.1_0.9";
//		for(int round = 0; round < 100; round++){
//			para.categoryNum = 2; //�����2: �ؼ���ǩ��� �� �ǹؼ���ǩ���ע��ʼ�ղ���
//			
//			para.setServerTagNum(50000); //���÷�������ǩ��
//			
//			// ��ʧ��ǩ����Խ�٣�ϵͳ��ǩ��ĿԽ�ࡣ
//			double[] missingRatio_Arr = new double[9];
//			for (int i = 0; i < missingRatio_Arr.length; i++) {
//				missingRatio_Arr[i] = 0.1 + i*0.1;   
//			}
//		//	double missingrate = 0.5;
//			double KEYRatio = 0.1;
//			
//			for(double missingRatio_i: missingRatio_Arr){
//				
//				
//				int []KEY_noKEY_Arr = Generator_KEY_noKEY_Arr(para.nTaginserver, KEYRatio); // KEYRatio �ؼ���ǩ����
//				
//				//����key��ǩ��no-key��ǩ����PICЭ���ָ�Ƴ���
//				int ntarget = KEY_noKEY_Arr[0];
//				int nnontarget = KEY_noKEY_Arr[1];
//				para.fingerLength = (int)( 6 - (Math.log((double)(ntarget)/nnontarget)/Math.log(2))); //ָ�Ƴ���
//				
//				for (int i = 0; i < KEY_noKEY_Arr.length; i++) {
//					System.out.println("KEY_noKEY_Arr "+KEY_noKEY_Arr[i]);
//				}
//				
//				para.setEachCategoryNum_server_KEY_noKEY(KEY_noKEY_Arr);
//				para.setEachCategoryNum_KEY_noKEY_system(KEY_noKEY_Arr, missingRatio_i);//���ö�ʧ����
//				
//				simSingleReader(para);
//				
//			}
//			
//		}
//		

/** FEAT exp1.5 (��ʧ�����仯  0.01 - 0.1�� ϵͳ��ǩ�������ڡ� ) , 
 * system tags = 50000, key tag ratio = 0.1,  missing rate varies: 0.01 to 0.1
 **/	
//		para.paraname = "FEAT_EFEAT/missingRate_0.01_0.1";
//		for(int round = 0; round < 100; round++){
//			para.categoryNum = 2; //�����2: �ؼ���ǩ��� �� �ǹؼ���ǩ���ע��ʼ�ղ���
//			
//			para.setServerTagNum(50000); //���÷�������ǩ��
//			
//			double[] missingRatio_Arr = new double[9];
//			for (int i = 0; i < missingRatio_Arr.length; i++) {
//				missingRatio_Arr[i] = 0.01 + i*0.01;
//			}
//		//	double missingrate = 0.5;
//			double KEYRatio = 0.1;
//			
//			for(double missingRatio_i: missingRatio_Arr){
//				
//				
//				int []KEY_noKEY_Arr = Generator_KEY_noKEY_Arr(para.nTaginserver, KEYRatio); // KEYRatio �ؼ���ǩ����
//				
//				//����key��ǩ��no-key��ǩ����PICЭ���ָ�Ƴ���
//				int ntarget = KEY_noKEY_Arr[0];
//				int nnontarget = KEY_noKEY_Arr[1];
//				para.fingerLength = (int)( 6 - (Math.log((double)(ntarget)/nnontarget)/Math.log(2))); //ָ�Ƴ���
//				
//				for (int i = 0; i < KEY_noKEY_Arr.length; i++) {
//					System.out.println("KEY_noKEY_Arr "+KEY_noKEY_Arr[i]);
//				}
//				
//				para.setEachCategoryNum_server_KEY_noKEY(KEY_noKEY_Arr);
//				para.setEachCategoryNum_KEY_noKEY_system(KEY_noKEY_Arr, missingRatio_i);//���ö�ʧ����
//				
//				simSingleReader(para);
//				
//			}
//			
//		}
//		
		
/**-- PMI exp1.2(Ŀ���ǩ�����仯) , fix missing rate = 0.5, server tags=10000. key tag ratio varies: 0.1 to 0.9 .--*/
		/*		
		para.paraname = "KEYMissing/keyratio_missRatio0.5_veki_erki";
		for(int round = 0; round < 30; round++){
			para.categoryNum = 2; //�����2: �ؼ���ǩ��� �� �ǹؼ���ǩ���ע��ʼ�ղ���
			
			para.setServerTagNum(10000); //���÷�������ǩ��
			
			double[] keyratio_Arr = new double[1];
			for (int i = 0; i < keyratio_Arr.length; i++) {
				keyratio_Arr[i] = 0.4;
			}
			double missingrate = 0.5;
			//double KEYRatio = 0.2;
			
			for(double keyratio_i: keyratio_Arr){
//	
				int []KEY_noKEY_Arr = Generator_KEY_noKEY_Arr(para.nTaginserver, keyratio_i); //
				
				//����key��ǩ��no-key��ǩ����PICЭ���ָ�Ƴ���
				int ntarget = KEY_noKEY_Arr[0];
				int nnontarget = KEY_noKEY_Arr[1];
				para.fingerLength = (int)( 6 - (Math.log((double)(ntarget)/nnontarget)/Math.log(2))); //ָ�Ƴ���
				
				for (int i = 0; i < KEY_noKEY_Arr.length; i++) {
					System.out.println("KEY_noKEY_Arr "+KEY_noKEY_Arr[i]);
				}
				
				para.setEachCategoryNum_server_KEY_noKEY(KEY_noKEY_Arr);
				
				//para.nTaginsys = (int) ((1 - missingrate)*para.nTaginserver);//����ϵͳ��ǩ��
				//para.nMissingNum = para.nTaginserver - para.nTaginsys;//��ʧ��ǩ
				para.setEachCategoryNum_KEY_noKEY_system(KEY_noKEY_Arr, missingrate);
				
				simSingleReader(para);
				
			}
			
		}
		*/
		
/**-- PMI exp1.1(���ȷֲ�) , fix missing rate = 0.5, category num = 50. server tags varies: 10,000 to 100,000. --*/
		
//		para.paraname = "KEYMissing/exp01_uniform/server_num";
//		for(int round = 0; round < 1; round++){
//			para.categoryNum = 2; //�����2: �ؼ���ǩ��� �� �ǹؼ���ǩ���
//			
//			int[] nTaginserver_Arr = new int[2];
//			for (int i = 0; i < nTaginserver_Arr.length; i++) {
//				nTaginserver_Arr[i] = 10000 + i*10000;
//			}
//			double missingrate = 0.5;
//			double KEYRatio = 0.2;
//			
//			for(int nInServer:nTaginserver_Arr){
////				
//				para.setServerTagNum(nInServer); //���÷�������ǩ��
//				
//				int []KEY_noKEY_Arr = Generator_KEY_noKEY_Arr(para.nTaginserver, KEYRatio); //
//	
//				//����key��ǩ��no-key��ǩ����PICЭ���ָ�Ƴ���
//				int ntarget = KEY_noKEY_Arr[0];
//				int nnontarget = KEY_noKEY_Arr[1];
//				para.fingerLength = (int)( 6 - (Math.log((double)(ntarget)/nnontarget)/Math.log(2))); //ָ�Ƴ���
//				
//				for (int i = 0; i < KEY_noKEY_Arr.length; i++) {
//					System.out.println("KEY_noKEY_Arr "+KEY_noKEY_Arr[i]);
//				}
//				
//				para.setEachCategoryNum_server_KEY_noKEY(KEY_noKEY_Arr);
//				
//				//para.nTaginsys = (int) ((1 - missingrate)*para.nTaginserver);//����ϵͳ��ǩ��
//				//para.nMissingNum = para.nTaginserver - para.nTaginsys;//��ʧ��ǩ
//				para.setEachCategoryNum_KEY_noKEY_system(KEY_noKEY_Arr, missingrate);
//				
//				simSingleReader(para);
//				
//			}
//			
//		}
		
		
///**-- PMI exp1.2(���ȷֲ�) , server tags = 50000, category num = 50. missing rate: 0.1 - 0.9. --*/
//		
//		para.paraname = "missing-opt/exp01_uniform/missing_rate";
//		for(int round = 0; round < 50; round++){
//			
//			para.categoryNum = 50; //�����
//			para.setServerTagNum(50000); //���÷�������ǩ��
//			
//			double[] missingrate_Arr = new double[9];
//			for (int i = 0; i < missingrate_Arr.length; i++) {
//				missingrate_Arr[i] = 0.1 + i*0.1;
//			}
//			
//			for(double missingrate:missingrate_Arr){//missing rate �ı�
////				
//				para.setEachCategoryNum_server_Uniform(para.nTaginserver, para.categoryNum);//���÷������У�ÿ����ı�ǩ��Ŀ(���ȷֲ�)
//				para.nTaginsys = (int) ((1 - missingrate)*para.nTaginserver);
//				para.nMissingNum = para.nTaginserver - para.nTaginsys;//����ϵͳ��ǩ��
//				para.setEachCategoryNum_system_Uniform(para.nTaginsys, para.categoryNum);//���÷������У�ÿ����ı�ǩ��Ŀ(���ȷֲ�)
//				simSingleReader(para);
//			
//				
//			}
//			
//		}
//		
///**-- PMI exp1.3(���ȷֲ�) , fix missing rate = 0.5, server num = 50. category num: 10 to 90. --*/
//		
//		para.paraname = "missing-opt/exp01_uniform/category_num";
//		for(int round = 0; round < 50; round++){
//			
//			int[] ncategory_Arr = new int[10];
//			for (int i = 0; i < ncategory_Arr.length; i++){
//				ncategory_Arr[i] = 10 + i*10;
//			}
//			
//			double missingrate = 0.5;
//			para.setServerTagNum(50000); //���÷�������ǩ��
//			
//			for(int ncategory:ncategory_Arr){
//				para.categoryNum = ncategory; //�����
//				para.setEachCategoryNum_server_Uniform(para.nTaginserver, para.categoryNum);//���÷������У�ÿ����ı�ǩ��Ŀ(���ȷֲ�)
//				para.nTaginsys = (int) ((1 - missingrate)*para.nTaginserver);
//				para.nMissingNum = para.nTaginserver - para.nTaginsys;//����ϵͳ��ǩ��
//				para.setEachCategoryNum_system_Uniform(para.nTaginsys, para.categoryNum);//���÷������У�ÿ����ı�ǩ��Ŀ(���ȷֲ�)
//				
//				simSingleReader(para);
//				
//				
//			}
//			
//		}
	
		
		
		//
	}//������Ų���ע��
	
	
	
	public static int[] Generator_KEY_noKEY_Arr(int n_server, double KEYtag_ratio){
		int N_category = 2;
		int[] KEY_noKEY_NumArr = new int[N_category];
		KEY_noKEY_NumArr[0] = (int) (n_server*KEYtag_ratio);
		KEY_noKEY_NumArr[1] = n_server - KEY_noKEY_NumArr[0];
		
		
		return KEY_noKEY_NumArr;
		
	}
	
	
	
	/**
	 * 
	 * @param totalM �ܱ�ǩ��
	 * @param N ��category��Ŀ
	 * @return
	 */
	public static int[] GuassGeneratorCategory(int totalM, int N){
		
		int[] categoryNumArr = new int[N];
		for (int i = 0; i < categoryNumArr.length; i++) {//��Ϊÿ�������ٷ���һ����ǩ
			categoryNumArr[i]=1;
		}
		int ii = 0;
		while (ii < totalM-N) {
			Random randGauss = new Random();
			int loc = (int) ( (N)*randGauss.nextGaussian() + (N/2) ); //mean: lambda/2 and standard: 8
			
			if(loc>=N||loc<0){
				
				System.out.println("����orС��: "+loc);
				
			}else{
				ii+=1;
				categoryNumArr[loc]++; //Ϊ����ı�ǩ����1.
			}
			
		}
//		int sum = 0;
//		for (int i = 0; i < categoryNumArr.length; i++) {
//			System.out.println(i+" :"+categoryNumArr[i]);
//			sum+=categoryNumArr[i];
//		}
//		System.out.println("�ܺͣ�"+sum);
		
		return categoryNumArr;
		
	}
	
	
	/**
	 * ��ָ�� fingerprint
	 * @param para
	 * @param rg
	 * @param repeat
	 */
	public static void simSingleReader(Params para){
		//first set up simulation environment
		//generate readers and compute the schedule by using a coloring algorithm
		
		ICNPBasic.alpha = para.alpha;
		ICNPBasic.categoryNum_Basic = para.categoryNum;
		ICNPBasic.categoryBitNum = para.categoryBitLength; //Ĭ��32
		ICNPBasic.setActualCategoryNumArr(para.categoryNum, para.Each_KEY_noKEY_NumArr_ser); //Ҫ���ó�static
		
		ICNPBasic.flag_key_missing = para.flag_key_missing;
		
		OneCategory_SFMTI sfmti = new OneCategory_SFMTI();
		
		OneCategory_IIP iip = new OneCategory_IIP();
		OneCategory_CLS cls = new OneCategory_CLS();
		OneCategory_CR_MTI cr_mti = new OneCategory_CR_MTI();
		
		OneCategory_VEKI veki = new OneCategory_VEKI();
		OneCategory_ERKI erki = new OneCategory_ERKI();
		
		FEAT feat = new FEAT();
		
		
		Advance_FEAT advfeat = new Advance_FEAT();
		
		Ablation_Advance_FEAT abltion_advfeat = new Ablation_Advance_FEAT();
		
		FEAT_mean feat_mean = new FEAT_mean();
		
		FEAT_alpha_random feat_random = new FEAT_alpha_random();
		
		FEAT_min feat_min = new FEAT_min();
		FEAT_alphais_1 feat_a1 = new FEAT_alphais_1();
		
		
		New_PIC pic = new New_PIC();
		BasicTIC tic = new BasicTIC();
		ETIC4_8 etic = new ETIC4_8();
		
		for(int repeattime = 0; repeattime < 1; repeattime++){
			
		    sfmti.reset();
		    iip.reset();
		    cls.reset();
		    cr_mti.reset();
		    
		    veki.reset();
		    erki.reset();
		    
		    feat.reset();
		    feat_min.reset();
		    feat_a1.reset();
		    feat_mean.reset();
		    feat_random.reset();
		    
		    pic.reset();
		    tic.reset();
		    etic.reset();
		    
			sfmti.totalcollection = 0;
			iip.totalcollection = 0;
		    cls.totalcollection = 0;
		    cr_mti.totalcollection = 0;
			
		    veki.totalcollection =0;
		    erki.totalcollection =0;
		   
		    feat.totalcollection =0;
		    
		    advfeat.totalcollection = 0;
		    abltion_advfeat.totalcollection = 0;
		    
		    feat_min.totalcollection =0;
		    feat_a1.totalcollection =0;
		    
		    feat_mean.totalcollection =0;
		    feat_random.totalcollection =0;
		    
		    pic.totalcollection=0;
		    tic.totalcollection=0;
		    etic.totalcollection=0;
		    
			//generate new tags in the system
			TagGenerator.flush();//flush old tags
			TagGenerator.realflush();  //�Ҿ���Ӧ��Ҫ���tagpool
			
		
			//generate tags in the system
			//TagGenerator.gettotalTags(para.nTaginsys, para.length, para.width);//�ڷ�Χ�����ɱ�ǩ. ��Ϊˢ�¹�
			//TagGenerator.gettotalTags(para.nTaginsys, para.length, para.width,para.fingerLength); //generate tags with fingerprint
			
			//TagGenerator.gettotalTags(para.nTaginsys, para.length, para.width, para.fingerLength, 64); //64��ʾ������ ������˹�ֲ�
			
			/**�����Ǹ�˹�ֲ�Ҳ�����ȴ�һ��groupNumArr�������ڱ�ǩ�������ŵ��м�Ӧ�ö����ǵķ���ʱ��Ч����á�*/
			//TagGenerator.gettotalTags(para.nTaginsys, para.length, para.width, para.fingerLength, para.groupNum, para.groupNumArr); //64��ʾ������ ������˹�ֲ�
			
			System.out.println(" para.Each_KEY_noKEY_NumArr_ser[0]:"+ para.Each_KEY_noKEY_NumArr_ser[0]);
			System.out.println(" para.Each_KEY_noKEY_NumArr_ser[1]:"+ para.Each_KEY_noKEY_NumArr_ser[1]);
			
			//server�ı�ǩ�����ı�ǩ��
		
			TagGenerator.gettotalTags(para.nTaginserver, para.length, para.width, para.fingerLength, para.categoryNum, para.Each_KEY_noKEY_NumArr_ser,para.categoryBitLength,para.CIDRandomGenerate ); //64��ʾ������ ������˹�ֲ�
			

			
			HashSet<Tag> inserver = TagGenerator.getTags(para.nTaginserver); //��������ǩ
			
			/* 1. �Ȼ�÷�������target��ǩ ��  nontarget��ǩ*/
//			System.out.println(" para.Each_KEY_noKEY_NumArr_ser[0]:"+ para.Each_KEY_noKEY_NumArr_ser[0]);
//			System.out.println(" para.Each_KEY_noKEY_NumArr_ser[1]:"+ para.Each_KEY_noKEY_NumArr_ser[1]);
			
			HashSet<Tag> target = TagGenerator.getTags( para.num_KEYtags_ser, 0); //obtain KEY tags
			HashSet<Tag> nontarget = new HashSet<Tag>(); //nontarget tags
			nontarget.addAll( inserver );  
			nontarget.removeAll( target ); //the remainder tags are nontarget.
				
			
			System.out.println("----para.Each_KEY_noKEY_NumArr_sys[0]"+para.Each_KEY_noKEY_NumArr_sys[0]);
			System.out.println("----para.Each_KEY_noKEY_NumArr_sys[1]"+para.Each_KEY_noKEY_NumArr_sys[1]);
			/*2. ����ϵͳ��ǩ��������������𣨹ؼ���ǩ���ͷǹؼ���ǩ��𣩣���ÿ��������� gettags*/
			HashSet<Tag> insys = new HashSet<Tag>();
			insys.clear();
			for(int cateIndex=0; cateIndex<para.categoryNum;cateIndex++){//�����𣬼����ǩ��ϵͳ��
				HashSet<Tag> temp =  TagGenerator.getTags(para.Each_KEY_noKEY_NumArr_sys[cateIndex], cateIndex) ;
				System.out.println("cateIndex: "+ cateIndex);
				for(Tag t: temp){
					System.out.print(" "+ t.categoryID);
				}
				//System.out.println("temp"+cateNum+" "+temp.size());
				insys.addAll( temp);//������˳�����ɣ�cateNum Ҳ���Կ���CID
				temp.clear();
			}
			
			Utils.debug( "**********Server Number: "+ inserver.size() + "*********System Number:" + insys.size() );
			
			
			//associate tags with readers that can read them, i.e., generate localset for every reader
			Reader rd = new Reader(1,0,0);
			rd.vTags.clear();
			rd.vTags.addAll(insys);
			
			//The time to collect all target tags
		
			boolean flag = true; //���ñ�ǣ�����ɫԭ�������һ��ʱ��Ҫ����Ŀ���ǩ���ϡ�
			
		
				//for the m-th round
				double te1dfsa = 0;
				double tsfmti =0, tpmi =0, tepmi =0;  //missing
				double tiip  = 0;
				double tcls = 0;
				double tcr_mti = 0;
				double tveki = 0, terki = 0;
				double tfeat = 0, tfeat_min=0, tfeat_a1=0 , tfeat_mean=0, tfeat_random=0 ;
				double tpic = 0, ttic=0, tetic=0;
				double tadvfeat = 0;
				double tabltion_advfeat = 0;
				
			
				//FEAT
				feat.reset();
				feat.setServerset(inserver);
				feat.settargetset(target);
				feat.setnontargetset(nontarget);
				feat.setlocalset(rd.vTags);
				tfeat = feat.execute();
				
				//abltion_advfeat
				abltion_advfeat.reset();
				abltion_advfeat.setServerset(inserver);
				abltion_advfeat.settargetset(target);
				abltion_advfeat.setnontargetset(nontarget);
				abltion_advfeat.setlocalset(rd.vTags);
				tabltion_advfeat = abltion_advfeat.execute();
				
				//Adv_FEAT
//				advfeat.reset();
//				advfeat.setServerset(inserver);
//				advfeat.settargetset(target);
//				advfeat.setnontargetset(nontarget);
//				advfeat.setlocalset(rd.vTags);
//				tadvfeat = advfeat.execute();
				
				
				//veki
				veki.reset();
				veki.setServerset(inserver);
				veki.settargetset(target);
				veki.setnontargetset(nontarget);
				veki.setlocalset(rd.vTags);
				tveki = veki.execute();
//				
//				//erki
				erki.reset();
				erki.setServerset(inserver);
				erki.settargetset(target);
				erki.setnontargetset(nontarget);
				erki.setlocalset(rd.vTags);
				terki = erki.execute();
				
				//sfmti
				sfmti.reset();
				sfmti.setServerset(inserver);
				sfmti.setlocalset(rd.vTags);
				tsfmti = sfmti.execute();
				
				//IIP
				iip.reset();
				iip.setServerset(inserver);
				iip.setlocalset(rd.vTags);
				tiip = iip.execute();
				
				//CLS
				cls.reset();
				cls.setServerset(inserver);
				cls.setlocalset(rd.vTags);
				tcls = cls.execute();
				
				//cr_mti
				cr_mti.reset();
				cr_mti.setServerset(inserver);
				cr_mti.setlocalset(rd.vTags);
				tcr_mti = cr_mti.execute();
				
			
			sfmti.totalcollection += tsfmti;
			iip.totalcollection += tiip;
			cls.totalcollection += tcls;
			cr_mti.totalcollection += tcr_mti;
			
			veki.totalcollection += tveki;
			erki.totalcollection += terki;
			
			feat.totalcollection += tfeat;
			
			feat_mean.totalcollection += tfeat_mean;
			feat_min.totalcollection += tfeat_min;
			
			advfeat.totalcollection += tadvfeat;
			abltion_advfeat.totalcollection += tabltion_advfeat;
			
			pic.totalcollection += tpic;
			tic.totalcollection += ttic;
			etic.totalcollection += tetic;
				
			sfmti.logdata(sfmti, para);	
			iip.logdata(iip, para);	
			cls.logdata(cls, para);
			cr_mti.logdata(cr_mti, para);
			
			veki.logdata(veki, para);
			erki.logdata(erki, para);
			
			feat.logdata(feat, para);
			
			
			advfeat.logdata(advfeat, para);
				
//			pic.logdata(pic, para);
//			tic.logdata(tic, para);
//			etic.logdata(etic, para);
			
			
			abltion_advfeat.logdata(abltion_advfeat, para);
			//System.exit(0); 
			
		}		
	}
	
	
	public static HashSet<Tag> getGlobalSearchset(HashSet<Tag> insys, Params para){
//		HashSet<Tag> sset = TagGenerator.getnewTags(para.nSearchoutsys,para.length,para.width);
		HashSet<Tag> sset = TagGenerator.getTags(para.nSearchoutsys);
//		Utils.debug("searchset size is " + sset.size() + ", nsearch insys is " + para.nSearchinsys + "n out sys is " + para.nSearchoutsys);
		int added = 0;
		for(Tag t:insys){
			sset.add(t);
			added++;
			if(added == para.nSearchinsys)
				break;
		}
//		Utils.debug("searchset size is " + sset.size());
		return sset;
	}
	
	
	public static ReaderGenerator getReaders(Params para){
		ReaderGenerator rg = new ReaderGenerator(para.length,para.width,para.rcom,para.rinfer);
		rg.GenerateRdsRegular(); //���沿���Ķ���
		int colors = rg.getColorsMin(); 
		rg.colors = colors;//stores the number of rounds needed to schedule all the readers
		System.out.println("total " + rg.coverset.length + " readers generated");
		return rg;
	}
	
}
