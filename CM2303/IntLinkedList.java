// stores dummy head node, tail reference & item count

public class IntLinkedList
{
    private IntNode head;
    private IntNode tail;
    private int count;

    public IntLinkedList()
    {
        head = tail = new IntNode(0,null);
        count = 0;
    }

    public boolean isEmpty()
    {
        return (count == 0);
    }

    // add item to start of list
    public void addHead (int item)
    {
        IntNode tmp = new IntNode(item,head.getLink()); 
        head.setLink(tmp);
        count++;                      
        if (count == 1)
            tail = tmp;
    }

    // remove item from start of list
    public void removeHead()
    {
        if (count != 0) {
            IntNode tmp = head.getLink();
            head.setLink(tmp.getLink());
            count--;        
            if (count == 0)
                tail = head;
        }
    }

    // add item to end of list
    public void addTail (int item)
    {
        IntNode tmp = new IntNode(item, null);
        tail.setLink(tmp);
        tail = tmp;
        count++;                      
    }

    // return item from end of list
    public int getTail()
    {
        if (count > 0)
            return tail.getData();
        else
            throw new IllegalArgumentException("no tail value to get");
    }

    // add an item at a given position
    public void addAtPos(int item, int position)
    {
        IntNode cursor = head;

        if (position <= 0)
             throw new IllegalArgumentException("position is not positive");
        
        for (int i = 1; (i < position) && (cursor != null); i++)
           cursor = cursor.getLink();
  
        if (cursor == null)
           throw new IllegalArgumentException("position is too big");
  
        cursor.addNodeAfter(item);
    }

    // remove an item at a given position
    public void removeAtPos(int position)
    {
        IntNode cursor = head;
        
        if (position <= 0)
             throw new IllegalArgumentException("position is not positive");
        
        if (position >= count)
             throw new IllegalArgumentException("position is beyond the end of the list");

        for (int i = 1; (i < position) && (cursor != null); i++)
           cursor = cursor.getLink();
   
        if (cursor == null)
           throw new IllegalArgumentException("position is too big");
  
        cursor.removeNodeAfter();
    }

    // print the nodes in a linked list
    public void listPrint()
    {
        for (IntNode cursor = head.getLink(); cursor != null; cursor = cursor.getLink())
           System.out.print(cursor.getData()+" ");
        System.out.println();
    }

    public static void main(String [] args)
    {
        IntLinkedList list;

        list = new IntLinkedList();

        list.addHead(1);
        list.addHead(3);
        list.addHead(5);
        list.addHead(8);
        list.listPrint();
        System.out.println("tail "+list.getTail());

        list.removeHead();
        list.listPrint();
        System.out.println("tail "+list.getTail());

        list.addTail(777);
        list.addTail(888);
        list.addTail(999);
        list.listPrint();
        System.out.println("tail "+list.getTail());

        list.addAtPos(123,2);
        list.addAtPos(505,5);

        list.listPrint();

        list.removeAtPos(1);
        list.removeAtPos(4);
        list.listPrint();
    }
}
