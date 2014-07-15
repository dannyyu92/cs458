import java.io.PrintWriter;
import java.security.MessageDigest;



public class PasswordHasher {
	public static void main(String argv[]) throws Exception {
		String AliceCard = "11111111";
		byte[] AlicePw = "1234".getBytes();

		String TomCard = "00000000";
		byte[] TomPw = "5678".getBytes();

		
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] AlicePwDigest = md.digest(AlicePw);
		byte[] TomPwDigest = md.digest(TomPw);
		System.out.println(AlicePwDigest);
		System.out.println(TomPwDigest);
		
		PrintWriter out = new PrintWriter("passwd");
		out.println(AliceCard + ":" + new String(AlicePwDigest));
		out.println(TomCard + ":" + new String(TomPwDigest));
		out.close();
	}
}
