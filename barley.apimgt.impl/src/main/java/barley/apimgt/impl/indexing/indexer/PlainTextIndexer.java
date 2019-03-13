package barley.apimgt.impl.indexing.indexer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrException;

import barley.registry.core.exceptions.RegistryException;
import barley.registry.core.utils.RegistryUtils;
import barley.registry.indexing.AsyncIndexer.File2Index;
import barley.registry.indexing.IndexingConstants;
import barley.registry.indexing.indexer.Indexer;
import barley.registry.indexing.solr.IndexDocument;

public class PlainTextIndexer implements Indexer {

	public IndexDocument getIndexedDocument(File2Index fileData) throws SolrException,
            RegistryException {
				
		IndexDocument indexDoc = new IndexDocument(fileData.path, RegistryUtils.decodeBytes(fileData.data), null);
				
		Map<String, List<String>> fields = new HashMap<String, List<String>>();
		fields.put("path", Arrays.asList(fileData.path));
				
		if (fileData.mediaType != null) {
			fields.put(IndexingConstants.FIELD_MEDIA_TYPE, Arrays.asList(fileData.mediaType));
		} else {
			fields.put(IndexingConstants.FIELD_MEDIA_TYPE, Arrays.asList("text/(.)"));
		}
		
		indexDoc.setFields(fields);
		
		return indexDoc;
	}	

}
