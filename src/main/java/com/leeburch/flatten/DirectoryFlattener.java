package com.leeburch.flatten;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JPanel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import javaxt.io.Image;

public class DirectoryFlattener extends JPanel {
	String[] source;
	String dest;
	boolean dryRun;
	boolean norotate;
	String include;
	String exclude;
	Dimension targetSize;
	Date before;
	Date after;
	
	TreeMap<String, FlattenEntity> folders;
	TreeSet<FlattenEntity> files;
	
	long totalBytes;
	long bytesFlattened;
	
	DirectoryFlattener(String[] source, String dest, boolean dryRun, boolean norotate, String include, String exclude, int targetX, int targetY, Date before, Date after) {
		this.source = source;
		this.dest = dest;
		this.dryRun = dryRun;
		this.norotate = norotate;
		this.exclude = exclude;
		this.include = include;
		this.before = before;
		this.after = after;
		
		if (targetX > 0 && targetY > 0) {
			this.targetSize = new Dimension(targetX, targetY);
		} else {
			this.targetSize = null;
		}
				
		files = new TreeSet<FlattenEntity>();
		folders = new TreeMap<String, FlattenEntity>();
	}
	
    private static final String CODE_BASE_36 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final Character ZERO = '0';

    public static String toBase36(Long numToConvert) {
        if (numToConvert < 0) {
            throw new NumberFormatException("the number you are trying to convert" + numToConvert + "is less than zero");
        }
        Long num = numToConvert;
        String text = StringUtils.EMPTY;
        int j = (int) Math.ceil(Math.log(num) / Math.log(CODE_BASE_36.length()));
        for (int i = 0; i < (j > 0 ? j : 1); i++) {
            text = CODE_BASE_36.charAt(Integer.parseInt(String.valueOf(num % CODE_BASE_36.length()))) + text;
            num /= CODE_BASE_36.length();
        }
        return text;
    }

    public static String toBase36(Long numToConvert, int length) {
       return StringUtils.leftPad(toBase36(numToConvert), length, ZERO);
    }
    
    
    void gather() throws IOException {

		for (String src: source) {
	    	Path startingDir = Paths.get(src);
			FileVisitor fileVisitor = new FileVisitor(folders, files, include, exclude, before, after);
			Files.walkFileTree(startingDir, fileVisitor);
		}
		
		long fileCount = 0;
		for (FlattenEntity f: files) {
			f.alias = DirectoryFlattener.toBase36(fileCount++, 6);
		}

		long folderCount = 0;
		for (FlattenEntity f: folders.values()) {
			f.alias = DirectoryFlattener.toBase36(folderCount++, 6);
		}
		
	}
    
    int requiredRotation(File f) throws ImageProcessingException, IOException, MetadataException {
    	try {
	    	Metadata metadata = ImageMetadataReader.readMetadata(f);
	
	    	for (Directory metaDir : metadata.getDirectories()) {
		    	if (metaDir != null) {
			    	if (metaDir.containsTag(ExifSubIFDDirectory.TAG_ORIENTATION) ) {
			    		return metaDir.getInt(ExifSubIFDDirectory.TAG_ORIENTATION);
			    	}
		    	}
	    		
	    	}
    	} catch (IOException | ImageProcessingException ioe) { 
    	}
		return 0;
    }

    void copy() throws Exception {
		
		long copied = 0;
		for (FlattenEntity f: files) {
			String folderAlias = folders.get(f.getPath() ).getAlias() ;
			String destFilePath = dest+ "/" + folderAlias + "-" + f.alias + "-" + f.getLastPathElement();
			File destFile = new File(destFilePath);
			File srcFile = new File(f.getName());
			// printOrientation(srcFile);
			double percent = (((double)copied) / files.size()) * 100;
			System.out.printf("%s => %s: %.2f%%%n", f.getName(), destFilePath, percent);
			if (!dryRun) {
			    if (targetSize != null) {  
			    	// catch resize errors rather than fail as there may be as few in a large collection
			    	try {
				    	int rotationRequired = requiredRotation(srcFile);
			    		Image orig = new javaxt.io.Image(srcFile.getAbsolutePath());
			    		if (rotationRequired != 1) { 
			    			int degrees = 0;
			    			switch (rotationRequired) {
			    				case 3:
			    				case 4:
			    					degrees = 180;
			    					break;
			    				case 5:
			    				case 6:
			    					degrees = 90;
			    					break;
			    				case 7:
			    				case 8:
			    					degrees = -90;
			    					break;
			    			}
			    			orig.rotate(degrees);
				    		if ((rotationRequired == 2) || (rotationRequired == 5) || (rotationRequired == 7) || (rotationRequired == 4)) {
				    			orig.flip();
				    		}
			    		}
			    		if ( (orig.getWidth() / targetSize.getWidth()) > (orig.getHeight() / targetSize.getHeight())) {
			    			orig.setWidth(targetSize.width);
			    		} else {
			    			orig.setHeight(targetSize.height);
			    		}			    		
			    		orig.saveAs(destFilePath);
			    		
			    	} catch (Exception e) {
			    		System.out.println("Error encountered resizing image " + f.getName());
			    	}
			    } else {
			    	// there is actually a reason to flip resized and not ones we copy.  I am hoping the exif on the file
			    	// will be honored by the destination while we loose exif during resize operations
			    	FileUtils.copyFile(srcFile, destFile, true);
			    }
			}
			copied++;
		}
		
	}
	
	void run() throws Exception {
		gather();
		copy();
	}
}
