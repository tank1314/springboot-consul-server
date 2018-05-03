package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class IndexController
{
	
	@Autowired
	RestTemplate template;
	
	@Autowired
	LoadBalancerClient loadBalancerClient;
	
	@GetMapping("/index")
	public String index()
	{
		
		/*ServiceInstance serviceInstance = loadBalancerClient.choose("app-consule-server");
		String url = "http://" + serviceInstance.getHost() + ":" + serviceInstance.getPort() + "/index";
		System.out.println(url);*/
		//tomcat 为服务提供者注册在consul上的名称[service-name]
		/*String url = "http://10.88.107.14:5201/consumer/groupList.query";
		String data = this.template.getForObject(url, String.class);
		System.out.println("data+++" + data);*/
		return this.template.getForObject("http://tomcat/userInfo", String.class);
	}
	
	// 能否访问数据库的标识
	public static boolean canVisitDb = true;
	
	@RequestMapping(value = "/db/{can}", method = RequestMethod.GET)
	public void setDb(@PathVariable boolean can)
	{
		canVisitDb = can;
	}
	
}
