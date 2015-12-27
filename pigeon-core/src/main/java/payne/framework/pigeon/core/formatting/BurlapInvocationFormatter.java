package payne.framework.pigeon.core.formatting;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import payne.framework.pigeon.core.Header;
import payne.framework.pigeon.core.Invocation;
import payne.framework.pigeon.core.exception.FormatterException;

import com.caucho.burlap.io.BurlapInput;
import com.caucho.burlap.io.BurlapOutput;

public class BurlapInvocationFormatter implements InvocationFormatter {

	public String algorithm() {
		return "application/burlap";
	}

	public void serialize(Header header, Invocation data, OutputStream out, String charset) throws FormatterException {
		BurlapOutput oos = null;
		try {
			oos = new BurlapOutput(out);
			oos.writeObject(data);
		} catch (Exception e) {
			throw new FormatterException(e, this, data, null);
		} finally {
			try {
				oos.close();
			} catch (Exception e) {
				throw new FormatterException(e, this, data, null);
			}
		}
	}

	public Invocation deserialize(Header header, InputStream in, String charset, Method method) throws FormatterException {
		BurlapInput ois = null;
		try {
			ois = new BurlapInput(in);
			return (Invocation) ois.readObject();
		} catch (Exception e) {
			throw new FormatterException(e, this, in, method);
		} finally {
			try {
				ois.close();
			} catch (Exception e) {
				throw new FormatterException(e, this, in, method);
			}
		}
	}

}
