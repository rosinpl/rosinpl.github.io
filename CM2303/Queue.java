public interface Queue {
  // accessor methods
  public int size(); //# return the number of elements stored in the queue
  public boolean isEmpty(); //# test whether the queue is empty
  public Object front() //# return the front element of the queue
    throws QueueEmptyException; //# thrown if called on an empty queue
  // update methods
  public void enqueue (Object element) //# insert an element at the rear
    throws QueueFullException; //# thrown if queue full
  public Object dequeue() //# return and remove the front element
    throws QueueEmptyException; //# thrown if called on an empty queue
}
