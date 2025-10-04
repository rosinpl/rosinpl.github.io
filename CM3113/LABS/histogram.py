# simple program to plot histogram of graylevel image

# import libraries
import numpy as np
import PIL
from PIL import Image
import matplotlib.pyplot as plt
import sys

if len(sys.argv) != 2:
    sys.exit("ERROR: provide single image filename on command line")

img = Image.open(sys.argv[1]);
histogram, bin_edges = np.histogram(img, bins=256, range=(0, 256))
plt.plot(bin_edges[0:-1], histogram)
plt.show()
