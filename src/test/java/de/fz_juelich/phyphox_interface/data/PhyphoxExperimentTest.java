package de.fz_juelich.phyphox_interface.data;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Matchers;

import de.fz_juelich.phyphox_interface.connection.PhyphoxConnectionSettings;
import de.fz_juelich.phyphox_interface.connection.PhyphoxDataRequest;

class PhyphoxExperimentTest {
	
	private static final double epsilon = 1e-5;
	
	@Test
	public void testContinuesBuffer() {
		PhyphoxExperiment experiment = new PhyphoxExperiment("time", "buffer_x");
		
		assertEquals(2, experiment.getAllData().size());
		//assertEquals(1, data.getContinuesBufferIndex());
	}
	
	@Test
	public void testGetBuffers() {
		PhyphoxExperiment experiment = new PhyphoxExperiment("time", "buffer_x");
		
		PhyphoxBuffer buffer1 = new PhyphoxBuffer("time", new double[] {1, 2, 3});
		List<PhyphoxBuffer> buffers = new ArrayList<PhyphoxBuffer>(2);
		buffers.add(buffer1);
		experiment.addNewDataToBuffers(buffers);
		
		PhyphoxBuffer bufferTime = experiment.getBufferData("time");
		assertEquals(3, bufferTime.size());
		assertArrayEquals(new double[] {1, 2, 3}, bufferTime.getData(), epsilon);
	}
	
	@Test
	public void testGetBuffers_wrongBufferName() {
		PhyphoxExperiment experiment = new PhyphoxExperiment("time", "buffer_x");
		
		assertThrows(IllegalStateException.class, () -> experiment.getBufferData("non_existing_buffer_name"));
	}
	
	@Test
	public void testAddNewDataToBuffers() {
		PhyphoxExperiment experiment = new PhyphoxExperiment("time", "buffer_x", "time");
		
		PhyphoxBuffer buffer1 = new PhyphoxBuffer("time", new double[] {1, 2, 3});
		PhyphoxBuffer buffer2 = new PhyphoxBuffer("buffer_x", new double[] {42, 43, 44, 45});
		List<PhyphoxBuffer> buffers = new ArrayList<PhyphoxBuffer>(2);
		//add the buffers in a wrong order to test the correct adding by names
		buffers.add(buffer1);
		buffers.add(buffer2);
		
		experiment.addNewDataToBuffers(buffers);
		
		PhyphoxBuffer bufferX = experiment.getBufferData("buffer_x");
		PhyphoxBuffer bufferTime = experiment.getBufferData("time");
		assertEquals(4, bufferX.size());
		assertArrayEquals(new double[] {42, 43, 44, 45}, bufferX.getData(), epsilon);
		assertEquals(3, bufferTime.size());
		assertArrayEquals(new double[] {1, 2, 3}, bufferTime.getData(), epsilon);
	}
	
	@Test
	public void testAddNewDataToBuffers_noContinuesBuffer() {
		PhyphoxExperiment experiment = new PhyphoxExperiment("buffer_x");
		
		PhyphoxBuffer buffer = new PhyphoxBuffer("buffer_x", new double[] {42, 43, 44, 45});
		List<PhyphoxBuffer> buffers = new ArrayList<PhyphoxBuffer>(2);
		buffers.add(buffer);
		experiment.addNewDataToBuffers(buffers);
		
		//add some more data; the old data appended in this version of the full buffer implementation
		buffer = new PhyphoxBuffer("buffer_x", new double[] {1, 2, 3});
		buffers.clear();
		buffers.add(buffer);
		experiment.addNewDataToBuffers(buffers);
		
		PhyphoxBuffer bufferX = experiment.getBufferData("buffer_x");
		assertEquals(7, bufferX.size());//the data is added and not overwritten
		assertArrayEquals(new double[] {42, 43, 44, 45, 1, 2, 3}, bufferX.getData(), epsilon);
	}
	
	@Test
	public void testCreateRequest() {
		PhyphoxExperiment experiment = new PhyphoxExperiment("time", "buffer_x");
		
		PhyphoxDataRequest request = experiment.createRequestForNewData();
		String requestText = request.getAsString();
		
		//only full updates are used in this implementation
		assertEquals("time=full&buffer_x=full", requestText);
	}
	
	@Test
	public void testCreateRequest_noContinuesBuffer() {
		PhyphoxExperiment experiment = new PhyphoxExperiment("buffer_x", "buffer_y");
		
		PhyphoxDataRequest request = experiment.createRequestForNewData();
		String requestText = request.getAsString();
		
		assertEquals("buffer_x=full&buffer_y=full", requestText);
	}
	
	@Test
	public void testGetNewData() {
		PhyphoxExperiment experiment = new PhyphoxExperiment("time", "buffer_x");
		
		PhyphoxBuffer buffer = new PhyphoxBuffer("buffer_x", new double[] {42, 43, 44, 45});
		List<PhyphoxBuffer> buffers = new ArrayList<PhyphoxBuffer>(2);
		buffers.add(buffer);
		experiment.addNewDataToBuffers(buffers);
		
		PhyphoxBuffer completeData = experiment.getBufferData("buffer_x");
		
		//add some new data
		buffer = new PhyphoxBuffer("buffer_x", new double[] {1, 2, 3});
		buffers.clear();
		buffers.add(buffer);
		experiment.addNewDataToBuffers(buffers);
		
		PhyphoxBuffer bufferNewData = experiment.getNewBufferData("buffer_x");
		assertEquals(3, bufferNewData.size());
		assertArrayEquals(new double[] {1, 2, 3}, bufferNewData.getData(), epsilon);
		assertEquals(7, completeData.size());
		assertArrayEquals(new double[] {42, 43, 44, 45, 1, 2, 3}, completeData.getData(), epsilon);
	}
	
	@Test
	public void testClearBuffers() {
		PhyphoxExperiment experiment = new PhyphoxExperiment("time", "buffer_x");
		
		PhyphoxBuffer buffer = new PhyphoxBuffer("buffer_x", new double[] {42, 43, 44, 45});
		List<PhyphoxBuffer> buffers = new ArrayList<PhyphoxBuffer>(2);
		buffers.add(buffer);
		experiment.addNewDataToBuffers(buffers);
		
		//clear all buffers
		experiment.clearAllBuffers();
		
		//add some new data
		buffer = new PhyphoxBuffer("buffer_x", new double[] {1, 2, 3});
		buffers.clear();
		buffers.add(buffer);
		experiment.addNewDataToBuffers(buffers);
		
		PhyphoxBuffer bufferNewData = experiment.getNewBufferData("buffer_x");
		assertEquals(3, bufferNewData.size());
		assertArrayEquals(new double[] {1, 2, 3}, bufferNewData.getData(), epsilon);
	}
	
	@Test
	public void testRealConstructor() {
		//use the real constructor to build a PhyphoxData object
		//the object has to be constructed correctly but the update thread will cause a runtime exception
		List<String> bufferNames = Arrays.asList(new String[] {"buffer_x", "buffer_y"});
		new PhyphoxExperiment(new PhyphoxConnectionSettings("1.1.1.1", 42), bufferNames, 1000);
	}
	
	@Test
	public void testListenerPattern() {
		PhyphoxDataListener listener = mock(PhyphoxDataListener.class);
		PhyphoxExperiment experiment = new PhyphoxExperiment("time", "buffer_x");
		experiment.addDataListener(listener);
		
		PhyphoxBuffer buffer = new PhyphoxBuffer("buffer_x", new double[] {42, 43, 44, 45});
		List<PhyphoxBuffer> buffers = new ArrayList<PhyphoxBuffer>(2);
		buffers.add(buffer);
		experiment.addNewDataToBuffers(buffers);
		
		verify(listener, times(1)).updateData(Matchers.anyListOf(PhyphoxBuffer.class), any(Boolean.class));
	}
	
	@Test
	public void testAddAndRemoveListeners() {
		PhyphoxDataListener listener = mock(PhyphoxDataListener.class);
		PhyphoxExperiment experiment = new PhyphoxExperiment("time", "buffer_x");
		experiment.addDataListener(listener);
		
		PhyphoxBuffer buffer = new PhyphoxBuffer("buffer_x", new double[] {1, 2, 3});
		List<PhyphoxBuffer> buffers = new ArrayList<PhyphoxBuffer>(Arrays.asList(new PhyphoxBuffer[] {buffer}));
		experiment.addNewDataToBuffers(buffers);
		
		//the update method was called
		verify(listener, times(1)).updateData(Matchers.anyListOf(PhyphoxBuffer.class), any(Boolean.class));
		
		experiment.removeDataListener(listener);
		
		//the update method is still called only once
		verify(listener, times(1)).updateData(Matchers.anyListOf(PhyphoxBuffer.class), any(Boolean.class));
	}
}