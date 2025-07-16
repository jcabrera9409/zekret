package com.zekret.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.zekret.repo.IGenericRepo;
import com.zekret.service.ICRUD;

public abstract class CRUDImpl<T, ID> implements ICRUD<T, ID> {
    protected abstract IGenericRepo<T, ID> getRepo();
    

    @Override
    public T register(T t) {
        return getRepo().save(t);
    }

    @Override
    public T modify(T t) {
        return getRepo().save(t);
    }

    @Override
    public void delete(ID id) {
        getRepo().deleteById(id);
    }

    @Override
    public Optional<T> getById(ID id) {
        return getRepo().findById(id);
    }

    @Override
    public List<T> getAll() {
        return getRepo().findAll();
    }

    @Override
    public Page<T> filter(Pageable pageable) {
        return getRepo().findAll(pageable);
    }
}