THRESHOLDING FOR CHANGE DETECTION
=================================

This code implements two methods for change detection. The input is an
unsigned difference image (e.g. two video frames, or one frame
subtracted from the estimated background) and this is thresholded
to produce a change map.

The two methods:
     1/ compute graph of Euler number versus threshold
        find corner in graph 
        i.e. point of maximum deviation from line between maximum peak and end of graph
     2/ maximise relative variance at different thresholds
        i.e. reduce randomness of spatial distribution
        computed over local windows
        i.e. Poisson distribution model

for details see
     P.L. Rosin,
     "Thresholding for Change Detection"
     Computer Vision and Image Understanding, vol. 86, no. 2, pp. 79-95, 2002. 
an updated version of
     P.L. Rosin
     "Thresholding for Change Detection"
     Int. Conf. Computer Vision, pp. 274-279, 1998 
which extends previous work in
     P.L. Rosin and T. Ellis
     "Image difference threshold strategies and shadow detection"
     British Machine Vision Conf., pp. 347-356 1995. 

This version of the code is substantially more efficient than the
original. For an image with N pixels and G gray levels the computation
time is improved from O(N x G) to O(N + G) .


FORMATS
=======

Images are expected in PGM image format.


COMPILING AND RUNNING THE PROGRAMS
==================================
 
Just type make to compile the programs

The following program files are included:
    motion_threshold.c    thresholding algorithm
    pgmio.h               utility to read PGM image format

Examples:

girl-diff.pgm is the image difference between girl1.pgm and girl8.pgm

motion_threshold -i girl-diff.pgm -o girl-Euler.pgm -e
motion_threshold -i girl-diff.pgm -o girl-Poisson.pgm -p

+==============================================+
    Prof. Paul Rosin
    School of Computer Science & Informatics
    Cardiff University
    Cardiff, CF24 3AA, UK
    http://users.cs.cf.ac.uk/Paul.Rosin
+==============================================+
