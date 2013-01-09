import junit.framework.Assert;
import org.junit.Test;

import java.awt.geom.Point2D;

public class IntervalTest {

    @Test
    public void testIntersects() throws Exception {
        Interval left = new Interval(new Point2D.Double(0, 0), new Point2D.Double(1, 0));
        Interval middle = new Interval(new Point2D.Double(0.5, 0), new Point2D.Double(2.5, 0));
        Interval right = new Interval(new Point2D.Double(2, 0), new Point2D.Double(3, 0));
        Interval offAxis = new Interval(new Point2D.Double(0, 0), new Point2D.Double(0, 1));

        Assert.assertTrue(left.intersects(middle));
        Assert.assertTrue(middle.intersects(left));
        Assert.assertTrue(middle.intersects(right));
        Assert.assertTrue(right.intersects(middle));
        Assert.assertFalse(left.intersects(right));
        Assert.assertFalse(right.intersects(left));

        Assert.assertFalse(left.intersects(offAxis));
        Assert.assertFalse(offAxis.intersects(left));
        Assert.assertFalse(middle.intersects(offAxis));
        Assert.assertFalse(offAxis.intersects(middle));
        Assert.assertFalse(right.intersects(offAxis));
        Assert.assertFalse(offAxis.intersects(right));
    }

    @Test
    public void testIntersection() throws Exception {
        Interval left = new Interval(new Point2D.Double(0, 0), new Point2D.Double(1, 0));
        Interval middle = new Interval(new Point2D.Double(0.5, 0), new Point2D.Double(2.5, 0));
        Interval right = new Interval(new Point2D.Double(2, 0), new Point2D.Double(3, 0));
        Interval offAxis = new Interval(new Point2D.Double(0, 0), new Point2D.Double(0, 1));

        Interval outer = new Interval(new Point2D.Double(0, 0), new Point2D.Double(3, 0));
        Interval inner = new Interval(new Point2D.Double(1, 0), new Point2D.Double(2, 0));

        //commutativity
        Assert.assertEquals(left.intersection(middle), middle.intersection(left));
        Assert.assertEquals(left.intersection(right), right.intersection(left));
        Assert.assertEquals(middle.intersection(right), right.intersection(middle));
        Assert.assertEquals(left.intersection(offAxis), offAxis.intersection(left));
        Assert.assertEquals(right.intersection(offAxis), offAxis.intersection(right));
        Assert.assertEquals(middle.intersection(offAxis), offAxis.intersection(middle));

        //check nulls
        Assert.assertEquals(left.intersection(right), null);
        Assert.assertEquals(left.intersection(offAxis), null);
        Assert.assertEquals(middle.intersection(offAxis), null);
        Assert.assertEquals(right.intersection(offAxis), null);

        //check intersections
        Assert.assertEquals(new Interval(new Point2D.Double(0.5, 0), new Point2D.Double(1, 0)), left.intersection(middle));
        Assert.assertEquals(new Interval(new Point2D.Double(2, 0), new Point2D.Double(2.5, 0)), right.intersection(middle));
        Assert.assertEquals(inner, outer.intersection(inner));

    }

    @Test
    public void testIsParallelTo() throws Exception {
        Interval top = new Interval(new Point2D.Double(0, 1), new Point2D.Double(1, 1));
        Interval bottom = new Interval(new Point2D.Double(0, 0), new Point2D.Double(1, 0));
        Interval left = new Interval(new Point2D.Double(0, 0), new Point2D.Double(0, 1));
        Interval right = new Interval(new Point2D.Double(1, 0), new Point2D.Double(1, 1));

        Assert.assertTrue(top.isParallelTo(bottom));
        Assert.assertTrue(bottom.isParallelTo(top));
        Assert.assertTrue(left.isParallelTo(right));
        Assert.assertTrue(right.isParallelTo(left));

        Assert.assertFalse(top.isParallelTo(right));
        Assert.assertFalse(top.isParallelTo(left));
        Assert.assertFalse(right.isParallelTo(bottom));
        Assert.assertFalse(right.isParallelTo(top));
        Assert.assertFalse(left.isParallelTo(top));
        Assert.assertFalse(left.isParallelTo(bottom));
        Assert.assertFalse(bottom.isParallelTo(right));
        Assert.assertFalse(bottom.isParallelTo(left));
    }

    @Test
    public void testIsVertical() throws Exception {
        Interval shouldBeVertical = new Interval(new Point2D.Double(0, 0), new Point2D.Double(0, 1));
        Interval shouldBeHorizontal = new Interval(new Point2D.Double(0, 0), new Point2D.Double(1, 0));
        Assert.assertTrue(shouldBeVertical.isVertical());
        Assert.assertFalse(shouldBeHorizontal.isVertical());
    }
}
