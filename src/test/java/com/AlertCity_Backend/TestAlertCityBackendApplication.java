package com.AlertCity_Backend;

import org.springframework.boot.SpringApplication;

public class TestAlertCityBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(AlertCityBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
