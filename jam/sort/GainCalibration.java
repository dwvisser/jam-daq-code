/*
 * Created on Dec 15, 2004
 */
package jam.sort;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for writers of sort routines to easily read in calibrations
 * and use them.
 * 
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 */
public final class GainCalibration {
    
    final Map gains;
    final Map offsets;
    boolean suppress=false;
    
    GainCalibration(){
        gains=new HashMap();
        offsets=new HashMap();
    }

    /**
     * Reads in a gain file. The expected format is a text file where every line
     * is lists a parameter id, a gain coefficient and an offset value.
     * 
     * @param name of the gain file in the sort routine's package
     * @param suppress <code>true</code> adjust to zero any value for
     * which no gain coeffiecients exist, <code>false</code> to simply
     * return the original value back to the user
     * @throws SortException if there's a problem reading the gain file
     */
    public void gainFile(String name, boolean suppress) throws SortException {
        try {
            this.suppress=suppress;
            final ClassLoader loader = getClass().getClassLoader();
            final InputStream input = loader.getResourceAsStream(name);
            final int rows=getNumberOfRows(input);
            readGains(input,rows);
        } catch (IOException ioe) {
            throw new SortException("There was an error while reading the gain file, \""+
                    name+"\".",ioe);
        }
    }
    
    /**
     * Returns the exact floating point gain-adjusted value for the given
     * parameter id and channel. The formula is: <code>rval=gain*value+offset</code>
     * 
     * @param param parameter id
     * @param value channel datum
     * @return exact gain-adjusted value
     */
    public double adjustExact(int param, int value){
        final Integer key = new Integer(param);
        double rval = suppress ? 0 : value;
        if (gains.containsKey(key) && offsets.containsKey(key)) {
            final double gain = ((Number) gains.get(key)).doubleValue();
            final double offset = ((Number) gains.get(key)).doubleValue();
            rval = gain * value + offset;
        }
        return rval;
    }
    
    /**
     * Returns an integer approximation to the gain-adjusted value for the given
     * parameter id and channel. The formula is: 
     * <code>rval=adjustFP(param,value)+random(-gain/2,+gain/2)</code>, 
     * where random(min,max) is a uniformly distributed random number between
     * min and max.
     * 
     * @param param parameter id
     * @param value channel datum
     * @return exact gain-adjusted value
     * @see #adjustExact(int, int)
     */
    public int adjust(int param, int value) {
        final Integer key = new Integer(param);
        int rval = suppress ? 0 : value;
        if (gains.containsKey(key) && offsets.containsKey(key)) {
            final double gain = ((Number) gains.get(key)).doubleValue();
            final double offset = ((Number) gains.get(key)).doubleValue();
            final double randomOffset = (Math.random() - 0.5) * gain;
            final double dval = gain * value + offset + randomOffset;
            rval = (int) Math.round(dval);
        }
        return rval;
    }

	private void readGains(
			InputStream in,
			int rows)
			throws IOException {
			LineNumberReader lnr = new LineNumberReader(new InputStreamReader(in));
			StreamTokenizer st = new StreamTokenizer(lnr);
			st.eolIsSignificant(false);
			for (int i = 0; i < rows; i++) {
				st.nextToken();
				final int parameter = (int) st.nval;
				st.nextToken();
				final double gain = st.nval;
				st.nextToken();
				final double offset = st.nval;
				final Integer iParam=new Integer(parameter);
				gains.put(iParam, new Double(gain));
				offsets.put(iParam, new Double(offset));
			}
		}
	
	private int getNumberOfRows(InputStream in) throws IOException {
		int rval = 0;
		final LineNumberReader lnr = new LineNumberReader(new InputStreamReader(in));
		//read in header lines, header are lines that start with a non-number token
		while (lnr.readLine() != null) {
			rval++;
		}
		lnr.close();
		return rval;
	}

}
