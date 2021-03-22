package com.students.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StudentProducerConfig {
	@Bean(name="com.students.config.StudentKeyExtractor")
	public StudentKeyExtractor extractor() {
		return new StudentKeyExtractor();
	}
}
