package barley.apimgt.gateway.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.Pipe;

public class RelayUtils {
	
	private static final Log log = LogFactory.getLog(RelayUtils.class);
	
	public static void consumeAndDiscardMessage(MessageContext msgContext) throws AxisFault {
        final Pipe pipe = (Pipe) msgContext.getProperty(PassThroughConstants.PASS_THROUGH_PIPE);
        if (pipe != null) {
            InputStream in = pipe.getInputStream();
            if (in != null) {
                try {
                    IOUtils.copy(in, new NullOutputStream());
                } catch (IOException exception) {
                    handleException("Error when consuming the input stream to discard ", exception);
                }
            }
        }
    }
	
	/**
     * Perform an error log message to all logs @ ERROR and throws a AxisFault
     *
     * @param msg the log message
     * @param e   an Exception encountered
     * @throws AxisFault
     */
    private static void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }
}
