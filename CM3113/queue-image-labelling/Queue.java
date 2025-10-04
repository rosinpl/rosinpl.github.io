public interface Queue
{
   public boolean isEmpty();
   public int getFrontElement();
   public int getRearElement();
   public void put(int theObject);
   public int remove();
}
