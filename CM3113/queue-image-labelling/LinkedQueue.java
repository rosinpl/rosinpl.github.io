

/** a linked queue class */

public class LinkedQueue implements Queue
{
   // data members
   protected ChainNode front;
   protected ChainNode rear;

   // constructors
   /** create an empty queue */
   public LinkedQueue(int initialCapacity)
   {
       // the default initial value of front is null
   }

   public LinkedQueue()
   {
      this(0);
   }

   // methods
   /** @return true iff queue is empty */
   public boolean isEmpty()
   {
       return front == null;
   }


   /** @return the element at the front of the queue
     * @return null if the queue is empty */
   public int getFrontElement()
   {
      return front.element;
   }

   /** @return the element at the rear of the queue
     * @return null if the queue is empty */
   public int getRearElement()
   {
      return rear.element;
   }

   /** insert theElement at the rear of the queue */
   public void put(int theElement)
   {
      // create a node for theElement
      ChainNode p = new ChainNode(theElement, null);

      // append p to the chain
      if (front == null) front = p;   // empty queue
      else rear.next = p;                // nonempty queue
      rear = p;
   }

   /** remove an element from the front of the queue
     * @return removed element
     * @return null if the queue is empty */
   public int remove()
   {
      int frontElement = front.element;
      front = front.next;
      return frontElement;
   }
}
