package com.example.demo.consul;

import java.io.Serializable;
import java.util.List;

public class ConsulCommod implements Serializable
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7779285795955625829L;

	private List<ConsulDataRow> list;
	
	private String node;
	
	public List<ConsulDataRow> getList()
	{
		return list;
	}
	
	public void setList(List<ConsulDataRow> list)
	{
		this.list = list;
	}
	
	public ConsulCommod(List<ConsulDataRow> list, String node)
	{
		super();
		this.list = list;
		this.node = node;
	}

	public String getNode()
	{
		return node;
	}
	
	public void setNode(String node)
	{
		this.node = node;
	}

	public ConsulCommod()
	{
		super();
	}

}
