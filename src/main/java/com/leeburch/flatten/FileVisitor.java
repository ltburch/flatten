package com.leeburch.flatten;

import static java.nio.file.FileVisitResult.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;

public class FileVisitor
    extends SimpleFileVisitor<Path> {

	TreeMap<String, FlattenEntity> folders;
	TreeSet<FlattenEntity> files;
	Pattern include;
	Pattern exclude;
	Date before;
	Date after;
	
	// ex: 2006:04:09 01:38:26
	SimpleDateFormat exifDate = new SimpleDateFormat("y:M:d H:m:s");
	
	FileVisitor(TreeMap<String, FlattenEntity> folders, TreeSet<FlattenEntity> files, String include, String exclude, Date after, Date before) {
		this.before = before;
		this.after = after;
		this.folders = folders;
		this.files = files;
		if (include != null) {
			this.include = Pattern.compile(include, Pattern.CASE_INSENSITIVE); 
		}
		
		if (exclude != null) {
			this.exclude = Pattern.compile(exclude, Pattern.CASE_INSENSITIVE);
		}
	}

	Date createdDate(File f)  {
    	try {
	    	Metadata metadata = ImageMetadataReader.readMetadata(f);
	
	    	for (Directory metaDir : metadata.getDirectories()) {
		    	if (metaDir != null) {
			    	if (metaDir.containsTag(ExifDirectoryBase.TAG_DATETIME ) ) {
			    		String s = metaDir.getString(ExifDirectoryBase.TAG_DATETIME);
			    		return exifDate.parse(s);
			    	}
		    	}
	    		
	    	}
    	} catch (Exception e) { 
		}
		return null;
    }

	// Print information about
    // each type of file.
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
    	if (attr.isRegularFile()) {
    		String name = file.getFileName().toString();

    		Date creationTime = createdDate(file.toFile());
    		boolean addFile;
    		if (include != null) {
    			if (include.matcher(name).matches()) {
    				addFile = true;
    			} else {
    				addFile = false;
    			}
    		} else {
    			addFile = true;
    		}
    		
    		if (exclude != null) {
    			if (exclude.matcher(name).matches()) {
    				addFile = false;
    			} 
    		}
    		if ((creationTime != null) && (!creationTime.after(after) && !creationTime.before(before))) {
    			System.out.println(creationTime + " after " + after + " or before " + before);
    			addFile = false;
    		}
    		if (addFile) {
            	files.add(new FlattenEntity(file, creationTime));  
    		}
    	}
        return CONTINUE;
    }

    // Print each directory visited.
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr) {
   		String name = dir.toAbsolutePath().toString();
        FlattenEntity f = new FlattenEntity(name, new Date(attr.creationTime().toMillis()));    		
       	folders.put(name, f);  
    	
        return CONTINUE;
    }

    // If there is some error accessing
    // the file, let the user know.
    // If you don't override this method
    // and an error occurs, an IOException 
    // is thrown.
    @Override
    public FileVisitResult visitFileFailed(Path file,
                                       IOException exc) {
        System.err.println(exc);
        return CONTINUE;
    }
}