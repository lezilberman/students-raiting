package com.students.config;

import org.apache.kafka.streams.processor.StreamPartitioner;

import com.students.model.Student;

public class StudentPartitioner implements StreamPartitioner<Integer, Student> {

	public Integer partition(String topic, Integer key, Student value, int numPartitions) {
		return (int) (key % numPartitions);
	}

}
