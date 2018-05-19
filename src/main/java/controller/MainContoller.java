package controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dao.OrderDAO;
import dao.ProductDAO;
import entity.Product;
import model.CartInfo;
import model.CustomerInfo;
import model.PaginationResult;
import model.ProductInfo;
import utils.Utils;
import validators.CustomerInfoValidator;

@Controller
@Transactional
@EnableWebMvc
@RequestMapping(value="pages")
public class MainContoller {

	@Autowired
	private OrderDAO orderDAO;
	
	@Autowired
	private ProductDAO productDAO;
	
	@Autowired
	private  CustomerInfoValidator customerInfoValidator;
	
	@InitBinder
	public void myinitBinder(WebDataBinder dataBinder) {
		Object target = dataBinder.getTarget();
		if(target == null) {
			return;
		}
		System.out.println("Target ="+target);
		
		if(target.getClass() == CartInfo.class) {
			
		}else if(target.getClass() == CustomerInfo.class) {
			dataBinder.setValidator(customerInfoValidator);
		}
		
	}
	
	@RequestMapping("/403")
	public String accessDenied() {
		return "/403";
	}
	
	@RequestMapping("/cart")
	public String home(){
		return "index";
	}
	
	@RequestMapping("/")
	public String home2(){
		return "index";
	}
	
	@RequestMapping({"/productList"})
	public String listProductHandler(Model model,
		@RequestParam(value = "name", defaultValue="")String likeName,
		@RequestParam(value = "page", defaultValue="1")int page){
			
			final int maxResult = 5;
			final int maxNavigationPage = 10;
			
			PaginationResult<ProductInfo> result = productDAO.queryProducts(page, maxResult, maxNavigationPage);
			model.addAttribute("PaginationProducts",result);
			return "productList";
	}
	
	@RequestMapping({"/buyProduct"})
	public String listProductHandler(HttpServletRequest request,Model model,
				@RequestParam(value = "code",defaultValue = "")String code) {
		
		Product product = null;
		if(code!=null && code.length()>0) {
			product = productDAO.findProduct(code);
		}
		
		if(product!=null) {
			CartInfo cartInfo = Utils.getCartInSession(request);
			ProductInfo productInfo = new ProductInfo(product);
			cartInfo.addProduct(productInfo,1);
		}
		return "redirect:/shoppingCart";
	}
	
	@RequestMapping({"/shoppingCartRemoveProduct"})
	public String removeProductHandler(HttpServletRequest request,Model model,
		@RequestParam(value = "code",defaultValue="")String code){
		
		Product product = null;
		if(code!=null && code.length()>0) {
			product = productDAO.findProduct(code);
		}
		
		if(product!=null) {
			CartInfo cartInfo = Utils.getCartInSession(request);
			ProductInfo productInfo = new ProductInfo(product);
			cartInfo.removeProduct(productInfo);
		}
		
		return "redirect:/shoppingCart";
	}
	
	@RequestMapping(value = {"/shoppingCart"},method=RequestMethod.POST)
	public String shoppingCartUpdateQty(HttpServletRequest request,
			Model model,
			@ModelAttribute("cartForm")CartInfo cartForm) {
		
		CartInfo cartInfo = Utils.getCartInSession(request);
		
		cartInfo.updateQuantity(cartForm);
		
		return "redirect:/shoppingCart";
	}
	
		
	@RequestMapping(value= {"/shoppingCart"},method = RequestMethod.GET)
	public String shoppingCartHandler(HttpServletRequest request,Model model) {
		CartInfo cartInfo = Utils.getCartInSession(request);
		
		if(cartInfo.isEmpty()) {
			return "redirect:/shoppingCart'";
		}
		
		CustomerInfo customerInfo = cartInfo.getCustomerInfo();
		if(customerInfo == null) {
			customerInfo = new CustomerInfo();
		}
		
		model.addAttribute("customerForm",customerInfo);
		
		return "shoppingCartCustomer";
	}
	
	@RequestMapping(value= {"/shoppingCartCustomer"},method = RequestMethod.POST)
	public String shoppingCartCustomerSave(HttpServletRequest request,
			Model model,
			@ModelAttribute("customerForm")@Validated CustomerInfo customerForm,
			BindingResult result,
			final RedirectAttributes redirectAttributes) {
		
		if(result.hasErrors()) {
			customerForm.setValid(false);
			return "shoppingCartCustomer";
		}
		customerForm.setValid(true);
		CartInfo cartInfo = Utils.getCartInSession(request);
		cartInfo.setCustomerInfo(customerForm);
		
		return "redirect:/shoppingCartConfirmation";
	}

	@RequestMapping(value= {"/shoppingCartConfirmation"},method = RequestMethod.GET)
	public String shoppingCartConfirmationReview(HttpServletRequest request,Model model) {
		CartInfo cartInfo = Utils.getCartInSession(request);
		
		if(cartInfo.isEmpty()) {
			return "redirect:/shoppingCart";
		}else if(!cartInfo.isValidCustomer()) {
			return "redirect:/shoppingCartCustomer";
		}
		
		return "shoppingCartConfirmation";
	}
	
	@RequestMapping(value= {"/shoppingCartConfirmation"},method=RequestMethod.POST)
	@Transactional(propagation = Propagation.NEVER)
	public String shoppingCartConfirmationSave(HttpServletRequest request,Model model) {
		
		CartInfo cartInfo = Utils.getCartInSession(request);
		
		if(cartInfo.isEmpty()) {
			return "redirect:/shoppingCart";
		}else if(!cartInfo.isValidCustomer()) {
			return "redirect:/shoppingCartCustomer";
		}
		try {
			orderDAO.saveOrder(cartInfo);
		}catch(Exception ex) {
			ex.printStackTrace();
			return "shoppingCartConfirmation";
		}
		
		Utils.removeCartInSession(request);
		Utils.storeLastOrderedCartInSession(request, cartInfo);
		
		return "redirect:/shoppingCartFinalize";
	}
	
	@RequestMapping(value= {"/shoppingCartFinalize"},method=RequestMethod.GET)
	public String shoppingCartFinalize(HttpServletRequest request,Model model) {
	CartInfo lastOrderedCart = Utils.getLastOrderedCartInSession(request);
	
	if(lastOrderedCart == null) {
		return "redirect:/shoppingCart";
	}
	
		return "shoppingCartFinalize";
	}
	
	@RequestMapping(value= {"/productImage"},method=RequestMethod.GET)
	public void productImage(HttpServletRequest request,HttpServletResponse response,Model model,
				@RequestParam("code")String code)throws IOException{
	
		Product product = null;
		
		if(code != null) {
			product = this.productDAO.findProduct(code);
		}
		
		if(product!=null && product.getImage()!=null) {
			response.setContentType("image/jpeg,image/jpg,image/png,image/gif");
			response.getOutputStream().write(product.getImage());
		}
		response.getOutputStream().close();
	}
}
