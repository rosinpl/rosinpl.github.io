SHAPE MEASURES
==============

Code is provided for computing convexity & rectilinearity of shapes.
Input is a boundary pixel list (see the examples for details of this format).
As well as computing and output the shape measure an optional output is the
shape rotated to minimise convexity or maximise rectilinearity.

Full details of the measures are given in:

J. Zunic and P.L. Rosin, "Rectilinearity measurements for polygons",
IEEE Trans. Pattern Analysis and Machine Intelligence,
vol. 25, no. 9, pp. 1193-1200, 2003 

J. Zunic and P.L. Rosin, "A New Convexity Measure for Polygons",
IEEE Transactions Pattern Analysis and Machine Intelligence,
vol. 26, no. 7, pp. 923-934, 2004.


COMPILING AND RUNNING THE PROGRAM
=================================
 
Just type "make all" to compile the programs.


Examples with expected outputs:

% convexity_zr -i elephant.pix 
0.499698
% convexity_zr -i man.pix
0.803647
% convexity_zr -i music.pix
0.390808

% rectilinearity_zr -i elephant.pix
0.102992
% rectilinearity_zr -i man.pix
0.241113
% rectilinearity_zr -i music.pix
0.720887

+--------------------------------------------------------------------+
| Dr. Paul Rosin                                                     |
| Cardiff School of Computer Science                                 |
| Cardiff University                          Paul.Rosin@cs.cf.ac.uk |
| 5 The Parade, Roath,             tel/fax: +44 (0)29 2087 5585/4598 |
| Cardiff, CF24 3AA, UK          http://users.cs.cf.ac.uk/Paul.Rosin |
+--------------------------------------------------------------------+
