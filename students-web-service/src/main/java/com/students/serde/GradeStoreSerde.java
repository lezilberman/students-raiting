package com.students.serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import com.students.model.GradeStore;

public class GradeStoreSerde implements Serde<GradeStore> {

    private Serializer<GradeStore> serializer;
    private Deserializer<GradeStore> deserializer;
	
	private GradeStoreSerde() {
		this.serializer = new ProtoGradeStoreSerializer();
		this.deserializer = new ProtoGradeStoreDeserializer();
	}

	public Deserializer<GradeStore> deserializer() {
		return deserializer;
	}

	public Serializer<GradeStore> serializer() {
		return serializer;
	}
	
    public static Serde<GradeStore> GradeStoresSerde() {
        return new GradeStoreSerde();
    }
	
}
