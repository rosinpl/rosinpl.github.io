code for linearity
==================

Code is provided for computing and using a linearity shape measure for curves.

For more details see:
   Measuring Linearity of Open Planar Curve Segments
   Jovisa Zunic and Paul L. Rosin
   Image and Vision Computing
   vol. 29, no. 12, pp. 873-879, 2011.

Input is a boundary pixel list (see the examples for details of this format).

The following programs are included:

linearity.c        - compute linearity of open or closed curves
linearity_local.c  - compute mean linearity of sections of open curves
lines_dp.c         - polygonal approximation of open curves using linearity, etc.

-------------------------------------------------------

running the shape measure programs is done like this:

linearity hand.pix
linearity_local hand.pix 10
linearity_local hand.pix 100

the outputs generated should be like this:

linearity: 0.060237
linearity: 0.898745
linearity: 0.423960

where it can be seen that decreasing the window size for linearity_local.c
increases the linearity value since the curve is locally reasonably linear.

A polygonal approximation of the curve is generated like this:

lines_dp -i hand.pix -n 10 -o hand.poly

Note that the output format is different from the input format.
This program is rather slow since dynamic programming is used.

+----------------------------------------------------------------+
| Dr. Paul Rosin                                                 |
| School of Computer Science & Informatics                       |
| Cardiff University                      Paul.Rosin@cs.cf.ac.uk |
| 5 The Parade, Roath,         tel/fax: +44 (0)29 2087 5585/4598 |
| Cardiff, CF24 3AA, UK      http://users.cs.cf.ac.uk/Paul.Rosin |
+----------------------------------------------------------------+
