/* inverts an image */

#include <stdio.h>

#define MAX_SIZE 1000

unsigned char image[MAX_SIZE][MAX_SIZE];
int height,width,depth;

main(argc,argv)
int argc;
char **argv;
{
    int x,y;
    
    if (argc != 3) {
        printf("usage: %s input_image output_image\n",argv[0]);
        exit(-1);
    }

    read_pgm(image,argv[1],&width,&height,&depth);

    for (y = 0; y < height; y++)
        for (x = 0; x < width; x++)
            image[x][y] = 255 - image[x][y];

    write_pgm(image,argv[2],width,height);
}

read_pgm(image,filename,width,height,depth)
char filename[];
unsigned char image[MAX_SIZE][MAX_SIZE];
int *width,*height,*depth;
{
    FILE *fp_in;
    int x,y;
    char ch,str[1000];

    if ((fp_in=fopen(filename,"r")) == NULL) {
        fprintf(stderr,"cant open %s\n",filename);
        exit(-2);
    }

    /* skip image type and comments in header */
    fgets(str,255,fp_in);
    do
        fgets(str,255,fp_in);
    while (str[0] == '#');

    /* read image parameters */
    /* the first line has already been read */
    sscanf(str,"%d %d",width,height);
    fscanf(fp_in,"%d\n",depth);
    /* skip CR */
    /***
    getc(fp_in);
    ***/

    if ((*width > MAX_SIZE) || (*height > MAX_SIZE)) {
        fprintf(stderr,"ERROR: Maximum image size is %d x %d\n",
            MAX_SIZE,MAX_SIZE);
        exit(-1);
    }

    if (*depth != 255) {
        fprintf(stderr,"ERROR: depth = %d; (instead of 255)\n",*depth);
        exit(-1);
    }

    /***
    printf("image size: %d x %d\n",*width,*height);
    printf("reading image from %s\n",filename);
    ***/

    for (y = 0; y < *height; y++)
       for (x = 0; x < *width; x++)
          image[x][y] = getc(fp_in);

    /* check the image file was the correct size */
    if (feof(fp_in)) {
        /***
        fprintf(stderr,"ERROR: premature end of file\n");
        exit(-1);
        ***/
    }
    else
        if (getc(fp_in) != EOF) {
            fprintf(stderr,"ERROR: extra characters in file\n");
            exit(-1);
        }

    fclose(fp_in);
}

write_pgm(image,filename,width,height)
unsigned char image[MAX_SIZE][MAX_SIZE];
char *filename;
int width,height;
{
    FILE *fp_out;
    int x,y;

    if ((fp_out = fopen(filename,"w")) == NULL) {
        fprintf(stderr,"cant open %s\n",filename);
        exit(-1);
    }

    fprintf(fp_out,"P5\n");
    fprintf(fp_out,"#created by Paul Rosin\n");
    fprintf(fp_out,"%d %d\n",width,height);
    fprintf(fp_out,"255\n");

    /***
    printf("writing image to %s\n",filename);
    ***/

    for (y = 0; y < height; y++)
        for (x = 0; x < width; x++)
            putc(image[x][y],fp_out);
    fclose(fp_out);
}
