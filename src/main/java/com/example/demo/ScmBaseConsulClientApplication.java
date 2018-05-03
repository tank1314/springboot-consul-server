package com.example.demo;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.ConsulRawClient;
import com.ecwid.consul.v1.agent.model.Service;

@SpringBootApplication
@EnableDiscoveryClient
@RestController
public class ScmBaseConsulClientApplication {

	/*@Autowired
	private LoadBalancerClient loadBalancerClient;*/

	@Autowired
	private DiscoveryClient discoveryClient;

	/**
	 * 当Ribbon与Eureka联合使用时，ribbonServerList会被DiscoveryEnabledNIWSServerList重写，
	 * 扩展成从Eureka注册中心中获取服务实例列表。同时它也会用NIWSDiscoveryPing来取代IPing，它将职责委托给Eureka来确定服务端是否已经启动。
	 * 
	 * 而当Ribbon与Consul联合使用时，ribbonServerList会被ConsulServerList来扩展成从Consul获取服务实例列表。同时由ConsulPing来作为IPing接口的实现。
	 * @return
	 */
	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	/**
	 * 从所有服务中选择一个[tomcat]服务（轮询）
	 */
	/*@RequestMapping("/discover")
	public String discover() {
		return loadBalancerClient.choose("tomcat").getUri().toString();
	}*/

	@RequestMapping("/services")
	public void service() {
		//return discoveryClient.getInstances("tomcat");
		List<String> serviceList = discoveryClient.getServices();
		for(String serviceName:serviceList) {
			Iterator<ServiceInstance> serviceNodeList = discoveryClient.getInstances(serviceName).iterator() ;
			while(serviceNodeList.hasNext()) {
				String host = serviceNodeList.next().getHost() ;
				int port = serviceNodeList.next().getPort() ;
				URI uri = serviceNodeList.next().getUri() ;
			}
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(ScmBaseConsulClientApplication.class, args);
		ConsulRawClient client = new ConsulRawClient("10.88.26.128", 8500);
		ConsulClient consul = new ConsulClient(client);
		// 获取所有服务
		Map<String, Service> map = consul.getAgentServices().getValue();
		System.out.println(map.get("++"));
	}
}
