DESCRIPTION
===========

These programs perform automatic thresholding of thin connected edges.

Details are described in the paper:
     Svetha Venkatesh & Paul L. Rosin,
     Dynamic threshold determination by local and global edge evaluation,
     Graphical Models & Image Processing,
     Vol. 75, No. 2, pp. 146-160, 1995.

Some further work is described in:
	 Paul L. Rosin,
	 Edges: saliency measures and automatic thresholding
	 Machine Vision and Applications,
	 Vol. 9, pp. 139-159, 1997.


COMPILING AND RUNNING THE PROGRAMS
==================================

Running the program is a little clumsy as it actually has to be run twice,
the first time to collect statistics, and then the second time to actually
perform the thresholding. Therefore, to make life easier it has been
packaged into a C shell script.

To compile the programs just type "make".

To run it on the provided example image that has already been edge detected
with the Canny operator, do:
	link_dynamic.sh can3.canny result
The resulting file "result" should be the same as "can3.thresh".
For comparison the unthresholded edges are given in "can3.pix".


FORMATS
=======

Images are expected in PGM image format.

The extracted edges are stored in a simple ASCII format. Each file starts
with "pixel". Each connected chain of edge pixels is preceded by "list: ID",
where ID is a unique integer for each edge chain, and is terminated by "-1 0",
except for the last list which is terminated by "-1 -1". Between the list
header and terminator the co-ordinates of each pixel are given.

+-------------------------------------------------------------------+
|Dr. Paul Rosin                                                     |
|Department of Computer Science    email:   Paul.Rosin@cs.cf.ac.uk  |
|Cardiff University                tel/fax: +44 (0)29 2087 5585/4598|
|Queen's Buildings, Newport Road, PO Box 916, Cardiff CF24 3XF, UK  |
|WWW: http://www.cs.cf.ac.uk/User/Paul.Rosin/                       |
+-------------------------------------------------------------------+
