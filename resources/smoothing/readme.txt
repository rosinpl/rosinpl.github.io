DESCRIPTION
===========

The program performs smoothing of curves. Appropriate levels of smoothing
are determined for dynamically determined sections of the curves.

Details are described in the paper
	"Non-parametric multiscale curve smoothing" by Paul L. Rosin in
	International J. of Pattern Recognition and Artificial Intelligence,
	vol. 8, no. 6, pp. 1381-1406, 1994.


DATA FORMAT
===========

Input curves are stored in a simple ASCII format. Each file starts
with "pixel". Each connected chain of edge pixels is preceded by "list: ID",
where ID is (usually but not necessarily) a unique integer for each curve,
and is terminated by "-1 0", except for the last list which is terminated
by "-1 -1". Between the list header and terminator the integer co-ordinates
of each pixel are given.

Output can either be:
(1) "pixel_float" which is identical to "pixel" except that the co-ordinates
are stored as floating point values,
(2) "pixel_curvature" which includes the curvature and a singular point
label with each co-ordinate and includes on the line following the
"list: ID" statement a "sigma: FLOAT" statement specifying the amount of
smoothing that was performed, or
(3) Postscript


EXAMPLES
========

A sample data file "lena.pix" is provided. To perform Gaussian smoothing
using the points of maximum deviation as breakpoints and outputing a
Postscript file do:

	best_smooth -i lena.pix -o f1 -b 1

To perform polynomial fitting using the midpoint as breakpoint, and outputing
in "pixel" format do:

	best_smooth -i lena.pix -o f2 -s 1 -f

---------------------------------------------------------------
Dr. Paul Rosin
Department of Computer Science & Information Systems
Brunel University
Kingston Lane                    email: Paul.Rosin@brunel.ac.uk
Uxbridge                         tel: +44-1895-274000 ext. 3632
Middlesex UB8 3PH                fax: +44-1895-251686
---------------------------------------------------------------
