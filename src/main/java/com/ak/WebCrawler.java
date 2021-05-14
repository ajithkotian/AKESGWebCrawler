package com.ak;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class WebCrawler {

	private static final String PATH_PREFIX = "E:\\Ajith\\UBS\\ESG\\download\\";
	private static final Integer maxRetries = 5;
	private String url;
	private String folderChar;
	private String companyName;

	public WebCrawler(String url) {
		this.url = url;
		this.companyName = StringUtils.substringAfterLast(url, "/");
		this.folderChar = StringUtils.substring(companyName, 0, 1);

	}

	public static void main(String[] args) throws Exception {
		new WebCrawler("https://www.responsibilityreports.com/Company/caesars-entertainment-corporation")
				.crawlAndDownload();
		// String url = "https://www.responsibilityreports.com/Click/1961";
		// URL link =
		// Jsoup.connect(url).ignoreContentType(true).followRedirects(true).execute().url();
		// System.out.println(link.toString());

	}

	public void crawlAndDownload() throws IOException, Exception {
		print("Fetching %s %s %s...", url, folderChar, companyName);
		int retries = maxRetries;
		Document doc = null;
		while(retries >= 0)
		try{
			doc = Jsoup.connect(url).get();
			break;
		}catch(Exception e){
			System.out.println(" Exception :" + e.getLocalizedMessage());
			System.out.println("Retry count : " + retries);
			retries--;
		}
		Elements allLinks = doc.select("a[href]");

		Set<String> pdfSet = extractPdfLinks(allLinks);
		pdfSet.forEach(System.out::println);

		downloadPDFs(pdfSet);
	}

	public void downloadPDFs(Set<String> pdfSet) throws InterruptedException {
		for (String link : pdfSet) {
			String fileName = PATH_PREFIX + this.folderChar + File.separator + this.companyName + File.separator
					+ getFileName(link);
			System.out.println("Downloading : " + link + " to File : " + fileName);
			downloadPDF(link, fileName, maxRetries);
		}
	}

	public Set<String> extractPdfLinks(Elements links) throws IOException {
		String tempLink;
		Set<String> pdfSet = new HashSet<String>();
		for (Element link : links) {
			tempLink = link.attr("abs:href");
			updatePdfLinksAndREdirectedLinks(tempLink, pdfSet);
		}
		return pdfSet;
	}

	public void updatePdfLinksAndREdirectedLinks(String tempLink, Set<String> pdfSet) throws IOException {
		if (checkIfPdfLink(tempLink)) {
			tempLink = redirectedPdfLinks(tempLink, maxRetries);
			pdfSet.add(tempLink);
		}
	}

	public String redirectedPdfLinks(String tempLink, Integer maxRetries) throws IOException {
		while (maxRetries >= 0) {
			try {
				if (tempLink.contains("/Click/")) {
					System.out.print("Replacing : " + tempLink);
					tempLink = Jsoup.connect(tempLink).ignoreContentType(true).followRedirects(true).execute().url()
							.toString();
					System.out.print(" with : " + tempLink);
				}
				break;
			} catch (Exception e) {
				System.out.println(" Exception :" + e.getMessage());
				System.out.println("Retry count : " + maxRetries);
				maxRetries--;
			}
		}
		return tempLink;
	}

	private void print(String msg, Object... args) {
		System.out.println(String.format(msg, args));
	}

	private void downloadPDF(String url, String fileName, Integer maxRetries) throws InterruptedException {
		while (maxRetries >= 0) {
			try {
				FileUtils.copyURLToFile(new URL(url), new File(fileName), 1000, 1000);
				Thread.sleep(2000);  
				break;
			} catch (IOException ioe) {
				System.out.println("Exception :" + ioe.getMessage());
				System.out.println("Retry count : " + maxRetries);
				maxRetries--;
			}
		}

	}

	private Boolean checkIfPdfLink(String url) {
		return url.endsWith("pdf") || url.endsWith("PDF") || url.contains("/Click/");
	}

	private String getFileName(String url) {
		return StringUtils.substringAfterLast(url, "/");
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getFolderChar() {
		return folderChar;
	}

	public void setFolderChar(String folderChar) {
		this.folderChar = folderChar;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
}
