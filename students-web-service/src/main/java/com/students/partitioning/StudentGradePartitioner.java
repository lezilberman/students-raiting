package com.students.partitioning;

import org.apache.kafka.streams.processor.StreamPartitioner;

import com.students.model.StudentGrade;


public class StudentGradePartitioner implements StreamPartitioner<Integer, StudentGrade> {

	public Integer partition(String topic, Integer key, StudentGrade value, int numPartitions) {
		return (int) (key % numPartitions);
	}

}
