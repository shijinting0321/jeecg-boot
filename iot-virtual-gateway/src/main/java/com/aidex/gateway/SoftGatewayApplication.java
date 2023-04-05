package com.aidex.gateway;

import com.gitee.starblues.loader.launcher.SpringBootstrap;
import com.gitee.starblues.loader.launcher.SpringMainBootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SoftGatewayApplication implements SpringBootstrap {

    public static void main(String[] args) {
        SpringMainBootstrap.launch(SoftGatewayApplication.class, args);
    }

    @Override
    public void run(String[] args) throws Exception {
        SpringApplication.run(SoftGatewayApplication.class, args);
    }
}
