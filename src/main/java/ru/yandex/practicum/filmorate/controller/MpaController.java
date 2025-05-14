package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.repository.mpa.MpaRepositoryInterface;
import ru.yandex.practicum.filmorate.model.Mpa;


import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
@Slf4j
public class MpaController {
    private final MpaRepositoryInterface mpaRepository;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Mpa> getAllRatings() {
        log.info("Получен запрос на получение списка всех рейтингов");
        return mpaRepository.getAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mpa getRatingById(@PathVariable int id) {
        log.info("Получен запрос на получение рейтинга с id={}", id);
        return mpaRepository.getById(id);
    }
}