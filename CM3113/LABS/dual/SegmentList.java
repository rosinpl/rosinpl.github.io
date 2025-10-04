class SegmentList 
{
    public SegmentList prev;
    public SegmentList next;
    public Segment s;

    public SegmentList(Segment s)
    {
	this.s = s;
	prev = next = null;
    }

    /* inserts b behind a, assumes b.prev == null && a.next == null*/
    public static SegmentList insert(SegmentList a, SegmentList b)
    {
	Util.assert(b.prev == null, "SegmentList::insert()");
	Util.assert(a.next == null, "SegmentList::insert() 2");
	    
	a.next = b;
	b.prev = a;
	return b;
    }
    
    public void remove()
    {
	if(prev != null) {
	    Util.assert(prev.next == this, "SegmentList::remove() next");
	    prev.next = next;
	}
	if(next != null) {
	    Util.assert(next.prev == this, "SegmentList::remove() prev");
	    next.prev = prev;
	}
    }

    public String toString()
    {
	return s.toString();
    }
}
