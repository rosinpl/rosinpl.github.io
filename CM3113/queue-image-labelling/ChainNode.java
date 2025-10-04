class ChainNode
{
   // package visible data members
   int element;
   ChainNode next;

   // package visible constructors
   ChainNode() {}
     
   ChainNode(int element)
      {this.element = element;}

   ChainNode(int element, ChainNode next)
      {this.element = element;
       this.next = next;}
}
