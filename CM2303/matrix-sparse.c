/* simple example of sparse matrix addition
 *
 * Paul Rosin
 */

#define MAX 50
#define MAXINDEX 6

print(int a[MAX][3])
{
    int i, j;
    int b[MAXINDEX][MAXINDEX];
    int nr = a[0][0], nc = a[0][1], ne = a[0][2];

    for (i = 0; i < nr; i++) 
        for (j = 0; j < nc; j++)
            b[i][j] = 0;
    for (i = 1; i <= ne; i++)
        b[a[i][0]][a[i][1]] = a[i][2];

    for (i = 0; i < nr; i++) {
        for (j = 0; j < nc; j++)
            printf("%2d ",b[i][j]);
        printf("\n");
    }
}

add(int a[MAX][3], int b[MAX][3], int c[MAX][3])
{
    int i, j;
    int ar,ac,br,bc;
    int ca,cb,cc;

    ca = cb = cc = 1;
    while ((ca <= a[0][2]) && (cb <= b[0][2])) {
        ar = a[ca][0], ac = a[ca][1];
        br = b[cb][0], bc = b[cb][1];
        if ((ar < br) || ((ar == br) && (ac < bc))) {
            c[cc][0] = ar; c[cc][1] = ac;
            c[cc][2] = a[ca][2];
            ca++;
        }
        else if ((ar > br) || ((ar == br) && (ac > bc))) {
            c[cc][0] = br; c[cc][1] = bc;
            c[cc][2] = b[cb][2];
            cb++;
        }
        else {
            c[cc][0] = ar; c[cc][1] = ac;
            c[cc][2] = a[ca][2] + b[cb][2];
            ca++; cb++;
        }
        cc++;
    }
    for (i = ca; i <= a[0][2]; i++) {
        ar = a[ca][0], ac = a[ca][1];
        c[cc][0] = ar; c[cc][1] = ac;
        c[cc][2] = a[ca][2];
        ca++; cc++;
    }
    for (i = cb; i <= b[0][2]; i++)  {
        br = b[cb][0], bc = b[cb][1];
        c[cc][0] = br; c[cc][1] = bc;
        c[cc][2] = b[cb][2];
        cb++; cc++;
    }
    cc--;
    c[0][0] = a[0][0]; c[0][1] = a[0][1];
    c[0][2] = cc;
}

main()
{
    int i, j;
    int a[MAX][3],b[MAX][3],c[MAX][3];

    a[0][0] = 5; a[0][1] = 6; a[0][2] = 5;
    a[1][0] = 0; a[1][1] = 5; a[1][2] = 22;
    a[2][0] = 1; a[2][1] = 2; a[2][2] = 3;
    a[3][0] = 3; a[3][1] = 1; a[3][2] = 7;
    a[4][0] = 3; a[4][1] = 4; a[4][2] = 2;
    a[5][0] = 4; a[5][1] = 1; a[5][2] = 11;

    b[0][0] = 5; b[0][1] = 6; b[0][2] = 6;
    b[1][0] = 0; b[1][1] = 1; b[1][2] = 1;
    b[2][0] = 1; b[2][1] = 0; b[2][2] = 6;
    b[3][0] = 1; b[3][1] = 2; b[3][2] = 4;
    b[4][0] = 2; b[4][1] = 3; b[4][2] = 9;
    b[5][0] = 3; b[5][1] = 0; b[5][2] = 2;
    b[6][0] = 3; b[6][1] = 4; b[6][2] = 8;

    add(a,b,c);

    printf("a:\n");
    print(a);
    printf("b:\n");
    print(b);
    printf("c:\n");
    print(c);
}
