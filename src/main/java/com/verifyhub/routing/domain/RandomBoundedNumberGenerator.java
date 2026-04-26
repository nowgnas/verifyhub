package com.verifyhub.routing.domain;

@FunctionalInterface
public interface RandomBoundedNumberGenerator {

    int nextInt(int bound);
}
