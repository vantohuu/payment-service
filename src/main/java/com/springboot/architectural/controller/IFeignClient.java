package com.springboot.architectural.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "serviceMainClient", url = "http://10.107.244.72:80/api/external")
public interface IFeignClient {
    @GetMapping(value = "/verify-token-external-service")
    String sendToken(@RequestHeader("Authorization") String serviceAuthToken);
}
