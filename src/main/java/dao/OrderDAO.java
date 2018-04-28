package dao;

import java.util.List;

import model.CartInfo;
import model.OrderDetailInfo;
import model.OrderInfo;
import model.PaginationResult;

public interface OrderDAO {

	public void saveOrder(CartInfo cartInfo);
	
	public PaginationResult<OrderInfo> listOrderInfo(int page,
			int maxResult, int maxNavigationPage);

	public OrderInfo getOrderInfo(String orderId);
	
	public List<OrderDetailInfo> listOrderDetailInfos(String orderId);
	
}
