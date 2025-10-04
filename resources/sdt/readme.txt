SALIENCE DISTANCE TRANSFORM
===========================

The distance transform (DT) labels each pixel with the distance to the closest
feature pixel. We have extended this with the salience distance transform
(Salience DT) by incorporating additional information reflecting the salience
of the feature pixels. Full details are given in:

    P.L. Rosin & G.A.W. West,
    "Salience distance transforms",
    CVGIP: Graphical Models and Image Processing,
    vol 57, no. 6, pp. 483-521, 1995.


FORMATS
=======

Images are expected in PGM image format.
 
The extracted edges are stored in a simple ASCII format. Each file starts
with "pixel". Each connected chain of edge pixels is preceded by "list: ID",
where ID is a unique integer for each edge chain, and is terminated by "-1 0",
except for the last list which is terminated by "-1 -1". Between the list
header and terminator the co-ordinates of each pixel are given.


COMPILING AND RUNNING THE PROGRAMS
==================================
 
Just type make to compile the programs

The following programs are included:
    chamfer34_s.c      the standard chamfer34 distance transform
    chamfer_sdt1.c     Salience DT incorporating edge strength
    chamfer_sdt2.c     Salience DT incorporating edge strength and curve length
    chamfer_sdt3.c     Salience DT incorporating edge strength, curve length,
                       and clutterness
    linknew.c          utility to extract curves from edge map

Examples:

To run the standard DT you need a binary image. For instance, you
could threshold the edge map of lena (program not supplied) so that edges
are black (0), and then:
    % chamfer34_s -i lena.threshold -o lena.dt

To run the Salience DT on the edge map of lena, incorporating edge strength:
    % chamfer_sdt1 -e lena.edge -o lena.sdt1

To run chamfer_sdt2 and chamfer_sdt3 you need to extract the edges as curves
using linknew:
    % linknew -i lena.edge -o lena.pix

Then, to run the Salience DT on the edge map of lena, incorporating edge
strength and curve length:
    % chamfer_sdt2 -e lena.edge -o lena.sdt2 -p lena.pix

Or, to run the Salience DT on the edge map of lena, incorporating edge, curve
length, and clutterness:
    % chamfer_sdt3 -e lena.edge -o lena.sdt3 -p lena.pix

+-------------------------------------------------------------------+
|Dr. Paul Rosin                                                     |
|Department of Computer Science    email:   Paul.Rosin@cs.cf.ac.uk  |
|Cardiff University                tel/fax: +44 (0)29 2087 5585/4598|
|Queen's Buildings, Newport Road, PO Box 916, Cardiff CF24 3XF, UK  |
|WWW: http://www.cs.cf.ac.uk/User/Paul.Rosin/                       |
+-------------------------------------------------------------------+
