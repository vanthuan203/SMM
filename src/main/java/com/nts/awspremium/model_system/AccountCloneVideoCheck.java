package com.nts.awspremium.model_system;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountCloneVideoCheck {
    private List<Long> value;

    public List<Long> getValue() {
        return value;
    }

    public void setValue(List<Long> value) {
        this.value = value;
    }
}
