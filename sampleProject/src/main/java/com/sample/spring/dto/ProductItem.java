package com.sample.spring.dto;

public class ProductItem {
    private String categoryType;
    private String categoryId;
    private String uid;
    private String name;
    private String payReferrer;
    private int count;
    
    

    public ProductItem() {
		super();
		// TODO Auto-generated constructor stub
	}

	// 생성자
    public ProductItem(String categoryType, String categoryId, String uid, String name, String payReferrer, int count) {
        this.categoryType = categoryType;
        this.categoryId = categoryId;
        this.uid = uid;
        this.name = name;
        this.payReferrer = payReferrer;
        this.count = count;
    }

	public String getCategoryType() {
		return categoryType;
	}

	public void setCategoryType(String categoryType) {
		this.categoryType = categoryType;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPayReferrer() {
		return payReferrer;
	}

	public void setPayReferrer(String payReferrer) {
		this.payReferrer = payReferrer;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
    
    

}