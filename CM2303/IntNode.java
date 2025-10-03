// adapted from Michael Main's colorado package
//
// Paul Rosin
// Cardiff University

public class IntNode
{
   private int data;
   private IntNode link;   

   // constructor for initialisation
   public IntNode(int initialData, IntNode initialLink)
   {
      data = initialData;
      link = initialLink;
   }

   // accessor method to get the data from this node
   public int getData()   
   {
      return data;
   }
   
   // accessor method to get a reference to the next node after this node
   public IntNode getLink()
   {
      return link;                                               
   } 
    
   // mutator method to set the data in this node
   public void setData(int newData)   
   {
      data = newData;
   }                                                               
   
   // mutator method to set the link to the next node after this node
   public void setLink(IntNode newLink)
   {                    
      link = newLink;
   }

   // add a new node after this node
   public void addNodeAfter(int item)   
   {
      link = new IntNode(item, link);
   }          
   
   // mutator method to remove the node after this node
   public void removeNodeAfter()   
   {
      link = link.link;
   }          
   
   // compute the number of nodes in a linked list
   // DON'T CALL WITH EMPTY LIST (i.e. null head reference)!
   public int listLength()
   {
      IntNode cursor;
      int answer;
      
      answer = 0;
      for (cursor = this; cursor != null; cursor = cursor.link)
         answer++;
        
      return answer;
   }
   
   // print the nodes in a linked list
   // DON'T CALL WITH EMPTY LIST (i.e. null head reference)!
   public void listPrint()
   {
      IntNode cursor;
      
      for (cursor = this; cursor != null; cursor = cursor.link)
         System.out.print(cursor.getData()+" ");
      System.out.println();
   }
   
   // search for an item of data
   // DON'T CALL WITH EMPTY LIST (i.e. null head reference)!
   public IntNode listSearch(int target)
   {
      IntNode cursor;
      
      for (cursor = this; cursor != null; cursor = cursor.link)
         if (target == cursor.data)
            return cursor;
        
      return null;
   }
}
