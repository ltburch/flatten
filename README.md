This is sort of a single trick program developed specifically to overcome the limitations in software of digital photo frames.  Digital photo frames are great but their software tends to be pretty poor.  They are built with the idea you will explicitly be creating playlists manually and then playing one of those playlists.  Problem is, doesn't map to what I want to do with them.  I have a bunch of digital pictures, thousands nearing ten thousand and they are arranged hierarchically (family, friends, trips etc) and I wanted to just be able to point the frame to a SD card/USB and say “go” and it would iterate over them.  

Unfortunately the software doesn’t seem to do that.  Instead it wants the files in a single large flat directory, as if hierarchical iteration is too difficult for them.  So I created a program that can flatten a set of hierarchical picture folders, avoiding name clashes.

The other thing I wound up throwing in is some optional image resizing.  Most photo frames, even the big ones tend to be pretty low resolution by today's standards somewhere in the 1MP range.  Well of course the typical pictures usually range from about 12MP to 24MP (lucky DSLR users even more).  Copying them directly works, because fortunately the frames can resize down, however it takes a lot more time to copy a set of 12-24MP images to a SD card than a bunch of 1MP pictures, they also consume a great deal more space than necessary.  Do the flattener has the option to resize the images on the fly as it writes them to the destination, maintaining aspect ratio of course.  With this I can fir around 9,000 images in 2GB of storage rather than 30GB.

So build use instructions are simple, it is just Maven so you can run a Maven install and should resolve the lot.  You are going to want to have a Java 8, I specified this in the pom so it will tell you if you are not on a compatible version.

Useage/flags are

usage: flatten
 -dest <directory>     directory to write flattened files
 -dryrun               only display do not copy data
 -exclude <pattern>    case insensitive regexp for files to exclude
 -include <pattern>    case insensitive regexp for files to include
 -resize <resize>      target bounding resolution ex 1024x768, images
                       maintain aspect ratio
 -sort <order>         sort by name or date
 -source <directory>   root source directories to flatten, comma separated

