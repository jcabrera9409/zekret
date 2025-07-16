package com.zekret.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICRUD<T, ID> {
    T register(T t);
    T modify(T t);
    void delete(ID id);
    Optional<T> getById(ID id);
    List<T> getAll();
    Page<T> filter(Pageable pageable);
}
