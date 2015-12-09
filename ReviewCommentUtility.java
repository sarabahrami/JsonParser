package wichita.edu.project.comment;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.json.JSONException;
/*
 * This class will return the the revision key for each review number and then finds the review comment(general, inline) for 
 * each revision. 
 */

import wichita.edu.codereview.comment.RevisionKeyReviewId;

public class ReviewCommentUtility {
	public   HashMap<String,ArrayList<String>> MapofRevisionKey=new HashMap<String,ArrayList<String>>();
	public  HashMap<String,ArrayList<String>> MapofFileComments=new HashMap<String,ArrayList<String>>();
	ArrayList<String> ReviewIdList=new ArrayList<String>();
	//public String Path="/media/extrav/Eclipse/";
	//public String urlpath1="https://git.eclipse.org/r/changes/?q=";
	//public String urlpath2="&o=ALL_REVISIONS&o=ALL_FILES&o=MESSAGES";
	public String Path;
	public String urlpath1;
	public String urlpath2;

	public ReviewCommentUtility(String Path, String urlpath1, String urlpath2)
	{
		this.Path=Path;
		this.urlpath1=urlpath1;
		this.urlpath2=urlpath2;
	}
	
	public void GeneratingReviewIds() throws JSONException, IOException
	{	
	ReviewIdList=ReturnReviewId();
	for(int j=0;j<ReviewIdList.size();j++)
	{
		String urlpath=urlpath1+ReviewIdList.get(j)+urlpath2;
		JsonArrayParserGeneral obj=new JsonArrayParserGeneral(urlpath);
		MapofRevisionKey.put(obj.getChangeId(), obj.getRevision());
		WriteinFilerevisionkey();
	}
	}
	
	public  ArrayList<String> ReturnReviewId() throws IOException
	{
		ArrayList<String> ReviewIdList=new ArrayList<String>();
		String strline="";
		BufferedReader reader=new BufferedReader(new FileReader(Path+"Eclipseallreviewnumbers"));
		while((strline=reader.readLine())!=null)
		{
			if(!(ReviewIdList.contains(strline)))
			{
				ReviewIdList.add(strline);
			}
		}
		reader.close();
		return ReviewIdList;
		
	}
	
	
	public  void WriteinFilerevisionkey() throws IOException
	{
		FileWriter fw1=new FileWriter(Path+"revisionkey.txt");
		for (Entry<String,ArrayList<String>> entry:MapofRevisionKey.entrySet())
		{
			fw1.write(entry.getKey()+"\t");
			for(String value:entry.getValue())
			{
				fw1.write(value+"\t");
			}
			fw1.write("\n");
		}
		fw1.flush();
		fw1.close();
	}
	
	
	public void RetrievinginlinComments() throws JSONException, IOException
	{
	FileWriter fw=new FileWriter(Path+"filecommentinline.txt");
	BufferedReader reader =new BufferedReader(new FileReader(Path+"revisionkey.txt"));
	String strline=null;
	while((strline=reader.readLine())!=null)
	{
		String strsplit[]=strline.split("\t");
		for(int i=1;i<strsplit.length;i++)
		{
			System.out.println(strsplit[0]);
			String urlpathcomment=urlpath1+strsplit[0]+"/revisions/"+strsplit[i]+"/comments";
			JsonArrayParserGeneral parser=new JsonArrayParserGeneral(urlpathcomment);
			parser.GetReviewComments();
			MapofFileComments=parser.returnfileComments();
			for(Entry<String,ArrayList<String>> entry1:MapofFileComments.entrySet())
			{
				if(entry1.getKey().endsWith("java"))
				{
					
					fw.write(entry1.getKey()+"\t");
					for (String value1: entry1.getValue())
					{
						fw.write(removeStopWordsAndStem(value1)+"\t");
						//count++;
					}
					fw.write("\n");
				}
				
			}
		}
			
	}
	fw.flush();
	fw.close();
	}
	
	
	public void RetrievingGeneralComments() throws JSONException, IOException
	{
	for(int j=0;j<ReviewIdList.size();j++)
	{
		String finalurlpath=urlpath1+ReviewIdList.get(j)+urlpath2;
		FilesinPatchplusGeneralComment(finalurlpath);
		System.out.println(ReviewIdList.get(j));
	}
	writeinFile();
	

	}
	


public  void FilesinPatchplusGeneralComment(String urlpath) throws JSONException
{
	ArrayList<ArrayList<String>> messages=new ArrayList<ArrayList<String>>();
	HashMap<Integer, String> fileVector= new HashMap<Integer, String>();
	String subject="";
	JsonArrayParserGeneral obj=new JsonArrayParserGeneral(urlpath);
	messages=obj.reviewMessageExtractor();
	fileVector=obj.reviewPatchFiles();
	subject=obj.getSubject();
	AddDatatoMapofFileComments(messages,fileVector,subject);
}

public  void AddDatatoMapofFileComments(ArrayList<ArrayList<String>> messages,HashMap<Integer, String> fileVector,String subject)
{
	//ArrayList<String> tempmessage=new ArrayList<String>();
	
	String temp;
	for (Entry<Integer,String> entry:fileVector.entrySet())
	{
		ArrayList<String>tempmessage=new ArrayList<String>();
		if(MapofFileComments.containsKey(entry.getValue()))
		{
			tempmessage=MapofFileComments.get(entry.getValue());
			//System.out.println(MapofFileComments.get(entry).size());
			//System.out.println(MapofFileComments.get(entry));
		}

		for(int i=0;i<messages.size();i++)
		{
			if((messages.get(i).get(0).length()>0) && !(messages.get(i).get(1).contains("Hudson CI")))
			//if((messages.get(i).get(0).length()>0))
			{
				//System.out.println(messages.get(i).get(0).replace("[\\r|\\n]",""));
				//System.out.println(messages.get(i).get(0).length());
				temp=FilterNoise(messages.get(i).get(0));
				if(temp.length()>0)
				{
					tempmessage.add(temp);
				}
				
				//System.out.println(entry.getValue());
			}
			
		}
		
		temp=FilterNoise(subject);
		tempmessage.add(temp);

		MapofFileComments.put(entry.getValue(), tempmessage);
	}
}

 public  String FilterNoise(String message)
{
	Pattern pattern =Pattern.compile("Change has been successfully merged into the git repository");
	Matcher matcher=pattern.matcher(message);
	if(matcher.find())
	{
		message="";
	}
	
	Pattern pattern1 =Pattern.compile("Looks good to me, approved; IP review completed");
	Matcher matcher1=pattern1.matcher(message);
	if(matcher1.find())
	{
		message="";
	}
	 
		Pattern pattern2 =Pattern.compile("I would prefer that you didn't submit this");
		Matcher matcher2=pattern2.matcher(message);
		if(matcher2.find())
		{
			message="";
		}
	
		Pattern pattern3 =Pattern.compile("([0-9]{5,6})");
		Matcher matcher3=pattern3.matcher(message);
		if(matcher3.find())
		{
			message=message.replaceAll("([0-9]{5,6})","");
		}
		Pattern pattern5 =Pattern.compile("([0-9]{1,2})");
		Matcher matcher5=pattern5.matcher(message);
		if(matcher5.find())
		{
			message=message.replaceAll("([0-9]{1,2})","");
		}
		
		Pattern pattern4 =Pattern.compile("https://bugs.eclipse.org/bugs/show_bug");
		Matcher matcher4=pattern4.matcher(message);
		if(matcher4.find())
		{
			message=message.replace("https://bugs.eclipse.org/bugs/show_bug.cgi?id","");
			//System.out.println("test");
		}
		
		
		return message;
	
}

public  void writeinFile() throws IOException
{
	FileWriter fw1=new FileWriter(Path+"fileGeneralcomment.txt");
	for (Entry<String,ArrayList<String>> entry:MapofFileComments.entrySet())
	{
		fw1.write(entry.getKey()+"\t");
		for(String value:entry.getValue())
		{
			fw1.write(removeStopWordsAndStem(value)+"\t");
			//fw1.write(value+"\t");
		}
		fw1.write("\n");
	}
	fw1.flush();
	fw1.close();
}




public  String removeStopWordsAndStem(String input) throws IOException 
{
	String[] stop_word={"Patch","Set","Uploaded","patch","set","comment","Abandoned", "1","2","3","4","5","6","comments","merged","Merged",
			"merge","inline","uploaded","Verified","Code-Review","+1","+2","Build", "Successful","uploaded","Abandon","abandon"};
	ArrayList<String> stopWords = new ArrayList<String>();
	for (int k=0;k<stop_word.length;k++)
		stopWords.add(stop_word[k]);
    TokenStream tokenStream = new StandardTokenizer(
            Version.LUCENE_40, new StringReader(input));
    tokenStream = new StopFilter(Version.LUCENE_36, tokenStream, StandardAnalyzer.STOP_WORDS_SET);
    tokenStream = new StopFilter(Version.LUCENE_36, tokenStream, StopFilter.makeStopSet(Version.LUCENE_40, stopWords));
    tokenStream = new PorterStemFilter(tokenStream);
    StringBuilder sb = new StringBuilder();
    CharTermAttribute token = tokenStream.getAttribute(CharTermAttribute.class);
    tokenStream.reset();
    while (tokenStream.incrementToken()) {
        if (sb.length() > 0) {
            sb.append(" ");
        }
        sb.append(token.toString());
    }
    tokenStream.end();
    tokenStream.close();
    return sb.toString();
}



}
