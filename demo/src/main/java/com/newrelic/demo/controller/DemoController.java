package com.newrelic.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    private final Logger logger = LoggerFactory.getLogger(DemoController.class);

    @GetMapping
    public ResponseEntity<String> checkHealth() {

        logger.info("Health check triggered.");

        return new ResponseEntity<String>("OK!", HttpStatus.OK);
    }
}
