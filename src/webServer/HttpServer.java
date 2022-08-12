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

public class HttpServer { 
	private Socket client; 
	static final String DEFAULTFILE = "index.html";
	static final String FILENOTFOUND = "404.html";
	static final String METHODNOTSUP= "methodNotSupported.html";
	static final String SCRIPT = "script.js";
	static final File WEBROOT = new File("WebROOT");
	static final int PORT = 8080;
	
	
	public HttpServer(Socket s) {
		client = s; 
		
	}
	
	
	
	public static void main(String[] args) {
		
		
		try {
			
			System.out.println("Server listening on Port: " + PORT); 
			
			while(true) {
				ServerSocket server = new ServerSocket(PORT);
				HttpServer h = new HttpServer(server.accept());
				
				h.init();
				server.close();
				
			}
			
			
			 
		}catch(IOException e ) {
			e.printStackTrace();
		}finally {
			
		}
		
		
		
	}
	
	public void init() {
		BufferedOutputStream outPdata = null; 
		PrintWriter out = null; 
		try {
			System.out.println("Connection established with client");
			
			 out = new PrintWriter(client.getOutputStream());
			
			// binary data from file 
			 outPdata = new BufferedOutputStream(client.getOutputStream());
			//get the request type we only support GET; 
			
			ArrayList<String> header = readHeaderMethod();
			 
			 String method = header.get(0);
			 String fileReq = header.get(1);
			 String content = "";
			 // if its not a GET or a Head Method 
			 if(!method.equals("GET") && !method.equals("HEAD")) {
				 File file  = new File(WEBROOT,FILENOTFOUND);
				 int len = (int) file.length();
				 byte[] datafromfile = readData((int) file.length(),file); 
				 
				 sendHeader(len, content,501,"Not Implemented");
				 
				 	
					
					
				 //binary data
				 outPdata.write(datafromfile, 0,len);
				 outPdata.flush();
				 
				 
			 }else {
				 
				if(fileReq.endsWith("/")){
					fileReq += DEFAULTFILE; 
				}
				File file = new File(WEBROOT,fileReq);
				
				int len = (int) file.length();
				
				String contend = "";
				
				if(fileReq.endsWith("html") || fileReq.endsWith("htm")) {
					contend = "text/html";
				}else {
					contend = "text/plain";
				}
				
				System.out.print(contend);
				if(method.equals("GET")) {
					byte[] dataf = readData(len,file);
					
					sendHeader(len, contend, 200, "OK");
					
					
					outPdata.write(dataf,0,len);
					outPdata.flush();
					
				}
				
				
				 
			 } 
			 
			 
			
		}catch(FileNotFoundException e ) {
			try {
				fileNotFound(out,outPdata);
			}catch(IOException io ){
				io.printStackTrace();
			}
			
			
		} catch(IOException e ){
			e.printStackTrace();
			System.err.print("Server Error: 501");
		}
		
		
		finally{
			try {
				
				out.close();
				outPdata.close();
				client.close();
			}catch(IOException e ) {
				e.printStackTrace();
			}
		
			
			
			
			
		}
		
		
	}
	// Reads the header from the client in order to get the request Method and the requested File
	private ArrayList<String> readHeaderMethod() {
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
		}finally {
			
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
	
	
	
	

}
