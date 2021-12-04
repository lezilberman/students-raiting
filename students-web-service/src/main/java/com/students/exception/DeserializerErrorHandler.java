package com.students.exception;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.errors.DeserializationExceptionHandler;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeserializerErrorHandler implements DeserializationExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DeserializerErrorHandler.class);
	
//	@Override
	public DeserializationHandlerResponse handle(ProcessorContext context, ConsumerRecord<byte[], byte[]> record, Exception exception) {
        LOG.error("Received a deserialize error for {} cause {}", record, exception);
		return DeserializationHandlerResponse.CONTINUE;
	}
	
//	@Override
	public void configure(Map<String, ?> arg0) {

	} 
}
