package de.fz_juelich.phyphox_interface.data;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.fz_juelich.phyphox_interface.connection.PhyphoxConnection;
import de.fz_juelich.phyphox_interface.connection.PhyphoxDataRequest;

class PhyphoxDataTest {
	
	private static final double epsilon = 1e-5;
	
	@Test
	public void testContinuesBuffer() {
		PhyphoxData data = new PhyphoxData("time", "buffer_x");
		
		assertEquals(2, data.getAllData().size());
		//assertEquals(1, data.getContinuesBufferIndex());
	}
	
	@Test
	public void testGetBuffers() {
		PhyphoxData data = new PhyphoxData("time", "buffer_x");
		
		PhyphoxBuffer buffer1 = new PhyphoxBuffer("time", new double[] {1, 2, 3});
		List<PhyphoxBuffer> buffers = new ArrayList<PhyphoxBuffer>(2);
		buffers.add(buffer1);
		data.addNewDataToBuffers(buffers);
		
		PhyphoxBuffer bufferTime = data.getBufferData("time");
		assertEquals(3, bufferTime.size());
		assertArrayEquals(new double[] {1, 2, 3}, bufferTime.getData(), epsilon);
	}
	
	@Test
	public void testGetBuffers_wrongBufferName() {
		PhyphoxData data = new PhyphoxData("time", "buffer_x");
		
		assertThrows(IllegalStateException.class, () -> data.getBufferData("non_existing_buffer_name"));
	}
	
	@Test
	public void testAddNewDataToBuffers() {
		PhyphoxData data = new PhyphoxData("time", "buffer_x", "time");
		
		PhyphoxBuffer buffer1 = new PhyphoxBuffer("time", new double[] {1, 2, 3});
		PhyphoxBuffer buffer2 = new PhyphoxBuffer("buffer_x", new double[] {42, 43, 44, 45});
		List<PhyphoxBuffer> buffers = new ArrayList<PhyphoxBuffer>(2);
		//add the buffers in a wrong order to test the correct adding by names
		buffers.add(buffer1);
		buffers.add(buffer2);
		
		data.addNewDataToBuffers(buffers);
		
		PhyphoxBuffer bufferX = data.getBufferData("buffer_x");
		PhyphoxBuffer bufferTime = data.getBufferData("time");
		assertEquals(4, bufferX.size());
		assertArrayEquals(new double[] {42, 43, 44, 45}, bufferX.getData(), epsilon);
		assertEquals(3, bufferTime.size());
		assertArrayEquals(new double[] {1, 2, 3}, bufferTime.getData(), epsilon);
	}
	
	@Test
	public void testAddNewDataToBuffers_noContinuesBuffer() {
		PhyphoxData data = new PhyphoxData(null, "buffer_x");
		
		PhyphoxBuffer buffer = new PhyphoxBuffer("buffer_x", new double[] {42, 43, 44, 45});
		List<PhyphoxBuffer> buffers = new ArrayList<PhyphoxBuffer>(2);
		buffers.add(buffer);
		data.addNewDataToBuffers(buffers);
		
		//add some more data; the old data is deleted because only full updates are used when there is no continues buffer
		buffer = new PhyphoxBuffer("buffer_x", new double[] {1, 2, 3});
		buffers.clear();
		buffers.add(buffer);
		data.addNewDataToBuffers(buffers);
		
		PhyphoxBuffer bufferX = data.getBufferData("buffer_x");
		assertEquals(3, bufferX.size());
		assertArrayEquals(new double[] {1, 2, 3}, bufferX.getData(), epsilon);
	}
	
	@Test
	public void testCreateRequest() {
		PhyphoxData data = new PhyphoxData("time", "buffer_x");
		
		PhyphoxDataRequest request = data.createRequestForNewData();
		String requestText = request.getAsString();
		
		assertEquals("buffer_x=0.0|time&time=0.0", requestText);
	}
	
	@Test
	public void testCreateRequest_noContinuesBuffer() {
		PhyphoxData data = new PhyphoxData(null, "buffer_x", "buffer_y");
		
		PhyphoxDataRequest request = data.createRequestForNewData();
		String requestText = request.getAsString();
		
		assertEquals("buffer_x=full&buffer_y=full", requestText);
	}
	
	@Test
	public void testGetNewData() {
		PhyphoxData data = new PhyphoxData("time", "buffer_x");
		
		PhyphoxBuffer buffer = new PhyphoxBuffer("buffer_x", new double[] {42, 43, 44, 45});
		List<PhyphoxBuffer> buffers = new ArrayList<PhyphoxBuffer>(2);
		buffers.add(buffer);
		data.addNewDataToBuffers(buffers);
		
		PhyphoxBuffer completeData = data.getBufferData("buffer_x");
		
		//add some new data
		buffer = new PhyphoxBuffer("buffer_x", new double[] {1, 2, 3});
		buffers.clear();
		buffers.add(buffer);
		data.addNewDataToBuffers(buffers);
		
		PhyphoxBuffer bufferNewData = data.getNewBufferData("buffer_x");
		assertEquals(3, bufferNewData.size());
		assertArrayEquals(new double[] {1, 2, 3}, bufferNewData.getData(), epsilon);
		assertEquals(7, completeData.size());
		assertArrayEquals(new double[] {42, 43, 44, 45, 1, 2, 3}, completeData.getData(), epsilon);
	}
	
	@Test
	public void testClearBuffers() {
		PhyphoxData data = new PhyphoxData("time", "buffer_x");
		
		PhyphoxBuffer buffer = new PhyphoxBuffer("buffer_x", new double[] {42, 43, 44, 45});
		List<PhyphoxBuffer> buffers = new ArrayList<PhyphoxBuffer>(2);
		buffers.add(buffer);
		data.addNewDataToBuffers(buffers);
		
		//clear all buffers
		data.clearAllBuffers();
		
		//add some new data
		buffer = new PhyphoxBuffer("buffer_x", new double[] {1, 2, 3});
		buffers.clear();
		buffers.add(buffer);
		data.addNewDataToBuffers(buffers);
		
		PhyphoxBuffer bufferNewData = data.getNewBufferData("buffer_x");
		assertEquals(3, bufferNewData.size());
		assertArrayEquals(new double[] {1, 2, 3}, bufferNewData.getData(), epsilon);
	}
	
	@Test
	public void testRealConstructor() {
		//use the real constructor to build a PhyphoxData object
		//the object has to be constructed correctly but the update thread will cause a runtime exception
		List<String> bufferNames = Arrays.asList(new String[] {"buffer_x", "buffer_y"});
		new PhyphoxData(new PhyphoxConnection("1.1.1.1", 42), bufferNames, 1000);
	}
}