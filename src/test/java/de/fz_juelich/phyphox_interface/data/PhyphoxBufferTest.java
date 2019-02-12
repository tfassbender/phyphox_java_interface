package de.fz_juelich.phyphox_interface.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PhyphoxBufferTest {
	
	@Test
	public void testAttachData() {
		double[] data = new double[] {1, 2, 3};
		double[] attachment = new double[] {4, 5};
		
		PhyphoxBuffer buffer = new PhyphoxBuffer("buffer1", data);
		buffer.attachData(attachment);
		
		final double epsilon = 1e-5;
		assertEquals(5, buffer.getData().length);
		assertArrayEquals(new double[] {1, 2, 3, 4, 5}, buffer.getData(), epsilon);
	}
	
	@Test
	public void testClone() {
		double[] data = new double[] {1, 2, 3, 4, 5};
		
		PhyphoxBuffer buffer = new PhyphoxBuffer("buffer1", data);
		PhyphoxBuffer subBuffer = buffer.getCopyFromIndex(2);
		
		final double epsilon = 1e-5;
		assertEquals(3, subBuffer.getData().length);
		assertArrayEquals(new double[] {3, 4, 5}, subBuffer.getData(), epsilon);
	}
}