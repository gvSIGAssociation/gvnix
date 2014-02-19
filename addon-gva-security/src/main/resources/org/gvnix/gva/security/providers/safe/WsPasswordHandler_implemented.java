import java.io.IOException;
import java.util.Properties;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.ws.security.WSPasswordCallback;

public class WsPasswordHandler implements CallbackHandler {

	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

		Properties configuration = WsSafeAuthenticationProvider.getSafeProperties();

		for(Callback callback: callbacks) {
			WSPasswordCallback pwdCallback = (WSPasswordCallback)callback;

			int usage = pwdCallback.getUsage();
			if (usage == WSPasswordCallback.SIGNATURE || usage == WSPasswordCallback.DECRYPT) {
					pwdCallback.setPassword(configuration.getProperty("org.apache.ws.security.crypto.merlin.alias.password"));

			}
		}
	}
}
