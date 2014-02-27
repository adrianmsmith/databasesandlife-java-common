package com.databasesandlife.util.socialnetwork;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.http.HttpServletResponse;

public class OAuth2Request {

	public static String doGet(String urlString) throws SocialNetworkUnavailableException {
		try{
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
	        urlConnection.setRequestMethod("GET");
	        urlConnection.setDoInput(true);
	        urlConnection.setDoOutput(true);
	        urlConnection.setUseCaches(false);
	        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        urlConnection.setRequestProperty("Content-Length", ""+ urlString.length());
	        
	        if (urlConnection.getResponseCode() != HttpServletResponse.SC_OK) 
	            throw new SocialNetworkUnavailableException("Response code '" + urlConnection.getResponseCode() + " " +
	                    urlConnection.getResponseMessage() + "' returned from URL '" + url.toExternalForm() + "'");
	        
	        BufferedReader inStream = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
	        
	        StringBuilder sb = new StringBuilder();
	        String line = "";
	        while((line = inStream.readLine()) != null) {
	        	sb.append(line);
	        }
	        return sb.toString();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	
	public static String doPost(String urlString){
		try{
			URL url = new URL(urlString);
			URLConnection urlConnection = url.openConnection();
	        ((HttpURLConnection)urlConnection).setRequestMethod("POST");
	        urlConnection.setDoInput(true);
	        urlConnection.setDoOutput(true);
	        urlConnection.setUseCaches(false);
	        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        urlConnection.setRequestProperty("Content-Length", ""+ urlString.length());
	        
	        BufferedReader inStream = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
	        
	        StringBuilder sb = new StringBuilder();
	        String line = "";
	        while((line = inStream.readLine()) != null) {
	        	sb.append(line);
	        }
	        return sb.toString();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	
	public static String doPost(String urlString,String... payload){
		try{
			URL url = new URL(urlString);
			URLConnection urlConnection = url.openConnection();
	        ((HttpURLConnection)urlConnection).setRequestMethod("POST");
	        urlConnection.setDoInput(true);
	        urlConnection.setDoOutput(true);
	        urlConnection.setUseCaches(false);
	        urlConnection.setRequestProperty("Content-Type", "text/xml");
	        
	        StringBuilder sb = new StringBuilder();
	        for(String s :payload){
	        	sb.append(s);
	        }
	        urlConnection.setRequestProperty("Content-Length", "" +sb.toString().length());
	        
	        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
        	wr.write(sb.toString());
        	wr.flush();
	        wr.close();
	        
	        BufferedReader inStream = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
	        sb = new StringBuilder();
	        String line = "";
	        while((line = inStream.readLine()) != null) {
	        	sb.append(line);
	        }
	        inStream.close();
	        return sb.toString();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	
}
