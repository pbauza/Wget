package practica1_xarxes;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Html2AsciiInputStream extends FilterInputStream{
	protected Html2AsciiInputStream(InputStream in) {
		super(in);
	}
	
	public int read() {
		int c = -1;
		
		try {
			c = in.read();
			if(c == 60) {
				c = -2;
			}else {
				if(c == 62) {
					c = -3;
				}
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return c;
	}
}
