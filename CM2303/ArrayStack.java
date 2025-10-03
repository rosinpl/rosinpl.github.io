public class ArrayStack implements Stack {
  // Implementation of the Stack interface using an array.

    public static final int CAPACITY = 1000; //# default capacity of the stack
    private int capacity;                    // maximum capacity of the stack.
    private Object S[];                      // S holds the elements of the stack
    private int top = -1;                    // the top element of the stack.
  
    public ArrayStack() {       //# Initialize the stack with default capacity
        this(CAPACITY);
    }

    public ArrayStack(int cap) {  //# Initialize the stack with given capacity
        capacity = cap;
        S = new Object[capacity];
    }

    public int size() {          //# Return the current stack size
        return (top + 1);
    }

    public boolean isEmpty() {   //# Return true iff the stack is empty
        return (top < 0);
    }

    public void push(Object obj) {  //# Push a new object on the stack 
        if (size() == capacity)
            throw new StackFullException("Stack overflow.");
        S[++top] = obj;
    }

    public Object top()            //# Return the top stack element
      throws StackEmptyException {
        if (isEmpty())
            throw new StackEmptyException("Stack is empty.");
        return S[top];
    }

    public Object pop()            //# Pop off the stack element
      throws StackEmptyException {
        Object elem;
        if (isEmpty())
            throw new StackEmptyException("Stack is Empty.");
        elem = S[top];
        S[top--] = null;               //# Dereference S[top] and decrement top
        return elem;
    }
}
