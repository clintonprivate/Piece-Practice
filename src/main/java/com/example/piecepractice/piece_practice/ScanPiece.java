/*
 * What this class is supposed to do is that its supposed to take a list of
 * BufferedImages and check them for specific images found in the res folder
 * to determine the challenges in the piece, in order for a different class
 * to generate general exercises that resemble the piece for the user to
 * practice using MIDI input and a piano to prepare for the piece they
 * wish to play.
 * 
 * What we're going to do first to be able to test quickly is that we'll
 * have a test image of the song Starsmitten by Lilypichu and we'll convert
 * this into a BufferedImage and check just that for indicators. We will
 * loop through the files in resources/images/indicators checking which
 * image (all the files there are images) can be found in only that one
 * page of Starsmitten. When this works successfully, we will connect it
 * to the DownloadPages class and have it loop through all pages of
 * Starsmitten, checking for its challenges instead of just that one page.
 */

package com.example.piecepractice.piece_practice;

import javax.imageio.ImageIO;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.*;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;

import nu.pattern.OpenCV;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;

public class ScanPiece {
	public static void main(String[] args) {
		// Test OpenCV
		OpenCV.loadLocally();
        Mat sheetMusic = loadImage("src/main/resources/images/starsmitten.png");
        Mat indicator = loadImage("src/main/resources/images/indicators/D.png");
        System.out.println(isSubImage2(indicator, sheetMusic));
        indicator = loadImage("src/main/resources/images/indicators/G.png");
        System.out.println(isSubImage2(indicator, sheetMusic));
        indicator = loadImage("src/main/resources/images/indicators/Eb.png");
        System.out.println(isSubImage2(indicator, sheetMusic));
	}
	
	@SuppressWarnings("deprecation")
	public static boolean isSubImage(Mat subImage, Mat mainImage) {
		// Initialize feature detectors and extractors
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        // Detect keypoints and compute descriptors for both images
        MatOfKeyPoint keypointsMain = new MatOfKeyPoint();
        MatOfKeyPoint keypointsSub = new MatOfKeyPoint();
        Mat descriptorsMain = new Mat();
        Mat descriptorsSub = new Mat();

        detector.detect(mainImage, keypointsMain);
        detector.detect(subImage, keypointsSub);
        extractor.compute(mainImage, keypointsMain, descriptorsMain);
        extractor.compute(subImage, keypointsSub, descriptorsSub);

        // Match descriptors using a matcher
        MatOfDMatch matches = new MatOfDMatch();
        matcher.match(descriptorsSub, descriptorsMain, matches);

        // Define a threshold for matching
        double thresholdDist = 50;

        // Check if there are enough good matches
        int goodMatches = 0;
        DMatch[] matchesArray = matches.toArray();
        for (DMatch match : matchesArray) {
            if (match.distance < thresholdDist) {
                goodMatches++;
            }
        }

        // Determine if the sub image is found in the main image based on good matches
        return goodMatches >= 10;
    }
	
	public static boolean isSubImage2(Mat subImage, Mat mainImage) {
		// Initialize feature detectors and extractors
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        // Detect keypoints and compute descriptors for both images
        MatOfKeyPoint keypointsMain = new MatOfKeyPoint();
        MatOfKeyPoint keypointsSub = new MatOfKeyPoint();
        Mat descriptorsMain = new Mat();
        Mat descriptorsSub = new Mat();

        detector.detect(mainImage, keypointsMain);
        detector.detect(subImage, keypointsSub);
        extractor.compute(mainImage, keypointsMain, descriptorsMain);
        extractor.compute(subImage, keypointsSub, descriptorsSub);

        // Match descriptors using a matcher
        MatOfDMatch matches = new MatOfDMatch();
        matcher.match(descriptorsSub, descriptorsMain, matches);

        // Define a threshold for matching
        double thresholdDist = 50;

        // Check if there are enough good matches
        int goodMatches = 0;
        DMatch[] matchesArray = matches.toArray();
        for (DMatch match : matchesArray) {
            if (match.distance < thresholdDist) {
                goodMatches++;
            }
        }

        // Determine if the sub image is found in the main image based on good matches
        return goodMatches >= 10;
    }
	
	public static Mat loadImage(String imagePath) {
	    Imgcodecs imageCodecs = new Imgcodecs();
	    return imageCodecs.imread(imagePath);
	}
}
