import java.io.PrintWriter;


public class BalanceGenerator {
	public static void main(String argv[]) throws Exception {
		String AliceCard = "11111111";
		String TomCard = "00000000";
		
		PrintWriter out = new PrintWriter("balance");
		out.println(AliceCard + ":1000:10000");
		out.println(TomCard + ":1000:10000");
		out.close();
	}
}
