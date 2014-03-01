//Danny Yu, dyu6@binghamton.edu

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
class TelnetServ {
    public static void main(String argv[]) throws Exception{
    	// Minimal error checking
    	if (argv.length != 1) {
    		System.out.println("Incorrect # of args.");
    		System.out.println("Correct useage: java TelnetServ <port#>");
    		System.exit(0);
    	}
    	
    	try {
	    	// Socket stuff
	    	int portNum = Integer.parseInt(argv[0]);
	    	ServerSocket listen = new ServerSocket(portNum);
	    	Socket conn = listen.accept(); 
	    	BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream())); 
	    	PrintWriter out = new PrintWriter(conn.getOutputStream(),true);
	    	
	    	// Keep track of current directory
	    	String currentDir = System.getProperty("user.dir");
	    	StringBuffer stringBuf = new StringBuffer(currentDir);
	    	
	    	// Handle input/output
	    	String inputLine;
	    	while ((inputLine = in.readLine()) != null) {
				String[] commandArgs = inputLine.split("\\s");
				// Make commands case-insensitive
				commandArgs[0].toLowerCase();
	
				if (commandArgs[0].equals("pwd")) {
					out.println(stringBuf.toString());
				}
				else if (commandArgs[0].equals("ls")) {
					listAllFiles(stringBuf.toString(), out);
				}
				else if (commandArgs[0].equals("cd")) {
					try {
						commandArgs[1] = removeTrailingSlash(commandArgs[1]);
						// Absolute path
						if (commandArgs[1].startsWith("/")) {
							if (checkIfPathExists(out, stringBuf, commandArgs[1])) {
								stringBuf = updateAbsPath(stringBuf, commandArgs[1]);
							}
						}
						// Relative path
						else if (!commandArgs[1].startsWith("/")){
							if (commandArgs[1].startsWith("..")) {
								if (checkIfPathExists(out, stringBuf, commandArgs[1])) {
									int indexOfSlash = stringBuf.lastIndexOf("/");
									stringBuf.delete(indexOfSlash, stringBuf.length());
								}
							}
							else if (commandArgs[1].startsWith(".")) {
								if (commandArgs[1].equals(".")) {
									// Do nothing because directory stays the same
								}
								else if (commandArgs[1].startsWith("./")) {
									if (checkIfPathExists(out, stringBuf, commandArgs[1].substring(2)))
									{
										stringBuf = updateRelPath(stringBuf, commandArgs[1].substring(2));
									}
								}
							}
							else {
								if (checkIfPathExists(out, stringBuf, commandArgs[1])) {
									stringBuf = updateRelPath(stringBuf, commandArgs[1]);
								}
							}
						}
					} catch (Exception e) {
						System.out.println(e);
					}
				}
				else if (commandArgs[0].equals("mkdir")) {
					makeDirectory(stringBuf.toString() + "/" + commandArgs[1], out);
				}
				else {
					out.println("Command not supported.");
					out.println("Possible commands are: ls, cd, pwd, mkdir, exit");
				}
	    	}
    	} catch(Exception e) {
    		System.out.println(e);
    		System.out.println("Correct useage: java TelnetServ <port#>");
    	}
    } 
    
    public static boolean checkIfPathExists(PrintWriter out, StringBuffer currDir, String path) {
    	boolean exists = true;
    	// Abs Path
    	if (path.startsWith("/")) {
    		if (!(new File(path)).exists()) {
    			out.println(path + " does not exist.");
    			exists = false;
    		}
    	}
    	// Relative Path
    	else {
    		if (!(new File(currDir + "/" + path)).exists()) {
    			out.println(path + " does not exist.");
    			exists = false;
    		}
    	}
    	return exists;
    }
    
    // Updates StringBuffer if it is an absolute path
    public static StringBuffer updateAbsPath(StringBuffer currDir, String path) {
    	StringBuffer newPath = currDir.replace(0, currDir.length(), path);
		return newPath;
    }
    
    // Updates StringBuffer if it is a relative path
    public static StringBuffer updateRelPath(StringBuffer currDir, String path) {
    	return currDir.append("/" + path);
    }
    
    // Removes any trailing /'s from arguments
    public static String removeTrailingSlash(String str) {
    	if (str.endsWith("/")) {
    		str = str.substring(0, str.length()-1);
    	}
    	return str;
    }
    
    // Creates directory
    public static void makeDirectory(String dirName, PrintWriter out) {
    	File directory = new File(dirName);
    	if (!directory.exists()) {
    		boolean result = directory.mkdir();
    		
    		if (!result) { out.println("Something went wrong :("); }
    	}
    	else { // Err handling
    		out.println(dirName + " exists.");
    	}
    }
    
    // List all files in a directory
    public static void listAllFiles(String currentDir, PrintWriter out) {
    	File folder = new File(currentDir);
    	File[] listOfFiles = folder.listFiles();
    	
    	for (int i=0; i< listOfFiles.length; i++) {
    		if (listOfFiles[i].isFile()) {
    			out.println(listOfFiles[i].getName());
    		}
    		else if (listOfFiles[i].isDirectory()) {
    			out.println(listOfFiles[i].getName());
    		}
    	}
    }
} 
	

