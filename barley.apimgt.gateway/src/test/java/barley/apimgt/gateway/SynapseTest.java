package barley.apimgt.gateway;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.rest.APIFactory;
import org.apache.synapse.config.xml.rest.APISerializer;
import org.apache.synapse.rest.API;

import junit.framework.TestCase;

public class SynapseTest extends TestCase {

	
	public void testAPISerialization1() throws Exception {
        String xml = "<api name=\"test\" context=\"/dictionary\" transports=\"https\" xmlns=\"http://ws.apache.org/ns/synapse\">" +
                "<resource url-mapping=\"/admin/view\" inSequence=\"in\" outSequence=\"out\"/></api>";
        OMElement om = AXIOMUtil.stringToOM(xml);
        API api = APIFactory.createAPI(om);
        OMElement out = APISerializer.serializeAPI(api);
        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.addAPI(api.getAPIName(), api);
        assertEquals(xml, out.toString());        
    }

}
