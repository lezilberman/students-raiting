package com.students.partitioning;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StudentProducerConfig {
	@Bean(name="com.students.partitioning.StudentKeyExtractor")
	public StudentKeyExtractor extractor() {
		return new StudentKeyExtractor();
	}
}
