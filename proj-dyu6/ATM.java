import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;


public class ATM {
    public static void main(String argv[]) throws Exception {
    	// Minimal error checking
    	if (argv.length != 2) {
    		System.out.println("Incorrect # of args: java ATM <server_domain> <server_port#>");
    		System.exit(0);
    	}

    	try {
	    	String serverDomain = argv[0];
	    	int serverPort = Integer.parseInt(argv[1]);
	    	
	    	// Socket stuff
	    	BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
	    	Socket sock = new Socket(serverDomain, serverPort); 
	
	    	//PrintWriter out = new PrintWriter(sock.getOutputStream(),true);
	    	//BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
	    	DataOutputStream out = new DataOutputStream(sock.getOutputStream());
	    	DataInputStream in = new DataInputStream(sock.getInputStream());
	    	
	    	// Load Public and Private Keys
	    	byte[] pubAkeyBytes = loadKey("pubA.key");
	    	byte[] privAkeyBytes = loadKey("privA.key");
	    	byte[] pubBkeyBytes = loadKey("pubB.key");
	    	//Key pubBkeySpec = new SecretKeySpec(pubBkey, "RSA");
	    	PublicKey pubBkey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubBkeyBytes));
    		
	    	// Generate DES Key
    		Key K = generateDesKey();
    		byte[] Kbytes = K.getEncoded();
    		
    		// E(Pub,K)
    		byte[] encryptedKey = encrypt("RSA", pubBkey, Kbytes);
    		// Send Key to Server
    		out.writeInt(encryptedKey.length);
    		out.write(encryptedKey);
    		out.flush();

    		// Handle input/output
	    	String userInput;
	    	String serverLine;
	    	System.out.println("Connected to bank.");
	    	
	    	while (true) {
	    		String cardNum = "";
	    		String password = "";

	    		// Get ATM Card #
	    		System.out.println("Please enter your ATM Card number:");
	    		System.out.print("> ");
	    		if ((userInput = console.readLine()) != null) {
	    			cardNum = userInput;
	    			if (cardNum.length() != 8) {
	    				System.out.println("Invalid card number. ");
	    				continue;
	    			}
		    		//System.out.println("Card Num:" + cardNum);
	    		}

	    		// Get Pin #
	    		System.out.println("Please enter your pin number");
	    		System.out.print("> ");
	    		if ((userInput = console.readLine()) != null) {
	    			password = userInput;
		    		//System.out.println("Pin Num:" + password);
	    		}

	    		// E(K, card||password)
	    		byte[] cardNumBytes = cardNum.getBytes();
	    		byte[] passwordBytes = password.getBytes();

	    		// Create byte[] of card||password size
	    		byte[] cardPwBytes = new byte[cardNumBytes.length + passwordBytes.length];
	    		System.arraycopy(cardNumBytes, 0, cardPwBytes, 0, cardNumBytes.length);
	    		System.arraycopy(passwordBytes, 0, cardPwBytes, cardNumBytes.length, passwordBytes.length);
	    		byte[] encryptedCardPw = encrypt("DES", K, cardPwBytes);
	    		
	    		// Send E(K, card||password)
	    		out.writeInt(encryptedCardPw.length);
	    		out.write(encryptedCardPw);
	    		out.flush();
	    		
	    		int length = in.readInt();
	    		byte[] passwordConfirmation = parseInput(length, in);
	    		//System.out.println("Passwd Confirmation: " + passwordConfirmation);
    			while (true) {
		    		if (new String(passwordConfirmation).equals("1")) {
	    				//System.out.println("Password correct.");
	    				System.out.println("Please choose an action: ");
	    				System.out.println("1. Withdraw\n2. Check account balance\n3. Quit");
	    				System.out.print("> ");
	    	    		if ((userInput = console.readLine()) != null) {
    	    				out.writeInt(userInput.length());
    	    	    		out.writeBytes(userInput);
    	    	    		out.flush();
		    				// Withdraw
	    	    			if (userInput.equals("1")) {
	    	    				System.out.println("1. Checkings acc\n2. Savings acc");
	    	    				System.out.print("> ");
	    	    	    		if ((userInput = console.readLine()) != null) {
	    	    	    			String userChoice;
	    	    	    			String amount;
	    	    	    			// Checkings
	    	    	    			String response;
	    	    	    			if (userInput.equals("1")) {
	    	    	    				userChoice = "0";
	    	    	    	    		out.writeInt(userChoice.length());
	    	    	    	    		out.writeBytes(userChoice);
	    	    	    	    		out.flush();
	    	    	    	    		System.out.println("How much would you like to withdraw from checking?");
	    	    	    	    		System.out.print("> ");
	    	    	    	    		if ((amount = console.readLine()) != null) {
		    	    	    	    		out.writeInt(amount.length());
		    	    	    	    		out.writeBytes(amount);
		    	    	    	    		out.flush();
		    	    	    	    		length = in.readInt();
		    	    	    	    		byte[] responseBytes = parseInput(length, in);
		    	    	    	    		response = new String(responseBytes);
		    	    	    	    		if (response.equals("1")) {
		    	    	    	    			System.out.println("Transaction successful.");
		    	    	    	    		}
		    	    	    	    		else {
		    	    	    	    			System.out.println("Transaction failed. Insufficient funds.");
		    	    	    	    		}
	    	    	    	    		}
	    	    	    			}
	    	    	    			// Savings
	    	    	    			else {
	    	    	    				userChoice = "1";
	    	    	    	    		out.writeInt(userChoice.length());
	    	    	    	    		out.writeBytes(userChoice);
	    	    	    	    		out.flush();
	    	    	    	    		System.out.println("How much would you like to withdraw from savings?");
	    	    	    	    		System.out.print("> ");
	    	    	    	    		if ((amount = console.readLine()) != null) {
		    	    	    	    		out.writeInt(amount.length());
		    	    	    	    		out.writeBytes(amount);
		    	    	    	    		out.flush();
		    	    	    	    		length = in.readInt();
		    	    	    	    		byte[] responseBytes = parseInput(length, in);
		    	    	    	    		response = new String(responseBytes);
		    	    	    	    		if (response.equals("1")) {
		    	    	    	    			System.out.println("Transaction successful.");
		    	    	    	    		}
		    	    	    	    		else {
		    	    	    	    			System.out.println("Transaction failed. Insufficient funds.");
		    	    	    	    		}
	    	    	    	    		}
	    	    	    			}
	    	    	    		}
	    	    			}
	    	    			// Check balance
	    	    			else if (userInput.equals("2")) {
	    	    				System.out.println("2");
	    	    	    		length = in.readInt();
	    	    	    		byte[] encryptedBalance = parseInput(length, in);
	    	    	    		byte[] balance = decrypt("DES", K, encryptedBalance);
	    	    	    		String[] balanceSplit = (new String(balance)).split(":");
	    	    	    		System.out.println("Checking: \t" + balanceSplit[1]);
	    	    	    		System.out.println("Saving: \t" + balanceSplit[2]);
	    	    			}
	    	    			else if (userInput.equals("3")) {
	    	    				System.out.println("Goodbye.");
	    	    				sock.close();
	    	    				System.exit(0);
	    	    			}
	    	    		}
	    			}
	    			else {
	    				System.out.println("Password incorrect.");
	    				break;
	    			}
    			}
	    	}
    	} catch(Exception e) {
    		System.out.println(e);
    		System.out.println("Correct useage: java atm <server_domain> <server_port#>");
    	}
	}
    
	public static byte[] parseInput(int length, DataInputStream in) throws IOException {
		byte[] message = new byte[length];
		if (length > 0) {
			in.readFully(message);
		}
		return message;
	}
	
	public static byte[] encrypt(String algo, Key key, byte[] data) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance(algo);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(data);
	}
	
	public static byte[] decrypt(String algo, Key key, byte[] data) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance(algo);
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(data);
	}
    
    public static Key generateDesKey() throws NoSuchAlgorithmException {
    	KeyGenerator gen = KeyGenerator.getInstance("DES");
    	return gen.generateKey();
    }
    
    /*
    public static String[] loadBalances(String cardNum) throws IOException {
    	BufferedReader br = new BufferedReader(new FileReader("balance"));
    	String line;
    	String[] splitLine;
    	String[] balances = new String[2];
    	while ((line = br.readLine()) != null) {
    	   splitLine = line.split(":");
    	   if (splitLine[0].equals(cardNum)) {
    		   balances[0] = splitLine[1];
    	   }
    	}
    	br.close();
    	//System.out.println(passwordMap.get("11111111"));
    	//System.out.println(passwordMap.get("00000000"));
    	return passwordMap;
    }
    */
    
    public static byte[] loadKey(String keyName) throws IOException {
    	FileInputStream keyfis = new FileInputStream(keyName);
    	byte[] encodedKey = new byte[keyfis.available()];  
    	keyfis.read(encodedKey);

    	keyfis.close();
    	return encodedKey;
    }
}