//Danny Yu, dyu6@binghamton.edu

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
class TelnetCli { 
    public static void main(String argv[]) throws Exception {
    	// Minimal error checking
    	if (argv.length != 2) {
    		System.out.println("Incorrect # of args.");
    		System.out.println("Correct useage: java TelnetCli <server_domain> <server_port#>");
    		System.exit(0);
    	}

    	try {
	    	String serverDomain = argv[0];
	    	int serverPort = Integer.parseInt(argv[1]);
	    	
	    	// Socket stuff
	    	BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
	    	Socket sock = new Socket(serverDomain, serverPort); 
	
	    	PrintWriter out = new PrintWriter(sock.getOutputStream(),true);
	    	BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
	
	    	// Handle input/output
	    	String userInput;
	    	String serverInput;
	    	System.out.print("telnet> ");
	    	while ((userInput = console.readLine()) != null) {
	    		if (userInput.equals("exit")) {
	    			System.exit(0);
	    		}
	    		out.println(userInput);
	    		Thread.sleep(500);
	    		while (in.ready()) {
	    			if ((serverInput = in.readLine()) != null) {
	    				System.out.println(serverInput);
	    			}
	    		}
	
	    		System.out.print("telnet> ");
	    	}
    	} catch(Exception e) {
    		System.out.println(e);
    		System.out.println("Correct useage: java TelnetCli <server_domain> <server_port#>");
    	}
	}
}