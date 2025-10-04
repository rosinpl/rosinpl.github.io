CORNER PROPERTIES
=================

Most corner detectors just signal the presence of corners. But sometimes it
can be useful to know a bit more. My paper
	"Measuring corner properties",
	Paul L. Rosin,
	Computer Vision and Image Understanding,
	vol. 73, no. 2, pp. 291-307, 1999
builds on of my earlier work, and describes some relatively simple and
efficient techniques for measuring orientation, subtended angle, contrast,
and relative colour from previously identified corners.

	   
FORMATS
=======

Images are expected in PGM image format.
 

COMPILING AND RUNNING THE PROGRAMS
==================================
 
Just type make to compile the program

A couple of simple synthetic images each containing one triangle with
different contrast and orientation are provided to check the program.

To run the program:

corner-p -i im1.pgm -o im1-crns.pgm -O properties1.txt -t 2000 -e 50

This should find 3 corners and overlay them in the image and save them in
the file im1-crns. The text file properties1.txt lists the corners and their
properties.

+==============================================+
    Prof. Paul Rosin
    School of Computer Science & Informatics
    Cardiff University
    Cardiff, CF24 3AA, UK
    http://users.cs.cf.ac.uk/Paul.Rosin
+==============================================+
