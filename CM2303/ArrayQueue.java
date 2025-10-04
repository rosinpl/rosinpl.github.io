public class ArrayQueue implements Queue {
  // Implementation of the Queue interface using an array.

    public static final int CAPACITY = 1000; //# default capacity of the queue
    private int capacity;                    // maximum capacity of the queue.
    private Object S[];                      // S holds the elements of the queue
    private int first = 0;                  // the first element of the queue.
    private int rear = 0;                   // the rear element of the queue.
  
    public ArrayQueue() {       //# Initialize the queue with default capacity
        this(CAPACITY);
    }

    public ArrayQueue(int cap) {  //# Initialize the queue with given capacity
        capacity = cap;
        S = new Object[capacity];
    }

    public int size() {          //# Return the current queue size
        return ((capacity - first + rear) % capacity);
    }

    public boolean isEmpty() {   //# Return true iff the queue is empty
        return (first == rear);
    }

    public Object front() //# Return the front element of the queue
      throws QueueEmptyException {
        if (isEmpty())
            throw new QueueEmptyException("Queue is Empty.");
        return S[first];
    }

    public void enqueue(Object element)   //# Insert an element at the rear
        throws QueueFullException {
        if (size() == (capacity - 1)) 
            throw new QueueFullException("Queue is Full.");
        S[rear] = element;
        rear = (rear + 1) % capacity;
    }

    public Object dequeue()            //# Return and remove the front element
      throws QueueEmptyException {
        Object elem;
        if (isEmpty())
            throw new QueueEmptyException("Queue is Empty.");
        elem = S[first];
        S[first] = null;               
        first = (first + 1) % capacity;
        return elem;
    }
}
