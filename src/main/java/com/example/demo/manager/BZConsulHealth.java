package com.example.demo.manager;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import com.example.demo.controller.IndexController;

@Component
public class BZConsulHealth implements HealthIndicator
{
	
	public Health health()
	{
		if (IndexController.canVisitDb)
		{
			Health.down().withDetail("", "") ;
			return new Health.Builder(Status.UP).build();
		}
		else
		{
			return new Health.Builder(Status.DOWN).build();
		}
	}
	
}
