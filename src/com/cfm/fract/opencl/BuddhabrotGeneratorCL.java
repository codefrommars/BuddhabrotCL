package com.cfm.fract.opencl;

import static org.lwjgl.opencl.CL10.CL_DEVICE_TYPE_ACCELERATOR;
import static org.lwjgl.opencl.CL10.CL_DEVICE_TYPE_CPU;
import static org.lwjgl.opencl.CL10.CL_DEVICE_TYPE_DEFAULT;
import static org.lwjgl.opencl.CL10.CL_DEVICE_TYPE_GPU;

import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLDevice;
import org.lwjgl.opencl.CLKernel;
import org.lwjgl.opencl.CLMem;
import org.lwjgl.opencl.CLPlatform;
import org.lwjgl.opencl.CLProgram;
import org.lwjgl.opencl.Util;

import com.cfm.fract.FreqCounter;

public class BuddhabrotGeneratorCL {

	public static CLContext context;
	public static CLPlatform platform;
	public static List<CLDevice> devices;
	public static CLCommandQueue queue;
	
	
	public static CLProgram bdProg;
	public static CLKernel bdKernel;
	
	static class XY {
		public float x, y;
	}

	public void initialize() throws LWJGLException {
		// Create our OpenCL context to run commands
		initializeCL();

		// Create an OpenCL 'program' from a source code file
		bdProg = CL10.clCreateProgramWithSource(context,
				loadText("kernels/buddhabrot.cl"), null);
		
		
		// Build the OpenCL program, store it on the specified device
		int error = CL10.clBuildProgram(bdProg, devices.get(0), "", null);
		// Check for any OpenCL errors
		Util.checkCLError(error);
		// Create a kernel instance of our OpenCl program
		bdKernel = CL10.clCreateKernel(bdProg, "accum", null);
	}

	public static String loadText(String name) {
		if (!name.endsWith(".cl")) {
			name += ".cl";
		}
		BufferedReader br = null;
		String resultString = null;
		try {
			File clSourceFile = new File(name);
			br = new BufferedReader(new FileReader(clSourceFile));
			String line = null;
			StringBuilder result = new StringBuilder();
			while ((line = br.readLine()) != null) {
				result.append(line);
				result.append("\n");
			}
			resultString = result.toString();
		} catch (NullPointerException npe) {
			System.err.println("Error retrieving OpenCL source file: ");
			npe.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("Error reading OpenCL source file: ");
			ioe.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException ex) {
				System.err.println("Error closing OpenCL source file");
				ex.printStackTrace();
			}
		}

		return resultString;
	}
	
	public static String getDeviceType(int i) {
        switch(i) {
            case CL_DEVICE_TYPE_DEFAULT: return "DEFAULT";
            case CL_DEVICE_TYPE_CPU: return "CPU";
            case CL_DEVICE_TYPE_GPU: return "GPU";
            case CL_DEVICE_TYPE_ACCELERATOR: return "ACCELERATOR";
        }
        return "?";
    }
	
	public static String formatMemory(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
	
	public static void initializeCL() throws LWJGLException {
		IntBuffer errorBuff = BufferUtils.createIntBuffer(1);
		CL.create();
		platform = CLPlatform.getPlatforms().get(0);
		devices = platform.getDevices(CL10.CL_DEVICE_TYPE_GPU);

		for (int deviceIndex = 0; deviceIndex < devices.size(); deviceIndex++) {
			CLDevice device = devices.get(deviceIndex);
			System.out.printf(Locale.ENGLISH, "Device #%d(%s):%s\n",
					deviceIndex, getDeviceType(device
							.getInfoInt(CL10.CL_DEVICE_TYPE)), device
							.getInfoString(CL10.CL_DEVICE_NAME));
			System.out.printf(Locale.ENGLISH,
					"\tCompute Units: %d @ %d mghtz\n",
					device.getInfoInt(CL10.CL_DEVICE_MAX_COMPUTE_UNITS),
					device.getInfoInt(CL10.CL_DEVICE_MAX_CLOCK_FREQUENCY));
			System.out.printf(Locale.ENGLISH, "\tLocal memory: %s\n", 
					formatMemory(device
							.getInfoLong(CL10.CL_DEVICE_LOCAL_MEM_SIZE)));
			System.out.printf(Locale.ENGLISH, "\tGlobal memory: %s\n", 
					formatMemory(device
							.getInfoLong(CL10.CL_DEVICE_GLOBAL_MEM_SIZE)));
			System.out.println();
		}

		context = CLContext.create(platform, devices, errorBuff);
		queue = CL10.clCreateCommandQueue(context, devices.get(0),
				CL10.CL_QUEUE_PROFILING_ENABLE, errorBuff);
		Util.checkCLError(errorBuff.get(0));
	}
	
	public static void destroyCL() {
		//Kill the kernel and program
		CL10.clReleaseKernel(bdKernel);
		CL10.clReleaseProgram(bdProg);
		
		// Finish destroying anything we created
		CL10.clReleaseCommandQueue(queue);
		CL10.clReleaseContext(context);
		CL.destroy();
	}
	
	//int 32 bits (4 bytes). max: 2'000.000.000
	public void gen(Rectangle2D.Float region, int points, FreqCounter counter,
			int bailout) {
		
		counter.reset();
		
		//Create the coords buffer.
		float x_points[] = new float[points];
		float y_points[] = new float[points];
		
		for( int i = 0; i < points; i++ ){
			x_points[i] = region.x + region.width * (float)Math.random();
			y_points[i] = region.y + region.height * (float)Math.random();
		}
		
		
		long t0 = System.currentTimeMillis();
		System.out.println("Start accum...");
		
		int freqs[] = new int[points];
		runKernels( x_points, y_points, points, bailout, freqs);
		
		float secs = (System.currentTimeMillis() - t0) / 1000f;
		
		System.out.println("Accum kernel done. Taken: " + secs + " secs.");
		
		for( int i = 0 ; i < points; i++ ){
			if( freqs[i] == 0 )
				continue;
			
			float x0 = x_points[i];
			float y0 = y_points[i];
			float x = 0, y = 0, nx, ny;
			
			for( int j = 0; j < freqs[i]; j++){
				
				nx = x * x - y * y + x0;
				ny = 2 * x * y + y0;

				x = nx;
				y = ny;
				
				int ix = (int) (((x - region.x) * counter.getWidth() / region.width));
				int iy = (int) (((y - region.y) * counter.getHeight() / region.height));
				
				if (ix >= 0 && iy >= 0 && ix < counter.getWidth() && iy < counter.getHeight())
					counter.addFreq(ix, iy);
			}
			
		}
	}


	private void runKernels(float[] x_points, float[] y_points, int points,
			int bailout, int[] freqs) {
		
		FloatBuffer xBuff = BufferUtils.createFloatBuffer(points);
		xBuff.put(x_points);
		xBuff.rewind();
		
		FloatBuffer yBuff = BufferUtils.createFloatBuffer(points);
		yBuff.put(y_points);
		yBuff.rewind();
		
		IntBuffer errorBuff = BufferUtils.createIntBuffer(1);
		
		CLMem xMem = CL10.clCreateBuffer(context, CL10.CL_MEM_READ_ONLY | CL10.CL_MEM_COPY_HOST_PTR, xBuff, errorBuff);
		Util.checkCLError(errorBuff.get(0));
		
		CLMem yMem = CL10.clCreateBuffer(context, CL10.CL_MEM_READ_ONLY | CL10.CL_MEM_COPY_HOST_PTR, yBuff, errorBuff);
		Util.checkCLError(errorBuff.get(0));
		
		CLMem oMem = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY, points * 4, errorBuff);
		Util.checkCLError(errorBuff.get(0));
		
		bdKernel.setArg(0, xMem);
		bdKernel.setArg(1, yMem);
		bdKernel.setArg(2, bailout);
		bdKernel.setArg(3, oMem);
		bdKernel.setArg(4, points);
		
		// Create a buffer of pointers defining the multi-dimensional size of the number of work units to execute
		final int dimensions = 1; 
		PointerBuffer globalWorkSize = BufferUtils.createPointerBuffer(dimensions);
		globalWorkSize.put(0, points);
		
		CL10.clEnqueueNDRangeKernel(queue, bdKernel, dimensions, null, globalWorkSize, null, null, null);
		CL10.clFinish(queue);
		
		//Read the result buffer.
		IntBuffer resultBuff = BufferUtils.createIntBuffer(points);
		CL10.clEnqueueReadBuffer(queue, oMem, CL10.CL_TRUE, 0, resultBuff, null, null);
		
		for(int i = 0; i < points; i++){
			freqs[i] = resultBuff.get(i);
		}
		
		CL10.clReleaseMemObject(xMem);
		CL10.clReleaseMemObject(yMem);
		CL10.clReleaseMemObject(oMem);
	}

}
