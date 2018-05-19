package com.trakshaya.dao;

import java.util.List;

import com.trakshaya.model.CartInfo;
import com.trakshaya.model.OrderDetailInfo;
import com.trakshaya.model.OrderInfo;
import com.trakshaya.model.PaginationResult;

public interface OrderDAO {

	public void saveOrder(CartInfo cartInfo);
	
	public PaginationResult<OrderInfo> listOrderInfo(int page,
			int maxResult, int maxNavigationPage);

	public OrderInfo getOrderInfo(String orderId);
	
	public List<OrderDetailInfo> listOrderDetailInfos(String orderId);
	
}
