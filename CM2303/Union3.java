// union of two ordered lists
// elements are stored in lists by adding them to the tail
// head contains a dummy node rather than referring to the first node

public class Union3
{
    public static void main(String [] args)
    {
        IntNode tail,next1,next2,list1,list2,list3;   
        int i1,i2;

        tail = list1 = new IntNode(0,null);
        for (int i = 1; i < 16; i += 2) {
            tail.addNodeAfter(i);
            tail = tail.getLink();
        }

        list1.listPrint();

        tail = list2 = new IntNode(0,null);
        for (int i = 4; i < 20; i += 3) {
            tail.addNodeAfter(i);
            tail = tail.getLink();
        }

        list2.listPrint();

        tail = list3 = new IntNode(0,null);
        next1 = list1.getLink(); next2 = list2.getLink();
        while ((next1 != null) && (next2 != null)) {
            i1 = next1.getData();
            i2 = next2.getData();
            System.out.println("processing: "+i1+"  "+i2);
            if (i1 == i2) {
                tail.addNodeAfter(i1);
                tail = tail.getLink();
                next1 = next1.getLink();
                next2 = next2.getLink();
            }
            if (i1 < i2) {
                tail.addNodeAfter(i1);
                tail = tail.getLink();
                next1 = next1.getLink();
            }
            if (i2 < i1) {
                tail.addNodeAfter(i2);
                tail = tail.getLink();
                next2 = next2.getLink();
            }
        }

        System.out.println("include remainder of list1");
        for (; next1 != null; next1 = next1.getLink()) {
            i1 = next1.getData();
            tail.addNodeAfter(i1);
            tail = tail.getLink();
            System.out.println("processing: "+i1);
        }

        System.out.println("include remainder of list2");
        for (; next2 != null; next2 = next2.getLink()) {
            i2 = next2.getData();
            tail.addNodeAfter(i2);
            tail = tail.getLink();
            System.out.println("processing: "+i2);
        }

        list3.listPrint();
    }
}
