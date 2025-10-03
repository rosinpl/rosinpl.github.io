public class LinkedQueue implements Queue {
    private Node head;          //# reference to the front node
    private Node tail;          //# reference to the rear node
    private int size;           //# number of elements in the queue
    
    public LinkedQueue() {     //# Initialize the queue
        head = tail = null;
        size = 0;
    }

    public int size() {              //# Returns the current queue size
        return size;
    }

    public boolean isEmpty() {       //# Returns true iff the queue is empty
        if (head == null)
            return true;
        return false;
    }

    //# Return the front element of the queue
    public Object front()      throws QueueEmptyException {
        if (isEmpty())
            throw new QueueEmptyException("Queue is empty.");
        return head.getElement();
    }

    //# Place a new object at the rear of the queue
    public void enqueue(Object obj) {
        Node node = new Node();
        node.setElement(obj);
        node.setNext(null);        // node will be new tail node
        if (size == 0)
            head = node;           // special case of a previously empty queue
        else
            tail.setNext(node);    // add node at the tail of the list
        tail = node;    // update the reference to the tail node
        size++;
    }

    //# Remove the first object from the queue
    public Object dequeue() throws QueueEmptyException {
        Object obj;
        if (size == 0)
            throw new QueueEmptyException("Queue is empty.");
        obj = head.getElement();
        head = head.getNext();
        size--;
        if (size == 0)
            tail = null;        // the queue is now empty
        return obj;
    }
}
