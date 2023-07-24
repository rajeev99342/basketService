package com.service;

import com.service.constants.values.IpAddress;
import com.service.service.homepage.HomePageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@ComponentScan(basePackages = {"com.service.*"})
@EnableJpaRepositories
@PropertySource("classpath:application.yaml")
//@EnableDiscoveryClient
public class ServiceApplication  implements CommandLineRunner {

	@Value("${spring.datasource.url}")
	private  String mysql;

	@Autowired
	HomePageHandler homePageHandler;

	@Autowired
	IpAddress address;
	public static void main(String[] args) {
		SpringApplication.run(ServiceApplication.class, args);
	}

	@Bean
	public CorsFilter corsFilter() {
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		final CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);

		List<String> pattern = new ArrayList<>();
//		pattern.add("http://localhost:8100");
		pattern.add("http://*");
		pattern.add("https://*");

//		pattern.add(address.getAddress(IpAddress.RAJEEV_MOBILE_IP));
		config.setAllowedOriginPatterns(pattern);
		config.addAllowedHeader("*");
		config.addAllowedMethod("OPTIONS");
		config.addAllowedMethod("HEAD");
		config.addAllowedMethod("GET");
		config.addAllowedMethod("PUT");
		config.addAllowedMethod("POST");
		config.addAllowedMethod("DELETE");
		config.addAllowedMethod("PATCH");
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}


	@Override
	public void run(String... args) throws Exception {
//		homePageHandler.putHomePageData();
		System.out.println("++++++++++++++++++++++++++++++ Melaa Grocery Store +++++++++++++++++++++++++++++++++++");
//		System.out.println(mysql);
	}
}
