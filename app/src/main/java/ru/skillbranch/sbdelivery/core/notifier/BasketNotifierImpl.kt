package ru.skillbranch.sbdelivery.core.notifier

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.ReplaySubject
import ru.skillbranch.sbdelivery.core.notifier.event.BasketEvent

class BasketNotifierImpl : BasketNotifier {

    private val dishesSubject = ReplaySubject.create<BasketEvent>()

    override fun eventSubscribe(): Observable<BasketEvent> = dishesSubject

    override fun putDishes(dish: BasketEvent.AddDish) {
        dishesSubject.onNext(dish)
    }

}