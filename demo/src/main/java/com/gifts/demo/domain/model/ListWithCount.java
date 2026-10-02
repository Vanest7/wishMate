package com.gifts.demo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ListWithCount<T> {

    private List<T> list;
    private long count;
}