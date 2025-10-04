CIRCULAR IMAGE THRESHOLDING
===========================

This code implements two versions of circular thresholding that
are based on minimising within class variance (as was done by Otsu
for linear thresholding). Our method is applicable to circular
data, e.g. hue, orientation, etc.

otsu_fast_circ.c can only thrsehold images into two classes, but
is very fast since the class size can be proven to be N/2 for N
intensity levels.

otsu_multilevel_circ.c is more general - it adapts Luessi &
Eichmann's SMAWK and dynamic programming approach to perform circular
thresholding for any number of classes (but is slower than
otsu_fast_circ.c for the 2 class case)

for details see
   Yukun Lai and Paul Rosin,
   Efficient Circular Thresholding,
   IEEE Trans. on Image Processing,
   vol. 23, no. 3, pp. 992-1001, 2014.


FORMATS
=======

Images are expected in PGM image format.


COMPILING AND RUNNING THE PROGRAMS
==================================
 
Just type make to compile the programs

Examples:

otsu_fast_circ bandleader-hue.pgm bandleader-hue-2.pgm
otsu_multilevel_circ bandleader-hue.pgm bandleader-hue-3.pgm

+==============================================+
    Prof. Paul Rosin
    School of Computer Science & Informatics
    Cardiff University
    Cardiff, CF24 3AA, UK
    http://users.cs.cf.ac.uk/Paul.Rosin
+==============================================+
