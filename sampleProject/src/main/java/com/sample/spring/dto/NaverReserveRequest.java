package com.sample.spring.dto;

import java.util.List;

public class NaverReserveRequest {
	
    public NaverReserveRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	private String modelVersion;
	private String merchantUserKey;
    private String merchantPayKey;
    private String productName;
    private int productCount;
    private int totalPayAmount;
    private String returnUrl;
    private int taxScopeAmount;
    private int taxExScopeAmount;
    private int environmentDepositAmount;
    private String purchaserName;
    private String purchaserBirthday;
    private List<ProductItem> productItems;
    
    public String getModelVersion() {
		return modelVersion;
	}
	public void setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
	}
	public String getMerchantUserKey() {
		return merchantUserKey;
	}
	public void setMerchantUserKey(String merchantUserKey) {
		this.merchantUserKey = merchantUserKey;
	}
	public String getMerchantPayKey() {
		return merchantPayKey;
	}
	public void setMerchantPayKey(String merchantPayKey) {
		this.merchantPayKey = merchantPayKey;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public int getProductCount() {
		return productCount;
	}
	public void setProductCount(int productCount) {
		this.productCount = productCount;
	}
	public int getTotalPayAmount() {
		return totalPayAmount;
	}
	public void setTotalPayAmount(int totalPayAmount) {
		this.totalPayAmount = totalPayAmount;
	}
	public String getReturnUrl() {
		return returnUrl;
	}
	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}
	public int getTaxScopeAmount() {
		return taxScopeAmount;
	}
	public void setTaxScopeAmount(int taxScopeAmount) {
		this.taxScopeAmount = taxScopeAmount;
	}
	public int getTaxExScopeAmount() {
		return taxExScopeAmount;
	}
	public void setTaxExScopeAmount(int taxExScopeAmount) {
		this.taxExScopeAmount = taxExScopeAmount;
	}
	public int getEnvironmentDepositAmount() {
		return environmentDepositAmount;
	}
	public void setEnvironmentDepositAmount(int environmentDepositAmount) {
		this.environmentDepositAmount = environmentDepositAmount;
	}
	public String getPurchaserName() {
		return purchaserName;
	}
	public void setPurchaserName(String purchaserName) {
		this.purchaserName = purchaserName;
	}
	public String getPurchaserBirthday() {
		return purchaserBirthday;
	}
	public void setPurchaserBirthday(String purchaserBirthday) {
		this.purchaserBirthday = purchaserBirthday;
	}
	public List<ProductItem> getProductItems() {
		return productItems;
	}
	public void setProductItems(List<ProductItem> productItems) {
		this.productItems = productItems;
	}

    // Getter, Setter, 생성자 생략 (Jackson 변환용 필수)
}