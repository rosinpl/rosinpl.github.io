SALIENT REGION DETECTION
========================

A very simple method for creating a salience map and detecting
salient regions by thresholding this salience map is provided.
Full details are given in:
    P.L. Rosin,
    "A Simple Method for Detecting Salient Regions",
    Pattern Recognition, vol. 42, no. 11, pp. 2363-2371, 2009.

March 2012 - This updated version includes options for erosion
of the salience mask with automatic disk size selection. This
is useful as otherwise the saliency mask tends to overestimate
the region size.

June 2015 - Updated to use colour images (but can still process graylevel)
          - Updated to weight saliency map using central prior 

The results for this method were published on the MIT Saliency Benchmark 
http://saliency.mit.edu/ and were achieved using the default parameter settings.

FORMATS
=======

Images are expected in PPM or PGM image format.


COMPILING AND RUNNING THE PROGRAM
=================================

Just type make to compile the programs

The following program files are included:
    salient_regions.c       salient region detection algorithm
    pgmio.h                 utility to read PGM image format
    ppmioNEW.h              utility to read PPM image format

Example:

    % salient_regions -i 19021.ppm -o overlay.pgm -m mask.pgm -s saliency.pgm

+--------------------------------------------------------------------+
| Dr. Paul Rosin                                                     |
| Cardiff School of Computer Science                                 |
| Cardiff University                          Paul.Rosin@cs.cf.ac.uk |
| 5 The Parade, Roath,             tel/fax: +44 (0)29 2087 5585/4598 |
| Cardiff, CF24 3AA, UK          http://users.cs.cf.ac.uk/Paul.Rosin |
+--------------------------------------------------------------------+
