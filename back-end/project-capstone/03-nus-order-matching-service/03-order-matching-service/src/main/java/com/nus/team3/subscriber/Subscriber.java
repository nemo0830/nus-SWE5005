package com.nus.team3.subscriber;

import com.nus.team3.controller.OrderMatchingController;
import com.nus.team3.dao.TransactionDao;
import com.nus.team3.dto.Order;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.aws.messaging.config.annotation.EnableSqs;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;

import static com.nus.team3.utils.Utils.constructOrder;

@Component
@EnableSqs
public class Subscriber {

	private static final Logger logger = LoggerFactory.getLogger(Subscriber.class);

	@Autowired
	OrderMatchingController orderMatchingController;

	@Autowired
	@Qualifier("mysqlSqlSessionTemplate")
	private SqlSessionTemplate sqlSessionTemplate;

	@SqsListener("order_matching_buyer_queue.fifo")
	public void receiveBuyMessage(String message) {
		logger.info("Message {} Received from order_matching_buyer_queue.fifo", message);
		Order order = constructOrder(message);
		if (order != null){
			logger.info("AUDIT: created_by: [{}], created_at: [{}]", order.getUser(), order.getTimestamp());
			sqlSessionTemplate.insert(TransactionDao.rootMapperPath + TransactionDao.saveTxnQuery, order);
			logger.info("Buy order {} successfully persisted to db, proceed to matching.", order.getTransactionId());
			orderMatchingController.processOrder(order);
		}
	}

	@SqsListener("order_matching_seller_queue.fifo")
	public void receiveSellMessage(String message) {
		logger.info("Message {} Received from order_matching_seller_queue.fifo", message);
		Order order = constructOrder(message);
		if (order != null){
			logger.info("AUDIT: created_by: [{}], created_at: [{}]", order.getUser(), order.getTimestamp());
			sqlSessionTemplate.insert(TransactionDao.rootMapperPath + TransactionDao.saveTxnQuery, order);
			logger.info("Sell order {} successfully persisted to db, proceed to matching.", order.getTransactionId());
			orderMatchingController.processOrder(order);
		}
	}

}
