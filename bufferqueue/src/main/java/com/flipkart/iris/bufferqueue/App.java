package com.flipkart.iris.bufferqueue;

import java.io.File;
import java.io.IOException;

import com.flipkart.iris.bufferqueue.mmapped.MappedBufferQueueFactory;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IllegalStateException, IOException
    {
    	File file = new File("test.ibq");
    	if (!file.exists()) {
    	    int maxDataLength = 4 * 1024; // max size of data that can be written to the queue
    	    long numMessages = 1000000; // maximum number of unconsumed messages that can be kept in the queue
    	    MappedBufferQueueFactory.format(file, maxDataLength, numMessages);
    	}
    	BufferQueue bufferQueue = MappedBufferQueueFactory.getInstance(file);
    	byte[] data = "Hello world!".getBytes();
    	
    	BufferQueueEntry entry = bufferQueue.next().orNull();
    	if (entry != null) {
    	    try {
    	        entry.set(data);
    	    }
    	    finally {
    	        entry.markPublished();
    	    }
    	}
    	else {
    	    System.out.println("Queue full, cannot write message");
    	}
    	
        
    	BufferQueueEntry entry1 = bufferQueue.consume().orNull();
    	if (entry1 != null) {
    	    try {
    	        byte[] data1 = entry1.get();
    	        System.out.println(data1);
    	    }
    	    finally {
    	        entry1.markConsumed();
    	    }
    	}
    	else {
    	    System.out.println("Nothing to consume");
    	}
    }
}
