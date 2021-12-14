package com.ipiecoles.java.mdd050.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/helloo")
public class HelloController {
    @RequestMapping(
            method = RequestMethod.GET,
            produces = "text/plain")

    public String hello(){
        return "hello to the world";
    }

// without routing in the top request mapping
    @RequestMapping(
            value = "/salam",
            method = RequestMethod.GET,
            produces = "text/html")
    public String helloHtml(){
        return "<p>Salam Roja</p>";
    }

}
