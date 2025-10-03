// set union of two ordered lists
// elements are stored in lists by adding them to the head
// but this reverses the inital element ordering...
// ...although later processing reverses them again!

public class Union2
{
    public static void main(String [] args)
    {
        IntNode next1,next2,list1,list2,list3;   
        int i1,i2;

        list1 = list2 = list3 = null;

        for (int i = 1; i < 16; i += 2)
            list1 = new IntNode(i,list1);

        list1.listPrint();

        for (int i = 4; i < 20; i += 3)
            list2 = new IntNode(i,list2);

        list2.listPrint();

        next1 = list1; next2 = list2;
        while ((next1 != null) && (next2 != null)) {
            i1 = next1.getData();
            i2 = next2.getData();
            System.out.println("processing: "+i1+"  "+i2);
            if (i1 > i2) {
                list3 = new IntNode(i1,list3);
                next1 = next1.getLink();
            }
            else if (i2 > i1) {
                list3 = new IntNode(i2,list3);
                next2 = next2.getLink();
            }
            else { // i.e. (i1 == i2)
                list3 = new IntNode(i1,list3);
                next1 = next1.getLink();
                next2 = next2.getLink();
            }
        }

        System.out.println("include remainder of list1");
        for (; next1 != null; next1 = next1.getLink()) {
            i1 = next1.getData();
            list3 = new IntNode(i1,list3);
            System.out.println("processing: "+i1);
        }

        System.out.println("include remainder of list2");
        for (; next2 != null; next2 = next2.getLink()) {
            i2 = next2.getData();
            list3 = new IntNode(i2,list3);
            System.out.println("processing: "+i2);
        }

        list3.listPrint();
    }
}
