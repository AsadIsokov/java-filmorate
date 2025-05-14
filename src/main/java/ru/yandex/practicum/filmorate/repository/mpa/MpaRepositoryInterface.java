package ru.yandex.practicum.filmorate.repository.mpa;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

public interface MpaRepositoryInterface {
    List<Mpa> getAll();

    Mpa getById(int id);
}