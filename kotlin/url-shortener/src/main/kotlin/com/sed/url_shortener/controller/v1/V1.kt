package com.sed.url_shortener.controller.v1

import com.sed.url_shortener.controller.Base
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1")
class V1 : Base()