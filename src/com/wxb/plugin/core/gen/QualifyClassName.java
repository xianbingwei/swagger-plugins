package com.wxb.plugin.core.gen;

public interface QualifyClassName {
    // swagger
    String Api = "io.swagger.annotations.Api";
    String ApiModel = "io.swagger.annotations.ApiModel";
    String ApiModelProperty = "io.swagger.annotations.ApiModelProperty";
    String ApiOperation = "io.swagger.annotations.ApiOperation";

    // spring
    String RequestMapping = "org.springframework.web.bind.annotation.RequestMapping";
    String PostMapping = "org.springframework.web.bind.annotation.PostMapping";
    String PutMapping = "org.springframework.web.bind.annotation.PutMapping";
    String GetMapping = "org.springframework.web.bind.annotation.GetMapping";
    String DeleteMapping = "org.springframework.web.bind.annotation.DeleteMapping";

    String controller = "org.springframework.stereotype.Controller";
    String restController = "org.springframework.web.bind.annotation.RestController";

    String collection = "java.util.Collection";
}
