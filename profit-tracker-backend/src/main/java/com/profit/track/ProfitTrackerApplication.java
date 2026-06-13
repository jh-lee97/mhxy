package com.profit.track;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.profit.track.mapper")
public class ProfitTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProfitTrackerApplication.class, args);
    }
}
