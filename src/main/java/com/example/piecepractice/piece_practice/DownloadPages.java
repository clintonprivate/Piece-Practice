/*
 * The purpose of this class is to take the link to a piece of sheet music on Musescore.com
 * and "download" all the pages of the piece. It wouldn't really be downloading the piece
 * onto the user's computer, but rather it will read and remember the response to an
 * image as text and turn this into an Image object for the ScanPiece class to use and
 * as soon as the piece has been scanned the text and Image objects will be thrown away.
 * So to avoid saving things to the user's actual computer and deciding where to save it
 * and stuff like that we'll just get the information and hold onto it tempoarily until
 * we are done using it to scan the piece, then we'll throw it away.
 */

package com.example.piecepractice.piece_practice;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.*;
import org.apache.batik.transcoder.*;
import org.apache.batik.transcoder.image.PNGTranscoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class DownloadPages {
	public List<BufferedImage> downloadPages(String musescoreLink) {
		List<BufferedImage> pageImages = new ArrayList<>();
		List<String> pageLinks = extractPageLinks(musescoreLink);
		for (String link : pageLinks) {
			System.out.println(link);
			HttpURLConnection connection = null;
	        BufferedReader reader = null;

	        try {
	            URL url = new URL(link);
	            connection = (HttpURLConnection) url.openConnection();
	            connection.setRequestMethod("GET");
	            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	            String line;
	            StringBuilder response = new StringBuilder();
	            while ((line = reader.readLine()) != null) {
	                response.append(line);
	            }
	            String svgString = response.toString();
	            BufferedImage pageImage = convertSvgStringToImage(svgString);
	            pageImages.add(pageImage);
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (connection != null) {
	                connection.disconnect();
	            }
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
		}
		return pageImages;
	}

	private static BufferedImage convertSvgStringToImage(String svgString) {
        try {
            byte[] svgBytes = svgString.getBytes();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(svgBytes);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PNGTranscoder transcoder = new PNGTranscoder();
            TranscoderInput input = new TranscoderInput(inputStream);
            TranscoderOutput output = new TranscoderOutput(outputStream);
            transcoder.transcode(input, output);
            byte[] pngImageData = outputStream.toByteArray();
            ByteArrayInputStream imageInputStream = new ByteArrayInputStream(pngImageData);
            return javax.imageio.ImageIO.read(imageInputStream);

        } catch (IOException | TranscoderException e) {
            e.printStackTrace();
        }
        return null;
    }
	
	private static List<String> extractPageLinks(String musescoreLink) {
        WebDriver driver = new FirefoxDriver();
        
        // Open Musescore link
        driver.get(musescoreLink);
        
        // Wait for the accept terms and conditions to pop up then click the accept button
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.className("css-1ucyjdz")));
        button.click();
        
        // Loop through pages collecting their sheet music image links
        List<String> links = new ArrayList<>();
        List<WebElement> pages = driver.findElements(By.className("EEnGW"));
        for (WebElement page : pages) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true); document.getElementById('jmuse-scroller-component').scrollBy(0, 100);", page);
            wait.until(innerHTMLContains(page, "src="));
            List<WebElement> elements = driver.findElements(By.className("KfFlO"));
            WebElement pageImage = elements.get(elements.size() - 1);
            String srcValue = pageImage.getAttribute("src");
            links.add(srcValue);
        }
        
        // Close the browser
        driver.quit();
        
		return links;
	}

	public static ExpectedCondition<Boolean> innerHTMLContains(final WebElement element, final String text) {
        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                return element.getAttribute("innerHTML").contains(text);
            }
            
            @Override
            public String toString() {
                return String.format("inner HTML to contain '%s'", text);
            }
        };
    }
}
