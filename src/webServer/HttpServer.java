package webServer;   

	import java.net.Socket;

import java.io.OutputStream; 
import java.io.BufferedOutputStream;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

public class HttpServer  { 
	private Socket client;
	private HashMap<String,File> webroot;
	
	static final String FILENOTFOUND = "404.html";
	static final String METHODNOTSUP= "methodNotSupported.html";
	
	static final File WEBROOT = new File("WebROOT");
	static final int PORT = 8080;
	
	
	public HttpServer(ServerSocket s) throws IOException {
		getFiles(WEBROOT);
		
		init(s);
		
		
	}
	
	
	
	public static void main(String[] args) {
		
		
		try {
			
			System.out.println("Server listening on Port: " + PORT); 
			
			while(true) {
				ServerSocket server = new ServerSocket(PORT);
				HttpServer h = new HttpServer(server);
				
				
				server.close();
				System.out.println("Server Closed Connection to client");
			}
			
			
			 
		}catch(IOException e ) {
			e.printStackTrace();
		}finally {
			
		}
		
		
		
	}
	// gets the files within the webroot
	public void getFiles(File file) {
		webroot = new HashMap<String,File>();
		File[] fileArray = file.listFiles();
		
		for(File f : fileArray) {
			if(f.isDirectory()) {
				getFiles(f);
				
			}
			webroot.put(f.getName().toUpperCase(), f);
			
		}


	}
	
	public void init(ServerSocket s) throws IOException {
		
		
		
		
		BufferedOutputStream outPdata = null; 
		PrintWriter out = null; 
		try {
			client = s.accept();
			System.out.println("Connection established with client");
			
			
			
			
			 out = new PrintWriter(client.getOutputStream());
			
			// binary data from file 
			 outPdata = new BufferedOutputStream(client.getOutputStream());
			//get the request type we only support GET; 
			
			ArrayList<String> header = readHeader();
			 
			 String method = header.get(0);
			 String fileReq = header.get(1);
			 System.out.println(fileReq);
			 String content = "";
			 // if its not a GET or a Head Method 
			 
			 
			 switch(method) {
			 case "GET": 
				  File file;
				
					
					String contend = "";
					if(fileReq.equals("/")){
						contend = "text/html";
						
						fileReq += ".html";
						file = webroot.get("INDEX.HTML");
						 
					}else {
						file = webroot.get(fileReq.toUpperCase().replaceAll("/",""));
					}
					if(file == null) {
						throw new FileNotFoundException() ; 
					}
					
					contend = writeContend(fileReq);
					
					
					
					System.out.print(contend);
					
						int len = (int) file.length();
						byte[] byteData = readData(len,file);
						
						sendHeader(len, contend, 200, "OK");
						
						
						outPdata.write(byteData,0,len);
						outPdata.flush();
						
					
				 break;
				 
			 case "POST":
				 break;
				 
				 
				 
			 }
			 
			 
			 
			 if(!method.equals("GET") && !method.equals("HEAD")) {
				 File file  = new File(WEBROOT,FILENOTFOUND);
				 int len = (int) file.length();
				 byte[] datafromfile = readData((int) file.length(),file); 
				 
				 sendHeader(len, content,501,"Not Implemented");
				 
				 	
					
					
				 //binary data
				 outPdata.write(datafromfile, 0,len);
				 outPdata.flush();
				 
				 
			 }
				
			 
			
		
			
			
		} catch(IOException e ){
			e.printStackTrace();
			System.err.print("Server Error: 501");
		}
		
		
		finally{
			
				
				out.close();
				outPdata.close();
				client.close();
			
				
			}
		
			
			
			
			
		}
		
		

	// Reads the header from the client in order to get the request Method and the requested File
	private ArrayList<String> readHeader() {
		ArrayList<String> HEADER = new ArrayList<String>();
		BufferedReader in; 
		
		try {
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String input = in.readLine();
			StringTokenizer parse = new StringTokenizer(input);
			for(int i = 0; i < 2 ; i++) {
				HEADER.add(parse.nextToken().toUpperCase());
			}
			 System.out.println(input);
			
		}catch(IOException e ) {
			e.printStackTrace();
		}
		
		return HEADER; 
	}
	
	
	private void sendHeader(int len, String contend, int statusCode, String status) {
		PrintWriter out= null; 
		
		try {
			 out = new PrintWriter(client.getOutputStream());
			
			//HttpHeader gets Send here 
			out.println("HTTP/1.1 "+ statusCode +" "+ status);
			out.println("Server: Java HTTP Server from lmargale : 1.0");
			out.println("Date: " + new Date());
			out.println("Content-type: " + contend);
			out.println("Content-length: " + len);
			out.println();
			
			out.flush();
		}catch(IOException e ) {
			e.printStackTrace();
		}
	}
	
	
	private void fileNotFound(PrintWriter out, OutputStream dataOut) throws IOException {
		File file = new File(WEBROOT,FILENOTFOUND); 
		int len = (int) file.length();
		String content = "text/html"; 
		byte[] data = readData(len, file); 
		out.println("HTTP/1.1 404 File Not Found");
		out.println("Server: Java HTTP Server from lmargale : 1.0");
		out.println("Date: " + new Date());
		out.println("Content-type: " + content);
		out.println("Content-length: " + len);
		out.println();
		
		out.flush();
		
		
		
		
		dataOut.write(data,0,len);
		dataOut.flush();
		
		
	}
	
	
	
	private byte[] readData(int lenght, File file) throws IOException {
			byte[] data  = new byte[lenght];
			FileInputStream in = null; 
			try {
				 in = new FileInputStream(file);
				//input stream from file gets read and stored into byte array 
				in.read(data);
				
			}finally {
				
				if(in != null) {
					in.close();
				}
			}
			
		return data; 
	}
	
	
	
	private String writeContend(String contend) {
		String util ="";
		if(contend.endsWith(".html")) {
			
			util = "text/html";
		}
		
		if(contend.endsWith(".css")) {
			util = "text/css";
		}
		
		if(contend.endsWith("js")) {
			System.out.println("jsjsjs");
			util = "text/script";
		}
		
		
		
		
		
		return util;
	}
	
	

}
