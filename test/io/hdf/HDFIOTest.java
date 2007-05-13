package test.io.hdf;

import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.TestCase;

public class HDFIOTest extends TestCase {
	
	HDFIOTest(){
		super();
	}
	

	public void testReadFileFileOpenModeFile() {
		final ClassLoader loader = this.getClass().getClassLoader();
		final URL url = loader.getResource("sampledata/exampleGates.hdf");
		URI uri = null;
		try {
			uri = url.toURI();
			final File file = new File(uri);
			final HDFIO hdfio=new HDFIO(null);
			hdfio.readFile(FileOpenMode.OPEN, file );
		} catch (URISyntaxException e) {//NOPMD
			//TODO something here
		}
		//TODO actual test completion
		fail();//test not completed
	}

}





