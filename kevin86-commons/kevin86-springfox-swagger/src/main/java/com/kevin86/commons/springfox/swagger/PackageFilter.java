package com.kevin86.commons.springfox.swagger;

import springfox.documentation.spi.service.contexts.ApiSelector;

import java.util.List;
import java.util.function.Predicate;

/**
 * @Description:
 * @Author: kevin chen
 * @Create: 2020/02/07 23:48
 */
public class PackageFilter {

    private static Predicate<String> pathSelector = ApiSelector.DEFAULT.getPathSelector();

    public static Predicate<String> add(List<Predicate<String>> predicates){
        predicates.parallelStream().forEach(stringPredicate -> {
            pathSelector.and(stringPredicate);
        });
        return pathSelector;
    }

}
