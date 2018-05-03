package com.example.demo.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
public class ApiController
{
	
	private static Logger log = Logger.getLogger(ApiController.class);
	
	@Autowired
	private ConsulClient consulClient;
	
	@Autowired
	private DiscoveryClient discoveryClient;
	
	@ApiOperation(value = "服务实例遍历", notes = "服务实例遍历")
	@RequestMapping(value = "/allservicer", method = RequestMethod.GET)
	public String getAllServicer(String address)
	{
		log.info("***********************consul上服务查询开始*******************************************");
		//获取所有的members的信息 [service/client]
		List<Member> members = consulClient.getAgentMembers().getValue();
		List<ConsulCommod> list = new ArrayList<>();
		if (address.length() == 0 || address == "" || address.equals("null"))
		{
			List<ConsulDataRow> resultList = null;
			for (int i = 0; i < members.size(); i++)
			{
				address = members.get(i).getAddress();
				ConsulCommod commod = new ConsulCommod();
				//根据role变量获取每个member的角色  role：consul---代表服务端   role：node---代表客户端  
				String role = members.get(i).getTags().get("role");
				//判断是否为consul   
				if (role.equals("node") || role.equals("consul"))
				{
					resultList = getConsulServerData(address, "get", "");
					commod.setNode(address);
					commod.setList(resultList);
					list.add(commod);
				}
			}
		}
		else
		{
			ConsulCommod commod = new ConsulCommod();
			List<ConsulDataRow> resultList = getConsulServerData(address, "get", "");
			commod.setList(resultList);
			commod.setNode(address);
			list.add(commod);
		}
		return JsonUtil.writeValue(list);
	}
	
	@ApiOperation(value = "服务实例注销", notes = "服务实例注销")
	@RequestMapping("/deregister")
	public String agentServiceDeregister(String address, String serverName)
	{
		List<ConsulCommod> list = new ArrayList<>();
		try
		{
			//获取所有的members的信息 [service/client]
			List<Member> members = consulClient.getAgentMembers().getValue();
			
			if (address.length() == 0 || address == "" || address.equals("null"))
			{
				List<ConsulDataRow> resultList = null;
				for (int i = 0; i < members.size(); i++)
				{
					ConsulCommod dataRow = new ConsulCommod();
					address = members.get(i).getAddress();
					//根据role变量获取每个member的角色  role：consul---代表服务端   role：node---代表客户端  
					String role = members.get(i).getTags().get("role");
					//判断是否为consul   
					if (role.equals("node") || role.equals("consul"))
					{
						resultList = getConsulServerData(address, "delete", serverName);
						dataRow.setList(resultList);
						dataRow.setNode(address);
						list.add(dataRow);
					}
				}
			}
			else
			{
				List<ConsulDataRow> resultList = getConsulServerData(address, "delete", serverName);
				ConsulCommod dataRow = new ConsulCommod();
				dataRow.setList(resultList);
				dataRow.setNode(address);
				list.add(dataRow);
			}
		}
		catch (Exception e)
		{
			System.out.println("delete happened exception");
		}
		return JsonUtil.writeValue(list);
	}
	
	@ApiOperation(value = "服务实例明细获取", notes = "服务实例明细获取")
	public List<ConsulDataRow> getConsulServerData(String address, String type, String dealSserviceName)
	{
		List<ConsulDataRow> resultList = new ArrayList<>();
		try
		{
			//将IP地址传给ConsulClient的构造方法，获取对象  
			ConsulClient clearClient = new ConsulClient(address);
			//根据clearClient，获取当前IP下所有的服务 使用迭代方式 获取map对象的值  
			Iterator<Map.Entry<String, Service>> it = clearClient.getAgentServices().getValue().entrySet().iterator();
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
				ConsulDataRow row = new ConsulDataRow();
				row.setServiceId(serviceId);
				row.setServiceName(serviceName);
				row.setStatus(checkStatus.name());
				if (type.equals("delete"))
				{
					if (serviceName.equals(dealSserviceName))
					{
						System.out.println("在{}" + address + "客户端上的服务 :{}" + serviceName + "为无效服务，准备清理...................");
						clearClient.agentServiceDeregister(serviceId);
						row.setDealStatus("success");
						resultList.add(row);
					}
				}
				else
				{
					resultList.add(row);
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("" + e.getMessage());
		}
		return resultList;
	}
	
	@RequestMapping("/checkStatus")
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
