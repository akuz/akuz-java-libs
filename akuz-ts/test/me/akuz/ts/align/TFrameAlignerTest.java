package me.akuz.ts.align;

import me.akuz.core.Out;
import me.akuz.ts.Frame;
import me.akuz.ts.FrameIterator;

import org.junit.Assert;
import org.junit.Test;

public final class TFrameAlignerTest {
	
	private static Frame<String, Integer> createSimpleFrame() {
		
		Frame<String, Integer> frame = new Frame<>();
		
		frame.add("f1", 0, 10);
		frame.add("f1", 1, 11);
		frame.add("f1", 2, 12);
		frame.add("f1", 3, 13);
		
		frame.add("f2", 0, 20);
		frame.add("f2", 3, 23);
		
		return frame;
	}

	@Test
	public void testMoveToSpecificTime() {
		
		Frame<String, Integer> frame = createSimpleFrame();
		
		FrameIterator<String, Integer> iter = new FrameIterator<>(frame, frame.getKeys());
		Out<Integer> nextTime = new Out<>();
		
		Assert.assertTrue(iter.getNextTime(nextTime));
		iter.moveToTime(0);

		Assert.assertEquals((Integer)0, iter.getCurrTime());
		
		Assert.assertEquals(1, iter.getMovedItems().get("f1").size());
		Assert.assertTrue(iter.getCurrItems().containsKey("f1"));
		Assert.assertEquals((Integer)0, iter.getCurrItems().get("f1").getTime());
		Assert.assertEquals((Integer)10, iter.getCurrItems().get("f1").getInteger());
		
		Assert.assertEquals(1, iter.getMovedItems().get("f2").size());
		Assert.assertTrue(iter.getCurrItems().containsKey("f2"));
		Assert.assertEquals((Integer)0, iter.getCurrItems().get("f2").getTime());
		Assert.assertEquals((Integer)20, iter.getCurrItems().get("f2").getInteger());
		
		Assert.assertTrue(iter.getNextTime(nextTime));
		iter.moveToTime(2);

		Assert.assertEquals((Integer)2, iter.getCurrTime());
		
		Assert.assertEquals(2, iter.getMovedItems().get("f1").size());
		Assert.assertTrue(iter.getCurrItems().containsKey("f1"));
		Assert.assertEquals((Integer)2, iter.getCurrItems().get("f1").getTime());
		Assert.assertEquals((Integer)12, iter.getCurrItems().get("f1").getInteger());
		
		Assert.assertEquals(0, iter.getMovedItems().get("f2").size());
		Assert.assertFalse(iter.getCurrItems().containsKey("f2"));
		
		Assert.assertTrue(iter.getNextTime(nextTime));
		iter.moveToTime(3);

		Assert.assertEquals((Integer)3, iter.getCurrTime());
		
		Assert.assertEquals(1, iter.getMovedItems().get("f1").size());
		Assert.assertTrue(iter.getCurrItems().containsKey("f1"));
		Assert.assertEquals((Integer)3, iter.getCurrItems().get("f1").getTime());
		Assert.assertEquals((Integer)13, iter.getCurrItems().get("f1").getInteger());
		
		Assert.assertEquals(1, iter.getMovedItems().get("f2").size());
		Assert.assertTrue(iter.getCurrItems().containsKey("f2"));
		Assert.assertEquals((Integer)3, iter.getCurrItems().get("f2").getTime());
		Assert.assertEquals((Integer)23, iter.getCurrItems().get("f2").getInteger());
		
		Assert.assertFalse(iter.getNextTime(nextTime));
	}

	@Test
	public void testMoveToNextTime() {
		
		Frame<String, Integer> frame = createSimpleFrame();
		
		FrameIterator<String, Integer> iter = new FrameIterator<>(frame, frame.getKeys());
		Out<Integer> nextTime = new Out<>();
		
		Assert.assertTrue(iter.getNextTime(nextTime));
		iter.moveToTime(nextTime.getValue());

		Assert.assertEquals((Integer)0, iter.getCurrTime());
		
		Assert.assertEquals(1, iter.getMovedItems().get("f1").size());
		Assert.assertTrue(iter.getCurrItems().containsKey("f1"));
		Assert.assertEquals((Integer)0, iter.getCurrItems().get("f1").getTime());
		Assert.assertEquals((Integer)10, iter.getCurrItems().get("f1").getInteger());
		
		Assert.assertEquals(1, iter.getMovedItems().get("f2").size());
		Assert.assertTrue(iter.getCurrItems().containsKey("f2"));
		Assert.assertEquals((Integer)0, iter.getCurrItems().get("f2").getTime());
		Assert.assertEquals((Integer)20, iter.getCurrItems().get("f2").getInteger());
		
		Assert.assertTrue(iter.getNextTime(nextTime));
		iter.moveToTime(nextTime.getValue());

		Assert.assertEquals((Integer)1, iter.getCurrTime());
		
		Assert.assertEquals(1, iter.getMovedItems().get("f1").size());
		Assert.assertTrue(iter.getCurrItems().containsKey("f1"));
		Assert.assertEquals((Integer)1, iter.getCurrItems().get("f1").getTime());
		Assert.assertEquals((Integer)11, iter.getCurrItems().get("f1").getInteger());
		
		Assert.assertEquals(0, iter.getMovedItems().get("f2").size());
		Assert.assertFalse(iter.getCurrItems().containsKey("f2"));
		
		Assert.assertTrue(iter.getNextTime(nextTime));
		iter.moveToTime(nextTime.getValue());

		Assert.assertEquals((Integer)2, iter.getCurrTime());
		
		Assert.assertEquals(1, iter.getMovedItems().get("f1").size());
		Assert.assertTrue(iter.getCurrItems().containsKey("f1"));
		Assert.assertEquals((Integer)2, iter.getCurrItems().get("f1").getTime());
		Assert.assertEquals((Integer)12, iter.getCurrItems().get("f1").getInteger());
		
		Assert.assertEquals(0, iter.getMovedItems().get("f2").size());
		Assert.assertFalse(iter.getCurrItems().containsKey("f2"));
		
		Assert.assertTrue(iter.getNextTime(nextTime));
		iter.moveToTime(nextTime.getValue());

		Assert.assertEquals((Integer)3, iter.getCurrTime());
		
		Assert.assertEquals(1, iter.getMovedItems().get("f1").size());
		Assert.assertTrue(iter.getCurrItems().containsKey("f1"));
		Assert.assertEquals((Integer)3, iter.getCurrItems().get("f1").getTime());
		Assert.assertEquals((Integer)13, iter.getCurrItems().get("f1").getInteger());
		
		Assert.assertEquals(1, iter.getMovedItems().get("f2").size());
		Assert.assertTrue(iter.getCurrItems().containsKey("f2"));
		Assert.assertEquals((Integer)3, iter.getCurrItems().get("f2").getTime());
		Assert.assertEquals((Integer)23, iter.getCurrItems().get("f2").getInteger());
		
		Assert.assertFalse(iter.getNextTime(nextTime));
	}

	@Test
	public void testSimpleBadTimes() {
		
		Frame<String, Integer> frame = createSimpleFrame();
		
		FrameIterator<String, Integer> iter = new FrameIterator<>(frame, frame.getKeys());
		
		try {
			iter.moveToTime(2);
			iter.moveToTime(0);
			throw new IllegalStateException("Should have thrown exception because of bad times");
		} catch (Exception ex) {
			// expected exception
			return;
		}
	}

}
