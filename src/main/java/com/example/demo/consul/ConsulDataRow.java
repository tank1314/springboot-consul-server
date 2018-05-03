package com.example.demo.consul;

import java.io.Serializable;

public class ConsulDataRow implements Serializable
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1845129968066492656L;

	private String serviceName;
	
	private String serviceId;
	
	private String status;
	
	private String dealStatus ;
	
	public String getServiceName()
	{
		return serviceName;
	}
	
	public void setServiceName(String serviceName)
	{
		this.serviceName = serviceName;
	}
	
	public String getServiceId()
	{
		return serviceId;
	}
	
	public void setServiceId(String serviceId)
	{
		this.serviceId = serviceId;
	}
	
	public String getStatus()
	{
		return status;
	}
	
	public void setStatus(String status)
	{
		this.status = status;
	}
	
	public String getDealStatus()
	{
		return dealStatus;
	}

	
	public void setDealStatus(String dealStatus)
	{
		this.dealStatus = dealStatus;
	}

	public ConsulDataRow(String serviceName, String serviceId, String status)
	{
		super();
		this.serviceName = serviceName;
		this.serviceId = serviceId;
		this.status = status;
	}

	public ConsulDataRow()
	{
		super();
	}
	
}
