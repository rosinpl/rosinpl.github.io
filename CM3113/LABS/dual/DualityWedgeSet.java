import java.util.*;

class DualityWedgeSet extends DualityObjectSet
{
    public DualityWedgeSet(DualityWedge s, DrawingArea d, DualitySegmentSet dual)
    {
	super(d);
	set.addElement(s);
	this.dual = dual;
    }

    public String toString()
    {
	return super.toString("DualityWedgeSet");
    }

    private Vector queryResult = new Vector();

    public Vector query(Vector dualityObjects)
    {
	queryResult.removeAllElements();
	for(int i = 0; i < dualityObjects.size(); i++) {
	    DualityObject obj = (DualityObject) dualityObjects.elementAt(i);
	    DualityVertex v = null;

	    if(obj instanceof DualityVertex) {
		v = (DualityVertex) obj;
		boolean add = true;
		for(int j = 0; j < set.size(); j++) {
		    if(!((DualityWedge)set.elementAt(j)).contains(v.p)) {
			add = false;
			break;
		    }
		}
		if(add)
		    queryResult.add(v);
	    }
	}
	return queryResult;
    }
}
