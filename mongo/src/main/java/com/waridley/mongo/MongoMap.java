package com.waridley.mongo;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import org.bson.BsonDocumentReader;
import org.bson.Document;
import org.bson.codecs.DecoderContext;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MongoMap<V> extends AbstractMap<String, V> {
	
	private Map<String, V> cache = new HashMap<>();
	private Class<V> valueClass;
	private MongoCollection<Document> collection;
	
	public MongoMap(MongoCollection<Document> collection, Class<V> valueClass) {
		this.valueClass = valueClass;
		this.collection = collection;
	}
	
	@Override
	public V put(String key, Object value) {
		Document storedMap = collection.findOneAndUpdate(
				Filters.eq("valueClass", valueClass.toString()),
				Updates.combine(
						Updates.setOnInsert("valueClass", valueClass.toString()),
						Updates.set(key, value)
				),
				new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.BEFORE)
		);
		if(storedMap != null) {
			return collection.getCodecRegistry().get(valueClass).decode(
					new BsonDocumentReader(storedMap.get(key, Document.class).toBsonDocument(valueClass, collection.getCodecRegistry())),
					DecoderContext.builder().build()
			);
		} else {
			return null;
		}
	}
	
	@Override
	public Set<Entry<String, V>> entrySet() {
		FindIterable<Document> mapDocs = collection.find(Filters.eq("valueClass", valueClass.toString()), Document.class);
		for(Document d : mapDocs) {
			Set<Entry<String, Object>> storedSet = d.entrySet();
			for(Entry<String, Object> entry : storedSet) {
				if(entry.getValue() instanceof Document) {
					V value = collection.getCodecRegistry().get(valueClass).decode(
							new BsonDocumentReader(((Document) entry.getValue()).toBsonDocument(valueClass, collection.getCodecRegistry())),
							DecoderContext.builder().build()
					);
					cache.put(entry.getKey(), value);
				}
			}
		}
		
		return cache.entrySet();
	}
	
	@Override
	public V get(Object key) {
		Document doc = collection.find(
					Filters.and(
							Filters.eq("valueClass", valueClass.toString()),
							Filters.exists(String.valueOf(key))
					)
		).first();
		if(doc != null) {
			return collection.getCodecRegistry().get(valueClass).decode(
					new BsonDocumentReader(doc.get(String.valueOf(key), Document.class).toBsonDocument(valueClass, collection.getCodecRegistry())),
					DecoderContext.builder().build()
			);
		} else {
			return null;
		}
	}
	
}
