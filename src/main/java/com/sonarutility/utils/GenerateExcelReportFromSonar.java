package com.sonarutility.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.sonar.wsclient.internal.HttpRequestFactory;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.issue.IssueClient;
import org.sonar.wsclient.issue.IssueQuery;
import org.sonar.wsclient.issue.Issues;
import org.sonar.wsclient.issue.internal.DefaultIssueClient;

/**
 * ToDO :  13/10/2016
 * 
 * 		  1) Display all the Issues in SonarList : Done
 * 		  2) Display Issues in Separate List. 100 Issues per List : Done
 *        3) Pick SonarURL from Config Propertiesq
 *        4) Upload to SVN : Done
 *         	
 *  @author sachin
 * 
 * 
 *
 */

public class GenerateExcelReportFromSonar {
	
	static HSSFWorkbook workbook 			 = new HSSFWorkbook();
	static DateFormat dateFormat 			 = new SimpleDateFormat("yyyyMMddHHmmss");
	static Date date 						 = new Date();
	static String dateNow 					 = null;
	static String filename 					 = null;
	static Properties properties 			 =  new Properties();
	static InputStream input				 = null;
	static String configFileName			 = "config.properties";
	static String login 					 = null; //"admin";
	static String password 					 = null; //"admin";
	static StringBuffer sonarHostUrl = null; //new StringBuffer("http://192.168.0.228:9000/sonar");
	
	//TODO: read Severity
	//ArrayList list = null;
	
	public GenerateExcelReportFromSonar () throws IOException {
		
		
		
	}
	public void loadProperties() throws IOException  {		
					
		input =  GenerateExcelReportFromSonar.class.getClassLoader().getResourceAsStream(File.separator + configFileName);  
		
		if(input!=null) {
			properties.load(input);			
		} else  {
			System.out.println(" Sorry, Unable to find file : " + configFileName);
			System.exit(0);
		}
		try {
		
			sonarHostUrl = new StringBuffer(properties.getProperty("url"));
			login 		 = properties.getProperty("login");
			password     = properties.getProperty("password");			
			System.out.println("*** Found Configurations : " + sonarHostUrl + " " + login + " " + password);
					
		} finally{
			
        	if(input!=null){
        		try {
        			input.close();
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
        	
        	}
        }	
	}
	
	
	
		
	public static void main(String args[]) throws IOException {
		GenerateExcelReportFromSonar app = new GenerateExcelReportFromSonar();	
		app.loadProperties();
		app.connectToSonar();		
		
			
		
	}
	
	private void connectToSonar() throws IOException {
		
	
		
		if (sonarHostUrl != null && sonarHostUrl.toString().endsWith("/")) { 
			 	sonarHostUrl.append(sonarHostUrl.toString().substring(0,sonarHostUrl.toString().length() - 1)); 
		  
		} 
		
		HttpRequestFactory requestFactory = new HttpRequestFactory(sonarHostUrl.toString()) 
				 	.setLogin(login).setPassword(password); 
	  
		IssueClient client = new DefaultIssueClient(requestFactory);
	
		 //Display 100 Issues per sheet
		int pageIndex = 0;		
		List<Issue> issueList = null;
		
		Issues  issues = client.find(IssueQuery.create().severities("BLOCKER","CRITICAL","MAJOR"));
		 
		 System.out.println(" TOTAL : " + issues.paging().total());
		 System.out.println(" Page Size : " + issues.paging().pageSize());
		 
		 int totalPages = issues.paging().total() / issues.paging().pageSize();
		 System.out.println(" Total Pages : " + totalPages);		 
		 
		for(int i = 0 ; i <totalPages; i++) {
			pageIndex = pageIndex + 1;
			issueList = getIssues(client,pageIndex);	
			System.out.println("*** Extracting Sonar Reports to Excel ***");
			if(!issueList.isEmpty()) {
				createExcel(issueList,pageIndex);
			}	
			
		}
			
	
		
		
		
	}
	
	private static List<Issue> getIssues(IssueClient client,int pageIndex) throws IOException { 
		
		 Issues result = client.find(IssueQuery.create().severities("BLOCKER","CRITICAL","MAJOR").pageIndex(pageIndex));
		  
		 List<Issue> issues = result.list(); 
		 return issues; 
	} 
		 
	

	//Create Sheets instead of diffrent Excel Files
	private static void createExcel(List<Issue> issueList, int wb) {
		// TODO Auto-generated method stub
	
		try {
			
				
			System.out.println(" Total Issues Found : " + issueList.size()); // First 100 issues			
			String sheetName = "Sheet-"+wb;	
	
			//Main Outer Loop to create Sheets
			 HSSFSheet sheet = workbook.createSheet(sheetName);
			 HSSFRow rowhead = sheet.createRow((short) 0);
			 
			 rowhead.createCell(0).setCellValue("No");
			 rowhead.createCell(1).setCellValue("Project Key");				     	
		     rowhead.createCell(2).setCellValue("Component");
		     rowhead.createCell(3).setCellValue("Line");
		     rowhead.createCell(4).setCellValue("Rule Key");
		     rowhead.createCell(5).setCellValue("Severity");
		     rowhead.createCell(6).setCellValue("Message");
		     rowhead.createCell(7).setCellValue("Author");
		     rowhead.createCell(8).setCellValue("Action Plan");
		     rowhead.createCell(9).setCellValue("Debt");
	                
		     for (int i = 0; i < issueList.size(); i++) { //First 100 Issues
		    	 HSSFRow row = sheet.createRow((short) i + 1);
		    	 row.createCell(0).setCellValue(i);
		    	 row.createCell(1).setCellValue(issueList.get(i).projectKey());
		    	 row.createCell(2).setCellValue(issueList.get(i).componentKey());
		    	 row.createCell(3).setCellValue(String.valueOf(issueList.get(i).line()));
		    	 row.createCell(4).setCellValue(issueList.get(i).ruleKey());
		    	 row.createCell(5).setCellValue(issueList.get(i).severity()); 
		    	 row.createCell(6).setCellValue(issueList.get(i).message());
		    	 row.createCell(7).setCellValue(issueList.get(i).author());
		    	 row.createCell(8).setCellValue(issueList.get(i).actionPlan());
		    	 row.createCell(9).setCellValue(issueList.get(i).debt());
		    	 
		    	 
		     }	
		    
		 	dateNow = dateFormat.format(date);
			filename = "vbroker-serviceSonarIssues"+dateNow+".xls";			
		    FileOutputStream fileOut = new FileOutputStream(filename);
			workbook.write(fileOut);			
			fileOut.close();
			System.out.println("Your excel file has been generated to : " + filename);
			 

		} catch (Exception ex) {
			System.out.println(ex);

		}
	}
	
	

}
