package barley.apimgt.impl.indexing.indexer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.SolrException;

import barley.registry.core.exceptions.RegistryException;
import barley.registry.core.utils.RegistryUtils;
import barley.registry.indexing.AsyncIndexer.File2Index;
import barley.registry.indexing.IndexingConstants;
import barley.registry.indexing.indexer.Indexer;
import barley.registry.indexing.solr.IndexDocument;


public class XMLIndexer implements Indexer {
	
	public static final Log log = LogFactory.getLog(XMLIndexer.class);

	public IndexDocument getIndexedDocument(File2Index fileData) throws SolrException, RegistryException {
        // we register both the content as it is and only text content
        String xmlAsStr = RegistryUtils.decodeBytes(fileData.data);

        final StringBuilder contentOnly = new StringBuilder();
        
        IndexDocument indexDocument = new IndexDocument(fileData.path, xmlAsStr,
                contentOnly.toString());
        Map<String, List<String>> attributes = new HashMap<String, List<String>>();
        attributes.put("path", Arrays.asList(fileData.path));
        if (fileData.mediaType != null) {
            attributes.put(IndexingConstants.FIELD_MEDIA_TYPE, Arrays.asList(fileData.mediaType));
        }
        indexDocument.setFields(attributes);
        return indexDocument;
	}


}
