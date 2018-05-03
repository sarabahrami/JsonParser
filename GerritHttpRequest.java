 /* 
  Copyright Motahareh Bahrami Zanjani <mxbahramizanjani@wichita.edu>

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Library General Public
 License as published by the Free Software Foundation; either
 version 2 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Library General Public License for more details.

 You should have received a copy of the GNU Library General Public
 License along with this program; if not, write to the Free
 Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

 Author: Sara Bahrami
	mxbahramizanjani@wichita.edu
Date: 05/03/2018
*/
	
package wichita.edu.project.comment;
import org.json.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.*;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class GerritHttpRequest {
		//MEMBER VARIABLES
		public JSONArray reviewDataArray = new JSONArray();
		public JSONObject reviewDataObject=new JSONObject(); 
		//CONSTRUCTORS
		public GerritHttpRequest(String url) throws JSONException 	
	
		{
		
		try {

			System.out.println(url);
			//sending an httprequest with GET method and get result by httpEntity
			 HttpClient httpclient = HttpClientBuilder.create().build();
	         HttpEntity httpEntity = null;
	         HttpResponse httpResponse = null;
	         HttpGet httpGet = new HttpGet(url);
	         httpResponse = httpclient.execute(httpGet);
	         httpEntity = httpResponse.getEntity();
	         String response = EntityUtils.toString(httpEntity);
	         System.out.println(response);
	         //if response be empty the length of response is 8.
	         if(response.length()>8)
	         {
	        	 //JSON response has )]} extra characters which should be remove.
	        	 response=response.replace(")]}'","");

	         
	        //response from this url:"https://git.eclipse.org/r/changes/?q=mylyn&o=ALL_REVISIONS&o=ALL_FILES&o=MESSAGES";
	         if(response.startsWith("\n["))
	         {
	        	 //declare JSONArray from response 
	        	 reviewDataArray=new JSONArray(response);
	        	 if(reviewDataArray.length()>1)
	        	 {
	        		//declare JSONObject from reviewDataArray
		        	 reviewDataObject=reviewDataArray.getJSONObject(1); 
	        	 }
	        	 else
	        	 {
	        		//declare JSONObject from reviewDataArray
		        	 reviewDataObject=reviewDataArray.getJSONObject(0); 
	        	 }
	        	 
	         }
	         //response from this url:""https://git.eclipse.org/r/changes/"+entry.getKey()+"/revisions/"+value+"/comments"
	         else if(response.startsWith("\n{"))
	         {
	        	 reviewDataObject=new JSONObject(response);
	        	// System.out.println(response.toString());
	         }
	         
	         

	         }
		
			
		} 
		
		catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		
	}
		//GET Method
		public JSONObject getjson()
		{
			return reviewDataObject;
		}
}
