package com.leeburch.flatten;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * this is basically a 2 pass process, it could be one but 2 pass provides much better status reporting and
 * in the end makes little difference in overall performance
 * 
 * First you recurse over the supplied folder(s) and gather a list of all the files as well as all unique directories
 * When recursing you will recurse either by alpha order or by date (actual recursion does not matter as containers do the sorting)
 * 
 *  You now have a set of files to be flattened as well as the set of directories containing them.  The containing folders can now be 
 *  aliased into a base36 (a-Z+0-9) to work with various OS special chars) of 6 char length, to perserve sorting (approx 2B item namespace)
 *  
 *  While gathering we note size, num of files and folders for progress reporting
 *  
 *  Now it is simply a matter of copying the file to a new alias in the flattened structure
 *  
 *  <dir alias>-<file alias>-<orig name>
 */
public class App 
{
    public static void main( String[] args )
    {
    	Option source   = Option.builder("source").argName( "directory" )
                .hasArgs()
                .required()
                .valueSeparator(',')
                .desc(  "root source directories to flatten, comma separated" )
                .build();

    	Option dest   = Option.builder("dest").argName( "directory" )
                .hasArg()
                .required()
                .desc(  "directory to write flattened files to" )
                .build();

    	Option include   = Option.builder("include").argName( "pattern" )
                .hasArg()
                .desc(  "case insensative regexp for files to include" )
                .build();

    	Option resize   = Option.builder("resize").argName( "resize" )
                .hasArg()
                .desc(  "target bounding resolution ex 1024x768, images maintain aspect ratio" )
                .build();

    	Option exclude   = Option.builder("exclude").argName( "pattern" )
                .hasArg()
                .desc(  "case insensative regexp for files to exclude" )
                .build();
    	
    	Option sort   = Option.builder("sort").argName( "order" )
                .hasArg()
                .desc(  "sort by name or date" )
                .build();

    	Option afterOpt   = Option.builder("after").argName( "after" )
                .hasArg()
                .desc(  "sort by name or date" )
                .build();

    	Option beforeOpt   = Option.builder("before").argName( "before" )
                .hasArg()
                .desc(  "sort by name or date" )
                .build();


    	Options options = new Options();
    	options.addOption(source);
    	options.addOption(dest);
    	options.addOption(sort);
    	options.addOption(include);
    	options.addOption(resize);
    	options.addOption(exclude);
    	options.addOption(afterOpt);
    	options.addOption(beforeOpt);
    	options.addOption("dryrun", false, "only display do not copy data");
    	options.addOption("norotate", false, "don't rotate images to proper orientation when resizing");
    	
    	int sizeX = 0, sizeY = 0;
    	
    	try {
        	CommandLineParser parser = new DefaultParser();
        	CommandLine cmd = parser.parse( options, args);        	
        	
        	if (cmd.hasOption("resize")) {
        		String resolutionString = cmd.getOptionValue("resize");
        		if (resolutionString.indexOf('x') == -1) {
        			sizeX = sizeY = 0;
        			System.out.println("invalid dimension string should be <horiz res>x<vert res> " + resolutionString);
        		} else {
        			int sepCharPos = resolutionString.indexOf('x');
        			sizeX = Integer.parseInt(resolutionString.substring(0, sepCharPos));
        			sizeY = Integer.parseInt(resolutionString.substring(sepCharPos+1));
        		}
        	}
        	SimpleDateFormat sdf = new SimpleDateFormat("M/d/Y");
        	Date before;
        	if (cmd.hasOption("before")) {
        		before = sdf.parse(cmd.getOptionValue("before"));
        	} else {
        		before = new Date(Long.MIN_VALUE);
        	}

        	Date after;
        	if (cmd.hasOption("after")) {
        		after = sdf.parse(cmd.getOptionValue("after"));
        	} else {
        		after = new Date(Long.MAX_VALUE);
        	}

        	
        	DirectoryFlattener df = new DirectoryFlattener(cmd.getOptionValues("source"), cmd.getOptionValue("dest"), cmd.hasOption("dryrun"), cmd.hasOption("norotate"), cmd.getOptionValue("include"), cmd.getOptionValue("exclude"), sizeX, sizeY, before, after);

        	df.run();
		} catch( ParseException exp ) {
			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "flatten", options );
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
        System.out.println("done");
    }
}
