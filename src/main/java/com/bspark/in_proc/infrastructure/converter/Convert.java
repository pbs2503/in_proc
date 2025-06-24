package com.bspark.in_proc.infrastructure.converter;

@FunctionalInterface
public interface Convert {
    void process(String tsc, byte[] data, int standard);
}
