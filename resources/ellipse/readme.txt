Fit an ellipse to data containing outliers using many different methods

most are based on:
   1/ doing lots of 5 point fits
   2/ choosing the best one
   3/ flagging the inliers
   4/ polishing (i.e. refitting to inliers using standard least squares algebraic distance criterion)

input format:  pixel list (integer or float format)
output format: superdata or Postscript

The methods are described in the following papers:

P.L. Rosin, "Further five-point fit ellipse fitting", Graphical Models and Image Processing, vol. 61, no. 5, pp. 245-259, 1999.
P.L. Rosin, "A note on the least squares fitting of ellipses", Pattern Recognition Letters, vol. 14, pp. 799-808, 1993.
P.L. Rosin, "Ellipse fitting using orthogonal hyperbolae and Stirling's oval", Graphical Models and Image Processing, vol. 60, no. 3, pp. 209-213, 1998.
P.L. Rosin, "Ellipse fitting by accumulating five-point fits", Pattern Recognition Letters, vol. 14, pp. 661-669, 1993.

methods based on the median of the parameters (options -i1 -> i6) are generally not as good as
methods based on the LMedS of some error (options -e1 -> -e6)
and methods -i5, -i6 (Hilbert scan, MVE) seem least reliable

-------------------------------------------------------------------------------------------------------

example of running the program:

fitellipse3 -e1 -p data1.pix output.eps

output to terminal:

number of ellipses fitted (reg. combs.; LMedS of ALGEBRAIC error): 13
total number of conics considered: 40
parameters: 152.092 240.882 76.272 23.076 -24.632
15 inlying points retained from 20
polishing fit
performing least square fit
fitted parameters: 150.180772 204.954542 108.198228 55.738987 -0.566828

output Postscript file: output.eps

further examples of results from running the program on the included data files are plotted in results.pdf

-------------------------------------------------------------------------------------------------------

Paul Rosin
Cardiff University
Paul.Rosin@cs.cf.ac.uk
October 1996 / April 2018
