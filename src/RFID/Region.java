package RFID;
/**
 * ���ÿ����()�����ٸ��Ķ������ǡ�sig 
 * @author Jiangjin
 *
 */
public class Region {
	String sig; //���û�б��Ķ�������Ϊ'x'
	public Region(){
		sig = null;
	}
	public void setSig(String psig){
		sig = psig;
	}
	public boolean isInSameSubelement(Region r1){
		if(sig.equals(r1.sig))
			return true;
		else
			return false;
	}
}
