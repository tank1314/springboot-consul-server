package com.example.demo.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.consul.ConsulEndpoint.ConsulData;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baozun.utilities.json.JsonUtil;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Member;
import com.ecwid.consul.v1.agent.model.Service;
import com.ecwid.consul.v1.health.model.Check;
import com.ecwid.consul.v1.health.model.Check.CheckStatus;
import com.example.demo.consul.ConsulCommod;
import com.example.demo.consul.ConsulDataRow;
import com.wordnik.swagger.annotations.ApiOperation;

/**
 * 查询consul服务端相关的consul服务 且删除无效服务
 * @author kun.tan
 *
 */
@RestController
public class ApiController2
{
	
	private static Logger log = Logger.getLogger(ApiController2.class);
	
	@Autowired
	private ConsulClient consulClient;
	
	@Autowired
	private DiscoveryClient discoveryClient;
	
	@ApiOperation(value = "剔除所有无效的服务实例", notes = "剔除所有无效的服务实例")
	@RequestMapping(value = "/allservicers", method = RequestMethod.GET)
	public String getAllServicer(String address)
	{
		log.info("***********************consul上无效服务清理开始*******************************************");
		//获取所有的members的信息 [service/client]
		List<Member> members = consulClient.getAgentMembers().getValue();
		List<ConsulCommod> resultList = new ArrayList<>();
		if (address.length() == 0 || address == "")
		{
			for (int i = 0; i < members.size(); i++)
			{
				address = members.get(i).getAddress();
				//根据role变量获取每个member的角色  role：consul---代表服务端   role：node---代表客户端  
				String role = members.get(i).getTags().get("role");
				//判断是否为consul   
				if (role.equals("node") || role.equals("consul"))
				{
					//将IP地址传给ConsulClient的构造方法，获取对象  
					ConsulClient clearClient = new ConsulClient(address);
					//根据clearClient，获取当前IP下所有的服务 使用迭代方式 获取map对象的值  
					Iterator<Map.Entry<String, Service>> it = clearClient.getAgentServices().getValue().entrySet().iterator();
					ConsulCommod comod = new ConsulCommod();
					comod.setNode(address);
					List<ConsulDataRow> listRow = new ArrayList<>();
					while (it.hasNext())
					{
						//迭代数据  
						Map.Entry<String, Service> serviceMap = it.next();
						//获得Service对象  
						Service service = serviceMap.getValue();
						//获取服务名称  
						String serviceName = service.getService();
						//获取服务ID  
						String serviceId = service.getId();
						//根据服务名称获取服务的健康检查信息  
						Response<List<Check>> checkList = clearClient.getHealthChecksForService(serviceName, null);
						List<Check> checks = checkList.getValue();
						//获取健康状态值  PASSING：正常  WARNING  CRITICAL  UNKNOWN：不正常  
						Check.CheckStatus checkStatus = checks.get(0).getStatus();
						System.out.println("在{}" + address + "客户端上的服务名称 :{}" + serviceName + "**服务ID:{}" + serviceId + " 状态：" + checkStatus);
						ConsulDataRow row = new ConsulDataRow();
						row.setServiceId(serviceId);
						row.setServiceName(serviceName);
						row.setStatus(checkStatus.name());
						listRow.add(row);
						/*if ((serviceName.equals("app-consule-server") || serviceName.equals("app-IP-w2erp-service-pro")))
						{
							System.out.println("在{}" + address + "客户端上的服务 :{}" + serviceName + "为无效服务，准备清理...................");
							clearClient.agentServiceDeregister(serviceId);
						}*/
					}
					comod.setList(listRow);
					resultList.add(comod);
				}
			}
		}
		return JsonUtil.writeValue(resultList);
	}
	
	public List<ConsulCommod> getData(String address)
	{
		List<ConsulCommod> resultList = new ArrayList<>();
		try
		{
			//将IP地址传给ConsulClient的构造方法，获取对象  
			ConsulClient clearClient = new ConsulClient(address);
			//根据clearClient，获取当前IP下所有的服务 使用迭代方式 获取map对象的值  
			Iterator<Map.Entry<String, Service>> it = clearClient.getAgentServices().getValue().entrySet().iterator();
			ConsulCommod comod = new ConsulCommod();
			comod.setNode(address);
			List<ConsulDataRow> listRow = new ArrayList<>();
			while (it.hasNext())
			{
				//迭代数据  
				Map.Entry<String, Service> serviceMap = it.next();
				//获得Service对象  
				Service service = serviceMap.getValue();
				//获取服务名称  
				String serviceName = service.getService();
				//获取服务ID  
				String serviceId = service.getId();
				//根据服务名称获取服务的健康检查信息  
				Response<List<Check>> checkList = clearClient.getHealthChecksForService(serviceName, null);
				List<Check> checks = checkList.getValue();
				//获取健康状态值  PASSING：正常  WARNING  CRITICAL  UNKNOWN：不正常  
				Check.CheckStatus checkStatus = checks.get(0).getStatus();
				System.out.println("在{}" + address + "客户端上的服务名称 :{}" + serviceName + "**服务ID:{}" + serviceId + " 状态：" + checkStatus);
				ConsulDataRow row = new ConsulDataRow();
				row.setServiceId(serviceId);
				row.setServiceName(serviceName);
				row.setStatus(checkStatus.name());
				listRow.add(row);
				/*if ((serviceName.equals("app-consule-server") || serviceName.equals("app-IP-w2erp-service-pro")))
				{
					System.out.println("在{}" + address + "客户端上的服务 :{}" + serviceName + "为无效服务，准备清理...................");
					clearClient.agentServiceDeregister(serviceId);
				}*/
			}
			comod.setList(listRow);
			resultList.add(comod);
		}
		catch (Exception e)
		{
			System.out.println("" + e.getMessage());
		}
		return resultList;
	}
	
	@RequestMapping("/checkStatuss")
	public String getServiceStatus()
	{
		
		Iterator<ServiceInstance> serviceNodeList = discoveryClient.getInstances("app-eca-seq-and-code-dev-1").iterator();
		while (serviceNodeList.hasNext())
		{
			String host = serviceNodeList.next().getHost();
			int port = serviceNodeList.next().getPort();
			System.out.println("++++uri++" + host + "+++port++" + port);
		}
		
		Response<List<Check>> checkList = consulClient.getHealthChecksForService("app-eca-seq-and-code-dev-1", null);
		List<Check> checks = checkList.getValue();
		for (Check check : checks)
		{
			CheckStatus checkStatus = check.getStatus();
			check.getServiceId();
			System.out.println("+++" + checkStatus);
		}
		/* Check.CheckStatus checkStatus = checks.get(0).getStatus();  
		if (checkStatus != Check.CheckStatus.PASSING){  
			//clearClient.agentServiceDeregister(serviceId);  
			System.out.println("正常...");
		}  */
		return "";
	}
	
}
