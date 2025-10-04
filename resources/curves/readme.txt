Source code for lines and arcs programs
=======================================

linknew.c    
    Reads in an image of edge points froma raw image
    and builts a text file of linked edges i.e. a list
    of connected edge points per connected list of edge pixels.
    This assumes that something like the Marr-Hildreth or Canny
    edge detectors have been used that produce one-pixel wide
    edges. There are a number of options with this code:
        -i filename
        -o filename
        -t threshold
    The -i and -o allow specification of input and output images
    respectively, -w and -h specify the width and height of the
    raw image (512x512 default), and -t specifies a threshold
    that encodes long strong edges, long weak edges, short
    strong edges but not short weak edges so removes much
    clutter from the edge image. A typical value for the
    threshold is 1000.
    
lines.c
    Takes the edge file produced by linknew and, using Lowe's
    significance measure, fits straight lines to the edges.
    Produces a file of "super" data in which the list of
    connected pixels is replaced by a list of lines with various
    parameters. There are a number of options:
        -i filename
        -o filename
        -I
        -3
        etc.
    The -I switch runs a second pass over the data to join
    together edges that are separated by the tree structure used
    (see paper). The -3 option saves intermediate pixel values
    in with the line data. These are required by the ellipse
    fitting stage when using ellin3 to give enough points to
    overdetermine the ellipse fit.
    
arcs.c
    Fit circular arcs to the edge file produced by linknew

ellipses.c
    Fit elliptical arcs to the edge file produced by linknew
    Kalman filter version

ellipses2.c
    Fit elliptical arcs to the edge file produced by linknew
    Bendtson's method

superellipses.c
    Fit superelliptical arcs to the edge file produced by linknew

arcline.c
    Fit circular arcs to some of the lines produced by line
    Produces a mixed line/arc representation

ellin3.c
    Takes the super data file from lines and attempts to replace
    sequences of lines by ellipses. Again see paper for details
    of algorithm. Has similar options to lines.
    
refine_ellipses.c
    Takes the line & ellipse super data file from ellin3 as well
    as the original edge file and refits the ellipses using all
    the available data, and corrects for bias too.

pix_normalise.c
    Takes a pixel list and normalises closed curves so that the
    starting point is furthest from the centroid

xplotdata.c 
    Plots various feature files to an X window on the screen.
    Has many options including ability to plot multiple files
    overlayed, a feature file over an image, crosses at end
    points, ellipses in bold etc. Run program with no options
    for details.
    
    Various other source code files are included in this tar
    file which are needed e.g. xplotlib.c which is a set of
    routines for simple use of X graphics for drawing. Used by
    xplotdata.c

pgm2raw
    C shell script to convert a PGM image to raw image format

---------------------------------------------------------------

For details of the curve approximation techniques see the paper:
    
    P. L. Rosin and G. A. W. West,
   "Non-Parametric Segmentation of Curves into Various Representations",
    IEEE Trans. PAMI, vol 17, pp. 1140-1153, 1995.

---------------------------------------------------------------
Dr. Paul Rosin
Department of Computer Science & Information Systems
Brunel University
Kingston Lane                    email: Paul.Rosin@brunel.ac.uk
Uxbridge                         tel: +44-1895-274000 ext. 3632
Middlesex UB8 3PH                fax: +44-1895-251686
---------------------------------------------------------------
replaced with versions updated (after porting to Linux, etc.)
the X stuff does not work 100%

Paul Rosin
Cardiff University
November 2004

modifications for ANSI C
Paul Rosin
Cardiff University
November 2023
---------------------------------------------------------------
