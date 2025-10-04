import java.util.*;

class DualitySegmentSet extends DualityObjectSet
{
    public DualitySegmentSet(DualitySegment s, DrawingArea d)
    {
	super(d);
	dual = new DualityWedgeSet((DualityWedge)s.dual, d, this);
    }

    public void addSegment(DualitySegment s)
    {
	set.addElement(s);
	((DualityWedgeSet)dual).set.addElement(s.dual);
    }

    public String toString()
    {
	return super.toString("DualitySegmentSet");
    }
}
