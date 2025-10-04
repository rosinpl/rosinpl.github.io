MDenoise.cpp: Feature-Preserving Mesh Denoising.
Copyright (C) 2007 Cardiff University, UK

Version: 1.0

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 59 Temple Place - Su

The copyright of triangle.h and triangle.c belong to Jonathan Richard Shewchuk
Further information about these two files see the files themselves.

Author: Xianfang Sun

usage: Mdenoise -i input_file [options]
      -e         Common Edge Type of Face Neighbourhood (Default: Common Vertex)
      -t float   Threshold (0,1), Default value: 0.4
      -n int     Number of Iterations for Normal updating, Default value: 20
      -v int     Number of Iterations for Vertex updating, Default value: 50
      -o char[]  Output file
      -a         Adds edges and vertices to generate high-quality triangle mesh.
                 Only function when the input is .xyz file.
      -z         Only z-direction position is updated.

Supported input type: .gts, .obj, .off, .ply, .ply2, .smf, .stl, .wrl, and .xyz
Supported output type: .obj, .off, .ply, .ply2, and .xyz
Default file extension: .off

Examples:
Mdenoise -i cylinderN02.ply2
Mdenoise -i cylinderN02.ply2 -n 5 -o cylinderDN
Mdenoise -i cylinderN02.ply2 -t 0.8 -e -v 20 -o cylinderDN.obj
Mdenoise -i FandiskNI02-05 -o FandiskDN.ply
Mdenoise -i Terrain.xyz -o TerrainP -n 1

Reference:
@article{SRML071,
   author = "Xianfang Sun and Paul L. Rosin and Ralph R. Martin and Frank C. Langbein",
   title = "Fast and effective feature-preserving mesh denoising",
   journal = "IEEE Transactions on Visualization and Computer Graphics",
   volume = "13",
   number = "5",
   pages  = "925--938",
   year = "2007",
}

To compile on unix platforms:
     g++ -o mdenoise mdenoise.cpp triangle.c
also lines 67 & 113 of mdenoise.cpp should be commented out on unix platforms
