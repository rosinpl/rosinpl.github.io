In a recent paper ("Multi-scale representation and matching of curves using
codons", Paul Rosin, CVGIP: Graphical Models and Image Processing, Vol 55,
pp. 286-310, 1993) I describe how curves can be segmented and represented by
a set of labels which includes the codons defined by Hoffman & Richards plus
additional ones to handle open curves, straight sections, etc. To overcome
the problem of noise the curve is smoothed at its "natural" scales - i.e.
those that describe some qualitatively distinctive structures of the curve.
Codons at different scales are linked to form a hierarchy (the "codon-tree").
Codon models are then matched to curves by searching the codon-tree. To
facilitate matching the codon labels are augmented by various shape measures
(e.g. compactness, skew).

The following files are included
   xmcodon3.c       source code for smoothing & segmenting curves into codons
   xstuff.c         source code for X windows & Postscript output
   face_ef          sample curve
   face_pr          sample curve
   codon_model      sample model
   Makefile

An example of running the program with all the options would be
   xmcodon3 -i face_pr -t -m codon_model -p dumpfile
This will smooth & segment the curve in "face_pr", create and plot the
codon tree, find & plot the best match to the model in "codon_model", and
dump the display in Postscript format to "dumpfile".

The above code follows on from the previous code for natural scales already in
the archives which is effectively included in this second lot. I would have
preferred to use the output of the "natural" program as the input to this
program, but I never quite sorted out various bugs. I recently ported the
code from a Sun to an Alpha and various bugs manifested themselves in the
process. Hopefully most have been eliminated, but a few probably still lurk
around!

+------------------------------------------------------------------+
|Dr. Paul Rosin                                                    |
|Department of Computer Science    email:   Paul.Rosin@cs.cf.ac.uk |
|Cardiff University                tel/fax: +44(0)29 2087 5585/4598|
|Queen's Buildings, Newport Road, Cardiff CF24 3XF, UK             |
|WWW: http://www.cs.cf.ac.uk:8008/User/Paul.Rosin/                 |
+------------------------------------------------------------------+
