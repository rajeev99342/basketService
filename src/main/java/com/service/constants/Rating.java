package com.service.constants;

public enum Rating {
    ONE_START(1),
    TWO_START(2),
    THREE_START(3),
    FOUR_START(4),
    FIVE_START(5);

    private int start ;

    Rating(int i) {
        this.start = i;
    }
}
