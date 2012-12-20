package db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;

import crowdtrust.BinaryTask;
import crowdtrust.Task;

import junit.framework.TestCase;

public class TestUploadToDatabase extends TestCase{
	
	private final String name1 = "test1n";
	private final String question1 = "test1q";
	private final int accuracy1 = 1;
	private final String name2 = "test2n";
	private final String question2 = "test2q";
	private final int accuracy2 = 3;

	public TestUploadToDatabase(String name) {
		super(name);
	}
	
	public void testUpload() throws Exception {
//		Process tomcatServer = null;
//		InputStream is = tomcatServer.getInputStream();
//		String result = IOUtils.toString(is, "ASCII");
//		System.out.println(result);
		Task t1 = new BinaryTask(1, name1, question1, accuracy1);
		Task t2 = new BinaryTask(2, name2, question2, accuracy2);
		t1.addToDatabase();
		t2.addToDatabase();
		Task x = TaskDb.getTask(name1);
		Task y = TaskDb.getTask(name2);
		assertTrue(x.getId()==1);
		assertTrue(y.getId()==2);
	}

}
