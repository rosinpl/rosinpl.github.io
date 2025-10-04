superellipse fitting
====================

3 different methods for superellipse fitting

input data is my pixel format

examples of calling the program are:

*** to use area of polygon and MBR for parameter estimation
se_epsilon mouse.pix mouse-results

*** to use intersection points of diagonals for parameter estimation
se_epsilon2 mouse.pix mouse-results2

*** to use intersection points of diagonals for initial parameter estimation followed by refinement
se_epsilon3 mouse.pix mouse-results3



for details see:

X. Zhang and P.L. Rosin, "Superellipse fitting to partial data", Pattern Recognition, vol. 36, no. 3, pp. 743-752, 2003

P.L. Rosin, "Fitting superellipses", IEEE Transactions Pattern Analysis and Machine Intelligence, vol.  22, no. 7, pp. 726-732, 2000.

-----------------------------------------
Paul Rosin
Cardiff University
