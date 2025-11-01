package com.aidredaline.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootApplication
public class AiDreDalineBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiDreDalineBeApplication.class, args);
	}

	@Bean
	CommandLineRunner testConnection(DataSource dataSource) {
		return args -> {
			System.out.println("\n==================================");
			System.out.println("PostgreSQL 연결 테스트 시작...");
			System.out.println("==================================\n");

			try (Connection conn = dataSource.getConnection()) {
				System.out.println("PostgreSQL 연결 성공!");
				System.out.println("Database: " + conn.getMetaData().getDatabaseProductName());
				System.out.println("Version: " + conn.getMetaData().getDatabaseProductVersion());
				System.out.println("Database Name: adrenaline");
				System.out.println("URL: " + conn.getMetaData().getURL());

				// PostGIS 확인
				var stmt = conn.createStatement();
				var rs = stmt.executeQuery("SELECT PostGIS_Version()");
				if (rs.next()) {
					System.out.println("PostGIS Version: " + rs.getString(1));
				}

				System.out.println("\n==================================");
				System.out.println("모든 연결 테스트 완료!!!!!!!!!");
				System.out.println("==================================\n");
			} catch (Exception e) {
				System.err.println("\n==================================");
				System.err.println("Database 연결 실패!");
				System.err.println("==================================");
				System.err.println("에러 메시지: " + e.getMessage());
				e.printStackTrace();
				System.err.println("==================================\n");
			}
		};
	}
}
