package com.bspark.in_proc.infrastructure.converter.tsc.wrapper.phase;

import java.util.List;

public class PhaseDataLine {

    private List<Integer> A;
    private List<Integer> B;

    public PhaseDataLine(List<Integer> A, List<Integer> B) {
        this.A = A;
        this.B = B;
    }
}
