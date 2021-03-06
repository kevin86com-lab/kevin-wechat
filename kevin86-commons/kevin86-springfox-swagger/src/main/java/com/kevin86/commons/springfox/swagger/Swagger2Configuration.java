package com.kevin86.commons.springfox.swagger;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import javax.annotation.Resource;
import java.util.*;
import java.util.function.Predicate;

/**
 *
 * @author kevinchen
 * @date 2016/12/14
 */
@Configuration
@EnableConfigurationProperties(ApiInfoProperties.class)
@ConditionalOnProperty(name = "swagger2.enabled", matchIfMissing = true)
public class Swagger2Configuration {

    private static final String DEFAULT_DOCKET = "default";

    @Resource
    private BeanFactory beanFactory;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "swagger2.enabled", matchIfMissing = true)
    public List<Docket> createRestApi(ApiInfoProperties apiInfoProperties) {
        ConfigurableBeanFactory configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
        List<Docket> docketList = new LinkedList<>();
        if (apiInfoProperties==null){
            return createRestApiWhenEmptyProperties(configurableBeanFactory,docketList);
        }
        if (apiInfoProperties.isEmptyGroup()){
            apiInfoProperties.constructGroupDocket();
        }
        Map<String, ApiInfoProperties.DocketInfo> groupDocket = apiInfoProperties.getGroupDocket();
        for(Iterator<Map.Entry<String, ApiInfoProperties.DocketInfo>> iterator = groupDocket.entrySet().iterator();
        iterator.hasNext();){
            Map.Entry<String, ApiInfoProperties.DocketInfo> docketInfoEntry = iterator.next();
            String groupName = docketInfoEntry.getKey();
            ApiInfoProperties.DocketInfo docketInfo = docketInfoEntry.getValue();
            List<Predicate<String>> basePath = getBasePath(docketInfo);
            List<Predicate<String>> excludePath = getExcludePath(docketInfo);
            Docket docket = buildDocket(apiInfoProperties, docketInfo, groupName, basePath, excludePath);
            configurableBeanFactory.registerSingleton(groupName, docket);
            docketList.add(docket);
        }
        return docketList;
    }

    /**
     * 通过docketInfo构造ApiInfo实体
     * @param docketInfo
     * @return
     */
    private ApiInfo buildApiInfo(ApiInfoProperties.DocketInfo docketInfo){
        return docketInfo==null ? ApiInfo.DEFAULT :
                new ApiInfoBuilder().title(docketInfo.getTitle())
                .description(docketInfo.getDescription())
                .version(docketInfo.getVersion())
                .license(docketInfo.getLicense())
                .licenseUrl(docketInfo.getLicenseUrl())
                .contact(docketInfo.getServiceContact())
                .termsOfServiceUrl(docketInfo.getTermsOfServiceUrl())
                .build();
    }

    /**
     * 当ApiInfoProperties为NULL的情况下 构造分组文档对象
     * @param configurableBeanFactory
     * @param docketList
     * @return
     */
    private List<Docket> createRestApiWhenEmptyProperties(ConfigurableBeanFactory configurableBeanFactory,
                                                          List<Docket> docketList){
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(ApiInfo.DEFAULT).select().build();
        configurableBeanFactory.registerSingleton("defaultDocket", docket);
        docketList.add(docket);
        return docketList;
    }

    /**
     * 处理basePath
     * @param docketInfo
     * @return
     */
    private List<Predicate<String>> getBasePath(ApiInfoProperties.DocketInfo docketInfo){
        if (docketInfo.getBasePath().isEmpty()) {
            docketInfo.getBasePath().add("/**");
        }
        List<Predicate<String>> basePath = new ArrayList();
        for (String path : docketInfo.getBasePath()) {
            basePath.add(PathSelectors.ant(path));
        }
        return basePath;
    }

    /**
     * 处理excludePath
     * @param docketInfo
     * @return
     */
    private List<Predicate<String>> getExcludePath(ApiInfoProperties.DocketInfo docketInfo){
        List<Predicate<String>> excludePath = new ArrayList();
        for (String path : docketInfo.getExcludePath()) {
            excludePath.add(PathSelectors.ant(path));
        }
        return excludePath;
    }


    /**
     * 构造单个Docket对象实体
     * @param apiInfoProperties
     * @param docketInfo
     * @param groupName
     * @param basePath
     * @param excludePath
     * @return
     */
    private Docket buildDocket(ApiInfoProperties apiInfoProperties,ApiInfoProperties.DocketInfo docketInfo,
                               String groupName,List<Predicate<String>> basePath,
                               List<Predicate<String>> excludePath){
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .host(apiInfoProperties.getHost())
                .apiInfo(buildApiInfo(docketInfo))
                .select().apis(RequestHandlerSelectors.basePackage(docketInfo.getBasePackages()))
                .paths(PackageFilter.add(excludePath))
                .paths(PackageFilter.add(basePath)).build();
        if (!DEFAULT_DOCKET.equals(groupName)){
            docket.groupName(groupName);
        }
        if (!StringUtils.isEmpty(docketInfo.getKeyName())){
            docket.securitySchemes(securitySchemes(docketInfo.getKeyName()));
        }
        return docket;
    }

    /**
     * 权限验证输入框
     * @param keyname
     * @return
     */
    private List<SecurityScheme> securitySchemes(String keyname) {
        return Arrays.asList(new ApiKey("", keyname, "header"));
    }

    /**
     * 屏蔽额外的model层
     * @return
     */
    @Bean
    public UiConfiguration uiConfig() {
        return UiConfigurationBuilder.builder().defaultModelsExpandDepth(-1).displayRequestDuration(true).build();
    }
}
