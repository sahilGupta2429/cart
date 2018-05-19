package com.trakshaya.daoimpl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.trakshaya.dao.OrderDAO;
import com.trakshaya.dao.ProductDAO;
import com.trakshaya.entity.Order;
import com.trakshaya.entity.OrderDetail;
import com.trakshaya.entity.Product;
import com.trakshaya.model.CartInfo;
import com.trakshaya.model.CartLineInfo;
import com.trakshaya.model.CustomerInfo;
import com.trakshaya.model.OrderDetailInfo;
import com.trakshaya.model.OrderInfo;
import com.trakshaya.model.PaginationResult;

@Transactional
public class OrderDaoImpl implements OrderDAO{

	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private ProductDAO productDao;
	
	
	private int getMaxOrderNum() {
		String sql = "select max(o.orderNum) from "+Order.class.getName() +" o ";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery(sql);
		Integer value = (Integer)query.uniqueResult();
		return (value==null)?0:value;
	}
	
	@Override
	public void saveOrder(CartInfo cartInfo) {
		Session session = sessionFactory.getCurrentSession();
		int orderNum = this.getMaxOrderNum();
		Order order = new Order();
		
		order.setId(UUID.randomUUID().toString());
		order.setOrderNum(orderNum);
		order.setOrderDate(new Date());
		order.setAmount(cartInfo.getAmountTotal());
	
		CustomerInfo customerInfo = cartInfo.getCustomerInfo();
		order.setCustomerName(customerInfo.getName());
		order.setCustomerEmail(customerInfo.getEmail());
		order.setCustomerPhone(customerInfo.getPhone());
		order.setCustomerAddress(customerInfo.getAddress());

		session.persist(order);
		
		List<CartLineInfo> lines = cartInfo.getCartLines();
		
		for(CartLineInfo line : lines) {
			OrderDetail detail = new OrderDetail();
			detail.setId(UUID.randomUUID().toString());
			detail.setOrder(order);
			detail.setAmount(line.getAmount());
			detail.setPrice(line.getAmount());
			detail.setQuantity(line.getQuantity());
			
			String code = line.getProductInfo().getCode();
			Product product = this.productDao.findProduct(code);
			detail.setProduct(product);
			
			session.persist(detail);
		}
		
		cartInfo.setOrderNum(orderNum);
	}
	
	
	@Override
	public PaginationResult<OrderInfo> listOrderInfo(int page, int maxResult, int maxNavigationPage) {
		String sql = "Select new " + OrderInfo.class.getName()
				+ " (ord.id, ord.orderDate,ord.orderNum,ord.amount, "
				+ " ord.customerName, ord.customerAddress, ord.customerEmail, "
				+ " ord.customerPhone)" + "from"
				+ Order.class.getName() + " ord"
				+ " order by ord.orderNum desc";
		
		Session session = this.sessionFactory.getCurrentSession();
		Query query = session.createQuery(sql);
		
		return new PaginationResult<OrderInfo>(query,page,maxResult,maxNavigationPage);
	}

	//find order
	public Order findOrder(String orderId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(Order.class);
		crit.add(Restrictions.eq("id",orderId));
		return (Order) crit.uniqueResult();
	}
	
	
	@Override
	public OrderInfo getOrderInfo(String orderId) {
		Order order = this.findOrder(orderId);
		if(order==null) {
			return null;
		}
		return new OrderInfo(order.getId(), order.getOrderDate(),
				order.getOrderNum(), order.getAmount(), order.getCustomerName(),
				order.getCustomerAddress(),order.getCustomerEmail(),
				order.getCustomerPhone());
	}

	@Override
	public List<OrderDetailInfo> listOrderDetailInfos(String orderId) {
		String sql = "Select new " + OrderDetailInfo.class.getName()
			 + " (d.id, d.productCode, d.productName, d.quantity, d.price, d.amount)"
			 + " from "+ OrderDetail.class.getName() + " d"
			 + " where d.order.id = :orderId";
		
		Session session = this.sessionFactory.getCurrentSession();
		
		Query query = session.createQuery(sql);
		query.setParameter("orderId", orderId);
		
		return query.list();
	}
}
