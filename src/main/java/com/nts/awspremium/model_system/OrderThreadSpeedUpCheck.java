package com.nts.awspremium.model_system;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderThreadSpeedUpCheck {
    private List<String> value;

    public List<String> getValue() {
        return value;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }
}
