package com.kevin86.commons.springfox.swagger;

import org.springframework.context.annotation.Import;
import springfox.documentation.oas.configuration.OpenApiDocumentationConfiguration;
import springfox.documentation.spring.web.SpringfoxWebMvcConfiguration;
import java.lang.annotation.*;

/**
 * 默认MVC
 * @author kevinchen
 * @date 2016/12/14
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({
        OpenApiDocumentationConfiguration.class,
})
public @interface EnableCustomSwagger2 {

}
