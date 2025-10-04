code for disconnectedness
=========================

Code is provided for computing a measure of disconnectedness for a multi-component
binary shape.

For more details see:

   Jovisa Zunic, Paul L. Rosin, Vladimir Ilic
   Disconnectedness: A New Moment Invariant for Multi-Component Shapes
   Pattern Recognition, vol. 78, pp. 91-102, 2018.

Input is an image in PGM format containing a multi-component binary shape

-------------------------------------------------------

Examples of running the program:

% disconnectedness -i test1.pgm
4 components
disconnectedness 0.307086

% disconnectedness -i test2.pgm
4 components
disconnectedness 0.859644

% disconnectedness -i test3.pgm
4 components
disconnectedness 2.111249

% disconnectedness -i test4.pgm
25 components
disconnectedness 0.190629

---
Prof. Paul L. Rosin
School of Computer Science & Informatics
Cardiff University                      Paul.Rosin@cs.cf.ac.uk
5 The Parade, Roath,         tel/fax: +44 (0)29 2087 5585/4598
Cardiff, CF24 3AA, UK      http://users.cs.cf.ac.uk/Paul.Rosin
