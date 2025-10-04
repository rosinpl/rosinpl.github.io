public interface Stack {
  // accessor methods
  public int size(); //# return the number of elements stored in the stack
  public boolean isEmpty(); //# test whether the stack is empty
  public Object top() //# return the top elemet
     throws StackEmptyException; //# thrown if called on an empty stack
  // update methods
  public void push (Object element); //# insert an element onto the stack
  public Object pop() //# return and remove the top element of the stack
     throws StackEmptyException; //# thrown if called on an empty stack
}
