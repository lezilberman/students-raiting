package com.students.restore;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.processor.StateRestoreListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LoggingStateRestoreListener implements StateRestoreListener {
	
    private static final Logger LOG = LoggerFactory.getLogger(LoggingStateRestoreListener.class);
    private final Map<TopicPartition, Long> totalToRestore = new ConcurrentHashMap<TopicPartition, Long>();
    private final Map<TopicPartition, Long> restoredSoFar = new ConcurrentHashMap<TopicPartition, Long>();
    
	public void onRestoreStart(TopicPartition topicPartition, String storeName, long start, long end) {
      long toRestore = end - start;
      totalToRestore.put(topicPartition, toRestore);
      LOG.info("Starting restoration for {} on topic-partition {} total to restore {}", storeName, topicPartition, toRestore);
	}
	
	public void onBatchRestored(TopicPartition topicPartition, String storeName, long batchEndOffset, long numRestored) {

      NumberFormat formatter = new DecimalFormat("#.##");
		
      long currentProgress = numRestored + restoredSoFar.getOrDefault(topicPartition, 0L);
      double percentComplete =  (double) currentProgress / totalToRestore.get(topicPartition);

      LOG.info("Completed {} for {}% of total restoration for {} on {}",
    		numRestored, formatter.format(percentComplete * 100.00), storeName, topicPartition);
      restoredSoFar.put(topicPartition, currentProgress);
      
	}
	
	public void onRestoreEnd(TopicPartition topicPartition, String storeName, long totalRestored) {
      LOG.info("Restoration completed for {} on topic-partition {}", storeName, topicPartition);
      restoredSoFar.put(topicPartition, 0L);		
	} 

}
