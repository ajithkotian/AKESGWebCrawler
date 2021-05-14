package com.ak;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CrawlCompanyNames {

	private static final String BASE_URL = "https://www.responsibilityreports.com/Companies?a=";
	private static final String[] startChars = { "A"};
	private static Set<String> allCompanyLinks = new TreeSet<String>(); 

	public static void main(String[] args) throws Exception {
		for (String companyStartChar : startChars) {
			String url = BASE_URL + companyStartChar;
			Document doc = Jsoup.connect(url).get();

			Elements links = doc.select("a[href]");
			String tempLink;
			for (Element link : links) {	
				tempLink = link.attr("abs:href");
				allCompanyLinks.add(tempLink);
			}
			cleanCompaniesUrl();
			allCompanyLinks.forEach(System.out::println);
			
			for(String companyUrl : allCompanyLinks){
				System.out.println("Crawling " + companyUrl);
				new WebCrawler(companyUrl).crawlAndDownload();
				System.out.println("============================================================================================================================");
			}
		}
	}
	
	private static void cleanCompaniesUrl(){
		Set<String> tempCompanyLinks = new TreeSet<String>(allCompanyLinks);
		for(String url : tempCompanyLinks){
			if(!StringUtils.contains(url, "/Company/")){
				System.out.println("Removing : " + url);
				allCompanyLinks.remove(url);
			}
		}
	}
}
