package ru.skillbranch.sbdelivery.domain.filter

import io.reactivex.rxjava3.core.Single
import ru.skillbranch.sbdelivery.domain.entity.DishEntity
import ru.skillbranch.sbdelivery.repository.DishesRepositoryContract
import ru.skillbranch.sbdelivery.repository.error.EmptyDishesError

class CategoriesFilterUseCase(private val repository: DishesRepositoryContract) : CategoriesFilter {

    override fun categoryFilterDishes(categoryId: String): Single<List<DishEntity>> {
        return repository.getCachedDishes()
            .map { dishes ->
                if(categoryId.isEmpty()) dishes
                else dishes.filter { it.categoryId == categoryId }
            }
            .flatMap { dishes ->
                if(dishes.isEmpty()) Single.error(EmptyDishesError("В этой категории нет блюд"))
                else Single.just(dishes)
            }
    }

}